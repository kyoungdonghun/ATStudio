package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.payment.AdminBillingAgreementResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentOrderResponse;
import com.atstudio.atstudio.dto.payment.AdminSubscriptionPaymentResponse;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminPaymentReadService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final BillingAgreementRepository billingAgreementRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;

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
