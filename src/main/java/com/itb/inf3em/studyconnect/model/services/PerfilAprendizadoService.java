package com.itb.inf3em.studyconnect.model.services;

import com.itb.inf3em.studyconnect.model.dto.PerfilAprendizadoDTO;
import com.itb.inf3em.studyconnect.model.entity.PerfilAprendizado;
import com.itb.inf3em.studyconnect.model.repository.PerfilAprendizadoRepository;
import com.itb.inf3em.studyconnect.security.AlunoAuthorization;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class PerfilAprendizadoService {

    private final PerfilAprendizadoRepository repo;
    private final AlunoAuthorization alunoAuthorization;

    public PerfilAprendizadoService(PerfilAprendizadoRepository repo,
                                    AlunoAuthorization alunoAuthorization) {
        this.repo = repo;
        this.alunoAuthorization = alunoAuthorization;
    }

    public Optional<PerfilAprendizado> findByAluno(Long alunoId) {
        alunoAuthorization.requireCanAccessAluno(alunoId);
        return repo.findByAlunoId(alunoId);
    }

    public PerfilAprendizado create(PerfilAprendizadoDTO dto) {
        dto.setAlunoId(alunoAuthorization.resolveAlunoId(dto.getAlunoId()));
        if (dto.getAlunoId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "alunoId é obrigatório.");
        if (repo.findByAlunoId(dto.getAlunoId()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Perfil já existe. Use PUT para atualizar.");

        PerfilAprendizado p = new PerfilAprendizado();
        apply(p, dto);
        return repo.save(p);
    }

    public PerfilAprendizado update(Long alunoId, PerfilAprendizadoDTO dto) {
        alunoAuthorization.requireCanAccessAluno(alunoId);
        dto.setAlunoId(alunoId);
        PerfilAprendizado p = repo.findByAlunoId(alunoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil não encontrado."));
        apply(p, dto);
        return repo.save(p);
    }

    private void apply(PerfilAprendizado p, PerfilAprendizadoDTO dto) {
        p.setAlunoId(dto.getAlunoId() != null ? dto.getAlunoId() : p.getAlunoId());
        p.setObjetivo(dto.getObjetivo());
        p.setNivel(dto.getNivel());
        p.setHorasSemana(dto.getHorasSemana());
        p.setMetaSemanal(dto.getMetaSemanal());
        p.setRitmo(dto.getRitmo());
        p.setInteresses(dto.getInteresses());
        p.setDificuldades(dto.getDificuldades());
    }
}
