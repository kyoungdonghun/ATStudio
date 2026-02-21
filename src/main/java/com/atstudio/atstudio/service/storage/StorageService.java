package com.atstudio.atstudio.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String store(MultipartFile file, String directory);
    String getUrl(String relativePath);
    void delete(String relativePath);
    Resource loadAsResource(String relativePath);
}
