package com.example.apigateway.common.file;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

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

}
