package com.atstudio.atstudio.dto.question;

import com.atstudio.atstudio.entity.QuestionAttachment;

public record AttachmentResponse(
        Long id,
        String originalName,
        Long fileSize
) {
    public static AttachmentResponse from(QuestionAttachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getOriginalName(),
                attachment.getFileSize()
        );
    }
}
