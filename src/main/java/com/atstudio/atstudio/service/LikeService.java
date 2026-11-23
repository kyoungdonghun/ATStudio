package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.like.LikeResponse;
import com.atstudio.atstudio.entity.Like;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.key.LikeId;
import com.atstudio.atstudio.repository.LikeRepository;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final PilotAccess pilot;

    @Transactional
    public void addLike(Long trackId, CustomUserDetails userDetails) {
        User user = getUserForUpdate(userDetails);
        requirePublishedForMutation(user, trackId);

        Track track = trackRepository.findByIdForUpdate(trackId)
                .filter(Track::isActive)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND));

        LikeId likeId = new LikeId(user.getId(), track.getId());
        if (likeRepository.existsById(likeId)) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_DUPLICATE);
        }

        Like like = Like.builder()
                .id(likeId)
                .user(user)
                .track(track)
                .build();
        likeRepository.save(like);
        track.incrementLikeCount();
    }

    public List<LikeResponse> getMyLikes(CustomUserDetails userDetails) {
        if (pilot.enabled()) {
            PilotAccess.Library library = pilot.libraryForRead(userDetails.getId());
            return likeRepository.findAllActiveByUser(library.grant().getUser()).stream()
                    .filter(like -> library.includes(like.getTrack().getId()))
                    .map(LikeResponse::from)
                    .toList();
        }
        User user = getUser(userDetails);
        return likeRepository.findAllActiveByUser(user).stream()
                .map(LikeResponse::from)
                .toList();
    }

    @Transactional
    public void removeLike(Long trackId, CustomUserDetails userDetails) {
        User user = getUserForUpdate(userDetails);
        requirePublishedForMutation(user, trackId);
        Track track = trackRepository.findByIdForUpdate(trackId)
                .filter(candidate -> !pilot.enabled() || candidate.isActive())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        Like like = likeRepository.findByUserAndTrack_Id(user, trackId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        likeRepository.delete(like);
        track.decrementLikeCount();
    }

    private void requirePublishedForMutation(User user, Long trackId) {
        if (pilot.enabled() && !pilot.libraryForWrite(user).includes(trackId)) {
            throw new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND);
        }
    }

    private User getUserForUpdate(CustomUserDetails userDetails) {
        // Match download lock order, including user FK checks when a Like is inserted.
        return userRepository.findByIdForUpdate(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private User getUser(CustomUserDetails userDetails) {
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }
}
