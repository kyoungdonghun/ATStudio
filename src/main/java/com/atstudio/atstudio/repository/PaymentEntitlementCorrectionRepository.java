package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.PaymentEntitlementCorrection;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentEntitlementCorrectionRepository
        extends JpaRepository<PaymentEntitlementCorrection, Long> {

    @EntityGraph(attributePaths = {
            "paymentRefund",
            "subscriptionPayment",
            "paymentOrder",
            "userSubscription",
            "user",
            "beforeSubscription",
            "beforePendingSubscription",
            "targetSubscription",
            "requestedBy",
            "approvedBy",
            "executedBy"
    })
    Page<PaymentEntitlementCorrection> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {
            "paymentRefund",
            "subscriptionPayment",
            "paymentOrder",
            "userSubscription",
            "user",
            "beforeSubscription",
            "beforePendingSubscription",
            "targetSubscription",
            "requestedBy",
            "approvedBy",
            "executedBy"
    })
    Optional<PaymentEntitlementCorrection> findDetailedById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from PaymentEntitlementCorrection c where c.id = :id")
    Optional<PaymentEntitlementCorrection> findByIdForUpdate(@Param("id") Long id);
}
