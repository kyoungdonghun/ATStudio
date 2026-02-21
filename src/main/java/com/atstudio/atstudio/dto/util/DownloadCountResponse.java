package com.atstudio.atstudio.dto.util;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DownloadCountResponse(
        long todayDownloads,
        int dailyLimit,
        long remaining
) {
}
