package com.atstudio.atstudio.service.storage;

public enum StorageDeleteResult {
    DELETED,
    NOT_FOUND,
    FAILED;

    public boolean isSuccess() {
        return this != FAILED;
    }
}
