package com.example.apigateway.service;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import com.example.apigateway.common.jwt.JwtTokenProvider;
import com.example.apigateway.entity.Course;
import com.example.apigateway.entity.User;
import com.example.apigateway.form.course.CourseCreateForm;
import com.example.apigateway.form.course.CourseUpdateForm;
import com.example.apigateway.repository.CourseRepository;
import com.example.apigateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.swing.undo.CannotRedoException;
import java.util.UUID;

@Profile("8081")
@Service
@RequiredArgsConstructor
public class CourseService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public String createCourse(Long userId, CourseCreateForm courseCreateForm) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Course course = Course.builder()
                .courseName(courseCreateForm.getCourseName())
                .courseUUid(UUID.randomUUID().toString())
                .courseStart(courseCreateForm.getCourseStart())
                .courseEnd(courseCreateForm.getCourseEnd())
                .owner(user)
                .build();

        courseRepository.save(course);

        return course.getCourseUUid();
    }

    public String updateCourse(Long userId, CourseUpdateForm courseUpdateForm) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Course course = courseRepository.findById(courseUpdateForm.getCourseId())
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        if (!course.getOwner().equals(user)) {
            throw new CustomException(CustomResponseException.FORBIDDEN);
        }

        course.updateCourse(courseUpdateForm);
        courseRepository.save(course);

        return course.getCourseUUid();
    }
}
