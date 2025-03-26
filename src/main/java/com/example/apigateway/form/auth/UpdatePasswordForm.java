package com.example.apigateway.form.auth;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class UpdatePasswordForm {
    private String currentPassword;

    private String newPassword;
}
