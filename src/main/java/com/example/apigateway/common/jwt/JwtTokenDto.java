package com.example.apigateway.common.jwt;

import lombok.*;
import org.springframework.boot.actuate.autoconfigure.metrics.export.signalfx.SignalFxPropertiesConfigAdapter;

@Builder @Getter
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenDto {
    private String accessToken;
    private String refreshToken;
}
