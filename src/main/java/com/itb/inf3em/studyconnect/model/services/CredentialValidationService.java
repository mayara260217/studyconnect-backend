package com.itb.inf3em.studyconnect.model.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class CredentialValidationService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile("[^A-Za-z0-9]");

    public void validateEmail(String email) {
        if (email == null || email.isBlank() || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail invalido.");
        }
    }

    public void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha e obrigatoria.");
        }

        if (PASSWORD_PATTERN.matcher(password).matches()) {
            return;
        }

        List<String> missing = new ArrayList<>();
        if (password.length() < 8) {
            missing.add("no minimo 8 caracteres");
        }
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            missing.add("pelo menos uma letra maiuscula");
        }
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            missing.add("pelo menos uma letra minuscula");
        }
        if (!NUMBER_PATTERN.matcher(password).find()) {
            missing.add("pelo menos um numero");
        }
        if (!SPECIAL_PATTERN.matcher(password).find()) {
            missing.add("pelo menos um caractere especial");
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A senha deve conter " + String.join(", ", missing) + "."
        );
    }
}
