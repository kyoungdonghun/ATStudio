package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.AdminSubscriptionCorrection;
import com.atstudio.atstudio.entity.enums.AdminSubscriptionCorrectionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AdminSubscriptionCorrectionRepository
        extends JpaRepository<AdminSubscriptionCorrection, Long> {

    interface ExecutionLockProjection {
        Long getCorrectionID();

        Long getUserID();

        Long getUserSubscriptionID();

        Long getTargetSubscriptionID();

        Long getBillingAgreementID();
    }

    @EntityGraph(attributePaths = {
            "userSubscription",
            "user",
            "billingAgreement",
            "beforeSubscription",
            "beforePendingSubscription",
            "targetSubscription",
            "requestedBy",
            "approvedBy",
            "executedBy"
    })
    Page<AdminSubscriptionCorrection> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {
            "userSubscription",
            "user",
            "billingAgreement",
            "beforeSubscription",
            "beforePendingSubscription",
            "targetSubscription",
            "requestedBy",
            "approvedBy",
            "executedBy"
    })
    Optional<AdminSubscriptionCorrection> findDetailedById(Long id);

    @EntityGraph(attributePaths = {
            "userSubscription",
            "user",
            "billingAgreement",
            "beforeSubscription",
            "beforePendingSubscription",
            "targetSubscription",
            "requestedBy",
            "approvedBy",
            "executedBy"
    })
    Optional<AdminSubscriptionCorrection>
            findFirstByUserSubscription_IdAndStatusInOrderByCreatedAtDescIdDesc(
                    Long userSubscriptionID,
                    Set<AdminSubscriptionCorrectionStatus> statuses);

    @Query("select correction.id as correctionID, correction.user.id as userID, "
            + "correction.userSubscription.id as userSubscriptionID, "
            + "correction.targetSubscription.id as targetSubscriptionID, "
            + "agreement.id as billingAgreementID "
            + "from AdminSubscriptionCorrection correction "
            + "left join correction.billingAgreement agreement "
            + "where correction.id = :correctionID")
    Optional<ExecutionLockProjection> findExecutionLockProjectionByID(
            @Param("correctionID") Long correctionID);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select correction from AdminSubscriptionCorrection correction "
            + "where correction.id = :correctionID")
    Optional<AdminSubscriptionCorrection> findByIDForUpdate(@Param("correctionID") Long correctionID);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select correction from AdminSubscriptionCorrection correction "
            + "where correction.userSubscription.id = :userSubscriptionID "
            + "and correction.status in :statuses "
            + "order by correction.id")
    List<AdminSubscriptionCorrection> findNonTerminalByUserSubscriptionIDForUpdate(
            @Param("userSubscriptionID") Long userSubscriptionID,
            @Param("statuses") Set<AdminSubscriptionCorrectionStatus> statuses);

    long countByUserSubscription_Id(Long userSubscriptionID);
}
