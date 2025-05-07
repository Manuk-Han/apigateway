package com.example.apigateway.form.submit;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class SubmitForm {
    private Long problemId;

    private String code;

    private String language;
}
