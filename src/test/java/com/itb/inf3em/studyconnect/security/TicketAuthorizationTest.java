package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.dto.TicketDTO;
import com.itb.inf3em.studyconnect.model.entity.Ticket;
import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.repository.TicketRepository;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import com.itb.inf3em.studyconnect.model.services.EmailService;
import com.itb.inf3em.studyconnect.model.services.TicketService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TicketAuthorizationTest {

    private final CurrentUser currentUser = new CurrentUser();
    private TicketRepository ticketRepository;
    private UsuarioRepository usuarioRepository;
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        ticketService = new TicketService(ticketRepository, usuarioRepository, mock(EmailService.class));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void authenticate(Long id, TipoUsuario tipo) {
        AuthenticatedUser user = new AuthenticatedUser(id, "user" + id + "@example.com", tipo);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + tipo.name()))));
    }

    private Usuario usuario(Long id, String nome, String email) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome(nome);
        u.setEmail(email);
        u.setTipoUsuario(TipoUsuario.ALUNO);
        u.setAtivo(true);
        return u;
    }

    private Ticket ticketSalvo(Long id, Long usuarioId) {
        Ticket t = new Ticket();
        t.setUsuarioId(usuarioId);
        t.setTipo("DUVIDA");
        t.setMensagem("mensagem");
        t.setStatus("ABERTO");
        return t;
    }

    private Map<String, Object> body() {
        return Map.of("tipo", "DUVIDA", "mensagem", "preciso de ajuda");
    }

    // ── cenário 1: usuário A cria ticket → ticket fica vinculado a A ─────────

    @Test
    void usuarioACriaTicket_ficaVinculadoA() {
        authenticate(10L, TipoUsuario.ALUNO);
        Usuario a = usuario(10L, "Alice", "alice@example.com");
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(a));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketDTO dto = ticketService.criar(body(), currentUser.require());

        assertEquals(10L, dto.getUsuarioId());
        assertEquals("alice@example.com", dto.getEmail());
        assertEquals("Alice", dto.getNome());
    }

    // ── cenário 2: usuário A envia usuarioId de B → ticket continua vinculado a A

    @Test
    void usuarioAEnviaUsuarioIdDeB_ticketVinculadoA() {
        authenticate(10L, TipoUsuario.ALUNO);
        Usuario a = usuario(10L, "Alice", "alice@example.com");
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(a));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // body contém usuarioId de B — deve ser ignorado
        Map<String, Object> bodyComIdDeB = Map.of(
                "tipo", "DUVIDA",
                "mensagem", "preciso de ajuda",
                "usuarioId", 99L
        );

        TicketDTO dto = ticketService.criar(bodyComIdDeB, currentUser.require());

        assertEquals(10L, dto.getUsuarioId());
    }

    // ── cenário 3: usuário A envia nome/e-mail de B → sistema usa dados de A ─

    @Test
    void usuarioAEnviaNomeEmailDeB_sistemaUsaDadosDeA() {
        authenticate(10L, TipoUsuario.ALUNO);
        Usuario a = usuario(10L, "Alice", "alice@example.com");
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(a));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> bodyComDadosDeB = Map.of(
                "tipo", "DUVIDA",
                "mensagem", "preciso de ajuda",
                "nome", "Bob",
                "email", "bob@example.com"
        );

        TicketDTO dto = ticketService.criar(bodyComDadosDeB, currentUser.require());

        assertEquals("Alice", dto.getNome());
        assertEquals("alice@example.com", dto.getEmail());
    }

    // ── cenário 4: usuário A consulta próprio ticket → permitido ─────────────

    @Test
    void usuarioAConsultaProprioTicket_permitido() {
        authenticate(10L, TipoUsuario.ALUNO);
        Ticket t = ticketSalvo(1L, 10L);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(t));

        TicketDTO dto = ticketService.buscarPorId(1L);
        // guarda de ownership: mesmo usuário → não lança exceção
        assertDoesNotThrow(() -> currentUser.requireSameUserOrAdmin(dto.getUsuarioId()));
        assertEquals(10L, dto.getUsuarioId());
    }

    // ── cenário 5: usuário A consulta ticket de B → 403 ──────────────────────

    @Test
    void usuarioAConsultaTicketDeB_403() {
        authenticate(10L, TipoUsuario.ALUNO);
        Ticket t = ticketSalvo(2L, 99L); // dono é 99
        when(ticketRepository.findById(2L)).thenReturn(Optional.of(t));

        TicketDTO dto = ticketService.buscarPorId(2L);
        assertThrows(AccessDeniedException.class,
                () -> currentUser.requireSameUserOrAdmin(dto.getUsuarioId()));
    }

    // ── cenário 6: sem JWT tentando criar ticket → 401 ───────────────────────

    @Test
    void semJwt_criarTicket_401() {
        // SecurityContext vazio → currentUser.require() lança AccessDeniedException
        assertThrows(AccessDeniedException.class, () -> currentUser.require());
    }

    // ── cenário 7: usuário comum tentando responder ticket → 403 ─────────────

    @Test
    void usuarioComum_responderTicket_403() {
        authenticate(10L, TipoUsuario.ALUNO);
        assertThrows(AccessDeniedException.class, () -> currentUser.requireAdmin());
    }

    // ── cenário 8: ADMIN consegue listar tickets ──────────────────────────────

    @Test
    void admin_listarTickets_permitido() {
        authenticate(99L, TipoUsuario.ADMIN);
        Ticket t1 = ticketSalvo(1L, 10L);
        Ticket t2 = ticketSalvo(2L, 20L);
        when(ticketRepository.findAllByOrderByCriadaEmDesc()).thenReturn(List.of(t1, t2));

        // admin pode chamar requireAdmin sem exceção
        assertDoesNotThrow(() -> currentUser.requireAdmin());
        List<TicketDTO> lista = ticketService.listarTodos();
        assertEquals(2, lista.size());
    }

    // ── cenário 9: ADMIN consegue responder ──────────────────────────────────

    @Test
    void admin_responderTicket_permitido() {
        authenticate(99L, TipoUsuario.ADMIN);
        Ticket t = ticketSalvo(1L, 10L);
        t.setEmail("alice@example.com");
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(t));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> currentUser.requireAdmin());
        TicketDTO dto = ticketService.responder(1L, "Resposta da equipe.");
        assertEquals("RESPONDIDO", dto.getStatus());
    }

    // ── cenário 10: ADMIN consegue fechar ────────────────────────────────────

    @Test
    void admin_fecharTicket_permitido() {
        authenticate(99L, TipoUsuario.ADMIN);
        Ticket t = ticketSalvo(1L, 10L);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(t));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> currentUser.requireAdmin());
        TicketDTO dto = ticketService.fechar(1L);
        assertEquals("FECHADO", dto.getStatus());
    }
}
