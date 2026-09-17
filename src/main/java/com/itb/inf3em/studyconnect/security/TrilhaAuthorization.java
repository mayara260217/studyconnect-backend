package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Trilha;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class TrilhaAuthorization {

    private final CurrentUser currentUser;

    public TrilhaAuthorization(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    public AuthenticatedUser requireProfessorOrAdmin() {
        AuthenticatedUser user = currentUser.require();
        if (user.tipoUsuario() != TipoUsuario.PROFESSOR && user.tipoUsuario() != TipoUsuario.ADMIN) {
            throw new AccessDeniedException("Apenas professores ou administradores podem gerenciar trilhas.");
        }
        return user;
    }

    public void requireCanManage(Trilha trilha) {
        AuthenticatedUser user = requireProfessorOrAdmin();
        if (user.tipoUsuario() == TipoUsuario.PROFESSOR
                && !user.usuarioId().equals(trilha.getProfessorId())) {
            throw new AccessDeniedException("Voce nao tem permissao para gerenciar esta trilha.");
        }
    }
}
