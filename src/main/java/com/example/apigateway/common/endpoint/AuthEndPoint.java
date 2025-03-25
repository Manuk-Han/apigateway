package com.example.apigateway.common.endpoint;

import com.example.apigateway.common.Role;
import lombok.Getter;

@Getter
public enum AuthEndPoint implements EndPoint {
    SIGN_UP("/sign-up"),
    SIGN_IN("/sign-in"),
    REFRESH("/refresh"),
    SIGN_OUT("/sign-out", Role.GUEST);

    private final String path;
    private final Role role;

    private static final String PREFIX = "/auth";

    AuthEndPoint(String path) {
        this(PREFIX + path, null);
    }

    AuthEndPoint(String path, Role role) {
        this.path = PREFIX + path;
        this.role = role;
    }
}
