package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.dto.AtualizarPerfilDTO;
import com.itb.inf3em.studyconnect.model.entity.TipoUsuario;
import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.repository.TrilhaRepository;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import com.itb.inf3em.studyconnect.model.services.CredentialValidationService;
import com.itb.inf3em.studyconnect.model.services.EmailVerificationService;
import com.itb.inf3em.studyconnect.model.services.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioUpdateSecurityTest {

    private UsuarioRepository usuarioRepository;
    private UsuarioService usuarioService;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder   = new BCryptPasswordEncoder();

        usuarioService = new UsuarioService();
        ReflectionTestUtils.setField(usuarioService, "usuarioRepository",    usuarioRepository);
        ReflectionTestUtils.setField(usuarioService, "trilhaRepository",     mock(TrilhaRepository.class));
        ReflectionTestUtils.setField(usuarioService, "passwordEncoder",      passwordEncoder);
        ReflectionTestUtils.setField(usuarioService, "credentialValidationService", mock(CredentialValidationService.class));
        ReflectionTestUtils.setField(usuarioService, "emailVerificationService",    mock(EmailVerificationService.class));

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
    }

    // ── 1. nome é editável ───────────────────────────────────────────────────

    @Test
    void updateOwn_nomeEhEditavel() {
        Usuario u = usuario(10L, "original@x.com", "NomeAntigo", "hash", TipoUsuario.ALUNO, true);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(u));

        AtualizarPerfilDTO dto = dto("NovoNome", null);
        Usuario resultado = usuarioService.updateOwn(10L, dto);

        assertThat(resultado.getNome()).isEqualTo("NovoNome");
    }

    // ── 2. fotoUrl é editável ────────────────────────────────────────────────

    @Test
    void updateOwn_fotoUrlEhEditavel() {
        Usuario u = usuario(10L, "original@x.com", "Nome", "hash", TipoUsuario.ALUNO, true);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(u));

        AtualizarPerfilDTO dto = dto(null, "https://foto.nova/img.png");
        Usuario resultado = usuarioService.updateOwn(10L, dto);

        assertThat(resultado.getFotoUrl()).isEqualTo("https://foto.nova/img.png");
    }

    // ── 3. email NÃO pode ser alterado via updateOwn ─────────────────────────

    @Test
    void updateOwn_emailNaoEhAlterado() {
        Usuario u = usuario(10L, "original@x.com", "Nome", "hash", TipoUsuario.ALUNO, true);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(u));

        // AtualizarPerfilDTO não tem campo email — o service nunca o toca
        AtualizarPerfilDTO dto = dto("NovoNome", null);
        Usuario resultado = usuarioService.updateOwn(10L, dto);

        assertThat(resultado.getEmail()).isEqualTo("original@x.com");
    }

    // ── 4. senha NÃO pode ser alterada via updateOwn ─────────────────────────

    @Test
    void updateOwn_senhaNaoEhAlterada() {
        String hashOriginal = passwordEncoder.encode("SenhaOriginal1!");
        Usuario u = usuario(10L, "u@x.com", "Nome", hashOriginal, TipoUsuario.ALUNO, true);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(u));

        AtualizarPerfilDTO dto = dto("NovoNome", null);
        Usuario resultado = usuarioService.updateOwn(10L, dto);

        // hash deve ser exatamente o mesmo — nenhuma nova codificação ocorreu
        assertThat(resultado.getSenha()).isEqualTo(hashOriginal);
    }

    // ── 5. tipoUsuario NÃO pode ser alterado via updateOwn ───────────────────

    @Test
    void updateOwn_tipoUsuarioNaoEhAlterado() {
        Usuario u = usuario(10L, "u@x.com", "Nome", "hash", TipoUsuario.ALUNO, true);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(u));

        AtualizarPerfilDTO dto = dto("NovoNome", null);
        Usuario resultado = usuarioService.updateOwn(10L, dto);

        assertThat(resultado.getTipoUsuario()).isEqualTo(TipoUsuario.ALUNO);
    }

    // ── 6. ativo NÃO pode ser alterado via updateOwn ─────────────────────────

    @Test
    void updateOwn_ativoNaoEhAlterado() {
        Usuario u = usuario(10L, "u@x.com", "Nome", "hash", TipoUsuario.ALUNO, true);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(u));

        AtualizarPerfilDTO dto = dto("NovoNome", null);
        Usuario resultado = usuarioService.updateOwn(10L, dto);

        assertThat(resultado.isAtivo()).isTrue();
    }

    // ── 7. identidade vem do path/JWT — não do DTO ───────────────────────────

    @Test
    void updateOwn_identidadeVemDoPathNaoDoDTO() {
        // O DTO não tem campo usuarioId — o service usa o id do path
        Usuario u = usuario(10L, "u@x.com", "Nome", "hash", TipoUsuario.ALUNO, true);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(u));

        AtualizarPerfilDTO dto = dto("NovoNome", null);
        usuarioService.updateOwn(10L, dto);

        // Deve ter buscado pelo id correto (10L), não por qualquer outro
        verify(usuarioRepository).findById(10L);
    }

    // ── 8. troca de e-mail continua funcionando pelo EmailChangeService ───────
    //    (verificado estruturalmente: updateOwn não tem campo email no DTO)

    @Test
    void atualizarPerfilDTO_naoTemCampoEmail() throws Exception {
        // AtualizarPerfilDTO não deve expor setter/getter de email
        var fields = java.util.Arrays.stream(AtualizarPerfilDTO.class.getDeclaredFields())
                .map(f -> f.getName())
                .toList();
        assertThat(fields).doesNotContain("email");
        assertThat(fields).doesNotContain("senha");
        assertThat(fields).doesNotContain("tipoUsuario");
        assertThat(fields).doesNotContain("ativo");
    }

    // ── 9. autenticação usa o usuário correto do JWT ──────────────────────────

    @Test
    void updateOwn_autenticacaoUsaUsuarioCorretoDoJwt() {
        // Simula SecurityContext com usuário 10L
        AuthenticatedUser user = new AuthenticatedUser(10L, "u@x.com", TipoUsuario.ALUNO);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null,
                        List.of(new SimpleGrantedAuthority("ROLE_ALUNO"))));

        CurrentUser currentUser = new CurrentUser();
        AuthenticatedUser resolved = currentUser.require();

        assertThat(resolved.usuarioId()).isEqualTo(10L);
        assertThat(resolved.tipoUsuario()).isEqualTo(TipoUsuario.ALUNO);

        SecurityContextHolder.clearContext();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Usuario usuario(Long id, String email, String nome, String senha,
                            TipoUsuario tipo, boolean ativo) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setEmail(email);
        u.setNome(nome);
        u.setSenha(senha);
        u.setTipoUsuario(tipo);
        u.setAtivo(ativo);
        return u;
    }

    private AtualizarPerfilDTO dto(String nome, String fotoUrl) {
        AtualizarPerfilDTO d = new AtualizarPerfilDTO();
        ReflectionTestUtils.setField(d, "nome",    nome);
        ReflectionTestUtils.setField(d, "fotoUrl", fotoUrl);
        return d;
    }
}
