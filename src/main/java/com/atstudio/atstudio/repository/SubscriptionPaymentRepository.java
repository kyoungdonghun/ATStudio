package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, Long> {

    boolean existsByUser(User user);

    boolean existsByPaymentOrder(PaymentOrder paymentOrder);

    @EntityGraph(attributePaths = {"user", "subscription", "paymentOrder", "billingAgreement"})
    Page<SubscriptionPayment> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "subscription", "paymentOrder", "billingAgreement"})
    Optional<SubscriptionPayment> findWithGraphById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "user",
            "userSubscription",
            "subscription",
            "paymentOrder",
            "billingAgreement"
    })
    @Query("select payment from SubscriptionPayment payment where payment.id = :id")
    Optional<SubscriptionPayment> findWithGraphByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"user", "subscription", "paymentOrder", "billingAgreement"})
    Optional<SubscriptionPayment> findByPaymentOrder(PaymentOrder paymentOrder);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select payment from SubscriptionPayment payment where payment.paymentOrder = :paymentOrder")
    Optional<SubscriptionPayment> findByPaymentOrderForUpdate(
            @Param("paymentOrder") PaymentOrder paymentOrder);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select payment from SubscriptionPayment payment "
            + "where payment.provider = :provider and payment.pgTransactionId = :pgTransactionID")
    Optional<SubscriptionPayment> findByProviderAndPgTransactionIdForUpdate(
            @Param("provider") PaymentProviderType provider,
            @Param("pgTransactionID") String pgTransactionID);

    @EntityGraph(attributePaths = {"user", "subscription", "paymentOrder", "billingAgreement"})
    Optional<SubscriptionPayment> findFirstByPgTransactionId(String pgTransactionId);

    @EntityGraph(attributePaths = {"user", "subscription", "paymentOrder", "billingAgreement"})
    List<SubscriptionPayment> findByPaymentStatusAndCreatedAtBetween(
            PaymentStatus paymentStatus,
            LocalDateTime createdAtFrom,
            LocalDateTime createdAtTo);

    @EntityGraph(attributePaths = {"user", "subscription", "paymentOrder", "billingAgreement"})
    List<SubscriptionPayment> findByPaymentStatusAndCreatedAtBetweenOrderByIdAsc(
            PaymentStatus paymentStatus,
            LocalDateTime createdAtFrom,
            LocalDateTime createdAtTo,
            Pageable pageable);
}
