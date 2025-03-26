package com.example.apigateway.form.auth;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class SignUpForm {
    private String name;

    private String accountId;

    private String password;

    private String email;
}
