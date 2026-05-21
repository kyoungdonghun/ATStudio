package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final RecurringRenewalService recurringRenewalService;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processRecurringRenewals() {
        recurringRenewalService.processDueRenewals();
    }

    /**
     * 매일 00:30에 만료된 구독을 처리한다.
     * recurring renewal job이 먼저 grace/renewal 상태를 정리한 뒤 만료 처리를 수행한다.
     * pending 변경은 결제 성공 갱신 경로에서만 적용하며, 결제 없이 만료된 구독은 EXPIRED로 닫는다.
     */
    @Scheduled(cron = "0 30 0 * * *")
    @Transactional
    public void processExpiredSubscriptions() {
        LocalDate today = LocalDate.now();
        List<UserSubscription> expired = userSubscriptionRepository.findExpired(today);

        int expiredCount = 0;

        for (UserSubscription sub : expired) {
            sub.expire();
            expiredCount++;
        }

        if (expiredCount > 0) {
            log.info("구독 스케줄러 처리 완료: 만료 {}건", expiredCount);
        }
    }
}
