package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    interface CommandLockProjection {
        Long getBillingAgreementID();

        Long getUserSubscriptionID();

        Long getUserID();

        PaymentPurpose getPurpose();
    }

    Optional<PaymentOrder> findByOrderId(String orderId);

    @EntityGraph(attributePaths = {"subscription", "userSubscription"})
    @Query("select paymentOrder from PaymentOrder paymentOrder "
            + "where paymentOrder.orderId = :orderID and paymentOrder.user.id = :userID")
    Optional<PaymentOrder> findRecoveryByOrderIdAndUserID(
            @Param("orderID") String orderID,
            @Param("userID") Long userID);

    @EntityGraph(attributePaths = {"subscription", "userSubscription"})
    @Query("select paymentOrder from PaymentOrder paymentOrder "
            + "where (paymentOrder.commandKey = :commandKey "
            + "or paymentOrder.commandKey like :sourcePattern escape '!') "
            + "and paymentOrder.user.id = :userID")
    List<PaymentOrder> findRecoveryCandidatesByCommandKeyAndUserID(
            @Param("commandKey") String commandKey,
            @Param("sourcePattern") String sourcePattern,
            @Param("userID") Long userID,
            Pageable pageable);

    default Optional<PaymentOrder> findRecoveryByCommandKeyAndUserID(String commandKey, Long userID) {
        return uniqueCommand(findRecoveryCandidatesByCommandKeyAndUserID(
                commandKey, sourcePattern(commandKey), userID, Pageable.ofSize(2)));
    }

    @Query("select paymentOrder.billingAgreement.id from PaymentOrder paymentOrder "
            + "where paymentOrder.orderId = :orderID")
    Optional<Long> findBillingAgreementIDByOrderId(@Param("orderID") String orderID);

    @Query("select paymentOrder.billingAgreement.id as billingAgreementID, "
            + "paymentOrder.userSubscription.id as userSubscriptionID, "
            + "paymentOrder.user.id as userID, paymentOrder.purpose as purpose "
            + "from PaymentOrder paymentOrder where paymentOrder.orderId = :orderID")
    Optional<CommandLockProjection> findCommandLockProjectionByOrderId(
            @Param("orderID") String orderID);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select paymentOrder from PaymentOrder paymentOrder where paymentOrder.orderId = :orderID")
    Optional<PaymentOrder> findByOrderIdForUpdate(@Param("orderID") String orderID);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select paymentOrder from PaymentOrder paymentOrder "
            + "where paymentOrder.commandKey = :commandKey "
            + "or paymentOrder.commandKey like :sourcePattern escape '!'")
    List<PaymentOrder> findCommandCandidatesForUpdate(
            @Param("commandKey") String commandKey,
            @Param("sourcePattern") String sourcePattern,
            Pageable pageable);

    default Optional<PaymentOrder> findByCommandKeyForUpdate(String commandKey) {
        return uniqueCommand(findCommandCandidatesForUpdate(commandKey, sourcePattern(commandKey), Pageable.ofSize(2)));
    }

    private static String sourcePattern(String commandKey) {
        return commandKey.replace("!", "!!").replace("%", "!%").replace("_", "!_") + ":SOURCE:%";
    }

    private static Optional<PaymentOrder> uniqueCommand(List<PaymentOrder> candidates) {
        if (candidates.size() > 1) {
            throw new DataIntegrityViolationException("Ambiguous payment command identity.");
        }
        return candidates.stream().findFirst();
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select paymentOrder from PaymentOrder paymentOrder "
            + "where paymentOrder.userSubscription = :userSubscription "
            + "and paymentOrder.purpose = 'UPGRADE' "
            + "and paymentOrder.status in ('READY', 'IN_PROGRESS', 'PROCESSING', "
            + "'PENDING_PROVIDER_CONFIRMATION', 'PROVIDER_SUCCEEDED') order by paymentOrder.id")
    List<PaymentOrder> findUnresolvedUpgradesForUpdate(
            @Param("userSubscription") UserSubscription userSubscription);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select paymentOrder from PaymentOrder paymentOrder "
            + "where paymentOrder.billingAgreement = :billingAgreement "
            + "and paymentOrder.purpose in ('SUBSCRIBE', 'UPGRADE', 'RENEWAL') "
            + "and paymentOrder.status in ('READY', 'IN_PROGRESS', 'PROCESSING', "
            + "'PENDING_PROVIDER_CONFIRMATION', 'PROVIDER_SUCCEEDED') order by paymentOrder.id")
    List<PaymentOrder> findUnresolvedMonetaryCommandsForUpdate(
            @Param("billingAgreement") BillingAgreement billingAgreement,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select paymentOrder from PaymentOrder paymentOrder "
            + "where paymentOrder.billingAgreement = :billingAgreement "
            + "and paymentOrder.userSubscription = :userSubscription "
            + "and paymentOrder.purpose = :purpose "
            + "and paymentOrder.billingPeriodStart = :billingPeriodStart")
    Optional<PaymentOrder> findRenewalPeriodForUpdate(
            @Param("billingAgreement") BillingAgreement billingAgreement,
            @Param("userSubscription") UserSubscription userSubscription,
            @Param("purpose") PaymentPurpose purpose,
            @Param("billingPeriodStart") LocalDate billingPeriodStart);

    @Query("select paymentOrder.id from PaymentOrder paymentOrder "
            + "where paymentOrder.id > :lastSeenID "
            + "and paymentOrder.purpose in ('SUBSCRIBE', 'UPGRADE', 'RENEWAL') "
            + "and (paymentOrder.status in ('PENDING_PROVIDER_CONFIRMATION', 'PROVIDER_SUCCEEDED') "
            + "or (paymentOrder.status = 'PROCESSING' "
            + "and paymentOrder.processingStartedAt is not null "
            + "and paymentOrder.processingStartedAt <= :staleBefore)) "
            + "order by paymentOrder.id asc")
    List<Long> findReconciliationCandidateIDs(
            @Param("staleBefore") LocalDateTime staleBefore,
            @Param("lastSeenID") Long lastSeenID,
            Pageable pageable);

    @Query("select paymentOrder.id from PaymentOrder paymentOrder "
            + "where paymentOrder.id > :lastSeenID "
            + "and paymentOrder.status = 'DONE' "
            + "and paymentOrder.purpose in ('SUBSCRIBE', 'UPGRADE', 'RENEWAL') "
            + "and paymentOrder.createdAt >= :createdAfter "
            + "order by paymentOrder.id asc")
    List<Long> findCompletedProviderReconciliationCandidateIDs(
            @Param("createdAfter") LocalDateTime createdAfter,
            @Param("lastSeenID") Long lastSeenID,
            Pageable pageable);

    @EntityGraph(attributePaths = {"user", "billingAgreement"})
    @Query("select paymentOrder from PaymentOrder paymentOrder "
            + "where paymentOrder.status = :status "
            + "and paymentOrder.purpose in :purposes "
            + "and paymentOrder.id > :lastSeenID "
            + "order by paymentOrder.id asc")
    List<PaymentOrder> findLocalReconciliationCandidates(
            @Param("status") PaymentOrderStatus status,
            @Param("purposes") Collection<PaymentPurpose> purposes,
            @Param("lastSeenID") Long lastSeenID,
            Pageable pageable);

    boolean existsByOrderId(String orderId);

    Optional<PaymentOrder> findFirstByBillingAgreementAndPurposeAndStatusInOrderByCreatedAtDesc(
            BillingAgreement billingAgreement,
            PaymentPurpose purpose,
            Collection<PaymentOrderStatus> statuses);

    boolean existsByBillingAgreementAndPurposeInAndStatusIn(
            BillingAgreement billingAgreement,
            Collection<PaymentPurpose> purposes,
            Collection<PaymentOrderStatus> statuses);

    List<PaymentOrder> findByStatusInAndExpiresAtBefore(
            Collection<PaymentOrderStatus> statuses,
            LocalDateTime expiresAt);

    @EntityGraph(attributePaths = {"user", "subscription", "billingAgreement"})
    Page<PaymentOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
