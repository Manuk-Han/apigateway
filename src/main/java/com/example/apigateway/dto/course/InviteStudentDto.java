package com.example.apigateway.dto.course;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InviteStudentDto {
    private String name;

    private String accountId;

    private String password;

    private String email;
}
