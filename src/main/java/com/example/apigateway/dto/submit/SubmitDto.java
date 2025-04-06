package com.example.apigateway.dto.submit;

import com.example.apigateway.common.type.Language;
import com.example.apigateway.dto.result.ResultDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;


@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitDto {
    private Long submitId;

    private Language language;

    private String code;

    private String submitTime;

    private Long studentId;

    private Long problemId;

    private ResultDto resultDto;
}
