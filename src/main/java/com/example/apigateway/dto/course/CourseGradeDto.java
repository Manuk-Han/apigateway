package com.example.apigateway.dto.course;

import com.example.apigateway.common.type.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseGradeDto {
    private Long problemId;

    private String problemTitle;

    private String accountId;

    private String studentName;

    private int score;

    private Status status;

    private Long submitId;
}
