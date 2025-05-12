package com.example.apigateway.controller;

import com.example.apigateway.common.jwt.JwtTokenDto;
import com.example.apigateway.form.auth.*;
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
    public ResponseEntity<?> signIn(SignInForm signInForm) {
        JwtTokenDto jwtTokenDto = authService.signIn(signInForm);

        return ResponseEntity.ok()
                .header("Authorization", jwtTokenDto.getAccessToken())
                .header("RefreshToken", jwtTokenDto.getRefreshToken())
                .build();
    }

    @GetMapping("/sign-out")
    public void signOut(@RequestHeader("Authorization") String accessToken) {
        authService.signOut(accessToken);
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("RefreshToken") String refreshToken) {
        String accessToken = authService.refreshAccessToken(refreshToken);

        return ResponseEntity.ok()
                .header("Authorization", accessToken)
                .build();
    }

    @PostMapping("/update-password")
    public void updatePassword(@RequestHeader("Authorization") String accessToken, UpdatePasswordForm updatePasswordForm) {
        authService.updatePassword(accessToken, updatePasswordForm);
    }

    @GetMapping("/reset-password")
    public ResponseEntity<?> resetPassword(String accountId, String email) {
        authService.resetPassword(accountId, email);

        return ResponseEntity.status(200)
                .body("Reset password email sent successfully");
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestHeader("Authorization") String accessToken, WithdrawForm withdrawForm) {
        String accountId = authService.withdraw(accessToken, withdrawForm);

        return ResponseEntity.ok(accountId);
    }

    @PostMapping("/cancel-withdraw")
    public ResponseEntity<?> cancelWithdraw(CancelWithdrawForm cancelWithdrawForm) {
        authService.cancelWithdraw(cancelWithdrawForm);

        return ResponseEntity.status(200)
                .body("Cancel withdraw email sent successfully");
    }

    @PostMapping("/check/cancel-withdraw")
    public ResponseEntity<String> checkCancelWithdraw(String accountId, String code) {
        String newPassword = authService.checkCancelWithdraw(accountId, code);

        return ResponseEntity.ok(newPassword);
    }
}
