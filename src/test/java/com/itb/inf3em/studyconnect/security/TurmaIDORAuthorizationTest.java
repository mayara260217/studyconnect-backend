package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Turma;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.repository.TurmaRepository;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import com.itb.inf3em.studyconnect.model.services.TurmaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testes de IDOR de leitura em turmas.
 * Cobre os 13 cenários obrigatórios definidos na correção de segurança.
 */
class TurmaIDORAuthorizationTest {

    private final CurrentUser currentUser = new CurrentUser();
    private TurmaRepository turmaRepository;
    private UsuarioRepository usuarioRepository;
    private TurmaService turmaService;
    private TurmaAuthorization turmaAuthorization;

    @BeforeEach
    void setUp() {
        turmaRepository = mock(TurmaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        turmaAuthorization = new TurmaAuthorization(currentUser);
        turmaService = new TurmaService(turmaRepository, usuarioRepository, turmaAuthorization);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ── Cenário 1: PROFESSOR acessa as próprias turmas por /turmas/professor/{id} → permitido ──

    @Test
    void professorAcessaPropriasTurmasPorProfessorId_permitido() {
        authenticate(30L, TipoUsuario.PROFESSOR);
        when(turmaRepository.findByProfessorId(30L)).thenReturn(List.of(turmaPrivada(30L)));

        assertDoesNotThrow(() -> turmaService.getTurmasByProfessor(30L));
    }

    // ── Cenário 2: PROFESSOR tenta /turmas/professor/{id} de outro professor → 403 ──

    @Test
    void professorTentaAcessarTurmasDeOutroProfessor_403() {
        authenticate(30L, TipoUsuario.PROFESSOR);

        assertThrows(AccessDeniedException.class, () -> turmaService.getTurmasByProfessor(40L));
    }

    // ── Cenário 3: ALUNO tenta /turmas/professor/{id} → 403 ──

    @Test
    void alunoTentaAcessarTurmasPorProfessorId_403() {
        authenticate(10L, TipoUsuario.ALUNO);

        assertThrows(AccessDeniedException.class, () -> turmaService.getTurmasByProfessor(30L));
    }

    // ── Cenário 4: ADMIN acessa /turmas/professor/{id} de qualquer professor → permitido ──

    @Test
    void adminAcessaTurmasDeQualquerProfessor_permitido() {
        authenticate(99L, TipoUsuario.ADMIN);
        when(turmaRepository.findByProfessorId(30L)).thenReturn(List.of(turmaPrivada(30L)));
        when(turmaRepository.findByProfessorId(40L)).thenReturn(List.of(turmaPrivada(40L)));

        assertDoesNotThrow(() -> turmaService.getTurmasByProfessor(30L));
        assertDoesNotThrow(() -> turmaService.getTurmasByProfessor(40L));
    }

    // ── Cenário 5: PROFESSOR acessa a própria turma por /turmas/{id} → permitido ──

    @Test
    void professorAcessaPropriasTurmasPorId_permitido() {
        authenticate(30L, TipoUsuario.PROFESSOR);
        when(turmaRepository.findById(700L)).thenReturn(Optional.of(turmaPrivada(30L)));

        assertDoesNotThrow(() -> turmaService.getTurmaById(700L));
    }

    // ── Cenário 6: PROFESSOR tenta acessar turma de outro professor → 403 ──

    @Test
    void professorTentaAcessarTurmaPrivadaDeOutroProfessor_403() {
        authenticate(30L, TipoUsuario.PROFESSOR);
        when(turmaRepository.findById(800L)).thenReturn(Optional.of(turmaPrivada(40L)));

        assertThrows(AccessDeniedException.class, () -> turmaService.getTurmaById(800L));
    }

    // ── Cenário 7: ALUNO tenta acessar turma PRIVADA de professor arbitrário → 403 ──

    @Test
    void alunoTentaAcessarTurmaPrivada_403() {
        authenticate(10L, TipoUsuario.ALUNO);
        when(turmaRepository.findById(700L)).thenReturn(Optional.of(turmaPrivada(30L)));

        assertThrows(AccessDeniedException.class, () -> turmaService.getTurmaById(700L));
    }

    // ── Cenário 8: ADMIN acessa turma PRIVADA de qualquer professor → permitido ──

    @Test
    void adminAcessaTurmaPrivadaDeQualquerProfessor_permitido() {
        authenticate(99L, TipoUsuario.ADMIN);
        when(turmaRepository.findById(700L)).thenReturn(Optional.of(turmaPrivada(30L)));
        when(turmaRepository.findById(800L)).thenReturn(Optional.of(turmaPrivada(40L)));

        assertDoesNotThrow(() -> turmaService.getTurmaById(700L));
        assertDoesNotThrow(() -> turmaService.getTurmaById(800L));
    }

    // ── Cenário 9: ALUNO consegue acessar turmas PÚBLICAS ──

    @Test
    void alunoAcessaTurmaPublica_permitido() {
        authenticate(10L, TipoUsuario.ALUNO);
        when(turmaRepository.findById(900L)).thenReturn(Optional.of(turmaPublica(30L)));

        assertDoesNotThrow(() -> turmaService.getTurmaById(900L));
    }

    // ── Cenário 10: Listagem GET /turmas não expõe turmas PRIVADAS para ALUNO ──

    @Test
    void listagemdeTurmasNaoExpoeTurmasPrivadasParaAluno() {
        authenticate(10L, TipoUsuario.ALUNO);
        Turma publica = turmaPublica(30L);
        Turma privada = turmaPrivada(40L);
        when(turmaRepository.findAll()).thenReturn(List.of(publica, privada));

        List<Turma> resultado = turmaService.getAllTurmas();

        assertTrue(resultado.contains(publica));
        assertFalse(resultado.contains(privada));
    }

    // ── Cenário 11: professorId arbitrário não permite bypass da autorização ──

    @Test
    void professorIdArbitrarioNaoPermiteBypassDeAutorizacao() {
        // PROFESSOR 30 tenta usar professorId=40 (outro professor) → 403
        authenticate(30L, TipoUsuario.PROFESSOR);
        assertThrows(AccessDeniedException.class, () -> turmaService.getTurmasByProfessor(40L));

        // ALUNO 10 tenta usar professorId=30 → 403
        authenticate(10L, TipoUsuario.ALUNO);
        assertThrows(AccessDeniedException.class, () -> turmaService.getTurmasByProfessor(30L));
    }

    // ── Cenário 12: sem JWT → AccessDeniedException (equivalente a 401) ──

    @Test
    void semJwt_bloqueado() {
        // SecurityContext vazio — nenhuma autenticação
        // getTurmasByProfessor chama require() diretamente → lança sem precisar de dados
        assertThrows(AccessDeniedException.class, () -> turmaService.getTurmasByProfessor(30L));

        // getTurmaById chama require() via requireCanReadTurma após carregar a turma
        when(turmaRepository.findById(700L)).thenReturn(Optional.of(turmaPrivada(30L)));
        assertThrows(AccessDeniedException.class, () -> turmaService.getTurmaById(700L));

        // getAllTurmas filtra via canRead que chama require() — precisa de ao menos 1 turma no mock
        when(turmaRepository.findAll()).thenReturn(List.of(turmaPrivada(30L)));
        assertThrows(AccessDeniedException.class, () -> turmaService.getAllTurmas());
    }

    // ── Cenário 13: usuário desativado/inexistente não ganha acesso via token antigo ──

    @Test
    void tokenAntigoDeUsuarioInexistenteNaoGanhaAcesso() {
        // Se o JwtAuthenticationFilter não populou o contexto (token inválido/revogado),
        // o SecurityContext fica vazio → AccessDeniedException.
        SecurityContextHolder.clearContext();

        assertThrows(AccessDeniedException.class, () -> turmaService.getTurmasByProfessor(30L));

        when(turmaRepository.findAll()).thenReturn(List.of(turmaPrivada(30L)));
        assertThrows(AccessDeniedException.class, () -> turmaService.getAllTurmas());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void authenticate(Long usuarioId, TipoUsuario tipoUsuario) {
        AuthenticatedUser user = new AuthenticatedUser(usuarioId, "usuario@example.com", tipoUsuario);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + tipoUsuario.name()))));
    }

    private Turma turmaPrivada(Long professorId) {
        Turma turma = new Turma();
        turma.setProfessorId(professorId);
        turma.setNome("Turma Privada");
        turma.setCodigo("PRIV-" + professorId);
        turma.setTipo("PRIVADA");
        return turma;
    }

    private Turma turmaPublica(Long professorId) {
        Turma turma = new Turma();
        turma.setProfessorId(professorId);
        turma.setNome("Turma Publica");
        turma.setCodigo("PUB-" + professorId);
        turma.setTipo("PUBLICA");
        return turma;
    }
}
