package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.subscription.AdminSubscriptionCorrectionApproveRequest;
import com.atstudio.atstudio.dto.subscription.AdminSubscriptionCorrectionExecuteRequest;
import com.atstudio.atstudio.dto.subscription.AdminSubscriptionCorrectionPreviewResponse;
import com.atstudio.atstudio.dto.subscription.AdminSubscriptionCorrectionRequest;
import com.atstudio.atstudio.dto.subscription.AdminSubscriptionCorrectionResponse;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.AdminSubscriptionCorrectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/user-subscription-corrections")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserSubscriptionCorrectionController {

    private final AdminSubscriptionCorrectionService correctionService;

    @PostMapping("/preview")
    public ResponseEntity<ResponseDTO<AdminSubscriptionCorrectionPreviewResponse>> preview(
            @AuthenticationPrincipal CustomUserDetails actorDetails,
            @Valid @RequestBody AdminSubscriptionCorrectionRequest request) {
        return ResponseEntity.ok(correctionService.previewCorrection(actorDetails, request));
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<AdminSubscriptionCorrectionResponse>> list(
            @AuthenticationPrincipal CustomUserDetails actorDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(correctionService.listCorrections(actorDetails, page, size));
    }

    @GetMapping("/{correctionId}")
    public ResponseEntity<ResponseDTO<AdminSubscriptionCorrectionResponse>> detail(
            @AuthenticationPrincipal CustomUserDetails actorDetails,
            @PathVariable Long correctionId) {
        return ResponseEntity.ok(correctionService.getCorrection(actorDetails, correctionId));
    }

    @GetMapping("/open")
    public ResponseEntity<ResponseDTO<AdminSubscriptionCorrectionResponse>> open(
            @AuthenticationPrincipal CustomUserDetails actorDetails,
            @RequestParam Long userSubscriptionId) {
        return correctionService.getOpenCorrection(actorDetails, userSubscriptionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<AdminSubscriptionCorrectionResponse>> request(
            @AuthenticationPrincipal CustomUserDetails actorDetails,
            @Valid @RequestBody AdminSubscriptionCorrectionRequest request) {
        return ResponseEntity.ok(correctionService.requestCorrection(actorDetails, request));
    }

    @PostMapping("/{correctionId}/approve")
    public ResponseEntity<ResponseDTO<AdminSubscriptionCorrectionResponse>> approve(
            @AuthenticationPrincipal CustomUserDetails actorDetails,
            @PathVariable Long correctionId,
            @Valid @RequestBody AdminSubscriptionCorrectionApproveRequest request) {
        return ResponseEntity.ok(correctionService.approveCorrection(
                correctionId,
                actorDetails,
                request));
    }

    @PostMapping("/{correctionId}/execute")
    public ResponseEntity<ResponseDTO<AdminSubscriptionCorrectionResponse>> execute(
            @AuthenticationPrincipal CustomUserDetails actorDetails,
            @PathVariable Long correctionId,
            @Valid @RequestBody AdminSubscriptionCorrectionExecuteRequest request) {
        return ResponseEntity.ok(correctionService.executeCorrection(
                correctionId,
                actorDetails,
                request));
    }
}
