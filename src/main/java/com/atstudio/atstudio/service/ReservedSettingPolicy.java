package com.atstudio.atstudio.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Generic setting keys are canonical ASCII; reject collation aliases before querying. */
public final class ReservedSettingPolicy {
    public static final String CATALOG_KEY = "listening.catalog.v1";
    private ReservedSettingPolicy() { }

    public static void requireGenericKey(String key) {
        if (key == null || !key.matches("[A-Za-z][A-Za-z0-9_.-]{0,99}") || key.replaceAll("[_.-]", "").toLowerCase(java.util.Locale.ROOT).startsWith("listening")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Reserved or noncanonical setting key");
        }
    }
}
