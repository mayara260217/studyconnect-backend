package com.itb.inf3em.studyconnect.service;

import com.itb.inf3em.studyconnect.model.dto.LoginRequestDTO;
import com.itb.inf3em.studyconnect.model.dto.LoginResponseDTO;
import com.itb.inf3em.studyconnect.model.entity.EmailVerificationToken;
import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.repository.EmailVerificationTokenRepository;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import com.itb.inf3em.studyconnect.model.services.AuthService;
import com.itb.inf3em.studyconnect.model.services.CredentialValidationService;
import com.itb.inf3em.studyconnect.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Sprint 7 - Testes unitarios da regra de negocio do login.
 *
 * Cobre os cenarios do plano de sprints sem tocar em banco de dados: o
 * UsuarioRepository, o BCryptPasswordEncoder, o repositorio de tokens de
 * verificacao e o TokenService sao substituidos por mocks (Mockito).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "aluno@email.com";
    private static final String SENHA = "Senha@123";

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private CredentialValidationService credentialValidationService;
    @Mock private EmailVerificationTokenRepository tokenRepository;
    @Mock private TokenService tokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Injecao por construtor: o teste monta o servico com dubles de teste,
        // sem precisar de contexto Spring nem de banco.
        authService = new AuthService(
                usuarioRepository,
                passwordEncoder,
                credentialValidationService,
                tokenRepository,
                tokenService
        );
    }

    // -- Cenario 1: login valido ---------------------------------------------

    @Test
    @DisplayName("login valido retorna LoginResponseDTO com token")
    void deveRetornarTokenQuandoCredenciaisValidas() {
        Usuario usuarioAtivo = usuarioAtivo();

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioAtivo));
        when(passwordEncoder.matches(SENHA, usuarioAtivo.getSenha())).thenReturn(true);
        when(tokenService.generateToken(usuarioAtivo)).thenReturn("token-de-teste");
        when(tokenService.getExpirationSeconds()).thenReturn(900L);

        LoginResponseDTO resultado = authService.login(request(EMAIL, SENHA));

        assertThat(resultado.getAccessToken()).isEqualTo("token-de-teste");
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getEmail()).isEqualTo(EMAIL);
        assertThat(resultado.getNome()).isEqualTo("Aluno Teste");
        assertThat(resultado.getRole()).isEqualTo("ALUNO");
        assertThat(resultado.getTokenType()).isEqualTo("Bearer");
        assertThat(resultado.getExpiresIn()).isEqualTo(900L);
        assertThat(resultado.isAtivo()).isTrue();
    }

    // -- Cenario 2: senha errada ---------------------------------------------

    @Test
    @DisplayName("senha errada lanca 401 UNAUTHORIZED")
    void deveLancar401QuandoSenhaIncorreta() {
        Usuario usuarioAtivo = usuarioAtivo();

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioAtivo));
        when(passwordEncoder.matches("senha-errada", usuarioAtivo.getSenha())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request(EMAIL, "senha-errada")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));

        verify(tokenService, never()).generateToken(any());
    }

    // -- Cenario 3: e-mail nao cadastrado ------------------------------------

    @Test
    @DisplayName("e-mail nao cadastrado lanca 401 UNAUTHORIZED")
    void deveLancar401QuandoEmailNaoCadastrado() {
        when(usuarioRepository.findByEmail("fantasma@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request("fantasma@email.com", SENHA)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));

        verify(tokenService, never()).generateToken(any());
    }

    // -- Cenario 4: e-mail nao verificado ------------------------------------

    @Test
    @DisplayName("conta inativa sem e-mail verificado lanca 403 FORBIDDEN")
    void deveLancar403QuandoEmailNaoVerificado() {
        Usuario usuarioInativo = usuarioInativo();
        EmailVerificationToken tokenNaoVerificado =
                new EmailVerificationToken(EMAIL, "123456", Instant.now().plusSeconds(600));

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioInativo));
        when(passwordEncoder.matches(SENHA, usuarioInativo.getSenha())).thenReturn(true);
        when(tokenRepository.findByEmail(EMAIL)).thenReturn(Optional.of(tokenNaoVerificado));

        assertThatThrownBy(() -> authService.login(request(EMAIL, SENHA)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(rse.getReason()).contains("E-mail nao verificado");
                });

        verify(tokenService, never()).generateToken(any());
    }

    // -- Cenario 5: conta suspensa (e-mail ja verificado) --------------------

    @Test
    @DisplayName("conta inativa com e-mail verificado lanca 403 FORBIDDEN (suspensa)")
    void deveLancar403QuandoContaSuspensa() {
        Usuario usuarioInativo = usuarioInativo();
        EmailVerificationToken tokenVerificado =
                new EmailVerificationToken(EMAIL, "123456", Instant.now().plusSeconds(600));
        tokenVerificado.setVerified(true);

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioInativo));
        when(passwordEncoder.matches(SENHA, usuarioInativo.getSenha())).thenReturn(true);
        when(tokenRepository.findByEmail(EMAIL)).thenReturn(Optional.of(tokenVerificado));

        assertThatThrownBy(() -> authService.login(request(EMAIL, SENHA)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(rse.getReason()).contains("Conta suspensa");
                });

        verify(tokenService, never()).generateToken(any());
    }

    // -- Cenario 6: senha em branco ------------------------------------------

    @Test
    @DisplayName("senha em branco ou nula lanca 401 UNAUTHORIZED sem consultar o banco")
    void deveLancar401QuandoSenhaEmBranco() {
        assertThatThrownBy(() -> authService.login(request(EMAIL, "")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> authService.login(request(EMAIL, null)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> authService.login(request(EMAIL, "   ")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));

        verify(usuarioRepository, never()).findByEmail(any());
    }

    // -- Bonus: a resposta nao revela se o e-mail existe na base -------------

    @Test
    @DisplayName("mensagem de erro nao diferencia e-mail inexistente de senha errada")
    void mensagemDeErroNaoRevelaSeEmailExiste() {
        Usuario usuarioAtivo = usuarioAtivo();

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioAtivo));
        when(passwordEncoder.matches("senha-errada", usuarioAtivo.getSenha())).thenReturn(false);

        ResponseStatusException senhaErrada = org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request(EMAIL, "senha-errada")));

        when(usuarioRepository.findByEmail("fantasma@email.com")).thenReturn(Optional.empty());
        ResponseStatusException emailInexistente = org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request("fantasma@email.com", SENHA)));

        assertThat(senhaErrada.getReason()).isEqualTo(emailInexistente.getReason());
        assertThat(senhaErrada.getReason()).isEqualTo("E-mail ou senha incorretos");
    }

    // -- helpers -------------------------------------------------------------

    private LoginRequestDTO request(String email, String senha) {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail(email);
        dto.setSenha(senha);
        return dto;
    }

    private Usuario usuarioAtivo() {
        Usuario u = usuario(EMAIL);
        u.setSenha("$2a$10$hash-de-teste");
        u.setAtivo(true);
        return u;
    }

    private Usuario usuarioInativo() {
        Usuario u = usuario(EMAIL);
        u.setSenha("$2a$10$hash-de-teste");
        u.setAtivo(false);
        return u;
    }

    private Usuario usuario(String email) {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNome("Aluno Teste");
        u.setEmail(email);
        u.setTipoUsuario(TipoUsuario.ALUNO);
        return u;
    }
}
