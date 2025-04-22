package com.example.apigateway.service;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import com.example.apigateway.common.kafka.KafkaSubmitForm;
import com.example.apigateway.common.type.Language;
import com.example.apigateway.entity.*;
import com.example.apigateway.form.submit.SubmitForm;
import com.example.apigateway.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
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
    private final SubmitRepository submitRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public Long submitProblem(Long userId, String courseUUId, Long problemId, SubmitForm submitForm) throws IOException {
        Course course = validateCourseStudent(userId, courseUUId);

        ProblemBank problemBank = problemBankRepository.findByCourse(course);

        Problem problem = problemRepository.findByProblemIdAndProblemBank(problemId, problemBank)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_PROBLEM));

        Submit submit = Submit.builder()
                .problem(problem)
                .student(userRepository.findById(userId)
                        .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT)))
                .code(submitForm.getCode())
                .language(Language.getLanguage(String.valueOf(submitForm.getLanguage())))
                .build();

        submitRepository.save(submit);

        String topic = "submission-" + submitForm.getLanguage();
        KafkaSubmitForm kafkaSubmitForm = KafkaSubmitForm.builder()
                .submitId(submit.getSubmitId())
                .problemId(problem.getProblemId())
                .userId(userId)
                .code(submitForm.getCode())
                .language(submitForm.getLanguage())
                .build();
        String payload = objectMapper.writeValueAsString(kafkaSubmitForm);

        kafkaTemplate.send(topic, payload);

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
