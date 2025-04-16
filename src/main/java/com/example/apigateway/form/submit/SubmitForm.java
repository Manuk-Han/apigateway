package com.example.apigateway.form.submit;

import com.example.apigateway.common.type.Language;
import com.example.apigateway.form.problem.ExampleForm;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Data
@Getter
public class SubmitForm {
    private String code;

    private Language language;
}
