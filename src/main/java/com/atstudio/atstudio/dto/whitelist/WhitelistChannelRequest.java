package com.atstudio.atstudio.dto.whitelist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WhitelistChannelRequest(
        @NotBlank @Size(max = 255) String channelUrl,
        @NotBlank @Size(max = 100) String channelName,
        @Size(max = 100) String youtubeHandle,
        @Size(max = 100) String youtubeChannelId
) {
    public WhitelistChannelRequest(
            String channelUrl,
            String channelName
    ) {
        this(channelUrl, channelName, null, null);
    }
}
