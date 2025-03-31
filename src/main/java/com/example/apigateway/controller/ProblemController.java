package com.example.apigateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile({"8081"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/problem")
public class ProblemController {
    @Value("${server.name}")
    private String name;

    @GetMapping("/detail/{problemId}")
    public String test1(@PathVariable Long problemId) {
        return name + " " + problemId;
    }

    @GetMapping("/detail/{problemId}")
    public String test2(@PathVariable Long problemId) {
        return name + " " + problemId;
    }
}