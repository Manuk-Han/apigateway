package com.example.apigateway.common.file;

import com.example.apigateway.dto.course.InviteStudentDto;
import com.example.apigateway.dto.problem.TestCaseDto;
import com.example.apigateway.entity.Course;
import com.example.apigateway.entity.Participant;
import com.example.apigateway.entity.Problem;
import com.example.apigateway.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Profile;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Profile("course")
@Component
@RequiredArgsConstructor
public class ExcelUtil {
    private final ParticipantRepository participantRepository;
    private final FileUtil fileUtil;

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

    public Mono<List<TestCaseDto>> parseExcelToTestcases(FilePart file, Problem problem) {
        return Mono.fromCallable(() -> {
            File tempFile = File.createTempFile("upload-", file.filename());
            file.transferTo(tempFile).block();

            try (InputStream inputStream = new FileInputStream(tempFile)) {
                Workbook workbook = new XSSFWorkbook(inputStream);
                Sheet sheet = workbook.getSheetAt(0);

                int num = 1;
                List<TestCaseDto> result = new ArrayList<>();
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;

                    result.add(TestCaseDto.builder()
                            .num(num++)
                            .input(getCellValue(row.getCell(0)))
                            .output(getCellValue(row.getCell(1)))
                            .build());
                }
                return result;
            } finally {
                tempFile.delete();
            }
        }).subscribeOn(Schedulers.boundedElastic());
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
