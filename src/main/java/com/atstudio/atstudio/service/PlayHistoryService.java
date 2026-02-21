package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.playhistory.PlayHistoryDeleteRequest;
import com.atstudio.atstudio.dto.playhistory.PlayHistoryListItemResponse;
import com.atstudio.atstudio.entity.PlayHistory;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.repository.PlayHistoryRepository;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlayHistoryService {

    private final PlayHistoryRepository playHistoryRepository;
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;

    @Transactional
    public void savePlayHistory(Long trackId, CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        Track track = trackRepository.findById(trackId)
                .filter(Track::isActive)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND));

        playHistoryRepository.save(PlayHistory.builder().user(user).track(track).build());
        trackRepository.incrementPlayCount(trackId);
    }

    public ResponseDTO<PlayHistoryListItemResponse> getMyHistory(CustomUserDetails userDetails, int page, int size) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size));
        Page<PlayHistory> result = playHistoryRepository.findAllByUserOrderByPlayedAtDesc(user, pageable);

        List<PlayHistoryListItemResponse> dataList = result.getContent().stream()
                .map(PlayHistoryListItemResponse::from)
                .toList();

        int total = (int) result.getTotalElements();

        return ResponseDTO.<PlayHistoryListItemResponse>builder()
                .message("Play history retrieved")
                .dataList(dataList)
                .pageInfo(PageInfo.of(page, size, total, 10))
                .build();
    }

    @Transactional
    public void deleteHistory(PlayHistoryDeleteRequest request, CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        if (request.historyIds().isEmpty()) {
            playHistoryRepository.deleteAllByUser(user);
        } else {
            playHistoryRepository.deleteByIdInAndUser(request.historyIds(), user);
        }
    }
}
