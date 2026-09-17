package com.itb.inf3em.studyconnect.controller;

import com.itb.inf3em.studyconnect.model.dto.ProgressoAulaDTO;
import com.itb.inf3em.studyconnect.model.dto.ProgressoDTO;
import com.itb.inf3em.studyconnect.model.services.ProgressoAulaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/progresso")
public class ProgressoAulaController {

    @Autowired
    private ProgressoAulaService progressoService;

    /**
     * POST /api/v1/progresso/concluir
     * Body: { "alunoId": 1, "aulaId": 2 }
     */
    @PostMapping("/concluir")
    public ResponseEntity<Void> concluir(@RequestBody Map<String, Long> body) {
        progressoService.concluirAula(body.get("alunoId"), body.get("aulaId"));
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/v1/progresso/aluno/{alunoId}
     * Retorna objetos { aulaId, concluidaEm } — usado pelo gráfico semanal.
     */
    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<ProgressoAulaDTO>> getByAluno(@PathVariable Long alunoId) {
        List<ProgressoAulaDTO> lista = progressoService.getProgressoCompleto(alunoId)
                .stream()
                .map(ProgressoAulaDTO::new)
                .toList();
        return ResponseEntity.ok(lista);
    }

    /**
     * GET /api/v1/progresso/aluno/{alunoId}/ids
     * Retorna só List<Long> de aulaIds — usado pelo hook useTrilhasAluno.
     */
    @GetMapping("/aluno/{alunoId}/ids")
    public ResponseEntity<List<Long>> getIdsByAluno(@PathVariable Long alunoId) {
        return ResponseEntity.ok(progressoService.getAulasConcluidas(alunoId));
    }

    /**
     * GET /api/v1/progresso/trilha/{trilhaId}/aluno/{alunoId}
     * Retorna { aulasConcluidas, totalAulas, percentual } para a trilha.
     */
    @GetMapping("/trilha/{trilhaId}/aluno/{alunoId}")
    public ResponseEntity<ProgressoDTO> getByTrilha(
            @PathVariable Long trilhaId,
            @PathVariable Long alunoId) {
        return ResponseEntity.ok(progressoService.getProgressoTrilha(trilhaId, alunoId));
    }
}
