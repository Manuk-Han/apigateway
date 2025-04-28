package com.example.apigateway.common.file;

import com.example.apigateway.common.exception.CustomException;
import com.example.apigateway.common.exception.CustomResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Profile("class")
@Slf4j
@Component
@RequiredArgsConstructor
public class FileSaveEventListener {
    @Value("${file.testcase.path}")
    private static String PATH_PREFIX;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void signatureFileSaveEvent(TestcaseFileSaveEvent testcase) {
        try {
            saveStringToFile(testcase.getInputContent(), testcase.getProblemId(), testcase.getNum());
            saveStringToFile(testcase.getOutputContent(), testcase.getProblemId(), testcase.getNum());
        } catch (IOException e) {
            log.error("Error saving user signature file: {}", e.getMessage());
            throw new CustomException(CustomResponseException.INVALID_EXCEL_FILE);
        }
    }

    public static void saveStringToFile(String content, Long problemId, int num) throws IOException {
        Path path = Paths.get(PATH_PREFIX + "/problem/" + problemId + "/testcase/input" + num + ".txt");
        Files.write(path, content.getBytes());
    }
}
