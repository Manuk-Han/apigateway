package com.example.apigateway.form.auth;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class CancelWithdrawForm {
    private String account;

    private String email;
}
