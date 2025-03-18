package com.example.apigateway.config.endpoint;

import com.example.apigateway.common.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.config.web.server.ServerHttpSecurity;

import java.util.List;

@Getter
@AllArgsConstructor
public enum AuthEndPoint implements EndPoint {
    SIGN_UP("/auth/sign-up"),
    SIGN_IN("/auth/sign-in"),
    SIGN_OUT("/auth/sign-out", Role.GUEST);

    private final String path;
    private final Role role;

    AuthEndPoint(String path) {
        this(path, null);
    }
}
