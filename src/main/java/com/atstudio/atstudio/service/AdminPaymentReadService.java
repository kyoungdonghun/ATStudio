package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.payment.AdminBillingAgreementResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentOperationAuditLogResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentOrderResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentReconciliationResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentReceiptResponse;
import com.atstudio.atstudio.dto.payment.AdminSubscriptionPaymentResponse;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOperationAuditLogRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentReceiptRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminPaymentReadService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final BillingAgreementRepository billingAgreementRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final PaymentReceiptRepository paymentReceiptRepository;
    private final PaymentOperationAuditLogRepository paymentOperationAuditLogRepository;
    private final PaymentReconciliationService paymentReconciliationService;

    public ResponseDTO<AdminPaymentOrderResponse> listPaymentOrders(int page, int size) {
        Pageable pageable = pageable(page, size);
        Page<AdminPaymentOrderResponse> result = paymentOrderRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(AdminPaymentOrderResponse::from);
        return paged(result, page, size);
    }

    public ResponseDTO<AdminBillingAgreementResponse> listBillingAgreements(int page, int size) {
        Pageable pageable = pageable(page, size);
        Page<AdminBillingAgreementResponse> result = billingAgreementRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(AdminBillingAgreementResponse::from);
        return paged(result, page, size);
    }

    public ResponseDTO<AdminSubscriptionPaymentResponse> listSubscriptionPayments(int page, int size) {
        Pageable pageable = pageable(page, size);
        Page<AdminSubscriptionPaymentResponse> result =
                subscriptionPaymentRepository.findAllByOrderByCreatedAtDesc(pageable)
                        .map(AdminSubscriptionPaymentResponse::from);
        return paged(result, page, size);
    }

    public ResponseDTO<AdminPaymentReceiptResponse> listPaymentReceipts(int page, int size) {
        Pageable pageable = pageable(page, size);
        Page<AdminPaymentReceiptResponse> result =
                paymentReceiptRepository.findAllByOrderByCreatedAtDesc(pageable)
                        .map(AdminPaymentReceiptResponse::from);
        return paged(result, page, size);
    }

    public ResponseDTO<AdminPaymentOperationAuditLogResponse> listPaymentOperationAuditLogs(int page, int size) {
        Pageable pageable = pageable(page, size);
        Page<AdminPaymentOperationAuditLogResponse> result =
                paymentOperationAuditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                        .map(AdminPaymentOperationAuditLogResponse::from);
        return paged(result, page, size);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    public ResponseDTO<AdminPaymentReconciliationResponse> reconcilePayments() {
        PaymentReconciliationService.ReconciliationResult local =
                paymentReconciliationService.reconcileLocalLedger();
        PaymentReconciliationService.ProviderReconciliationResult provider =
                paymentReconciliationService.diagnoseProviderLedger();
        return ResponseDTO.<AdminPaymentReconciliationResponse>builder()
                .data(AdminPaymentReconciliationResponse.from(local, provider))
                .build();
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(0, page - 1), Math.max(1, size));
    }

    private <T> ResponseDTO<T> paged(Page<T> result, int page, int size) {
        return ResponseDTO.<T>builder()
                .dataList(result.getContent())
                .pageInfo(PageInfo.of(page, size, (int) result.getTotalElements(), 10))
                .build();
    }
}
