package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.payment.AdminBillingAgreementResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentEntitlementCorrectionApproveRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentEntitlementCorrectionExecuteRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentEntitlementCorrectionPreviewResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentEntitlementCorrectionRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentEntitlementCorrectionResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentOperationAuditLogResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentOrderResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentReconciliationIncidentResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentReconciliationResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentReceiptResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundApproveRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundCreateRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundExecuteRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundPreviewResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementIgnoreRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementImportAttemptResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementImportResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementReconcileRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementResponse;
import com.atstudio.atstudio.dto.payment.AdminSubscriptionPaymentResponse;
import com.atstudio.atstudio.dto.payment.AdminUpdatePaymentReconciliationIncidentRequest;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import com.atstudio.atstudio.entity.enums.PaymentSettlementSource;
import com.atstudio.atstudio.entity.enums.PaymentSettlementStatus;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.AdminPaymentEntitlementCorrectionService;
import com.atstudio.atstudio.service.AdminPaymentIncidentService;
import com.atstudio.atstudio.service.AdminPaymentReadService;
import com.atstudio.atstudio.service.AdminPaymentRefundService;
import com.atstudio.atstudio.service.AdminPaymentSettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final AdminPaymentReadService adminPaymentReadService;
    private final AdminPaymentIncidentService adminPaymentIncidentService;
    private final AdminPaymentRefundService adminPaymentRefundService;
    private final AdminPaymentEntitlementCorrectionService adminPaymentEntitlementCorrectionService;
    private final AdminPaymentSettlementService adminPaymentSettlementService;

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

    @GetMapping("/settlements")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentSettlementResponse>> listSettlements(
            @RequestParam(required = false) PaymentSettlementStatus status,
            @RequestParam(required = false) PaymentSettlementSource source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDateTo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminPaymentSettlementService.listSettlements(
                status,
                source,
                baseDateFrom,
                baseDateTo,
                page,
                size));
    }

    @PostMapping(value = "/settlements/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentSettlementImportResponse>> importSettlements(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String note,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return ResponseEntity.ok(adminPaymentSettlementService.importSettlements(
                userDetails,
                file,
                note,
                idempotencyKey));
    }

    @GetMapping("/settlement-import-attempts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentSettlementImportAttemptResponse>> listSettlementImportAttempts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminPaymentSettlementService.listImportAttempts(page, size));
    }

    @GetMapping("/settlement-import-attempts/recovery")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentSettlementImportAttemptResponse>> recoverSettlementImportAttempt(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return ResponseEntity.ok(adminPaymentSettlementService.recoverImportAttempt(userDetails, idempotencyKey));
    }

    @GetMapping("/settlement-import-attempts/{attemptId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentSettlementImportAttemptResponse>> getSettlementImportAttempt(
            @PathVariable Long attemptId) {
        return ResponseEntity.ok(adminPaymentSettlementService.getImportAttempt(attemptId));
    }

    @PostMapping("/settlements/reconcile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentSettlementImportResponse>> reconcileSettlements(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody(required = false) AdminPaymentSettlementReconcileRequest request) {
        return ResponseEntity.ok(adminPaymentSettlementService.reconcileMissingProviderSettlements(
                userDetails,
                request));
    }

    @PutMapping("/settlements/{settlementId}/ignore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentSettlementResponse>> ignoreSettlement(
            @PathVariable Long settlementId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AdminPaymentSettlementIgnoreRequest request) {
        return ResponseEntity.ok(adminPaymentSettlementService.ignoreSettlement(settlementId, userDetails, request));
    }

    @GetMapping("/refunds")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentRefundResponse>> listRefunds(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminPaymentRefundService.listRefunds(page, size));
    }

    @GetMapping("/refunds/{refundId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentRefundResponse>> getRefund(@PathVariable Long refundId) {
        return ResponseEntity.ok(adminPaymentRefundService.getRefund(refundId));
    }

    @GetMapping("/refund-preview/{subscriptionPaymentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentRefundPreviewResponse>> previewRefund(
            @PathVariable Long subscriptionPaymentId) {
        return ResponseEntity.ok(adminPaymentRefundService.previewRefund(subscriptionPaymentId));
    }

    @PostMapping("/refunds")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentRefundResponse>> createRefund(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AdminPaymentRefundCreateRequest request) {
        return ResponseEntity.ok(adminPaymentRefundService.createRefund(userDetails, request));
    }

    @PostMapping("/refunds/{refundId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentRefundResponse>> approveRefund(
            @PathVariable Long refundId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AdminPaymentRefundApproveRequest request) {
        return ResponseEntity.ok(adminPaymentRefundService.approveRefund(refundId, userDetails, request));
    }

    @PostMapping("/refunds/{refundId}/execute")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentRefundResponse>> executeRefund(
            @PathVariable Long refundId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AdminPaymentRefundExecuteRequest request) {
        return ResponseEntity.ok(adminPaymentRefundService.executeRefund(refundId, userDetails, request));
    }

    @PostMapping("/entitlement-correction-preview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentEntitlementCorrectionPreviewResponse>> previewEntitlementCorrection(
            @Valid @RequestBody AdminPaymentEntitlementCorrectionRequest request) {
        return ResponseEntity.ok(adminPaymentEntitlementCorrectionService.previewCorrection(request));
    }

    @GetMapping("/entitlement-corrections")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentEntitlementCorrectionResponse>> listEntitlementCorrections(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminPaymentEntitlementCorrectionService.listCorrections(page, size));
    }

    @GetMapping("/entitlement-corrections/{correctionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentEntitlementCorrectionResponse>> getEntitlementCorrection(
            @PathVariable Long correctionId) {
        return ResponseEntity.ok(adminPaymentEntitlementCorrectionService.getCorrection(correctionId));
    }

    @PostMapping("/entitlement-corrections")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentEntitlementCorrectionResponse>> createEntitlementCorrection(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AdminPaymentEntitlementCorrectionRequest request) {
        return ResponseEntity.ok(adminPaymentEntitlementCorrectionService.createCorrection(userDetails, request));
    }

    @PostMapping("/entitlement-corrections/{correctionId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentEntitlementCorrectionResponse>> approveEntitlementCorrection(
            @PathVariable Long correctionId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AdminPaymentEntitlementCorrectionApproveRequest request) {
        return ResponseEntity.ok(adminPaymentEntitlementCorrectionService.approveCorrection(
                correctionId,
                userDetails,
                request));
    }

    @PostMapping("/entitlement-corrections/{correctionId}/execute")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminPaymentEntitlementCorrectionResponse>> executeEntitlementCorrection(
            @PathVariable Long correctionId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AdminPaymentEntitlementCorrectionExecuteRequest request) {
        return ResponseEntity.ok(adminPaymentEntitlementCorrectionService.executeCorrection(
                correctionId,
                userDetails,
                request));
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
