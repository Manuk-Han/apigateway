package com.example.apigateway.common.endpoint.common;


import com.example.apigateway.common.type.Role;

public interface EndPoint {
    String getPath();

    Role getRole();
}
