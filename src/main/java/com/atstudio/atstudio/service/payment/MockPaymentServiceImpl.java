package com.atstudio.atstudio.service.payment;

import com.atstudio.atstudio.entity.*;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Primary
@RequiredArgsConstructor
public class MockPaymentServiceImpl implements PaymentService {

    private final SubscriptionPaymentRepository paymentRepository;

    @Override
    @Transactional
    public SubscriptionPayment processPayment(User user, UserSubscription userSubscription,
                                               Subscription subscription, BillingCycle billingCycle,
                                               BigDecimal amount) {
        return paymentRepository.save(SubscriptionPayment.builder()
                .user(user)
                .userSubscription(userSubscription)
                .subscription(subscription)
                .billingCycle(billingCycle)
                .amount(amount)
                .paymentStatus(PaymentStatus.DONE)
                .pgTransactionId("MOCK-" + UUID.randomUUID())
                .build());
    }
}
