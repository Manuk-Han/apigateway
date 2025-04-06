package com.example.apigateway.service;

import com.example.apigateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Profile("8081")
@Service
@RequiredArgsConstructor
public class ProblemService {
    private final UserRepository userRepository;


}
