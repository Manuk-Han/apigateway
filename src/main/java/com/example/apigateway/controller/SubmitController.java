package com.example.apigateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile({"8082"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/submit")
public class SubmitController {
    @Value("${server.name}")
    private String name;

    @GetMapping("/api1/detail/{problemId}")
    public String test1(@PathVariable Long problemId) {
        return name + " " + problemId;
    }

    @GetMapping("/api2/detail/{problemId}")
    public String test2(@PathVariable Long problemId) {
        return name + " " + problemId;
    }
}