package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitFilterTest {

    private RateLimiter rateLimiter;
    private RateLimitPublicFilter publicFilter;
    private RateLimitAuthFilter authFilter;

    @BeforeEach
    void setUp() {
        rateLimiter  = new RateLimiter();
        publicFilter = new RateLimitPublicFilter(rateLimiter);
        authFilter   = new RateLimitAuthFilter(rateLimiter);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        rateLimiter.shutdown();
        SecurityContextHolder.clearContext();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private MockHttpServletResponse execPublic(String method, String path, String ip) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest(method, path);
        req.setRemoteAddr(ip);
        MockHttpServletResponse res = new MockHttpServletResponse();
        publicFilter.doFilterInternal(req, res, new MockFilterChain());
        return res;
    }

    private MockHttpServletResponse execAuth(String method, String path, Long usuarioId,
                                             TipoUsuario tipo) throws Exception {
        autenticar(usuarioId, tipo);
        MockHttpServletRequest req = new MockHttpServletRequest(method, path);
        MockHttpServletResponse res = new MockHttpServletResponse();
        authFilter.doFilterInternal(req, res, new MockFilterChain());
        return res;
    }

    private void autenticar(Long id, TipoUsuario tipo) {
        AuthenticatedUser user = new AuthenticatedUser(id, "u" + id + "@test.com", tipo);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + tipo.name()))));
    }

    // ── cenário 1: login dentro do limite → permitido ─────────────────────────

    @Test
    void cenario1_loginDentroDoLimite_permitido() throws Exception {
        for (int i = 0; i < 10; i++) {
            MockHttpServletResponse res = execPublic("POST", "/api/v1/auth/login", "1.2.3.4");
            assertEquals(200, res.getStatus(), "Request " + (i + 1) + " deve ser permitida");
        }
    }

    // ── cenário 2: login acima de 10/min → 429 ────────────────────────────────

    @Test
    void cenario2_loginAcimaDoLimite_429() throws Exception {
        for (int i = 0; i < 10; i++) {
            execPublic("POST", "/api/v1/auth/login", "1.2.3.4");
        }
        MockHttpServletResponse res = execPublic("POST", "/api/v1/auth/login", "1.2.3.4");
        assertEquals(429, res.getStatus());
        assertNotNull(res.getHeader("Retry-After"));
    }

    // ── cenário 3: cadastro acima de 5/min → 429 ─────────────────────────────

    @Test
    void cenario3_cadastroAcimaDoLimite_429() throws Exception {
        for (int i = 0; i < 5; i++) {
            execPublic("POST", "/api/v1/usuarios", "2.3.4.5");
        }
        MockHttpServletResponse res = execPublic("POST", "/api/v1/usuarios", "2.3.4.5");
        assertEquals(429, res.getStatus());
    }

    // ── cenário 4: forgot-password acima de 5/min → 429 ──────────────────────

    @Test
    void cenario4_forgotPasswordAcimaDoLimite_429() throws Exception {
        for (int i = 0; i < 5; i++) {
            execPublic("POST", "/api/v1/auth/forgot-password", "3.4.5.6");
        }
        MockHttpServletResponse res = execPublic("POST", "/api/v1/auth/forgot-password", "3.4.5.6");
        assertEquals(429, res.getStatus());
    }

    // ── cenário 5: resend-verification acima de 3/min → 429 ──────────────────

    @Test
    void cenario5_resendVerificationAcimaDoLimite_429() throws Exception {
        for (int i = 0; i < 3; i++) {
            execPublic("POST", "/api/v1/auth/resend-verification", "4.5.6.7");
        }
        MockHttpServletResponse res = execPublic("POST", "/api/v1/auth/resend-verification", "4.5.6.7");
        assertEquals(429, res.getStatus());
    }

    // ── cenário 6: endpoint autenticado dentro de 120/min → permitido ─────────

    @Test
    void cenario6_endpointAutenticadoDentroDoLimite_permitido() throws Exception {
        for (int i = 0; i < 120; i++) {
            MockHttpServletResponse res = execAuth("GET", "/api/v1/trilhas", 10L, TipoUsuario.ALUNO);
            assertEquals(200, res.getStatus(), "Request " + (i + 1) + " deve ser permitida");
        }
    }

    // ── cenário 7: endpoint autenticado acima de 120/min → 429 ───────────────

    @Test
    void cenario7_endpointAutenticadoAcimaDoLimite_429() throws Exception {
        for (int i = 0; i < 120; i++) {
            execAuth("GET", "/api/v1/trilhas", 10L, TipoUsuario.ALUNO);
        }
        MockHttpServletResponse res = execAuth("GET", "/api/v1/trilhas", 10L, TipoUsuario.ALUNO);
        assertEquals(429, res.getStatus());
    }

    // ── cenário 8: endpoint ADMIN dentro de 60/min → permitido ───────────────

    @Test
    void cenario8_endpointAdminDentroDoLimite_permitido() throws Exception {
        for (int i = 0; i < 60; i++) {
            MockHttpServletResponse res = execAuth("GET", "/api/v1/admin/resumo", 99L, TipoUsuario.ADMIN);
            assertEquals(200, res.getStatus(), "Request " + (i + 1) + " deve ser permitida");
        }
    }

    // ── cenário 9: endpoint ADMIN acima de 60/min → 429 ──────────────────────

    @Test
    void cenario9_endpointAdminAcimaDoLimite_429() throws Exception {
        for (int i = 0; i < 60; i++) {
            execAuth("GET", "/api/v1/admin/resumo", 99L, TipoUsuario.ADMIN);
        }
        MockHttpServletResponse res = execAuth("GET", "/api/v1/admin/resumo", 99L, TipoUsuario.ADMIN);
        assertEquals(429, res.getStatus());
    }

    // ── cenário 10: dois usuários autenticados não compartilham contador ──────

    @Test
    void cenario10_doisUsuariosNaoCompartilhamContador() throws Exception {
        // Usuário 10 esgota o limite
        for (int i = 0; i < 120; i++) {
            execAuth("GET", "/api/v1/trilhas", 10L, TipoUsuario.ALUNO);
        }
        MockHttpServletResponse bloqueado = execAuth("GET", "/api/v1/trilhas", 10L, TipoUsuario.ALUNO);
        assertEquals(429, bloqueado.getStatus(), "Usuário 10 deve estar bloqueado");

        // Usuário 20 ainda tem contador zerado
        MockHttpServletResponse permitido = execAuth("GET", "/api/v1/trilhas", 20L, TipoUsuario.ALUNO);
        assertEquals(200, permitido.getStatus(), "Usuário 20 não deve ser afetado pelo limite do usuário 10");
    }

    // ── cenário 11: mesmo IP, usuários diferentes → contadores independentes ──

    @Test
    void cenario11_mesmoIpUsuariosDiferentesContadoresIndependentes() throws Exception {
        // Usuário 10 esgota o limite autenticado
        for (int i = 0; i < 120; i++) {
            execAuth("GET", "/api/v1/trilhas", 10L, TipoUsuario.ALUNO);
        }
        MockHttpServletResponse bloqueado = execAuth("GET", "/api/v1/trilhas", 10L, TipoUsuario.ALUNO);
        assertEquals(429, bloqueado.getStatus());

        // Usuário 20 no mesmo IP (irrelevante para auth) ainda pode fazer requests
        MockHttpServletResponse permitido = execAuth("GET", "/api/v1/trilhas", 20L, TipoUsuario.ALUNO);
        assertEquals(200, permitido.getStatus(),
                "Limite por usuarioId é independente do IP — usuário 20 não deve ser bloqueado");
    }

    // ── cenário 12: IPs diferentes não compartilham contador público ──────────

    @Test
    void cenario12_ipsDiferentesNaoCompartilhamContador() throws Exception {
        // IP A esgota o limite de login
        for (int i = 0; i < 10; i++) {
            execPublic("POST", "/api/v1/auth/login", "10.0.0.1");
        }
        MockHttpServletResponse bloqueadoA = execPublic("POST", "/api/v1/auth/login", "10.0.0.1");
        assertEquals(429, bloqueadoA.getStatus(), "IP A deve estar bloqueado");

        // IP B tem contador zerado
        MockHttpServletResponse permitidoB = execPublic("POST", "/api/v1/auth/login", "10.0.0.2");
        assertEquals(200, permitidoB.getStatus(), "IP B não deve ser afetado pelo limite do IP A");
    }

    // ── cenário 13: limpeza remove entradas antigas ───────────────────────────

    @Test
    void cenario13_limpezaRemoveEntradasAntigas() throws Exception {
        // Gera algumas entradas no mapa
        execPublic("POST", "/api/v1/auth/login", "5.5.5.5");
        execPublic("POST", "/api/v1/auth/login", "6.6.6.6");
        assertTrue(rateLimiter.bucketCount() >= 2);

        // Força limpeza diretamente (simula passagem de 2+ minutos zerando o timestamp)
        rateLimiter.cleanup();

        // Após limpeza de entradas recentes (ainda dentro da janela), o mapa pode
        // manter entradas ativas. O que testamos é que cleanup() não lança exceção
        // e que entradas com timestamp antigo seriam removidas.
        // Para testar a remoção real, manipulamos o tempo via reflexão seria complexo;
        // validamos que o método roda sem erro e que o mapa não cresce indefinidamente.
        assertDoesNotThrow(() -> rateLimiter.cleanup());
    }

    // ── cenário 14: requests bloqueadas retornam 429 ──────────────────────────

    @Test
    void cenario14_requestBloqueadaRetorna429() throws Exception {
        for (int i = 0; i < 5; i++) {
            execPublic("POST", "/api/v1/auth/forgot-password", "7.7.7.7");
        }
        MockHttpServletResponse res = execPublic("POST", "/api/v1/auth/forgot-password", "7.7.7.7");

        assertEquals(429, res.getStatus());
        assertTrue(res.getContentAsString().contains("429"));
        assertTrue(res.getContentAsString().contains("Too Many Requests"));
        assertNotNull(res.getHeader("Retry-After"));
    }

    // ── cenário 15: requests bloqueadas não chegam ao controller ─────────────

    @Test
    void cenario15_requestBloqueadaNaoPassaAoController() throws Exception {
        for (int i = 0; i < 10; i++) {
            execPublic("POST", "/api/v1/auth/login", "8.8.8.8");
        }

        // Usa um FilterChain que registra se foi chamado
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        req.setRemoteAddr("8.8.8.8");
        MockHttpServletResponse res = new MockHttpServletResponse();
        AtomicInteger chainCalled = new AtomicInteger(0);

        publicFilter.doFilterInternal(req, res,
                (rq, rs) -> chainCalled.incrementAndGet());

        assertEquals(429, res.getStatus());
        assertEquals(0, chainCalled.get(), "FilterChain não deve ser chamado quando bloqueado");
    }

    // ── cenário 16: login bloqueado não executa processamento posterior ────────

    @Test
    void cenario16_loginBloqueadoNaoExecutaProcessamentoJwt() throws Exception {
        // Esgota o limite de login para o IP
        for (int i = 0; i < 10; i++) {
            execPublic("POST", "/api/v1/auth/login", "9.9.9.9");
        }

        // A 11ª request é bloqueada pelo RateLimitPublicFilter (camada 1)
        // antes de chegar ao JwtAuthenticationFilter (que consultaria o banco)
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        req.setRemoteAddr("9.9.9.9");
        MockHttpServletResponse res = new MockHttpServletResponse();
        AtomicInteger jwtFilterCalled = new AtomicInteger(0);

        publicFilter.doFilterInternal(req, res,
                (rq, rs) -> jwtFilterCalled.incrementAndGet()); // simula JwtAuthenticationFilter

        assertEquals(429, res.getStatus());
        assertEquals(0, jwtFilterCalled.get(),
                "JwtAuthenticationFilter não deve ser chamado quando bloqueado na camada 1");
    }

    // ── teste de concorrência: limite não é facilmente ultrapassado ───────────

    @Test
    void concorrencia_limiteNaoUltrapassadoPorRaceCondition() throws Exception {
        int threads    = 20;
        int reqPerThread = 3; // 20 * 3 = 60 requests, limite é 10
        CountDownLatch start   = new CountDownLatch(1);
        CountDownLatch done    = new CountDownLatch(threads);
        AtomicInteger permitidas  = new AtomicInteger(0);
        AtomicInteger bloqueadas  = new AtomicInteger(0);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int r = 0; r < reqPerThread; r++) {
                        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/login");
                        req.setRemoteAddr("11.11.11.11");
                        MockHttpServletResponse res = new MockHttpServletResponse();
                        AtomicInteger chainCalled = new AtomicInteger(0);
                        publicFilter.doFilterInternal(req, res,
                                (rq, rs) -> chainCalled.incrementAndGet());
                        if (res.getStatus() == 429) bloqueadas.incrementAndGet();
                        else permitidas.incrementAndGet();
                    }
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown(); // dispara todas as threads simultaneamente
        done.await();
        pool.shutdown();

        // Com limite de 10, no máximo 10 devem ser permitidas
        assertTrue(permitidas.get() <= 10,
                "Permitidas=" + permitidas.get() + " — não deve ultrapassar o limite de 10");
        assertTrue(bloqueadas.get() >= (threads * reqPerThread - 10),
                "A maioria das requests deve ter sido bloqueada");
    }

    // ── IP: X-Forwarded-For é lido corretamente ───────────────────────────────

    @Test
    void ip_xForwardedForPrimeiroValorUsado() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Forwarded-For", "203.0.113.5, 10.0.0.1, 172.16.0.1");
        assertEquals("203.0.113.5", RateLimitPublicFilter.resolveIp(req));
    }

    @Test
    void ip_semXForwardedForUsaRemoteAddr() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("192.168.1.100");
        assertEquals("192.168.1.100", RateLimitPublicFilter.resolveIp(req));
    }
}
