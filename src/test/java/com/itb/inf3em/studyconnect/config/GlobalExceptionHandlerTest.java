package com.itb.inf3em.studyconnect.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ── erro inesperado não expõe mensagem interna ────────────────────────────

    @Test
    void erroInesperado_naoExpoeMensagemInterna() {
        Exception ex = new RuntimeException("Connection to database failed: jdbc:sqlserver://somee...");

        ResponseEntity<Map<String, Object>> response = handler.handleGeneric(ex);

        assertEquals(500, response.getStatusCode().value());
        String error = (String) response.getBody().get("error");
        assertFalse(error.contains("Connection"), "Mensagem de banco nao deve ser exposta");
        assertFalse(error.contains("jdbc"), "URL de banco nao deve ser exposta");
        assertFalse(error.contains("sqlserver"), "Detalhes de banco nao devem ser expostos");
    }

    // ── erro inesperado não expõe stack trace ─────────────────────────────────

    @Test
    void erroInesperado_naoExpoeStackTrace() {
        Exception ex = new NullPointerException("at com.itb.inf3em.studyconnect.SomeClass.method(SomeClass.java:42)");

        ResponseEntity<Map<String, Object>> response = handler.handleGeneric(ex);

        String error = (String) response.getBody().get("error");
        assertFalse(error.contains("at com."), "Stack trace nao deve ser exposto");
        assertFalse(error.contains("NullPointerException"), "Nome de classe nao deve ser exposto");
        assertFalse(error.contains(".java:"), "Linha de codigo nao deve ser exposta");
    }

    // ── resposta 500 contém apenas mensagem segura ────────────────────────────

    @Test
    void erroInesperado_retornaMensagemGenerica() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleGeneric(new IllegalStateException("internal state error"));

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Ocorreu um erro interno no servidor.", response.getBody().get("error"));
        assertEquals(500, response.getBody().get("status"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    // ── erro inesperado não expõe nome de classe ──────────────────────────────

    @Test
    void erroInesperado_naoExpoeNomeDeClasse() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleGeneric(new ClassCastException("Cannot cast Foo to Bar"));

        String error = (String) response.getBody().get("error");
        assertFalse(error.contains("ClassCastException"), "Nome de classe nao deve ser exposto");
        assertFalse(error.contains("Cannot cast"), "Mensagem interna nao deve ser exposta");
    }

    // ── erros de negócio continuam com mensagens apropriadas ─────────────────

    @Test
    void erroNegocio_400_mensagemPreservada() {
        ResponseStatusException ex = new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Mensagem obrigatoria.");

        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatus(ex);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Mensagem obrigatoria.", response.getBody().get("error"));
    }

    @Test
    void erroNegocio_401_mensagemPreservada() {
        ResponseStatusException ex = new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos");

        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatus(ex);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("E-mail ou senha incorretos", response.getBody().get("error"));
    }

    @Test
    void erroNegocio_403_mensagemPreservada() {
        ResponseStatusException ex = new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Conta suspensa. Entre em contato com o suporte.");

        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatus(ex);

        assertEquals(403, response.getStatusCode().value());
        assertEquals("Conta suspensa. Entre em contato com o suporte.", response.getBody().get("error"));
    }

    @Test
    void erroNegocio_404_mensagemPreservada() {
        ResponseStatusException ex = new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Ticket nao encontrado.");

        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatus(ex);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Ticket nao encontrado.", response.getBody().get("error"));
    }

    // ── ResponseStatusException sem reason usa mensagem padrão segura ─────────

    @Test
    void erroNegocio_semReason_usaMensagemPadrao() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST);

        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatus(ex);

        assertEquals(400, response.getStatusCode().value());
        String error = (String) response.getBody().get("error");
        assertNotNull(error);
        assertFalse(error.isBlank(), "Mensagem padrao nao deve ser vazia");
        assertFalse(error.contains("BAD_REQUEST"), "Enum do Spring nao deve ser exposto");
    }

    // ── status HTTP corretos preservados ──────────────────────────────────────

    @Test
    void statusHttpCorretos_preservados() {
        assertEquals(400, handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "x")).getStatusCode().value());
        assertEquals(401, handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "x")).getStatusCode().value());
        assertEquals(403, handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.FORBIDDEN, "x")).getStatusCode().value());
        assertEquals(404, handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "x")).getStatusCode().value());
        assertEquals(500, handler.handleGeneric(
                new RuntimeException("x")).getStatusCode().value());
    }
}
