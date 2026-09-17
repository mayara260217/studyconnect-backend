package com.itb.inf3em.studyconnect.model.services;

import com.itb.inf3em.studyconnect.model.dto.LoginResponseDTO;
import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import com.itb.inf3em.studyconnect.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class GoogleAuthService {

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthService.class);
    private static final String GOOGLE_TOKEN_INFO = "https://oauth2.googleapis.com/tokeninfo?id_token=";
    static final List<String> VALID_ISSUERS = List.of("accounts.google.com", "https://accounts.google.com");

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final RestClient restClient;

    @Value("${app.google.client-id:}")
    private String expectedClientId;

    @Autowired
    public GoogleAuthService(UsuarioRepository usuarioRepository, JwtService jwtService) {
        this(usuarioRepository, jwtService, RestClient.create());
    }

    public GoogleAuthService(UsuarioRepository usuarioRepository, JwtService jwtService, RestClient restClient) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.restClient = restClient;
    }

    @Transactional
    public LoginResponseDTO autenticar(String idToken) {
        Map<String, Object> payload = verificarToken(idToken);

        String googleId = (String) payload.get("sub");
        String email    = (String) payload.get("email");
        String nome     = (String) payload.get("name");
        String fotoUrl  = (String) payload.get("picture");

        if (googleId == null || email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Google invalido.");
        }

        Usuario usuario = usuarioRepository.findByEmail(email.toLowerCase())
                .orElseGet(() -> criarUsuarioGoogle(nome, email, googleId, fotoUrl));

        // Atualiza googleId e foto se ainda nao estiver salvo
        boolean atualizado = false;
        if (usuario.getGoogleId() == null) {
            usuario.setGoogleId(googleId);
            atualizado = true;
        }
        if (fotoUrl != null && !fotoUrl.equals(usuario.getFotoUrl())) {
            usuario.setFotoUrl(fotoUrl);
            atualizado = true;
        }
        if (atualizado) {
            usuarioRepository.save(usuario);
        }

        if (!usuario.isAtivo()) {
            // Bloqueia login se a conta estiver suspensa pelo admin
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Conta suspensa. Entre em contato com o suporte.");
        }

        return new LoginResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getTipoUsuario().name(),
                usuario.getFotoUrl(),
                usuario.getEmail(),
                usuario.isAtivo(),
                jwtService.generateToken(usuario),
                jwtService.getExpirationSeconds()
        );
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> verificarToken(String idToken) {
        if (expectedClientId == null || expectedClientId.isBlank()) {
            log.error("[GoogleAuth] APP_GOOGLE_CLIENT_ID nao configurado.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Google invalido.");
        }

        try {
            Map<String, Object> payload = restClient.get()
                    .uri(GOOGLE_TOKEN_INFO + idToken)
                    .retrieve()
                    .body(Map.class);

            if (payload == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Google invalido.");
            }

            // Valida audience
            String aud = (String) payload.get("aud");
            if (!expectedClientId.equals(aud)) {
                log.warn("[GoogleAuth] aud invalido.");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Google invalido.");
            }

            // Valida issuer
            String iss = (String) payload.get("iss");
            if (iss == null || !VALID_ISSUERS.contains(iss)) {
                log.warn("[GoogleAuth] iss invalido.");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Google invalido.");
            }

            // Valida email_verified
            Object emailVerified = payload.get("email_verified");
            boolean verified = "true".equals(emailVerified) || Boolean.TRUE.equals(emailVerified);
            if (!verified) {
                log.warn("[GoogleAuth] email_verified ausente ou false.");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Google invalido.");
            }

            return payload;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("[GoogleAuth] Falha ao verificar token: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nao foi possivel verificar o token Google.");
        }
    }

    private Usuario criarUsuarioGoogle(String nome, String email, String googleId, String fotoUrl) {
        Usuario u = new Usuario();
        u.setNome(nome != null ? nome : email.split("@")[0]);
        u.setEmail(email.toLowerCase());
        u.setGoogleId(googleId);
        u.setFotoUrl(fotoUrl);
        u.setTipoUsuario(TipoUsuario.ALUNO);
        u.setAtivo(true);
        // senha nula — usuario Google nao tem senha local
        return usuarioRepository.save(u);
    }
}
