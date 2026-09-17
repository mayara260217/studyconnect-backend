package com.itb.inf3em.studyconnect.config;

import com.itb.inf3em.studyconnect.security.JwtAuthenticationFilter;
import com.itb.inf3em.studyconnect.security.RateLimitAuthFilter;
import com.itb.inf3em.studyconnect.security.RateLimitPublicFilter;
import com.itb.inf3em.studyconnect.security.RateLimiter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   RateLimiter rateLimiter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Autenticacao obrigatoria."))
                        .accessDeniedHandler((request, response, exception) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.")))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/health").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/login",
                                "/api/v1/auth/google",
                                "/api/v1/auth/verify-email",
                                "/api/v1/auth/resend-verification",
                                "/api/v1/auth/forgot-password",
                                "/api/v1/auth/reset-password",
                                "/api/v1/usuarios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/email-change/confirm").permitAll()
                        .requestMatchers("/api/v1/admin/**", "/api/email/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/tickets").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/tickets/*/responder",
                                "/api/v1/tickets/*/fechar").hasRole("ADMIN")
                        .anyRequest().authenticated())
                // Pipeline: RateLimitPublicFilter → JwtAuthenticationFilter → RateLimitAuthFilter
                //
                // Âncoras usadas (filtros padrão com ordem registrada no Spring Security):
                //   BasicAuthenticationFilter  — posição anterior ao UsernamePasswordAuthenticationFilter
                //   UsernamePasswordAuthenticationFilter — posição de referência central
                //
                // Resultado final na cadeia:
                //   … → RateLimitPublicFilter → JwtAuthenticationFilter
                //       → UsernamePasswordAuthenticationFilter (nunca executa, STATELESS)
                //       → RateLimitAuthFilter → …
                .addFilterBefore(new RateLimitPublicFilter(rateLimiter), BasicAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(new RateLimitAuthFilter(rateLimiter), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
