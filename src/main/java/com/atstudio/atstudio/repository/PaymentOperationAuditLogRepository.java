package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.PaymentOperationAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentOperationAuditLogRepository extends JpaRepository<PaymentOperationAuditLog, Long> {

    @EntityGraph(attributePaths = {
            "actorUser",
            "targetUser",
            "paymentOrder",
            "subscriptionPayment",
            "reconciliationIncident"
    })
    Page<PaymentOperationAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
