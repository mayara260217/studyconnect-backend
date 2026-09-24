package com.itb.inf3em.studyconnect.controller;

import com.itb.inf3em.studyconnect.model.dto.LoginRequestDTO;
import com.itb.inf3em.studyconnect.model.dto.LoginResponseDTO;
import com.itb.inf3em.studyconnect.model.services.AuthService;
import com.itb.inf3em.studyconnect.model.services.PasswordResetService;
import com.itb.inf3em.studyconnect.config.SecurityConfig;
import com.itb.inf3em.studyconnect.security.JwtAuthenticationFilter;
import com.itb.inf3em.studyconnect.security.RateLimitAuthFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 12 - Teste de integracao da camada controller (HTTP -> Controller -> resposta).
 *
 * O MockMvc sobe apenas a camada web (sem banco e sem servidor Tomcat), com o
 * AuthService substituido por um duble de teste. As configuracoes de seguranca
 * (SecurityConfig e JwtAuthenticationFilter) sao excluidas do slice e os filtros
 * desligados: aqui o objetivo e validar o controller, e as regras de autorizacao
 * tem testes dedicados no pacote {@code security}.
 */
@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { SecurityConfig.class, JwtAuthenticationFilter.class, RateLimitAuthFilter.class }
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    private static final String LOGIN_URL = "/api/v1/auth/login";

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AuthService authService;
    @MockitoBean private PasswordResetService passwordResetService;

    // -- Cenario 1: credenciais validas -> 200 com token ---------------------

    @Test
    @DisplayName("POST /auth/login com credenciais validas retorna 200 e token")
    void loginComCredenciaisValidas_retorna200ComToken() throws Exception {
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(new LoginResponseDTO(
                1L, "Aluno Teste", "ALUNO", null, "aluno@email.com", true, "token-de-teste", 900L));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"aluno@email.com\",\"senha\":\"Senha@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token-de-teste"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("aluno@email.com"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    // -- Cenario 2: senha errada -> 401 --------------------------------------

    @Test
    @DisplayName("POST /auth/login com senha errada retorna 401")
    void loginComSenhaErrada_retorna401() throws Exception {
        when(authService.login(any(LoginRequestDTO.class))).thenThrow(
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"aluno@email.com\",\"senha\":\"senha-errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("E-mail ou senha incorretos"));
    }

    // -- Cenario 3: body vazio -> 400 ----------------------------------------

    @Test
    @DisplayName("POST /auth/login com body vazio retorna 400")
    void loginComBodyVazio_retorna400() throws Exception {
        when(authService.login(any(LoginRequestDTO.class))).thenThrow(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail invalido."));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // -- Bonus: e-mail nao verificado -> 403 ---------------------------------

    @Test
    @DisplayName("POST /auth/login com e-mail nao verificado retorna 403")
    void loginComEmailNaoVerificado_retorna403() throws Exception {
        when(authService.login(any(LoginRequestDTO.class))).thenThrow(new ResponseStatusException(
                HttpStatus.FORBIDDEN, "E-mail nao verificado. Verifique sua caixa de entrada."));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"aluno@email.com\",\"senha\":\"Senha@123\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("E-mail nao verificado. Verifique sua caixa de entrada."));
    }
}
