package com.example.apigateway.dto.problem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExampleDto {
    private String exampleInput;

    private String exampleOutput;
}
