package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.whitelist.AdminWhitelistChannelResponse;
import com.atstudio.atstudio.dto.whitelist.AdminWhitelistExportFile;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.WhitelistChannel;
import com.atstudio.atstudio.entity.WhitelistExportBatch;
import com.atstudio.atstudio.entity.WhitelistExportItem;
import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.repository.WhitelistChannelRepository;
import com.atstudio.atstudio.repository.WhitelistExportBatchRepository;
import com.atstudio.atstudio.repository.WhitelistExportItemRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminWhitelistChannelService {

    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final Set<WhitelistChannelStatus> ADMIN_MUTABLE_STATUSES = Set.of(
            WhitelistChannelStatus.REGISTERED,
            WhitelistChannelStatus.REVISION_REQUESTED,
            WhitelistChannelStatus.REJECTED,
            WhitelistChannelStatus.REMOVAL_REQUESTED,
            WhitelistChannelStatus.CANCELLED
    );

    private final WhitelistChannelRepository whitelistChannelRepository;
    private final WhitelistExportBatchRepository whitelistExportBatchRepository;
    private final WhitelistExportItemRepository whitelistExportItemRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    public ResponseDTO<AdminWhitelistChannelResponse> listChannels(
            WhitelistChannelStatus status,
            String keyword,
            int page,
            int size
    ) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, size);
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();

        Page<WhitelistChannel> channelPage = whitelistChannelRepository.searchForAdmin(
                status,
                normalizedKeyword,
                PageRequest.of(safePage - 1, safeSize));

        List<AdminWhitelistChannelResponse> responses = channelPage.getContent().stream()
                .map(channel -> AdminWhitelistChannelResponse.from(channel, activeSubscription(channel.getUser())))
                .toList();

        return ResponseDTO.<AdminWhitelistChannelResponse>withAll()
                .dataList(responses)
                .pageInfo(PageInfo.of(safePage, safeSize, (int) channelPage.getTotalElements(), 10))
                .build();
    }

    @Transactional
    public AdminWhitelistChannelResponse updateStatus(
            Long channelId,
            CustomUserDetails userDetails,
            WhitelistChannelStatus status,
            String adminNote
    ) {
        WhitelistChannel channel = whitelistChannelRepository.findById(channelId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        User admin = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        if (!ADMIN_MUTABLE_STATUSES.contains(status)) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }

        if (status == WhitelistChannelStatus.REMOVAL_REQUESTED) {
            channel.requestRemoval();
        }
        channel.updateAdminStatus(status, admin, normalizeBlank(adminNote));

        return AdminWhitelistChannelResponse.from(channel, activeSubscription(channel.getUser()));
    }

    @Transactional
    public AdminWhitelistExportFile exportChannels(
            CustomUserDetails userDetails,
            WhitelistChannelStatus status,
            String note
    ) {
        WhitelistChannelStatus exportStatus = status == null ? WhitelistChannelStatus.PENDING : status;
        List<WhitelistChannel> channels = whitelistChannelRepository.findByStatusOrderByRequestedAtAsc(exportStatus);
        String fileName = "whitelist-channels-" + FILE_TS.format(LocalDateTime.now()) + ".csv";

        if (channels.isEmpty()) {
            return new AdminWhitelistExportFile(fileName, csvWithRows(List.of()).getBytes(StandardCharsets.UTF_8));
        }

        User admin = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        WhitelistExportBatch batch = whitelistExportBatchRepository.save(WhitelistExportBatch.builder()
                .fileName(fileName)
                .itemCount(channels.size())
                .exportedBy(admin)
                .note(normalizeBlank(note))
                .build());

        List<CsvRow> rows = new ArrayList<>();
        List<WhitelistExportItem> items = new ArrayList<>();
        LocalDateTime exportedAt = LocalDateTime.now();
        for (WhitelistChannel channel : channels) {
            UserSubscription subscription = activeSubscription(channel.getUser());
            WhitelistChannelStatus statusAtExport = channel.getStatus();
            CsvRow row = CsvRow.from(channel, subscription, exportedAt);
            rows.add(row);
            items.add(WhitelistExportItem.builder()
                    .batch(batch)
                    .whitelistChannel(channel)
                    .statusAtExport(statusAtExport)
                    .userIdSnapshot(channel.getUser().getId())
                    .userEmailSnapshot(channel.getUser().getEmail())
                    .userNicknameSnapshot(channel.getUser().getNickname())
                    .channelNameSnapshot(channel.getChannelName())
                    .youtubeHandleSnapshot(channel.getYoutubeHandle())
                    .channelUrlSnapshot(channel.getChannelUrl())
                    .youtubeChannelIdSnapshot(channel.getYoutubeChannelId())
                    .planNameSnapshot(subscription != null ? subscription.getSubscription().getName() : null)
                    .billingCycleSnapshot(subscription != null ? subscription.getBillingCycle() : null)
                    .requestedAtSnapshot(channel.getRequestedAt())
                    .exportedAtSnapshot(exportedAt)
                    .build());
            if (statusAtExport == WhitelistChannelStatus.PENDING) {
                channel.markExported();
            }
        }
        whitelistExportItemRepository.saveAll(items);

        return new AdminWhitelistExportFile(fileName, csvWithRows(rows).getBytes(StandardCharsets.UTF_8));
    }

    private UserSubscription activeSubscription(User user) {
        return userSubscriptionRepository.findActiveByUser(user, LocalDate.now()).orElse(null);
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String csvWithRows(List<CsvRow> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("requestId,userId,userEmail,userNickname,channelName,youtubeHandle,channelUrl,youtubeChannelId,requestedAt,planName,billingCycle,exportedAt\n");
        for (CsvRow row : rows) {
            sb.append(csv(row.requestId())).append(',')
                    .append(csv(row.userId())).append(',')
                    .append(csv(row.userEmail())).append(',')
                    .append(csv(row.userNickname())).append(',')
                    .append(csv(row.channelName())).append(',')
                    .append(csv(row.youtubeHandle())).append(',')
                    .append(csv(row.channelUrl())).append(',')
                    .append(csv(row.youtubeChannelId())).append(',')
                    .append(csv(row.requestedAt())).append(',')
                    .append(csv(row.planName())).append(',')
                    .append(csv(row.billingCycle())).append(',')
                    .append(csv(row.exportedAt())).append('\n');
        }
        return sb.toString();
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private record CsvRow(
            Long requestId,
            Long userId,
            String userEmail,
            String userNickname,
            String channelName,
            String youtubeHandle,
            String channelUrl,
            String youtubeChannelId,
            LocalDateTime requestedAt,
            String planName,
            Object billingCycle,
            LocalDateTime exportedAt
    ) {
        static CsvRow from(
                WhitelistChannel channel,
                UserSubscription subscription,
                LocalDateTime exportedAt
        ) {
            return new CsvRow(
                    channel.getId(),
                    channel.getUser().getId(),
                    channel.getUser().getEmail(),
                    channel.getUser().getNickname(),
                    channel.getChannelName(),
                    channel.getYoutubeHandle(),
                    channel.getChannelUrl(),
                    channel.getYoutubeChannelId(),
                    channel.getRequestedAt(),
                    subscription != null ? subscription.getSubscription().getName() : null,
                    subscription != null ? subscription.getBillingCycle() : null,
                    exportedAt
            );
        }
    }
}
