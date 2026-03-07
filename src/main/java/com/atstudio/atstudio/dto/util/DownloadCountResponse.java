package com.atstudio.atstudio.dto.util;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DownloadCountResponse(
        long todayDownloads,
        int dailyLimit,
        long remaining,
        LocalDateTime nextResetAt
) {
}
