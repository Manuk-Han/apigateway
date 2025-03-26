package com.example.apigateway.service;

import com.example.apigateway.common.jwt.JwtTokenProvider;
import com.example.apigateway.repository.CourseRepository;
import com.example.apigateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("8081")
@Service
@RequiredArgsConstructor
public class CourseService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    private final JwtTokenProvider jwtTokenProvider;

    public Long createCourse() {
        return 0L;
    }
}
