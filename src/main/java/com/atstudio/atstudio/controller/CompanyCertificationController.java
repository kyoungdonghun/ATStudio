package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.certification.CompanyCertificationDocumentDownload;
import com.atstudio.atstudio.dto.certification.CompanyCertificationResponse;
import com.atstudio.atstudio.dto.certification.CompanyCertificationReviewRequest;
import com.atstudio.atstudio.dto.certification.CompanyCertificationSummaryResponse;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.CompanyCertificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.UriUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    // ── 13.2 POST /api/company-certifications/me/documents ──────────────────

    @PostMapping(value = "/me/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO<CompanyCertificationResponse>> resubmit(
            @RequestPart("documents") List<MultipartFile> documents,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CompanyCertificationResponse response = certificationService.resubmit(userDetails, documents);
        return ResponseEntity.ok(ResponseDTO.<CompanyCertificationResponse>withSingleData()
                .message("Company certification documents resubmitted")
                .data(response)
                .build());
    }

    // ── 13.3 GET /api/company-certifications/me ──────────────────────────────

    @GetMapping("/me")
    public ResponseEntity<ResponseDTO<CompanyCertificationResponse>> getMyStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CompanyCertificationResponse response = certificationService.getMyStatus(userDetails);
        return ResponseEntity.ok(ResponseDTO.<CompanyCertificationResponse>withSingleData()
                .message("My certification status retrieved")
                .data(response)
                .build());
    }

    // ── 13.4 GET /api/company-certifications ─────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<CompanyCertificationSummaryResponse>> listAll(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(certificationService.listAll(status, page, size));
    }

    // ── 13.5 GET /api/company-certifications/{certificationId} ───────────────

    @GetMapping("/{certificationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<CompanyCertificationResponse>> getDetail(
            @PathVariable Long certificationId) {
        CompanyCertificationResponse response = certificationService.getDetail(certificationId);
        return ResponseEntity.ok(ResponseDTO.<CompanyCertificationResponse>withSingleData()
                .message("Certification detail retrieved")
                .data(response)
                .build());
    }

    // ── 13.6 GET /api/company-certifications/{certificationId}/documents/{documentId}

    @GetMapping("/{certificationId}/documents/{documentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable Long certificationId,
            @PathVariable Long documentId) throws IOException {
        CompanyCertificationDocumentDownload download = certificationService.downloadDocument(
                certificationId,
                documentId
        );
        String filename = UriUtils.encode(download.originalFilename(), StandardCharsets.UTF_8);
        byte[] body = StreamUtils.copyToByteArray(download.resource().getInputStream());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .header(HttpHeaders.CACHE_CONTROL, "no-store, private")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "default-src 'none'; sandbox")
                .header(HttpHeaders.ACCEPT_RANGES, "none")
                .body(body);
    }

    // ── 13.7 PUT /api/company-certifications/{certificationId} ───────────────

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
