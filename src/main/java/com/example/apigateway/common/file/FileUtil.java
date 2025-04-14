package com.example.apigateway.common.file;

import com.example.apigateway.common.type.TestCaseType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FileUtil {

    public String save(MultipartFile file, String filePath) throws IOException {
        String baseDir = System.getProperty("user.dir") + filePath;
        File directory = new File(baseDir);
        if (!directory.exists()) {
            if (!directory.mkdirs())
                throw new IOException("디렉터리 생성 실패: " + baseDir);
        }

        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID() + extension;
        File saveFile = new File(directory, fileName);
        file.transferTo(saveFile);

        return baseDir + "/" + fileName;
    }

    public String getFilePath(String filePath, String originalFileName) throws IOException {
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
