package com.itb.inf3em.studyconnect.security;

import com.itb.inf3em.studyconnect.model.entity.Usuario;
import com.itb.inf3em.studyconnect.model.repository.UsuarioRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. Valida assinatura, expiração e claims obrigatórios
            AuthenticatedUser fromJwt = jwtService.parseToken(header.substring(7));

            // 2. Consulta o banco — fonte de verdade para existência, ativo e tipoUsuario
            Optional<Usuario> opt = usuarioRepository.findById(fromJwt.usuarioId());
            if (opt.isEmpty() || !opt.get().isAtivo()) {
                // Usuário inexistente ou desativado: não autentica, segue sem contexto
                filterChain.doFilter(request, response);
                return;
            }

            // 3. Constrói AuthenticatedUser com dados ATUAIS do banco
            Usuario usuario = opt.get();
            AuthenticatedUser user = new AuthenticatedUser(
                    usuario.getId(),
                    usuario.getEmail(),
                    usuario.getTipoUsuario()
            );

            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.tipoUsuario().name()))
                    )
            );
        } catch (JwtException | IllegalArgumentException exception) {
            // Token inválido, expirado ou adulterado: segue sem contexto → 401
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
