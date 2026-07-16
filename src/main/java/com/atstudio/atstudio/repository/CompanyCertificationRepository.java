package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.CompanyCertification;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyCertificationRepository extends JpaRepository<CompanyCertification, Long> {
    Optional<CompanyCertification> findTopByUserOrderByCreatedAtDescIdDesc(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CompanyCertification c WHERE c.user = :user ORDER BY c.createdAt DESC, c.id DESC")
    List<CompanyCertification> findByUserForUpdate(@Param("user") User user, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CompanyCertification c WHERE c.id = :id")
    Optional<CompanyCertification> findByIdForUpdate(@Param("id") Long id);

    boolean existsByUserAndStatusIn(User user, List<CompanyCertificationStatus> statuses);
    Page<CompanyCertification> findByStatus(CompanyCertificationStatus status, Pageable pageable);
}
