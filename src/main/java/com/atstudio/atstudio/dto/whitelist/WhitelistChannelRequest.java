package com.atstudio.atstudio.dto.whitelist;

import jakarta.validation.constraints.NotBlank;

public record WhitelistChannelRequest(
        @NotBlank String channelUrl,
        @NotBlank String channelName
) {}
