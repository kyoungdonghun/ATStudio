package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.PaymentRefund;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.enums.PaymentRefundStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {

    @EntityGraph(attributePaths = {
            "user",
            "paymentOrder",
            "subscriptionPayment",
            "requestedBy",
            "approvedBy",
            "executedBy"
    })
    Page<PaymentRefund> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {
            "user",
            "paymentOrder",
            "subscriptionPayment",
            "requestedBy",
            "approvedBy",
            "executedBy"
    })
    Optional<PaymentRefund> findWithGraphById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from PaymentRefund r where r.id = :id")
    Optional<PaymentRefund> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select coalesce(sum(r.amount), 0)
            from PaymentRefund r
            where r.subscriptionPayment = :subscriptionPayment
              and r.status in :statuses
            """)
    BigDecimal sumAmountBySubscriptionPaymentAndStatuses(
            @Param("subscriptionPayment") SubscriptionPayment subscriptionPayment,
            @Param("statuses") Collection<PaymentRefundStatus> statuses);
}
