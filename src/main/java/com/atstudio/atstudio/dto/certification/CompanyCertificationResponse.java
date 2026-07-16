package com.atstudio.atstudio.dto.certification;

import com.atstudio.atstudio.entity.CompanyCertification;

import java.time.LocalDateTime;
import java.util.List;

public record CompanyCertificationResponse(
        Long id,
        Long userId,
        String userNickname,
        String userEmail,
        String companyName,
        String phoneCompany,
        String status,
        String adminNote,
        String certificationCode,
        List<CompanyCertificationDocumentResponse> documents,
        LocalDateTime approvedAt,
        LocalDateTime createdAt
) {
    public static CompanyCertificationResponse from(CompanyCertification cert) {
        return new CompanyCertificationResponse(
                cert.getId(),
                cert.getUser().getId(),
                cert.getUser().getNickname(),
                cert.getUser().getEmail(),
                cert.getUser().getCompanyName(),
                cert.getUser().getPhoneCompany(),
                cert.getStatus().name(),
                cert.getAdminNote(),
                cert.getCertificationCode(),
                cert.getDocuments().stream()
                        .map(CompanyCertificationDocumentResponse::from)
                        .toList(),
                cert.getApprovedAt(),
                cert.getCreatedAt()
        );
    }
}
