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
public class ProblemBank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long problemBankId;

    @ManyToOne
    private Course course;

    @OneToMany(mappedBy = "problemBank", cascade = CascadeType.ALL)
    private List<Problem> problemList;
}
