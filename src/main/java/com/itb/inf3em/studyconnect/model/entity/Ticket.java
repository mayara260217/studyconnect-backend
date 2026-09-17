package com.itb.inf3em.studyconnect.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(length = 150)
    private String nome;

    @Column(length = 150)
    private String email;

    @Column(length = 100, nullable = false)
    private String tipo;

    @Column(length = 2000, nullable = false)
    private String mensagem;

    @Column(length = 2000)
    private String resposta;

    @Column(length = 20, nullable = false)
    private String status = "ABERTO"; // ABERTO | RESPONDIDO | FECHADO

    @Column(name = "criada_em", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    @Column(name = "respondida_em")
    private LocalDateTime respondidaEm;

    @PrePersist
    protected void onCreate() { this.criadaEm = LocalDateTime.now(); }

    public Ticket() {}

    public Long getId()                          { return id; }
    public Long getUsuarioId()                   { return usuarioId; }
    public void setUsuarioId(Long usuarioId)     { this.usuarioId = usuarioId; }
    public String getNome()                      { return nome; }
    public void setNome(String nome)             { this.nome = nome; }
    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }
    public String getTipo()                      { return tipo; }
    public void setTipo(String tipo)             { this.tipo = tipo; }
    public String getMensagem()                  { return mensagem; }
    public void setMensagem(String mensagem)     { this.mensagem = mensagem; }
    public String getResposta()                  { return resposta; }
    public void setResposta(String resposta)     { this.resposta = resposta; }
    public String getStatus()                    { return status; }
    public void setStatus(String status)         { this.status = status; }
    public LocalDateTime getCriadaEm()           { return criadaEm; }
    public LocalDateTime getRespondidaEm()       { return respondidaEm; }
    public void setRespondidaEm(LocalDateTime t) { this.respondidaEm = t; }
}
