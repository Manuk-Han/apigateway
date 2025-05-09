package com.example.apigateway.repository;

import com.example.apigateway.common.type.InviteType;
import com.example.apigateway.entity.Course;
import com.example.apigateway.entity.CourseStudent;
import com.example.apigateway.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseStudentRepository extends JpaRepository<CourseStudent, Long> {
    Optional<CourseStudent> findByCourseAndUser(Course course, User user);

    List<CourseStudent> findCourseStudentsByCourse(Course course);

    boolean existsByCourseAndUser(Course course, User user);

    void deleteCourseStudentsByCourseAndInviteType(Course course, InviteType inviteType);
}
