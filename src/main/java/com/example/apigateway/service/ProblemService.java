package com.example.apigateway.service;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import com.example.apigateway.common.file.ExcelUtil;
import com.example.apigateway.dto.problem.ExampleDto;
import com.example.apigateway.dto.problem.ProblemDetailDto;
import com.example.apigateway.dto.problem.ProblemDto;
import com.example.apigateway.entity.*;
import com.example.apigateway.form.problem.ProblemCreateForm;
import com.example.apigateway.form.problem.ProblemUpdateForm;
import com.example.apigateway.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
    private final CourseStudentRepository courseStudentRepository;

    public Long createProblem(Long userId, String courseUUId, ProblemCreateForm problemCreateForm, MultipartFile testCaseFile) throws IOException {
        Course course = validateCourseOwner(userId, courseUUId);

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

    public Long updateProblem(Long userId, String courseUUId, ProblemUpdateForm problemUpdateForm, MultipartFile testCaseFile) throws IOException {
        validateCourseOwner(userId, courseUUId);

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
    
    public void deleteProblem(Long userId, String courseUUId, Long problemId) {
        Course course = validateCourseOwner(userId, courseUUId);

        ProblemBank problemBank = problemBankRepository.findByCourse(course);

        problemRepository.deleteProblemByProblemIdAndProblemBank(problemId, problemBank);
    }
    
    public List<ProblemDto> getProblemList(Long userId, String courseUUId) {
        if (checkManager(userId, courseUUId)) {
            return problemBankRepository.findByCourse(courseRepository.findCourseBycourseUUId(courseUUId)
                    .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE)))
                    .getProblemList()
                    .stream()
                    .map(problem -> 
                        ProblemDto.builder()
                                .problemId(problem.getProblemId())
                                .problemTitle(problem.getProblemTitle())
                                .startDate(problem.getStartDate())
                                .endDate(problem.getEndDate())
                                .build())
                    .toList();
        } else {
            Course course = validateCourseStudent(userId, courseUUId);
            
            return problemBankRepository.findByCourse(course)
                    .getProblemList()
                    .stream()
                    .map(problem -> 
                        ProblemDto.builder()
                                .problemId(problem.getProblemId())
                                .problemTitle(problem.getProblemTitle())
                                .startDate(problem.getStartDate())
                                .endDate(problem.getEndDate())
                                .build())
                    .toList();
        }
    }

    public ProblemDetailDto getProblemDetail(Long userId, String courseUUId, Long problemId) {
        Course course = validateCourseStudent(userId, courseUUId);

        ProblemBank problemBank = problemBankRepository.findByCourse(course);

        Problem problem = problemRepository.findByProblemIdAndProblemBank(problemId, problemBank).orElseThrow(
                () -> new CustomException(CustomResponseException.NOT_FOUND_PROBLEM));

        return ProblemDetailDto.builder()
                .problemId(problem.getProblemId())
                .problemTitle(problem.getProblemTitle())
                .restrictionList(problem.getRestrictionList()
                        .stream()
                        .map(Restriction::getRestrictionDescription)
                        .collect(Collectors.toList()))
                .exampleList(problem.getExampleList()
                        .stream()
                        .map(example -> ExampleDto.builder()
                                .exampleInput(example.getInputExample())
                                .exampleOutput(example.getOutputExample())
                                .build())
                        .collect(Collectors.toList()))
                .exampleCode(problem.getExampleCode())
                .startDate(problem.getStartDate())
                .endDate(problem.getEndDate())
                .build();
    }
    
    private boolean checkManager(Long userId, String courseUUId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));
        
        Course course = courseRepository.findCourseBycourseUUId(courseUUId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));
        
        return course.getOwner().equals(user);
    }

    private Course validateCourseOwner(Long userId, String courseUUId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));
        
        Course course = courseRepository.findCourseBycourseUUId(courseUUId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        if (!course.getOwner().equals(user))
            throw new CustomException(CustomResponseException.FORBIDDEN);
        
        return course;
    }

    private Course validateCourseStudent(Long userId, String courseUUId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));
        
        Course course = courseRepository.findCourseBycourseUUId(courseUUId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        if (!courseStudentRepository.existsByCourseAndUser(course, user))
            throw new CustomException(CustomResponseException.FORBIDDEN);
        
        return course;
    }
}
