package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.entity.*;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.repository.*;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
}
