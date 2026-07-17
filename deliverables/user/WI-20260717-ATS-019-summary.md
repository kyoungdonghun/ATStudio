# WI-20260717-ATS-019 완료 요약

## 결과

Acceptance lifecycle의 backend 환경변수 allowlist를 현재 V2 결제 설정 계약에 맞췄습니다.

- 현재 이름 4개를 optional allowlist에 추가했습니다.
  - `PAYMENT_BILLING_KEY_ACTIVE_KEY_ID`
  - `PAYMENT_BILLING_KEY_0_ID`
  - `PAYMENT_BILLING_KEY_0_SECRET`
  - `APP_PAYMENT_SCHEDULER_ZONE`
- 폐기된 `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET`은 allowlist에서 제거했습니다.
- 새 이름은 backend child에만 전달되고 tunnel/frontend에는 전달되지 않으며, child 생성 후 launcher 환경이 원래 상태로 복원되는 것을 테스트로 고정했습니다.
- 애플리케이션의 `BillingKeyCrypto`, `AcceptanceStartupGuard`, business code, 설정 파일, DB는 수정하지 않았습니다.

## 원인 경계

stale acceptance DB와 이번 문제는 별개입니다.

- 이전 stale DB는 Hibernate 검증은 통과했지만 fresh V1 manifest와 물리적으로 완전히 일치하지 않았던 선행 이슈였습니다.
- 해당 DB는 WI-016에서 승인된 fresh V1 baseline으로 재생성됐습니다.
- WI-019의 직접 원인은 그 이후에도 acceptance launcher allowlist가 legacy 이름을 유지하고 현재 V2 이름을 받지 못한 계약 드리프트였습니다.
- 애플리케이션의 fail-closed 검증은 정상적인 안전 경계이므로 완화하거나 우회하지 않았습니다.

## 검증

- `test-backend-environment.ps1`: 통과
  - 새 4개 이름 수용
  - backend-only 전달과 child isolation
  - launcher 환경 복원
  - obsolete 이름 거부
- `test-dry-run.ps1`: 통과
  - secret-free dry-run 출력 계약 포함
- Java focused test classes 3개: 통과
  - `AcceptanceStartupGuardTest`
  - `V1BackendBaselineContractTest`
  - `BillingKeyCryptoTest`
- `git diff --check`: 통과

실제 운영자 secret 값, 외부 bundle 내용, raw URL은 읽거나 산출물에 기록하지 않았습니다. 테스트는 synthetic marker만 사용했습니다.

## 위험 및 롤백

- 이번 결과는 launcher 계약 수정과 focused regression 검증까지이며, 전체 acceptance 실행이나 production readiness를 증명하지 않습니다.
- 롤백이 필요하면 현재 diff를 확인한 뒤 두 PowerShell 파일의 WI-019 hunk만 되돌려야 합니다. 다른 작업자의 변경이나 애플리케이션 fail-closed 검증은 건드리면 안 됩니다.
- 롤백하면 WI-018의 acceptance startup이 다시 launcher allowlist 단계에서 막힐 수 있습니다.

## 다음 상태

WI-019는 완료됐으며 WI-018 acceptance execution을 재개할 수 있습니다. WI-018에서 fresh V1 DB와 operator-managed V2 bundle을 사용해 startup readiness, local/public proxy, 역할별 API/UI smoke test, secret-safe 로그, runtime cleanup 상태를 계속 검증해야 합니다.
