package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    /**
     * ACTIVE 또는 CANCELLED 상태이면서 expiresAt이 아직 지나지 않은 구독을 조회한다.
     * CANCELLED 상태라도 expiresAt까지는 서비스 이용이 가능하다 (BD-1 유예 기간).
     */
    @Query("SELECT us FROM UserSubscription us JOIN FETCH us.subscription " +
           "WHERE us.user = :user AND us.status IN ('ACTIVE', 'CANCELLED') AND us.expiresAt >= :today")
    Optional<UserSubscription> findActiveByUser(
            @Param("user") User user,
            @Param("today") LocalDate today);

    @EntityGraph(attributePaths = {"user", "subscription"})
    Page<UserSubscription> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "subscription"})
    Optional<UserSubscription> findById(Long id);
}
