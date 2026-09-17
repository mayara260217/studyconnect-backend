package com.itb.inf3em.studyconnect.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Trata ResponseStatusException — erros de negócio deliberados.
     * getReason() contém a mensagem segura definida pelo código da aplicação.
     * Quando getReason() é null (ex: lançado sem mensagem), retorna mensagem genérica por status.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        String message = ex.getReason() != null ? ex.getReason() : defaultMessageFor(ex.getStatusCode().value());
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                "timestamp", Instant.now(),
                "status", ex.getStatusCode().value(),
                "error", message
        ));
    }

    /**
     * Trata exceções inesperadas.
     * Loga o stack trace internamente para diagnóstico.
     * Retorna mensagem genérica ao cliente — sem detalhes internos.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("[GlobalExceptionHandler] Erro inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", Instant.now(),
                "status", 500,
                "error", "Ocorreu um erro interno no servidor."
        ));
    }

    private String defaultMessageFor(int status) {
        return switch (status) {
            case 400 -> "Requisicao invalida.";
            case 401 -> "Autenticacao obrigatoria.";
            case 403 -> "Acesso negado.";
            case 404 -> "Recurso nao encontrado.";
            case 409 -> "Conflito de dados.";
            default  -> "Erro ao processar a requisicao.";
        };
    }
}
