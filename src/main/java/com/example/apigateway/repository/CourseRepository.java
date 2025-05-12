package com.example.apigateway.repository;

import com.example.apigateway.entity.Course;
import com.example.apigateway.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findCourseByCourseUUid(String courseUUid);

    List<Course> findAllByOwner(User user);

    boolean existsCourseByCourseUUidAndOwner(String courseUUid, User user);
}
