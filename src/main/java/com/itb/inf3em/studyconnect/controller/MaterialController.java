package com.itb.inf3em.studyconnect.controller;

import com.itb.inf3em.studyconnect.model.dto.MaterialDTO;
import com.itb.inf3em.studyconnect.model.entity.Material;
import com.itb.inf3em.studyconnect.model.services.MaterialService;
import com.itb.inf3em.studyconnect.model.services.UploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

// Controller é a "porta de entrada" das requisições HTTP.
// Recebe a requisição, chama o Service e devolve a resposta.
@RestController
@RequestMapping("/api/v1/materiais")
public class MaterialController {

    private final MaterialService materialService;
    private final UploadService uploadService;

    public MaterialController(MaterialService materialService, UploadService uploadService) {
        this.materialService = materialService;
        this.uploadService = uploadService;
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
     * POST /api/v1/materiais/upload
     * Recebe um arquivo (PDF, PNG, JPG, JPEG), salva em disco e retorna a URL pública.
     * Apenas ADMIN e PROFESSOR podem fazer upload.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(@RequestParam("arquivo") MultipartFile arquivo) {
        String url = uploadService.salvar(arquivo);
        return ResponseEntity.ok(Map.of("url", url));
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
     * PUT /api/v1/materiais/{id}
     * Atualiza um material existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MaterialDTO> update(@PathVariable Long id, @RequestBody Material material) {
        return ResponseEntity.ok(new MaterialDTO(materialService.update(id, material)));
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
