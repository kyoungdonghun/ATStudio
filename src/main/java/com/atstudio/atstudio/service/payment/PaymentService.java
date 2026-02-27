package com.atstudio.atstudio.service.payment;

import com.atstudio.atstudio.entity.*;
import com.atstudio.atstudio.entity.enums.BillingCycle;

import java.math.BigDecimal;

public interface PaymentService {
    SubscriptionPayment processPayment(User user, UserSubscription userSubscription,
                                       Subscription subscription, BillingCycle billingCycle,
                                       BigDecimal amount);
}
