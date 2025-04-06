package com.example.apigateway.form.problem;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Data
@Getter
public class ProblemCreateForm {
    private String problemTitle;

    private String problemDescription;

    private List<String> problemRestriction;

    private List<ExampleForm> exampleList;

    private String exampleCode;

    private String problemCode;

    private LocalDate startDate;

    private LocalDate endDate;
}
