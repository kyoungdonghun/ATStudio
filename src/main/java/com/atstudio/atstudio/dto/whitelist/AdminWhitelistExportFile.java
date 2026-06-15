package com.atstudio.atstudio.dto.whitelist;

public record AdminWhitelistExportFile(
        String fileName,
        byte[] content
) {}
