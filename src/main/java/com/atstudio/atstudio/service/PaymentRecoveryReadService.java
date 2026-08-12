package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.PaymentCommandOutcomeResponse;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentRecoveryReadService {

    private static final PaymentProviderType RECURRING_PROVIDER = PaymentProviderType.TOSS;

    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentCommandKeyFactory paymentCommandKeyFactory;

    public PaymentCommandOutcomeResponse getCallbackOutcome(
            CustomUserDetails userDetails,
            String orderID) {
        PaymentOrder order = paymentOrderRepository
                .findRecoveryByOrderIdAndUserID(orderID, userDetails.getId())
                .filter(this::isCallbackOrder)
                .orElseThrow(this::paymentOrderNotFound);
        return PaymentCommandOutcomeResponse.from(order);
    }

    public PaymentCommandOutcomeResponse getUpgradeOutcome(
            CustomUserDetails userDetails,
            Long targetSubscriptionID,
            BillingCycle targetBillingCycle) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        UserSubscription current = userSubscriptionRepository.findActiveByUser(user, LocalDate.now())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
        String commandKey = paymentCommandKeyFactory.upgrade(
                current.getId(),
                current.getStartedAt(),
                current.getExpiresAt(),
                targetSubscriptionID,
                targetBillingCycle);
        PaymentOrder order = paymentOrderRepository
                .findRecoveryByCommandKeyAndUserID(commandKey, userDetails.getId())
                .filter(candidate -> isExactUpgrade(candidate, targetSubscriptionID, targetBillingCycle))
                .orElseThrow(this::paymentOrderNotFound);
        return PaymentCommandOutcomeResponse.from(order);
    }

    private boolean isCallbackOrder(PaymentOrder order) {
        return order.getProvider() == RECURRING_PROVIDER
                && (order.getPurpose() == PaymentPurpose.SUBSCRIBE
                || order.getPurpose() == PaymentPurpose.BILLING_AGREEMENT);
    }

    private boolean isExactUpgrade(
            PaymentOrder order,
            Long targetSubscriptionID,
            BillingCycle targetBillingCycle) {
        return order.getProvider() == RECURRING_PROVIDER
                && order.getPurpose() == PaymentPurpose.UPGRADE
                && order.getSubscription().getId().equals(targetSubscriptionID)
                && order.getUpgradeTargetBillingCycle() == targetBillingCycle;
    }

    private BusinessException paymentOrderNotFound() {
        return new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_NOT_FOUND);
    }
}
