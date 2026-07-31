package com.drakalabs.schoolmngsys.auth.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS for the two Vite SPAs (staff-console, guardian-portal), which run on their own dev-server
 * origins — see {@link CorsProperties} for the allowlist/localhost-pattern split. Applied in
 * {@link SecurityConfig} via {@code .cors(Customizer.withDefaults())}.
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> originPatterns = new ArrayList<>(properties.allowedOrigins());
        if (properties.allowAnyLocalhostPort()) {
            originPatterns.add("http://localhost:*");
            originPatterns.add("http://127.0.0.1:*");
        }
        configuration.setAllowedOriginPatterns(originPatterns);

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Idempotency-Key"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
