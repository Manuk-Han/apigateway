package com.example.apigateway.config.endpoint;

import com.example.apigateway.common.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.config.web.server.ServerHttpSecurity;

@Getter
@AllArgsConstructor
public enum AuthEndPoint {
    SIGN_UP("/auth/sign-up", null),
    SIGN_IN("/auth/sign-in", null),

    SIGN_OUT("/auth/sign-out", Role.GUEST);

    private final String path;
    private final Role role;

    private ServerHttpSecurity.AuthorizeExchangeSpec applySingleAuth(ServerHttpSecurity.AuthorizeExchangeSpec auth, AuthEndPoint endPoint) {
        if(endPoint.getRole() == null) return auth.pathMatchers(endPoint.getPath()).permitAll();
        else return auth.pathMatchers(endPoint.getPath()).hasRole(endPoint.getRole().getRoleName());
    }

    public static ServerHttpSecurity.AuthorizeExchangeSpec applyAllAuth(ServerHttpSecurity.AuthorizeExchangeSpec auth) {
        for (AuthEndPoint endPoint : values()) {
            auth = endPoint.applySingleAuth(auth, endPoint);
        }

        return auth;
    }
}
