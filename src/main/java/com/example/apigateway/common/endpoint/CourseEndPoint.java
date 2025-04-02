package com.example.apigateway.common.endpoint;

import com.example.apigateway.common.Role;
import lombok.Getter;

@Getter
public enum CourseEndPoint implements EndPoint {
    LIST("/list", Role.USER),
    OWN_LIST("/own-list"),
    CREATE("/create"),
    UPDATE("/update"),
    INVITE("/invite/**"),
    INVITE_WITH_FILE("/invite-file/**"),
    KICK("/kick/**"),
    GRADE("/grade/**");

    private final String path;
    private final Role role;

    private static final String PREFIX = "/course";

    CourseEndPoint(String path) {
        this(PREFIX + path, Role.MANAGER);
    }

    CourseEndPoint(String path, Role role) {
        this.path = PREFIX + path;
        this.role = role;
    }
}
