package com.example.apigateway.form.problem;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Data
@Getter
public class ProblemUpdateForm {
    private Long problemId;

    private String problemTitle;

    private String problemDescription;

    private List<String> problemRestriction;

    private List<ExampleForm> exampleList;

    private String exampleCode;

    private LocalDate startDate;

    private LocalDate endDate;
}
