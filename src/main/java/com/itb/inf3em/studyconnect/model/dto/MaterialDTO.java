package com.itb.inf3em.studyconnect.model.dto;

import com.itb.inf3em.studyconnect.model.entity.Material;
import java.time.LocalDateTime;

// DTO (Data Transfer Object) — objeto que carrega os dados do Material
// entre o back-end e o front-end. Evita expor a entidade diretamente.
public class MaterialDTO {

    private Long id;
    private String titulo;
    private String materia;
    private String tipo;
    private String categoria;
    private String url;
    private Long usuarioId;
    private LocalDateTime criadoEm;

    // Constrói o DTO a partir da entidade Material
    public MaterialDTO(Material material) {
        this.id        = material.getId();
        this.titulo    = material.getTitulo();
        this.materia   = material.getMateria();
        this.tipo      = material.getTipo();
        this.categoria = material.getCategoria();
        this.url       = material.getUrl();
        this.usuarioId = material.getUsuarioId();
        this.criadoEm  = material.getCriadoEm();
    }

    // Construtor vazio necessário para o Jackson (deserialização JSON)
    public MaterialDTO() {}

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
