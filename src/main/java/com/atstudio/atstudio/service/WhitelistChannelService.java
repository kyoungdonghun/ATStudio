package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.whitelist.WhitelistChannelRequest;
import com.atstudio.atstudio.dto.whitelist.WhitelistChannelResponse;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.WhitelistChannel;
import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.repository.WhitelistChannelRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WhitelistChannelService {

    private static final Set<WhitelistChannelStatus> PLAN_LIMIT_STATUSES = Set.of(
            WhitelistChannelStatus.PENDING,
            WhitelistChannelStatus.EXPORTED,
            WhitelistChannelStatus.REGISTERED,
            WhitelistChannelStatus.REVISION_REQUESTED,
            WhitelistChannelStatus.REMOVAL_REQUESTED
    );

    private final WhitelistChannelRepository whitelistChannelRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    // ── 12.1 POST /api/whitelist-channels ────────────────────────────────────

    @Transactional
    public WhitelistChannelResponse registerChannel(CustomUserDetails userDetails,
                                                     WhitelistChannelRequest request) {
        validateChannelUrl(request.channelUrl());

        User user = findUserById(userDetails.getId());
        boolean firstChannel = whitelistChannelRepository.countByUser(user) == 0L;

        WhitelistChannel channel = WhitelistChannel.builder()
                .user(user)
                .channelUrl(request.channelUrl())
                .channelName(request.channelName())
                .youtubeHandle(normalizeBlank(request.youtubeHandle()))
                .youtubeChannelId(normalizeBlank(request.youtubeChannelId()))
                .primary(firstChannel)
                .build();
        channel = whitelistChannelRepository.save(channel);

        return WhitelistChannelResponse.from(channel);
    }

    // ── 12.2 GET /api/whitelist-channels ─────────────────────────────────────

    public List<WhitelistChannelResponse> getMyChannels(CustomUserDetails userDetails) {
        User user = findUserById(userDetails.getId());
        return whitelistChannelRepository.findByUserOrderByPrimaryDescCreatedAtDesc(user).stream()
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
        WhitelistChannelStatus currentStatus = channel.getStatus();

        if (requiresReprocessingOnUpdate(currentStatus)) {
            ensureCanEnterPending(channel, currentStatus);
        }

        channel.update(
                request.channelUrl(),
                request.channelName(),
                normalizeBlank(request.youtubeHandle()),
                normalizeBlank(request.youtubeChannelId()));

        return WhitelistChannelResponse.from(channel);
    }

    @Transactional
    public WhitelistChannelResponse requestRegistration(CustomUserDetails userDetails, Long channelId) {
        WhitelistChannel channel = findChannelById(channelId);
        checkOwnership(channel, userDetails.getId());

        WhitelistChannelStatus currentStatus = channel.getStatus();
        if (currentStatus == WhitelistChannelStatus.PENDING) {
            return WhitelistChannelResponse.from(channel);
        }

        if (currentStatus == WhitelistChannelStatus.REMOVAL_REQUESTED
                || currentStatus == WhitelistChannelStatus.EXPORTED
                || currentStatus == WhitelistChannelStatus.REGISTERED) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }

        ensureCanEnterPending(channel, currentStatus);
        channel.requestRegistration();
        return WhitelistChannelResponse.from(channel);
    }

    private void ensureCanEnterPending(WhitelistChannel channel, WhitelistChannelStatus currentStatus) {
        User user = channel.getUser();
        UserSubscription subscription = userSubscriptionRepository
                .findActiveByUser(user, LocalDate.now())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));

        long currentCount = whitelistChannelRepository.countByUserAndStatusIn(user, PLAN_LIMIT_STATUSES);
        if (WhitelistChannel.countsAgainstPlanLimit(currentStatus)) {
            currentCount = Math.max(0L, currentCount - 1L);
        }
        int maxChannels = subscription.getSubscription().getMaxWhitelistChannels();
        if (currentCount >= maxChannels) {
            throw new BusinessException(BUSINESS_ERROR.WHITELIST_CHANNEL_LIMIT_EXCEEDED);
        }
    }

    @Transactional
    public WhitelistChannelResponse setPrimary(CustomUserDetails userDetails, Long channelId) {
        WhitelistChannel channel = findChannelById(channelId);
        checkOwnership(channel, userDetails.getId());

        User user = channel.getUser();
        whitelistChannelRepository.findByUserAndPrimaryTrue(user)
                .ifPresent(current -> current.setPrimary(false));
        channel.setPrimary(true);

        return WhitelistChannelResponse.from(channel);
    }

    // ── 12.6 DELETE /api/whitelist-channels/{channelId} ──────────────────────

    @Transactional
    public void deleteChannel(CustomUserDetails userDetails, Long channelId) {
        WhitelistChannel channel = findChannelById(channelId);
        checkOwnership(channel, userDetails.getId());

        if (channel.canBeDeletedImmediately()) {
            promoteReplacementPrimaryIfNeeded(channel);
            whitelistChannelRepository.delete(channel);
            return;
        }

        channel.requestRemoval();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void validateChannelUrl(String channelUrl) {
        try {
            URI uri = URI.create(channelUrl);
            String host = uri.getHost();
            if (host == null
                    || !(host.equals("youtube.com") || host.endsWith(".youtube.com"))) {
                throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
            }
        } catch (IllegalArgumentException e) {
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

    private void promoteReplacementPrimaryIfNeeded(WhitelistChannel channel) {
        if (!channel.isPrimary()) {
            return;
        }

        whitelistChannelRepository.findByUserOrderByCreatedAtDesc(channel.getUser()).stream()
                .filter(candidate -> !candidate.getId().equals(channel.getId()))
                .findFirst()
                .ifPresent(candidate -> candidate.setPrimary(true));
    }

    private boolean requiresReprocessingOnUpdate(WhitelistChannelStatus status) {
        return status == WhitelistChannelStatus.REGISTERED
                || status == WhitelistChannelStatus.EXPORTED
                || status == WhitelistChannelStatus.REVISION_REQUESTED;
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
