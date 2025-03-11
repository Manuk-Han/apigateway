package com.example.apigateway;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile({"8081", "8082"})
@RequiredArgsConstructor
@RestController
public class TestController {
    @Value("${server.name}")
    private String name;

    @GetMapping("/api1/test")
    public String test1() {
        return name;
    }

    @GetMapping("/api2/test")
    public String test2() {
        return name;
    }
}