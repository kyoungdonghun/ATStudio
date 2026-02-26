package com.atstudio.atstudio.dto.whitelist;

import com.atstudio.atstudio.entity.WhitelistChannel;

import java.time.LocalDateTime;

public record WhitelistChannelResponse(
        Long id,
        String channelUrl,
        String channelName,
        LocalDateTime createdAt
) {
    public static WhitelistChannelResponse from(WhitelistChannel channel) {
        return new WhitelistChannelResponse(
                channel.getId(),
                channel.getChannelUrl(),
                channel.getChannelName(),
                channel.getCreatedAt()
        );
    }
}
