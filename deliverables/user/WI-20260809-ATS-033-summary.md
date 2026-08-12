---
version: 1.0
last_updated: 2026-08-12
project: ATS
owner: docops
category: work-summary
status: stable
related_wi: WI-20260809-ATS-033
dependencies:
  - path: ../agent/WI-20260809-ATS-033-evidence-pack.md
    reason: 상세 구현, 검증, 위험, 롤백 근거
  - path: ../agent/WI-20260809-ATS-033-re-review.md
    reason: 독립 RE 최종 승인
---

# WI-20260809-ATS-033 사용자 요약

## 결과

WI-033 결제 준비 멱등성과 중복 주문 제어를 완료했습니다. 독립 RE는 기존
BLOCK 6건이 모두 해소되었다고 확인하고 최종 `APPROVE`했습니다.

## 변경된 계약

- UI: 체크아웃 시도마다 소문자 표준 UUIDv4 키 하나를 `sessionStorage`에
  유지합니다. StrictMode 재마운트, 새로고침, 네트워크 재시도, 같은 시도
  재시도에서는 키가 바뀌지 않습니다.
- 키 교체: 로컬 저장값 손상, `PAYMENT_PREPARE_IDEMPOTENCY_KEY_INVALID`,
  `PAYMENT_ORDER_EXPIRED`, `PAYMENT_ORDER_TERMINAL`에서만 사용자가 명시적으로
  새 시도를 눌러 교체합니다. tuple 충돌, 임의 409, Provider/네트워크 오류,
  결과 불명, 처리 중 상태는 기존 키를 유지합니다.
- API/서버: `Idempotency-Key`는 필수 헤더이며 body에는 넣지 않습니다.
  잘못된 값은 DB/Provider 접근 전에 안정적인 오류로 거절합니다.
- 저장 상태: 인증 owner를 포함해 해시한
  `BILLING_PREPARE:v1:<sha256>`만 저장합니다. raw key는 저장하거나
  로그에 남기지 않습니다. 같은 owner/key/exact tuple은 같은 주문을
  재사용하고, 같은 owner의 tuple 불일치는 409
  `PAYMENT_PREPARE_ATTEMPT_CONFLICT`, 다른 owner는 독립된 영역입니다.
- Confirm: non-null prepare `command_key`는 그대로 유지합니다. legacy null
  주문만 `BILLING_CONFIRM:<orderID>`를 한 번 사용하며, 실제 Provider 시도
  fence인 `provider_idempotency_key`와 분리됩니다.
- 동시성: 첫 Billing Agreement는 nonlocking probe 후 기존 named unique
  constraint insert/flush로 승자를 정하고, 패자만 제한된 fresh transaction
  재조회로 수렴합니다. 이후 lock 순서는
  `BillingAgreement -> UserSubscription -> PaymentOrder`이며 Provider prepare
  descriptor는 로컬 transaction 밖에서만 실행됩니다. 스키마는 바꾸지
  않았습니다.

## 검증

- 독립 RE 최종 `APPROVE`, 기존 BLOCK 6건 모두 resolved.
- disposable MySQL 8.0.45/InnoDB/`REPEATABLE_READ`: 41 tables, 493 columns,
  168 indexes, 89 FKs, 6 plans manifest PASS. 동시성 3개 테스트 전부 PASS,
  guarded drop PASS, 잔여 임시 디렉터리/프로세스 0.
- 집중 백엔드 10 suites/113 tests PASS, 집중 프런트엔드 6 files/107 tests
  PASS, 관련 썸네일 회귀 5회 x 9 = 45 PASS.
- 전체 백엔드 174 suites/1,445 tests, conditional skip 16, 실패/오류 0.
  JaCoCo line 86.517%, method 83.895%, branch 71.353%, build PASS.
- 전체 프런트엔드 72 files/660 tests PASS. statements 87.60%, branches
  78.33%, functions 87.15%, lines 89.69%. typecheck, ESLint, Prettier, build PASS.
- 전체 gate에서 빠진 integration context `@MockitoBean` wiring 2건을 보정해
  관련 6/6을 다시 통과했습니다. 기존 이미지 cached-load/effect race는
  `useLayoutEffect`로 최소 보정했고 45회 및 전체 suite가 통과했습니다.
- 문서 validator PASS. 지정 5파일 `git diff --check` PASS: 줄바꿈 경고만
  있고 공백 오류는 0입니다. 세 계약 문서의 낡은 WI-033 future/out-of-scope
  표현은 검색 및 의미 검토 결과 0건입니다.

## 위험과 증거 한계

실제 Toss/SDK, 실결제, 환불, 취소, 메일, 배포, retained DB, secret은
사용하지 않았습니다. 따라서 근거는 자동 테스트, Provider double, H2,
일회용 MySQL 환경에 한정됩니다.

WI-033은 prepare replay와 로컬 claim만 해결합니다. confirm 이후 callback
응답 유실, 금융 결과 불명, 새로고침 복구, reconciliation은 WI-034 범위이며
처리 중/결과 불명 상태에서는 새 prepare 시도를 만들지 않습니다.

## 롤백과 후속

롤백은 WI-033 제품/테스트 변경과 이번 문서 5개에만 한정합니다. 스키마와
기존 데이터, 실제 Provider 상태를 바꾸지 않았으므로 migration 취소,
데이터 삭제, 결제 취소, 환불은 필요하지 않습니다.

다음 WI는 `WI-20260809-ATS-034`이며 callback response-loss, unknown
financial outcome, reload recovery, reconciliation을 별도로 처리합니다.

## 관련 문서

- [상세 Evidence Pack](../agent/WI-20260809-ATS-033-evidence-pack.md)
- [Payment Integration Design](../../docs/design/payment-integration-design.md)
- [API Specification](../../docs/design/api-spec.md)
- [Screen Flows](../../docs/ui/screen-flow.md)
