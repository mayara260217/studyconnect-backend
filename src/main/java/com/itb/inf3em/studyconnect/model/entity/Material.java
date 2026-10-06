package com.itb.inf3em.studyconnect.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Representa um material de estudo enviado por um usuário (PDF, Resumo, Word, Mapa Mental).
// Cada material pertence a um usuário (quem enviou) e pode ser associado a uma matéria.
@Entity
@Table(name = "material")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Título do material (ex: "Resumo de Frações")
    @Column(length = 150, nullable = false)
    private String titulo;

    // Matéria relacionada (ex: "Matemática", "Português")
    @Column(length = 100, nullable = false)
    private String materia;

    // Tipo do arquivo: PDF, Resumo, Word, Mapa Mental
    @Column(length = 50, nullable = false)
    private String tipo;

    // Categoria usada para filtro na biblioteca (ex: "Resumos", "PDFs")
    @Column(length = 50, nullable = false)
    private String categoria;

    // URL de acesso ao arquivo (pode ser link externo ou caminho interno)
    @Column(length = 500)
    private String url;

    // ID do usuário que enviou o material
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    // Data e hora em que o material foi enviado — preenchido automaticamente
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    // Constructors
    public Material() {}

    // Preenchido automaticamente pelo JPA antes de salvar pela primeira vez
    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMateria() { return materia; }
    public void setMateria(String materia) { this.materia = materia; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
