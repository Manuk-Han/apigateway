package com.example.apigateway.common.endpoint;

import com.example.apigateway.common.Role;
import lombok.Getter;

@Getter
public enum ProblemEndPoint implements EndPoint {
    CREATE("/create", Role.MANAGER),
    UPDATE("/update/**", Role.MANAGER),
    DELETE("/delete/**", Role.MANAGER),
    MANAGER_PROBLEM_LIST("/admin/all/**", Role.MANAGER),
    MANAGER_PROBLEM_DETAIL("/admin/**", Role.MANAGER),
    REJUDGE("/rejudge/**", Role.MANAGER),
    PROBLEM_LIST("/all/**"),
    PROBLEM_DETAIL("/**");

    private final String path;
    private final Role role;

    private static final String PREFIX = "/problem";

    ProblemEndPoint(String path) {
        this(PREFIX + path, Role.USER);
    }

    ProblemEndPoint(String path, Role role) {
        this.path = PREFIX + path;
        this.role = role;
    }
}
