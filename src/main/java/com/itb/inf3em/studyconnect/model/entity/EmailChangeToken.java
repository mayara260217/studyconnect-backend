package com.itb.inf3em.studyconnect.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "EmailChangeToken")
public class EmailChangeToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "email_atual", nullable = false)
    private String emailAtual;

    @Column(name = "email_novo", nullable = false)
    private String emailNovo;

    /** UUID enviado no link de confirmação para o e-mail atual (etapa 1) */
    @Column(name = "confirm_token", nullable = false, unique = true)
    private String confirmToken;

    /** OTP de 6 dígitos enviado para o novo e-mail (etapa 2) */
    @Column(name = "otp_code", length = 6)
    private String otpCode;

    /** STEP1 = aguardando confirmação no e-mail atual | STEP2 = aguardando OTP no novo e-mail */
    @Column(nullable = false, length = 10)
    private String etapa;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public EmailChangeToken() {}

    public EmailChangeToken(Long usuarioId, String emailAtual, String emailNovo,
                            String confirmToken, Instant expiresAt) {
        this.usuarioId    = usuarioId;
        this.emailAtual   = emailAtual;
        this.emailNovo    = emailNovo;
        this.confirmToken = confirmToken;
        this.expiresAt    = expiresAt;
        this.etapa        = "STEP1";
    }

    public Long getId()                        { return id; }
    public Long getUsuarioId()                 { return usuarioId; }
    public String getEmailAtual()              { return emailAtual; }
    public String getEmailNovo()               { return emailNovo; }
    public String getConfirmToken()            { return confirmToken; }
    public String getOtpCode()                 { return otpCode; }
    public void   setOtpCode(String otpCode)   { this.otpCode = otpCode; }
    public String getEtapa()                   { return etapa; }
    public void   setEtapa(String etapa)       { this.etapa = etapa; }
    public Instant getExpiresAt()              { return expiresAt; }
    public void    setExpiresAt(Instant t)     { this.expiresAt = t; }
}
