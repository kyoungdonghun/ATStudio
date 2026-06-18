package com.atstudio.atstudio.dto.certification;

import com.atstudio.atstudio.entity.CompanyCertification;

import java.time.LocalDateTime;

public record CompanyCertificationSummaryResponse(
        Long id,
        Long userId,
        String userNickname,
        String userEmail,
        String companyName,
        String status,
        LocalDateTime createdAt
) {
    public static CompanyCertificationSummaryResponse from(CompanyCertification cert) {
        return new CompanyCertificationSummaryResponse(
                cert.getId(),
                cert.getUser().getId(),
                cert.getUser().getNickname(),
                cert.getUser().getEmail(),
                cert.getUser().getCompanyName(),
                cert.getStatus().name(),
                cert.getCreatedAt()
        );
    }
}
