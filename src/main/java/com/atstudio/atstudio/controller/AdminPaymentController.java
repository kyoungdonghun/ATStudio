package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.payment.AdminBillingAgreementResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentOrderResponse;
import com.atstudio.atstudio.dto.payment.AdminSubscriptionPaymentResponse;
import com.atstudio.atstudio.service.AdminPaymentReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final AdminPaymentReadService adminPaymentReadService;

    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentOrderResponse>> listPaymentOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminPaymentReadService.listPaymentOrders(page, size));
    }

    @GetMapping("/billing-agreements")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminBillingAgreementResponse>> listBillingAgreements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminPaymentReadService.listBillingAgreements(page, size));
    }

    @GetMapping("/subscription-payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminSubscriptionPaymentResponse>> listSubscriptionPayments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminPaymentReadService.listSubscriptionPayments(page, size));
    }
}
