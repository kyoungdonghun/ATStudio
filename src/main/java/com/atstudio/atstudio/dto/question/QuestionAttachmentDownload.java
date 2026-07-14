package com.atstudio.atstudio.dto.question;

import org.springframework.core.io.Resource;

public record QuestionAttachmentDownload(
        Resource resource,
        String originalFilename
) {}
