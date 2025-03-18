package com.example.apigateway.config.endpoint;


import com.example.apigateway.common.Role;

public interface EndPoint {
    String getPath();

    Role getRole();
}
