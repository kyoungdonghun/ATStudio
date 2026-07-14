package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.StorageMutation;
import com.atstudio.atstudio.service.storage.StorageMutationState;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StorageMutationRepository extends JpaRepository<StorageMutation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT mutation
            FROM StorageMutation mutation
            WHERE mutation.attemptCount < :maxAttempts
              AND (
                    (mutation.state = :preparedState
                     AND mutation.updatedAt <= :staleBefore
                     AND (mutation.nextAttemptAt IS NULL OR mutation.nextAttemptAt <= :now))
                 OR (mutation.state IN :readyStates
                     AND (mutation.nextAttemptAt IS NULL OR mutation.nextAttemptAt <= :now))
              )
            ORDER BY mutation.nextAttemptAt ASC, mutation.id ASC
            """)
    List<StorageMutation> findRecoveryCandidates(
            @Param("preparedState") StorageMutationState preparedState,
            @Param("readyStates") Collection<StorageMutationState> readyStates,
            @Param("now") LocalDateTime now,
            @Param("staleBefore") LocalDateTime staleBefore,
            @Param("maxAttempts") int maxAttempts,
            Pageable pageable
    );
}
