package com.itb.inf3em.studyconnect.service;

import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Trilha;
import com.itb.inf3em.studyconnect.model.entity.Turma;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.repository.TrilhaRepository;
import com.itb.inf3em.studyconnect.model.repository.TurmaRepository;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import com.itb.inf3em.studyconnect.model.services.TrilhaService;
import com.itb.inf3em.studyconnect.model.services.TurmaService;
import com.itb.inf3em.studyconnect.security.AuthenticatedUser;
import com.itb.inf3em.studyconnect.security.CurrentUser;
import com.itb.inf3em.studyconnect.security.TrilhaAuthorization;
import com.itb.inf3em.studyconnect.security.TurmaAuthorization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Garante que DataIntegrityViolationException não expõe detalhes internos do banco no response.
 */
class DataIntegrityExposureTest {

    private static final String CONSTRAINT_MSG =
            "Violation of UNIQUE KEY constraint 'UQ_Trilha_nome'. "
            + "The duplicate key value is (Matemática). "
            + "The statement has been terminated.";

    private static final String SQL_MSG =
            "INSERT INTO Turma (nome, codigo, professor_id) VALUES (?, ?, ?) "
            + "-- com.microsoft.sqlserver.jdbc.SQLServerException";

    private final CurrentUser currentUser = new CurrentUser();
    private TrilhaRepository trilhaRepository;
    private TurmaRepository turmaRepository;
    private UsuarioRepository usuarioRepository;
    private TrilhaService trilhaService;
    private TurmaService turmaService;

    @BeforeEach
    void setUp() {
        trilhaRepository = mock(TrilhaRepository.class);
        turmaRepository = mock(TurmaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        trilhaService = new TrilhaService(trilhaRepository, usuarioRepository, new TrilhaAuthorization(currentUser));
        turmaService = new TurmaService(turmaRepository, usuarioRepository, new TurmaAuthorization(currentUser));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ── Cenário 1: DataIntegrityViolationException em Trilha não expõe mensagem interna ──

    @Test
    void trilha_dataIntegrityViolation_naoExpoeMensagemInterna() {
        authenticate(10L, TipoUsuario.PROFESSOR);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(professor(10L)));
        when(trilhaRepository.save(any(Trilha.class)))
                .thenThrow(new DataIntegrityViolationException(CONSTRAINT_MSG));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> trilhaService.createTrilha(trilhaValida()));

        String reason = ex.getReason();
        assertFalse(reason.contains("UQ_Trilha"), "Nome de constraint não deve ser exposto");
        assertFalse(reason.contains("Matemática"), "Valor do banco não deve ser exposto");
        assertFalse(reason.contains("duplicate key"), "Detalhe SQL não deve ser exposto");
        assertFalse(reason.contains("INSERT"), "SQL não deve ser exposto");
        assertFalse(reason.contains("DataIntegrityViolationException"), "Nome da exception não deve ser exposto");
    }

    // ── Cenário 2: DataIntegrityViolationException em Turma não expõe mensagem interna ──

    @Test
    void turma_dataIntegrityViolation_naoExpoeMensagemInterna() {
        authenticate(10L, TipoUsuario.PROFESSOR);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(professor(10L)));
        when(turmaRepository.existsByCodigo(any())).thenReturn(false);
        when(turmaRepository.save(any(Turma.class)))
                .thenThrow(new DataIntegrityViolationException(SQL_MSG));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> turmaService.createTurma(turmaValida()));

        String reason = ex.getReason();
        assertFalse(reason.contains("INSERT"), "SQL não deve ser exposto");
        assertFalse(reason.contains("professor_id"), "Nome de coluna não deve ser exposto");
        assertFalse(reason.contains("SQLServerException"), "Nome da exception não deve ser exposto");
        assertFalse(reason.contains("jdbc"), "Detalhe de conexão não deve ser exposto");
        assertFalse(reason.contains("DataIntegrityViolationException"), "Nome da exception não deve ser exposto");
    }

    // ── Cenário 3: response não contém nenhum detalhe interno ────────────────

    @Test
    void trilha_responseNaoContemDetalhesInternos() {
        authenticate(10L, TipoUsuario.PROFESSOR);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(professor(10L)));
        when(trilhaRepository.save(any(Trilha.class)))
                .thenThrow(new DataIntegrityViolationException(CONSTRAINT_MSG));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> trilhaService.createTrilha(trilhaValida()));

        String reason = ex.getReason();
        // nenhum fragmento do CONSTRAINT_MSG deve aparecer
        assertFalse(reason.contains("UNIQUE KEY"), "Detalhe de constraint não deve ser exposto");
        assertFalse(reason.contains("The duplicate"), "Mensagem do banco não deve ser exposta");
        assertFalse(reason.contains("The statement"), "Mensagem do banco não deve ser exposta");
    }

    // ── Cenário 4: status HTTP 500 é preservado ───────────────────────────────

    @Test
    void statusHttp500_preservado_emDataIntegrityViolation() {
        authenticate(10L, TipoUsuario.PROFESSOR);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(professor(10L)));
        when(trilhaRepository.save(any(Trilha.class)))
                .thenThrow(new DataIntegrityViolationException(CONSTRAINT_MSG));

        ResponseStatusException exTrilha = assertThrows(ResponseStatusException.class,
                () -> trilhaService.createTrilha(trilhaValida()));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exTrilha.getStatusCode());

        when(turmaRepository.existsByCodigo(any())).thenReturn(false);
        when(turmaRepository.save(any(Turma.class)))
                .thenThrow(new DataIntegrityViolationException(SQL_MSG));

        ResponseStatusException exTurma = assertThrows(ResponseStatusException.class,
                () -> turmaService.createTurma(turmaValida()));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exTurma.getStatusCode());
    }

    // ── Cenário 5: mensagens legítimas de negócio continuam funcionando ───────

    @Test
    void mensagensDeNegocio_continuamFuncionando() {
        authenticate(10L, TipoUsuario.PROFESSOR);

        // 404 — professor não encontrado
        when(usuarioRepository.findById(10L)).thenReturn(Optional.empty());
        ResponseStatusException ex404 = assertThrows(ResponseStatusException.class,
                () -> trilhaService.createTrilha(trilhaValida()));
        assertEquals(HttpStatus.NOT_FOUND, ex404.getStatusCode());

        // 400 — nome obrigatório
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(professor(10L)));
        Trilha semNome = trilhaValida();
        semNome.setNome(null);
        ResponseStatusException ex400 = assertThrows(ResponseStatusException.class,
                () -> trilhaService.createTrilha(semNome));
        assertEquals(HttpStatus.BAD_REQUEST, ex400.getStatusCode());
        assertEquals("Nome da trilha é obrigatório.", ex400.getReason());

        // 409 — código de turma duplicado
        authenticate(10L, TipoUsuario.PROFESSOR);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(professor(10L)));
        when(turmaRepository.existsByCodigo("COD-DUP")).thenReturn(true);
        Turma turmaDup = turmaValida();
        turmaDup.setCodigo("COD-DUP");
        ResponseStatusException ex409 = assertThrows(ResponseStatusException.class,
                () -> turmaService.createTurma(turmaDup));
        assertEquals(HttpStatus.CONFLICT, ex409.getStatusCode());
        assertEquals("Este código de turma já está em uso.", ex409.getReason());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void authenticate(Long usuarioId, TipoUsuario tipoUsuario) {
        AuthenticatedUser user = new AuthenticatedUser(usuarioId, "usuario@example.com", tipoUsuario);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + tipoUsuario.name()))));
    }

    private Usuario professor(Long id) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome("Professor Teste");
        return u;
    }

    private Trilha trilhaValida() {
        Trilha t = new Trilha();
        t.setNome("Matemática");
        t.setTipo("PUBLICA");
        t.setNivel("BASICO");
        return t;
    }

    private Turma turmaValida() {
        Turma t = new Turma();
        t.setNome("Turma Teste");
        t.setCodigo("COD-001");
        return t;
    }
}
