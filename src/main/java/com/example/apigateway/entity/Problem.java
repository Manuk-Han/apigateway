package com.example.apigateway.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long problemId;

    private String problemTitle;

    private String problemDescription;

    private String problemRestriction;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL)
    private List<Example> exampleList;

    private String exampleCode;

    private String testCasePath;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @ManyToOne
    private ProblemBank problemBank;
}
