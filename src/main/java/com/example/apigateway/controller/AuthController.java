package com.example.apigateway.controller;

import com.example.apigateway.form.SignUpForm;
import com.example.apigateway.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-up")
    public void signUp(SignUpForm signUpForm) {
        authService.signUp(signUpForm);
    }
}
