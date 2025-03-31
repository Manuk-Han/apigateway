package com.example.apigateway.common.file;

import com.example.apigateway.entity.User;
import com.example.apigateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class ExcelUtil {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void processExcel(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String studentId = getCellValue(row.getCell(0));
                if(userRepository.existsByAccountId(studentId)) continue;

                String name = getCellValue(row.getCell(1));
                String email = getCellValue(row.getCell(2));
                String phone = getCellValue(row.getCell(3));

                userRepository.save(
                        User.builder()
                                .name(name)
                                .accountId(studentId)
                                .password(passwordEncoder.encode(phone.substring(phone.length() - 4)))
                                .email(email)
                                .withdraw(false)
                                .build()
                );
            }
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
}
