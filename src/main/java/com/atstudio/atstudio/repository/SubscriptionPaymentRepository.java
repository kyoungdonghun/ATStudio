package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, Long> {

    boolean existsByUser(User user);

    boolean existsByPaymentOrder(PaymentOrder paymentOrder);

    @EntityGraph(attributePaths = {"user", "subscription", "paymentOrder", "billingAgreement"})
    Page<SubscriptionPayment> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
