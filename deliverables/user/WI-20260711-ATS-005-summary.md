# WI-20260711-ATS-005 통합 QA 요약

## 결론

- 정적·읽기 전용 통합 감사 결과: **P0 0건, P1 3건, P2 7건**.
- `schema.sql`의 39개 테이블과 JPA 엔티티 39개는 테이블 단위로 모두 대응한다. 그러나 결제 정산 감사 `ENUM`, 기존 DB 마이그레이션, 동시 실행, 스케줄러 배포 전제에서 운영 차단 가능성이 확인됐다.
- 결제·정산·화이트리스트·기업 인증의 대표 고위험 API는 Controller/DTO/React API의 URL, 메서드, 요청 필드, 응답 envelope가 대체로 일치했다.
- SQL, DB 변경, 실제 Provider 호출, 빌드·테스트는 수행하지 않았다. 기존 사용자 파일도 변경하지 않았다.

## 핵심 발견사항

| ID | 심각도 | 적용 범위 | 발견 및 운영 영향 | 핵심 근거 |
|---|---|---|---|---|
| INT-005-01 | P1 | 공통(MySQL) | Java/DB 설계에는 정산 감사 액션 3개와 `PAYMENT_SETTLEMENT` 대상이 있지만 `schema.sql`의 두 `ENUM`에는 없다. 엄격 SQL 모드에서는 정산 import/reconcile/ignore 트랜잭션이 감사 행 저장 시 롤백될 수 있고, 비엄격 모드에서는 감사값이 훼손될 수 있다. | `PaymentOperationAuditAction.java:3-19`, `PaymentOperationAuditTargetType.java:3-8`, `AdminPaymentSettlementService.java:93-108`, `schema.sql:797-815`, `db-schema.md:52-58` |
| INT-005-02 | P1 | 운영 동시성 | 초기 결제 확인과 정기 갱신은 순차 재호출만 멱등 처리한다. 주문/약정 행 잠금·버전과 `subscription_payments.payment_order_id` 유일 제약이 없어 동시 콜백 또는 중복 스케줄 실행 시 로컬 결제 원장·구독 상태가 경쟁할 수 있다. Provider `Idempotency-Key`는 중복 청구를 줄이지만 로컬 원장 원자성까지 보장하지 않는다. | `BillingAgreementApplicationService.java:161-175`, `BillingAgreementApplicationService.java:212-243`, `RecurringRenewalService.java:89-105`, `RecurringRenewalService.java:147-160`, `schema.sql:518-538`, `TossBillingProvider.java:257-274` |
| INT-005-03 | P1 | 운영의 기존 DB | 기본 `ddl-auto=validate`인데 수동 패치는 2개뿐이며, 현재 패치가 요구하는 "earlier payment table migrations"는 저장소에 없다. 오래된 DB는 저장소만으로 재현 가능한 업그레이드 경로가 없어 시작 검증 실패 또는 임의 DDL 적용 위험이 있다. | `src/main/resources/application.yml:16-20`, `20260615_align_payment_whitelist_schema.sql:18-23`, `db-schema.md:11-19` |
| INT-005-04 | P2 | 운영 배포 | 모든 애플리케이션 인스턴스에서 스케줄링이 항상 켜지고 작업 락/선점이 없다. 문서는 단일 서버를 전제로 하며 cron zone도 명시되지 않는다. 다중 인스턴스 또는 JVM 시간대 오설정 시 중복 실행·기준일 오프셋이 발생한다. | `AtStudioApplication.java:5-8`, `SubscriptionScheduler.java:32-60`, `PaymentReconciliationService.java:48-53`, `system-overview.md:142-151` |
| INT-005-05 | P2 | 운영 규모 | 결제 재조정은 최신 주문 100건만 검사하지만 활성 약정은 전부 로드해 약정별 구독 조회를 수행한다. 거래량 증가 시 오래된 불일치가 검사 창 밖으로 밀리고, 약정 검사는 무제한/N+1 형태로 커진다. | `PaymentReconciliationService.java:56-114`, `BillingAgreementRepository.java:34-35`, `api-spec.md:2032-2054` |
| INT-005-06 | P2 | 공통(규모 증가 시) | 실제 조회 패턴에 필요한 `payment_orders(status, expires_at/created_at)` 및 `subscription_payments(payment_status, created_at/pg_transaction_id)` 인덱스가 없다. 스케줄 만료, 최신 100건 정렬, 정산 기간 조회·행별 fallback 조회가 테이블 스캔으로 악화될 수 있다. | `PaymentOrderRepository.java:28-33`, `SubscriptionPaymentRepository.java:31-38`, `schema.sql:485-538`, `AdminPaymentSettlementService.java:259-267` |
| INT-005-07 | P2 | 공통 운영 추적성 | 요청된 교차검증 결과 `schema.sql` 머리말은 v12, 꼬리말은 38개라고 적지만 실제 `CREATE TABLE`은 39개다. `db-schema.md`와 인덱스는 v13/39다. 실행 DDL 자체의 39번째 테이블은 존재하므로 단독 런타임 결함은 아니지만, 수동 배포자가 버전·패치 적용 여부를 오판할 수 있다. | `schema.sql:2-4`, `schema.sql:1014-1017`, `db-schema.md:1-4`, `db-schema.md:23-29`, `db-schema.md:1061-1105`, `docs/design/index.md:26-30` |
| INT-005-08 | P2 | 공통/운영 규모 | 화이트리스트 CSV export는 대상 상태의 모든 행을 한 번에 읽고 잠금·claim 없이 상태와 export ledger를 갱신한다. 동시 관리자 export는 같은 채널을 중복 batch에 담을 수 있고 대량 데이터는 메모리·트랜잭션을 키운다. | `AdminWhitelistChannelService.java:104-157`, `WhitelistChannelRepository.java:47-48`, `schema.sql:257-294` |
| INT-005-09 | P2 | 운영 배포 | billing-key 암호화 secret은 사용 시점에만 검사되고 단일 v1 키만 지원한다. 누락 또는 사전 재암호화 없는 secret 교체는 복호화 실패로 갱신 batch를 중단시킬 수 있다. | `BillingKeyCrypto.java:53-74`, `BillingKeyCrypto.java:91-110`, `RecurringRenewalService.java:99-105`, `RecurringRenewalService.java:147-156`, `payment-operations-runbook.md:227-249` |
| INT-005-10 | P2 | 로컬 검증 | 테스트 프로필은 `schema.sql`을 끄고 H2 `create-drop`을 사용하며 정산 서비스 테스트는 저장소를 mock 처리한다. 따라서 INT-005-01과 수동 마이그레이션 결함은 현재 로컬 단위 테스트가 검출하지 못한다. | `src/test/resources/application.yml:1-7`, `AdminPaymentSettlementServiceTest.java:75-118`, `application-local.example.yml:13-15` |

## 확인된 정합성

- **DB 테이블:** `schema.sql` 39개 `CREATE TABLE` ↔ `@Entity` 39개, 누락 0개.
- **v13 기업 인증 패치:** `20260618_company_certification_documents.sql:5-22`가 엔티티 및 `schema.sql:162-178`과 일치한다.
- **REST 수:** Controller mapping 148개 중 SPA forward 1개를 제외한 REST API 147개로 `docs/index.md:71` 및 `docs/design/index.md:28`과 일치한다.
- **대표 계약:** billing agreement, settlement CSV/reconcile, whitelist, company certification 경로의 Controller/DTO/React API 필드와 envelope가 일치한다.
- **역할 경계:** `/admin` 화면은 `ProtectedRoute minRole="ADMIN"`이고 관련 백엔드 API도 `hasRole('ADMIN')`이다 (`frontend/src/router/index.tsx:113-115`, `frontend/src/router/index.tsx:185-206`, `AdminPaymentController.java:66-107`).
- **기존 보호 장치:** 정산 row SHA-256 dedupe + DB unique, refund row pessimistic lock + persisted idempotency key, Provider `Idempotency-Key`, 기업 인증 파일 rollback cleanup은 확인됐다.

## 권장 처분

1. **즉시 수정:** INT-005-01의 두 `ENUM`을 `schema.sql`과 기존 DB용 버전 패치에 함께 반영하고 MySQL 통합 테스트를 추가한다.
2. **배포 전 필수:** INT-005-03의 전체 순서형 마이그레이션 체인을 복원하거나 Flyway/Liquibase 도입을 승인한다. 현재 DB 버전을 별도로 확인하기 전에는 운영 적용 가능 상태로 보지 않는다.
3. **정책 결정 후 수정:** 주문/약정 claim 또는 row lock, 갱신 period 유일키, `subscription_payments.payment_order_id` 유일성 정책을 설계한다.
4. **배포 게이트:** 현 구조에서는 scheduler 실행 인스턴스를 정확히 1개로 고정하고 JVM zone 및 billing secret 불변/rotation 절차를 명시한다.
5. **후순위 개선:** 재조정 cursor/backfill, 조회 인덱스, whitelist export claim/paging, MySQL Testcontainers 회귀 검증을 추가한다.
6. **문서 즉시 정정:** `schema.sql` 머리말을 v13, 꼬리말을 39로 맞추되 실제 DDL 변경과 별개 커밋으로 추적한다.

상세 근거와 재현 명령은 `deliverables/agent/WI-20260711-ATS-005-evidence-pack.md`에 있다.
