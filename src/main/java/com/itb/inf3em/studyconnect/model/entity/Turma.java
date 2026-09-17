package com.itb.inf3em.studyconnect.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Turma")
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Column(length = 50, nullable = false, unique = true)
    private String codigo;

    @Column(length = 20)
    private String tipo;  // PUBLICA or PRIVADA

    @Column(length = 50)
    private String nivel;  // Fundamental, Médio, Superior, etc.

    @Column(name = "professor_id", nullable = false)
    private Long professorId;

    @Column(name = "professor_nome", nullable = false)
    private String professorNome;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    @Column(name = "atualizada_em", nullable = false)
    private LocalDateTime atualizadaEm;

    // Constructors
    public Turma() {}

    public Turma(String nome, String descricao, String codigo, String tipo, String nivel, Long professorId, String professorNome) {
        this.nome = nome;
        this.descricao = descricao;
        this.codigo = codigo;
        this.tipo = tipo;
        this.nivel = nivel;
        this.professorId = professorId;
        this.professorNome = professorNome;
    }

    // JPA Lifecycle Callbacks
    @PrePersist
    protected void onCreate() {
        this.criadaEm = LocalDateTime.now();
        this.atualizadaEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.atualizadaEm = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }

    public String getProfessorNome() {
        return professorNome;
    }

    public void setProfessorNome(String professorNome) {
        this.professorNome = professorNome;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }

    public void setAtualizadaEm(LocalDateTime atualizadaEm) {
        this.atualizadaEm = atualizadaEm;
    }
}
