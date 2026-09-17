package com.itb.inf3em.studyconnect.model.dto;

import com.itb.inf3em.studyconnect.model.entity.Turma;
import java.time.LocalDateTime;

public class TurmaDTO {

    private Long id;
    private String nome;
    private String descricao;
    private String codigo;
    private String tipo;
    private String nivel;
    private Long professorId;
    private String professorNome;
    private LocalDateTime criadaEm;
    private LocalDateTime atualizadaEm;

    // Constructor from Turma entity
    public TurmaDTO(Turma turma) {
        this.id = turma.getId();
        this.nome = turma.getNome();
        this.descricao = turma.getDescricao();
        this.codigo = turma.getCodigo();
        this.tipo = turma.getTipo();
        this.nivel = turma.getNivel();
        this.professorId = turma.getProfessorId();
        this.professorNome = turma.getProfessorNome();
        this.criadaEm = turma.getCriadaEm();
        this.atualizadaEm = turma.getAtualizadaEm();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNivel() {
        return nivel;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public String getProfessorNome() {
        return professorNome;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }

    public void setProfessorNome(String professorNome) {
        this.professorNome = professorNome;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }

    public void setAtualizadaEm(LocalDateTime atualizadaEm) {
        this.atualizadaEm = atualizadaEm;
    }
}
