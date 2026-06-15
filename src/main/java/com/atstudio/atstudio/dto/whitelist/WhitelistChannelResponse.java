package com.atstudio.atstudio.dto.whitelist;

import com.atstudio.atstudio.entity.WhitelistChannel;
import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;

import java.time.LocalDateTime;

public record WhitelistChannelResponse(
        Long id,
        String channelUrl,
        String channelName,
        String youtubeHandle,
        String youtubeChannelId,
        WhitelistChannelStatus status,
        boolean primary,
        String adminNote,
        LocalDateTime requestedAt,
        LocalDateTime exportedAt,
        LocalDateTime processedAt,
        LocalDateTime removalRequestedAt,
        LocalDateTime createdAt
) {
    public WhitelistChannelResponse(
            Long id,
            String channelUrl,
            String channelName,
            LocalDateTime createdAt
    ) {
        this(
                id,
                channelUrl,
                channelName,
                null,
                null,
                WhitelistChannelStatus.DRAFT,
                false,
                null,
                null,
                null,
                null,
                null,
                createdAt);
    }

    public static WhitelistChannelResponse from(WhitelistChannel channel) {
        return new WhitelistChannelResponse(
                channel.getId(),
                channel.getChannelUrl(),
                channel.getChannelName(),
                channel.getYoutubeHandle(),
                channel.getYoutubeChannelId(),
                channel.getStatus(),
                channel.isPrimary(),
                channel.getAdminNote(),
                channel.getRequestedAt(),
                channel.getExportedAt(),
                channel.getProcessedAt(),
                channel.getRemovalRequestedAt(),
                channel.getCreatedAt()
        );
    }
}
