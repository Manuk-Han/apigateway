package com.example.apigateway.controller;

import com.example.apigateway.common.jwt.JwtTokenDto;
import com.example.apigateway.form.SignInForn;
import com.example.apigateway.form.SignUpForm;
import com.example.apigateway.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Profile("default")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-up")
    public void signUp(SignUpForm signUpForm) {
        authService.signUp(signUpForm);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(SignInForn signInForm) {
        JwtTokenDto jwtTokenDto = authService.signIn(signInForm);

        return ResponseEntity.ok()
                .header("Authorization", jwtTokenDto.getAccessToken())
                .header("RefreshToken", jwtTokenDto.getRefreshToken())
                .build();
    }

    @GetMapping("/sign-out")
    public void signOut() {
        // sign out
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("RefreshToken") String refreshToken) {
        String accessToken = authService.refreshAccessToken(refreshToken);

        return ResponseEntity.ok()
                .header("Authorization", accessToken)
                .build();
    }
}
