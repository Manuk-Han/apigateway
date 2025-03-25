package com.example.apigateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Profile({"8081"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/course")
public class CourseController {
    @PostMapping("/create")
    public ResponseEntity<Long> create() {
        return new ResponseEntity<>(1L, HttpStatusCode.valueOf(200));
    }
}