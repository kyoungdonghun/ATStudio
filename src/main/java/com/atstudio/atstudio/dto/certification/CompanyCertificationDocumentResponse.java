package com.atstudio.atstudio.dto.certification;

import com.atstudio.atstudio.entity.CompanyCertificationDocument;

import java.time.LocalDateTime;

public record CompanyCertificationDocumentResponse(
        Long id,
        String originalFilename,
        String contentType,
        Long sizeBytes,
        LocalDateTime createdAt
) {
    public static CompanyCertificationDocumentResponse from(CompanyCertificationDocument document) {
        return new CompanyCertificationDocumentResponse(
                document.getId(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getCreatedAt()
        );
    }
}
