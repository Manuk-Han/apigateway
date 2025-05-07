package com.example.apigateway.common.file;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Profile("course")
@Slf4j
@Component
@RequiredArgsConstructor
public class FileSaveEventListener {
    @Value("${file.save.testcase.path}")
    private String PATH_PREFIX;

    @Async
    @EventListener
    public void signatureFileSaveEvent(TestcaseFileSaveEvent testcase) {
        try {
            Path dir = Paths.get(PATH_PREFIX + "/problem/" + testcase.getProblemId() + "/testcase");
            Files.createDirectories(dir);

            Path input = dir.resolve("input" + testcase.getNum() + ".txt");
            Path output = dir.resolve("output" + testcase.getNum() + ".txt");

            Files.write(input, testcase.getInputContent().getBytes());
            Files.write(output, testcase.getOutputContent().getBytes());
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", e.getMessage());
            throw new CustomException(CustomResponseException.INVALID_EXCEL_FILE);
        }
    }

}
