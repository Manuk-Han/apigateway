package com.example.apigateway.common.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InviteType {
    ONE("one"),
    FILE("file"),;

    private final String inviteType;
}
