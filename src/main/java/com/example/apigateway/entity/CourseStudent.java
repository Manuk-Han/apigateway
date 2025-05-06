package com.example.apigateway.entity;

import com.example.apigateway.common.type.InviteType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseStudent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseStudentId;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private InviteType inviteType;

    public CourseStudent updateInviteType() {
        if (inviteType == InviteType.FILE) inviteType = InviteType.ONE;
        else inviteType = InviteType.FILE;

        return this;
    }
}
