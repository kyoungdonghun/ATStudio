package com.atstudio.atstudio.service.storage;

import com.atstudio.atstudio.common.exception.TECHNIC_ERROR;
import com.atstudio.atstudio.common.exception.TechnicException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LocalStorageService implements StorageService {

    @Value("${app.storage.base-path:uploads}")
    private String basePath;

    private Path resolvedBasePath;

    @jakarta.annotation.PostConstruct
    void init() {
        Path candidate = Paths.get(basePath).toAbsolutePath().normalize();
        if (!Files.isDirectory(candidate)) {
            // Gradle bootRun may set working dir to a subdirectory; try parent paths
            Path current = Paths.get("").toAbsolutePath();
            for (int i = 0; i < 3; i++) {
                Path alt = current.resolve(basePath).normalize();
                if (Files.isDirectory(alt)) {
                    candidate = alt;
                    break;
                }
                current = current.getParent();
                if (current == null) break;
            }
        }
        resolvedBasePath = candidate;
        log.info("StorageService base path resolved to: {} (exists: {})", resolvedBasePath, Files.isDirectory(resolvedBasePath));
    }

    @Override
    public String store(MultipartFile file, String directory) {
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String sanitized = Paths.get(originalFilename).getFileName().toString();
        String filename = UUID.randomUUID().toString().replace("-", "").substring(0, 8)
                + "_" + sanitized;
        String relativePath = directory + "/" + filename;

        try {
            Path targetDir = resolvedBasePath.resolve(directory).normalize();
            validateInsideBasePath(targetDir);
            Files.createDirectories(targetDir);

            Path targetFile = targetDir.resolve(filename).normalize();
            validateInsideBasePath(targetFile);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
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
            Path target = resolvedBasePath.resolve(relativePath).normalize();
            validateInsideBasePath(target);
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("파일 삭제 실패: {}", relativePath, e);
        }
    }

    @Override
    public Resource loadAsResource(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new TechnicException(TECHNIC_ERROR.IO_EXCEPTION);
        }
        // Strip leading base directory name if accidentally included (e.g. "uploads/tracks/..." → "tracks/...")
        String baseDir = resolvedBasePath.getFileName().toString() + "/";
        if (relativePath.startsWith(baseDir)) {
            relativePath = relativePath.substring(baseDir.length());
        }
        try {
            Path file = resolvedBasePath.resolve(relativePath).normalize();
            validateInsideBasePath(file);
            log.debug("Loading resource: {} (exists: {})", file, Files.exists(file));
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists()) {
                log.warn("File not found at resolved path: {}", file);
                throw new TechnicException(TECHNIC_ERROR.IO_EXCEPTION);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new TechnicException(TECHNIC_ERROR.IO_EXCEPTION);
        }
    }

    private void validateInsideBasePath(Path target) {
        if (!target.startsWith(resolvedBasePath)) {
            throw new TechnicException(TECHNIC_ERROR.IO_EXCEPTION);
        }
    }
}
