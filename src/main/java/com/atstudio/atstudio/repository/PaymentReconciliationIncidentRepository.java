package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.PaymentReconciliationIncident;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentReconciliationIncidentRepository extends JpaRepository<PaymentReconciliationIncident, Long> {

    Optional<PaymentReconciliationIncident> findByDedupeKey(String dedupeKey);

    @EntityGraph(attributePaths = {"user", "paymentOrder", "billingAgreement"})
    Page<PaymentReconciliationIncident> findAllByOrderByLastDetectedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "paymentOrder", "billingAgreement"})
    Page<PaymentReconciliationIncident> findByStatusOrderByLastDetectedAtDesc(
            PaymentReconciliationIncidentStatus status,
            Pageable pageable);
}
