package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;

public record PaymentCommandOutcomeResponse(
        PaymentPurpose purpose,
        PaymentOrderStatus orderStatus,
        Long userSubscriptionId,
        Long targetSubscriptionId,
        BillingCycle targetBillingCycle
) {
    public static PaymentCommandOutcomeResponse from(PaymentOrder order) {
        BillingCycle targetCycle = order.getPurpose() == PaymentPurpose.UPGRADE
                ? order.getUpgradeTargetBillingCycle()
                : order.getBillingCycle();
        return new PaymentCommandOutcomeResponse(
                order.getPurpose(),
                order.getStatus(),
                order.getUserSubscription() == null ? null : order.getUserSubscription().getId(),
                order.getSubscription().getId(),
                targetCycle);
    }
}
