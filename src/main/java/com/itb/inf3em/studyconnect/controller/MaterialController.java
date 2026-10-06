package com.itb.inf3em.studyconnect.controller;

import com.itb.inf3em.studyconnect.model.dto.MaterialDTO;
import com.itb.inf3em.studyconnect.model.entity.Material;
import com.itb.inf3em.studyconnect.model.services.MaterialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller é a "porta de entrada" das requisições HTTP.
// Recebe a requisição, chama o Service e devolve a resposta.
@RestController
@RequestMapping("/api/v1/materiais")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    /**
     * GET /api/v1/materiais
     * Retorna todos os materiais cadastrados.
     * Requer autenticação (JWT).
     */
    @GetMapping
    public ResponseEntity<List<MaterialDTO>> findAll() {
        List<MaterialDTO> dtos = materialService.findAll()
                .stream()
                .map(MaterialDTO::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/v1/materiais/{id}
     * Retorna um material específico pelo ID.
     * Retorna 404 se não encontrado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MaterialDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(new MaterialDTO(materialService.findById(id)));
    }

    /**
     * GET /api/v1/materiais/usuario/{usuarioId}
     * Retorna todos os materiais enviados por um usuário específico.
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<MaterialDTO>> findByUsuario(@PathVariable Long usuarioId) {
        List<MaterialDTO> dtos = materialService.findByUsuario(usuarioId)
                .stream()
                .map(MaterialDTO::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * POST /api/v1/materiais
     * Cria um novo material.
     * Body obrigatório: { titulo, materia, tipo, categoria, usuarioId }
     * Body opcional: { url }
     * Retorna 201 Created com o material criado.
     */
    @PostMapping
    public ResponseEntity<MaterialDTO> save(@RequestBody Material material) {
        Material salvo = materialService.save(material);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MaterialDTO(salvo));
    }

    /**
     * DELETE /api/v1/materiais/{id}
     * Remove um material pelo ID.
     * Retorna 204 No Content se removido com sucesso.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        materialService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
