package com.itb.inf3em.studyconnect.controller;

import com.itb.inf3em.studyconnect.model.dto.DuvidaDTO;
import com.itb.inf3em.studyconnect.model.services.DuvidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/duvidas")
public class DuvidaController {

    @Autowired
    private DuvidaService duvidaService;

    /** POST /api/v1/duvidas
     *  Body: { alunoId, aulaId, trilhaId, mensagem } */
    @PostMapping
    public ResponseEntity<DuvidaDTO> criar(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(duvidaService.criar(body));
    }

    /** GET /api/v1/duvidas?alunoId=X — todas as dúvidas do aluno */
    @GetMapping
    public ResponseEntity<List<DuvidaDTO>> listar(@RequestParam(required = false) Long alunoId) {
        if (alunoId != null) return ResponseEntity.ok(duvidaService.listarPorAluno(alunoId));
        return ResponseEntity.ok(List.of());
    }

    /** PUT /api/v1/duvidas/{id}/responder
     *  Body: { "resposta": "..." } */
    @PutMapping("/{id}/responder")
    public ResponseEntity<DuvidaDTO> responder(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(duvidaService.responder(id, body.get("resposta")));
    }

    /** PUT /api/v1/duvidas/{id}/resolver */
    @PutMapping("/{id}/resolver")
    public ResponseEntity<DuvidaDTO> resolver(@PathVariable Long id) {
        return ResponseEntity.ok(duvidaService.resolver(id));
    }

    /** GET /api/v1/duvidas/trilha/{trilhaId} */
    @GetMapping("/trilha/{trilhaId}")
    public ResponseEntity<List<DuvidaDTO>> porTrilha(@PathVariable Long trilhaId) {
        return ResponseEntity.ok(duvidaService.listarPorTrilha(trilhaId));
    }

    /** GET /api/v1/duvidas/aula/{aulaId} */
    @GetMapping("/aula/{aulaId}")
    public ResponseEntity<List<DuvidaDTO>> porAula(@PathVariable Long aulaId) {
        return ResponseEntity.ok(duvidaService.listarPorAula(aulaId));
    }

    /** GET /api/v1/duvidas/aluno/{alunoId}/aula/{aulaId} */
    @GetMapping("/aluno/{alunoId}/aula/{aulaId}")
    public ResponseEntity<List<DuvidaDTO>> porAlunoEAula(
            @PathVariable Long alunoId,
            @PathVariable Long aulaId) {
        return ResponseEntity.ok(duvidaService.listarPorAlunoEAula(alunoId, aulaId));
    }
}
