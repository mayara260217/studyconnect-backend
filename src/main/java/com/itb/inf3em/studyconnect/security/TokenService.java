package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    private final long expirationSeconds;

    private record Session(long usuarioId, Instant expiresAt) {}

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public TokenService(@Value("${app.security.token.expiration-seconds:900}") long expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(Usuario usuario) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new Session(usuario.getId(), Instant.now().plusSeconds(expirationSeconds)));
        return token;
    }

    public Optional<Long> getUsuarioId(String token) {
        Session session = sessions.get(token);
        if (session == null || Instant.now().isAfter(session.expiresAt())) {
            sessions.remove(token);
            return Optional.empty();
        }
        return Optional.of(session.usuarioId());
    }

    public void revokeToken(String token) {
        sessions.remove(token);
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
