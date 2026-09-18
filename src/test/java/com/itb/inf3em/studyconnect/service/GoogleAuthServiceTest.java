package com.itb.inf3em.studyconnect.service;

import tools.jackson.databind.ObjectMapper;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import com.itb.inf3em.studyconnect.model.services.GoogleAuthService;
import com.itb.inf3em.studyconnect.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GoogleAuthServiceTest {

    private static final String CLIENT_ID = "my-client-id.apps.googleusercontent.com";
    private static final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token={token}";

    private MockRestServiceServer mockServer;
    private GoogleAuthService service;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        service = new GoogleAuthService(
                mock(UsuarioRepository.class),
                mock(TokenService.class),
                restClient
        );
        ReflectionTestUtils.setField(service, "expectedClientId", CLIENT_ID);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Map<String, Object> validPayload() {
        Map<String, Object> p = new HashMap<>();
        p.put("aud", CLIENT_ID);
        p.put("iss", "accounts.google.com");
        p.put("email_verified", "true");
        p.put("sub", "1234567890");
        p.put("email", "user@example.com");
        p.put("name", "Test User");
        return p;
    }

    private void expectTokenInfo(String token, Map<String, Object> payload) throws Exception {
        mockServer.expect(requestToUriTemplate(TOKEN_INFO_URL, token))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(payload), MediaType.APPLICATION_JSON));
    }

    // ── cenário 1: token válido com aud correto → permitido ──────────────────

    @Test
    void tokenValido_audCorreto_retornaPayload() throws Exception {
        expectTokenInfo("valid-token", validPayload());

        Map<String, Object> result = service.verificarToken("valid-token");

        assertThat(result.get("email")).isEqualTo("user@example.com");
        mockServer.verify();
    }

    // ── cenário 2: aud incorreto → rejeitado ─────────────────────────────────

    @Test
    void audIncorreto_rejeitado() throws Exception {
        Map<String, Object> payload = validPayload();
        payload.put("aud", "outro-client-id.apps.googleusercontent.com");
        expectTokenInfo("bad-aud-token", payload);

        assertThatThrownBy(() -> service.verificarToken("bad-aud-token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
        mockServer.verify();
    }

    // ── cenário 3: APP_GOOGLE_CLIENT_ID ausente → rejeitado ──────────────────

    @Test
    void clientIdAusente_rejeitado() {
        ReflectionTestUtils.setField(service, "expectedClientId", "");

        assertThatThrownBy(() -> service.verificarToken("any-token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
        // nenhuma chamada HTTP deve ter sido feita
        mockServer.verify();
    }

    // ── cenário 4: issuer incorreto → rejeitado ───────────────────────────────

    @Test
    void issuerIncorreto_rejeitado() throws Exception {
        Map<String, Object> payload = validPayload();
        payload.put("iss", "https://evil.com");
        expectTokenInfo("bad-iss-token", payload);

        assertThatThrownBy(() -> service.verificarToken("bad-iss-token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
        mockServer.verify();
    }

    // ── cenário 5: email_verified=false → rejeitado ───────────────────────────

    @Test
    void emailVerifiedFalse_rejeitado() throws Exception {
        Map<String, Object> payload = validPayload();
        payload.put("email_verified", "false");
        expectTokenInfo("unverified-token", payload);

        assertThatThrownBy(() -> service.verificarToken("unverified-token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
        mockServer.verify();
    }

    // ── cenário 6: email_verified ausente → rejeitado ─────────────────────────

    @Test
    void emailVerifiedAusente_rejeitado() throws Exception {
        Map<String, Object> payload = validPayload();
        payload.remove("email_verified");
        expectTokenInfo("no-verified-token", payload);

        assertThatThrownBy(() -> service.verificarToken("no-verified-token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
        mockServer.verify();
    }

    // ── cenário 7: token inválido/assinatura inválida → rejeitado ─────────────

    @Test
    void tokenInvalido_assinaturaInvalida_rejeitado() {
        mockServer.expect(requestToUriTemplate(TOKEN_INFO_URL, "invalid-token"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> service.verificarToken("invalid-token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
        mockServer.verify();
    }

    // ── cenário 8: token expirado → rejeitado ─────────────────────────────────

    @Test
    void tokenExpirado_rejeitado() {
        mockServer.expect(requestToUriTemplate(TOKEN_INFO_URL, "expired-token"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> service.verificarToken("expired-token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
        mockServer.verify();
    }

    // ── bônus: aceita iss alternativo do Google ───────────────────────────────

    @Test
    void issuerAlternativoGoogle_aceito() throws Exception {
        Map<String, Object> payload = validPayload();
        payload.put("iss", "https://accounts.google.com");
        expectTokenInfo("alt-iss-token", payload);

        Map<String, Object> result = service.verificarToken("alt-iss-token");

        assertThat(result.get("sub")).isEqualTo("1234567890");
        mockServer.verify();
    }
}
