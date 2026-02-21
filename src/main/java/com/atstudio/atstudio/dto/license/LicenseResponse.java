package com.atstudio.atstudio.dto.license;

import com.atstudio.atstudio.entity.License;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LicenseResponse(
        Long id,
        TrackDetail track,
        String licenseCode,
        LocalDateTime issuedAt,
        UserSummary user
) {
    public record TrackDetail(Long id, String title, Integer bpm, String tonality) {}
    public record UserSummary(Long id, String nickname) {}

    public static LicenseResponse from(License license) {
        return new LicenseResponse(
                license.getId(),
                new TrackDetail(
                        license.getTrack().getId(),
                        license.getTrack().getTitle(),
                        license.getTrack().getBpm(),
                        license.getTrack().getTonality()
                ),
                license.getLicenseCode(),
                license.getCreatedAt(),
                new UserSummary(license.getUser().getId(), license.getUser().getNickname())
        );
    }
}
