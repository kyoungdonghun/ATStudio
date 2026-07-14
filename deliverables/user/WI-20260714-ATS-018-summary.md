# WI-20260714-ATS-018 Summary

## 결과

결제 명령의 재시도, ambiguous Provider 결과, finalize-only 복구, 동시 요청 수렴을 fake provider 기반 focused 통합 테스트로 독립 검증했습니다.

- 초기 billing confirm 동시 호출은 Provider `confirm` 1회, `charge` 1회로 수렴했고, `PaymentOrder` 1건, `SubscriptionPayment` 1건, `UserSubscription` 1건만 커밋됐습니다.
- upgrade Provider ambiguous 결과는 `PENDING_PROVIDER_CONFIRMATION`으로 커밋되고, 재시도 시 blind replay 없이 Provider `charge` 총 1회로 멈춥니다.
- renewal Provider 성공 후 local finalize 실패는 `PROVIDER_SUCCEEDED`로 남고, 재시도는 Provider 재호출 없이 local finalize만 수행해 `DONE` 1건과 결제 1건으로 수렴합니다.
- renewal 동시 worker는 Provider `charge` 1회, renewal order 1건, 결제 1건으로 수렴합니다.
- refund Provider 예외는 `PENDING_PROVIDER_CONFIRMATION`으로 커밋되고, 재시도는 같은 refund row와 idempotency key를 재사용해 `SUCCEEDED`로 수렴합니다.
- refund 동시 예약은 원 결제 9,900원에 대해 6,000원 예약 1건만 커밋되고, 초과 예약 1건은 거절됐습니다. Provider 호출은 0회입니다.

## 생산 코드 수정

재현된 결함 1건을 결제 범위 안에서 최소 수정했습니다.

- `AdminPaymentRefundService.executeRefund()`가 Provider 호출을 로컬 트랜잭션 안에서 수행하던 문제를 분리했습니다.
- 새 `PaymentRefundTransactionService`가 refund 실행 claim/result 기록을 `REQUIRES_NEW`로 커밋하고, Provider 호출은 `NOT_SUPPORTED` 경계 밖에서 수행합니다.
- Provider 예외, null result, success-without-transaction-id는 durable `PENDING_PROVIDER_CONFIRMATION`으로 기록됩니다.

## 검증

- `.\gradlew.bat compileTestJava`: 1회 통과.
- 이후 focused test 실행 중 다른 작업자의 비소유 `CompanyCertificationSecurityVerificationTest.java:117` 변경으로 `compileTestJava`가 막혀, security 파일은 수정하지 않았습니다.
- WI-018 focused rerun은 기존 컴파일 산출물과 WI-018 테스트 수동 컴파일 후 `-x compileTestJava`로 실행했습니다.
- focused tests: 16개 통과, 실패 0개.

## 남은 제한

이번 검증은 H2/Spring test context 기반입니다. live Toss, 실제 DB, 서버, DB rehearsal은 실행하지 않았습니다. MySQL InnoDB lock wait/deadlock 특성은 WI-021 또는 Phase 7의 별도 승인된 disposable MySQL 검증에서 확인해야 합니다.
