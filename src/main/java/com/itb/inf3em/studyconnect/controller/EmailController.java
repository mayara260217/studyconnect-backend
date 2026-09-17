package com.itb.inf3em.studyconnect.controller;

import com.itb.inf3em.studyconnect.model.dto.EmailTestRequestDTO;
import com.itb.inf3em.studyconnect.model.services.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> sendTestEmail(@RequestBody EmailTestRequestDTO request) {
        emailService.sendTestEmail(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "E-mail de teste enviado com sucesso."));
    }
}
