package com.atstudio.atstudio.dto.certification;

import org.springframework.core.io.Resource;

public record CompanyCertificationDocumentDownload(
        Resource resource,
        String originalFilename
) {}
