package com.itb.inf3em.studyconnect.controller;

import com.itb.inf3em.studyconnect.model.dto.PerfilAprendizadoDTO;
import com.itb.inf3em.studyconnect.model.entity.PerfilAprendizado;
import com.itb.inf3em.studyconnect.model.services.PerfilAprendizadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/perfil-aprendizado")
public class PerfilAprendizadoController {

    @Autowired
    private PerfilAprendizadoService service;

    /** GET /api/v1/perfil-aprendizado/:alunoId — retorna 404 se não existir */
    @GetMapping("/{alunoId}")
    public ResponseEntity<PerfilAprendizadoDTO> get(@PathVariable Long alunoId) {
        return service.findByAluno(alunoId)
            .map(p -> ResponseEntity.ok(new PerfilAprendizadoDTO(p)))
            .orElse(ResponseEntity.notFound().build());
    }

    /** POST /api/v1/perfil-aprendizado — cria perfil (primeiro acesso) */
    @PostMapping
    public ResponseEntity<PerfilAprendizadoDTO> create(@RequestBody PerfilAprendizadoDTO dto) {
        PerfilAprendizado saved = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PerfilAprendizadoDTO(saved));
    }

    /** PUT /api/v1/perfil-aprendizado/:alunoId — atualiza perfil existente */
    @PutMapping("/{alunoId}")
    public ResponseEntity<PerfilAprendizadoDTO> update(
            @PathVariable Long alunoId,
            @RequestBody PerfilAprendizadoDTO dto) {
        dto.setAlunoId(alunoId);
        PerfilAprendizado saved = service.update(alunoId, dto);
        return ResponseEntity.ok(new PerfilAprendizadoDTO(saved));
    }
}
