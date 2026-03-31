package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.downloadqueue.DownloadQueueResponse;
import com.atstudio.atstudio.entity.DownloadQueue;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.key.DownloadQueueId;
import com.atstudio.atstudio.repository.DownloadQueueRepository;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DownloadQueueService {

    private final DownloadQueueRepository downloadQueueRepository;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    @Transactional
    public void addToQueue(Long trackId, CustomUserDetails userDetails) {
        User user = getUser(userDetails);

        // ADMIN bypasses subscription check
        if (user.getRole() != UserRole.ADMIN) {
            userSubscriptionRepository.findActiveByUser(user, LocalDate.now())
                    .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
        }

        Track track = trackRepository.findById(trackId)
                .filter(Track::isActive)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND));

        DownloadQueueId queueId = new DownloadQueueId(user.getId(), track.getId());
        if (downloadQueueRepository.existsById(queueId)) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_DUPLICATE);
        }

        DownloadQueue downloadQueue = DownloadQueue.builder()
                .id(queueId)
                .user(user)
                .track(track)
                .build();
        downloadQueueRepository.save(downloadQueue);
    }

    public List<DownloadQueueResponse> getMyQueue(CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        return downloadQueueRepository.findAllByUser(user).stream()
                .map(DownloadQueueResponse::from)
                .toList();
    }

    @Transactional
    public void removeFromQueue(Long trackId, CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        DownloadQueue downloadQueue = downloadQueueRepository.findByUserAndTrack_Id(user, trackId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        downloadQueueRepository.delete(downloadQueue);
    }

    private User getUser(CustomUserDetails userDetails) {
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }
}
