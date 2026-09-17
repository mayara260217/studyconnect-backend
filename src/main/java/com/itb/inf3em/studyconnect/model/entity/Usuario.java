package com.itb.inf3em.studyconnect.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 45, nullable = false)
    private String nome;

    @Column(length = 45, nullable = false, unique = true)
    private String email;

    @Column(length = 255)
    private String senha;

    @Column(name = "google_id", length = 255)
    private String googleId;

    @Column(name = "foto_url", columnDefinition = "NVARCHAR(MAX)")
    private String fotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", length = 20, nullable = false)
    private TipoUsuario tipoUsuario;

    @Column(nullable = false)
    private boolean ativo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public TipoUsuario getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(TipoUsuario tipoUsuario) { this.tipoUsuario = tipoUsuario; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

}
