package com.itb.inf3em.studyconnect.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "email_verification_token")
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    private boolean verified;

    public EmailVerificationToken() {}

    public EmailVerificationToken(String email, String code, Instant expiresAt) {
        this.email = email;
        this.code = code;
        this.expiresAt = expiresAt;
        this.verified = false;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}
