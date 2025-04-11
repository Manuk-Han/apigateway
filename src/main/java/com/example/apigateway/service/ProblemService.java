package com.example.apigateway.service;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import com.example.apigateway.entity.Course;
import com.example.apigateway.entity.Problem;
import com.example.apigateway.entity.ProblemBank;
import com.example.apigateway.entity.User;
import com.example.apigateway.form.problem.ProblemCreateForm;
import com.example.apigateway.repository.CourseRepository;
import com.example.apigateway.repository.ProblemBankRepository;
import com.example.apigateway.repository.ProblemRepository;
import com.example.apigateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Profile("8081")
@Service
@RequiredArgsConstructor
public class ProblemService {
    private final UserRepository userRepository;

    private final CourseRepository courseRepository;

    private final ProblemBankRepository problemBankRepository;

    private final ProblemRepository problemRepository;

    public Long createProblem(Long userId, String courseUUid, ProblemCreateForm problemCreateForm) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Course course = courseRepository.findCourseByCourseUUid(courseUUid)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        validateCourseOwner(user, course);

        if (problemBankRepository.existsByCourse(course)) {
            ProblemBank problemBank = problemBankRepository.findByCourse(course);

            Problem problem = Problem.builder()
                    .problemTitle(problemCreateForm.getProblemTitle())
                    .problemDescription(problemCreateForm.getProblemDescription())
//                    .problemRestriction(problemCreateForm.getProblemRestriction())
                    .build();
        }

        return null;
    }

    private void validateCourseOwner(User user, Course course) {
        if (!course.getOwner().equals(user)) {
            throw new CustomException(CustomResponseException.FORBIDDEN);
        }
    }
}
