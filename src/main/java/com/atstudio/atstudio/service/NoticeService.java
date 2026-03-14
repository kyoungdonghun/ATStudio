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
import com.atstudio.atstudio.entity.NoticeAttachment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.repository.NoticeAttachmentRepository;
import com.atstudio.atstudio.repository.NoticeRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Transactional
    public NoticeResponse createNotice(NoticeCreateRequest request, CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        Notice notice = Notice.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .isPinned(request.getIsPinned())
                .build();

        notice = noticeRepository.save(notice);

        List<NoticeAttachment> attachments = saveAttachments(notice, request.getAttachments());

        return NoticeResponse.from(notice, attachments);
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

        List<NoticeAttachment> attachments = attachmentRepository.findAllByNoticeId(noticeId);

        return NoticeResponse.from(notice, attachments);
    }

    @Transactional
    public NoticeResponse updateNotice(Long noticeId, NoticeUpdateRequest request,
                                       CustomUserDetails userDetails) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        validateNoticeOwnership(notice, userDetails);
        notice.update(request.title(), request.content(), request.isPinned());

        List<NoticeAttachment> attachments = attachmentRepository.findAllByNoticeId(noticeId);

        return NoticeResponse.from(notice, attachments);
    }

    @Transactional
    public void deleteNotice(Long noticeId, CustomUserDetails userDetails) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        validateNoticeOwnership(notice, userDetails);

        // Delete attachment files from storage + DB before notice
        List<NoticeAttachment> attachments = attachmentRepository.findAllByNoticeId(noticeId);
        for (NoticeAttachment attachment : attachments) {
            storageService.delete(attachment.getFilePath());
        }
        attachmentRepository.deleteAllByNotice(notice);

        noticeRepository.delete(notice);
    }

    // ── Attachment download ──────────────────────────────────────────────────

    public Resource downloadAttachment(Long noticeId, Long attachmentId) {
        // Verify notice exists
        noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        NoticeAttachment attachment = attachmentRepository.findByIdAndNoticeId(attachmentId, noticeId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        return storageService.loadAsResource(attachment.getFilePath());
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

    private List<NoticeAttachment> saveAttachments(Notice notice, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return List.of();

        List<NoticeAttachment> attachments = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String filePath = storageService.store(file, "notices/attachments");
            NoticeAttachment attachment = NoticeAttachment.builder()
                    .notice(notice)
                    .originalName(file.getOriginalFilename())
                    .filePath(filePath)
                    .fileSize(file.getSize())
                    .build();
            attachments.add(attachmentRepository.save(attachment));
        }
        return attachments;
    }
}
