package com.example.apigateway.common.endpoint;

import com.example.apigateway.common.endpoint.common.EndPoint;
import com.example.apigateway.common.type.Role;
import lombok.Getter;

@Getter
public enum ResultEndPoint implements EndPoint {
    RESULT_LIST("/list/**"),
    RESULT_DETAIL("/detail/**"),
    MANAGER_RESULT_LIST("/manager/list/**", Role.MANAGER),
    RESULT_FEEDBACK("/feedback/**", Role.MANAGER);


    private final String path;
    private final Role role;

    private static final String PREFIX = "/result";

    ResultEndPoint(String path) {
        this(PREFIX + path, Role.USER);
    }

    ResultEndPoint(String path, Role role) {
        this.path = PREFIX + path;
        this.role = role;
    }
}
