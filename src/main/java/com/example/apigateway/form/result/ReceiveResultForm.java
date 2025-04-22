package com.example.apigateway.form.result;

import com.example.apigateway.common.type.Language;
import com.example.apigateway.common.type.Status;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class ReceiveResultForm {
    private Long submissionId;

    private int score;

    private Status status;

    private double executionTime;

    private String errorDetail;
}
