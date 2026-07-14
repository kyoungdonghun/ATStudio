# WI-20260714-ATS-007 Summary

## 요약

갱신 결제 배치를 계약별로 격리하도록 구현했습니다. 갱신 대상은 bounded keyset 방식으로 ID만 조회하고, 각 계약은 짧은 `REQUIRES_NEW` 단계로 claim, provider outcome 기록, local finalize를 분리합니다.

## 구현 내용

- 갱신 주문 identity를 `billingAgreement + userSubscription + purpose + billingPeriodStart`로 고정했습니다.
- 기존 기간/다른 구독의 renewal order를 현재 갱신에 재사용하지 않도록 exact-period lock lookup을 추가했습니다.
- `SubscriptionScheduler.processRecurringRenewals()`가 배치 트랜잭션을 소유하지 않게 했습니다.
- `PROCESSING` stale claim은 자동 재청구하지 않고 `PENDING_PROVIDER_CONFIRMATION`으로 남긴 뒤 중단합니다.
- 계약별 최외곽 예외 처리를 추가해 한 계약의 `FINALIZE_ONLY`, provider 성공 저장, local finalize 실패가 뒤 계약 처리를 멈추지 않게 했습니다.
- provider `charge()`가 `null`을 반환하거나 success 결과의 transaction ID가 blank인 경우 `PENDING_PROVIDER_CONFIRMATION`으로 내구성 있게 기록하고 다음 계약을 계속 처리합니다.

## 검증

- `.\gradlew.bat compileTestJava` 통과
- focused tests 통과:
  - `RecurringRenewalServiceTest`
  - `RecurringRenewalCommandIntegrationTest`
  - `SubscriptionSchedulerTest`
  - `BillingAgreementRepositoryTest`
  - `PaymentCommandKeyFactoryTest`
- `git diff --check` 통과

## 범위 밖

- 멀티서버 scheduler lock, distributed lease, live Toss 호출, 실제 DB 적용은 수행하지 않았습니다.
- 다른 payment initial/upgrade/refund, storage, acceptance, auth, image 변경은 되돌리거나 덮어쓰지 않았습니다.
