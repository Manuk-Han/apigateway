package com.example.apigateway.dto.problem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProblemDto {
    private Long problemId;

    private String problemTitle;

    private LocalDate startDate;

    private LocalDate endDate;
}
