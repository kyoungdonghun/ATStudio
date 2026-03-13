package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.subscription.SubscriptionResponse;
import com.atstudio.atstudio.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // -- 6.1 GET /api/subscriptions ------------------------------------------

    @GetMapping
    public ResponseEntity<ResponseDTO<SubscriptionResponse>> list(
            @RequestParam(required = false) String userType) {
        List<SubscriptionResponse> subscriptions = subscriptionService.getActiveSubscriptions(userType);
        return ResponseEntity.ok(ResponseDTO.<SubscriptionResponse>builder()
                .dataList(subscriptions)
                .build());
    }

    // -- 6.1-admin GET /api/subscriptions/admin (all plans) -------------------

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<SubscriptionResponse>> listAll() {
        List<SubscriptionResponse> subscriptions = subscriptionService.getAllSubscriptions();
        return ResponseEntity.ok(ResponseDTO.<SubscriptionResponse>builder()
                .dataList(subscriptions)
                .build());
    }

    // -- 6.2 GET /api/subscriptions/{subscriptionId} -------------------------

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<ResponseDTO<SubscriptionResponse>> detail(
            @PathVariable Long subscriptionId) {
        SubscriptionResponse response = subscriptionService.getSubscription(subscriptionId);
        return ResponseEntity.ok(ResponseDTO.<SubscriptionResponse>withSingleData()
                .message("Subscription plan retrieved")
                .data(response)
                .build());
    }
}
