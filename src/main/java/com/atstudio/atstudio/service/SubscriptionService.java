package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.subscription.SubscriptionResponse;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    // -- 6.1 GET /api/subscriptions ------------------------------------------

    public List<SubscriptionResponse> getActiveSubscriptions(String userType) {
        if (userType != null && !userType.isBlank()) {
            UserType type = UserType.valueOf(userType);
            return subscriptionRepository.findAllByUserTypeAndIsActive(type, true).stream()
                    .map(SubscriptionResponse::from)
                    .toList();
        }
        return subscriptionRepository.findAllByIsActive(true).stream()
                .map(SubscriptionResponse::from)
                .toList();
    }

    // -- 6.2 GET /api/subscriptions/{subscriptionId} -------------------------

    public SubscriptionResponse getSubscription(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .map(SubscriptionResponse::from)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));
    }
}
