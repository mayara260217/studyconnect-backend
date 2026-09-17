package com.itb.inf3em.studyconnect.controller;

import com.itb.inf3em.studyconnect.model.dto.GoogleAuthRequestDTO;
import com.itb.inf3em.studyconnect.model.dto.LoginResponseDTO;
import com.itb.inf3em.studyconnect.model.services.GoogleAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class GoogleAuthController {

    @Autowired
    private GoogleAuthService googleAuthService;

    @PostMapping("/google")
    public LoginResponseDTO googleLogin(@RequestBody GoogleAuthRequestDTO request) {
        return googleAuthService.autenticar(request.getIdToken());
    }
}
