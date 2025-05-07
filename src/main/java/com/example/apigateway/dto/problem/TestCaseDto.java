package com.example.apigateway.dto.problem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestCaseDto {
    private int num;

    private String input;

    private String output;
}
