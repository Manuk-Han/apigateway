package com.example.apigateway.common.type;

import com.example.apigateway.common.exception.CustomResponseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Language {
    C("docker/c", ".c"),
    CPP("c++", ".cpp"),
    JAVA("java", ".java"),
    PYTHON("docker/python", ".py");

    private final String languageName;
    private final String fileExtension;

    public static Language getLanguage(String language) {
        for (Language languageEnum : Language.values()){
            if (languageEnum.languageName.equals(language)){
                return languageEnum;
            }
        }
        throw new IllegalArgumentException(language + CustomResponseException.UNKNOWN_LANGUAGE);
    }
}
