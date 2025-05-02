package com.example.apigateway.config;

import com.example.apigateway.common.jwt.JwtAuthenticationWebFilter;
import com.example.apigateway.common.endpoint.AuthEndPoint;
import com.example.apigateway.common.endpoint.common.EndPoint;
import lombok.RequiredArgsConstructor;
import org.reflections.Reflections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

@Profile("default")
@Configuration
@RequiredArgsConstructor
public class GatewaySecurityConfig {

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
                .addFilterAt(corsFilter(), SecurityWebFiltersOrder.CORS)
                .addFilterBefore(apiKeyFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterBefore(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)

                .authorizeExchange(auth -> {
                            auth.pathMatchers("/webjars/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll();

                            applyAllAuth(auth);

                            auth.anyExchange().authenticated();
                        }
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



    private static ServerHttpSecurity.AuthorizeExchangeSpec applySingleAuth(ServerHttpSecurity.AuthorizeExchangeSpec auth, EndPoint endPoint) {
        if (endPoint.getRole() == null) return auth.pathMatchers(endPoint.getPath()).permitAll();
        else return auth.pathMatchers(endPoint.getPath()).hasRole(endPoint.getRole().name());
    }

    public static ServerHttpSecurity.AuthorizeExchangeSpec applyAllAuth(ServerHttpSecurity.AuthorizeExchangeSpec auth) {
        Reflections reflections = new Reflections("com.example.apigateway.common.endpoint");
        Set<Class<? extends EndPoint>> endpointEnums = reflections.getSubTypesOf(EndPoint.class);

        EndPoint[] endPoints = endpointEnums.stream()
                .filter(Class::isEnum)
                .flatMap(cls -> Arrays.stream(cls.getEnumConstants()))
                .toArray(EndPoint[]::new);

        for (EndPoint endPoint : endPoints)
            auth = applySingleAuth(auth, endPoint);

        return auth;
    }
}
