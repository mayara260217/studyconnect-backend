package com.itb.inf3em.studyconnect.controller;

import com.itb.inf3em.studyconnect.model.repository.MatriculaTrilhaRepository;
import com.itb.inf3em.studyconnect.model.repository.TrilhaRepository;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TrilhaRepository  trilhaRepository;
    @Autowired private MatriculaTrilhaRepository matriculaRepository;

    /**
     * GET /api/v1/admin/resumo
     *
     * Retorna em UMA request tudo que o dashboard admin precisa:
     *   - contagens de usuários por tipo
     *   - total de matrículas
     *   - últimos 6 usuários cadastrados
     *   - trilhas com totalAlunos já embutido (sem N+1)
     */
    @GetMapping("/resumo")
    public ResponseEntity<Map<String, Object>> resumo() {

        // ── 1. Contagens de usuários por tipo (1 query) ──────────────
        Map<String, Long> contagemPorTipo = new HashMap<>();
        for (Object[] row : usuarioRepository.countByTipoUsuario()) {
            // row[0] é o enum TipoUsuario
            contagemPorTipo.put(row[0].toString(), (Long) row[1]);
        }
        long totalUsuarios    = contagemPorTipo.values().stream().mapToLong(Long::longValue).sum();
        long totalAlunos      = contagemPorTipo.getOrDefault("ALUNO",     0L);
        long totalProfessores = contagemPorTipo.getOrDefault("PROFESSOR", 0L);

        // ── 2. Total de matrículas ativas (1 query) ──────────────────
        long totalMatriculas = matriculaRepository.countByAtivoTrue();

        // ── 3. Últimos 6 usuários (1 query) ──────────────────────────
        List<Map<String, Object>> recentUsers = usuarioRepository.findTop6ByOrderByIdDesc()
            .stream()
            .map(u -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id",          u.getId());
                m.put("nome",        u.getNome());
                m.put("email",       u.getEmail());
                m.put("tipoUsuario", u.getTipoUsuario().name());
                m.put("ativo",       u.isAtivo());
                return m;
            })
            .toList();

        // ── 4. Trilhas + totalAlunos agregado (2 queries no total) ───
        // 4a. Busca contagens em uma só query GROUP BY
        Map<Long, Long> alunosPorTrilha = matriculaRepository.countAlunosPorTrilha()
            .stream()
            .collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> (Long) row[1]
            ));

        // 4b. Busca todas as trilhas (1 query)
        List<Map<String, Object>> trilhas = trilhaRepository.findAll()
            .stream()
            .map(t -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id",            t.getId());
                m.put("nome",          t.getNome());
                m.put("descricao",     t.getDescricao());
                m.put("nivel",         t.getNivel());
                m.put("disciplina",    t.getDisciplina());
                m.put("professorId",   t.getProfessorId());
                m.put("professorNome", t.getProfessorNome());
                m.put("totalAlunos",   alunosPorTrilha.getOrDefault(t.getId(), 0L));
                return m;
            })
            .toList();

        // ── Monta resposta ────────────────────────────────────────────
        Map<String, Object> resp = new HashMap<>();
        resp.put("totalUsuarios",    totalUsuarios);
        resp.put("totalAlunos",      totalAlunos);
        resp.put("totalProfessores", totalProfessores);
        resp.put("totalTrilhas",     (long) trilhas.size());
        resp.put("totalMatriculas",  totalMatriculas);
        resp.put("recentUsers",      recentUsers);
        resp.put("trilhas",          trilhas);

        return ResponseEntity.ok(resp);
    }
}
