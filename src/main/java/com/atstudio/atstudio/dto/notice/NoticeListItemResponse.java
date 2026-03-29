package com.atstudio.atstudio.dto.notice;

import com.atstudio.atstudio.entity.Notice;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NoticeListItemResponse(
        Long id,
        String title,
        Boolean isPinned,
        long viewCount,
        LocalDateTime createdAt
) {
    public static NoticeListItemResponse from(Notice notice) {
        return new NoticeListItemResponse(
                notice.getId(),
                notice.getTitle(),
                notice.isPinned(),
                notice.getViewCount(),
                notice.getCreatedAt()
        );
    }
}
