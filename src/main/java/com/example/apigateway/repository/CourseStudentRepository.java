package com.example.apigateway.repository;

import com.example.apigateway.entity.Course;
import com.example.apigateway.entity.CourseStudent;
import com.example.apigateway.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseStudentRepository extends JpaRepository<CourseStudent, Long> {
    Optional<CourseStudent> findByCourseAndUser(Course course, User user);
}
