package com.example.apigateway.form.auth;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class SignInForm {
    private String id;

    private String password;
}
