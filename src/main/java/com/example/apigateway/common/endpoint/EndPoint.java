package com.example.apigateway.common.endpoint;


import com.example.apigateway.common.type.Role;

public interface EndPoint {
    String getPath();

    Role getRole();
}
