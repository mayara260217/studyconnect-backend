package com.itb.inf3em.studyconnect.model.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final RestClient restClient = RestClient.create();

    @Value("${app.brevo.api-key:}")
    private String apiKey;

    @Value("${app.mail.from-name:StudyConnect}")
    private String fromName;

    @Value("${app.mail.from-email:studyconnect2026@gmail.com}")
    private String fromEmail;

    public void sendTestEmail(String to) {
        sendSimpleEmail(to, "Teste de envio de e-mail", "O envio de e-mail do StudyConnect funcionou.");
    }

    public void sendSimpleEmail(String to, String subject, String body) {
        validateRecipient(to);

        if (apiKey == null || apiKey.isBlank()) {
            log.error("[EmailService] APP_BREVO_API_KEY nao configurada.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Servico de e-mail nao configurado.");
        }

        Map<String, Object> payload = Map.of(
                "sender",  Map.of("name", fromName, "email", fromEmail),
                "to",      List.of(Map.of("email", to.trim())),
                "subject", subject,
                "textContent", body
        );

        try {
            restClient.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("[EmailService] E-mail enviado para: {}", to);
        } catch (Exception ex) {
            log.error("[EmailService] Falha ao enviar e-mail para {}: {}", to, ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Nao foi possivel enviar o e-mail. Tente novamente.");
        }
    }

    private void validateRecipient(String to) {
        if (to == null || to.isBlank() || !EMAIL_PATTERN.matcher(to.trim()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail de destino invalido.");
        }
    }
}
