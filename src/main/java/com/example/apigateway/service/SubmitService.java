package com.example.apigateway.service;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import com.example.apigateway.common.file.ExcelUtil;
import com.example.apigateway.entity.*;
import com.example.apigateway.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Profile("submit")
@Service
@RequiredArgsConstructor
public class SubmitService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ProblemBankRepository problemBankRepository;
    private final ProblemRepository problemRepository;
    private final CourseStudentRepository courseStudentRepository;

    public Long submitProblem(Long userId, String courseUUId, Long problemId) throws IOException {
        Course course = validateCourseStudent(userId, courseUUId);

        ProblemBank problemBank = problemBankRepository.findByCourse(course);

        Problem problem = problemRepository.findByProblemIdAndProblemBank(problemId, problemBank)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_PROBLEM));

        //TODO: Implement run submission logic

        return problem.getProblemId();
    }

    private Course validateCourseOwner(Long userId, String courseUUId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Course course = courseRepository.findCourseByCourseUUid(courseUUId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        if (!course.getOwner().equals(user))
            throw new CustomException(CustomResponseException.FORBIDDEN);

        return course;
    }

    private Course validateCourseStudent(Long userId, String courseUUId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Course course = courseRepository.findCourseByCourseUUid(courseUUId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        if (!courseStudentRepository.existsByCourseAndUser(course, user))
            throw new CustomException(CustomResponseException.FORBIDDEN);

        return course;
    }
}
