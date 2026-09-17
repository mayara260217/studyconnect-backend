package com.itb.inf3em.studyconnect.controller;

import com.itb.inf3em.studyconnect.model.dto.MatriculaAlunoDTO;
import com.itb.inf3em.studyconnect.model.dto.MatriculaTrilhaDTO;
import com.itb.inf3em.studyconnect.model.entity.MatriculaTrilha;
import com.itb.inf3em.studyconnect.model.services.MatriculaTrilhaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/matriculas")
public class MatriculaTrilhaController {

    @Autowired
    private MatriculaTrilhaService matriculaService;

    /**
     * POST /api/v1/matriculas
     * Body: { "alunoId": 1, "trilhaId": 2 }
     */
    @PostMapping
    public ResponseEntity<MatriculaTrilhaDTO> matricular(@RequestBody Map<String, Long> body) {
        Long alunoId  = body.get("alunoId");
        Long trilhaId = body.get("trilhaId");

        if (trilhaId == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            MatriculaTrilha m = matriculaService.matricular(alunoId, trilhaId);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MatriculaTrilhaDTO(m));
        } catch (MatriculaTrilhaService.AlreadySavedException e) {
            // Reativação já foi salva no service — retorna 200
            return ResponseEntity.ok().build();
        }
    }

    /**
     * DELETE /api/v1/matriculas/{trilhaId}/aluno/{alunoId}
     */
    @DeleteMapping("/{trilhaId}/aluno/{alunoId}")
    public ResponseEntity<Void> desmatricular(
            @PathVariable Long trilhaId,
            @PathVariable Long alunoId) {
        matriculaService.desmatricular(alunoId, trilhaId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/matriculas/aluno/{alunoId}
     */
    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<MatriculaTrilhaDTO>> listarPorAluno(@PathVariable Long alunoId) {
        List<MatriculaTrilhaDTO> lista = matriculaService.listarPorAluno(alunoId)
                .stream()
                .map(MatriculaTrilhaDTO::new)
                .toList();
        return ResponseEntity.ok(lista);
    }

    /**
     * GET /api/v1/matriculas/trilha/{trilhaId}
     * Lista alunos matriculados em uma trilha específica
     */
    @GetMapping("/trilha/{trilhaId}")
    public ResponseEntity<List<MatriculaAlunoDTO>> listarPorTrilha(@PathVariable Long trilhaId) {
        return ResponseEntity.ok(matriculaService.listarPorTrilha(trilhaId));
    }

    /**
     * GET /api/v1/matriculas/professor/{professorId}/resumo
     * Retorna stats agregadas para o dashboard do professor
     */
    @GetMapping("/professor/{professorId}/resumo")
    public ResponseEntity<Map<String, Object>> resumoProfessor(@PathVariable Long professorId) {
        return ResponseEntity.ok(matriculaService.resumoProfessor(professorId));
    }

    /**
     * GET /api/v1/matriculas/trilha/{trilhaId}/estatisticas
     * Retorna stats da trilha para o professor
     */
    @GetMapping("/trilha/{trilhaId}/estatisticas")
    public ResponseEntity<Map<String, Object>> estatisticasTrilha(@PathVariable Long trilhaId) {
        return ResponseEntity.ok(matriculaService.estatisticasTrilha(trilhaId));
    }

    /**
     * GET /api/v1/matriculas/existe?alunoId=X&trilhaId=Y
     */
    @GetMapping("/existe")
    public ResponseEntity<Map<String, Boolean>> verificar(
            @RequestParam Long alunoId,
            @RequestParam Long trilhaId) {
        boolean existe = matriculaService.verificarMatricula(alunoId, trilhaId);
        return ResponseEntity.ok(Map.of("matriculado", existe));
    }
}
