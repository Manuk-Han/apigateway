package com.example.apigateway.entity;

import com.example.apigateway.common.type.Language;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Submit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long submitId;

    @Enumerated(EnumType.STRING)
    private Language language;

    @Column(columnDefinition = "TEXT")
    private String code;

    private LocalDateTime submitTime;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @OneToOne(mappedBy = "submit", cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "result_id")
    private Result result;
}
