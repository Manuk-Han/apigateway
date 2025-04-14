package com.example.apigateway.common.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TestCaseType {
    IN(".in"),
    OUT(".out");

    private final String fileExtension;
}
