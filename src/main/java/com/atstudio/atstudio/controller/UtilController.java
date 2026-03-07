package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.util.CheckResponse;
import com.atstudio.atstudio.dto.util.DownloadCountResponse;
import com.atstudio.atstudio.dto.util.SubscriptionChangePreviewResponse;
import com.atstudio.atstudio.dto.util.SubscriptionStatusResponse;
import com.atstudio.atstudio.dto.util.UserTypeResponse;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.UserService;
import com.atstudio.atstudio.service.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/utils")
@RequiredArgsConstructor
public class UtilController {

    private final UserService userService;
    private final UtilService utilService;

    @GetMapping("/check-email")
    public ResponseEntity<ResponseDTO<CheckResponse>> checkEmail(
            @RequestParam String email) {
        return ResponseEntity.ok(ResponseDTO.<CheckResponse>withSingleData()
                .data(new CheckResponse(userService.isEmailAvailable(email)))
                .build());
    }

    @GetMapping("/check-phone")
    public ResponseEntity<ResponseDTO<CheckResponse>> checkPhone(
            @RequestParam String phone) {
        return ResponseEntity.ok(ResponseDTO.<CheckResponse>withSingleData()
                .data(new CheckResponse(userService.isPhoneAvailable(phone)))
                .build());
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<ResponseDTO<CheckResponse>> checkNickname(
            @RequestParam String nickname) {
        return ResponseEntity.ok(ResponseDTO.<CheckResponse>withSingleData()
                .data(new CheckResponse(userService.isNicknameAvailable(nickname)))
                .build());
    }

    @GetMapping("/subscription-status")
    public ResponseEntity<ResponseDTO<SubscriptionStatusResponse>> getSubscriptionStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ResponseDTO.<SubscriptionStatusResponse>withSingleData()
                .data(utilService.getSubscriptionStatus(userDetails))
                .build());
    }

    @GetMapping("/download-count")
    public ResponseEntity<ResponseDTO<DownloadCountResponse>> getDownloadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ResponseDTO.<DownloadCountResponse>withSingleData()
                .data(utilService.getDownloadCount(userDetails))
                .build());
    }

    @GetMapping("/user-type")
    public ResponseEntity<ResponseDTO<UserTypeResponse>> getUserType(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ResponseDTO.<UserTypeResponse>withSingleData()
                .data(utilService.getUserType(userDetails))
                .build());
    }

    @GetMapping("/subscription-change-preview")
    public ResponseEntity<ResponseDTO<SubscriptionChangePreviewResponse>> previewSubscriptionChange(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long subscriptionId,
            @RequestParam String billingCycle) {
        return ResponseEntity.ok(ResponseDTO.<SubscriptionChangePreviewResponse>withSingleData()
                .data(utilService.previewSubscriptionChange(userDetails, subscriptionId, billingCycle))
                .build());
    }
}
