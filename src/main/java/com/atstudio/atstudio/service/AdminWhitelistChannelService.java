package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.WhitelistExportProperties;
import com.atstudio.atstudio.dto.whitelist.AdminWhitelistChannelResponse;
import com.atstudio.atstudio.dto.whitelist.AdminWhitelistExportFile;
import com.atstudio.atstudio.dto.whitelist.AdminWhitelistExportRequest;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminWhitelistChannelService {

    private static final int ADMIN_PAGE_MAX_SIZE = 100;
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final Map<WhitelistChannelStatus, Set<WhitelistChannelStatus>> STATUS_TRANSITIONS = Map.of(
            WhitelistChannelStatus.DRAFT, Set.of(),
            WhitelistChannelStatus.PENDING, Set.of(
                    WhitelistChannelStatus.REGISTERED,
                    WhitelistChannelStatus.REVISION_REQUESTED,
                    WhitelistChannelStatus.REJECTED),
            WhitelistChannelStatus.EXPORTED, Set.of(
                    WhitelistChannelStatus.REGISTERED,
                    WhitelistChannelStatus.REVISION_REQUESTED,
                    WhitelistChannelStatus.REJECTED,
                    WhitelistChannelStatus.REMOVAL_REQUESTED),
            WhitelistChannelStatus.REGISTERED, Set.of(
                    WhitelistChannelStatus.REVISION_REQUESTED,
                    WhitelistChannelStatus.REMOVAL_REQUESTED),
            WhitelistChannelStatus.REVISION_REQUESTED, Set.of(
                    WhitelistChannelStatus.REGISTERED,
                    WhitelistChannelStatus.REJECTED),
            WhitelistChannelStatus.REJECTED, Set.of(),
            WhitelistChannelStatus.REMOVAL_REQUESTED, Set.of(WhitelistChannelStatus.CANCELLED),
            WhitelistChannelStatus.CANCELLED, Set.of());

    private final WhitelistChannelRepository whitelistChannelRepository;
    private final WhitelistExportBatchRepository whitelistExportBatchRepository;
    private final WhitelistExportItemRepository whitelistExportItemRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final WhitelistExportProperties whitelistExportProperties;

    static boolean isStatusTransitionAllowed(
            WhitelistChannelStatus source,
            WhitelistChannelStatus target
    ) {
        return source == target || STATUS_TRANSITIONS.getOrDefault(source, Set.of()).contains(target);
    }

    public ResponseDTO<AdminWhitelistChannelResponse> listChannels(
            WhitelistChannelStatus status,
            String keyword,
            int page,
            int size
    ) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(ADMIN_PAGE_MAX_SIZE, Math.max(1, size));
        String normalizedKeyword = normalizeBlank(keyword);

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
            Long channelID,
            CustomUserDetails userDetails,
            WhitelistChannelStatus targetStatus,
            String adminNote
    ) {
        WhitelistChannel initial = whitelistChannelRepository.findById(channelID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        userRepository.findByIdForUpdate(initial.getUser().getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        WhitelistChannel channel = whitelistChannelRepository.findByIdForUpdate(channelID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        User admin = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        WhitelistChannelStatus currentStatus = channel.getStatus();
        if (currentStatus == targetStatus) {
            return AdminWhitelistChannelResponse.from(channel, activeSubscription(channel.getUser()));
        }
        if (!isStatusTransitionAllowed(currentStatus, targetStatus)) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }

        boolean wasPrimary = channel.isPrimary();
        if (targetStatus == WhitelistChannelStatus.REMOVAL_REQUESTED) {
            channel.requestRemoval();
        }
        channel.updateAdminStatus(targetStatus, admin, normalizeBlank(adminNote));

        if (wasPrimary && !channel.isPrimaryEligible()) {
            promoteReplacementPrimary(channel);
        }

        return AdminWhitelistChannelResponse.from(channel, activeSubscription(channel.getUser()));
    }

    @Transactional
    public AdminWhitelistExportFile exportChannels(
            CustomUserDetails userDetails,
            AdminWhitelistExportRequest request
    ) {
        String keyword = normalizeBlank(request.keyword());
        if (request.status() == null && keyword == null) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }

        int maxItems = whitelistExportProperties.getMaxItems();
        List<WhitelistChannel> candidates = whitelistChannelRepository.findExportCandidates(
                request.status(),
                keyword,
                PageRequest.of(0, maxItems + 1));
        if (candidates.size() > maxItems) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }

        List<Long> candidateIDs = candidates.stream()
                .map(WhitelistChannel::getId)
                .toList();
        candidates.stream()
                .map(channel -> channel.getUser().getId())
                .distinct()
                .sorted()
                .forEach(userID -> userRepository.findByIdForUpdate(userID)
                        .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND)));

        List<WhitelistChannel> channels = candidateIDs.isEmpty()
                ? List.of()
                : whitelistChannelRepository.findAllByIdForUpdate(candidateIDs).stream()
                        .filter(channel -> matchesExportScope(channel, request.status(), keyword))
                        .toList();

        User admin = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        LocalDateTime exportedAt = LocalDateTime.now();
        String fileName = "whitelist-channels-" + FILE_TS.format(exportedAt) + ".csv";
        WhitelistExportBatch batch = whitelistExportBatchRepository.save(WhitelistExportBatch.builder()
                .fileName(fileName)
                .itemCount(channels.size())
                .exportedBy(admin)
                .note(normalizeBlank(request.note()))
                .statusFilter(request.status())
                .keywordFilter(keyword)
                .build());

        List<WhitelistExportItem> items = new ArrayList<>(channels.size());
        for (int index = 0; index < channels.size(); index++) {
            WhitelistChannel channel = channels.get(index);
            UserSubscription subscription = activeSubscription(channel.getUser());
            WhitelistChannelStatus statusAtExport = channel.getStatus();
            items.add(WhitelistExportItem.builder()
                    .batch(batch)
                    .whitelistChannel(channel)
                    .statusAtExport(statusAtExport)
                    .itemOrder(index + 1)
                    .channelIdSnapshot(channel.getId())
                    .userEmailSnapshot(channel.getUser().getEmail())
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

        return exportFile(batch, items);
    }

    public AdminWhitelistExportFile downloadExportBatch(Long batchID) {
        WhitelistExportBatch batch = whitelistExportBatchRepository.findById(batchID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        int maxItems = whitelistExportProperties.getMaxItems();
        if (batch.getItemCount() > maxItems) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        List<WhitelistExportItem> items = whitelistExportItemRepository.findImmutableBatchItems(
                batchID,
                PageRequest.of(0, maxItems + 1));
        if (items.size() != batch.getItemCount()) {
            throw new IllegalStateException("Whitelist export batch item count mismatch");
        }
        return exportFile(batch, items);
    }

    private AdminWhitelistExportFile exportFile(
            WhitelistExportBatch batch,
            List<WhitelistExportItem> items
    ) {
        List<CsvRow> rows = items.stream().map(CsvRow::from).toList();
        return new AdminWhitelistExportFile(
                batch.getId(),
                batch.getFileName(),
                csvWithRows(rows).getBytes(StandardCharsets.UTF_8));
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

    private UserSubscription activeSubscription(User user) {
        return userSubscriptionRepository.findActiveByUser(user, LocalDate.now()).orElse(null);
    }

    private boolean matchesExportScope(
            WhitelistChannel channel,
            WhitelistChannelStatus status,
            String keyword
    ) {
        if (status != null && channel.getStatus() != status) {
            return false;
        }
        if (keyword == null) {
            return true;
        }

        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        User user = channel.getUser();
        return containsIgnoreCase(user.getEmail(), normalizedKeyword)
                || containsIgnoreCase(user.getNickname(), normalizedKeyword)
                || containsIgnoreCase(channel.getChannelName(), normalizedKeyword)
                || containsIgnoreCase(channel.getChannelUrl(), normalizedKeyword)
                || containsIgnoreCase(channel.getYoutubeHandle(), normalizedKeyword)
                || containsIgnoreCase(channel.getYoutubeChannelId(), normalizedKeyword);
    }

    private boolean containsIgnoreCase(String value, String normalizedKeyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedKeyword);
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String csvWithRows(List<CsvRow> rows) {
        StringBuilder csv = new StringBuilder();
        csv.append('\uFEFF');
        csv.append("requestId,userEmail,channelName,youtubeHandle,channelUrl,youtubeChannelId,")
                .append("requestedAt,planName,billingCycle,exportedAt\n");
        for (CsvRow row : rows) {
            csv.append(csv(row.requestID())).append(',')
                    .append(csvText(row.userEmail())).append(',')
                    .append(csvText(row.channelName())).append(',')
                    .append(csvText(row.youtubeHandle())).append(',')
                    .append(csvText(row.channelUrl())).append(',')
                    .append(csvText(row.youtubeChannelID())).append(',')
                    .append(csv(row.requestedAt())).append(',')
                    .append(csvText(row.planName())).append(',')
                    .append(csv(row.billingCycle())).append(',')
                    .append(csv(row.exportedAt())).append('\n');
        }
        return csv.toString();
    }

    private String csvText(String value) {
        return csv(neutralizeFormulaCell(value));
    }

    private String neutralizeFormulaCell(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        char first = value.charAt(0);
        if (first == '\t' || first == '\r' || first == '\n') {
            return "'" + value;
        }

        int effectiveIndex = 0;
        while (effectiveIndex < value.length()) {
            char current = value.charAt(effectiveIndex);
            if (current != '\uFEFF' && current != ' ' && current != '\t') {
                break;
            }
            effectiveIndex++;
        }

        if (effectiveIndex < value.length() && isFormulaPrefix(value.charAt(effectiveIndex))) {
            return "'" + value;
        }
        return value;
    }

    private boolean isFormulaPrefix(char value) {
        return value == '=' || value == '+' || value == '-' || value == '@';
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private record CsvRow(
            Long requestID,
            String userEmail,
            String channelName,
            String youtubeHandle,
            String channelUrl,
            String youtubeChannelID,
            LocalDateTime requestedAt,
            String planName,
            Object billingCycle,
            LocalDateTime exportedAt
    ) {
        static CsvRow from(WhitelistExportItem item) {
            return new CsvRow(
                    item.getChannelIdSnapshot(),
                    item.getUserEmailSnapshot(),
                    item.getChannelNameSnapshot(),
                    item.getYoutubeHandleSnapshot(),
                    item.getChannelUrlSnapshot(),
                    item.getYoutubeChannelIdSnapshot(),
                    item.getRequestedAtSnapshot(),
                    item.getPlanNameSnapshot(),
                    item.getBillingCycleSnapshot(),
                    item.getExportedAtSnapshot());
        }
    }
}
