package com.itb.inf3em.studyconnect.model.dto;

public class ResetPasswordRequestDTO {

    private String token;
    private String novaSenha;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getNovaSenha() { return novaSenha; }
    public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }
}
