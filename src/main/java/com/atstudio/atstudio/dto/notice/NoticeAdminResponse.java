package com.atstudio.atstudio.dto.notice;

import java.util.List;

public record NoticeAdminResponse(
        String title,
        String content,
        Boolean isPinned,
        List<NoticeAttachmentResponse> attachments
) {
}
