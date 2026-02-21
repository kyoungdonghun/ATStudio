package com.atstudio.atstudio.dto.license;

import com.atstudio.atstudio.entity.License;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LicenseListItemResponse(
        Long id,
        TrackSummary track,
        String licenseCode,
        LocalDateTime issuedAt
) {
    public record TrackSummary(Long id, String title) {}

    public static LicenseListItemResponse from(License license) {
        return new LicenseListItemResponse(
                license.getId(),
                new TrackSummary(license.getTrack().getId(), license.getTrack().getTitle()),
                license.getLicenseCode(),
                license.getCreatedAt()
        );
    }
}
