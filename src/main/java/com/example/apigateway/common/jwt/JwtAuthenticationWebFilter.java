package com.example.apigateway.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationWebFilter implements WebFilter {

    @Value("${jwt.security.key}")
    private String secretKey;

    private final JwtTokenProvider tokenProvider;

    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String HEADER_REFRESH_TOKEN = "Refresh-Token";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String token = resolveToken(request);

        if (token != null) {
            if (tokenProvider.validateAccessToken(token)) {
                Authentication authentication = tokenProvider.getAuthentication(token);
                SecurityContext securityContext = new SecurityContextImpl(authentication);

                Long userId = tokenProvider.getUserId(token);

                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-USER-ID", String.valueOf(userId))
                        .build();

                ServerWebExchange mutatedExchange = exchange.mutate()
                        .request(mutatedRequest)
                        .build();

                return chain.filter(mutatedExchange)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
            }
        }

        return chain.filter(exchange);
    }

    private String resolveToken(ServerHttpRequest request) {
        String accessToken = removeBearerPrefix(request.getHeaders().getFirst(HEADER_AUTHORIZATION));
        if (accessToken != null) {
            return accessToken;
        }

        return removeBearerPrefix(request.getHeaders().getFirst(HEADER_REFRESH_TOKEN));
    }

    private String getAccountTypeFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("role", String.class);
    }

    private String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("user_id", String.class);
    }

    private SecretKey jwtSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String removeBearerPrefix(String token) {
        if (token == null)
            return null;

        return token.replace("Bearer ", "").trim();
    }

}
