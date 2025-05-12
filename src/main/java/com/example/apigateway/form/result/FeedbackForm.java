package com.example.apigateway.form.result;

import com.example.apigateway.common.type.Status;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class FeedbackForm {
    private Long resultId;

    private String feedback;

    private Status status;
}
