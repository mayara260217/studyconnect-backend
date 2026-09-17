package com.itb.inf3em.studyconnect.model.dto;

import com.itb.inf3em.studyconnect.model.entity.Duvida;
import java.time.LocalDateTime;

public class DuvidaDTO {

    private Long id;
    private Long alunoId;
    private String alunoNome;
    private Long aulaId;
    private String aulaTitulo;
    private Long trilhaId;
    private String mensagem;
    private String resposta;
    private String status;
    private LocalDateTime criadaEm;
    private LocalDateTime respondidaEm;

    public DuvidaDTO() {}

    public DuvidaDTO(Duvida d, String alunoNome, String aulaTitulo) {
        this.id           = d.getId();
        this.alunoId      = d.getAlunoId();
        this.alunoNome    = alunoNome;
        this.aulaId       = d.getAulaId();
        this.aulaTitulo   = aulaTitulo;
        this.trilhaId     = d.getTrilhaId();
        this.mensagem     = d.getMensagem();
        this.resposta     = d.getResposta();
        this.status       = d.getStatus();
        this.criadaEm     = d.getCriadaEm();
        this.respondidaEm = d.getRespondidaEm();
    }

    public Long getId()                    { return id; }
    public Long getAlunoId()               { return alunoId; }
    public String getAlunoNome()           { return alunoNome; }
    public Long getAulaId()                { return aulaId; }
    public String getAulaTitulo()          { return aulaTitulo; }
    public Long getTrilhaId()              { return trilhaId; }
    public String getMensagem()            { return mensagem; }
    public String getResposta()            { return resposta; }
    public String getStatus()              { return status; }
    public LocalDateTime getCriadaEm()     { return criadaEm; }
    public LocalDateTime getRespondidaEm() { return respondidaEm; }
}
