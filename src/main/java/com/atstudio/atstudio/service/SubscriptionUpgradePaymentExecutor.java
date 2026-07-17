package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.service.payment.billing.BillingKeyCrypto;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeResult;
import com.atstudio.atstudio.service.payment.provider.recurring.RecurringPaymentProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionUpgradePaymentExecutor {

    private static final PaymentProviderType RECURRING_PROVIDER = PaymentProviderType.TOSS;

    private final BillingKeyCrypto billingKeyCrypto;
    private final List<RecurringPaymentProvider> recurringProviders;

    @Transactional(propagation = Propagation.NEVER)
    public BillingChargeResult charge(PaymentCommandTransactionService.UpgradeClaim claim) {
        return recurringProvider().charge(new BillingChargeCommand(
                billingKeyCrypto.decrypt(claim.billingKeyCiphertext()),
                claim.providerCustomerKey(),
                claim.orderID(),
                claim.orderName(),
                claim.amount(),
                claim.userEmail(),
                claim.userNickname(),
                claim.providerIdempotencyKey()));
    }

    private RecurringPaymentProvider recurringProvider() {
        return recurringProviders.stream()
                .filter(provider -> provider.getProviderType() == RECURRING_PROVIDER)
                .findFirst()
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.PAYMENT_PROVIDER_NOT_CONFIGURED));
    }
}
