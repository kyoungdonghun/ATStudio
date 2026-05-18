package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@DisplayName("BillingAgreementRepository tests")
class BillingAgreementRepositoryTest {

    @Autowired
    private BillingAgreementRepository billingAgreementRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("find by user/provider and provider/customerKey")
    void findByUserProviderAndCustomerKey() {
        User user = userRepository.save(user("billing-user", "billing@test.com"));
        BillingAgreement saved = billingAgreementRepository.save(BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("ats_billing_random")
                .build());

        assertThat(billingAgreementRepository.findByUserAndProvider(user, PaymentProviderType.TOSS_BILLING))
                .contains(saved);
        assertThat(billingAgreementRepository.findByProviderAndProviderCustomerKey(
                PaymentProviderType.TOSS_BILLING,
                "ats_billing_random"))
                .contains(saved);
    }

    @Test
    @DisplayName("find due active agreements")
    void findDueActiveAgreements() {
        User dueUser = userRepository.save(user("due-user", "due@test.com"));
        User futureUser = userRepository.save(user("future-user", "future@test.com"));
        BillingAgreement due = activeAgreement(dueUser, "ats_billing_due", LocalDate.of(2026, 5, 17));
        BillingAgreement future = activeAgreement(futureUser, "ats_billing_future", LocalDate.of(2026, 5, 18));
        billingAgreementRepository.save(due);
        billingAgreementRepository.save(future);

        assertThat(billingAgreementRepository.findByStatusAndNextBillingAtLessThanEqual(
                BillingAgreementStatus.ACTIVE,
                LocalDate.of(2026, 5, 17)))
                .containsExactly(due);
    }

    private BillingAgreement activeAgreement(User user, String customerKey, LocalDate nextBillingAt) {
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey(customerKey)
                .build();
        agreement.activate("v1:nonce:ciphertext", "fingerprint-" + customerKey, "카드", "masked", nextBillingAt);
        return agreement;
    }

    private User user(String nickname, String email) {
        return User.builder()
                .nickname(nickname)
                .email(email)
                .build();
    }
}
