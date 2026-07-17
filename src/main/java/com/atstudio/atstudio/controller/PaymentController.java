package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.payment.BillingAgreementConfirmRequest;
import com.atstudio.atstudio.dto.payment.BillingAgreementConfirmResponse;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareRequest;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareResponse;
import com.atstudio.atstudio.dto.payment.BillingAgreementResponse;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.BillingAgreementApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final BillingAgreementApplicationService billingAgreementApplicationService;

    @PostMapping("/billing-agreements/prepare")
    public ResponseEntity<ResponseDTO<BillingAgreementPrepareResponse>> prepareBillingAgreement(
            @Valid @RequestBody BillingAgreementPrepareRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        BillingAgreementPrepareResponse response =
                billingAgreementApplicationService.prepareBillingAgreement(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.<BillingAgreementPrepareResponse>withSingleData()
                        .message("Billing agreement prepared")
                        .data(response)
                        .build());
    }

    @PostMapping("/billing-agreements/confirm")
    public ResponseEntity<ResponseDTO<BillingAgreementConfirmResponse>> confirmBillingAgreement(
            @Valid @RequestBody BillingAgreementConfirmRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        BillingAgreementConfirmResponse response =
                billingAgreementApplicationService.confirmBillingAgreement(userDetails, request);
        return ResponseEntity.ok(ResponseDTO.<BillingAgreementConfirmResponse>withSingleData()
                .message("Billing agreement confirmed")
                .data(response)
                .build());
    }

    @GetMapping("/billing-agreements/me")
    public ResponseEntity<ResponseDTO<BillingAgreementResponse>> getMyBillingAgreement(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        BillingAgreementResponse response = billingAgreementApplicationService.getMyBillingAgreement(userDetails);
        return ResponseEntity.ok(ResponseDTO.<BillingAgreementResponse>withSingleData()
                .message("Billing agreement found")
                .data(response)
                .build());
    }

    @DeleteMapping("/billing-agreements/me")
    public ResponseEntity<ResponseDTO<BillingAgreementResponse>> cancelMyBillingAgreement(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        BillingAgreementResponse response = billingAgreementApplicationService.cancelMyBillingAgreement(userDetails);
        return ResponseEntity.ok(ResponseDTO.<BillingAgreementResponse>withSingleData()
                .message("Billing agreement cancelled")
                .data(response)
                .build());
    }
}
