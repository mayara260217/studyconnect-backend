package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.controller.EmailChangeController;
import com.itb.inf3em.studyconnect.model.entity.EmailChangeToken;
import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.repository.EmailChangeTokenRepository;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import com.itb.inf3em.studyconnect.model.services.CredentialValidationService;
import com.itb.inf3em.studyconnect.model.services.EmailChangeService;
import com.itb.inf3em.studyconnect.model.services.EmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailChangeSecurityTest {

    private final CurrentUser currentUser = new CurrentUser();
    private EmailChangeTokenRepository tokenRepository;
    private UsuarioRepository usuarioRepository;
    private EmailChangeService emailChangeService;

    @BeforeEach
    void setUp() {
        tokenRepository = mock(EmailChangeTokenRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        emailChangeService = new EmailChangeService();
        ReflectionTestUtils.setField(emailChangeService, "tokenRepository", tokenRepository);
        ReflectionTestUtils.setField(emailChangeService, "usuarioRepository", usuarioRepository);
        ReflectionTestUtils.setField(emailChangeService, "emailService", mock(EmailService.class));
        ReflectionTestUtils.setField(emailChangeService, "credentialValidationService", mock(CredentialValidationService.class));
        ReflectionTestUtils.setField(emailChangeService, "currentUser", currentUser);
        ReflectionTestUtils.setField(emailChangeService, "frontendUrl", "http://localhost:5173");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requestIsBoundToAuthenticatedUserEvenWhenBodyContainsAnotherId() {
        authenticate(10L);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario(10L, "atual@example.com")));
        when(usuarioRepository.existsByEmail("novo@example.com")).thenReturn(false);
        when(tokenRepository.save(any(EmailChangeToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        emailChangeService.solicitarTroca("novo@example.com");

        ArgumentCaptor<EmailChangeToken> captor = ArgumentCaptor.forClass(EmailChangeToken.class);
        verify(tokenRepository).save(captor.capture());
        assertEquals(10L, captor.getValue().getUsuarioId());
    }

    @Test
    void controllerIgnoresUsuarioIdFromRequestBody() {
        authenticate(10L);
        EmailChangeService service = mock(EmailChangeService.class);
        EmailChangeController controller = new EmailChangeController();
        ReflectionTestUtils.setField(controller, "emailChangeService", service);
        ReflectionTestUtils.setField(controller, "currentUser", currentUser);

        controller.request(Map.of("usuarioId", 20L, "emailNovo", "novo@example.com"));

        verify(service).solicitarTroca("novo@example.com");
        verify(service, never()).verificarOtp(any());
    }

    @Test
    void userCannotVerifyAnotherUsersFlow() {
        authenticate(10L);
        EmailChangeToken tokenDeB = token(20L, "novo-b@example.com", "123456", "STEP2", Instant.now().plusSeconds(60));
        when(tokenRepository.findByUsuarioId(10L)).thenReturn(Optional.of(tokenDeB));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> emailChangeService.verificarOtp("123456"));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void expiredAndInvalidOtpAreRejectedAndInvalidateTheFlow() {
        authenticate(10L);
        EmailChangeToken expired = token(10L, "novo@example.com", "123456", "STEP2", Instant.now().minusSeconds(1));
        when(tokenRepository.findByUsuarioId(10L)).thenReturn(Optional.of(expired));
        assertThrows(ResponseStatusException.class, () -> emailChangeService.verificarOtp("123456"));
        verify(tokenRepository).delete(expired);

        EmailChangeToken invalid = token(10L, "novo@example.com", "123456", "STEP2", Instant.now().plusSeconds(60));
        when(tokenRepository.findByUsuarioId(10L)).thenReturn(Optional.of(invalid));
        assertThrows(ResponseStatusException.class, () -> emailChangeService.verificarOtp("000000"));
        verify(tokenRepository).delete(invalid);
    }

    @Test
    void successfulOtpCannotBeReused() {
        authenticate(10L);
        EmailChangeToken token = token(10L, "novo@example.com", "123456", "STEP2", Instant.now().plusSeconds(60));
        Usuario usuario = usuario(10L, "atual@example.com");
        when(tokenRepository.findByUsuarioId(10L)).thenReturn(Optional.of(token), Optional.empty());
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        emailChangeService.verificarOtp("123456");
        assertEquals("novo@example.com", usuario.getEmail());
        verify(tokenRepository).delete(token);
        assertThrows(ResponseStatusException.class, () -> emailChangeService.verificarOtp("123456"));
    }

    @Test
    void expiredConfirmationAndRegisteredNewEmailAreRejected() {
        EmailChangeToken expired = token(10L, "novo@example.com", null, "STEP1", Instant.now().minusSeconds(1));
        when(tokenRepository.findByConfirmToken("expired")).thenReturn(Optional.of(expired));
        assertThrows(ResponseStatusException.class, () -> emailChangeService.confirmarEtapa1("expired"));
        verify(tokenRepository).delete(expired);

        authenticate(10L);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario(10L, "atual@example.com")));
        when(usuarioRepository.existsByEmail("ocupado@example.com")).thenReturn(true);
        assertThrows(ResponseStatusException.class, () -> emailChangeService.solicitarTroca("ocupado@example.com"));
    }

    @Test
    void requestAndVerifyWithoutIdentityAreBlockedBeforeAnyPersistence() {
        assertThrows(AccessDeniedException.class, () -> emailChangeService.solicitarTroca("novo@example.com"));
        assertThrows(AccessDeniedException.class, () -> emailChangeService.verificarOtp("123456"));
    }

    private void authenticate(Long usuarioId) {
        AuthenticatedUser user = new AuthenticatedUser(usuarioId, "usuario@example.com", TipoUsuario.ALUNO);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_ALUNO"))));
    }

    private EmailChangeToken token(Long usuarioId, String emailNovo, String otp, String etapa, Instant expiresAt) {
        EmailChangeToken token = new EmailChangeToken(usuarioId, "atual@example.com", emailNovo, "confirm-token", expiresAt);
        token.setEtapa(etapa);
        token.setOtpCode(otp);
        return token;
    }

    private Usuario usuario(Long id, String email) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setEmail(email);
        usuario.setNome("Usuario");
        return usuario;
    }
}
