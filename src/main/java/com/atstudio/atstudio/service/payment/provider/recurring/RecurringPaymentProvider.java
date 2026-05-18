package com.atstudio.atstudio.service.payment.provider.recurring;

import com.atstudio.atstudio.entity.enums.PaymentProviderType;

public interface RecurringPaymentProvider {

    PaymentProviderType getProviderType();

    BillingAgreementPrepareResult prepareAgreement(BillingAgreementPrepareCommand command);

    BillingAgreementConfirmResult confirmAgreement(BillingAgreementConfirmCommand command);

    BillingChargeResult charge(BillingChargeCommand command);

    BillingAgreementCancelResult cancelAgreement(BillingAgreementCancelCommand command);
}
