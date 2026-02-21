package com.atstudio.atstudio.dto.notice;

import jakarta.validation.constraints.Size;

public record NoticeUpdateRequest(
        @Size(max = 200) String title,
        String content,
        Boolean isPinned
) {}
