package com.itb.inf3em.studyconnect.controller;

import com.itb.inf3em.studyconnect.model.dto.TurmaDTO;
import com.itb.inf3em.studyconnect.model.entity.Turma;
import com.itb.inf3em.studyconnect.model.services.TurmaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/turmas")
public class TurmaController {

    @Autowired
    private TurmaService turmaService;

    /**
     * GET /api/v1/turmas
     * Get all turmas (public view).
     */
    @GetMapping
    public ResponseEntity<List<TurmaDTO>> getAllTurmas() {
        List<Turma> turmas = turmaService.getAllTurmas();
        List<TurmaDTO> dtos = turmas.stream()
                .map(TurmaDTO::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/v1/turmas/minhas
     * Get all turmas for the current user (either teaching or enrolled).
     * Query param: professorId (required for now)
     */
    @GetMapping("/minhas")
    public ResponseEntity<List<TurmaDTO>> getMyTurmas(@RequestParam(required = false) Long professorId) {
        List<Turma> turmas = turmaService.getMyTurmas(professorId);
        List<TurmaDTO> dtos = turmas.stream()
                .map(TurmaDTO::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * POST /api/v1/turmas/entrar
     * Join a turma using codigo.
     * Request body: { "codigo": "MAT-3A-7X2K" }
     */
    @PostMapping("/entrar")
    public ResponseEntity<TurmaDTO> joinTurma(@RequestBody Map<String, String> request) {
        String codigo = request.get("codigo");

        if (codigo == null || codigo.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Turma turma = turmaService.joinTurmaByCode(codigo);
        return ResponseEntity.ok(new TurmaDTO(turma));
    }

    /**
     * GET /api/v1/turmas/professor/{professorId}
     * Get all turmas created by a specific professor.
     */
    @GetMapping("/professor/{professorId}")
    public ResponseEntity<List<TurmaDTO>> getTurmasByProfessor(@PathVariable Long professorId) {
        List<Turma> turmas = turmaService.getTurmasByProfessor(professorId);
        List<TurmaDTO> dtos = turmas.stream()
                .map(TurmaDTO::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * POST /api/v1/turmas
     * Create a new turma.
     * Required fields: nome, codigo, professorId, professorNome
     */
    @PostMapping
    public ResponseEntity<TurmaDTO> createTurma(@RequestBody Turma turma) {
        Turma novaTurma = turmaService.createTurma(turma);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new TurmaDTO(novaTurma));
    }

    /**
     * GET /api/v1/turmas/{id}
     * Get a specific turma by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TurmaDTO> getTurmaById(@PathVariable Long id) {
        Turma turma = turmaService.getTurmaById(id);
        return ResponseEntity.ok(new TurmaDTO(turma));
    }

    /**
     * POST /api/v1/turmas/{id}/sair
     * Leave a turma (student leaving an enrolled class).
     */
    @PostMapping("/{id}/sair")
    public ResponseEntity<String> leaveTurma(@PathVariable Long id) {
        // In production, use current user context from SecurityContext
        // For now, just verify the turma exists
        turmaService.getTurmaById(id);  // Throws 404 if not found
        return ResponseEntity.ok("Você saiu da turma com sucesso.");
    }

    /**
     * PUT /api/v1/turmas/{id}
     * Update a turma (professor only).
     * Requires professorId header or query param for verification.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TurmaDTO> updateTurma(
            @PathVariable Long id,
            @RequestBody Turma turmaUpdate,
            @RequestParam(required = false) Long professorId) {

        Turma atualizada = turmaService.updateTurma(id, turmaUpdate, professorId);
        return ResponseEntity.ok(new TurmaDTO(atualizada));
    }

    /**
     * DELETE /api/v1/turmas/{id}
     * Delete a turma (professor only).
     * Requires professorId query param for verification.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTurma(
            @PathVariable Long id,
            @RequestParam(required = false) Long professorId) {

        turmaService.deleteTurma(id, professorId);
        return ResponseEntity.ok("Turma deletada com sucesso.");
    }
}
