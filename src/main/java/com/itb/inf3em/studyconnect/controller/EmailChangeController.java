package com.itb.inf3em.studyconnect.controller;

import com.itb.inf3em.studyconnect.model.services.EmailChangeService;
import com.itb.inf3em.studyconnect.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class EmailChangeController {

    @Autowired
    private EmailChangeService emailChangeService;

    @Autowired
    private CurrentUser currentUser;

    /** Etapa 1 — solicita troca, envia aviso ao e-mail atual */
    @PostMapping("/email-change/request")
    public ResponseEntity<Map<String, String>> request(@RequestBody Map<String, Object> body) {
        currentUser.require();
        String emailNovo = body.get("emailNovo") == null ? null : body.get("emailNovo").toString();
        emailChangeService.solicitarTroca(emailNovo);
        return ResponseEntity.ok(Map.of("message", "Aviso enviado para o seu e-mail atual. Verifique sua caixa de entrada."));
    }

    /** Etapa 1b — usuário clicou no link de confirmação */
    @GetMapping("/email-change/confirm")
    public ResponseEntity<Map<String, String>> confirm(@RequestParam String token) {
        String emailNovo = emailChangeService.confirmarEtapa1(token);
        return ResponseEntity.ok(Map.of(
                "message", "Confirmado! Enviamos um código para o novo e-mail.",
                "emailNovo", emailNovo
        ));
    }

    /** Etapa 2 — verifica OTP e efetua a troca */
    @PostMapping("/email-change/verify")
    public ResponseEntity<Map<String, String>> verify(@RequestBody Map<String, Object> body) {
        currentUser.require();
        String otp = body.get("otp") == null ? null : body.get("otp").toString();
        emailChangeService.verificarOtp(otp);
        return ResponseEntity.ok(Map.of("message", "E-mail alterado com sucesso."));
    }
}
