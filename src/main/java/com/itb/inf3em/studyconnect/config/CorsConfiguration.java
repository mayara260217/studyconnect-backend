package com.itb.inf3em.studyconnect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Arrays;

@Configuration
public class CorsConfiguration {

    // Lista de origens fixas usada em produção (ex: Vercel)
    private final List<String> allowedOrigins;

    // Flag que ativa o modo "aceitar qualquer origem" — usar apenas em dev local
    private final boolean allowAll;

    public CorsConfiguration(
            @Value("${app.cors.allowed-origins:https://plutcc.vercel.app,http://localhost:5173,http://127.0.0.1:5173}") String allowedOrigins,
            @Value("${app.cors.allow-all:false}") boolean allowAll) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        this.allowAll = allowAll;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();

        if (allowAll) {
            // Modo dev: aceita qualquer origem (qualquer IP, qualquer porta)
            // setAllowedOriginPatterns("*") é necessário porque setAllowedOrigins("*")
            // é incompatível com allowCredentials=true no Spring Security
            configuration.setAllowedOriginPatterns(List.of("*"));
        } else {
            // Modo produção: apenas as origens explicitamente listadas
            configuration.setAllowedOrigins(allowedOrigins);
        }

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
