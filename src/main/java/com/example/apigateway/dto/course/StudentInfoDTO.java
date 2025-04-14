package com.example.apigateway.dto.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentInfoDTO {
    private String accountId;

    private String name;

    private String email;
}
