package com.example.apigateway.service;

import com.example.apigateway.common.email.EmailMessage;
import com.example.apigateway.common.email.HtmlEmailService;
import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import com.example.apigateway.common.jwt.JwtTokenDto;
import com.example.apigateway.common.jwt.JwtTokenProvider;
import com.example.apigateway.entity.User;
import com.example.apigateway.entity.Withdraw;
import com.example.apigateway.form.auth.*;
import com.example.apigateway.repository.UserRepository;
import com.example.apigateway.repository.WithdrawRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Profile("default")
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final HtmlEmailService htmlEmailService;
    private final WithdrawRepository withdrawRepository;

    @Value("${server.domain}")
    private String domain;

    public void signUp(SignUpForm signUpForm) {
        userRepository.save(User.builder()
                .name(signUpForm.getName())
                .accountId(signUpForm.getAccountId())
                .password(bCryptPasswordEncoder.encode(signUpForm.getPassword()))
                .email(signUpForm.getEmail())
                .withdraw(false)
                .build());
    }

    public JwtTokenDto signIn(SignInForm signInForm) {
        User user = userRepository.findByAccountId(signInForm.getId())
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        if (!bCryptPasswordEncoder.matches(signInForm.getPassword(), user.getPassword()))
            throw new CustomException(CustomResponseException.WRONG_PASSWORD);

        return JwtTokenDto.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(user.getUserId(), user.getRoles()))
                .refreshToken(jwtTokenProvider.generateRefreshToken(user.getUserId(), user.getRoles()))
                .build();
    }

    public String refreshAccessToken(String refreshToken) {
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.INVALID_TOKEN));

        return jwtTokenProvider.refreshAccessToken(refreshToken);
    }

    public void signOut(String accessToken) {
        jwtTokenProvider.invalidateToken(accessToken);
    }

    public void updatePassword(String accessToken, UpdatePasswordForm updatePasswordForm) {
        Long userId = jwtTokenProvider.getUserId(accessToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.INVALID_TOKEN));

        if (!bCryptPasswordEncoder.matches(updatePasswordForm.getCurrentPassword(), user.getPassword()))
            throw new CustomException(CustomResponseException.WRONG_PASSWORD);

        user.updatePassword(bCryptPasswordEncoder.encode(updatePasswordForm.getNewPassword()));
        userRepository.save(user);
    }

    public String withdraw(String accessToken, WithdrawForm withdrawForm) {
        Long userId = jwtTokenProvider.getUserId(accessToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.INVALID_TOKEN));

        if (!bCryptPasswordEncoder.matches(withdrawForm.getPassword(), user.getPassword()))
            throw new CustomException(CustomResponseException.WRONG_PASSWORD);

        user.updateWithdraw();
        userRepository.save(user);

        jwtTokenProvider.invalidateToken(accessToken);

        return user.getAccountId();
    }

    public void cancelWithdraw(CancelWithdrawForm cancelWithdrawForm) {
        User user = userRepository.findByAccountId(cancelWithdrawForm.getAccount())
                .orElseThrow(() -> new CustomException(CustomResponseException.INVALID_TOKEN));

        if (!Objects.equals(user.getEmail(), cancelWithdrawForm.getEmail()))
            throw new CustomException(CustomResponseException.NOT_REGISTERED_EMAIL);

        String code = UUID.randomUUID().toString();
        String url = domain + "/auth/check/cancel-withdraw?accountId=" + user.getAccountId() + "&code=" + code;

        htmlEmailService.sendEmail(
                EmailMessage.builder()
                        .to(user.getEmail())
                        .message("탈퇴 신청을 취소하려면 아래 링크를 클릭하세요.\n" + url)
                        .build()
        );

        withdrawRepository.save(
                Withdraw.builder()
                        .code(code)
                        .sendTime(LocalDateTime.now())
                        .user(user)
                        .build()
        );
    }

    public String checkCancelWithdraw(String accountId, String code) {
        User user = userRepository.findByAccountId(accountId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Withdraw withdraw = withdrawRepository.findByUserAndCode(user, code)
                .orElseThrow(() -> new CustomException(CustomResponseException.INVALID_CODE));

        if (withdraw.getSendTime().isBefore(LocalDateTime.now().minusMinutes(5)))
            throw new CustomException(CustomResponseException.CERTIFICATION_EXPIRED);

        user.updateWithdraw();

        String newPassword = UUID.randomUUID().toString().substring(0, 8);
        user.updatePassword(bCryptPasswordEncoder.encode(newPassword));

        userRepository.save(user);
        withdrawRepository.delete(withdraw);

        return newPassword;
    }
}
