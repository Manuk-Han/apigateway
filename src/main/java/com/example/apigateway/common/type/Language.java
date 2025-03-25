package com.example.apigateway.common.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Language {
    C("c", ".c"),
    CPP("c++", ".cpp"),
    JAVA("java", ".java"),
    PYTHON("python", ".py");

    private final String languageName;
    private final String fileExtension;
}
