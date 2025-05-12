package com.example.apigateway.common.type;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    PASS("PASS", 1),
    REJECT("REJECTED", 2),
    CORRECT("CORRECT", 3),
    WRONG("WRONG", 4),
    ERROR("ERROR", 5),
    NOT_SUBMITTED("NOT_SUBMITTED", 6);

    private final String value;
    private final int priority;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Status getByPriority(Integer priority) {
        if (priority == null) {
            return Status.NOT_SUBMITTED;
        }

        for (Status status : Status.values()) {
            if (status.getPriority() == priority) {
                return status;
            }
        }

        return Status.NOT_SUBMITTED;
    }

}

