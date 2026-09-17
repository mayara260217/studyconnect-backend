package com.itb.inf3em.studyconnect.controller;

import com.itb.inf3em.studyconnect.model.dto.ForgotPasswordRequestDTO;
import com.itb.inf3em.studyconnect.model.dto.LoginRequestDTO;
import com.itb.inf3em.studyconnect.model.dto.LoginResponseDTO;
import com.itb.inf3em.studyconnect.model.dto.ResetPasswordRequestDTO;
import com.itb.inf3em.studyconnect.model.services.AuthService;
import com.itb.inf3em.studyconnect.model.services.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
        passwordResetService.solicitarRecuperacao(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Se o e-mail estiver cadastrado, você receberá as instruções em breve."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        passwordResetService.redefinirSenha(request.getToken(), request.getNovaSenha());
        return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso."));
    }
}