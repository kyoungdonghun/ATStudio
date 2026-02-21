package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.license.LicenseListItemResponse;
import com.atstudio.atstudio.dto.license.LicenseResponse;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.LicenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class LicenseController {

    private final LicenseService licenseService;

    @GetMapping("/api/licenses/me")
    public ResponseEntity<ResponseDTO<LicenseListItemResponse>> getMyLicenses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(licenseService.getMyLicenses(userDetails, page, size));
    }

    @GetMapping("/api/licenses/{licenseId}")
    public ResponseEntity<ResponseDTO<LicenseResponse>> getMyLicense(
            @PathVariable Long licenseId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ResponseDTO.<LicenseResponse>withSingleData()
                .data(licenseService.getMyLicense(licenseId, userDetails))
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/users/{userId}/licenses")
    public ResponseEntity<ResponseDTO<LicenseListItemResponse>> getUserLicenses(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(licenseService.getUserLicenses(userId, page, size));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/users/{userId}/licenses/{licenseId}")
    public ResponseEntity<ResponseDTO<LicenseResponse>> getUserLicense(
            @PathVariable Long userId,
            @PathVariable Long licenseId) {
        return ResponseEntity.ok(ResponseDTO.<LicenseResponse>withSingleData()
                .data(licenseService.getUserLicense(userId, licenseId))
                .build());
    }
}
