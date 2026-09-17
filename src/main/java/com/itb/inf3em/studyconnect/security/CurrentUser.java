package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public AuthenticatedUser require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new AccessDeniedException("Autenticacao obrigatoria.");
        }
        return user;
    }

    public AuthenticatedUser requireAdmin() {
        AuthenticatedUser user = require();
        if (user.tipoUsuario() != TipoUsuario.ADMIN) {
            throw new AccessDeniedException("Acesso restrito a administradores.");
        }
        return user;
    }

    public AuthenticatedUser requireSameUserOrAdmin(Long usuarioId) {
        AuthenticatedUser user = require();
        if (!user.usuarioId().equals(usuarioId) && user.tipoUsuario() != TipoUsuario.ADMIN) {
            throw new AccessDeniedException("Voce nao tem permissao para acessar esta conta.");
        }
        return user;
    }
}
