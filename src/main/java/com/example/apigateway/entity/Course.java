package com.example.apigateway.entity;

import com.example.apigateway.form.course.CourseUpdateForm;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;

    private String courseUUid;

    private String courseName;

    private LocalDate courseStart;

    private LocalDate courseEnd;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<CourseStudent> courseStudentList;

    @OneToOne(mappedBy = "course", cascade = CascadeType.ALL)
    private ProblemBank problemBank;

    private boolean deleted;

    public void updateCourse(CourseUpdateForm courseUpdateForm) {
        this.courseName = courseUpdateForm.getCourseName();
        this.courseStart = courseUpdateForm.getCourseStart();
        this.courseEnd = courseUpdateForm.getCourseEnd();
    }
}
