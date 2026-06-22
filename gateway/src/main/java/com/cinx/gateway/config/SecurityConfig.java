package com.cinx.gateway.config;

import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Value("${jwt.access.secret}")
    private String accessKey;

    @Value("${cors.allowed-origins:http://localhost:*}")
    private List<String> allowedOrigins;

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/internal/**").denyAll()
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
                .pathMatchers(HttpMethod.GET,
                        "/api/v1/courses/mine",
                        "/api/v1/courses/upload",
                        "/api/v1/courses/mine",
                        "/api/v1/policies/versions").authenticated()
                .pathMatchers(HttpMethod.GET,
                        "/api/v1/courses",
                        "/api/v1/courses/{courseId}",
                        "/api/v1/courses/ids",
                        "/api/v1/courses/*/curriculum",
                        "/api/v1/courses/*/lessons/*/articles",
                        "/api/v1/courses/*/lessons/*/videos",
                        "/api/v1/categories/**",
                        "/api/v1/reviews/**",
                        "/api/v1/policies",
                        "/api/v1/policies/*").permitAll()
                .pathMatchers("/api/v1/auth/**",
                        "/api/v1/payments/IPN",
                        "/api/v1/payments/momo-callback",
                        "/api/v1/payments/stripe-webhook",
                        "/ws/**", "/sockjs/**").permitAll()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(reactiveJwtDecoder()))
                .authenticationEntryPoint((exchange, ex) -> GatewayProblemDetailWriter.write(
                         exchange,
                         org.springframework.http.HttpStatus.UNAUTHORIZED,
                         "UNAUTHORIZED",
                         "Unauthorized",
                         "Please login and try again"))
                .accessDeniedHandler((exchange, ex) -> GatewayProblemDetailWriter.write(
                         exchange,
                         org.springframework.http.HttpStatus.FORBIDDEN,
                         "FORBIDDEN",
                         "Forbidden",
                         "Access denied"))
            ).cors(cors -> cors.configurationSource(corsConfigurationSource()));
        return http.build();
    }

    @Bean
    ReactiveJwtDecoder reactiveJwtDecoder() {
        SecretKey secretKey = new SecretKeySpec(accessKey.getBytes(), "HS512");
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v1/**", config);
        return source;
    }
}
