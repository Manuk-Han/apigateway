package com.example.apigateway.common.endpoint;

import com.example.apigateway.common.endpoint.common.EndPoint;
import com.example.apigateway.common.type.Role;
import lombok.Getter;

@Getter
public enum AuthEndPoint implements EndPoint {
    SIGN_UP("/sign-up"),
    SIGN_IN("/sign-in"),
    REFRESH("/refresh"),
    UPDATE_PASSWORD("/update-password"),
    WITHDRAWAL("/withdraw", Role.USER),
    CANCEL_WITHDRAWAL("/cancel-withdraw", Role.USER),
    CHECK_WITHDRAWAL("/check/cancel-withdraw", Role.ADMIN),
    SIGN_OUT("/sign-out", Role.USER);

    private final String path;
    private final Role role;

    private static final String PREFIX = "/auth";

    AuthEndPoint(String path) {
        this(path, null);
    }

    AuthEndPoint(String path, Role role) {
        this.path = PREFIX + path;
        this.role = role;
    }
}
