package com.example.apigateway.entity;

import com.example.apigateway.common.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class UserRole {
    @Id
    private Long userRoleId;

    private Role role;
}
