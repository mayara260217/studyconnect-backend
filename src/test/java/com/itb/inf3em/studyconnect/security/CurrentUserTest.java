package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrentUserTest {

    private final CurrentUser currentUser = new CurrentUser();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void alunoNaoPodeExecutarOperacaoAdministrativa() {
        authenticate(10L, TipoUsuario.ALUNO);

        assertThrows(AccessDeniedException.class, currentUser::requireAdmin);
    }

    @Test
    void usuarioNaoPodeAcessarContaDeOutroUsuario() {
        authenticate(10L, TipoUsuario.ALUNO);

        assertThrows(AccessDeniedException.class, () -> currentUser.requireSameUserOrAdmin(20L));
    }

    @Test
    void usuarioPodeAcessarPropriaConta() {
        authenticate(10L, TipoUsuario.ALUNO);

        assertDoesNotThrow(() -> currentUser.requireSameUserOrAdmin(10L));
    }

    private void authenticate(Long usuarioId, TipoUsuario tipoUsuario) {
        AuthenticatedUser user = new AuthenticatedUser(usuarioId, "usuario@example.com", tipoUsuario);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + tipoUsuario.name()))));
    }
}
