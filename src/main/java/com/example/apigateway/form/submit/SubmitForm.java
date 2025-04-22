package com.example.apigateway.form.submit;

import com.example.apigateway.common.type.Language;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class SubmitForm {
    private String code;

    private Language language;
}
