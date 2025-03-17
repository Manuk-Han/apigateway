package com.example.apigateway.service;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import com.example.apigateway.entity.User;
import com.example.apigateway.form.SignInForn;
import com.example.apigateway.form.SignUpForm;
import com.example.apigateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public void signUp(SignUpForm signUpForm) {
        userRepository.save(User.builder()
                .accountId(signUpForm.getId())
                .password(bCryptPasswordEncoder.encode(signUpForm.getPassword()))
                .nickname(signUpForm.getNickname())
                .build());
    }

    public void signIn(SignInForn signInForm) {
        User user = userRepository.findByAccountId(signInForm.getId())
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        if (!bCryptPasswordEncoder.matches(signInForm.getPassword(), user.getPassword()))
            throw new CustomException(CustomResponseException.WRONG_PASSWORD);

    }
}
