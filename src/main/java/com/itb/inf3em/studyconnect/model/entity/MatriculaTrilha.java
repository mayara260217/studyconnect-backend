package com.itb.inf3em.studyconnect.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "MatriculaTrilha",
       uniqueConstraints = @UniqueConstraint(columnNames = {"aluno_id", "trilha_id"}))
public class MatriculaTrilha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aluno_id", nullable = false)
    private Long alunoId;

    @Column(name = "trilha_id", nullable = false)
    private Long trilhaId;

    @Column(name = "data_matricula", nullable = false, updatable = false)
    private LocalDateTime dataMatricula;

    @Column(nullable = false)
    private boolean ativo = true;

    @PrePersist
    protected void onCreate() {
        this.dataMatricula = LocalDateTime.now();
        this.ativo = true;
    }

    public MatriculaTrilha() {}

    public MatriculaTrilha(Long alunoId, Long trilhaId) {
        this.alunoId = alunoId;
        this.trilhaId = trilhaId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAlunoId() { return alunoId; }
    public void setAlunoId(Long alunoId) { this.alunoId = alunoId; }

    public Long getTrilhaId() { return trilhaId; }
    public void setTrilhaId(Long trilhaId) { this.trilhaId = trilhaId; }

    public LocalDateTime getDataMatricula() { return dataMatricula; }
    public void setDataMatricula(LocalDateTime dataMatricula) { this.dataMatricula = dataMatricula; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
