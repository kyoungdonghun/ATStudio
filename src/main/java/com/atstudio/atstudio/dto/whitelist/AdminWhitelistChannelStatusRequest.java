package com.atstudio.atstudio.dto.whitelist;

import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminWhitelistChannelStatusRequest(
        @NotNull WhitelistChannelStatus status,
        @Size(max = 500) String adminNote
) {}
