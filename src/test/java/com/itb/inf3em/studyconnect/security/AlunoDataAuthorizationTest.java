package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.dto.PerfilAprendizadoDTO;
import com.itb.inf3em.studyconnect.model.entity.Aula;
import com.itb.inf3em.studyconnect.model.entity.MatriculaTrilha;
import com.itb.inf3em.studyconnect.model.entity.PerfilAprendizado;
import com.itb.inf3em.studyconnect.model.entity.ProgressoAula;
import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Trilha;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.repository.AulaRepository;
import com.itb.inf3em.studyconnect.model.repository.DuvidaRepository;
import com.itb.inf3em.studyconnect.model.repository.MatriculaTrilhaRepository;
import com.itb.inf3em.studyconnect.model.repository.PerfilAprendizadoRepository;
import com.itb.inf3em.studyconnect.model.repository.ProgressoAulaRepository;
import com.itb.inf3em.studyconnect.model.repository.TrilhaRepository;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import com.itb.inf3em.studyconnect.model.services.MatriculaTrilhaService;
import com.itb.inf3em.studyconnect.model.services.PerfilAprendizadoService;
import com.itb.inf3em.studyconnect.model.services.ProgressoAulaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlunoDataAuthorizationTest {

    private final CurrentUser currentUser = new CurrentUser();
    private MatriculaTrilhaRepository matriculaRepository;
    private ProgressoAulaRepository progressoRepository;
    private PerfilAprendizadoRepository perfilRepository;
    private UsuarioRepository usuarioRepository;
    private TrilhaRepository trilhaRepository;
    private AulaRepository aulaRepository;
    private MatriculaTrilhaService matriculaService;
    private ProgressoAulaService progressoService;
    private PerfilAprendizadoService perfilService;

    @BeforeEach
    void setUp() {
        matriculaRepository = mock(MatriculaTrilhaRepository.class);
        progressoRepository = mock(ProgressoAulaRepository.class);
        perfilRepository = mock(PerfilAprendizadoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        trilhaRepository = mock(TrilhaRepository.class);
        aulaRepository = mock(AulaRepository.class);
        AlunoAuthorization alunoAuthorization = new AlunoAuthorization(currentUser);
        TrilhaAuthorization trilhaAuthorization = new TrilhaAuthorization(currentUser);
        matriculaService = new MatriculaTrilhaService(matriculaRepository, usuarioRepository, trilhaRepository,
                aulaRepository, progressoRepository, mock(DuvidaRepository.class), alunoAuthorization, trilhaAuthorization);
        progressoService = new ProgressoAulaService(progressoRepository, aulaRepository, trilhaRepository,
                matriculaRepository, alunoAuthorization);
        perfilService = new PerfilAprendizadoService(perfilRepository, alunoAuthorization);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void alunoACriaMatriculaParaSiMesmoMesmoComAlunoIdManipulado() {
        authenticate(10L, TipoUsuario.ALUNO);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario(10L)));
        when(trilhaRepository.findById(100L)).thenReturn(Optional.of(trilha(100L)));
        when(matriculaRepository.findByAlunoIdAndTrilhaId(10L, 100L)).thenReturn(Optional.empty());
        when(matriculaRepository.save(any(MatriculaTrilha.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MatriculaTrilha matricula = matriculaService.matricular(20L, 100L);

        assertEquals(10L, matricula.getAlunoId());
    }

    @Test
    void alunoANaoConsultaNemCancelaMatriculaDeB() {
        authenticate(10L, TipoUsuario.ALUNO);

        assertThrows(AccessDeniedException.class, () -> matriculaService.listarPorAluno(20L));
        assertThrows(AccessDeniedException.class, () -> matriculaService.desmatricular(20L, 100L));
    }

    @Test
    void alunoARegistraProgressoProprioEIgnoraAlunoIdManipulado() {
        authenticate(10L, TipoUsuario.ALUNO);
        Aula aula = new Aula();
        aula.setTrilhaId(100L);
        when(aulaRepository.findById(200L)).thenReturn(Optional.of(aula));
        when(trilhaRepository.findById(100L)).thenReturn(Optional.of(trilha(100L)));
        when(matriculaRepository.existsByAlunoIdAndTrilhaIdAndAtivoTrue(10L, 100L)).thenReturn(true);
        when(progressoRepository.findByAlunoIdAndAulaId(10L, 200L)).thenReturn(Optional.empty());
        when(progressoRepository.save(any(ProgressoAula.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProgressoAula progresso = progressoService.concluirAula(20L, 200L);

        assertEquals(10L, progresso.getAlunoId());
    }

    @Test
    void alunoANaoConsultaProgressoDeB() {
        authenticate(10L, TipoUsuario.ALUNO);

        assertThrows(AccessDeniedException.class, () -> progressoService.getProgressoCompleto(20L));
        assertThrows(AccessDeniedException.class, () -> progressoService.getProgressoTrilha(100L, 20L));
    }

    @Test
    void alunoAConsultaECriaPerfilProprioMesmoComAlunoIdManipulado() {
        authenticate(10L, TipoUsuario.ALUNO);
        PerfilAprendizadoDTO dto = new PerfilAprendizadoDTO();
        dto.setAlunoId(20L);
        dto.setObjetivo("ENEM");
        when(perfilRepository.findByAlunoId(10L)).thenReturn(Optional.empty());
        when(perfilRepository.save(any(PerfilAprendizado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PerfilAprendizado perfil = perfilService.create(dto);

        assertEquals(10L, perfil.getAlunoId());
        when(perfilRepository.findByAlunoId(10L)).thenReturn(Optional.of(perfil));
        assertEquals(10L, perfilService.findByAluno(10L).orElseThrow().getAlunoId());
    }

    @Test
    void alunoAPodeEditarProprioPerfilMasNaoPerfilDeB() {
        authenticate(10L, TipoUsuario.ALUNO);
        PerfilAprendizado proprio = new PerfilAprendizado();
        proprio.setAlunoId(10L);
        when(perfilRepository.findByAlunoId(10L)).thenReturn(Optional.of(proprio));
        when(perfilRepository.save(any(PerfilAprendizado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PerfilAprendizadoDTO dto = new PerfilAprendizadoDTO();
        dto.setObjetivo("FACULDADE");
        PerfilAprendizado atualizado = perfilService.update(10L, dto);

        assertEquals(10L, atualizado.getAlunoId());
        assertThrows(AccessDeniedException.class, () -> perfilService.findByAluno(20L));
        assertThrows(AccessDeniedException.class, () -> perfilService.update(20L, new PerfilAprendizadoDTO()));
    }

    @Test
    void adminPodeAdministrarDadosDeOutroAluno() {
        authenticate(99L, TipoUsuario.ADMIN);
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(usuario(20L)));
        when(trilhaRepository.findById(100L)).thenReturn(Optional.of(trilha(100L)));
        when(matriculaRepository.findByAlunoIdAndTrilhaId(20L, 100L)).thenReturn(Optional.empty());
        when(matriculaRepository.save(any(MatriculaTrilha.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PerfilAprendizado perfilDeB = new PerfilAprendizado();
        perfilDeB.setAlunoId(20L);
        when(perfilRepository.findByAlunoId(20L)).thenReturn(Optional.of(perfilDeB));

        assertEquals(20L, matriculaService.matricular(20L, 100L).getAlunoId());
        assertEquals(20L, perfilService.findByAluno(20L).orElseThrow().getAlunoId());
    }

    @Test
    void ausenciaDeIdentidadeBloqueiaMutacoesELeiturasPrivadas() {
        assertThrows(AccessDeniedException.class, () -> matriculaService.matricular(10L, 100L));
        assertThrows(AccessDeniedException.class, () -> progressoService.concluirAula(10L, 200L));
        assertThrows(AccessDeniedException.class, () -> perfilService.findByAluno(10L));
    }

    private void authenticate(Long usuarioId, TipoUsuario tipoUsuario) {
        AuthenticatedUser user = new AuthenticatedUser(usuarioId, "usuario@example.com", tipoUsuario);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + tipoUsuario.name()))));
    }

    private Usuario usuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        return usuario;
    }

    private Trilha trilha(Long id) {
        Trilha trilha = new Trilha();
        trilha.setId(id);
        return trilha;
    }
}
