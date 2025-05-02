package com.example.apigateway.dto.problem;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProblemDetailDto {
    private Long problemId;

    private String problemTitle;

    private List<String> restrictionList;

    private List<ExampleDto> exampleList;

    private String exampleCode;

    private LocalDate startDate;

    private LocalDate endDate;
}
