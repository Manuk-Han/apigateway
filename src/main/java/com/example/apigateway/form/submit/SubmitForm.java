package com.example.apigateway.form.submit;

import com.example.apigateway.common.type.Language;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class SubmitForm {
    private Long problemId;

    private String code;

    private Language language;
}
