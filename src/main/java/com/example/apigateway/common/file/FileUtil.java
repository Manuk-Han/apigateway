package com.example.apigateway.common.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.UUID;

@Slf4j
@Component
public class FileUtil {
    public Mono<String> save(FilePart file, String filePath) {
        return Mono.fromCallable(() -> {
                    String baseDir = System.getProperty("user.dir") + filePath;
                    File directory = new File(baseDir);
                    if (!directory.exists() && !directory.mkdirs()) {
                        throw new IOException("디렉터리 생성 실패: " + baseDir);
                    }

                    String originalFileName = file.filename();
                    String extension = "";
                    if (originalFileName.contains(".")) {
                        extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                    }

                    String fileName = UUID.randomUUID() + extension;
                    File saveFile = new File(directory, fileName);

                    return new FileSaveContext(saveFile, baseDir + "/" + fileName);
                })
                .flatMap(ctx -> file.transferTo(ctx.file)
                        .thenReturn(ctx.fullPath)
                )
                .subscribeOn(Schedulers.boundedElastic());
    }

    private static class FileSaveContext {
        File file;
        String fullPath;

        FileSaveContext(File file, String fullPath) {
            this.file = file;
            this.fullPath = fullPath;
        }
    }

    public static String getFilePath(String filePath, String originalFileName) throws IOException {
        String path = System.getProperty("user.dir") + filePath;

        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID() + extension;
        new File(path, fileName);

        return path + "/" + fileName;
    }

    public void deleteOldTestcaseFiles(String path) {
        Path dir = Paths.get(path);

        try {
            if (Files.exists(dir)) {
                Files.walk(dir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch (IOException e) {
            log.error("기존 테스트케이스 파일 삭제 실패: {}", e.getMessage());
        }
    }
}
