package com.itb.inf3em.studyconnect.model.dto;

import com.itb.inf3em.studyconnect.model.entity.ProgressoAula;
import java.time.LocalDateTime;

public class ProgressoAulaDTO {

    private Long aulaId;
    private LocalDateTime concluidaEm;

    public ProgressoAulaDTO(ProgressoAula p) {
        this.aulaId      = p.getAulaId();
        this.concluidaEm = p.getConcluidaEm();
    }

    public Long getAulaId()                 { return aulaId; }
    public LocalDateTime getConcluidaEm()   { return concluidaEm; }
}
