package com.atstudio.atstudio.dto.whitelist;

public record AdminWhitelistExportFile(
        Long batchId,
        String fileName,
        byte[] content
) {}
