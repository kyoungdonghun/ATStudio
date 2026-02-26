package com.atstudio.atstudio.dto.certification;

import com.atstudio.atstudio.entity.CompanyCertification;

import java.time.LocalDateTime;

public record CompanyCertificationResponse(
        Long id,
        String status,
        String adminNote,
        String certificationCode,
        String documentPath,
        LocalDateTime approvedAt,
        LocalDateTime createdAt
) {
    public static CompanyCertificationResponse from(CompanyCertification cert) {
        return new CompanyCertificationResponse(
                cert.getId(),
                cert.getStatus().name(),
                cert.getAdminNote(),
                cert.getCertificationCode(),
                cert.getDocumentPath(),
                cert.getApprovedAt(),
                cert.getCreatedAt()
        );
    }
}
