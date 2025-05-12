package com.example.apigateway.dto.problem;

import com.example.apigateway.common.type.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ProblemStudentDto extends ProblemDto {
    private Status status;
}
