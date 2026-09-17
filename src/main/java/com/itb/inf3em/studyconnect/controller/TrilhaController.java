package com.itb.inf3em.studyconnect.controller;

import com.itb.inf3em.studyconnect.model.dto.TrilhaDTO;
import com.itb.inf3em.studyconnect.model.entity.Trilha;
import com.itb.inf3em.studyconnect.model.services.TrilhaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trilhas")
public class TrilhaController {

    @Autowired
    private TrilhaService trilhaService;

    /** GET /api/v1/trilhas
     *  GET /api/v1/trilhas?professorId=X  → filtra por professor */
    @GetMapping
    public ResponseEntity<List<TrilhaDTO>> findAll(
            @RequestParam(required = false) Long professorId) {
        List<Trilha> trilhas = (professorId != null)
                ? trilhaService.getTrilhasByProfessor(professorId)
                : trilhaService.getAllTrilhas();
        return ResponseEntity.ok(trilhas.stream().map(TrilhaDTO::new).toList());
    }

    /** GET /api/v1/trilhas/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<TrilhaDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(new TrilhaDTO(trilhaService.getTrilhaById(id)));
    }

    /** POST /api/v1/trilhas */
    @PostMapping
    public ResponseEntity<TrilhaDTO> create(@RequestBody Trilha trilha) {
        Trilha nova = trilhaService.createTrilha(trilha);
        return ResponseEntity.status(HttpStatus.CREATED).body(new TrilhaDTO(nova));
    }

    /** PUT /api/v1/trilhas/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<TrilhaDTO> update(@PathVariable Long id,
                                            @RequestBody Trilha trilha) {
        Trilha atualizada = trilhaService.updateTrilha(id, trilha);
        return ResponseEntity.ok(new TrilhaDTO(atualizada));
    }

    /** DELETE /api/v1/trilhas/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        trilhaService.deleteTrilha(id);
        return ResponseEntity.ok("Trilha deletada com sucesso.");
    }
}
