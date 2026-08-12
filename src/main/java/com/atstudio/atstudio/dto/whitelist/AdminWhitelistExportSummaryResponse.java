package com.atstudio.atstudio.dto.whitelist;

import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;

import java.time.LocalDateTime;

public record AdminWhitelistExportSummaryResponse(
        Long batchId,
        String fileName,
        int itemCount,
        WhitelistChannelStatus status,
        String keyword,
        LocalDateTime createdAt
) {}
