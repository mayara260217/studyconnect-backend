package com.itb.inf3em.studyconnect.model.dto;

import com.itb.inf3em.studyconnect.model.entity.Usuario;

public class UsuarioDTO {

    private Long id;
    private String nome;
    private String email;
    private String tipoUsuario;
    private boolean ativo;

    public UsuarioDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.tipoUsuario = usuario.getTipoUsuario().name();
        this.ativo = usuario.isAtivo();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getTipoUsuario() { return tipoUsuario; }
    public boolean isAtivo() { return ativo; }
}
