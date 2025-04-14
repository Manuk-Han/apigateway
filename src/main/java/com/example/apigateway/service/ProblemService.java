package com.example.apigateway.service;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import com.example.apigateway.common.file.ExcelUtil;
import com.example.apigateway.common.file.FileUtil;
import com.example.apigateway.entity.*;
import com.example.apigateway.form.problem.ProblemCreateForm;
import com.example.apigateway.form.problem.ProblemUpdateForm;
import com.example.apigateway.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Profile("8081")
@Service
@RequiredArgsConstructor
public class ProblemService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ProblemBankRepository problemBankRepository;
    private final RestrictionRepository restrictionRepository;
    private final ExampleRepository exampleRepository;
    private final ProblemRepository problemRepository;
    private final ExcelUtil excelUtil;

    public Long createProblem(Long userId, String courseUUid, ProblemCreateForm problemCreateForm, MultipartFile testCaseFile) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Course course = courseRepository.findCourseByCourseUUid(courseUUid)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        validateCourseOwner(user, course);

        ProblemBank problemBank = problemBankRepository.findByCourse(course);

        Problem problem = Problem.builder()
                .problemTitle(problemCreateForm.getProblemTitle())
                .problemDescription(problemCreateForm.getProblemDescription())
                .exampleCode(problemCreateForm.getExampleCode())
                .startDate(problemCreateForm.getStartDate())
                .endDate(problemCreateForm.getEndDate())
                .problemBank(problemBank)
                .build();
        problemRepository.save(problem);

        restrictionRepository.saveAll(problemCreateForm.getProblemRestriction()
                .stream()
                .map(restriction -> Restriction.builder()
                        .restrictionDescription(restriction)
                        .problem(problem)
                        .build())
                .toList());

        exampleRepository.saveAll(problemCreateForm.getExampleList()
                .stream()
                .map(example -> Example.builder()
                        .inputExample(example.getInputExample())
                        .outputExample(example.getInputExample())
                        .problem(problem)
                        .build())
                .toList());

        excelUtil.addTestCaseByExcel(problem, testCaseFile);

        problemRepository.save(problem);

        return problem.getProblemId();
    }

    public Long updateProblem(Long userId, String courseUUid, ProblemUpdateForm problemUpdateForm, MultipartFile testCaseFile) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));

        Course course = courseRepository.findCourseByCourseUUid(courseUUid)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        validateCourseOwner(user, course);

        Problem problem = problemRepository.findById(problemUpdateForm.getProblemId())
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_PROBLEM));

        restrictionRepository.deleteRestrictionsByProblem(problem);
        exampleRepository.deleteExamplesByProblem(problem);

        problemRepository.save(problem.updateProblem(problemUpdateForm));

        restrictionRepository.saveAll(problemUpdateForm.getProblemRestriction()
                .stream()
                .map(restriction -> Restriction.builder()
                        .restrictionDescription(restriction)
                        .problem(problem)
                        .build())
                .toList());

        exampleRepository.saveAll(problemUpdateForm.getExampleList()
                .stream()
                .map(example -> Example.builder()
                        .inputExample(example.getInputExample())
                        .outputExample(example.getInputExample())
                        .problem(problem)
                        .build())
                .toList());

        if(testCaseFile != null)
            excelUtil.addTestCaseByExcel(problem, testCaseFile);

        problemRepository.save(problem);

        return problem.getProblemId();
    }

    private void validateCourseOwner(User user, Course course) {
        if (!course.getOwner().equals(user)) {
            throw new CustomException(CustomResponseException.FORBIDDEN);
        }
    }
}
