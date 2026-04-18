package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findAllByIsActive(boolean isActive);
    List<Subscription> findAllByUserTypeAndIsActive(UserType userType, boolean isActive);
    Optional<Subscription> findByNameAndUserTypeAndIsActiveTrue(String name, UserType userType);
}
