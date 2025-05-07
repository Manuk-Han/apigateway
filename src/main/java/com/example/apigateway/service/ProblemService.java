package com.example.apigateway.service;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import com.example.apigateway.common.file.ExcelUtil;
import com.example.apigateway.common.file.TestcaseFileSaveEvent;
import com.example.apigateway.dto.problem.ExampleDto;
import com.example.apigateway.dto.problem.ProblemDetailDto;
import com.example.apigateway.dto.problem.ProblemDto;
import com.example.apigateway.dto.problem.TestCaseDto;
import com.example.apigateway.entity.*;
import com.example.apigateway.form.problem.ProblemCreateForm;
import com.example.apigateway.form.problem.ProblemUpdateForm;
import com.example.apigateway.repository.*;
import com.example.apigateway.service.common.ValidateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Profile("course")
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProblemService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ProblemBankRepository problemBankRepository;
    private final RestrictionRepository restrictionRepository;
    private final ExampleRepository exampleRepository;
    private final ProblemRepository problemRepository;
    private final ExcelUtil excelUtil;
    private final ValidateUtil validateUtil;

    @Value("${file.sample.testcase.path}")
    private String sampleExcelFilePath;

    public byte[] getSampleExcel() throws IOException {
        File file = new File(sampleExcelFilePath);

        if (!file.exists()) {
            throw new CustomException(CustomResponseException.FILE_NOT_FOUND);
        }

        return Files.readAllBytes(file.toPath());
    }

    private final ApplicationEventPublisher applicationEventPublisher;

    public Mono<Long> createProblem(Long userId, String courseUUId, ProblemCreateForm form, FilePart testCaseFile) {
        Course course = validateUtil.validateCourseOwner(userId, courseUUId);
        ProblemBank problemBank = problemBankRepository.findByCourse(course);

        Problem problem = Problem.builder()
                .problemTitle(form.getProblemTitle())
                .problemDescription(form.getProblemDescription())
                .exampleCode(form.getExampleCode())
                .startDate(form.getStartDate())
                .endDate(form.getEndDate())
                .problemBank(problemBank)
                .build();

        problemRepository.save(problem);

        restrictionRepository.saveAll(form.getProblemRestriction()
                .stream()
                .map(r -> Restriction.builder()
                        .restrictionDescription(r)
                        .problem(problem)
                        .build())
                .toList());

        exampleRepository.saveAll(form.getExampleList()
                .stream()
                .map(ex -> Example.builder()
                        .inputExample(ex.getInputExample())
                        .outputExample(ex.getOutputExample())
                        .problem(problem)
                        .build())
                .toList());

        if (testCaseFile != null) {
            return excelUtil.parseExcelToTestcases(testCaseFile, problem)
                    .flatMap(testCaseDtoList -> Mono.fromRunnable(() -> {
                        List<TestcaseFileSaveEvent> eventList = testCaseDtoList.stream()
                                .map(testcase -> TestcaseFileSaveEvent.builder()
                                        .problemId(problem.getProblemId())
                                        .inputContent(testcase.getInput())
                                        .outputContent(testcase.getOutput())
                                        .num(testcase.getNum())
                                        .build())
                                .toList();

                        createProblemTestCase(eventList, problem.getProblemId()); // ✅ 이벤트 발행
                    }))
                    .subscribeOn(Schedulers.boundedElastic())
                    .thenReturn(problem.getProblemId());
        }


        return Mono.just(problem.getProblemId());
    }

    public void createProblemTestCase(List<TestcaseFileSaveEvent> eventList, Long problemId) {
        eventList.forEach(applicationEventPublisher::publishEvent);
    }

    public Mono<Long> updateProblem(Long userId, String courseUUId, ProblemUpdateForm form, FilePart testCaseFile) {
        validateUtil.validateCourseOwner(userId, courseUUId);

        Problem problem = problemRepository.findById(form.getProblemId())
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_PROBLEM));

        restrictionRepository.deleteRestrictionsByProblem(problem);
        exampleRepository.deleteExamplesByProblem(problem);

        problemRepository.save(problem.updateProblem(form));

        restrictionRepository.saveAll(form.getProblemRestriction()
                .stream()
                .map(desc -> Restriction.builder()
                        .restrictionDescription(desc)
                        .problem(problem)
                        .build())
                .toList());

        exampleRepository.saveAll(form.getExampleList()
                .stream()
                .map(ex -> Example.builder()
                        .inputExample(ex.getInputExample())
                        .outputExample(ex.getOutputExample())
                        .problem(problem)
                        .build())
                .toList());

        Mono<Void> testCaseSaveMono = Mono.empty();
//        if (testCaseFile != null) {
//            testCaseSaveMono = excelUtil.addTestCaseByExcel(problem, testCaseFile)
//                    .doOnNext(events -> events.forEach(applicationEventPublisher::publishEvent))
//                    .then();
//        }

        return testCaseSaveMono.thenReturn(problem.getProblemId());
    }

    public void deleteProblem(Long userId, String courseUUId, Long problemId) {
        Course course = validateUtil.validateCourseOwner(userId, courseUUId);

        ProblemBank problemBank = problemBankRepository.findByCourse(course);

        problemRepository.deleteProblemByProblemIdAndProblemBank(problemId, problemBank);
    }

    public List<ProblemDto> getProblemList(Long userId, String courseUUId) {
        if (checkManager(userId, courseUUId)) {
            return problemBankRepository.findByCourse(courseRepository.findCourseByCourseUUid(courseUUId)
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
            Course course = validateUtil.validateCourseMember(userId, courseUUId);

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
        Course course = validateUtil.validateCourseMember(userId, courseUUId);

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

        Course course = courseRepository.findCourseByCourseUUid(courseUUId)
                .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_COURSE));

        return course.getOwner().equals(user);
    }
}
