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
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Profile("8081")
@Component
@RequiredArgsConstructor
public class ExcelUtil {
    private final UserRepository userRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final ParticipantRepository participantRepository;
    private final TestCaseRepository testCaseRepository;
    private final FileUtil fileUtil;
    private final PasswordEncoder passwordEncoder;

    public void addStudentByExcel(Course course, MultipartFile file) throws IOException {
        try {
            courseStudentRepository.deleteCourseStudentsByCourseAndInviteType(course, InviteType.FILE);

            InputStream inputStream = file.getInputStream();
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

                courseStudentRepository.save(
                        CourseStudent.builder()
                                .course(course)
                                .user(student)
                                .inviteType(InviteType.FILE)
                                .build()
                );
            }
        } catch (IOException e) {
            throw new CustomException(CustomResponseException.INVALID_EXCEL_FILE);
        } finally {
            String savedFileName = fileUtil.save(file, "/course/" + course.getCourseId() + "/excel/");

            participantRepository.save(
                    Participant.builder()
                            .course(course)
                            .savedFileName(savedFileName)
                            .originalFileName(file.getOriginalFilename())
                            .filePath("/course/" + course.getCourseId() + "/excel/")
                            .build()
            );
        }
    }

    public void addTestCaseByExcel(Problem problem, MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            int num = 1;
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String input = getCellValue(row.getCell(0));
                String output = getCellValue(row.getCell(1));

                if (input.isEmpty() || output.isEmpty()) {
                    throw new CustomException(CustomResponseException.INVALID_TESTCASE);
                }

                saveStringToFile(input, "/problem/" + problem.getProblemId() + "/testcase/input" + num + ".txt");
                saveStringToFile(output, "/problem/" + problem.getProblemId() + "/testcase/output" + num + ".txt");
            }
        } catch (IOException e) {
            throw new CustomException(CustomResponseException.INVALID_EXCEL_FILE);
        } finally {
            String savedFileName = fileUtil.save(file, "/problem/" + problem.getProblemId() + "/excel/");

            testCaseRepository.save(
                    TestCase.builder()
                            .problem(problem)
                            .savedFileName(savedFileName)
                            .originalFileName(file.getOriginalFilename())
                            .filePath("/problem/" + problem.getProblemId() + "/excel/")
                            .build()
            );
        }
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

    public static void saveStringToFile(String content, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, content.getBytes());
    }
}
