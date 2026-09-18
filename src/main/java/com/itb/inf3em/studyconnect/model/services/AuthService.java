package com.itb.inf3em.studyconnect.model.services;

import com.itb.inf3em.studyconnect.model.dto.LoginRequestDTO;
import com.itb.inf3em.studyconnect.model.dto.LoginResponseDTO;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.repository.EmailVerificationTokenRepository;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import com.itb.inf3em.studyconnect.security.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CredentialValidationService credentialValidationService;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private TokenService tokenService;

    public LoginResponseDTO login(LoginRequestDTO request) {
        long t0 = System.currentTimeMillis();

        credentialValidationService.validateEmail(request.getEmail());
        if (request.getSenha() == null || request.getSenha().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos"));

        log.info("[AUTH] query DB: {}ms", System.currentTimeMillis() - t0);

        if (usuario.getSenha() == null || !passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos");
        }

        if (!usuario.isAtivo()) {
            boolean jaVerificada = usuario.getGoogleId() != null
                    || tokenRepository.findByEmail(usuario.getEmail())
                        .map(t -> t.isVerified())
                        .orElse(false);

            if (!jaVerificada) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "E-mail nao verificado. Verifique sua caixa de entrada.");
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Conta suspensa. Entre em contato com o suporte.");
        }

        log.info("[AUTH] login total: {}ms", System.currentTimeMillis() - t0);

        return new LoginResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getTipoUsuario().name(),
                usuario.getFotoUrl(),
                usuario.getEmail(),
                usuario.isAtivo(),
                tokenService.generateToken(usuario),
                tokenService.getExpirationSeconds()
        );
    }
}
