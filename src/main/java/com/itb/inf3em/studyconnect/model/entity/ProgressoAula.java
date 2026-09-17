package com.itb.inf3em.studyconnect.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ProgressoAula",
       uniqueConstraints = @UniqueConstraint(columnNames = {"aluno_id", "aula_id"}))
public class ProgressoAula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aluno_id", nullable = false)
    private Long alunoId;

    @Column(name = "aula_id", nullable = false)
    private Long aulaId;

    @Column(nullable = false)
    private boolean concluida = false;

    @Column(name = "concluida_em")
    private LocalDateTime concluidaEm;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        if (this.concluida && this.concluidaEm == null) {
            this.concluidaEm = LocalDateTime.now();
        }
    }

    public ProgressoAula() {}

    public ProgressoAula(Long alunoId, Long aulaId) {
        this.alunoId = alunoId;
        this.aulaId  = aulaId;
    }

    public Long getId()                    { return id; }
    public Long getAlunoId()               { return alunoId; }
    public void setAlunoId(Long alunoId)   { this.alunoId = alunoId; }
    public Long getAulaId()                { return aulaId; }
    public void setAulaId(Long aulaId)     { this.aulaId = aulaId; }
    public boolean isConcluida()           { return concluida; }
    public void setConcluida(boolean c)    { this.concluida = c; }
    public LocalDateTime getConcluidaEm()  { return concluidaEm; }
    public void setConcluidaEm(LocalDateTime t) { this.concluidaEm = t; }
}
