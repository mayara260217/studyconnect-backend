package com.itb.inf3em.studyconnect.model.services;

import com.itb.inf3em.studyconnect.model.dto.ProgressoDTO;
import com.itb.inf3em.studyconnect.model.entity.ProgressoAula;
import com.itb.inf3em.studyconnect.model.repository.AulaRepository;
import com.itb.inf3em.studyconnect.model.repository.MatriculaTrilhaRepository;
import com.itb.inf3em.studyconnect.model.repository.ProgressoAulaRepository;
import com.itb.inf3em.studyconnect.model.repository.TrilhaRepository;
import com.itb.inf3em.studyconnect.security.AlunoAuthorization;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProgressoAulaService {

    private final ProgressoAulaRepository progressoRepository;
    private final AulaRepository aulaRepository;
    private final TrilhaRepository trilhaRepository;
    private final MatriculaTrilhaRepository matriculaRepository;
    private final AlunoAuthorization alunoAuthorization;

    public ProgressoAulaService(ProgressoAulaRepository progressoRepository,
                                AulaRepository aulaRepository,
                                TrilhaRepository trilhaRepository,
                                MatriculaTrilhaRepository matriculaRepository,
                                AlunoAuthorization alunoAuthorization) {
        this.progressoRepository = progressoRepository;
        this.aulaRepository = aulaRepository;
        this.trilhaRepository = trilhaRepository;
        this.matriculaRepository = matriculaRepository;
        this.alunoAuthorization = alunoAuthorization;
    }

    /** Upsert: marca aula como concluída. Idempotente. */
    public ProgressoAula concluirAula(Long alunoId, Long aulaId) {
        Long authenticatedAlunoId = alunoAuthorization.resolveAlunoId(alunoId);
        if (aulaId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "alunoId e aulaId são obrigatórios.");

        var aula = aulaRepository.findById(aulaId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada."));

        trilhaRepository.findById(aula.getTrilhaId()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Trilha nao encontrada."));

        // Verifica matrícula ativa na trilha antes de registrar progresso
        if (!matriculaRepository.existsByAlunoIdAndTrilhaIdAndAtivoTrue(authenticatedAlunoId, aula.getTrilhaId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Matricula ativa na trilha e obrigatoria para registrar progresso.");
        }

        ProgressoAula progresso = progressoRepository
            .findByAlunoIdAndAulaId(authenticatedAlunoId, aulaId)
            .orElseGet(() -> new ProgressoAula(authenticatedAlunoId, aulaId));

        if (!progresso.isConcluida()) {
            progresso.setConcluida(true);
            progresso.setConcluidaEm(LocalDateTime.now());
            progressoRepository.save(progresso);
        }

        return progresso;
    }

    /** Retorna IDs das aulas concluídas (usado internamente e pelo hook de progresso). */
    public List<Long> getAulasConcluidas(Long alunoId) {
        alunoAuthorization.requireCanAccessAluno(alunoId);
        return progressoRepository
            .findByAlunoIdAndConcluidaTrue(alunoId)
            .stream()
            .map(ProgressoAula::getAulaId)
            .toList();
    }

    /** Retorna objetos completos com aulaId + concluidaEm (usado pelo gráfico semanal). */
    public List<ProgressoAula> getProgressoCompleto(Long alunoId) {
        alunoAuthorization.requireCanAccessAluno(alunoId);
        return progressoRepository.findByAlunoIdAndConcluidaTrue(alunoId);
    }

    /** Retorna progresso do aluno em uma trilha específica. */
    public ProgressoDTO getProgressoTrilha(Long trilhaId, Long alunoId) {
        alunoAuthorization.requireCanAccessAluno(alunoId);
        trilhaRepository.findById(trilhaId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Trilha nao encontrada."));
        List<Long> aulaIds = aulaRepository
            .findByTrilhaIdOrderByOrdem(trilhaId)
            .stream()
            .map(a -> a.getId())
            .toList();

        List<Long> concluidas = progressoRepository
            .findByAlunoIdAndAulaIdInAndConcluidaTrue(alunoId, aulaIds.isEmpty() ? List.of(-1L) : aulaIds)
            .stream()
            .map(ProgressoAula::getAulaId)
            .toList();

        return new ProgressoDTO(concluidas, aulaIds.size());
    }
}
