package com.example.apigateway.form.course;

import lombok.Data;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Data
@Getter
public class AddStudentByFileForm {
    private MultipartFile file;
}
