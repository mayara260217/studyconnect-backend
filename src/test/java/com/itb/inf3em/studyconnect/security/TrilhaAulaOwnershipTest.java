package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.entity.Aula;
import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Trilha;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.repository.AulaRepository;
import com.itb.inf3em.studyconnect.model.repository.TrilhaRepository;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import com.itb.inf3em.studyconnect.model.services.AulaService;
import com.itb.inf3em.studyconnect.model.services.TrilhaService;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrilhaAulaOwnershipTest {

    private final CurrentUser currentUser = new CurrentUser();
    private TrilhaRepository trilhaRepository;
    private AulaRepository aulaRepository;
    private UsuarioRepository usuarioRepository;
    private TrilhaService trilhaService;
    private AulaService aulaService;

    @BeforeEach
    void setUp() {
        trilhaRepository = mock(TrilhaRepository.class);
        aulaRepository = mock(AulaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        TrilhaAuthorization authorization = new TrilhaAuthorization(currentUser);
        trilhaService = new TrilhaService(trilhaRepository, usuarioRepository, authorization);
        aulaService = new AulaService(aulaRepository, trilhaRepository, authorization);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void professorACriaTrilhaVinculadaAPropriaIdentidade() {
        authenticate(10L, TipoUsuario.PROFESSOR);
        Usuario professorA = usuario(10L, "Professor A");
        Trilha novaTrilha = trilha(20L);
        novaTrilha.setProfessorId(99L);
        novaTrilha.setProfessorNome("Valor ignorado");
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(professorA));
        when(trilhaRepository.save(any(Trilha.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trilha criada = trilhaService.createTrilha(novaTrilha);

        assertEquals(10L, criada.getProfessorId());
        assertEquals("Professor A", criada.getProfessorNome());
    }

    @Test
    void professorAPodeEditarPropriaTrilha() {
        authenticate(10L, TipoUsuario.PROFESSOR);
        Trilha trilha = trilha(10L);
        when(trilhaRepository.findById(100L)).thenReturn(Optional.of(trilha));
        when(trilhaRepository.save(any(Trilha.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trilha alteracao = new Trilha();
        alteracao.setNome("Nome atualizado");
        trilhaService.updateTrilha(100L, alteracao);

        assertEquals("Nome atualizado", trilha.getNome());
    }

    @Test
    void professorBNaoPodeEditarNemExcluirTrilhaDeA() {
        authenticate(20L, TipoUsuario.PROFESSOR);
        Trilha trilha = trilha(10L);
        when(trilhaRepository.findById(100L)).thenReturn(Optional.of(trilha));

        assertThrows(AccessDeniedException.class, () -> trilhaService.updateTrilha(100L, new Trilha()));
        assertThrows(AccessDeniedException.class, () -> trilhaService.deleteTrilha(100L));
    }

    @Test
    void professorAPodeCriarAulaNaPropriaTrilha() {
        authenticate(10L, TipoUsuario.PROFESSOR);
        when(trilhaRepository.findById(100L)).thenReturn(Optional.of(trilha(10L)));
        when(aulaRepository.save(any(Aula.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Aula criada = aulaService.createAula(aula(100L));

        assertEquals(100L, criada.getTrilhaId());
        verify(aulaRepository).save(criada);
    }

    @Test
    void professorBNaoPodeCriarEditarNemExcluirAulaDaTrilhaDeA() {
        authenticate(20L, TipoUsuario.PROFESSOR);
        Trilha trilhaDeA = trilha(10L);
        Aula aulaDeA = aula(100L);
        when(trilhaRepository.findById(100L)).thenReturn(Optional.of(trilhaDeA));
        when(aulaRepository.findById(500L)).thenReturn(Optional.of(aulaDeA));

        assertThrows(AccessDeniedException.class, () -> aulaService.createAula(aula(100L)));
        assertThrows(AccessDeniedException.class, () -> aulaService.updateAula(500L, new Aula()));
        assertThrows(AccessDeniedException.class, () -> aulaService.deleteAula(500L));
    }

    @Test
    void adminPodeGerenciarTrilhaEAulaDeQualquerProfessor() {
        authenticate(99L, TipoUsuario.ADMIN);
        Trilha trilhaDeA = trilha(10L);
        Aula aulaDeA = aula(100L);
        when(trilhaRepository.findById(100L)).thenReturn(Optional.of(trilhaDeA));
        when(trilhaRepository.save(any(Trilha.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(aulaRepository.findById(500L)).thenReturn(Optional.of(aulaDeA));
        when(aulaRepository.save(any(Aula.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> trilhaService.updateTrilha(100L, new Trilha()));
        assertDoesNotThrow(() -> trilhaService.deleteTrilha(100L));
        assertDoesNotThrow(() -> aulaService.createAula(aula(100L)));
        assertDoesNotThrow(() -> aulaService.updateAula(500L, new Aula()));
        assertDoesNotThrow(() -> aulaService.deleteAula(500L));
    }

    @Test
    void semIdentidadeAutenticadaNaoPodeGerenciarTrilhaOuAula() {
        when(trilhaRepository.findById(100L)).thenReturn(Optional.of(trilha(10L)));

        assertThrows(AccessDeniedException.class, () -> trilhaService.updateTrilha(100L, new Trilha()));
        assertThrows(AccessDeniedException.class, () -> aulaService.createAula(aula(100L)));
    }

    private void authenticate(Long usuarioId, TipoUsuario tipoUsuario) {
        AuthenticatedUser user = new AuthenticatedUser(usuarioId, "usuario@example.com", tipoUsuario);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + tipoUsuario.name()))));
    }

    private Trilha trilha(Long professorId) {
        Trilha trilha = new Trilha();
        trilha.setProfessorId(professorId);
        trilha.setNome("Trilha de teste");
        trilha.setTipo("PUBLICA");
        trilha.setNivel("BASICO");
        return trilha;
    }

    private Aula aula(Long trilhaId) {
        Aula aula = new Aula();
        aula.setTrilhaId(trilhaId);
        aula.setTitulo("Aula de teste");
        return aula;
    }

    private Usuario usuario(Long id, String nome) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome(nome);
        return usuario;
    }
}
