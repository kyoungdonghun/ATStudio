package com.atstudio.atstudio.service.storage;

import com.atstudio.atstudio.common.exception.TECHNIC_ERROR;
import com.atstudio.atstudio.common.exception.TechnicException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    @Value("${app.storage.base-path:uploads}")
    private String basePath;

    @Override
    public String store(MultipartFile file, String directory) {
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String filename = UUID.randomUUID().toString().replace("-", "").substring(0, 8)
                + "_" + originalFilename;
        String relativePath = directory + "/" + filename;

        try {
            Path targetDir = Paths.get(basePath, directory);
            Files.createDirectories(targetDir);
            file.transferTo(targetDir.resolve(filename).toFile());
        } catch (IOException e) {
            throw new TechnicException(TECHNIC_ERROR.IO_EXCEPTION);
        }

        return relativePath;
    }

    @Override
    public String getUrl(String relativePath) {
        return "/uploads/" + relativePath;
    }

    @Override
    public void delete(String relativePath) {
        if (relativePath == null) return;
        try {
            Path target = Paths.get(basePath, relativePath);
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
        }
    }
}
