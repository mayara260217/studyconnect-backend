package com.itb.inf3em.studyconnect.model.services;

import com.itb.inf3em.studyconnect.model.dto.DuvidaDTO;
import com.itb.inf3em.studyconnect.model.entity.Duvida;
import com.itb.inf3em.studyconnect.model.repository.AulaRepository;
import com.itb.inf3em.studyconnect.model.repository.DuvidaRepository;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import com.itb.inf3em.studyconnect.security.DuvidaAuthorization;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class DuvidaService {

    private final DuvidaRepository duvidaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AulaRepository aulaRepository;
    private final DuvidaAuthorization duvidaAuthorization;

    public DuvidaService(DuvidaRepository duvidaRepository,
                         UsuarioRepository usuarioRepository,
                         AulaRepository aulaRepository,
                         DuvidaAuthorization duvidaAuthorization) {
        this.duvidaRepository = duvidaRepository;
        this.usuarioRepository = usuarioRepository;
        this.aulaRepository = aulaRepository;
        this.duvidaAuthorization = duvidaAuthorization;
    }

    private DuvidaDTO toDTO(Duvida d) {
        String alunoNome  = usuarioRepository.findById(d.getAlunoId()).map(u -> u.getNome()).orElse("Aluno");
        String aulaTitulo = aulaRepository.findById(d.getAulaId()).map(a -> a.getTitulo()).orElse("Aula");
        return new DuvidaDTO(d, alunoNome, aulaTitulo);
    }

    public DuvidaDTO criar(Map<String, Object> body) {
        Long requestedAlunoId = body.containsKey("alunoId") && body.get("alunoId") != null
                ? Long.valueOf(body.get("alunoId").toString()) : null;
        Long alunoId = duvidaAuthorization.resolveAlunoId(requestedAlunoId);
        Long aulaId   = Long.valueOf(body.get("aulaId").toString());
        String msg    = body.get("mensagem").toString().trim();

        if (msg.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mensagem obrigatória.");

        usuarioRepository.findById(alunoId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado."));
        aulaRepository.findById(aulaId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Aula não encontrada."));

        var aula = duvidaAuthorization.getAula(aulaId);

        duvidaAuthorization.getTrilha(aula.getTrilhaId());

        Duvida d = new Duvida();
        d.setAlunoId(alunoId);
        d.setAulaId(aulaId);
        d.setTrilhaId(aula.getTrilhaId());
        d.setMensagem(msg);
        return toDTO(duvidaRepository.save(d));
    }

    public DuvidaDTO responder(Long id, String resposta) {
        Duvida d = duvidaRepository.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Dúvida não encontrada."));
        duvidaAuthorization.requireCanRespond(d);
        if (resposta == null || resposta.trim().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resposta obrigatória.");
        d.setResposta(resposta.trim());
        d.setStatus("RESPONDIDA");
        d.setRespondidaEm(LocalDateTime.now());
        return toDTO(duvidaRepository.save(d));
    }

    public DuvidaDTO resolver(Long id) {
        Duvida d = duvidaRepository.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Dúvida não encontrada."));
        duvidaAuthorization.requireCanRespond(d);
        d.setStatus("RESPONDIDA");
        if (d.getRespondidaEm() == null) d.setRespondidaEm(LocalDateTime.now());
        return toDTO(duvidaRepository.save(d));
    }

    public List<DuvidaDTO> listarPorTrilha(Long trilhaId) {
        duvidaAuthorization.requireCanManageTrilha(trilhaId);
        return duvidaRepository.findByTrilhaIdOrderByCriadaEmDesc(trilhaId)
            .stream().map(this::toDTO).toList();
    }

    public List<DuvidaDTO> listarPorAula(Long aulaId) {
        duvidaAuthorization.requireCanManageAula(aulaId);
        return duvidaRepository.findByAulaIdOrderByCriadaEmDesc(aulaId)
            .stream().map(this::toDTO).toList();
    }

    public List<DuvidaDTO> listarPorAlunoEAula(Long alunoId, Long aulaId) {
        duvidaAuthorization.requireCanAccessAlunoAula(alunoId, aulaId);
        return duvidaRepository.findByAlunoIdAndAulaIdOrderByCriadaEmDesc(alunoId, aulaId)
            .stream().map(this::toDTO).toList();
    }

    public List<DuvidaDTO> listarPorAluno(Long alunoId) {
        duvidaAuthorization.requireCanAccessAluno(alunoId);
        return duvidaRepository.findByAlunoIdOrderByCriadaEmDesc(alunoId)
            .stream().map(this::toDTO).toList();
    }
}
