package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.download.DownloadHistoryItemResponse;
import com.atstudio.atstudio.entity.*;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.repository.*;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DownloadService {

    private final TrackRepository trackRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final TrackDownloadRepository trackDownloadRepository;
    private final LicenseRepository licenseRepository;
    private final StorageService storageService;

    @Transactional
    public Resource download(Long trackId, CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        Track track = trackRepository.findById(trackId)
                .filter(Track::isActive)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND));

        // Re-download: if license already exists, skip count & just return file
        boolean isRedownload = licenseRepository.findByUserAndTrack(user, track).isPresent();

        if (!isRedownload) {
            // ADMIN bypasses subscription and daily limit checks
            if (user.getRole() != UserRole.ADMIN) {
                UserSubscription subscription = userSubscriptionRepository
                        .findActiveByUser(user, LocalDate.now())
                        .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));

                LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
                LocalDateTime endOfDay = startOfDay.plusDays(1);
                long todayCount = trackDownloadRepository.countByUserAndDownloadedAtBetween(user, startOfDay, endOfDay);
                int downloadPerDay = subscription.getSubscription().getDownloadPerDay();
                if (downloadPerDay != -1 && todayCount >= downloadPerDay) {
                    throw new BusinessException(BUSINESS_ERROR.DOWNLOAD_LIMIT_EXCEEDED);
                }
            }

            trackDownloadRepository.save(TrackDownload.builder()
                    .user(user)
                    .track(track)
                    .build());

            licenseRepository.save(License.builder()
                    .user(user)
                    .track(track)
                    .licenseCode(UUID.randomUUID().toString())
                    .build());

            track.incrementDownloadCount();
        }

        String audioFile = track.getAudioFile();
        if (audioFile == null || audioFile.isBlank()) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND);
        }

        return storageService.loadAsResource(audioFile);
    }

    /**
     * SR-79 — "다운로드 기록" page. Paged + keyword-searchable + sortable
     * by download timestamp. Returns one row per historical download event
     * from the {@code track_downloads} table (not the legacy
     * {@code download_queue}).
     *
     * @param sort "latest" (default, DESC) or "oldest" (ASC)
     */
    public ResponseDTO<DownloadHistoryItemResponse> getMyDownloadHistory(
            CustomUserDetails userDetails,
            String keyword,
            String sort,
            int page,
            int size
    ) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        Sort.Direction direction = "oldest".equalsIgnoreCase(sort)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(
                Math.max(0, page - 1),
                size,
                Sort.by(direction, "downloadedAt").and(Sort.by(direction, "id"))
        );

        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<TrackDownload> result = trackDownloadRepository
                .findMyDownloadHistory(user, normalizedKeyword, pageable);

        List<DownloadHistoryItemResponse> dataList = result.getContent().stream()
                .map(DownloadHistoryItemResponse::from)
                .toList();

        int total = (int) result.getTotalElements();
        return ResponseDTO.<DownloadHistoryItemResponse>builder()
                .message("Download history retrieved")
                .dataList(dataList)
                .pageInfo(PageInfo.of(page, size, total, 10))
                .build();
    }

    /**
     * SR-79 — "전체 재다운로드" helper. Returns the distinct, ordered list of
     * track IDs matching the user's current filter. The frontend then calls
     * {@code GET /api/tracks/{id}/download} for each — re-downloads don't
     * decrement the daily quota because a license already exists.
     */
    public List<Long> getMyDownloadHistoryTrackIds(
            CustomUserDetails userDetails,
            String keyword
    ) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return trackDownloadRepository.findMyDownloadHistoryTrackIds(user, normalizedKeyword);
    }
}
