package com.atstudio.atstudio.common.validation;

/**
 * Centralized validation rules — single source of truth for backend.
 * Frontend mirror: frontend/src/utils/validation.ts
 */
public final class ValidationConstants {

    private ValidationConstants() {}

    // ── User ──
    public static final int NICKNAME_MIN = 2;
    public static final int NICKNAME_MAX = 20;
    public static final String NICKNAME_PATTERN = "^[가-힣a-zA-Z0-9_]+$";

    public static final int PASSWORD_MIN = 8;
    public static final int PASSWORD_MAX = 100;

    public static final String PHONE_PATTERN = "^\\d{2,3}-\\d{3,4}-\\d{4}$";
    public static final int PHONE_MAX = 20;

    // ── Content ──
    public static final int TITLE_TRACK_MAX = 100;
    public static final int TITLE_ALBUM_MAX = 100;
    public static final int TITLE_PLAYLIST_MAX = 50;
    public static final int TITLE_NOTICE_MAX = 200;
    public static final int TITLE_QUESTION_MAX = 200;

    public static final int DESCRIPTION_MAX = 1000;

    public static final int TAG_NAME_MAX = 50;

    public static final int BPM_MIN = 1;
    public static final int BPM_MAX = 999;

    // ── File ──
    public static final long AUDIO_MAX_SIZE_BYTES = 50L * 1024 * 1024;
    public static final long IMAGE_MAX_SIZE_BYTES = 10L * 1024 * 1024;
    public static final long ATTACHMENT_MAX_SIZE_BYTES = 20L * 1024 * 1024;
    public static final int ATTACHMENT_MAX_COUNT = 5;
    public static final int TRACK_UPLOAD_MAX_COUNT = 20;

    // ── Company Certification ──
    public static final long CERT_DOC_MAX_SIZE_BYTES = 20L * 1024 * 1024;
    public static final int CERT_DOC_MAX_COUNT = 10;
    public static final java.util.Set<String> CERT_DOC_ALLOWED_EXTENSIONS =
            java.util.Set.of("pdf", "hwp", "hwpx", "doc", "docx", "jpg", "jpeg", "png");

    // ── Search ──
    public static final int SEARCH_KEYWORD_MAX = 100;

    // ── Whitelist Channel ──
    public static final int CHANNEL_NAME_MAX = 100;
    public static final String CHANNEL_URL_PATTERN = "^https?://.+";
}
