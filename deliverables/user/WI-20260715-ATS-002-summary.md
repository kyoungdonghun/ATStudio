# WI-20260715-ATS-002 완료 요약

## 완료 판정

- Package B의 F-01 갱신 식별자, canonical lock, upgrade target cycle, cleanup/stale projection, reconciliation-safe finalizer 계약을 구현했습니다.
- HEAD `103fdf4` Package A 기반에서 작업했으며 Package E 소유 파일과 기존 미추적 로그를 변경하거나 되돌리지 않았습니다.
- retained/local/production DB, live Toss, preview/public 서버에는 접근하거나 변경하지 않았습니다.

## 주요 변경

- 갱신 기간은 `nextBillingAt`으로 고정하고 `renewalRetryAt`은 정확한 `FAILED` 주문의 재시도 게이트로만 사용합니다.
- due query는 신규 exact period, retry-due `FAILED`, exact `PROVIDER_SUCCEEDED`만 선택합니다. `PROCESSING`과 `PENDING_PROVIDER_CONFIRMATION`은 자동 재청구에서 제외됩니다.
- 2026-08-17 실패 후 2026-08-18 재시도 시 동일 order ID, command key, billing period를 유지하고 provider attempt/key만 `1 -> 2`로 전진하는 통합 테스트를 추가했습니다.
- 갱신 실패, upgrade finalizer, renewal finalizer를 `BillingAgreement -> UserSubscription -> PaymentOrder -> SubscriptionPayment` 잠금 순서로 정렬했습니다.
- 신규 upgrade 주문에 `upgradeTargetBillingCycle`을 저장하고 finalizer가 호출자 인자 대신 잠긴 주문의 값을 사용하도록 변경했습니다.
- 기존 Package D 호출부 컴파일을 위해 4-인자 finalizer는 임시 호환 진입점으로 유지하지만, 전달된 cycle은 최종화 판단에 사용하지 않습니다.
- provider transaction 중복 소유와 기존 payment row를 `PESSIMISTIC_WRITE`로 검증한 뒤 finalization합니다.
- Package C가 사용할 bounded withdrawal cleanup/stale lease projection과 Package F가 사용할 command lock/reconciliation candidate 및 reconciled-success 진입점을 추가했습니다.
- renewal provider 호출 orchestration은 외부 transaction을 suspend하지 않고 `Propagation.NEVER`로 거부합니다.

## 검증

- Package B focused tests: PASS, 24/24, failures/errors/skips 0.
- 영향권 regression: upgrade, initial billing, failure persistence, entity/DDL, payment lock 6개 test class 모두 PASS.
- `./gradlew.bat compileJava`: PASS, `BUILD SUCCESSFUL in 1s`.
- `git diff --check`: PASS. 공백 오류는 없고 기존 LF-to-CRLF working-copy 안내만 출력됐습니다.

## 잔여 위험 및 후속

- focused integration tests는 ephemeral H2 기반입니다. MySQL/InnoDB lock 및 정확한 race loser 증명은 Package G 범위입니다.
- Package D가 3-인자 persisted-target finalizer로 이동하면 임시 4-인자 호환 overload를 제거해야 합니다.
- Package A 엔티티에는 retry gate만 지우는 전이가 없습니다. 따라서 재시도 claim 뒤 `PROCESSING` 또는 `PENDING_PROVIDER_CONFIRMATION` 동안 소비된 `renewalRetryAt` 값이 남을 수 있으나, exact status query가 이를 자동 재청구에 사용하지 않습니다. strict null-state parity가 필요하면 Package A 소유 파일의 후속 변경이 필요합니다.
- 실제 DB DDL, copied/disposable MySQL, live provider, preview 서버 검증은 수행하지 않았습니다.
