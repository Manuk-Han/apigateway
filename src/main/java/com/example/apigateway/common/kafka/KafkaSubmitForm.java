package com.example.apigateway.common.kafka;

import com.example.apigateway.common.type.Language;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KafkaSubmitForm {
    private Long submitId;

    private Long problemId;

    private Long userId;

    private String code;

    private Language language;
}
