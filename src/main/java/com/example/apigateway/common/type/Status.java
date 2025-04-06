package com.example.apigateway.common.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    CORRECT("CORRECT"),
    WRONG("WRONG"),
    REJECT("REJECTED"),
    PASS("PASS"),
    ERROR("ERROR"),
    NOT_SUBMITTED("NOT_SUBMITTED");

    private final String languageName;
}
