package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationSeconds;

    public JwtService(@Value("${app.security.jwt.secret}") String encodedSecret,
                      @Value("${app.security.jwt.expiration-seconds:900}") long expirationSeconds) {
        if (encodedSecret == null || encodedSecret.isBlank()) {
            throw new IllegalStateException("APP_JWT_SECRET deve estar configurada.");
        }
        if (expirationSeconds < 60 || expirationSeconds > 3600) {
            throw new IllegalStateException("A expiracao do JWT deve estar entre 60 e 3600 segundos.");
        }

        try {
            this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedSecret));
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("APP_JWT_SECRET deve ser Base64 e conter ao menos 32 bytes.", exception);
        }
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(Usuario usuario) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("usuarioId", usuario.getId())
                .claim("tipoUsuario", usuario.getTipoUsuario().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(signingKey)
                .compact();
    }

    public AuthenticatedUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Number usuarioId = claims.get("usuarioId", Number.class);
        String email = claims.getSubject();
        String role = claims.get("tipoUsuario", String.class);
        if (usuarioId == null || email == null || email.isBlank() || role == null) {
            throw new IllegalArgumentException("JWT sem identidade completa.");
        }
        return new AuthenticatedUser(usuarioId.longValue(), email, TipoUsuario.valueOf(role));
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
