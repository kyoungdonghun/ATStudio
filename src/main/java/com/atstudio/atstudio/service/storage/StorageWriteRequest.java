package com.atstudio.atstudio.service.storage;

import org.springframework.web.multipart.MultipartFile;

public record StorageWriteRequest(
        MultipartFile file,
        String directory,
        String oldKey
) {
    public static StorageWriteRequest create(MultipartFile file, String directory) {
        return new StorageWriteRequest(file, directory, null);
    }

    public static StorageWriteRequest replace(MultipartFile file, String directory, String oldKey) {
        return new StorageWriteRequest(file, directory, oldKey);
    }
}
