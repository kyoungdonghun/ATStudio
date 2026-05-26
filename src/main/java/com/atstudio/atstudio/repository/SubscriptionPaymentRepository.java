package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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

    @EntityGraph(attributePaths = {"user", "subscription", "paymentOrder", "billingAgreement"})
    Optional<SubscriptionPayment> findByPaymentOrder(PaymentOrder paymentOrder);

    @EntityGraph(attributePaths = {"user", "subscription", "paymentOrder", "billingAgreement"})
    Optional<SubscriptionPayment> findFirstByPgTransactionId(String pgTransactionId);

    @EntityGraph(attributePaths = {"user", "subscription", "paymentOrder", "billingAgreement"})
    List<SubscriptionPayment> findByPaymentStatusAndCreatedAtBetween(
            PaymentStatus paymentStatus,
            LocalDateTime createdAtFrom,
            LocalDateTime createdAtTo);
}
