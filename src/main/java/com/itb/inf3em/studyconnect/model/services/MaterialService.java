package com.itb.inf3em.studyconnect.model.services;

import com.itb.inf3em.studyconnect.model.entity.Material;
import com.itb.inf3em.studyconnect.model.repository.MaterialRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Service é onde ficam as regras de negócio.
// O Controller recebe a requisição e delega para o Service processar.
@Service
public class MaterialService {

    // Injeção via construtor — mais testável que @Autowired no campo
    private final MaterialRepository materialRepository;

    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    // Retorna todos os materiais cadastrados, do mais recente para o mais antigo
    public List<Material> findAll() {
        return materialRepository.findAll();
    }

    // Retorna todos os materiais de um usuário específico
    public List<Material> findByUsuario(Long usuarioId) {
        return materialRepository.findByUsuarioIdOrderByCriadoEmDesc(usuarioId);
    }

    // Busca um material pelo ID — lança 404 se não encontrar
    public Material findById(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Material não encontrado com o id: " + id));
    }

    // Cria um novo material após validar os campos obrigatórios
    public Material save(Material material) {
        // Valida campos obrigatórios antes de tentar salvar
        if (material.getTitulo() == null || material.getTitulo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Título é obrigatório.");
        }
        if (material.getMateria() == null || material.getMateria().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Matéria é obrigatória.");
        }
        if (material.getTipo() == null || material.getTipo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo é obrigatório.");
        }
        if (material.getCategoria() == null || material.getCategoria().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria é obrigatória.");
        }
        if (material.getUsuarioId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID do usuário é obrigatório.");
        }
        return materialRepository.save(material);
    }

    // Remove um material pelo ID — lança 404 se não existir
    public void delete(Long id) {
        Material material = findById(id);
        materialRepository.delete(material);
    }
}
