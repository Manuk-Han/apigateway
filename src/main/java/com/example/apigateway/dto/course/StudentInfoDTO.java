package com.example.apigateway.dto.course;

import com.example.apigateway.common.type.InviteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentInfoDTO {
    private String accountId;

    private String name;

    private String email;

    private InviteType inviteType;
}
