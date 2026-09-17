package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Turma;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class TurmaAuthorization {

    private static final String PRIVADA = "PRIVADA";

    private final CurrentUser currentUser;

    public TurmaAuthorization(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    public AuthenticatedUser requireProfessorOrAdmin() {
        AuthenticatedUser user = currentUser.require();
        if (user.tipoUsuario() != TipoUsuario.PROFESSOR && user.tipoUsuario() != TipoUsuario.ADMIN) {
            throw new AccessDeniedException("Apenas professores ou administradores podem gerenciar turmas.");
        }
        return user;
    }

    public void requireCanManage(Turma turma) {
        AuthenticatedUser user = requireProfessorOrAdmin();
        if (user.tipoUsuario() == TipoUsuario.PROFESSOR
                && !user.usuarioId().equals(turma.getProfessorId())) {
            throw new AccessDeniedException("Voce nao tem permissao para gerenciar esta turma.");
        }
    }

    /**
     * Leitura de turmas por professorId:
     * - ADMIN: qualquer professorId
     * - PROFESSOR: somente o próprio id
     * - ALUNO: negado
     */
    public void requireCanReadByProfessor(Long professorId) {
        AuthenticatedUser user = currentUser.require();
        if (user.tipoUsuario() == TipoUsuario.ADMIN) return;
        if (user.tipoUsuario() == TipoUsuario.PROFESSOR && user.usuarioId().equals(professorId)) return;
        throw new AccessDeniedException("Acesso negado.");
    }

    /**
     * Leitura de uma turma por id:
     * - ADMIN: sempre permitido
     * - PROFESSOR dono: sempre permitido
     * - Outros (ALUNO ou PROFESSOR de outra turma): permitido somente se PUBLICA
     */
    public void requireCanReadTurma(Turma turma) {
        AuthenticatedUser user = currentUser.require();
        if (user.tipoUsuario() == TipoUsuario.ADMIN) return;
        if (user.tipoUsuario() == TipoUsuario.PROFESSOR && user.usuarioId().equals(turma.getProfessorId())) return;
        if (PRIVADA.equalsIgnoreCase(turma.getTipo())) {
            throw new AccessDeniedException("Acesso negado.");
        }
    }

    /**
     * Filtra uma lista de turmas para o caller:
     * - ADMIN: vê todas
     * - PROFESSOR: vê as próprias + públicas de outros
     * - ALUNO: vê apenas públicas
     */
    public boolean canRead(Turma turma) {
        AuthenticatedUser user = currentUser.require();
        if (user.tipoUsuario() == TipoUsuario.ADMIN) return true;
        if (user.tipoUsuario() == TipoUsuario.PROFESSOR && user.usuarioId().equals(turma.getProfessorId())) return true;
        return !PRIVADA.equalsIgnoreCase(turma.getTipo());
    }
}
