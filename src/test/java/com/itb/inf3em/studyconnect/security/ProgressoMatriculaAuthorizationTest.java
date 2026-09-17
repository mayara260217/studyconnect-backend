package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.entity.Aula;
import com.itb.inf3em.studyconnect.model.entity.ProgressoAula;
import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Trilha;
import com.itb.inf3em.studyconnect.model.repository.AulaRepository;
import com.itb.inf3em.studyconnect.model.repository.MatriculaTrilhaRepository;
import com.itb.inf3em.studyconnect.model.repository.ProgressoAulaRepository;
import com.itb.inf3em.studyconnect.model.repository.TrilhaRepository;
import com.itb.inf3em.studyconnect.model.services.ProgressoAulaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProgressoMatriculaAuthorizationTest {

    private final CurrentUser currentUser = new CurrentUser();
    private ProgressoAulaRepository progressoRepository;
    private AulaRepository aulaRepository;
    private TrilhaRepository trilhaRepository;
    private MatriculaTrilhaRepository matriculaRepository;
    private ProgressoAulaService progressoService;

    @BeforeEach
    void setUp() {
        progressoRepository = mock(ProgressoAulaRepository.class);
        aulaRepository = mock(AulaRepository.class);
        trilhaRepository = mock(TrilhaRepository.class);
        matriculaRepository = mock(MatriculaTrilhaRepository.class);
        AlunoAuthorization alunoAuthorization = new AlunoAuthorization(currentUser);
        progressoService = new ProgressoAulaService(
                progressoRepository, aulaRepository, trilhaRepository,
                matriculaRepository, alunoAuthorization);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void authenticate(Long id, TipoUsuario tipo) {
        AuthenticatedUser user = new AuthenticatedUser(id, "user" + id + "@example.com", tipo);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + tipo.name()))));
    }

    private Aula aula(Long aulaId, Long trilhaId) {
        Aula a = new Aula();
        a.setId(aulaId);
        a.setTrilhaId(trilhaId);
        a.setTitulo("Aula teste");
        return a;
    }

    private Trilha trilha(Long trilhaId) {
        Trilha t = new Trilha();
        t.setId(trilhaId);
        return t;
    }

    // ── cenário 1: aluno matriculado conclui aula → permitido ─────────────────

    @Test
    void alunoMatriculadoConcluiAula_permitido() {
        authenticate(10L, TipoUsuario.ALUNO);
        when(aulaRepository.findById(200L)).thenReturn(Optional.of(aula(200L, 100L)));
        when(trilhaRepository.findById(100L)).thenReturn(Optional.of(trilha(100L)));
        when(matriculaRepository.existsByAlunoIdAndTrilhaIdAndAtivoTrue(10L, 100L)).thenReturn(true);
        when(progressoRepository.findByAlunoIdAndAulaId(10L, 200L)).thenReturn(Optional.empty());
        when(progressoRepository.save(any(ProgressoAula.class))).thenAnswer(inv -> inv.getArgument(0));

        ProgressoAula p = progressoService.concluirAula(10L, 200L);

        assertEquals(10L, p.getAlunoId());
        assertTrue(p.isConcluida());
    }

    // ── cenário 2: aluno NÃO matriculado tenta concluir aula → 403 ───────────

    @Test
    void alunoNaoMatriculadoConcluiAula_403() {
        authenticate(10L, TipoUsuario.ALUNO);
        when(aulaRepository.findById(200L)).thenReturn(Optional.of(aula(200L, 100L)));
        when(trilhaRepository.findById(100L)).thenReturn(Optional.of(trilha(100L)));
        when(matriculaRepository.existsByAlunoIdAndTrilhaIdAndAtivoTrue(10L, 100L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> progressoService.concluirAula(10L, 200L));
        assertEquals(403, ex.getStatusCode().value());
    }

    // ── cenário 3: matrícula inativa → 403 ───────────────────────────────────

    @Test
    void matriculaInativaConcluiAula_403() {
        authenticate(10L, TipoUsuario.ALUNO);
        when(aulaRepository.findById(200L)).thenReturn(Optional.of(aula(200L, 100L)));
        when(trilhaRepository.findById(100L)).thenReturn(Optional.of(trilha(100L)));
        // existsByAlunoIdAndTrilhaIdAndAtivoTrue retorna false para matrícula inativa
        when(matriculaRepository.existsByAlunoIdAndTrilhaIdAndAtivoTrue(10L, 100L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> progressoService.concluirAula(10L, 200L));
        assertEquals(403, ex.getStatusCode().value());
    }

    // ── cenário 4: matriculado em trilha A tenta concluir aula de trilha B → 403

    @Test
    void alunoMatriculadoEmTrilhaATentaConcluirAulaDeTrilhaB_403() {
        authenticate(10L, TipoUsuario.ALUNO);
        // aula 300 pertence à trilha 200 (trilha B)
        when(aulaRepository.findById(300L)).thenReturn(Optional.of(aula(300L, 200L)));
        when(trilhaRepository.findById(200L)).thenReturn(Optional.of(trilha(200L)));
        // aluno tem matrícula ativa na trilha 100 (trilha A), mas NÃO na trilha 200
        when(matriculaRepository.existsByAlunoIdAndTrilhaIdAndAtivoTrue(10L, 200L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> progressoService.concluirAula(10L, 300L));
        assertEquals(403, ex.getStatusCode().value());
    }

    // ── cenário 5: alunoId do body é de B → progresso vinculado a A (JWT) ────

    @Test
    void alunoIdManipuladoNoBody_progressoVinculadoAoJwt() {
        authenticate(10L, TipoUsuario.ALUNO);
        when(aulaRepository.findById(200L)).thenReturn(Optional.of(aula(200L, 100L)));
        when(trilhaRepository.findById(100L)).thenReturn(Optional.of(trilha(100L)));
        when(matriculaRepository.existsByAlunoIdAndTrilhaIdAndAtivoTrue(10L, 100L)).thenReturn(true);
        when(progressoRepository.findByAlunoIdAndAulaId(10L, 200L)).thenReturn(Optional.empty());
        when(progressoRepository.save(any(ProgressoAula.class))).thenAnswer(inv -> inv.getArgument(0));

        // body envia alunoId=99 (outro usuário), mas JWT é 10
        ProgressoAula p = progressoService.concluirAula(99L, 200L);

        assertEquals(10L, p.getAlunoId());
        // matrícula verificada para o aluno do JWT (10), não para 99
        verify(matriculaRepository).existsByAlunoIdAndTrilhaIdAndAtivoTrue(10L, 100L);
    }

    // ── cenário 6: aula inexistente → 404 ────────────────────────────────────

    @Test
    void aulaNaoExiste_404() {
        authenticate(10L, TipoUsuario.ALUNO);
        when(aulaRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> progressoService.concluirAula(10L, 999L));
        assertEquals(404, ex.getStatusCode().value());
    }

    // ── cenário 7: aluno consulta próprio progresso → permitido ──────────────

    @Test
    void alunoConsultaProprioProgresso_permitido() {
        authenticate(10L, TipoUsuario.ALUNO);
        when(progressoRepository.findByAlunoIdAndConcluidaTrue(10L)).thenReturn(List.of());

        assertDoesNotThrow(() -> progressoService.getProgressoCompleto(10L));
        assertDoesNotThrow(() -> progressoService.getAulasConcluidas(10L));
    }

    // ── cenário 8: aluno consulta progresso de outro aluno → 403 ─────────────

    @Test
    void alunoConsultaProgressoDeOutroAluno_403() {
        authenticate(10L, TipoUsuario.ALUNO);

        assertThrows(AccessDeniedException.class, () -> progressoService.getProgressoCompleto(20L));
        assertThrows(AccessDeniedException.class, () -> progressoService.getAulasConcluidas(20L));
        assertThrows(AccessDeniedException.class, () -> progressoService.getProgressoTrilha(100L, 20L));
    }

    // ── cenário 9: ADMIN funciona conforme regra atual ────────────────────────
    // ADMIN usa resolveAlunoId que, para ADMIN, aceita o alunoId do body.
    // A verificação de matrícula usa o alunoId resolvido (do body para ADMIN).

    @Test
    void adminRegistraProgressoComMatriculaAtiva_permitido() {
        authenticate(99L, TipoUsuario.ADMIN);
        when(aulaRepository.findById(200L)).thenReturn(Optional.of(aula(200L, 100L)));
        when(trilhaRepository.findById(100L)).thenReturn(Optional.of(trilha(100L)));
        // ADMIN passa alunoId=20 no body → matrícula verificada para 20
        when(matriculaRepository.existsByAlunoIdAndTrilhaIdAndAtivoTrue(20L, 100L)).thenReturn(true);
        when(progressoRepository.findByAlunoIdAndAulaId(20L, 200L)).thenReturn(Optional.empty());
        when(progressoRepository.save(any(ProgressoAula.class))).thenAnswer(inv -> inv.getArgument(0));

        ProgressoAula p = progressoService.concluirAula(20L, 200L);

        assertEquals(20L, p.getAlunoId());
    }

    // ── cenário 10: ausência de JWT → 401 (AccessDeniedException) ────────────

    @Test
    void semJwt_concluirAula_401() {
        assertThrows(AccessDeniedException.class,
                () -> progressoService.concluirAula(10L, 200L));
    }
}
