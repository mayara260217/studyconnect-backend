package com.itb.inf3em.studyconnect.service;

import com.itb.inf3em.studyconnect.model.entity.EmailChangeToken;
import com.itb.inf3em.studyconnect.model.entity.EmailVerificationToken;
import com.itb.inf3em.studyconnect.model.entity.PasswordResetToken;
import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.repository.EmailChangeTokenRepository;
import com.itb.inf3em.studyconnect.model.repository.EmailVerificationTokenRepository;
import com.itb.inf3em.studyconnect.model.repository.PasswordResetTokenRepository;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import com.itb.inf3em.studyconnect.model.services.EmailChangeService;
import com.itb.inf3em.studyconnect.model.services.EmailService;
import com.itb.inf3em.studyconnect.model.services.EmailVerificationService;
import com.itb.inf3em.studyconnect.model.services.PasswordResetService;
import com.itb.inf3em.studyconnect.model.services.CredentialValidationService;
import com.itb.inf3em.studyconnect.security.AuthenticatedUser;
import com.itb.inf3em.studyconnect.security.CurrentUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TokenSecurityTest {

    private EmailVerificationTokenRepository verificationRepo;
    private PasswordResetTokenRepository resetRepo;
    private EmailChangeTokenRepository changeRepo;
    private UsuarioRepository usuarioRepo;
    private EmailService emailService;

    private EmailVerificationService verificationService;
    private PasswordResetService passwordResetService;
    private EmailChangeService emailChangeService;

    @BeforeEach
    void setUp() {
        verificationRepo = mock(EmailVerificationTokenRepository.class);
        resetRepo        = mock(PasswordResetTokenRepository.class);
        changeRepo       = mock(EmailChangeTokenRepository.class);
        usuarioRepo      = mock(UsuarioRepository.class);
        emailService     = mock(EmailService.class);

        verificationService = new EmailVerificationService();
        ReflectionTestUtils.setField(verificationService, "tokenRepository", verificationRepo);
        ReflectionTestUtils.setField(verificationService, "usuarioRepository", usuarioRepo);
        ReflectionTestUtils.setField(verificationService, "emailService", emailService);

        passwordResetService = new PasswordResetService();
        ReflectionTestUtils.setField(passwordResetService, "usuarioRepository", usuarioRepo);
        ReflectionTestUtils.setField(passwordResetService, "tokenRepository", resetRepo);
        ReflectionTestUtils.setField(passwordResetService, "emailService", emailService);
        ReflectionTestUtils.setField(passwordResetService, "passwordEncoder", new BCryptPasswordEncoder());
        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "http://localhost:5173");

        emailChangeService = new EmailChangeService();
        ReflectionTestUtils.setField(emailChangeService, "tokenRepository", changeRepo);
        ReflectionTestUtils.setField(emailChangeService, "usuarioRepository", usuarioRepo);
        ReflectionTestUtils.setField(emailChangeService, "emailService", emailService);
        ReflectionTestUtils.setField(emailChangeService, "credentialValidationService", mock(CredentialValidationService.class));
        ReflectionTestUtils.setField(emailChangeService, "currentUser", new CurrentUser());
        ReflectionTestUtils.setField(emailChangeService, "frontendUrl", "http://localhost:5173");
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    // ── 1. OTP de verificação de e-mail tem 6 dígitos ────────────────────────

    @Test
    void otpVerificacaoEmail_temSeisDígitos() {
        when(verificationRepo.findByEmail("a@b.com")).thenReturn(Optional.empty());
        when(verificationRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        verificationService.enviarCodigo("a@b.com");

        ArgumentCaptor<EmailVerificationToken> cap = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(verificationRepo).save(cap.capture());
        String code = cap.getValue().getCode();
        assertThat(code).matches("\\d{6}");
    }

    // ── 2. OTP de verificação usa SecureRandom ───────────────────────────────

    @Test
    void emailVerificationService_usaSecureRandom() throws Exception {
        Field f = EmailVerificationService.class.getDeclaredField("OTP_RANDOM");
        f.setAccessible(true);
        assertThat(f.get(null)).isInstanceOf(SecureRandom.class);
    }

    // ── 3. OTP de troca de e-mail usa SecureRandom ───────────────────────────

    @Test
    void emailChangeService_usaSecureRandom() throws Exception {
        Field f = EmailChangeService.class.getDeclaredField("OTP_RANDOM");
        f.setAccessible(true);
        assertThat(f.get(null)).isInstanceOf(SecureRandom.class);
    }

    // ── 4. OTP de troca de e-mail tem 6 dígitos ──────────────────────────────

    @Test
    void otpTrocaEmail_temSeisDígitos() {
        EmailChangeToken token = new EmailChangeToken(1L, "atual@b.com", "novo@b.com", "tok", Instant.now().plusSeconds(60));
        when(changeRepo.findByConfirmToken("tok")).thenReturn(Optional.of(token));
        when(usuarioRepo.existsByEmail("novo@b.com")).thenReturn(false);
        when(changeRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        emailChangeService.confirmarEtapa1("tok");

        assertThat(token.getOtpCode()).matches("\\d{6}");
    }

    // ── 5. Token de reset de senha é UUID (128 bits de entropia) ─────────────

    @Test
    void tokenResetSenha_éUUID() {
        Usuario u = usuario(1L, "u@b.com");
        when(usuarioRepo.findByEmail("u@b.com")).thenReturn(Optional.of(u));
        when(resetRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        passwordResetService.solicitarRecuperacao("u@b.com");

        ArgumentCaptor<PasswordResetToken> cap = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(resetRepo).save(cap.capture());
        String token = cap.getValue().getToken();
        // UUID format: 8-4-4-4-12 hex chars
        assertThat(token).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    // ── 6. Token de reset expirado é rejeitado ───────────────────────────────

    @Test
    void tokenResetExpirado_rejeitado() {
        PasswordResetToken expired = new PasswordResetToken("tok", "u@b.com", Instant.now().minusSeconds(1));
        when(resetRepo.findByToken("tok")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> passwordResetService.redefinirSenha("tok", "novaSenha123"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    // ── 7. Token de reset usado não pode ser reutilizado ─────────────────────

    @Test
    void tokenResetUsado_nãoPodeSerReutilizado() {
        PasswordResetToken used = new PasswordResetToken("tok", "u@b.com", Instant.now().plusSeconds(60));
        used.setUsed(true);
        when(resetRepo.findByToken("tok")).thenReturn(Optional.of(used));

        assertThatThrownBy(() -> passwordResetService.redefinirSenha("tok", "novaSenha123"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    // ── 8. Token de reset marcado como usado após sucesso ────────────────────

    @Test
    void tokenResetSucesso_marcadoComoUsado() {
        PasswordResetToken token = new PasswordResetToken("tok", "u@b.com", Instant.now().plusSeconds(60));
        Usuario u = usuario(1L, "u@b.com");
        when(resetRepo.findByToken("tok")).thenReturn(Optional.of(token));
        when(usuarioRepo.findByEmail("u@b.com")).thenReturn(Optional.of(u));
        when(resetRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(usuarioRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        passwordResetService.redefinirSenha("tok", "novaSenha123");

        assertThat(token.isUsed()).isTrue();
    }

    // ── 9. OTP de verificação expirado é rejeitado ───────────────────────────

    @Test
    void otpVerificacaoExpirado_rejeitado() {
        EmailVerificationToken expired = new EmailVerificationToken("a@b.com", "123456", Instant.now().minusSeconds(1));
        when(verificationRepo.findByEmail("a@b.com")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> verificationService.verificar("a@b.com", "123456"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    // ── 10. OTP de verificação usado com sucesso não pode ser reutilizado ────

    @Test
    void otpVerificacaoSucesso_nãoPodeSerReutilizado() {
        EmailVerificationToken token = new EmailVerificationToken("a@b.com", "123456", Instant.now().plusSeconds(60));
        Usuario u = usuario(1L, "a@b.com");
        when(verificationRepo.findByEmail("a@b.com"))
                .thenReturn(Optional.of(token))
                .thenReturn(Optional.of(new EmailVerificationToken("a@b.com", "123456", Instant.now().plusSeconds(60)) {{
                    setVerified(true);
                }}));
        when(usuarioRepo.findByEmail("a@b.com")).thenReturn(Optional.of(u));
        when(verificationRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(usuarioRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        verificationService.verificar("a@b.com", "123456");
        assertThat(token.isVerified()).isTrue();

        assertThatThrownBy(() -> verificationService.verificar("a@b.com", "123456"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    // ── 11. Resposta de erro não expõe OTP nem token ─────────────────────────

    @Test
    void respostaErro_nãoExpõeOtpNemToken() {
        when(resetRepo.findByToken("meu-token-secreto")).thenReturn(Optional.empty());

        ResponseStatusException ex = org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class,
                () -> passwordResetService.redefinirSenha("meu-token-secreto", "senha123"));

        String reason = ex.getReason() != null ? ex.getReason() : "";
        assertThat(reason).doesNotContain("meu-token-secreto");
        assertThat(reason).doesNotContain("token");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void authenticate(Long id) {
        AuthenticatedUser user = new AuthenticatedUser(id, "u@b.com", TipoUsuario.ALUNO);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null,
                        List.of(new SimpleGrantedAuthority("ROLE_ALUNO"))));
    }

    private Usuario usuario(Long id, String email) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setEmail(email);
        u.setNome("Teste");
        return u;
    }
}
