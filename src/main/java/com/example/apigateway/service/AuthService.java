package com.example.apigateway.service;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import com.example.apigateway.common.jwt.JwtTokenDto;
import com.example.apigateway.common.jwt.JwtTokenProvider;
import com.example.apigateway.entity.User;
import com.example.apigateway.form.SignInForn;
import com.example.apigateway.form.SignUpForm;
import com.example.apigateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Profile("default")
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    public void signUp(SignUpForm signUpForm) {
        userRepository.save(User.builder()
                .accountId(signUpForm.getId())
                .password(bCryptPasswordEncoder.encode(signUpForm.getPassword()))
                .nickname(signUpForm.getNickname())
                .build());
    }

    public JwtTokenDto signIn(SignInForn signInForm) {
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
}
