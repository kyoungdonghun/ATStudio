package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.whitelist.WhitelistChannelRequest;
import com.atstudio.atstudio.dto.whitelist.WhitelistChannelResponse;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.WhitelistChannel;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.repository.WhitelistChannelRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WhitelistChannelService {

    private final WhitelistChannelRepository whitelistChannelRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    // ── 12.1 POST /api/whitelist-channels ────────────────────────────────────

    @Transactional
    public WhitelistChannelResponse registerChannel(CustomUserDetails userDetails,
                                                     WhitelistChannelRequest request) {
        validateChannelUrl(request.channelUrl());

        User user = findUserById(userDetails.getId());

        UserSubscription subscription = userSubscriptionRepository
                .findActiveByUser(user, SubscriptionStatus.ACTIVE, LocalDate.now())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));

        long currentCount = whitelistChannelRepository.countByUser(user);
        int maxChannels = subscription.getSubscription().getMaxWhitelistChannels();
        if (currentCount >= maxChannels) {
            throw new BusinessException(BUSINESS_ERROR.WHITELIST_CHANNEL_LIMIT_EXCEEDED);
        }

        WhitelistChannel channel = WhitelistChannel.builder()
                .user(user)
                .channelUrl(request.channelUrl())
                .channelName(request.channelName())
                .build();
        channel = whitelistChannelRepository.save(channel);

        return WhitelistChannelResponse.from(channel);
    }

    // ── 12.2 GET /api/whitelist-channels ─────────────────────────────────────

    public List<WhitelistChannelResponse> getMyChannels(CustomUserDetails userDetails) {
        User user = findUserById(userDetails.getId());
        return whitelistChannelRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(WhitelistChannelResponse::from)
                .toList();
    }

    // ── 12.3 PUT /api/whitelist-channels/{channelId} ─────────────────────────

    @Transactional
    public WhitelistChannelResponse updateChannel(CustomUserDetails userDetails,
                                                   Long channelId,
                                                   WhitelistChannelRequest request) {
        validateChannelUrl(request.channelUrl());

        WhitelistChannel channel = findChannelById(channelId);
        checkOwnership(channel, userDetails.getId());

        channel.update(request.channelUrl(), request.channelName());

        return WhitelistChannelResponse.from(channel);
    }

    // ── 12.4 DELETE /api/whitelist-channels/{channelId} ──────────────────────

    @Transactional
    public void deleteChannel(CustomUserDetails userDetails, Long channelId) {
        WhitelistChannel channel = findChannelById(channelId);
        checkOwnership(channel, userDetails.getId());

        whitelistChannelRepository.delete(channel);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void validateChannelUrl(String channelUrl) {
        if (!channelUrl.contains("youtube.com")) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private WhitelistChannel findChannelById(Long channelId) {
        return whitelistChannelRepository.findById(channelId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private void checkOwnership(WhitelistChannel channel, Long userId) {
        if (!channel.getUser().getId().equals(userId)) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }
    }
}
