package com.itb.inf3em.studyconnect.model.dto;

import com.itb.inf3em.studyconnect.model.entity.Ticket;
import java.time.LocalDateTime;

public class TicketDTO {

    private Long id;
    private Long usuarioId;
    private String nome;
    private String email;
    private String tipo;
    private String mensagem;
    private String resposta;
    private String status;
    private LocalDateTime criadaEm;
    private LocalDateTime respondidaEm;

    public TicketDTO() {}

    public TicketDTO(Ticket t) {
        this.id           = t.getId();
        this.usuarioId    = t.getUsuarioId();
        this.nome         = t.getNome();
        this.email        = t.getEmail();
        this.tipo         = t.getTipo();
        this.mensagem     = t.getMensagem();
        this.resposta     = t.getResposta();
        this.status       = t.getStatus();
        this.criadaEm     = t.getCriadaEm();
        this.respondidaEm = t.getRespondidaEm();
    }

    public Long getId()                    { return id; }
    public Long getUsuarioId()             { return usuarioId; }
    public String getNome()                { return nome; }
    public String getEmail()               { return email; }
    public String getTipo()                { return tipo; }
    public String getMensagem()            { return mensagem; }
    public String getResposta()            { return resposta; }
    public String getStatus()              { return status; }
    public LocalDateTime getCriadaEm()     { return criadaEm; }
    public LocalDateTime getRespondidaEm() { return respondidaEm; }
}
