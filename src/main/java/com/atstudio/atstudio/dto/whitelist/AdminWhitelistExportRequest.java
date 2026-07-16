package com.atstudio.atstudio.dto.whitelist;

import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;
import jakarta.validation.constraints.Size;

public record AdminWhitelistExportRequest(
        WhitelistChannelStatus status,
        @Size(max = 100) String keyword,
        @Size(max = 500) String note
) {}
