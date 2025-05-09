package com.example.apigateway.common.email;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailMessage {
    private String to;

    private String message;
}
