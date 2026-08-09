package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.enums.UserType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findAllByIsActive(boolean isActive);
    List<Subscription> findAllByUserTypeAndIsActive(UserType userType, boolean isActive);
    Optional<Subscription> findByNameAndUserTypeAndIsActiveTrue(String name, UserType userType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select subscription from Subscription subscription where subscription.id = :id")
    Optional<Subscription> findByIdForUpdate(@Param("id") Long id);
}
