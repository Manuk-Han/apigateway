package com.example.apigateway.form.result;

import com.example.apigateway.common.type.Language;
import com.example.apigateway.common.type.Status;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class ReceiveResultForm {
    private Long submitId;

    private int score;

    private String status;

    private double executionTime;

    private String errorDetail;
}
