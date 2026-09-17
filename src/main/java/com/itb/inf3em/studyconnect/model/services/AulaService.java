package com.itb.inf3em.studyconnect.model.services;

import com.itb.inf3em.studyconnect.model.entity.Aula;
import com.itb.inf3em.studyconnect.model.entity.Trilha;
import com.itb.inf3em.studyconnect.model.repository.AulaRepository;
import com.itb.inf3em.studyconnect.model.repository.TrilhaRepository;
import com.itb.inf3em.studyconnect.security.TrilhaAuthorization;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AulaService {

    private final AulaRepository aulaRepository;
    private final TrilhaRepository trilhaRepository;
    private final TrilhaAuthorization trilhaAuthorization;

    public AulaService(AulaRepository aulaRepository,
                       TrilhaRepository trilhaRepository,
                       TrilhaAuthorization trilhaAuthorization) {
        this.aulaRepository = aulaRepository;
        this.trilhaRepository = trilhaRepository;
        this.trilhaAuthorization = trilhaAuthorization;
    }

    /**
     * Get all aulas for a specific trilha.
     */
    public List<Aula> getAulasByTrilha(Long trilhaId) {
        Trilha trilha = trilhaRepository.findById(trilhaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trilha não encontrada com o id: " + trilhaId
                ));

        trilhaAuthorization.requireCanView(trilha);
        try {
            List<Aula> aulas = aulaRepository.findByTrilhaIdOrderByOrdem(trilhaId);
            return trilhaAuthorization.canManage(trilha) ? aulas : aulas.stream()
                    .filter(aula -> "PUBLICADA".equalsIgnoreCase(aula.getStatus()))
                    .toList();
        } catch (Exception ex) {
            // Banco pode estar desatualizado (ex: coluna nova ainda não migrada)
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Nao foi possivel consultar as aulas.");
        }
    }

    /**
     * Get a aula by its ID.
     * Throws exception if not found.
     */
    public Aula getAulaById(Long id) {
        Aula aula = aulaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Aula não encontrada com o id: " + id
                ));
        Trilha trilha = trilhaRepository.findById(aula.getTrilhaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trilha nao encontrada."));
        trilhaAuthorization.requireCanView(trilha);
        if (!trilhaAuthorization.canManage(trilha) && !"PUBLICADA".equalsIgnoreCase(aula.getStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula nao encontrada com o id: " + id);
        }
        return aula;
    }

    /**
     * Create a new aula.
     */
    public Aula createAula(Aula aula) {
        if (aula.getTrilhaId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID da trilha é obrigatório.");

        if (aula.getTitulo() == null || aula.getTitulo().trim().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Título da aula é obrigatório.");

        trilhaRepository.findById(aula.getTrilhaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Trilha não encontrada com o id: " + aula.getTrilhaId()));

        // blocos chegam no campo conteudo como JSON string — ja vem serializado do frontend
        // se conteudo vier nulo, inicializa com objeto vazio
        requireManageTrilha(aula.getTrilhaId());
        if (aula.getConteudo() == null) {
            aula.setConteudo("{}");
        }

        // status padrão: PUBLICADA
        if (aula.getStatus() == null || aula.getStatus().isBlank()) {
            aula.setStatus("PUBLICADA");
        }

        try {
            return aulaRepository.save(aula);
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Nao foi possivel criar a aula.");
        }
    }

    /**
     * Update a aula's information.
     */
    public Aula updateAula(Long id, Aula aulaUpdate) {
        Aula aulaExistente = getAulaById(id);
        requireManageTrilha(aulaExistente.getTrilhaId());

        // Update allowed fields
        if (aulaUpdate.getTitulo() != null && !aulaUpdate.getTitulo().trim().isEmpty()) {
            aulaExistente.setTitulo(aulaUpdate.getTitulo());
        }

        if (aulaUpdate.getTipo() != null) {
            aulaExistente.setTipo(aulaUpdate.getTipo());
        }

        if (aulaUpdate.getConteudo() != null) {
            aulaExistente.setConteudo(aulaUpdate.getConteudo());
        }

        if (aulaUpdate.getOrdem() != null) {
            aulaExistente.setOrdem(aulaUpdate.getOrdem());
        }

        if (aulaUpdate.getStatus() != null && !aulaUpdate.getStatus().isBlank()) {
            aulaExistente.setStatus(aulaUpdate.getStatus());
        }

        return aulaRepository.save(aulaExistente);
    }

    /**
     * Delete a aula.
     */
    public void deleteAula(Long id) {
        Aula aula = getAulaById(id);
        requireManageTrilha(aula.getTrilhaId());
        aulaRepository.delete(aula);
    }

    private void requireManageTrilha(Long trilhaId) {
        Trilha trilha = trilhaRepository.findById(trilhaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Trilha nao encontrada."));
        trilhaAuthorization.requireCanManage(trilha);
    }
}
