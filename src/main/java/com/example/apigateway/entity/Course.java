package com.example.apigateway.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;

    private String courseName;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<CourseStudent> courseStudentList;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<ProblemBank> problemBankList;

    private boolean deleted;
}
