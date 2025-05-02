package com.example.apigateway.dto.result;

import com.example.apigateway.common.type.Language;
import com.example.apigateway.common.type.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResultDetailDto {
    private Long resultId;

    private Long submitId;

    private int score;

    private Language language;

    private Status status;

    private LocalDateTime submitTime;

    private String accountId;

    private String errorMessage;

    private String feedback;
}
