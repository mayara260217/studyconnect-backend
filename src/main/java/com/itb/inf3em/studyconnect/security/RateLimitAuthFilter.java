package com.itb.inf3em.studyconnect.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Camada 2 de rate limit: por usuarioId, aplicada DEPOIS do JwtAuthenticationFilter.
 *
 * Só atua quando há um AuthenticatedUser no SecurityContext (request autenticada).
 * Requests sem autenticação são ignoradas — já cobertas pela camada 1 (IP).
 *
 * Endpoints ADMIN (/api/v1/admin/**, /api/email/**): 60/min
 * email-change/request: 5/min
 * email-change/verify: 10/min
 * Demais autenticados: 120/min
 */
public class RateLimitAuthFilter extends OncePerRequestFilter {

    private final RateLimiter rateLimiter;

    public RateLimitAuthFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser user)) {
            chain.doFilter(request, response);
            return;
        }

        String path  = request.getRequestURI();
        int    limit = resolveAuthLimit(path);
        // Normaliza IDs numéricos no path para evitar explosão de chaves no mapa
        String key   = "uid:" + user.usuarioId() + ":" + path.replaceAll("/\\d+", "/{id}");

        if (!rateLimiter.tryConsume(key, limit)) {
            reject(response, rateLimiter.retryAfterSeconds(key));
            return;
        }

        chain.doFilter(request, response);
    }

    private int resolveAuthLimit(String path) {
        if (path.startsWith("/api/v1/admin/") || path.startsWith("/api/email/")) return 60;
        if (path.equals("/api/v1/auth/email-change/request"))                    return 5;
        if (path.equals("/api/v1/auth/email-change/verify"))                     return 10;
        return 120;
    }

    private void reject(HttpServletResponse response, long retryAfter) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfter));
        response.getWriter().write(
                "{\"status\":429,\"error\":\"Too Many Requests\"," +
                "\"message\":\"Limite de requisicoes excedido. Tente novamente em breve.\"}");
    }
}
