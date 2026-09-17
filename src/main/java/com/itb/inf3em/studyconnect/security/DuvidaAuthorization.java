package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.entity.Aula;
import com.itb.inf3em.studyconnect.model.entity.Duvida;
import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Trilha;
import com.itb.inf3em.studyconnect.model.repository.AulaRepository;
import com.itb.inf3em.studyconnect.model.repository.TrilhaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class DuvidaAuthorization {

    private final CurrentUser currentUser;
    private final AlunoAuthorization alunoAuthorization;
    private final TrilhaAuthorization trilhaAuthorization;
    private final AulaRepository aulaRepository;
    private final TrilhaRepository trilhaRepository;

    public DuvidaAuthorization(CurrentUser currentUser,
                               AlunoAuthorization alunoAuthorization,
                               TrilhaAuthorization trilhaAuthorization,
                               AulaRepository aulaRepository,
                               TrilhaRepository trilhaRepository) {
        this.currentUser = currentUser;
        this.alunoAuthorization = alunoAuthorization;
        this.trilhaAuthorization = trilhaAuthorization;
        this.aulaRepository = aulaRepository;
        this.trilhaRepository = trilhaRepository;
    }

    public Long resolveAlunoId(Long requestedAlunoId) {
        return alunoAuthorization.resolveAlunoId(requestedAlunoId);
    }

    public Aula getAula(Long aulaId) {
        return aulaRepository.findById(aulaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula nao encontrada."));
    }

    public void requireCanManageAula(Long aulaId) {
        Aula aula = getAula(aulaId);
        Trilha trilha = trilhaRepository.findById(aula.getTrilhaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trilha nao encontrada."));
        trilhaAuthorization.requireCanManage(trilha);
    }

    public void requireCanManageTrilha(Long trilhaId) {
        Trilha trilha = getTrilha(trilhaId);
        trilhaAuthorization.requireCanManage(trilha);
    }

    public Trilha getTrilha(Long trilhaId) {
        return trilhaRepository.findById(trilhaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trilha nao encontrada."));
    }

    public void requireCanAccessAluno(Long alunoId) {
        alunoAuthorization.requireCanAccessAluno(alunoId);
    }

    /**
     * Exige que o caller possa RESPONDER ou RESOLVER uma duvida.
     * ALUNO nunca pode — mesmo sendo o dono da duvida.
     * PROFESSOR pode somente se for dono da trilha relacionada.
     * ADMIN pode sempre.
     */
    public void requireCanRespond(Duvida duvida) {
        AuthenticatedUser user = currentUser.require();
        if (user.tipoUsuario() == TipoUsuario.ADMIN) {
            return;
        }
        if (user.tipoUsuario() == TipoUsuario.PROFESSOR) {
            requireCanManageAula(duvida.getAulaId());
            return;
        }
        throw new AccessDeniedException("Apenas professores ou administradores podem responder duvidas.");
    }

    public void requireCanAccessAlunoAula(Long alunoId, Long aulaId) {
        AuthenticatedUser user = currentUser.require();
        if (user.tipoUsuario() == TipoUsuario.ADMIN) {
            return;
        }
        if (user.tipoUsuario() == TipoUsuario.ALUNO && user.usuarioId().equals(alunoId)) {
            return;
        }
        if (user.tipoUsuario() == TipoUsuario.PROFESSOR) {
            requireCanManageAula(aulaId);
            return;
        }
        throw new AccessDeniedException("Acesso negado.");
    }
}
