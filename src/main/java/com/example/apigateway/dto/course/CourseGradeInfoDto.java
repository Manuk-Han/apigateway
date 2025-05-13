package com.example.apigateway.dto.course;

import com.example.apigateway.common.type.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseGradeInfoDto {
    private Long problemId;

    private String problemName;

    private List<CourseGradeDto> courseGradeDtoList;
}
