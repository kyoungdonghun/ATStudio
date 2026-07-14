# WI-20260714-ATS-002 결제/DB 무결성 설계 요약

## 결론

`ATS020-P1-05`~`P1-10`을 구현할 수 있도록 결제 명령, 갱신, 환불,
MySQL DDL 계약을 하나의 설계로 고정했다. 핵심은 외부 Provider 호출을
Spring DB 트랜잭션 밖으로 분리하고, 호출 전 claim, Provider 결과 저장,
로컬 최종화를 각각 짧은 `REQUIRES_NEW` 트랜잭션으로 처리하는 것이다.

## 확정한 계약

1. **명령 원장:** 초기 confirm, upgrade, renewal은 각각 하나의
   `payment_orders` 행과 결정적 `command_key`를 가진다.
2. **Provider 성공 보존:** Provider 성공을 `PROVIDER_SUCCEEDED`로 먼저
   커밋한 뒤 구독/결제 원장을 최종화한다. 최종화 실패 시 재시도는
   Provider를 다시 호출하지 않는다.
3. **실패 원장 보존:** API가 `BusinessException`을 반환해도 주문의
   `FAILED` 또는 `PENDING_PROVIDER_CONFIRMATION` 상태는 별도 트랜잭션으로
   남는다.
4. **중복 최종화 차단:** `subscription_payments.payment_order_id`와
   `(provider, pg_transaction_id)`에 고유 제약을 둔다.
5. **갱신 주기 식별:** renewal identity는
   `(billingAgreementId, userSubscriptionId, billingPeriodStart)`이다. 단순히
   최근 non-DONE 주문을 재사용하지 않는다.
6. **계약별 격리:** scheduler는 트랜잭션을 소유하지 않고 계약 ID를
   keyset 방식으로 순회한다. 한 계약의 실패가 이전 계약의 로컬 결과를
   롤백할 수 없다.
7. **환불 직렬화:** 환불 생성 시 원 `SubscriptionPayment`를
   `PESSIMISTIC_WRITE`로 잠근 뒤 예약 합계와 새 요청을 한 트랜잭션에서
   처리한다. preview는 참고값일 뿐 승인 근거가 아니다.
8. **ENUM 정렬:** MySQL 감사 action에 정산 3개 값, target에
   `PAYMENT_SETTLEMENT`을 추가한다.

## 구현 영향

새 `PaymentCommandTransactionService`, `PaymentCommandKeyFactory`,
`PaymentRefundTransactionService`가 짧은 트랜잭션 경계를 소유한다.
기존 `BillingAgreementApplicationService`, `UserSubscriptionService`,
`RecurringRenewalService`, `SubscriptionScheduler`,
`AdminPaymentRefundService`는 외부 호출 오케스트레이션과 로컬 저장을
분리해야 한다.

DB에는 payment order 명령 키, 갱신 시작일, 시도 번호, Provider 멱등 키,
처리 시작 시각과 새 상태 3개를 추가한다. 수동 패치 파일은
`src/main/resources/db/manual/20260714_payment_db_integrity.sql`로 고정했다.

## 위험과 불확실성

- 저장소에는 2026-06-15 패치가 전제로 삼는 과거 payment baseline이 없다.
  따라서 retained DB에 적용 가능한 완전한 순서는 현재 증명되지 않았다.
- 실제 DB의 중복 payment order/provider transaction/renewal period 여부를
  확인하지 않았다. 중복 발견 시 자동 삭제·덮어쓰기를 금지하고 적용을
  중단하도록 설계했다.
- billing-key issue에는 현재 Provider 조회 API가 없다. stale confirm은
  자동 재호출하지 않고 재인증과 Incident 처리가 필요하다.
- H2는 ENUM과 InnoDB 잠금의 증거가 아니다. 별도 승인된 disposable MySQL
  8 환경에서 검증해야 한다.

## 승인 포인트

| ID | 승인 필요 사항 | 권고안 |
|---|---|---|
| `PAYDB-AP-01` | 결제 명령 상태/컬럼/고유 제약 도입 | 본 설계안 승인 |
| `PAYDB-AP-02` | stale `PROCESSING` 기준 | 15분, blind replay 금지 |
| `PAYDB-AP-03` | 최초 결제 실패 후 billing key 처리 | 추적 가능한 cleanup 성공 전 암호화 상태로 보존 |
| `PAYDB-AP-04` | retained DB baseline 및 중복 행 처분 | copied DB inventory 후 행별 승인, 자동 정리 금지 |
| `PAYDB-AP-05` | disposable MySQL 생성/삭제 | 기존 JDBC를 쓰는 사용자 제공 MySQL 8, Testcontainers 미도입 |
| `PAYDB-AP-06` | 고액 환불 maker-checker 기준 | 운영 환불 전 별도 정책 승인; P1 잠금 구현과 분리 |

실제 DB DDL 적용, disposable DB 생성/삭제, 새 라이브러리 도입, live Toss
호출은 이 WI에서 수행하지 않았다.

## WI 체인

이 설계 완료는 `WI-20260714-ATS-004`~`008`, `015`, `018`, `021`, `023`,
`025`의 시작 조건이다. 구현 WI는 위 승인 포인트를 먼저 확정하고,
`WI-021`은 retained DB baseline/중복 여부를 실제 copied DB에서 판정해야
한다.
