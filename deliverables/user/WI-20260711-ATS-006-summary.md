# WI-20260711-ATS-006 결제 3-way 통합 검증 요약

## 결론

- 최종 판정: **FAIL**
- 중복 제거 결과: **P0 1건, P1 8건, P2 5건**입니다. P2 중 1건은 현재 배포 환경 확인이 필요한 조건부 위험입니다.
- Phase 1 결론을 그대로 옮기지 않고 설계, 현재 Java/React 코드, `schema.sql`과 수동 패치, 운영/클라이언트 문서를 다시 대조했습니다.
- Phase 1에서 "billing prepare/confirm 계약이 일치한다"고 본 주장은 철회합니다. 현재 프론트엔드와 백엔드는 서로 맞지만 `docs/design/api-spec.md`의 응답 예시는 실제 DTO와 다릅니다.
- 새로 확인한 핵심 결함은 오래된 `FAILED` 갱신 주문이 새 구독 주기에도 재사용될 수 있다는 점과, 빌링키 발급/삭제의 provider-local 분리 실패를 복구할 원장이 없다는 점입니다.

## 최종 발견사항

| ID | 우선순위 | 최종 판정 | 영향과 핵심 근거 |
|---|---:|---|---|
| PAY-006-01 | P0 | 결함 | 회원 탈퇴는 `users.is_deleted`만 바꾸고 활성 구독과 빌링 약정을 중단하지 않습니다. 갱신 조회에도 탈퇴 사용자 제외 조건이 없어 탈퇴 후 자동 청구가 가능합니다. `UserService.java:104-122`, `BillingAgreementRepository.java:26-29`, `RecurringRenewalService.java:89-159` |
| PAY-006-02 | P1 | 결함 | Java는 정산 감사 action 3개와 `PAYMENT_SETTLEMENT` target을 저장하지만 `schema.sql`과 수동 패치의 MySQL ENUM에는 없습니다. 정산 import/reconcile/ignore가 감사 row flush에서 실패하거나 잘못 저장될 수 있습니다. `PaymentOperationAuditAction.java:17-19`, `PaymentOperationAuditTargetType.java:8`, `schema.sql:797-815` |
| PAY-006-03 | P1 | 결함 | 최초 청구 실패 시 주문 `FAILED`와 실패 횟수를 표시한 뒤 `BusinessException`을 던지지만 기본 트랜잭션이 모두 롤백합니다. 현재 Mockito 테스트는 영속 상태를 검증하지 못합니다. `BillingAgreementApplicationService.java:161-226`, `BusinessException.java:7`, `BillingAgreementApplicationServiceTest.java:310-349` |
| PAY-006-04 | P1 | 결함 | 최초 confirm, 업그레이드, 갱신에 row lock/version/최종 결제 unique가 없습니다. 특히 동시 업그레이드는 서로 다른 주문과 idempotency key를 만들어 실제 중복 청구로 이어질 수 있습니다. `UserSubscriptionService.java:118-200,339-373`, `PaymentOrderRepository.java:17-33`, `schema.sql:518-538` |
| PAY-006-05 | P1 | 결함 | 최신 갱신 주문이 `FAILED`이면 결제 주기가 달라도 무조건 재사용됩니다. 만료 후 새 구독을 시작해도 과거 주문의 grace 종료일로 새 구독을 즉시 만료시킬 수 있습니다. `RecurringRenewalService.java:48-52,167-197`, `BillingAgreementApplicationService.java:285-305`, `UserSubscription.java:113-125` |
| PAY-006-06 | P1 | 결함 | 전체 due 약정을 한 목록과 한 트랜잭션에서 provider 호출까지 처리합니다. 후반 항목의 예외나 commit 실패가 앞선 provider 성공의 로컬 기록을 롤백할 수 있습니다. `SubscriptionScheduler.java:32-36`, `RecurringRenewalService.java:84-105,147-159` |
| PAY-006-07 | P1 | 결함 | 환불 요청 생성은 잠금 없이 누적 예약액을 합산한 뒤 새 row를 만듭니다. 동시 요청이 원 결제액보다 큰 금액을 각각 예약할 수 있습니다. `AdminPaymentRefundService.java:89-111,248-262`, `PaymentRefundRepository.java:49-57` |
| PAY-006-08 | P1 | 결함 | ADMIN도 USER 이상으로 취급되어 구독 checkout에 접근하고 백엔드 prepare로 주문/약정을 만들 수 있습니다. 관리 화면에서 메뉴만 숨긴 것으로 서버 경계가 닫히지 않습니다. `ProtectedRoute.tsx:7-24`, `router/index.tsx:130,153-161`, `BillingAgreementApplicationService.java:108-137` |
| PAY-006-09 | P1 | 조건부 결함 | 기존 DB를 재현 가능하게 올릴 결제 마이그레이션 체인이 저장소에 없습니다. `schema.sql`은 수동 reference이고, 최신 패치도 이전 결제 패치를 전제로 하지만 그 파일이 없습니다. 헤더 v12/꼬리 38도 실제 v13/39와 다릅니다. `application.yml:16-20`, `20260615_align_payment_whitelist_schema.sql:18-23`, `schema.sql:2-15,1014-1017` |
| PAY-006-10 | P2 | 문서 계약 결함 | billing prepare 예시는 `BILLING_AGREEMENT`인데 금액 9,900원으로 적혀 있고 실제 필드를 누락합니다. confirm/my/cancel 응답 예시도 실제 DTO 및 프론트 타입과 구조가 다릅니다. `api-spec.md:1316-1423`, `BillingAgreementConfirmResponse.java:10-17`, `BillingAgreementResponse.java:10-18` |
| PAY-006-11 | P2 | 결함 | reconciliation은 최신 주문 100건만 확인하고 ACTIVE 약정 전체를 무제한 조회한 뒤 건별 구독 조회를 합니다. 관련 복합 인덱스도 부족해 오래된 불일치를 영구히 놓치거나 대량 스캔할 수 있습니다. `PaymentReconciliationService.java:56-178`, `schema.sql:485-538` |
| PAY-006-12 | P2 | 복구 결함 | 빌링키 발급 후 암호화/로컬 commit 실패, provider 키 삭제 후 로컬 commit 실패를 복구할 durable operation이나 provider 약정 조회가 없습니다. 현재 runbook은 결제 성공 후 로컬 실패만 다룹니다. `BillingAgreementApplicationService.java:174-226,255-281`, `RecurringPaymentProvider.java:5-15`, `payment-operations-runbook.md:130-163` |
| PAY-006-13 | P2 | 프론트 결함 | 관리자 결제 탭의 비동기 목록 요청에 취소나 latest-request 경계가 없어 빠른 탭/필터/페이지 전환 시 오래된 응답이 목록·페이지·loading 상태를 덮어쓸 수 있습니다. `PaymentReadOnlyPage.tsx:179-248` |
| PAY-006-14 | P2 | 외부 확인 필요 | 단일 scheduler 인스턴스, JVM 시간대, billing encryption secret의 존재와 rotation 절차가 코드에서 강제되지 않습니다. 현재 운영 환경이 문서의 단일 서버 전제를 실제로 만족하는지 확인해야 합니다. `AtStudioApplication.java:5-8`, `SubscriptionScheduler.java:32-60`, `BillingKeyCrypto.java:22-110` |

## 정합한 영역

- 순차 실행 기준 새 구독 성공, 빌링 수단 재등록, 다운그레이드·주기 변경 예약, 예약 해제 흐름은 설계와 구현이 맞습니다.
- 사용자 구독 취소는 다음 갱신만 중단하고 만료일까지 접근을 유지하며, 유효한 빌링키가 있으면 만료 전 재활성화할 수 있습니다.
- 환불과 권한 보정은 분리되어 있습니다. 환불 성공만으로 구독 권한이 자동 변경되지 않으며, 권한 보정은 별도 미리보기·요청·승인·실행 절차를 가집니다.
- 영수증 evidence는 결제 commit 뒤 별도 트랜잭션에서 최소 필드만 저장하고, 사용자/관리자 빌링 DTO는 raw billing key를 반환하지 않습니다.
- 웹훅, Toss Settlement API adapter, multi-PG, 현금영수증 mutation, 세금계산서, creator payout은 현재 범위에서 의도적으로 유예되어 있으며 결함으로 세지 않았습니다.
- legacy one-time subscription 경로는 현재 recurring-first 정책에 맞게 차단되어 있습니다.

## 정책 결정 필요

1. 회원 탈퇴 시 future charge 중단은 필수입니다. 그 뒤 provider billing key를 즉시 삭제할지, 분쟁·감사 보존을 위해 제한적으로 유지할지, 어떤 감사 row와 보존 기간을 둘지는 별도 정책 승인이 필요합니다.
2. 환불 요청자와 승인자/실행자를 분리할지, 금액 기준 2인 승인 문턱을 둘지는 아직 미정입니다. 현재 코드는 동일 ADMIN의 요청·승인·실행을 허용합니다.

## 필수 후속 검증

1. MySQL 8 Testcontainers에서 fresh schema와 기존 DB migration 경로를 각각 검증하고 모든 결제 감사 ENUM을 실제 flush합니다.
2. mock provider와 두 worker를 사용해 최초 confirm, 동시 업그레이드, 갱신, 환불 예약의 동시성 테스트를 추가합니다.
3. `withdraw -> due renewal` 통합 테스트에서 provider 호출 0회와 비갱신 상태를 검증합니다.
4. 오래된 `FAILED` 갱신 주문이 있는 사용자의 재구독 및 다음 갱신 테스트를 추가합니다.
5. 결제수단 발급/삭제 직후 로컬 commit 실패를 주입해 재시도·복구 가능성을 검증합니다.
6. ADMIN direct URL과 prepare API가 모두 403이며 주문/약정이 생성되지 않는지 검증합니다.
7. 관리자 결제 화면에 deferred promise 기반 latest-request-wins 테스트를 추가합니다.

## 실행 제한

- SQL, DB, provider, 환불, 관리자 mutation은 실행하지 않았습니다.
- 소스·기존 문서는 수정하지 않았고 이 WI의 두 산출물만 생성했습니다.
- Gradle/Vitest는 build/cache 산출물을 만드는 현재 read-only WI 제약 때문에 실행하지 않았습니다. 필요한 정확한 테스트와 안전 검증 절차는 Evidence Pack에 기록했습니다.

상세 근거: `deliverables/agent/WI-20260711-ATS-006-evidence-pack.md`
