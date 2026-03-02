package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.notice.NoticeCreateRequest;
import com.atstudio.atstudio.dto.notice.NoticeListItemResponse;
import com.atstudio.atstudio.dto.notice.NoticeResponse;
import com.atstudio.atstudio.dto.notice.NoticeUpdateRequest;
import com.atstudio.atstudio.entity.Notice;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.repository.NoticeRepository;
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
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    @Transactional
    public NoticeResponse createNotice(NoticeCreateRequest request, CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        Notice notice = Notice.builder()
                .user(user)
                .title(request.title())
                .content(request.content())
                .isPinned(request.isPinned())
                .build();

        return NoticeResponse.from(noticeRepository.save(notice));
    }

    public ResponseDTO<NoticeListItemResponse> getNotices(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size));
        Page<Notice> result = noticeRepository.findAllByOrderByIsPinnedDescCreatedAtDesc(pageable);

        List<NoticeListItemResponse> dataList = result.getContent().stream()
                .map(NoticeListItemResponse::from)
                .toList();
        int total = (int) result.getTotalElements();

        return ResponseDTO.<NoticeListItemResponse>builder()
                .dataList(dataList)
                .pageInfo(PageInfo.of(page, size, total, 10))
                .build();
    }

    public NoticeResponse getNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        return NoticeResponse.from(notice);
    }

    @Transactional
    public NoticeResponse updateNotice(Long noticeId, NoticeUpdateRequest request,
                                       CustomUserDetails userDetails) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        validateNoticeOwnership(notice, userDetails);
        notice.update(request.title(), request.content(), request.isPinned());
        return NoticeResponse.from(notice);
    }

    @Transactional
    public void deleteNotice(Long noticeId, CustomUserDetails userDetails) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        validateNoticeOwnership(notice, userDetails);
        noticeRepository.delete(notice);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void validateNoticeOwnership(Notice notice, CustomUserDetails userDetails) {
        if (userDetails.getRole() == UserRole.ADMIN) {
            return;
        }
        if (!notice.getUser().getId().equals(userDetails.getId())) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }
    }
}
