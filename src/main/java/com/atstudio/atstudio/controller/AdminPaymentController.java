package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.payment.AdminBillingAgreementResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentOperationAuditLogResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentOrderResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentReconciliationIncidentResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentReconciliationResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentReceiptResponse;
import com.atstudio.atstudio.dto.payment.AdminSubscriptionPaymentResponse;
import com.atstudio.atstudio.dto.payment.AdminUpdatePaymentReconciliationIncidentRequest;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.AdminPaymentIncidentService;
import com.atstudio.atstudio.service.AdminPaymentReadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final AdminPaymentReadService adminPaymentReadService;
    private final AdminPaymentIncidentService adminPaymentIncidentService;

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

    @GetMapping("/receipts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentReceiptResponse>> listPaymentReceipts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminPaymentReadService.listPaymentReceipts(page, size));
    }

    @GetMapping("/operation-audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentOperationAuditLogResponse>> listPaymentOperationAuditLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminPaymentReadService.listPaymentOperationAuditLogs(page, size));
    }

    @GetMapping("/reconciliation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentReconciliationResponse>> reconcilePayments() {
        return ResponseEntity.ok(adminPaymentReadService.reconcilePayments());
    }

    @GetMapping("/reconciliation-incidents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentReconciliationIncidentResponse>> listReconciliationIncidents(
            @RequestParam(required = false) PaymentReconciliationIncidentStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminPaymentIncidentService.listIncidents(status, page, size));
    }

    @PutMapping("/reconciliation-incidents/{incidentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentReconciliationIncidentResponse>> updateReconciliationIncidentStatus(
            @PathVariable Long incidentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AdminUpdatePaymentReconciliationIncidentRequest request) {
        return ResponseEntity.ok(adminPaymentIncidentService.updateIncidentStatus(
                incidentId,
                userDetails,
                request.status(),
                request.note()));
    }
}
