package com.example.apigateway.entity;

import com.example.apigateway.common.Role;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userRoleId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
