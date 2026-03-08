package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.certification.CompanyCertificationResponse;
import com.atstudio.atstudio.dto.certification.CompanyCertificationReviewRequest;
import com.atstudio.atstudio.dto.certification.CompanyCertificationSummaryResponse;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.CompanyCertificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/company-certifications")
@RequiredArgsConstructor
public class CompanyCertificationController {

    private final CompanyCertificationService certificationService;

    // ── 13.1 POST /api/company-certifications ────────────────────────────────

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO<CompanyCertificationResponse>> apply(
            @RequestPart("documents") List<MultipartFile> documents,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CompanyCertificationResponse response = certificationService.apply(userDetails, documents);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.<CompanyCertificationResponse>withSingleData()
                        .message("Company certification application submitted")
                        .data(response)
                        .build());
    }

    // ── 13.2 GET /api/company-certifications/me ──────────────────────────────

    @GetMapping("/me")
    public ResponseEntity<ResponseDTO<CompanyCertificationResponse>> getMyStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CompanyCertificationResponse response = certificationService.getMyStatus(userDetails);
        return ResponseEntity.ok(ResponseDTO.<CompanyCertificationResponse>withSingleData()
                .message("My certification status retrieved")
                .data(response)
                .build());
    }

    // ── 13.3 GET /api/company-certifications ─────────────────────────────────

    @GetMapping
    public ResponseEntity<ResponseDTO<CompanyCertificationSummaryResponse>> listAll(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(certificationService.listAll(status, page, size));
    }

    // ── 13.4 GET /api/company-certifications/{certificationId} ───────────────

    @GetMapping("/{certificationId}")
    public ResponseEntity<ResponseDTO<CompanyCertificationResponse>> getDetail(
            @PathVariable Long certificationId) {
        CompanyCertificationResponse response = certificationService.getDetail(certificationId);
        return ResponseEntity.ok(ResponseDTO.<CompanyCertificationResponse>withSingleData()
                .message("Certification detail retrieved")
                .data(response)
                .build());
    }

    // ── 13.5 PUT /api/company-certifications/{certificationId} ───────────────

    @PutMapping("/{certificationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<CompanyCertificationResponse>> processReview(
            @PathVariable Long certificationId,
            @Valid @RequestBody CompanyCertificationReviewRequest request) {
        CompanyCertificationResponse response = certificationService.processReview(
                certificationId, request);
        return ResponseEntity.ok(ResponseDTO.<CompanyCertificationResponse>withSingleData()
                .message("Certification review processed")
                .data(response)
                .build());
    }
}
