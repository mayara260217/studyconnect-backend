package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testa o comportamento SEGURO do JwtAuthenticationFilter após a correção.
 * O filtro agora consulta o banco após validar o token.
 */
class JwtRevocationAuditTest {

    private static final String SECRET_B64 =
            Base64.getEncoder().encodeToString(
                    "auditoria-secreta-32-bytes-minimo!!".getBytes());

    private JwtService jwtService;
    private UsuarioRepository usuarioRepository;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET_B64, 900);
        usuarioRepository = mock(UsuarioRepository.class);
        filter = new JwtAuthenticationFilter(jwtService, usuarioRepository);
        SecurityContextHolder.clearContext();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Usuario usuarioAtivo(Long id, String email, TipoUsuario tipo) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setEmail(email);
        u.setTipoUsuario(tipo);
        u.setAtivo(true);
        return u;
    }

    private Usuario usuarioInativo(Long id, String email, TipoUsuario tipo) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setEmail(email);
        u.setTipoUsuario(tipo);
        u.setAtivo(false);
        return u;
    }

    /** Executa o filtro com o token no header Authorization. */
    private void executarFiltro(String token) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());
    }

    private Authentication autenticacao() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    // ── cenário 1: usuário ativo + JWT válido → autenticado ──────────────────

    @Test
    void cenario1_usuarioAtivo_jwtValido_autenticado() throws Exception {
        Usuario alice = usuarioAtivo(10L, "alice@example.com", TipoUsuario.ALUNO);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(alice));

        String token = jwtService.generateToken(alice);
        executarFiltro(token);

        assertNotNull(autenticacao());
        assertTrue(autenticacao().isAuthenticated());
        AuthenticatedUser auth = (AuthenticatedUser) autenticacao().getPrincipal();
        assertEquals(10L, auth.usuarioId());
        assertEquals("alice@example.com", auth.email());
        assertEquals(TipoUsuario.ALUNO, auth.tipoUsuario());
    }

    // ── cenário 2: JWT válido + usuário desativado → não autenticado / 401 ───

    @Test
    void cenario2_usuarioDesativado_naoAutenticado() throws Exception {
        // Token foi emitido quando o usuário estava ativo
        Usuario alice = usuarioAtivo(10L, "alice@example.com", TipoUsuario.ALUNO);
        String token = jwtService.generateToken(alice);

        // Admin desativou o usuário — banco agora retorna ativo=false
        when(usuarioRepository.findById(10L))
                .thenReturn(Optional.of(usuarioInativo(10L, "alice@example.com", TipoUsuario.ALUNO)));

        executarFiltro(token);

        assertNull(autenticacao(),
                "Usuário desativado não deve ser autenticado");
    }

    // ── cenário 3: JWT válido + usuário excluído → não autenticado / 401 ─────

    @Test
    void cenario3_usuarioExcluido_naoAutenticado() throws Exception {
        Usuario alice = usuarioAtivo(10L, "alice@example.com", TipoUsuario.ALUNO);
        String token = jwtService.generateToken(alice);

        // Usuário foi deletado — banco retorna vazio
        when(usuarioRepository.findById(10L)).thenReturn(Optional.empty());

        executarFiltro(token);

        assertNull(autenticacao(),
                "Usuário excluído não deve ser autenticado");
    }

    // ── cenário 4: JWT com ROLE_PROFESSOR, banco tem ALUNO → usa ALUNO ───────

    @Test
    void cenario4_tipoUsuarioAlteradoNoBanco_usaRoleAtual() throws Exception {
        // Token emitido quando era PROFESSOR
        Usuario professor = usuarioAtivo(10L, "alice@example.com", TipoUsuario.PROFESSOR);
        String tokenDeProfessor = jwtService.generateToken(professor);

        // Admin rebaixou para ALUNO — banco agora tem ALUNO
        when(usuarioRepository.findById(10L))
                .thenReturn(Optional.of(usuarioAtivo(10L, "alice@example.com", TipoUsuario.ALUNO)));

        executarFiltro(tokenDeProfessor);

        assertNotNull(autenticacao());
        AuthenticatedUser auth = (AuthenticatedUser) autenticacao().getPrincipal();

        assertEquals(TipoUsuario.ALUNO, auth.tipoUsuario(),
                "tipoUsuario deve vir do banco, não do JWT");
        assertFalse(autenticacao().getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_PROFESSOR")),
                "Não deve possuir ROLE_PROFESSOR após rebaixamento");
        assertTrue(autenticacao().getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ALUNO")),
                "Deve possuir ROLE_ALUNO conforme banco");
    }

    // ── cenário 5: JWT com email antigo, banco tem email atualizado ───────────

    @Test
    void cenario5_emailAtualizadoNoBanco_usaEmailAtual() throws Exception {
        // Token emitido com email antigo
        Usuario alice = usuarioAtivo(10L, "alice-old@example.com", TipoUsuario.ALUNO);
        String token = jwtService.generateToken(alice);

        // Usuário atualizou o email — banco tem o novo email
        when(usuarioRepository.findById(10L))
                .thenReturn(Optional.of(usuarioAtivo(10L, "alice-new@example.com", TipoUsuario.ALUNO)));

        executarFiltro(token);

        assertNotNull(autenticacao());
        AuthenticatedUser auth = (AuthenticatedUser) autenticacao().getPrincipal();
        assertEquals("alice-new@example.com", auth.email(),
                "email deve vir do banco, não do JWT");
    }

    // ── cenário 6: JWT expirado → não autenticado / 401 ──────────────────────

    @Test
    void cenario6_jwtExpirado_naoAutenticado() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_B64));
        String tokenExpirado = Jwts.builder()
                .subject("alice@example.com")
                .claim("usuarioId", 10L)
                .claim("tipoUsuario", "ALUNO")
                .issuedAt(Date.from(Instant.now().minusSeconds(1800)))
                .expiration(Date.from(Instant.now().minusSeconds(900)))
                .signWith(key)
                .compact();

        executarFiltro(tokenExpirado);

        assertNull(autenticacao(), "Token expirado deve ser rejeitado");
        // banco não deve ser consultado para token inválido
        verifyNoInteractions(usuarioRepository);
    }

    // ── cenário 7: JWT adulterado → não autenticado / 401 ────────────────────

    @Test
    void cenario7_jwtAdulterado_naoAutenticado() throws Exception {
        Usuario alice = usuarioAtivo(10L, "alice@example.com", TipoUsuario.ALUNO);
        String tokenValido = jwtService.generateToken(alice);

        String[] partes = tokenValido.split("\\.");
        String payloadAdulterado = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(
                        "{\"sub\":\"admin@example.com\",\"usuarioId\":99,\"tipoUsuario\":\"ADMIN\"}"
                                .getBytes());
        String tokenAdulterado = partes[0] + "." + payloadAdulterado + "." + partes[2];

        executarFiltro(tokenAdulterado);

        assertNull(autenticacao(), "Token adulterado deve ser rejeitado");
        verifyNoInteractions(usuarioRepository);
    }

    // ── cenário 8: claims obrigatórios ausentes → não autenticado / 401 ──────

    @Test
    void cenario8_claimsAusentes_naoAutenticado() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_B64));
        String tokenSemClaims = Jwts.builder()
                .subject("alice@example.com")
                // sem usuarioId e tipoUsuario
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(900)))
                .signWith(key)
                .compact();

        executarFiltro(tokenSemClaims);

        assertNull(autenticacao(), "Token sem claims obrigatórios deve ser rejeitado");
        verifyNoInteractions(usuarioRepository);
    }
}
