package com.itb.inf3em.studyconnect.model.dto;

import com.itb.inf3em.studyconnect.model.entity.Aula;
import java.time.LocalDateTime;

public class AulaDTO {

    private Long id;
    private String titulo;
    private String tipo;
    private String conteudo;
    private Long trilhaId;
    private Integer ordem;
    private LocalDateTime criadaEm;
    private LocalDateTime atualizadaEm;
    private String status;

    // Constructor from Aula entity
    public AulaDTO(Aula aula) {
        this.id = aula.getId();
        this.titulo = aula.getTitulo();
        this.tipo = aula.getTipo();
        this.conteudo = aula.getConteudo();
        this.trilhaId = aula.getTrilhaId();
        this.ordem = aula.getOrdem();
        this.criadaEm = aula.getCriadaEm();
        this.atualizadaEm = aula.getAtualizadaEm();
        this.status = aula.getStatus();
    }

    // Default constructor
    public AulaDTO() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public Long getTrilhaId() {
        return trilhaId;
    }

    public void setTrilhaId(Long trilhaId) {
        this.trilhaId = trilhaId;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}