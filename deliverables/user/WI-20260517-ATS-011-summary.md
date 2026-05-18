# WI-20260517-ATS-011 Summary

정기 결제 갱신 스케줄러와 실패 정책을 구현했습니다.

- `RecurringRenewalService`를 추가했습니다.
  - `ACTIVE` billing agreement 중 `nextBillingAt <= today`인 대상을 조회합니다.
  - `RENEWAL` payment order를 생성하거나 기존 열린 renewal order를 재사용해 중복 결제를 막습니다.
  - billing key를 복호화해 `RecurringPaymentProvider.charge`를 호출합니다.
- 갱신 성공 정책을 구현했습니다.
  - `SubscriptionPayment`를 저장합니다.
  - `UserSubscription`의 구독 기간을 다음 주기로 연장합니다.
  - `BillingAgreement.nextBillingAt`을 다음 만료일로 갱신하고 failure count를 0으로 초기화합니다.
- 갱신 실패 정책을 구현했습니다.
  - 실패 order를 기록하고 `failureCount`를 증가시킵니다.
  - 첫 실패부터 구독 `expiresAt`을 원 결제 예정일 + 3일 grace로 연장합니다.
  - 다음 재시도일을 grace 안쪽으로 예약합니다.
  - 3회 실패 또는 grace 만료 시 자동결제를 `SUSPENDED`로 전환합니다.
  - 구독은 grace 종료 이후에만 `EXPIRED` 처리되도록 했습니다.
- `SubscriptionScheduler`를 조정했습니다.
  - 정기 갱신은 매일 00:00에 먼저 실행합니다.
  - 기존 만료/다운그레이드 처리는 00:30에 실행해 갱신 처리와 충돌하지 않게 했습니다.

검증:

- `./gradlew.bat test --tests "com.atstudio.atstudio.service.RecurringRenewalServiceTest" --tests "com.atstudio.atstudio.service.SubscriptionSchedulerTest"` 통과.
- 결제/구독 관련 회귀 묶음 테스트 통과.
- `./gradlew.bat test` 전체 통과.

다음 흐름:

- WI-20260517-ATS-012에서 API/design/db 문서 현행화와 프론트엔드 billing UX 연결 범위를 정리하면 됩니다.
