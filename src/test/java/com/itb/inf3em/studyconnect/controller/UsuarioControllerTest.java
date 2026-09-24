package com.itb.inf3em.studyconnect.controller;

import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.services.UsuarioService;
import com.itb.inf3em.studyconnect.security.CurrentUser;
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
 * Sprint 12 - Testes de integracao da camada controller para o cadastro de usuario.
 *
 * Endpoint testado: {@code POST /api/v1/usuarios} - a mesma rota usada pelo app
 * mobile na tela de cadastro (Sprint 10).
 */
@WebMvcTest(
        controllers = UsuarioController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { SecurityConfig.class, JwtAuthenticationFilter.class, RateLimitAuthFilter.class }
        )
)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    private static final String USUARIOS_URL = "/api/v1/usuarios";

    @Autowired private MockMvc mockMvc;

    @MockitoBean private UsuarioService usuarioService;
    @MockitoBean private CurrentUser currentUser;

    // -- Cenario 1: cadastro valido -> 201 sem expor a senha -----------------

    @Test
    @DisplayName("POST /usuarios com dados validos retorna 201 sem expor a senha")
    void cadastroValido_retorna201() throws Exception {
        when(usuarioService.save(any(Usuario.class))).thenReturn(usuarioSalvo());

        mockMvc.perform(post(USUARIOS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Aluno Novo\",\"email\":\"novo@email.com\",\"senha\":\"Senha@123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nome").value("Aluno Novo"))
                .andExpect(jsonPath("$.email").value("novo@email.com"))
                .andExpect(jsonPath("$.tipoUsuario").value("ALUNO"))
                .andExpect(jsonPath("$.ativo").value(false))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    // -- Cenario 2: e-mail duplicado -> 409 ----------------------------------

    @Test
    @DisplayName("POST /usuarios com e-mail ja cadastrado retorna 409")
    void cadastroComEmailDuplicado_retorna409() throws Exception {
        when(usuarioService.save(any(Usuario.class))).thenThrow(
                new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail ja esta cadastrado."));

        mockMvc.perform(post(USUARIOS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Aluno Novo\",\"email\":\"novo@email.com\",\"senha\":\"Senha@123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Este e-mail ja esta cadastrado."));
    }

    // -- Cenario 3: senha fora da politica -> 400 ----------------------------

    @Test
    @DisplayName("POST /usuarios com senha fraca retorna 400")
    void cadastroComSenhaFraca_retorna400() throws Exception {
        when(usuarioService.save(any(Usuario.class))).thenThrow(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A senha deve conter no minimo 8 caracteres, pelo menos uma letra maiuscula."));

        mockMvc.perform(post(USUARIOS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Aluno Novo\",\"email\":\"novo@email.com\",\"senha\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // -- helper --------------------------------------------------------------

    private Usuario usuarioSalvo() {
        Usuario u = new Usuario();
        u.setId(10L);
        u.setNome("Aluno Novo");
        u.setEmail("novo@email.com");
        u.setSenha("$2a$10$hash");
        u.setTipoUsuario(TipoUsuario.ALUNO);
        u.setAtivo(false);
        return u;
    }
}
