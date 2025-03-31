package com.example.apigateway.form.course;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;

@Data
@Getter
public class AddStudentForm {
    private String studentId;

    private String studentName;

    private String phone;

    private String email;
}
