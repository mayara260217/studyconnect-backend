package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtRevocationAuditTest {

    private TokenService tokenService;
    private UsuarioRepository usuarioRepository;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(900);
        usuarioRepository = mock(UsuarioRepository.class);
        filter = new JwtAuthenticationFilter(tokenService, usuarioRepository);
        SecurityContextHolder.clearContext();
    }

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

    private void executarFiltro(String token) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());
    }

    private Authentication autenticacao() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Test
    void cenario1_usuarioAtivo_tokenValido_autenticado() throws Exception {
        Usuario alice = usuarioAtivo(10L, "alice@example.com", TipoUsuario.ALUNO);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(alice));

        String token = tokenService.generateToken(alice);
        executarFiltro(token);

        assertNotNull(autenticacao());
        assertTrue(autenticacao().isAuthenticated());
        AuthenticatedUser auth = (AuthenticatedUser) autenticacao().getPrincipal();
        assertEquals(10L, auth.usuarioId());
        assertEquals("alice@example.com", auth.email());
        assertEquals(TipoUsuario.ALUNO, auth.tipoUsuario());
    }

    @Test
    void cenario2_usuarioDesativado_naoAutenticado() throws Exception {
        Usuario alice = usuarioAtivo(10L, "alice@example.com", TipoUsuario.ALUNO);
        String token = tokenService.generateToken(alice);

        when(usuarioRepository.findById(10L))
                .thenReturn(Optional.of(usuarioInativo(10L, "alice@example.com", TipoUsuario.ALUNO)));

        executarFiltro(token);

        assertNull(autenticacao(), "Usuário desativado não deve ser autenticado");
    }

    @Test
    void cenario3_usuarioExcluido_naoAutenticado() throws Exception {
        Usuario alice = usuarioAtivo(10L, "alice@example.com", TipoUsuario.ALUNO);
        String token = tokenService.generateToken(alice);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.empty());

        executarFiltro(token);

        assertNull(autenticacao(), "Usuário excluído não deve ser autenticado");
    }

    @Test
    void cenario4_tipoUsuarioAlteradoNoBanco_usaRoleAtual() throws Exception {
        Usuario professor = usuarioAtivo(10L, "alice@example.com", TipoUsuario.PROFESSOR);
        String token = tokenService.generateToken(professor);

        when(usuarioRepository.findById(10L))
                .thenReturn(Optional.of(usuarioAtivo(10L, "alice@example.com", TipoUsuario.ALUNO)));

        executarFiltro(token);

        assertNotNull(autenticacao());
        AuthenticatedUser auth = (AuthenticatedUser) autenticacao().getPrincipal();
        assertEquals(TipoUsuario.ALUNO, auth.tipoUsuario());
        assertFalse(autenticacao().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROFESSOR")));
        assertTrue(autenticacao().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ALUNO")));
    }

    @Test
    void cenario5_emailAtualizadoNoBanco_usaEmailAtual() throws Exception {
        Usuario alice = usuarioAtivo(10L, "alice-old@example.com", TipoUsuario.ALUNO);
        String token = tokenService.generateToken(alice);

        when(usuarioRepository.findById(10L))
                .thenReturn(Optional.of(usuarioAtivo(10L, "alice-new@example.com", TipoUsuario.ALUNO)));

        executarFiltro(token);

        assertNotNull(autenticacao());
        AuthenticatedUser auth = (AuthenticatedUser) autenticacao().getPrincipal();
        assertEquals("alice-new@example.com", auth.email());
    }

    @Test
    void cenario6_tokenRevogado_naoAutenticado() throws Exception {
        Usuario alice = usuarioAtivo(10L, "alice@example.com", TipoUsuario.ALUNO);
        String token = tokenService.generateToken(alice);

        tokenService.revokeToken(token);
        executarFiltro(token);

        assertNull(autenticacao(), "Token revogado deve ser rejeitado");
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void cenario7_tokenInexistente_naoAutenticado() throws Exception {
        executarFiltro("token-que-nao-existe");

        assertNull(autenticacao(), "Token inválido deve ser rejeitado");
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void cenario8_semHeader_naoAutenticado() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(autenticacao());
        verifyNoInteractions(usuarioRepository);
    }
}
