package com.itb.inf3em.studyconnect.security;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiter em memória usando janela fixa de 1 minuto por chave.
 *
 * Cada entrada armazena: [contador, timestamp_inicio_janela_ms].
 * Thread-safe: operações atômicas via AtomicInteger + volatile timestamp.
 *
 * Limpeza periódica a cada 5 minutos remove entradas inativas há mais de 2 minutos,
 * evitando crescimento indefinido do mapa.
 */
@Component
public class RateLimiter {

    static final long WINDOW_MS = 60_000L;          // janela de 1 minuto
    private static final long CLEANUP_INTERVAL_MIN = 5;
    private static final long INACTIVE_THRESHOLD_MS = 120_000L; // 2 minutos

    // Cada entrada: long[] { contador, timestamp_inicio_janela_ms }
    // Acesso sincronizado por chave via computeIfAbsent + synchronized no array
    private final ConcurrentHashMap<String, long[]> buckets = new ConcurrentHashMap<>();

    private final ScheduledExecutorService cleaner =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "rate-limit-cleaner");
                t.setDaemon(true);
                return t;
            });

    public RateLimiter() {
        cleaner.scheduleAtFixedRate(
                this::cleanup,
                CLEANUP_INTERVAL_MIN,
                CLEANUP_INTERVAL_MIN,
                TimeUnit.MINUTES
        );
    }

    /**
     * Tenta consumir um token para a chave dada.
     *
     * @param key   identificador (IP ou "uid:<usuarioId>")
     * @param limit máximo de requests por janela
     * @return true se permitido, false se limite excedido
     */
    public boolean tryConsume(String key, int limit) {
        long now = System.currentTimeMillis();
        long[] bucket = buckets.computeIfAbsent(key, k -> new long[]{0L, now});

        synchronized (bucket) {
            // Se a janela expirou, reinicia
            if (now - bucket[1] >= WINDOW_MS) {
                bucket[0] = 0L;
                bucket[1] = now;
            }
            if (bucket[0] >= limit) {
                return false;
            }
            bucket[0]++;
            return true;
        }
    }

    /**
     * Retorna quantos segundos faltam para a janela atual expirar.
     * Usado no header Retry-After.
     */
    public long retryAfterSeconds(String key) {
        long[] bucket = buckets.get(key);
        if (bucket == null) return 0;
        synchronized (bucket) {
            long elapsed = System.currentTimeMillis() - bucket[1];
            long remaining = WINDOW_MS - elapsed;
            return Math.max(0, (remaining / 1000) + 1);
        }
    }

    /** Remove entradas sem atividade há mais de 2 minutos. */
    void cleanup() {
        long cutoff = System.currentTimeMillis() - INACTIVE_THRESHOLD_MS;
        buckets.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                return e.getValue()[1] < cutoff;
            }
        });
    }

    /** Expõe o tamanho do mapa para testes. */
    int bucketCount() {
        return buckets.size();
    }

    @PreDestroy
    public void shutdown() {
        cleaner.shutdownNow();
    }
}
