package com.example.apigateway.common.file;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileSaveEventListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void signatureFileSaveEvent(TestcaseFileSaveEvent testcase) {
        try {
            saveStringToFile(testcase.getInputContent(), "/problem/" + testcase.getProblemId() + "/testcase/input" + testcase.getNum() + ".txt");
            saveStringToFile(testcase.getOutputContent(), "/problem/" + testcase.getProblemId() + "/testcase/output" + testcase.getNum() + ".txt");
        } catch (IOException e) {
            log.error("Error saving user signature file: {}", e.getMessage());
            throw new CustomException(CustomResponseException.INVALID_EXCEL_FILE);
        }
    }

    public static void saveStringToFile(String content, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, content.getBytes());
    }
}
