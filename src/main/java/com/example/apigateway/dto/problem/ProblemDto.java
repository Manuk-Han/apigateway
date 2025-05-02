package com.example.apigateway.dto.problem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProblemDto {
    private Long problemId;

    private String problemTitle;

    private LocalDate startDate;

    private LocalDate endDate;
}
