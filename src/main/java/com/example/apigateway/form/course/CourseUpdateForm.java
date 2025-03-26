package com.example.apigateway.form.course;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;

@Data
@Getter
public class CourseUpdateForm {
    private Long courseId;

    private String courseName;

    private LocalDate courseStart;

    private LocalDate courseEnd;
}
