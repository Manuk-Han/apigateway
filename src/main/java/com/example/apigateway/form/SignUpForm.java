package com.example.apigateway.form;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class SignUpForm {
    private String id;

    private String password;

    private String email;
}
