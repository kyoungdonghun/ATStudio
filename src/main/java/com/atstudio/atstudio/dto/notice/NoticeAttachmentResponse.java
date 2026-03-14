package com.atstudio.atstudio.dto.notice;

import com.atstudio.atstudio.entity.NoticeAttachment;

public record NoticeAttachmentResponse(
        Long id,
        String originalName,
        Long fileSize
) {
    public static NoticeAttachmentResponse from(NoticeAttachment attachment) {
        return new NoticeAttachmentResponse(
                attachment.getId(),
                attachment.getOriginalName(),
                attachment.getFileSize()
        );
    }
}
