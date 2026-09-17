package com.itb.inf3em.studyconnect.model.services;

import com.itb.inf3em.studyconnect.model.entity.EmailChangeToken;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.repository.EmailChangeTokenRepository;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import com.itb.inf3em.studyconnect.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.security.SecureRandom;
import java.util.UUID;

@Service
public class EmailChangeService {

    private static final int EXPIRY_MINUTES = 15;
    private static final SecureRandom OTP_RANDOM = new SecureRandom();

    @Autowired private EmailChangeTokenRepository tokenRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EmailService emailService;
    @Autowired private CredentialValidationService credentialValidationService;
    @Autowired private CurrentUser currentUser;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /** Etapa 1: valida o novo e-mail, invalida solicitação anterior e envia aviso ao e-mail atual. */
    @Transactional
    public void solicitarTroca(String emailNovo) {
        Long usuarioId = currentUser.require().usuarioId();
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        credentialValidationService.validateEmail(emailNovo);
        String emailNovoNorm = emailNovo.trim().toLowerCase();

        if (usuario.getEmail().equalsIgnoreCase(emailNovoNorm)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O novo e-mail deve ser diferente do atual.");
        }
        if (usuarioRepository.existsByEmail(emailNovoNorm)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado.");
        }

        // Invalida qualquer solicitação anterior
        tokenRepository.deleteByUsuarioId(usuarioId);

        String confirmToken = UUID.randomUUID().toString();
        Instant expiracao   = Instant.now().plus(EXPIRY_MINUTES, ChronoUnit.MINUTES);

        tokenRepository.save(new EmailChangeToken(usuarioId, usuario.getEmail(), emailNovoNorm, confirmToken, expiracao));

        String link  = frontendUrl + "/confirmar-troca-email?token=" + confirmToken;
        String corpo = "Olá, " + usuario.getNome() + "!\n\n"
                + "Recebemos uma solicitação para alterar o e-mail da sua conta StudyConnect.\n\n"
                + "  De: " + usuario.getEmail() + "\n"
                + "  Para: " + emailNovoNorm + "\n\n"
                + "Se foi você, confirme clicando no link abaixo (válido por " + EXPIRY_MINUTES + " minutos):\n"
                + link + "\n\n"
                + "Se não foi você, ignore este e-mail. Nenhuma alteração será feita.";

        emailService.sendSimpleEmail(usuario.getEmail(), "Confirmação de troca de e-mail — StudyConnect", corpo);
    }

    /** Etapa 1b: usuário clicou no link — avança para etapa 2 e envia OTP ao novo e-mail. */
    @Transactional
    public String confirmarEtapa1(String confirmToken) {
        EmailChangeToken token = tokenRepository.findByConfirmToken(confirmToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link inválido ou expirado."));

        if (!token.getEtapa().equals("STEP1")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este link já foi utilizado.");
        }
        if (Instant.now().isAfter(token.getExpiresAt())) {
            tokenRepository.delete(token);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link expirado. Solicite uma nova troca.");
        }

        // Verifica de novo se o novo e-mail ainda está disponível
        if (usuarioRepository.existsByEmail(token.getEmailNovo())) {
            tokenRepository.delete(token);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já foi cadastrado por outro usuário.");
        }

        String otp      = String.format("%06d", OTP_RANDOM.nextInt(1_000_000));
        Instant expiracao = Instant.now().plus(EXPIRY_MINUTES, ChronoUnit.MINUTES);

        token.setOtpCode(otp);
        token.setEtapa("STEP2");
        token.setExpiresAt(expiracao);
        tokenRepository.save(token);

        String corpo = "Seu código de verificação para confirmar o novo e-mail é:\n\n"
                + otp + "\n\n"
                + "Válido por " + EXPIRY_MINUTES + " minutos.\n"
                + "Se você não solicitou esta alteração, ignore este e-mail.";

        emailService.sendSimpleEmail(token.getEmailNovo(), "Código de verificação — StudyConnect", corpo);

        // Retorna o novo e-mail para o frontend poder mostrar para onde enviou o OTP
        return token.getEmailNovo();
    }

    /** Etapa 2: valida o OTP e efetua a troca no banco. */
    @Transactional
    public void verificarOtp(String otp) {
        Long usuarioId = currentUser.require().usuarioId();
        EmailChangeToken token = tokenRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma solicitação de troca em andamento."));

        if (!token.getEtapa().equals("STEP2")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confirme primeiro o e-mail atual.");
        }
        if (!token.getUsuarioId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Fluxo de troca de e-mail invalido.");
        }
        if (Instant.now().isAfter(token.getExpiresAt())) {
            tokenRepository.delete(token);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código expirado. Inicie o processo novamente.");
        }
        if (otp == null || !token.getOtpCode().equals(otp)) {
            tokenRepository.delete(token);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido.");
        }

        Usuario usuario = usuarioRepository.findById(token.getUsuarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        usuario.setEmail(token.getEmailNovo());
        usuarioRepository.save(usuario);
        tokenRepository.delete(token);
    }
}
