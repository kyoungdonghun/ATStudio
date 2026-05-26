package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.PaymentSettlement;
import com.atstudio.atstudio.entity.enums.PaymentSettlementSource;
import com.atstudio.atstudio.entity.enums.PaymentSettlementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PaymentSettlementRepository extends JpaRepository<PaymentSettlement, Long> {

    boolean existsByDeduplicationKey(String deduplicationKey);

    boolean existsByOrderIdAndSourceNot(String orderId, PaymentSettlementSource source);

    @EntityGraph(attributePaths = {"user", "paymentOrder", "subscriptionPayment", "ignoredBy"})
    Optional<PaymentSettlement> findWithGraphById(Long id);

    @EntityGraph(attributePaths = {"user", "paymentOrder", "subscriptionPayment", "ignoredBy"})
    @Query("""
            select s from PaymentSettlement s
            where (:status is null or s.status = :status)
              and (:source is null or s.source = :source)
              and (:baseDateFrom is null or s.settlementBaseDate >= :baseDateFrom)
              and (:baseDateTo is null or s.settlementBaseDate <= :baseDateTo)
            order by s.createdAt desc
            """)
    Page<PaymentSettlement> search(
            @Param("status") PaymentSettlementStatus status,
            @Param("source") PaymentSettlementSource source,
            @Param("baseDateFrom") LocalDate baseDateFrom,
            @Param("baseDateTo") LocalDate baseDateTo,
            Pageable pageable);
}
