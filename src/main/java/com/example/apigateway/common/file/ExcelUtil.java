package com.example.apigateway.common.file;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import com.example.apigateway.common.type.InviteType;
import com.example.apigateway.entity.*;
import com.example.apigateway.repository.CourseStudentRepository;
import com.example.apigateway.repository.ParticipantRepository;
import com.example.apigateway.repository.TestCaseRepository;
import com.example.apigateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Profile("course")
@Component
@RequiredArgsConstructor
public class ExcelUtil {
    private final UserRepository userRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final ParticipantRepository participantRepository;
    private final TestCaseRepository testCaseRepository;
    private final FileUtil fileUtil;
    private final PasswordEncoder passwordEncoder;

    private final ApplicationEventPublisher applicationEventPublisher;

    public void addStudentByExcel(Course course, FilePart file) {
        Mono.fromCallable(() -> {
                    courseStudentRepository.deleteCourseStudentsByCourseAndInviteType(course, InviteType.FILE);
                    return File.createTempFile("upload-", file.filename());
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(tempFile ->
                        file.transferTo(tempFile)
                                .then(Mono.fromCallable(() -> {
                                    try (InputStream inputStream = new FileInputStream(tempFile)) {
                                        Workbook workbook = new XSSFWorkbook(inputStream);
                                        Sheet sheet = workbook.getSheetAt(0);

                                        for (Row row : sheet) {
                                            if (row.getRowNum() == 0) continue;

                                            String studentId = getCellValue(row.getCell(0));
                                            User student;

                                            if (userRepository.existsByAccountId(studentId)) {
                                                student = userRepository.findByAccountId(studentId)
                                                        .orElseThrow(() -> new CustomException(CustomResponseException.NOT_FOUND_ACCOUNT));
                                            } else {
                                                String name = getCellValue(row.getCell(1));
                                                String email = getCellValue(row.getCell(2));
                                                String phone = getCellValue(row.getCell(3));

                                                student = User.builder()
                                                        .name(name)
                                                        .accountId(studentId)
                                                        .password(passwordEncoder.encode(phone.substring(phone.length() - 4)))
                                                        .email(email)
                                                        .withdraw(false)
                                                        .build();

                                                userRepository.save(student);
                                            }

                                            courseStudentRepository.save(CourseStudent.builder()
                                                    .course(course)
                                                    .user(student)
                                                    .inviteType(InviteType.FILE)
                                                    .build());
                                        }

                                        return tempFile;
                                    } catch (IOException e) {
                                        throw new CustomException(CustomResponseException.INVALID_EXCEL_FILE);
                                    }
                                }))
                )
                .flatMap(tempFile ->
                        fileUtil.save(file, "/course/" + course.getCourseId() + "/excel/")
                                .doOnNext(savedPath -> {
                                    participantRepository.save(
                                            Participant.builder()
                                                    .course(course)
                                                    .savedFileName(savedPath.substring(savedPath.lastIndexOf("/") + 1))
                                                    .originalFileName(file.filename())
                                                    .filePath("/course/" + course.getCourseId() + "/excel/")
                                                    .build()
                                    );
                                    tempFile.delete();
                                })
                )
                .then();
    }

    public void addTestCaseByExcel(Problem problem, FilePart file) {
        Mono.fromCallable(() -> File.createTempFile("upload-", file.filename()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(tempFile -> file.transferTo(tempFile)
                        .then(Mono.fromRunnable(() -> {
                            try (InputStream inputStream = new FileInputStream(tempFile)) {
                                Workbook workbook = new XSSFWorkbook(inputStream);
                                Sheet sheet = workbook.getSheetAt(0);

                                int num = 1;
                                for (Row row : sheet) {
                                    if (row.getRowNum() == 0) continue;

                                    String input = getCellValue(row.getCell(0));
                                    String output = getCellValue(row.getCell(1));

                                    if (input.isEmpty() || output.isEmpty()) {
                                        throw new CustomException(CustomResponseException.INVALID_TESTCASE);
                                    }

                                    applicationEventPublisher.publishEvent(
                                            TestcaseFileSaveEvent.builder()
                                                    .inputContent(input)
                                                    .outputContent(output)
                                                    .problemId(problem.getProblemId())
                                                    .num(num++)
                                                    .build()
                                    );
                                }
                            } catch (IOException e) {
                                throw new CustomException(CustomResponseException.INVALID_EXCEL_FILE);
                            } finally {
                                tempFile.delete();
                            }
                        }))
                        .then(fileUtil.save(file, "/problem/" + problem.getProblemId() + "/excel/"))
                        .doOnNext(savedPath -> {
                            testCaseRepository.save(TestCase.builder()
                                    .problem(problem)
                                    .savedFileName(savedPath.substring(savedPath.lastIndexOf("/") + 1))
                                    .originalFileName(file.filename())
                                    .filePath("/problem/" + problem.getProblemId() + "/excel/")
                                    .build());
                        })
                        .then()
                );
    }


    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
