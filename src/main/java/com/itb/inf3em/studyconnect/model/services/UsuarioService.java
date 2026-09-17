package com.itb.inf3em.studyconnect.model.services;

import com.itb.inf3em.studyconnect.model.dto.AtualizarPerfilDTO;
import com.itb.inf3em.studyconnect.model.entity.Trilha;
import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.repository.DuvidaRepository;
import com.itb.inf3em.studyconnect.model.repository.EmailChangeTokenRepository;
import com.itb.inf3em.studyconnect.model.repository.MatriculaTrilhaRepository;
import com.itb.inf3em.studyconnect.model.repository.PerfilAprendizadoRepository;
import com.itb.inf3em.studyconnect.model.repository.ProgressoAulaRepository;
import com.itb.inf3em.studyconnect.model.repository.TrilhaRepository;
import com.itb.inf3em.studyconnect.model.repository.TurmaRepository;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TrilhaRepository trilhaRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private MatriculaTrilhaRepository matriculaRepository;

    @Autowired
    private ProgressoAulaRepository progressoRepository;

    @Autowired
    private DuvidaRepository duvidaRepository;

    @Autowired
    private PerfilAprendizadoRepository perfilRepository;

    @Autowired
    private EmailChangeTokenRepository emailChangeTokenRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CredentialValidationService credentialValidationService;

    @Autowired
    private EmailVerificationService emailVerificationService;

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Usuario save(Usuario usuario) {
        credentialValidationService.validateEmail(usuario.getEmail());
        credentialValidationService.validatePassword(usuario.getSenha());

        usuario.setEmail(usuario.getEmail().trim().toLowerCase());
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setTipoUsuario(TipoUsuario.ALUNO);

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail ja esta cadastrado.");
        }

        usuario.setAtivo(false); // ativado apenas após verificação de e-mail

        Usuario salvo;
        try {
            salvo = usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail ja esta cadastrado.");
        }

        emailVerificationService.enviarCodigo(salvo.getEmail());
        return salvo;
    }

    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado com o id" + id));
    }

    public Usuario updateOwn(long id, AtualizarPerfilDTO dto) {
        Usuario usuarioExistente = findById(id);

        // Apenas nome e fotoUrl são editáveis pelo próprio usuário.
        // email, senha, tipoUsuario e ativo são imutáveis por este fluxo.
        if (dto.getNome() != null && !dto.getNome().isBlank()) {
            usuarioExistente.setNome(dto.getNome().trim());
        }
        if (dto.getFotoUrl() != null) {
            usuarioExistente.setFotoUrl(dto.getFotoUrl());
        }

        return usuarioRepository.save(usuarioExistente);
    }

    public Usuario updateStatusAsAdmin(long id, boolean ativo) {
        Usuario usuarioExistente = findById(id);
        usuarioExistente.setAtivo(ativo);
        return usuarioRepository.save(usuarioExistente);
    }

    public int removeDuplicateUsuariosByEmail() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        Map<String, Usuario> seen = new HashMap<>();
        int deleted = 0;

        for (Usuario usuario : usuarios) {
            if (usuario.getEmail() == null) {
                continue;
            }

            String normalizedEmail = usuario.getEmail().trim().toLowerCase();

            if (seen.containsKey(normalizedEmail)) {
                usuarioRepository.delete(usuario);
                deleted++;
            } else {
                seen.put(normalizedEmail, usuario);
            }
        }

        return deleted;
    }

    /**
     * Exclui o usuário e todos os seus dados dependentes dentro de uma única transação.
     *
     * Ordem de exclusão respeita as FKs do banco (sem ON DELETE CASCADE):
     *
     * 1. EmailChangeToken (usuario_id → sem FK declarada, mas limpa por consistência)
     * 2. MatriculaTrilha  (aluno_id  → FK_Matricula_Aluno RESTRICT)
     * 3. ProgressoAula    (aluno_id  → FK_Progresso_Aluno RESTRICT)
     * 4. Duvida como aluno (aluno_id → FK_Duvida_Aluno RESTRICT)
     * 5. [PROFESSOR] Duvidas das trilhas do professor (FK_Duvida_Trilha RESTRICT — antes de deletar Trilha)
     * 6. [PROFESSOR] Trilhas → banco faz CASCADE em Aula, MatriculaTrilha, ProgressoAula
     * 7. [PROFESSOR] Turmas
     * 8. PerfilAprendizado (ON DELETE CASCADE no banco, mas deletado explicitamente por segurança)
     * 9. Usuario
     */
    @Transactional
    public void delete(long id) {
        Usuario usuario = findById(id);

        // 1. Tokens de troca de e-mail pendentes
        emailChangeTokenRepository.deleteByUsuarioId(id);

        // 2. Matrículas do aluno
        matriculaRepository.deleteByAlunoId(id);

        // 3. Progresso de aulas do aluno
        progressoRepository.deleteByAlunoId(id);

        // 4. Dúvidas criadas pelo aluno
        duvidaRepository.deleteByAlunoId(id);

        // 5-7. Dados de professor: dúvidas das trilhas, trilhas e turmas
        if (usuario.getTipoUsuario() == TipoUsuario.PROFESSOR
                || usuario.getTipoUsuario() == TipoUsuario.ADMIN) {
            List<Trilha> trilhas = trilhaRepository.findByProfessorId(id);
            for (Trilha trilha : trilhas) {
                // FK_Duvida_Trilha é RESTRICT — dúvidas da trilha devem ser removidas antes
                duvidaRepository.deleteByTrilhaId(trilha.getId());
            }
            // Trilha → banco faz CASCADE em Aula, MatriculaTrilha (FK_Matricula_Trilha CASCADE),
            // ProgressoAula (FK_Progresso_Aula CASCADE)
            trilhaRepository.deleteAll(trilhas);

            turmaRepository.deleteAll(turmaRepository.findByProfessorId(id));
        }

        // 8. Perfil de aprendizado (FK_Perfil_Aluno tem ON DELETE CASCADE, mas explicitamos)
        perfilRepository.deleteByAlunoId(id);

        // 9. Usuário
        usuarioRepository.deleteById(id);
    }
}
