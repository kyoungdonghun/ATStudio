package com.atstudio.atstudio.service.storage;

public enum StorageDeleteResult {
    DELETED,
    NOT_FOUND,
    DEFERRED,
    FAILED;

    public boolean isSuccess() {
        return this == DELETED || this == NOT_FOUND;
    }
}
