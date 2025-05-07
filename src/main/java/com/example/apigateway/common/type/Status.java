package com.example.apigateway.common.type;

import com.fasterxml.jackson.annotation.JsonValue;
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

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Status fromString(String status) {
        for (Status s : Status.values()) {
            if (s.getValue().equalsIgnoreCase(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("No enum constant " + Status.class.getCanonicalName() + "." + status);
    }
}
