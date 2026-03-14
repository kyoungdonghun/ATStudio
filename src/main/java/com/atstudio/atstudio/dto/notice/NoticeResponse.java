package com.atstudio.atstudio.dto.notice;

import com.atstudio.atstudio.entity.Notice;
import com.atstudio.atstudio.entity.NoticeAttachment;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NoticeResponse(
        Long id,
        String title,
        String content,
        Boolean isPinned,
        List<NoticeAttachmentResponse> attachments,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /** Without attachments (list, create without fetch) */
    public static NoticeResponse from(Notice notice) {
        return new NoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.isPinned(),
                null,
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }

    /** With attachments (detail) */
    public static NoticeResponse from(Notice notice, List<NoticeAttachment> attachments) {
        return new NoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.isPinned(),
                attachments.stream().map(NoticeAttachmentResponse::from).toList(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
