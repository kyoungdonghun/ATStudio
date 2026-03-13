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

    /**
     * 매일 자정에 만료된 구독을 처리한다.
     * - pending 다운그레이드가 있으면 새 플랜으로 전환
     * - 없으면 EXPIRED 상태로 변경
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processExpiredSubscriptions() {
        LocalDate today = LocalDate.now();
        List<UserSubscription> expired = userSubscriptionRepository.findExpired(today);

        int downgraded = 0;
        int expiredCount = 0;

        for (UserSubscription sub : expired) {
            if (sub.hasPending()) {
                sub.applyPending();
                downgraded++;
            } else {
                sub.expire();
                expiredCount++;
            }
        }

        if (downgraded > 0 || expiredCount > 0) {
            log.info("구독 스케줄러 처리 완료: 다운그레이드 {}건, 만료 {}건", downgraded, expiredCount);
        }
    }
}
