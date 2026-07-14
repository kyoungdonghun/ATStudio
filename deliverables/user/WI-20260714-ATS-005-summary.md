# WI-20260714-ATS-005 Summary

## 요약
- WI-005 인계 구현을 리뷰하고, 초기 billing confirm 경로가 provider 호출과 로컬 DB 트랜잭션을 분리하도록 구성되어 있음을 확인했다.
- 범위 내 결함은 추가로 발견되지 않아 코드 수정은 하지 않았다.
- 집중 테스트, Java 컴파일, diff check를 실행했고 모두 통과했다.

## 확인한 핵심 요구사항
- `BillingAgreementApplicationService.confirmBillingAgreement()`는 `NOT_SUPPORTED`로 실행되어 provider `confirmAgreement`, `charge`, cleanup `cancelAgreement` 호출이 로컬 트랜잭션 밖에서 수행된다.
- `PaymentCommandTransactionService`는 별도 Spring bean으로 `REQUIRES_NEW` claim, billing-key 저장, provider outcome 기록, finalize, cleanup incident 기록을 수행한다.
- provider 성공 후 local finalization 실패 시 `PaymentOrder`가 `PROVIDER_SUCCEEDED`로 남고, 재시도는 provider를 다시 호출하지 않고 local finalize만 수행한다.
- 15분 이상 stale `PROCESSING` billing confirm은 `PENDING_PROVIDER_CONFIRMATION`으로 전환되고 blind replay/provider 재호출을 하지 않는다.
- 초기 charge 실패 후 billing-key cleanup 실패는 encrypted key를 보존하고 reconciliation incident로 남긴다.

## 변경 파일
- 추가: `deliverables/user/WI-20260714-ATS-005-summary.md`
- 추가: `deliverables/agent/WI-20260714-ATS-005-evidence-pack.md`
- 코드 파일은 이번 인계 마감 단계에서 추가 수정하지 않았다.

## 검증
- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.BillingAgreementApplicationServiceTest" --tests "com.atstudio.atstudio.service.BillingAgreementFailurePersistenceIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentProviderSuccessRecoveryIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentCommandKeyFactoryTest"`: PASS, 16 tests
- `.\gradlew.bat compileJava compileTestJava`: PASS
- `git diff --check`: PASS

## 남은 위험
- live Toss, 실제 MySQL, SMTP, 서버 실행은 사용하지 않았다.
- disposable MySQL/concurrency proof는 후속 WI-018/WI-021 범위로 남아 있다.
- WI-006 범위인 upgrade 변경은 수행하지 않았다.
