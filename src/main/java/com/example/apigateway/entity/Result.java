package com.example.apigateway.entity;

import com.example.apigateway.common.type.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resultId;

    private int score;

    @Enumerated(EnumType.STRING)
    private Status status;

    private double executionTime;

    @Column(columnDefinition = "TEXT")
    private String errorDetail;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @OneToOne
    @JoinColumn(name = "submit_id")
    private Submit submit;

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
