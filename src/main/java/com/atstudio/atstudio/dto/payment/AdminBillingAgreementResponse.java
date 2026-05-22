package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminBillingAgreementResponse(
        Long id,
        Long userId,
        String userNickname,
        PaymentProviderType provider,
        BillingAgreementStatus status,
        String payMethod,
        String maskedMethod,
        LocalDate nextBillingAt,
        LocalDateTime lastChargedAt,
        int failureCount,
        LocalDateTime cancelledAt,
        LocalDateTime createdAt
) {

    public static AdminBillingAgreementResponse from(BillingAgreement agreement) {
        return new AdminBillingAgreementResponse(
                agreement.getId(),
                agreement.getUser().getId(),
                agreement.getUser().getNickname(),
                agreement.getProvider(),
                agreement.getStatus(),
                agreement.getPayMethod(),
                agreement.getMaskedMethod(),
                agreement.getNextBillingAt(),
                agreement.getLastChargedAt(),
                agreement.getFailureCount(),
                agreement.getCancelledAt(),
                agreement.getCreatedAt()
        );
    }
}
