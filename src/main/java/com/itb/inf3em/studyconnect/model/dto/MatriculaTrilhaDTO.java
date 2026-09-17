package com.itb.inf3em.studyconnect.model.dto;

import com.itb.inf3em.studyconnect.model.entity.MatriculaTrilha;
import java.time.LocalDateTime;

public class MatriculaTrilhaDTO {

    private Long id;
    private Long alunoId;
    private Long trilhaId;
    private LocalDateTime dataMatricula;
    private boolean ativo;

    public MatriculaTrilhaDTO() {}

    public MatriculaTrilhaDTO(MatriculaTrilha m) {
        this.id            = m.getId();
        this.alunoId       = m.getAlunoId();
        this.trilhaId      = m.getTrilhaId();
        this.dataMatricula = m.getDataMatricula();
        this.ativo         = m.isAtivo();
    }

    public Long getId() { return id; }
    public Long getAlunoId() { return alunoId; }
    public Long getTrilhaId() { return trilhaId; }
    public LocalDateTime getDataMatricula() { return dataMatricula; }
    public boolean isAtivo() { return ativo; }
}
