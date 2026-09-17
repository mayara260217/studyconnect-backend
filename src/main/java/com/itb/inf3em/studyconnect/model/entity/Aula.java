package com.itb.inf3em.studyconnect.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Aula")
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String titulo;

    @Column(length = 20)  // nullable: frontend usa blocos, nao tipo
    private String tipo;

    @Column(columnDefinition = "NVARCHAR(MAX)")  // blocos JSON
    private String conteudo;

    @Column(name = "trilha_id", nullable = false)
    private Long trilhaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trilha_id", insertable = false, updatable = false)
    @JsonBackReference
    private Trilha trilha;

    @Column
    private Integer ordem;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    @Column(name = "atualizada_em", nullable = false)
    private LocalDateTime atualizadaEm;

    @Column(length = 20, nullable = false, columnDefinition = "NVARCHAR(20) NOT NULL DEFAULT 'PUBLICADA'")
    private String status = "PUBLICADA";  // PUBLICADA | RASCUNHO

    // Constructors
    public Aula() {}

    public Aula(String titulo, String tipo, String conteudo, Long trilhaId, Integer ordem) {
        this.titulo = titulo;
        this.tipo = tipo;
        this.conteudo = conteudo;
        this.trilhaId = trilhaId;
        this.ordem = ordem;
    }

    // JPA Lifecycle Callbacks
    @PrePersist
    protected void onCreate() {
        this.criadaEm = LocalDateTime.now();
        this.atualizadaEm = LocalDateTime.now();
        if (this.status == null) this.status = "PUBLICADA";
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

    public Trilha getTrilha() {
        return trilha;
    }

    public void setTrilha(Trilha trilha) {
        this.trilha = trilha;
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