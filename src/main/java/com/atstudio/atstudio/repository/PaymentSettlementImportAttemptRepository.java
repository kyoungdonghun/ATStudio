package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.PaymentSettlementImportAttempt;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentSettlementImportAttemptRepository
        extends JpaRepository<PaymentSettlementImportAttempt, Long> {

    @EntityGraph(attributePaths = "actorUser")
    Optional<PaymentSettlementImportAttempt> findByKeyDigest(String keyDigest);

    @EntityGraph(attributePaths = "actorUser")
    Optional<PaymentSettlementImportAttempt> findWithActorById(Long id);

    @EntityGraph(attributePaths = "actorUser")
    Page<PaymentSettlementImportAttempt> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select attempt from PaymentSettlementImportAttempt attempt where attempt.id = :id")
    Optional<PaymentSettlementImportAttempt> findByIdForUpdate(@Param("id") Long id);
}
