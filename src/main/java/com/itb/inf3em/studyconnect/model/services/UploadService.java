package com.itb.inf3em.studyconnect.model.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class UploadService {

    private static final List<String> TIPOS_PERMITIDOS = List.of(
            "application/pdf",
            "image/png",
            "image/jpeg"
    );

    private static final long MAX_BYTES = 10 * 1024 * 1024; // 10 MB

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.base-url:http://localhost:8080}")
    private String baseUrl;

    public String salvar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo vazio.");
        }
        if (!TIPOS_PERMITIDOS.contains(arquivo.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tipo não permitido. Envie PDF, PNG, JPG ou JPEG.");
        }
        if (arquivo.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo maior que 10 MB.");
        }

        String extensao = extensao(arquivo.getOriginalFilename());
        String nomeArquivo = UUID.randomUUID() + extensao;

        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);
            arquivo.transferTo(dir.resolve(nomeArquivo));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao salvar arquivo.");
        }

        return baseUrl + "/uploads/" + nomeArquivo;
    }

    private String extensao(String nomeOriginal) {
        if (nomeOriginal == null) return "";
        int idx = nomeOriginal.lastIndexOf('.');
        return idx >= 0 ? nomeOriginal.substring(idx).toLowerCase() : "";
    }
}
