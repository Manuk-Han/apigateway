package com.example.apigateway.form.auth;

import com.example.apigateway.common.type.Role;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class SignUpForm {
    private String name;

    private String accountId;

    private String password;

    private String email;

    private Role role;
}
