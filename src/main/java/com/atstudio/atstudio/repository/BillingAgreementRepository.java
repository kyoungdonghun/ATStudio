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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BillingAgreementRepository extends JpaRepository<BillingAgreement, Long> {

    String DUE_RENEWAL_FROM = " FROM BillingAgreement ba JOIN ba.user u "
            + "WHERE ba.status = :status AND ba.nextBillingAt <= :today "
            + "AND u.isDeleted = false AND ("
            + "EXISTS (SELECT us.id FROM UserSubscription us "
            + "WHERE us.user = u AND us.status = 'ACTIVE' AND us.expiresAt >= :today AND ("
            + "(ba.renewalRetryAt IS NULL AND NOT EXISTS (SELECT po.id FROM PaymentOrder po "
            + "WHERE po.billingAgreement = ba AND po.userSubscription = us "
            + "AND po.purpose = 'RENEWAL' AND po.billingPeriodStart = ba.nextBillingAt)) "
            + "OR EXISTS (SELECT failed.id FROM PaymentOrder failed "
            + "WHERE failed.billingAgreement = ba AND failed.userSubscription = us "
            + "AND failed.purpose = 'RENEWAL' AND failed.billingPeriodStart = ba.nextBillingAt "
            + "AND failed.status = 'FAILED' AND failed.providerAttempt < 3 "
            + "AND ba.renewalRetryAt IS NOT NULL AND ba.renewalRetryAt <= :today "
            + "AND failed.billingPeriodStart >= :oldestRetryPeriod))) "
            + "OR EXISTS (SELECT succeeded.id FROM PaymentOrder succeeded "
            + "WHERE succeeded.billingAgreement = ba "
            + "AND succeeded.userSubscription.user = u "
            + "AND succeeded.purpose = 'RENEWAL' "
            + "AND succeeded.billingPeriodStart = ba.nextBillingAt "
            + "AND succeeded.status = 'PROVIDER_SUCCEEDED'))";

    @EntityGraph(attributePaths = {"user"})
    Optional<BillingAgreement> findByUserAndProvider(User user, PaymentProviderType provider);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ba FROM BillingAgreement ba JOIN FETCH ba.user "
            + "WHERE ba.user.id = :userID AND ba.provider = :provider")
    Optional<BillingAgreement> findByUserIDAndProviderForUpdate(
            @Param("userID") Long userID,
            @Param("provider") PaymentProviderType provider);

    @EntityGraph(attributePaths = {"user"})
    Optional<BillingAgreement> findByProviderAndProviderCustomerKey(
            PaymentProviderType provider,
            String providerCustomerKey);

    @Query("SELECT ba.id" + DUE_RENEWAL_FROM + " ORDER BY ba.id ASC")
    List<Long> findDueRenewalCandidateIDs(
            @Param("status") BillingAgreementStatus status,
            @Param("today") LocalDate today,
            @Param("oldestRetryPeriod") LocalDate oldestRetryPeriod);

    @Query("SELECT ba.id" + DUE_RENEWAL_FROM
            + " AND ba.id > :lastSeenID ORDER BY ba.id ASC")
    List<Long> findDueRenewalCandidateIDs(
            @Param("status") BillingAgreementStatus status,
            @Param("today") LocalDate today,
            @Param("oldestRetryPeriod") LocalDate oldestRetryPeriod,
            @Param("lastSeenID") Long lastSeenID,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ba FROM BillingAgreement ba JOIN FETCH ba.user WHERE ba.id = :billingAgreementID")
    Optional<BillingAgreement> findByIDForRenewal(
            @Param("billingAgreementID") Long billingAgreementID);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ba FROM BillingAgreement ba JOIN FETCH ba.user WHERE ba.id = :billingAgreementID")
    Optional<BillingAgreement> findByIDForUpdate(
            @Param("billingAgreementID") Long billingAgreementID);

    @Query("SELECT ba.id FROM BillingAgreement ba JOIN ba.user u "
            + "WHERE ba.status = 'CANCELLED' AND u.isDeleted = true "
            + "AND ba.billingKeyCiphertext IS NOT NULL AND TRIM(ba.billingKeyCiphertext) <> '' "
            + "AND ba.billingKeyCleanupStatus IN ('NONE', 'REQUIRED') "
            + "ORDER BY ba.id ASC")
    List<Long> findWithdrawalCleanupCandidateIDs();

    @Query("SELECT ba.id FROM BillingAgreement ba JOIN ba.user u "
            + "WHERE ba.status = 'CANCELLED' AND u.isDeleted = true "
            + "AND ba.billingKeyCiphertext IS NOT NULL AND TRIM(ba.billingKeyCiphertext) <> '' "
            + "AND ba.billingKeyCleanupStatus IN ('NONE', 'REQUIRED') "
            + "AND ba.id > :lastSeenID ORDER BY ba.id ASC")
    List<Long> findWithdrawalCleanupCandidateIDs(
            @Param("lastSeenID") Long lastSeenID,
            Pageable pageable);

    @Query("SELECT ba.id FROM BillingAgreement ba "
            + "WHERE ba.status = 'CANCELLED' "
            + "AND ba.billingKeyCiphertext IS NOT NULL AND TRIM(ba.billingKeyCiphertext) <> '' "
            + "AND ba.billingKeyCleanupStatus = 'PROCESSING' "
            + "AND ba.billingKeyCleanupStartedAt IS NOT NULL "
            + "AND ba.billingKeyCleanupStartedAt <= :staleBefore "
            + "AND ba.id > :lastSeenID ORDER BY ba.id ASC")
    List<Long> findStaleBillingKeyCleanupCandidateIDs(
            @Param("staleBefore") LocalDateTime staleBefore,
            @Param("lastSeenID") Long lastSeenID,
            Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT ba FROM BillingAgreement ba "
            + "WHERE ba.status = :status AND ba.id > :lastSeenID "
            + "ORDER BY ba.id ASC")
    List<BillingAgreement> findLocalReconciliationCandidates(
            @Param("status") BillingAgreementStatus status,
            @Param("lastSeenID") Long lastSeenID,
            Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<BillingAgreement> findAllByOrderByCreatedAtDesc(Pageable pageable);

}
