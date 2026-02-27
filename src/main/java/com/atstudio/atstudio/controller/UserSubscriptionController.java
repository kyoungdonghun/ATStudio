package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.subscription.*;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.UserSubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-subscriptions")
@RequiredArgsConstructor
public class UserSubscriptionController {

    private final UserSubscriptionService userSubscriptionService;

    // -- 6.3 POST /api/user-subscriptions ------------------------------------

    @PostMapping
    public ResponseEntity<ResponseDTO<UserSubscriptionResponse>> subscribe(
            @Valid @RequestBody UserSubscriptionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserSubscriptionResponse response = userSubscriptionService.subscribe(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.<UserSubscriptionResponse>withSingleData()
                        .message("Subscription created")
                        .data(response)
                        .build());
    }

    // -- 6.4 GET /api/user-subscriptions/me ----------------------------------

    @GetMapping("/me")
    public ResponseEntity<ResponseDTO<UserSubscriptionResponse>> getMySubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserSubscriptionResponse response = userSubscriptionService.getMySubscription(userDetails);
        return ResponseEntity.ok(ResponseDTO.<UserSubscriptionResponse>withSingleData()
                .message("My subscription retrieved")
                .data(response)
                .build());
    }

    // -- 6.5 GET /api/user-subscriptions -------------------------------------

    @GetMapping
    public ResponseEntity<ResponseDTO<UserSubscriptionResponse>> listAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userSubscriptionService.listAll(page, size));
    }

    // -- 6.6 GET /api/user-subscriptions/{id} --------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<UserSubscriptionResponse>> getDetail(
            @PathVariable Long id) {
        UserSubscriptionResponse response = userSubscriptionService.getDetail(id);
        return ResponseEntity.ok(ResponseDTO.<UserSubscriptionResponse>withSingleData()
                .message("User subscription detail retrieved")
                .data(response)
                .build());
    }

    // -- 6.7 PUT /api/user-subscriptions/me ----------------------------------

    @PutMapping("/me")
    public ResponseEntity<ResponseDTO<ChangeSubscriptionResponse>> changeSubscription(
            @Valid @RequestBody ChangeSubscriptionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ChangeSubscriptionResponse response = userSubscriptionService.changeSubscription(
                userDetails, request);
        return ResponseEntity.ok(ResponseDTO.<ChangeSubscriptionResponse>withSingleData()
                .message("Subscription changed")
                .data(response)
                .build());
    }

    // -- 6.8 PUT /api/user-subscriptions/{id} --------------------------------

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<UserSubscriptionResponse>> adminUpdate(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateSubscriptionRequest request) {
        UserSubscriptionResponse response = userSubscriptionService.adminUpdate(id, request);
        return ResponseEntity.ok(ResponseDTO.<UserSubscriptionResponse>withSingleData()
                .message("Subscription updated by admin")
                .data(response)
                .build());
    }

    // -- 6.9 DELETE /api/user-subscriptions/{id} -----------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<Void>> adminCancel(@PathVariable Long id) {
        userSubscriptionService.adminCancel(id);
        return ResponseEntity.ok(ResponseDTO.<Void>withMessage()
                .message("Subscription cancelled by admin")
                .build());
    }

    // -- 6.10 DELETE /api/user-subscriptions/me ------------------------------

    @DeleteMapping("/me")
    public ResponseEntity<ResponseDTO<Void>> selfCancel(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userSubscriptionService.selfCancel(userDetails);
        return ResponseEntity.ok(ResponseDTO.<Void>withMessage()
                .message("Subscription cancelled")
                .build());
    }
}
