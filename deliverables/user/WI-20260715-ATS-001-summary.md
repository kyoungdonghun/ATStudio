# WI-20260715-ATS-001 완료 요약

## 완료 판정

- Package A의 엔티티, fresh DDL, 수동 패치, focused contract test 구현을 완료했습니다.
- 인수 리뷰에서 확인된 Package A 수동 패치 순서 결함을 보정했습니다.
- retained/local/production DB, copied/disposable DB, preview 서버는 실행하거나 변경하지 않았습니다.

## 주요 변경

- `BillingAgreement`에 갱신 재시도일과 billing-key cleanup 상태/lease를 추가했습니다.
- 갱신 실패 시 `nextBillingAt`을 바꾸지 않고 `renewalRetryAt`만 설정하도록 기간 정체성을 보존했습니다.
- `PaymentOrder`에 upgrade target billing cycle과 reconciliation provider-success 전이를 추가했습니다.
- `PaymentRefund`에 초 단위 processing lease, stale result fencing, 결과 확정 시 lease 해제를 추가했습니다.
- `schema.sql`에 설계 Section 9.1의 컬럼, ENUM 값, 인덱스를 반영했습니다.
- 수동 패치를 기존 command 준비 -> Package A 사전 판별/중단 -> A 컬럼 생성 -> 정확한 repair/backfill -> 인덱스 -> 사후 검증 순서로 분리했습니다.
- legacy renewal 후보가 정확히 하나인지 A 컬럼 생성 전에 확인하고, 결제 주문과 구독의 사용자도 billing agreement 사용자와 일치하도록 판별식을 강화했습니다.

## 동작 영향

- 같은 갱신 기간의 재시도일과 실제 결제 기간이 분리되어 downstream Package B가 동일 order/command를 재사용할 기반이 생겼습니다.
- billing-key cleanup과 refund 처리에는 상태와 lease fencing 기반이 생겼습니다.
- 신규 upgrade 주문은 downstream 구현에서 target billing cycle을 반드시 저장해야 합니다.
- 이번 WI는 기반 작업만 완료하며 provider 호출, 갱신/취소/환불/reconciliation orchestration은 변경하지 않았습니다.

## 검증

- `.\gradlew.bat test --tests "com.atstudio.atstudio.entity.PaymentDatabaseIntegrityContractTest"`: PASS, 8/8, `BUILD SUCCESSFUL in 4s`.
- `.\gradlew.bat compileJava`: PASS, `BUILD SUCCESSFUL in 1s`.
- `git diff --check`: PASS. 공백 오류는 없고 기존 LF-to-CRLF 안내만 출력됐습니다.
- 신규 enum과 완료 산출물의 no-index whitespace 검사도 PASS했습니다.

## 위험 및 후속 의존성

- 수동 패치는 이 WI에서 어떤 DB에도 실행하지 않았습니다. copied/disposable MySQL 8 rehearsal과 Hibernate validate는 별도 승인된 검증에서 수행해야 합니다.
- Package A만으로 F-01~F-05가 종료되지는 않습니다. command/refund/reconciliation 서비스가 새 기간·target cycle·lease 계약을 사용해야 합니다.
- handoff 기준 `WI-20260715-ATS-002`, `WI-20260715-ATS-005`가 다음 진행 대상으로 해제됩니다.
- 추후 additive DDL이 적용된 환경에서는 애플리케이션을 먼저 롤백하고, 결제 컬럼·인덱스·ENUM·감사 증거는 보존해야 합니다. 장애 중 destructive schema contraction은 금지합니다.
