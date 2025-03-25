package com.example.apigateway.common.endpoint;

import com.example.apigateway.common.Role;
import lombok.Getter;

@Getter
public enum ClassEndPoint implements EndPoint {
    CREATE("/create"),
    INVITE("/invite/**"),
    INVITE_WITH_FILE("/invite-file/**"),
    KICK("/kick/**"),
    GRADE("/grade/**");

    private final String path;
    private final Role role;

    private static final String PREFIX = "/class";

    ClassEndPoint(String path) {
        this(PREFIX + path, Role.MANAGER);
    }

    ClassEndPoint(String path, Role role) {
        this.path = PREFIX + path;
        this.role = role;
    }
}
