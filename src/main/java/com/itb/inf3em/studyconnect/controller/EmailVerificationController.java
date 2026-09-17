package com.itb.inf3em.studyconnect.controller;

import com.itb.inf3em.studyconnect.model.dto.ResendVerificationRequestDTO;
import com.itb.inf3em.studyconnect.model.dto.VerifyEmailRequestDTO;
import com.itb.inf3em.studyconnect.model.services.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class EmailVerificationController {

    @Autowired
    private EmailVerificationService emailVerificationService;

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestBody VerifyEmailRequestDTO request) {
        emailVerificationService.verificar(request.getEmail(), request.getCode());
        return ResponseEntity.ok(Map.of("message", "E-mail verificado com sucesso."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(@RequestBody ResendVerificationRequestDTO request) {
        emailVerificationService.enviarCodigo(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Código reenviado."));
    }
}
