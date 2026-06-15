package com.atstudio.atstudio.dto.whitelist;

import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.WhitelistChannel;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;

import java.time.LocalDateTime;

public record AdminWhitelistChannelResponse(
        Long id,
        Long userId,
        String userEmail,
        String userNickname,
        String channelUrl,
        String channelName,
        String youtubeHandle,
        String youtubeChannelId,
        WhitelistChannelStatus status,
        boolean primary,
        String adminNote,
        String processedByEmail,
        String planName,
        BillingCycle billingCycle,
        LocalDateTime requestedAt,
        LocalDateTime exportedAt,
        LocalDateTime processedAt,
        LocalDateTime removalRequestedAt,
        LocalDateTime createdAt
) {
    public static AdminWhitelistChannelResponse from(
            WhitelistChannel channel,
            UserSubscription activeSubscription
    ) {
        return new AdminWhitelistChannelResponse(
                channel.getId(),
                channel.getUser().getId(),
                channel.getUser().getEmail(),
                channel.getUser().getNickname(),
                channel.getChannelUrl(),
                channel.getChannelName(),
                channel.getYoutubeHandle(),
                channel.getYoutubeChannelId(),
                channel.getStatus(),
                channel.isPrimary(),
                channel.getAdminNote(),
                channel.getProcessedBy() != null ? channel.getProcessedBy().getEmail() : null,
                activeSubscription != null ? activeSubscription.getSubscription().getName() : null,
                activeSubscription != null ? activeSubscription.getBillingCycle() : null,
                channel.getRequestedAt(),
                channel.getExportedAt(),
                channel.getProcessedAt(),
                channel.getRemovalRequestedAt(),
                channel.getCreatedAt()
        );
    }
}
