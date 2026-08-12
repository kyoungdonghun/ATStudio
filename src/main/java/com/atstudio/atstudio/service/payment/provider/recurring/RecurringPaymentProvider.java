package com.atstudio.atstudio.service.payment.provider.recurring;

import com.atstudio.atstudio.entity.enums.PaymentProviderType;

public interface RecurringPaymentProvider {

    PaymentProviderType getProviderType();

    /**
     * Declares that {@link #prepareAgreement(BillingAgreementPrepareCommand)} satisfies the
     * pure descriptor contract. Callers must fail closed unless a Provider explicitly opts in.
     */
    default boolean supportsPureDeterministicPrepare() {
        return false;
    }

    /**
     * Builds a deterministic, side-effect-free checkout descriptor without Provider mutation.
     * The same command must produce an equal descriptor, and callers must invoke this outside
     * any local transaction. Confirm or charge remains the first Provider mutation boundary.
     */
    BillingAgreementPrepareResult prepareAgreement(BillingAgreementPrepareCommand command);

    BillingAgreementConfirmResult confirmAgreement(BillingAgreementConfirmCommand command);

    BillingChargeResult charge(BillingChargeCommand command);

    BillingAgreementCancelResult cancelAgreement(BillingAgreementCancelCommand command);
}
