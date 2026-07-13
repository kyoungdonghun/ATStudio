package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.Lock;

import java.lang.reflect.Method;
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
    @DisplayName("find due agreement IDs and lock the selected agreement before renewal")
    void findDueActiveAgreements() throws Exception {
        User dueUser = userRepository.save(user("due-user", "due@test.com"));
        User futureUser = userRepository.save(user("future-user", "future@test.com"));
        User deletedUser = userRepository.save(user("deleted-user", "deleted@test.com"));
        deletedUser.withdraw();
        BillingAgreement due = activeAgreement(dueUser, "ats_billing_due", LocalDate.of(2026, 5, 17));
        BillingAgreement future = activeAgreement(futureUser, "ats_billing_future", LocalDate.of(2026, 5, 18));
        BillingAgreement deleted = activeAgreement(
                deletedUser,
                "ats_billing_deleted",
                LocalDate.of(2026, 5, 17));
        billingAgreementRepository.save(due);
        billingAgreementRepository.save(future);
        billingAgreementRepository.save(deleted);

        assertThat(billingAgreementRepository.findDueRenewalCandidateIDs(
                BillingAgreementStatus.ACTIVE,
                LocalDate.of(2026, 5, 17)))
                .containsExactly(due.getId());
        assertThat(billingAgreementRepository.findByIDForRenewal(due.getId()))
                .contains(due);
        Method lockedLookup = BillingAgreementRepository.class.getMethod("findByIDForRenewal", Long.class);
        assertThat(lockedLookup.getAnnotation(Lock.class).value())
                .isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    @DisplayName("withdrawal cleanup candidates contain only deleted users with cancelled agreements and retained keys")
    void findWithdrawalCleanupCandidates() {
        User targetUser = userRepository.save(user("cleanup-target", "cleanup-target@test.com"));
        targetUser.withdraw();
        BillingAgreement target = activeAgreement(
                targetUser,
                "ats_billing_cleanup_target",
                LocalDate.of(2026, 5, 17));
        target.cancel();

        User activeUser = userRepository.save(user("cleanup-active", "cleanup-active@test.com"));
        BillingAgreement activeUserAgreement = activeAgreement(
                activeUser,
                "ats_billing_cleanup_active",
                LocalDate.of(2026, 5, 17));
        activeUserAgreement.cancel();

        User noKeyUser = userRepository.save(user("cleanup-no-key", "cleanup-no-key@test.com"));
        noKeyUser.withdraw();
        BillingAgreement noKeyAgreement = BillingAgreement.builder()
                .user(noKeyUser)
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("ats_billing_cleanup_no_key")
                .build();
        noKeyAgreement.cancel();

        User nonCancelledUser = userRepository.save(user("cleanup-pending", "cleanup-not-cancelled@test.com"));
        nonCancelledUser.withdraw();
        BillingAgreement nonCancelledAgreement = activeAgreement(
                nonCancelledUser,
                "ats_billing_cleanup_not_cancelled",
                LocalDate.of(2026, 5, 17));

        billingAgreementRepository.save(target);
        billingAgreementRepository.save(activeUserAgreement);
        billingAgreementRepository.save(noKeyAgreement);
        billingAgreementRepository.save(nonCancelledAgreement);

        assertThat(billingAgreementRepository.findWithdrawalCleanupCandidateIDs())
                .containsExactly(target.getId());
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
