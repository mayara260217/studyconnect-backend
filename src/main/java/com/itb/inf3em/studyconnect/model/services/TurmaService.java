package com.itb.inf3em.studyconnect.model.services;

import com.itb.inf3em.studyconnect.model.entity.Turma;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.repository.TurmaRepository;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import com.itb.inf3em.studyconnect.security.AuthenticatedUser;
import com.itb.inf3em.studyconnect.security.TurmaAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TurmaService {

    private static final Logger log = LoggerFactory.getLogger(TurmaService.class);

    private final TurmaRepository turmaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurmaAuthorization turmaAuthorization;

    public TurmaService(TurmaRepository turmaRepository,
                        UsuarioRepository usuarioRepository,
                        TurmaAuthorization turmaAuthorization) {
        this.turmaRepository = turmaRepository;
        this.usuarioRepository = usuarioRepository;
        this.turmaAuthorization = turmaAuthorization;
    }

    /**
     * GET /api/v1/turmas
     * Filtra turmas PRIVADAS para usuários sem permissão de leitura.
     * ADMIN vê todas. PROFESSOR vê as próprias + públicas. ALUNO vê apenas públicas.
     */
    public List<Turma> getAllTurmas() {
        return turmaRepository.findAll().stream()
                .filter(turmaAuthorization::canRead)
                .toList();
    }

    /**
     * GET /api/v1/turmas/{id}
     * Carrega a turma e aplica autorização baseada no dono e visibilidade.
     */
    public Turma getTurmaById(Long id) {
        Turma turma = turmaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada."));
        turmaAuthorization.requireCanReadTurma(turma);
        return turma;
    }

    /**
     * GET /api/v1/turmas/professor/{professorId}
     * ADMIN: qualquer professorId. PROFESSOR: somente o próprio. ALUNO: negado.
     */
    public List<Turma> getTurmasByProfessor(Long professorId) {
        turmaAuthorization.requireCanReadByProfessor(professorId);
        return turmaRepository.findByProfessorId(professorId);
    }

    public List<Turma> getMyTurmas(Long requestedProfessorId) {
        AuthenticatedUser user = turmaAuthorization.requireProfessorOrAdmin();
        Long professorId = user.tipoUsuario() == com.itb.inf3em.studyconnect.model.entity.TipoUsuario.ADMIN
                && requestedProfessorId != null ? requestedProfessorId : user.usuarioId();
        return turmaRepository.findByProfessorId(professorId);
    }

    public Turma createTurma(Turma turma) {
        AuthenticatedUser authenticatedUser = turmaAuthorization.requireProfessorOrAdmin();
        Long professorId = authenticatedUser.usuarioId();
        if (authenticatedUser.tipoUsuario() == com.itb.inf3em.studyconnect.model.entity.TipoUsuario.ADMIN
                && turma.getProfessorId() != null) {
            professorId = turma.getProfessorId();
        }
        turma.setProfessorId(professorId);

        Usuario professor = usuarioRepository.findById(professorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Professor não encontrado com o id: " + turma.getProfessorId()
                ));

        if (turmaRepository.existsByCodigo(turma.getCodigo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este código de turma já está em uso.");
        }

        if (turma.getNome() == null || turma.getNome().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome da turma é obrigatório.");
        }

        if (turma.getCodigo() == null || turma.getCodigo().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código da turma é obrigatório.");
        }

        if (turma.getProfessorId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID do professor é obrigatório.");
        }

        turma.setProfessorNome(professor.getNome());

        try {
            return turmaRepository.save(turma);
        } catch (DataIntegrityViolationException ex) {
            log.error("Erro de integridade ao salvar turma", ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível salvar a turma devido a dados inválidos ou conflitantes."
            );
        } catch (Exception ex) {
            log.error("Erro inesperado ao criar turma", ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível salvar a turma devido a dados inválidos ou conflitantes."
            );
        }
    }

    public Turma joinTurmaByCode(String codigo) {
        return turmaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Turma não encontrada. Verifique o código digitado."
                ));
    }

    public Turma updateTurma(Long id, Turma turmaUpdate, Long currentUserId) {
        Turma turmaExistente = turmaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada."));
        turmaAuthorization.requireCanManage(turmaExistente);

        if (false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar esta turma.");
        }

        if (turmaUpdate.getNome() != null && !turmaUpdate.getNome().trim().isEmpty()) {
            turmaExistente.setNome(turmaUpdate.getNome());
        }
        if (turmaUpdate.getDescricao() != null) {
            turmaExistente.setDescricao(turmaUpdate.getDescricao());
        }
        if (turmaUpdate.getTipo() != null) {
            turmaExistente.setTipo(turmaUpdate.getTipo());
        }
        if (turmaUpdate.getNivel() != null) {
            turmaExistente.setNivel(turmaUpdate.getNivel());
        }

        return turmaRepository.save(turmaExistente);
    }

    public void deleteTurma(Long id, Long currentUserId) {
        Turma turma = turmaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada."));
        turmaAuthorization.requireCanManage(turma);

        if (false) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para deletar esta turma.");
        }

        turmaRepository.delete(turma);
    }
}
