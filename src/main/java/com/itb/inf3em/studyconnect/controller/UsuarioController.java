package com.itb.inf3em.studyconnect.controller;

import com.itb.inf3em.studyconnect.model.dto.AtualizarPerfilDTO;
import com.itb.inf3em.studyconnect.model.dto.UsuarioDTO;
import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.services.UsuarioService;
import com.itb.inf3em.studyconnect.security.AuthenticatedUser;
import com.itb.inf3em.studyconnect.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CurrentUser currentUser;

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> findAll() {
        List<UsuarioDTO> dtos = usuarioService.findAll().stream()
                .map(UsuarioDTO::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> findById(@PathVariable Long id) {
        currentUser.requireSameUserOrAdmin(id);
        Usuario usuario = usuarioService.findById(id);
        return ResponseEntity.ok(new UsuarioDTO(usuario));
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> cadastrar(@RequestBody Usuario usuario) {
        Usuario novoUsuario = usuarioService.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UsuarioDTO(novoUsuario));
    }

    /**
     * PUT /api/v1/usuarios/{id}
     * Aceita apenas AtualizarPerfilDTO (nome, fotoUrl).
     * email, senha, tipoUsuario e ativo são imutáveis por este endpoint.
     * ADMIN atualizando outra conta usa updateStatusAsAdmin (ativo).
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> atualizar(@PathVariable Long id,
                                                @RequestBody AtualizarPerfilDTO dto) {
        AuthenticatedUser authenticatedUser = currentUser.requireSameUserOrAdmin(id);
        Usuario atualizado = authenticatedUser.tipoUsuario() == TipoUsuario.ADMIN
                && !authenticatedUser.usuarioId().equals(id)
                ? usuarioService.updateStatusAsAdmin(id, true)
                : usuarioService.updateOwn(id, dto);
        return ResponseEntity.ok(new UsuarioDTO(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletar(@PathVariable Long id) {
        currentUser.requireSameUserOrAdmin(id);
        usuarioService.delete(id);
        return ResponseEntity.ok("Usuário excluído com sucesso!");
    }
}
