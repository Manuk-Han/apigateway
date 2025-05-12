package com.example.apigateway.dto.result;

import com.example.apigateway.common.type.Language;
import com.example.apigateway.common.type.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ResultDto {
    private Long resultId;

    private Long submitId;

    private int score;

    private Status status;

    private Language language;

    private LocalDateTime submitTime;
}
