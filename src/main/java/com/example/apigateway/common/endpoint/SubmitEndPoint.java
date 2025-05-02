package com.example.apigateway.common.endpoint;

import com.example.apigateway.common.endpoint.common.EndPoint;
import com.example.apigateway.common.type.Role;
import lombok.Getter;

@Getter
public enum SubmitEndPoint implements EndPoint {
    SUBMIT_CODE(""),
//    RECEIVE_RESULT("/receive")
    ;

    private final String path;
    private final Role role;

    private static final String PREFIX = "/submit";

    SubmitEndPoint(String path) {
        this(path, Role.USER);
    }

    SubmitEndPoint(String path, Role role) {
        this.path = PREFIX + path;
        this.role = role;
    }
}
