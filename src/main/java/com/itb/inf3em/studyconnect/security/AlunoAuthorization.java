package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class AlunoAuthorization {

    private final CurrentUser currentUser;

    public AlunoAuthorization(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    public Long resolveAlunoId(Long requestedAlunoId) {
        AuthenticatedUser user = requireAlunoOrAdmin();
        return user.tipoUsuario() == TipoUsuario.ADMIN && requestedAlunoId != null
                ? requestedAlunoId
                : user.usuarioId();
    }

    public void requireCanAccessAluno(Long alunoId) {
        AuthenticatedUser user = requireAlunoOrAdmin();
        if (user.tipoUsuario() == TipoUsuario.ALUNO && !user.usuarioId().equals(alunoId)) {
            throw new AccessDeniedException("Voce nao tem permissao para acessar dados de outro aluno.");
        }
    }

    private AuthenticatedUser requireAlunoOrAdmin() {
        AuthenticatedUser user = currentUser.require();
        if (user.tipoUsuario() != TipoUsuario.ALUNO && user.tipoUsuario() != TipoUsuario.ADMIN) {
            throw new AccessDeniedException("Acesso restrito ao aluno ou administrador.");
        }
        return user;
    }
}
