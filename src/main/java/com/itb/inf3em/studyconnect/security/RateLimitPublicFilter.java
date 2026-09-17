package com.itb.inf3em.studyconnect.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Camada 1 de rate limit: por IP, aplicada ANTES do JwtAuthenticationFilter.
 *
 * Protege endpoints públicos/sensíveis contra brute force e abuso
 * antes que qualquer processamento de JWT ou consulta ao banco ocorra.
 *
 * IP: lido do header X-Forwarded-For (primeiro valor, injetado pelo Render)
 * com fallback para request.getRemoteAddr().
 *
 * Decisão sobre X-Forwarded-For:
 * O Render injeta o IP real do cliente como primeiro valor do header.
 * Usamos apenas o primeiro token para evitar spoofing de IPs intermediários.
 * Um atacante poderia adicionar um X-Forwarded-For falso antes do proxy do Render,
 * mas o Render prefixa o IP real — tornando o primeiro valor confiável neste ambiente.
 */
public class RateLimitPublicFilter extends OncePerRequestFilter {

    private final RateLimiter rateLimiter;

    public RateLimitPublicFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path   = request.getRequestURI();
        String method = request.getMethod();

        Integer limit = resolvePublicLimit(method, path);
        if (limit == null) {
            chain.doFilter(request, response);
            return;
        }

        String ip  = resolveIp(request);
        String key = "ip:" + ip + ":" + method + ":" + path;

        if (!rateLimiter.tryConsume(key, limit)) {
            reject(response, rateLimiter.retryAfterSeconds(key));
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Retorna o limite para endpoints públicos/sensíveis, ou null se não aplicável.
     */
    private Integer resolvePublicLimit(String method, String path) {
        if ("GET".equals(method) && "/health".equals(path))          return 30;
        if (!"POST".equals(method))                                   return null;

        return switch (path) {
            case "/api/v1/auth/login"               -> 10;
            case "/api/v1/usuarios"                 -> 5;
            case "/api/v1/auth/google"              -> 10;
            case "/api/v1/auth/forgot-password"     -> 5;
            case "/api/v1/auth/reset-password"      -> 10;
            case "/api/v1/auth/verify-email"        -> 10;
            case "/api/v1/auth/resend-verification" -> 3;
            default                                 -> null;
        };
    }

    /**
     * Extrai o IP do cliente.
     * Usa o primeiro valor do X-Forwarded-For (IP original antes dos proxies),
     * com fallback para getRemoteAddr() se o header estiver ausente.
     */
    static String resolveIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        return request.getRemoteAddr();
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
