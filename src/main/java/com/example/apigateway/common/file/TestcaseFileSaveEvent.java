package com.example.apigateway.common.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder @Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class TestcaseFileSaveEvent {

    private String inputContent;

    private String outputContent;

    private Long problemId;

    private int num;
}