package com.example.apigateway.config;

import com.example.apigateway.common.jwt.JwtAuthenticationWebFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;

import java.util.Collections;

@Profile({"class", "submit"})
@Configuration
@RequiredArgsConstructor
public class ApiSecurityConfig {

    private final WebFilter ipAddressFilter;
    private final JwtAuthenticationWebFilter jwtAuthenticationWebFilter;
    private final ApiKeyFilter apiKeyFilter;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .addFilterAt(ipAddressFilter, SecurityWebFiltersOrder.FIRST)
                .addFilterAt(corsFilter(), SecurityWebFiltersOrder.CORS)
                .addFilterBefore(apiKeyFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterBefore(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)

                .authorizeExchange(auth -> auth
                        .pathMatchers("/**").permitAll()
                        .anyExchange().permitAll()
                )
                .build();
    }

    @Bean
    public WebFilter corsFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Collections.singletonList("*"));
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*");
        corsConfig.addExposedHeader(HttpHeaders.AUTHORIZATION);
        corsConfig.addExposedHeader(HttpHeaders.SET_COOKIE);
        corsConfig.addExposedHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
        corsConfig.addExposedHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
        corsConfig.addExposedHeader("RefreshToken");
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
