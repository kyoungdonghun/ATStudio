package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillingAgreementRepository extends JpaRepository<BillingAgreement, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<BillingAgreement> findByUserAndProvider(User user, PaymentProviderType provider);

    @EntityGraph(attributePaths = {"user"})
    Optional<BillingAgreement> findByProviderAndProviderCustomerKey(
            PaymentProviderType provider,
            String providerCustomerKey);

    @Query("SELECT ba.id FROM BillingAgreement ba JOIN ba.user u "
            + "WHERE ba.status = :status AND ba.nextBillingAt <= :nextBillingAt "
            + "AND u.isDeleted = false")
    List<Long> findDueRenewalCandidateIDs(
            BillingAgreementStatus status,
            LocalDate nextBillingAt);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ba FROM BillingAgreement ba JOIN FETCH ba.user WHERE ba.id = :billingAgreementID")
    Optional<BillingAgreement> findByIDForRenewal(
            @Param("billingAgreementID") Long billingAgreementID);

    @Query("SELECT ba.id FROM BillingAgreement ba JOIN ba.user u "
            + "WHERE ba.status = 'CANCELLED' AND u.isDeleted = true "
            + "AND ba.billingKeyCiphertext IS NOT NULL AND TRIM(ba.billingKeyCiphertext) <> ''")
    List<Long> findWithdrawalCleanupCandidateIDs();

    @EntityGraph(attributePaths = {"user"})
    Page<BillingAgreement> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    List<BillingAgreement> findByStatus(BillingAgreementStatus status);
}
