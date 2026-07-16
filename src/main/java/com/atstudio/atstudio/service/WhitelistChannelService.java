package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.WhitelistChannelProperties;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
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
    private final WhitelistChannelProperties whitelistChannelProperties;

    // ── 12.1 POST /api/whitelist-channels ────────────────────────────────────

    @Transactional
    public WhitelistChannelResponse registerChannel(CustomUserDetails userDetails,
                                                     WhitelistChannelRequest request) {
        validateChannelUrl(request.channelUrl());

        User user = findUserByIdForUpdate(userDetails.getId());
        if (whitelistChannelRepository.countByUser(user)
                >= whitelistChannelProperties.getMaxSavedChannels()) {
            throw new BusinessException(BUSINESS_ERROR.WHITELIST_CHANNEL_LIMIT_EXCEEDED);
        }
        boolean firstChannel = !whitelistChannelRepository.existsByUserAndPrimaryTrue(user);

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
        return whitelistChannelRepository.findByUserOrderByPrimaryDescCreatedAtDesc(
                        user,
                        PageRequest.of(0, whitelistChannelProperties.getMaxSavedChannels()))
                .stream()
                .map(WhitelistChannelResponse::from)
                .toList();
    }

    // ── 12.3 PUT /api/whitelist-channels/{channelId} ─────────────────────────

    @Transactional
    public WhitelistChannelResponse updateChannel(CustomUserDetails userDetails,
                                                   Long channelId,
                                                   WhitelistChannelRequest request) {
        validateChannelUrl(request.channelUrl());

        findUserByIdForUpdate(userDetails.getId());
        WhitelistChannel channel = findChannelByIdForUpdate(channelId);
        checkOwnership(channel, userDetails.getId());
        WhitelistChannelStatus currentStatus = channel.getStatus();

        if (currentStatus == WhitelistChannelStatus.REMOVAL_REQUESTED
                || currentStatus == WhitelistChannelStatus.CANCELLED) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }

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
        findUserByIdForUpdate(userDetails.getId());
        WhitelistChannel channel = findChannelByIdForUpdate(channelId);
        checkOwnership(channel, userDetails.getId());

        WhitelistChannelStatus currentStatus = channel.getStatus();
        if (currentStatus == WhitelistChannelStatus.PENDING) {
            return WhitelistChannelResponse.from(channel);
        }

        if (currentStatus == WhitelistChannelStatus.CANCELLED
                || currentStatus == WhitelistChannelStatus.REMOVAL_REQUESTED
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
        User user = findUserByIdForUpdate(userDetails.getId());
        WhitelistChannel channel = findChannelByIdForUpdate(channelId);
        checkOwnership(channel, userDetails.getId());

        if (!channel.isPrimaryEligible()) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }
        if (channel.isPrimary()) {
            return WhitelistChannelResponse.from(channel);
        }

        whitelistChannelRepository.clearPrimaryByUserID(user.getId());
        channel.setPrimary(true);

        return WhitelistChannelResponse.from(channel);
    }

    // ── 12.6 DELETE /api/whitelist-channels/{channelId} ──────────────────────

    @Transactional
    public void deleteChannel(CustomUserDetails userDetails, Long channelId) {
        findUserByIdForUpdate(userDetails.getId());
        WhitelistChannel channel = findChannelByIdForUpdate(channelId);
        checkOwnership(channel, userDetails.getId());

        if (channel.canBeDeletedImmediately()) {
            promoteReplacementPrimaryIfNeeded(channel);
            whitelistChannelRepository.delete(channel);
            return;
        }

        if (channel.getStatus() == WhitelistChannelStatus.REMOVAL_REQUESTED) {
            return;
        }

        if (channel.getStatus() != WhitelistChannelStatus.EXPORTED
                && channel.getStatus() != WhitelistChannelStatus.REGISTERED) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }

        boolean wasPrimary = channel.isPrimary();
        channel.requestRemoval();
        if (wasPrimary) {
            promoteReplacementPrimary(channel);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void validateChannelUrl(String channelUrl) {
        try {
            URI uri = URI.create(channelUrl);
            String host = uri.getHost();
            String normalizedHost = host == null ? null : host.toLowerCase(Locale.ROOT);
            boolean allowedHost = normalizedHost != null
                    && (normalizedHost.equals("youtube.com")
                    || normalizedHost.endsWith(".youtube.com"));
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getUserInfo() != null
                    || (uri.getPort() != -1 && uri.getPort() != 443)
                    || !allowedHost) {
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

    private User findUserByIdForUpdate(Long userID) {
        return userRepository.findByIdForUpdate(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private WhitelistChannel findChannelByIdForUpdate(Long channelID) {
        return whitelistChannelRepository.findByIdForUpdate(channelID)
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

        promoteReplacementPrimary(channel);
    }

    private void promoteReplacementPrimary(WhitelistChannel channel) {
        channel.setPrimary(false);
        whitelistChannelRepository.clearPrimaryByUserID(channel.getUser().getId());
        whitelistChannelRepository.findPrimaryReplacement(
                        channel.getUser(),
                        channel.getId(),
                        PageRequest.of(0, 1))
                .stream()
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
