package com.example.apigateway.dto.course;

import lombok.*;


@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseDto {
    private String courseUUid;

    private String courseName;
}
