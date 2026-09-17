package com.itb.inf3em.studyconnect.controller;

import com.itb.inf3em.studyconnect.model.dto.AulaDTO;
import com.itb.inf3em.studyconnect.model.entity.Aula;
import com.itb.inf3em.studyconnect.model.services.AulaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aulas")
public class AulaController {

    @Autowired
    private AulaService aulaService;

    /**
     * GET /api/v1/aulas/trilha/{trilhaId}
     * Get all aulas for a specific trilha.
     */
    @GetMapping("/trilha/{trilhaId}")
    public ResponseEntity<List<AulaDTO>> getAulasByTrilha(@PathVariable Long trilhaId) {
        List<Aula> aulas = aulaService.getAulasByTrilha(trilhaId);
        List<AulaDTO> dtos = aulas.stream()
                .map(AulaDTO::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/v1/aulas/{id}
     * Get a specific aula by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AulaDTO> getAulaById(@PathVariable Long id) {
        Aula aula = aulaService.getAulaById(id);
        return ResponseEntity.ok(new AulaDTO(aula));
    }

    /**
     * POST /api/v1/aulas
     * Create a new aula.
     * Required fields: titulo, tipo, conteudo, trilhaId
     */
    @PostMapping
    public ResponseEntity<AulaDTO> createAula(@RequestBody Aula aula) {
        Aula novaAula = aulaService.createAula(aula);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AulaDTO(novaAula));
    }

    /**
     * PUT /api/v1/aulas/{id}
     * Update a aula.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AulaDTO> updateAula(
            @PathVariable Long id,
            @RequestBody Aula aulaUpdate) {

        Aula atualizada = aulaService.updateAula(id, aulaUpdate);
        return ResponseEntity.ok(new AulaDTO(atualizada));
    }

    /**
     * DELETE /api/v1/aulas/{id}
     * Delete a aula.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAula(@PathVariable Long id) {
        aulaService.deleteAula(id);
        return ResponseEntity.ok("Aula deletada com sucesso.");
    }
}