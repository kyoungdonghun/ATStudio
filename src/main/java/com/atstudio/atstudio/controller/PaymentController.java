package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.BillingAgreementConfirmRequest;
import com.atstudio.atstudio.dto.payment.BillingAgreementConfirmResponse;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareRequest;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareResponse;
import com.atstudio.atstudio.dto.payment.BillingAgreementResponse;
import com.atstudio.atstudio.dto.payment.PaymentCommandOutcomeResponse;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.BillingAgreementApplicationService;
import com.atstudio.atstudio.service.PaymentCommandKeyFactory;
import com.atstudio.atstudio.service.PaymentRecoveryReadService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final BillingAgreementApplicationService billingAgreementApplicationService;
    private final PaymentCommandKeyFactory paymentCommandKeyFactory;
    private final PaymentRecoveryReadService paymentRecoveryReadService;

    @PostMapping("/billing-agreements/prepare")
    public ResponseEntity<ResponseDTO<BillingAgreementPrepareResponse>> prepareBillingAgreement(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody BillingAgreementPrepareRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            paymentCommandKeyFactory.requireCanonicalBillingAgreementPrepareKey(idempotencyKey);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_PREPARE_IDEMPOTENCY_KEY_INVALID);
        }
        BillingAgreementPrepareResponse response =
                billingAgreementApplicationService.prepareBillingAgreement(userDetails, request, idempotencyKey);
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

    @GetMapping("/orders/{orderID}/outcome")
    public ResponseEntity<ResponseDTO<PaymentCommandOutcomeResponse>> getCallbackOutcome(
            @PathVariable String orderID,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PaymentCommandOutcomeResponse response =
                paymentRecoveryReadService.getCallbackOutcome(userDetails, orderID);
        return ResponseEntity.ok(ResponseDTO.<PaymentCommandOutcomeResponse>withSingleData()
                .message("Payment command outcome found")
                .data(response)
                .build());
    }

    @GetMapping("/subscription-upgrades/outcome")
    public ResponseEntity<ResponseDTO<PaymentCommandOutcomeResponse>> getUpgradeOutcome(
            @Positive @RequestParam Long subscriptionId,
            @RequestParam BillingCycle billingCycle,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PaymentCommandOutcomeResponse response = paymentRecoveryReadService.getUpgradeOutcome(
                userDetails,
                subscriptionId,
                billingCycle);
        return ResponseEntity.ok(ResponseDTO.<PaymentCommandOutcomeResponse>withSingleData()
                .message("Payment command outcome found")
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
