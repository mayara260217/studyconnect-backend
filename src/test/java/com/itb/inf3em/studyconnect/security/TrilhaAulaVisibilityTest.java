package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.entity.Aula;
import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Trilha;
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
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrilhaAulaVisibilityTest {
    private final CurrentUser currentUser = new CurrentUser();
    private TrilhaRepository trilhaRepository;
    private AulaRepository aulaRepository;
    private AulaService aulaService;
    private TrilhaService trilhaService;

    @BeforeEach void setUp() {
        trilhaRepository = mock(TrilhaRepository.class);
        aulaRepository = mock(AulaRepository.class);
        TrilhaAuthorization authorization = new TrilhaAuthorization(currentUser);
        aulaService = new AulaService(aulaRepository, trilhaRepository, authorization);
        trilhaService = new TrilhaService(trilhaRepository, mock(UsuarioRepository.class), authorization);
    }
    @AfterEach void clearContext() { SecurityContextHolder.clearContext(); }

    @Test void alunoVeSomenteAulasPublicadasDeTrilhaPublica() {
        authenticate(2L, TipoUsuario.ALUNO);
        when(trilhaRepository.findById(1L)).thenReturn(Optional.of(trilha("PUBLICA", 10L)));
        when(aulaRepository.findByTrilhaIdOrderByOrdem(1L)).thenReturn(List.of(aula("PUBLICADA"), aula("RASCUNHO")));
        assertEquals(1, aulaService.getAulasByTrilha(1L).size());
    }

    @Test void alunoNaoAcessaTrilhaPrivada() {
        authenticate(2L, TipoUsuario.ALUNO);
        when(trilhaRepository.findById(1L)).thenReturn(Optional.of(trilha("PRIVADA", 10L)));
        assertThrows(AccessDeniedException.class, () -> trilhaService.getTrilhaById(1L));
        assertThrows(AccessDeniedException.class, () -> aulaService.getAulasByTrilha(1L));
    }

    @Test void alunoNaoObtemRascunhoPorIdentificador() {
        authenticate(2L, TipoUsuario.ALUNO);
        Aula rascunho = aula("RASCUNHO"); rascunho.setTrilhaId(1L);
        when(aulaRepository.findById(5L)).thenReturn(Optional.of(rascunho));
        when(trilhaRepository.findById(1L)).thenReturn(Optional.of(trilha("PUBLICA", 10L)));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> aulaService.getAulaById(5L));
        assertEquals(404, exception.getStatusCode().value());
    }

    private void authenticate(Long id, TipoUsuario tipo) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(id, "user@test.com", tipo), null, List.of(new SimpleGrantedAuthority("ROLE_" + tipo.name()))));
    }
    private Trilha trilha(String tipo, Long professorId) { Trilha t = new Trilha(); t.setTipo(tipo); t.setProfessorId(professorId); return t; }
    private Aula aula(String status) { Aula a = new Aula(); a.setTitulo("Teste"); a.setStatus(status); return a; }
}
