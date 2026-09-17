package com.itb.inf3em.studyconnect.model.services;

import com.itb.inf3em.studyconnect.model.entity.Trilha;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.repository.TrilhaRepository;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import com.itb.inf3em.studyconnect.security.AuthenticatedUser;
import com.itb.inf3em.studyconnect.security.TrilhaAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TrilhaService {

    private static final Logger log = LoggerFactory.getLogger(TrilhaService.class);

    private final TrilhaRepository trilhaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TrilhaAuthorization trilhaAuthorization;

    public TrilhaService(TrilhaRepository trilhaRepository,
                         UsuarioRepository usuarioRepository,
                         TrilhaAuthorization trilhaAuthorization) {
        this.trilhaRepository = trilhaRepository;
        this.usuarioRepository = usuarioRepository;
        this.trilhaAuthorization = trilhaAuthorization;
    }

    /**
     * Get all trilhas (public + all types).
     * Can be filtered in the future if needed.
     */
    public List<Trilha> getAllTrilhas() {
        return trilhaRepository.findAll().stream().filter(this::canView).toList();
    }

    /**
     * Get a trilha by its ID.
     * Throws exception if not found.
     */
    public Trilha getTrilhaById(Long id) {
        Trilha trilha = trilhaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trilha não encontrada com o id: " + id
                ));
        trilhaAuthorization.requireCanView(trilha);
        return trilha;
    }

    /**
     * Get all trilhas created by a specific teacher.
     */
    public List<Trilha> getTrilhasByProfessor(Long professorId) {
        return trilhaRepository.findByProfessorId(professorId).stream().filter(this::canView).toList();
    }

    /**
     * Create a new trilha.
     * Validates that the professorId exists.
     */
    public Trilha createTrilha(Trilha trilha) {
        AuthenticatedUser authenticatedUser = trilhaAuthorization.requireProfessorOrAdmin();
        trilha.setProfessorId(authenticatedUser.usuarioId());

        // Validate professor exists
        Usuario professor = usuarioRepository.findById(authenticatedUser.usuarioId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Professor não encontrado com o id: " + trilha.getProfessorId()
                ));

        // Validate required fields
        if (trilha.getNome() == null || trilha.getNome().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nome da trilha é obrigatório."
            );
        }

        if (trilha.getTipo() == null || trilha.getTipo().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo da trilha é obrigatório."
            );
        }

        if (trilha.getNivel() == null || trilha.getNivel().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nível da trilha é obrigatório."
            );
        }

        if (trilha.getProfessorId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID do professor é obrigatório."
            );
        }

        trilha.setProfessorNome(professor.getNome());

        try {
            return trilhaRepository.save(trilha);
        } catch (DataIntegrityViolationException ex) {
            log.error("Erro de integridade ao salvar trilha", ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível salvar a trilha devido a dados inválidos ou conflitantes."
            );
        } catch (Exception ex) {
            log.error("Erro inesperado ao criar trilha", ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível salvar a trilha devido a dados inválidos ou conflitantes."
            );
        }
    }

    /**
     * Update a trilha's information.
     * Only the professor who created it can update.
     */
    public Trilha updateTrilha(Long id, Trilha trilhaUpdate) {
        Trilha trilhaExistente = getTrilhaById(id);
        trilhaAuthorization.requireCanManage(trilhaExistente);

        if (trilhaUpdate.getNome() != null && !trilhaUpdate.getNome().trim().isEmpty())
            trilhaExistente.setNome(trilhaUpdate.getNome());
        if (trilhaUpdate.getDescricao() != null)
            trilhaExistente.setDescricao(trilhaUpdate.getDescricao());
        if (trilhaUpdate.getTipo() != null)
            trilhaExistente.setTipo(trilhaUpdate.getTipo());
        if (trilhaUpdate.getNivel() != null)
            trilhaExistente.setNivel(trilhaUpdate.getNivel());
        if (trilhaUpdate.getDisciplina() != null)
            trilhaExistente.setDisciplina(trilhaUpdate.getDisciplina());

        return trilhaRepository.save(trilhaExistente);
    }

    /**
     * Delete a trilha.
     * Only the professor who created it can delete.
     */
    public void deleteTrilha(Long id) {
        Trilha trilha = getTrilhaById(id);
        trilhaAuthorization.requireCanManage(trilha);
        trilhaRepository.delete(trilha);
    }

    private boolean canView(Trilha trilha) {
        try {
            trilhaAuthorization.requireCanView(trilha);
            return true;
        } catch (org.springframework.security.access.AccessDeniedException exception) {
            return false;
        }
    }
}
