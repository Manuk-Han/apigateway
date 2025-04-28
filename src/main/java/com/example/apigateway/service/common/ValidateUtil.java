package com.example.apigateway.service.common;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import com.example.apigateway.common.type.Role;
import com.example.apigateway.entity.Course;
import com.example.apigateway.entity.User;
import com.example.apigateway.repository.CourseRepository;
import com.example.apigateway.repository.CourseStudentRepository;
import com.example.apigateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidateUtil {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseStudentRepository courseStudentRepository;

    public Course validateCourseOwner(Long userId, String courseUUId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Course course = courseRepository.findCourseByCourseUUid(courseUUId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        if (user.getRole() == Role.ADMIN)
            return course;

        if (!course.getOwner().equals(user))
            throw new CustomException(CustomResponseException.FORBIDDEN);

        return course;
    }

    public Course validateCourseMember(Long userId, String courseUUId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Course course = courseRepository.findCourseByCourseUUid(courseUUId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        if (!(courseStudentRepository.existsByCourseAndUser(course, user) || course.getOwner().equals(user)))
            throw new CustomException(CustomResponseException.FORBIDDEN);

        return course;
    }
}
