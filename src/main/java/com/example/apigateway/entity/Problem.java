package com.example.apigateway.entity;

import com.example.apigateway.form.problem.ProblemUpdateForm;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Restriction> restrictionList;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Example> exampleList;

    @Column(columnDefinition = "TEXT")
    private String exampleCode;

    @OneToOne(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private TestCase testCase;

    private LocalDate startDate;

    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "problem_bank_id")
    private ProblemBank problemBank;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Submit> submitList;

    public Problem updateProblem(ProblemUpdateForm problemUpdateForm) {
        this.problemTitle = problemUpdateForm.getProblemTitle();
        this.problemDescription = problemUpdateForm.getProblemDescription();
        this.exampleCode = problemUpdateForm.getExampleCode();
        this.startDate = problemUpdateForm.getStartDate();
        this.endDate = problemUpdateForm.getEndDate();

        return this;
    }
}
