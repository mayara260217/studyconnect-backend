package com.itb.inf3em.studyconnect.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Duvida")
public class Duvida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aluno_id", nullable = false)
    private Long alunoId;

    @Column(name = "aula_id", nullable = false)
    private Long aulaId;

    @Column(name = "trilha_id", nullable = false)
    private Long trilhaId;

    @Column(length = 1000, nullable = false)
    private String mensagem;

    @Column(length = 1000)
    private String resposta;

    @Column(length = 20, nullable = false)
    private String status = "PENDENTE"; // PENDENTE | RESPONDIDA

    @Column(name = "criada_em", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    @Column(name = "respondida_em")
    private LocalDateTime respondidaEm;

    @PrePersist
    protected void onCreate() {
        this.criadaEm = LocalDateTime.now();
    }

    public Duvida() {}

    public Long getId()                        { return id; }
    public Long getAlunoId()                   { return alunoId; }
    public void setAlunoId(Long alunoId)       { this.alunoId = alunoId; }
    public Long getAulaId()                    { return aulaId; }
    public void setAulaId(Long aulaId)         { this.aulaId = aulaId; }
    public Long getTrilhaId()                  { return trilhaId; }
    public void setTrilhaId(Long trilhaId)     { this.trilhaId = trilhaId; }
    public String getMensagem()                { return mensagem; }
    public void setMensagem(String mensagem)   { this.mensagem = mensagem; }
    public String getResposta()                { return resposta; }
    public void setResposta(String resposta)   { this.resposta = resposta; }
    public String getStatus()                  { return status; }
    public void setStatus(String status)       { this.status = status; }
    public LocalDateTime getCriadaEm()         { return criadaEm; }
    public LocalDateTime getRespondidaEm()     { return respondidaEm; }
    public void setRespondidaEm(LocalDateTime t) { this.respondidaEm = t; }
}
