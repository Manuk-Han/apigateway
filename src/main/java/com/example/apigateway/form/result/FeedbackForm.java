package com.example.apigateway.form.result;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class FeedbackForm {
    private Long resultId;

    private String feedback;
}
