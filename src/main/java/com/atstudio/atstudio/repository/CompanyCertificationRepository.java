package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.CompanyCertification;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyCertificationRepository extends JpaRepository<CompanyCertification, Long> {
    Optional<CompanyCertification> findTopByUserOrderByCreatedAtDescIdDesc(User user);
    boolean existsByUserAndStatusIn(User user, List<CompanyCertificationStatus> statuses);
    Page<CompanyCertification> findByStatus(CompanyCertificationStatus status, Pageable pageable);
}
