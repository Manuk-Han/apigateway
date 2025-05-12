package com.example.apigateway.dto.course;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseDto {
    private String courseUUid;

    private String courseName;

    private String creatorName;
}
