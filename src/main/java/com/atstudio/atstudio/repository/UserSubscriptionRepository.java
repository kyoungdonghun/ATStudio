package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    @Query("SELECT us FROM UserSubscription us JOIN FETCH us.subscription " +
           "WHERE us.user = :user AND us.status = :status AND us.expiresAt >= :today")
    Optional<UserSubscription> findActiveByUser(
            @Param("user") User user,
            @Param("status") SubscriptionStatus status,
            @Param("today") LocalDate today);

    @EntityGraph(attributePaths = {"user", "subscription"})
    Page<UserSubscription> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "subscription"})
    Optional<UserSubscription> findById(Long id);
}
