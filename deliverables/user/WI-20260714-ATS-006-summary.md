# WI-20260714-ATS-006 Summary

## 요약

- `UserSubscriptionService`의 charged upgrade 흐름을 단일 payment command lifecycle로 전환했습니다.
- upgrade Provider 호출은 `SubscriptionUpgradePaymentExecutor`의 `NOT_SUPPORTED` 경계에서 수행되며, local claim/outcome/finalize는 `PaymentCommandTransactionService`의 짧은 트랜잭션으로 분리했습니다.
- 승인된 proration 및 플랜 변경 정책은 유지했습니다. 0원 upgrade는 기존 local-only 전환을 유지하고, 유료 upgrade만 command helper 경로를 사용합니다.

## 완료 범위

- stable upgrade command key 기반 order reuse
- persisted provider attempt/idempotency key 기반 retry
- `PROVIDER_SUCCEEDED` 재시도 시 Provider 재호출 없이 local finalize-only 처리
- deterministic failure 후 명시 재시도 시 같은 order에서 attempt 증가
- 처리 중 중복 요청의 Provider 중복 호출 방지
- removed billing-key 실패 시 기존 reauth 정책 보존

## 검증

- `.\gradlew.bat compileJava compileTestJava` PASS
- `.\gradlew.bat compileTestJava` PASS
- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest" --tests "com.atstudio.atstudio.service.SubscriptionUpgradeCommandIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentCommandKeyFactoryTest"` PASS
- `git diff --check -- <WI-006 owned files>` PASS

## 범위 밖

- renewal(WI-007), 실제 DB 적용, disposable/live MySQL 증명, live Toss 호출은 수행하지 않았습니다.
- 테스트는 H2 test context에서 수행되었고 실제 로컬 MySQL에는 접근하지 않았습니다.
