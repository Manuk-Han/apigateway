package com.example.apigateway.dto.result;

import com.example.apigateway.common.type.Language;
import com.example.apigateway.common.type.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;


@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResultDto {
    private Long resultId;

    private int score;

    private Status status;

    private String errorDetail;

    private String feedback;
}
