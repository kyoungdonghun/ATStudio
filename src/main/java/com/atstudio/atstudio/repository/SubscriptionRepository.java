package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findAllByIsActive(boolean isActive);
    List<Subscription> findAllByUserTypeAndIsActive(UserType userType, boolean isActive);
}
