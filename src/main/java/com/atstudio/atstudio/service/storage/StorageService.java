package com.atstudio.atstudio.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String generateKey(String directory, String submittedFilename);

    void stage(StorageRoot root, String operationId, String finalKey, MultipartFile file);

    void promote(StorageRoot root, String operationId, String finalKey);

    StorageDeleteResult delete(StorageRoot root, String relativeKey);

    StorageDeleteResult deleteStaged(StorageRoot root, String operationId, String finalKey);

    Resource loadAsResource(StorageRoot root, String relativeKey);

    String getUrl(StorageRoot root, String relativeKey);
}
