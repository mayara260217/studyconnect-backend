package com.itb.inf3em.studyconnect.model.dto;

import java.util.List;

public class ProgressoDTO {

    private List<Long> aulasConcluidas;
    private long totalAulas;
    private int percentual;

    public ProgressoDTO(List<Long> aulasConcluidas, long totalAulas) {
        this.aulasConcluidas = aulasConcluidas;
        this.totalAulas      = totalAulas;
        this.percentual      = totalAulas == 0 ? 0
                : (int) Math.round((aulasConcluidas.size() * 100.0) / totalAulas);
    }

    public List<Long> getAulasConcluidas() { return aulasConcluidas; }
    public long getTotalAulas()            { return totalAulas; }
    public int getPercentual()             { return percentual; }
}
