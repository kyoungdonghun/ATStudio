# WI-20260724-ATS-001 완료 요약

## 결과

Acceptance launcher의 backend 환경변수 계약에서 폐기된 단건 결제 호환 이름을 제거했습니다.

- `APP_PAYMENT_PROVIDER`를 optional allowlist에서 제거했습니다.
- `TOSS_CONFIRM_URL`을 optional allowlist에서 제거했습니다.
- 현재 V2 빌링키와 결제 스케줄러 환경변수는 계속 수용합니다.
- 제품의 결제 동작, DB, application 설정, 외부 환경변수 번들은 수정하지 않았습니다.

## 회귀 방지

PowerShell 계약 테스트는 다음 세 이름이 allowlist에 없고, 합성 번들에 포함되면 각각 거부되는지 검증합니다.

- `APP_PAYMENT_PROVIDER`
- `TOSS_CONFIRM_URL`
- `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET`

Java V1 기준선 테스트도 acceptance launcher가 현재 V2 이름은 유지하고 위 폐기 이름은 포함하지 않는지 확인합니다. SR-93의 현재 상태 설명은 launcher가 폐기 별칭을 호환하지 않는다는 사실에 맞게 갱신했습니다.

## 검증

- `scripts/acceptance/test-backend-environment.ps1`: 통과
- `scripts/acceptance/test-dry-run.ps1`: 통과
- `V1BackendBaselineContractTest`: 6개 테스트 통과, 실패·오류·건너뜀 0
- 대상 파일 `git diff --check`: 통과

검증은 테스트가 생성한 합성 값만 사용했습니다. 외부 번들의 값은 읽거나 출력하지 않았습니다.

## 외부 번들 영향

외부 번들을 자동 수정하지 않았습니다.

- 이미 현재 allowlist 이름만 사용하는 번들은 재생성할 필요가 없습니다.
- `APP_PAYMENT_PROVIDER` 또는 `TOSS_CONFIRM_URL`을 포함한 기존 번들은 다음 acceptance 실행 전에 운영자가 해당 속성을 제거하거나 번들을 재생성해야 합니다.
- 폐기 이름이 남은 번들은 의도적으로 fail-closed 방식으로 거부됩니다.

## 위험 및 롤백

- 위험은 오래된 외부 번들이 새 계약에서 거부될 수 있다는 점이며, V1에서는 이를 호환 대상으로 유지하지 않는 것이 승인된 정책입니다.
- 롤백하려면 이 WI가 변경한 launcher allowlist, 집중 테스트, V1 기준선 테스트, SR-93 문구만 함께 되돌려야 합니다.
- 롤백하면 폐기된 단건 결제 별칭이 acceptance 실행 경로로 다시 유입될 수 있으므로 권장하지 않습니다.

## 다음 상태

WI-20260724-ATS-001은 완료됐습니다. 이 결과는 WI-20260724-ATS-004의 backend/acceptance 전체 검증과 WI-20260724-ATS-006의 문서 정합성 검증으로 이어집니다.
