package com.example.apigateway.common.file;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import com.example.apigateway.dto.course.InviteStudentDto;
import com.example.apigateway.entity.Course;
import com.example.apigateway.entity.Participant;
import com.example.apigateway.entity.Problem;
import com.example.apigateway.entity.TestCase;
import com.example.apigateway.repository.*;
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
import java.util.ArrayList;
import java.util.List;

@Profile("course")
@Component
@RequiredArgsConstructor
public class ExcelUtil {
    private final ParticipantRepository participantRepository;
    private final TestCaseRepository testCaseRepository;
    private final FileUtil fileUtil;

    private final ApplicationEventPublisher applicationEventPublisher;

    public Mono<List<InviteStudentDto>> parseExcelToStudents(FilePart file, Course course) {
        return Mono.fromCallable(() -> {
            File tempFile = File.createTempFile("upload-", file.filename());
            file.transferTo(tempFile).block();

            try (InputStream inputStream = new FileInputStream(tempFile)) {
                Workbook workbook = new XSSFWorkbook(inputStream);
                Sheet sheet = workbook.getSheetAt(0);

                List<InviteStudentDto> result = new ArrayList<>();
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;

                    result.add(InviteStudentDto.builder()
                            .accountId(getCellValue(row.getCell(0)))
                            .name(getCellValue(row.getCell(1)))
                            .email(getCellValue(row.getCell(2)))
                            .password(getCellValue(row.getCell(3)).substring(getCellValue(row.getCell(3)).length() - 4))
                            .build());
                }
                return result;
            } finally {
                tempFile.delete();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }


    public Mono<Void> saveInviteFileRecord(FilePart file, Course course) {
        return fileUtil.save(file, "/course/" + course.getCourseId() + "/excel/")
                .doOnNext(savedPath -> participantRepository.save(Participant.builder()
                        .course(course)
                        .savedFileName(savedPath.substring(savedPath.lastIndexOf("/") + 1))
                        .originalFileName(file.filename())
                        .filePath("/course/" + course.getCourseId() + "/excel/")
                        .build()))
                .then();
    }

    public Mono<Void> addTestCaseByExcel(Problem problem, FilePart file) {
        return Mono.fromCallable(() -> File.createTempFile("upload-", file.filename()))  // ✅ 여기 return 추가
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
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d)) {
                    yield String.format("%.0f", d);
                } else {
                    yield String.valueOf(d);
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
