# Evidence Pack: WI-20260711-ATS-005

## Summary (one-liner)

- Completed a read-only QA Integration audit of API, JPA/MySQL schema, manual migrations, provider adapters, schedulers, reconciliation, imports/exports, and deployment prerequisites; found 3 P1 and 7 P2 risks.

## Scope / DoD Check

- [x] Compared all 39 JPA entity tables with all 39 `schema.sql` tables and both tracked manual patches.
- [x] Checked high-risk billing agreement, settlement, whitelist, company-certification, and subscription API contracts across spec/controller/DTO/frontend.
- [x] Inspected scheduler, reconciliation, settlement import, whitelist export, refund, receipt, and certification-file recovery/idempotency behavior.
- [x] Identified constraints, indexes, migration, callback, environment, storage, and deployment prerequisites.
- [x] Classified each risk as local-only, production-only/conditional, or universal.
- [x] Used static/read-only commands only; no SQL, provider mutation, build, or test execution.

## Baseline

- Workspace: `C:\Users\jm991\Desktop\project\ATStudio`
- Branch: `dev/kyoung`
- HEAD: `27d22446e5d21324dadcfcb322dbe51704dfe914`
- Baseline was dirty and treated as user-owned: 22 status entries at the final pre-write snapshot (6 modified, 4 deleted, 12 untracked).
- This WI created only:
  - `deliverables/user/WI-20260711-ATS-005-summary.md`
  - `deliverables/agent/WI-20260711-ATS-005-evidence-pack.md`

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and traceability baseline |
| 0 | `docs/standards/development-standards.md` | Java/JPA/testing standards |
| 0 | `docs/standards/documentation-standards.md` | Evidence document structure |
| 0 | `docs/standards/glossary.md` | Canonical ATStudio terms |
| 1 | `docs/policies/security-policy.md` | Secret/config handling and environment baseline |
| 1 | `docs/policies/quality-gates.md` | Review and evidence requirements |
| 2 | `docs/design/api-spec.md` | REST contract source |
| 2 | `docs/design/db-schema.md` | DB design/version source |
| 2 | `docs/design/payment-integration-design.md` | Payment idempotency and recovery intent |
| 2 | `docs/design/payment-operations-runbook.md` | Production payment prerequisites |
| 2 | `docs/payment/` | Payment feature, operations, limits, and acceptance context |
| 2 | `docs/registry/` | Registry/workboard context |
| 3 | `deliverables/user/REQ-20260711-ATS-001.md` | Approved audit scope and quality gates |

Injection rules applied:

- Rule source: `.claude/config/context-injection-rules.json:226-231`
- Assignee: `qa-integ`
- Required tiers: `[0, 1]` (`.claude/config/context-injection-rules.json:48-63`)
- The handoff pointer `docs/guides/` does not exist in this checkout; relevant operational guidance was found under `docs/design/payment-operations-runbook.md` and `docs/payment/`.

### Evidence Path Resolution

Line references use unique basenames for readability. They resolve to these exact repository paths:

| Basenames used below | Repository directory |
|---|---|
| `AtStudioApplication.java` | `src/main/java/com/atstudio/atstudio/` |
| `PaymentController.java`, `AdminPaymentController.java`, `WhitelistChannelController.java`, `AdminWhitelistChannelController.java`, `CompanyCertificationController.java`, `SpaForwardController.java` | `src/main/java/com/atstudio/atstudio/controller/` |
| `SecurityConfig.java` | `src/main/java/com/atstudio/atstudio/config/` |
| `AdminPaymentSettlementService.java`, `AdminPaymentRefundService.java`, `AdminWhitelistChannelService.java`, `BillingAgreementApplicationService.java`, `CompanyCertificationService.java`, `PaymentReceiptEvidenceService.java`, `PaymentReconciliationService.java`, `RecurringRenewalService.java`, `SubscriptionScheduler.java` | `src/main/java/com/atstudio/atstudio/service/` |
| `BillingAgreementRepository.java`, `PaymentOrderRepository.java`, `PaymentRefundRepository.java`, `SubscriptionPaymentRepository.java`, `WhitelistChannelRepository.java` | `src/main/java/com/atstudio/atstudio/repository/` |
| `CompanyCertificationDocument.java`, `PaymentOperationAuditLog.java` | `src/main/java/com/atstudio/atstudio/entity/` |
| `PaymentOperationAuditAction.java`, `PaymentOperationAuditTargetType.java` | `src/main/java/com/atstudio/atstudio/entity/enums/` |
| `BillingKeyCrypto.java` | `src/main/java/com/atstudio/atstudio/service/payment/billing/` |
| `TossBillingProvider.java` | `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/` |
| `AdminPaymentSettlementImportResponse.java`, `CompanyCertificationResponse.java` | `src/main/java/com/atstudio/atstudio/dto/payment/` and `src/main/java/com/atstudio/atstudio/dto/certification/`, respectively |
| `BillingAgreementApplicationServiceTest.java`, `RecurringRenewalServiceTest.java`, `AdminPaymentRefundServiceTest.java`, `AdminPaymentSettlementServiceTest.java` | `src/test/java/com/atstudio/atstudio/service/` |
| `SubscriptionPaymentPage.tsx` | `frontend/src/pages/subscriber/` |
| `application.yml`, `schema.sql` | `src/main/resources/` |
| `20260615_align_payment_whitelist_schema.sql`, `20260618_company_certification_documents.sql` | `src/main/resources/db/manual/` |
| `api-spec.md`, `db-schema.md`, `payment-integration-design.md`, `payment-operations-runbook.md` | `docs/design/` |
| `known-limits-and-next-steps.md`, `system-overview.md` | `docs/payment/` |

## Inventory Map

| Surface | Inventory | Result / pointers |
|---|---:|---|
| REST controllers | 148 mapped methods | 147 REST APIs after excluding the single SPA forward (`SpaForwardController.java:9`); matches `docs/index.md:71` and `docs/design/index.md:28`. |
| High-risk controller methods | 56 | Payment 7, admin payment 24, whitelist 6, admin whitelist 3, certification 7, user subscription 9. |
| Frontend API modules | 20 non-test `.ts` files | `frontend/src/api/`; billing/settlement/whitelist/certification contracts checked below. |
| JPA tables | 39 | `@Entity`/`@Table` inventory under `src/main/java/com/atstudio/atstudio/entity/`. |
| Fresh-schema tables | 39 | 39 `CREATE TABLE` statements in `src/main/resources/schema.sql`; table-name delta against JPA = 0. |
| Manual DB patches | 2 | `20260615_align_payment_whitelist_schema.sql`, `20260618_company_certification_documents.sql`. |
| Migration framework | 0 | No Flyway/Liquibase match in `build.gradle`; upgrade flow is manual SQL. |
| Provider implementations | 3 | `MockPaymentProvider`, `TossPaymentProvider`, `TossBillingProvider`; the last implements recurring charge, lookup, and refund. |
| Scheduled jobs | 4 | 00:00 renewal, 00:10 stale-order expiry, 00:30 subscription expiry, 01:00 reconciliation. |
| Import/export | 3 primary paths | Settlement CSV import/reconcile, whitelist CSV export ledger, company-certification file storage. |

## High-Risk API Contract Matrix

| Flow | Spec | Backend | Frontend | Result |
|---|---|---|---|---|
| Billing prepare/confirm | `api-spec.md:1301`, `api-spec.md:1338` | `PaymentController.java:72-94`; request/response records under `dto/payment/` | `frontend/src/api/payments.ts:25-57`, `frontend/src/api/payments.ts:70-87` | URL, POST method, field names, enums, nullable dates/subscription, and `ResponseDTO.data` unwrap align. |
| Settlement import/reconcile | `api-spec.md:1603-1658` | `AdminPaymentController.java:124-140`; `AdminPaymentSettlementImportResponse.java:6-13` | `frontend/src/api/admin.ts:613-635` | Multipart part `file`, optional query `note`, date body, and result fields align. |
| Whitelist user/admin | `api-spec.md:2847-3103` | `WhitelistChannelController.java:26-95`; `AdminWhitelistChannelController.java:36-74` | `frontend/src/api/whitelistChannels.ts:6-68`; `frontend/src/api/admin.ts:126-191` | Paths, verbs, request fields, list envelope, blob handling, and status unions align. |
| Company certification | `api-spec.md:3097-3260` | `CompanyCertificationController.java:35-127`; `CompanyCertificationResponse.java:8-40` | `frontend/src/api/companyCerts.ts:6-39`; `frontend/src/api/admin.ts:64-121`; `frontend/src/types/index.ts:243-278` | Multipart `documents`, admin review, blob download, nullable fields, and response fields align. |
| Role boundary | API admin guards | `AdminPaymentController.java:66-107`, `AdminWhitelistChannelController.java:36-67`, `CompanyCertificationController.java:74-127` | `frontend/src/router/index.tsx:113-115`, `frontend/src/router/index.tsx:185-206` | Frontend admin route guard and backend `hasRole('ADMIN')` align for checked flows. |

No accidental request/response shape mismatch was confirmed in these high-risk flows. The on-demand `GET /api/admin/payments/reconciliation` endpoint exists at `AdminPaymentController.java:253-257` and in `api-spec.md:2032-2038`, but has no frontend API consumer; this is recorded as a follow-up coverage input rather than a contract defect because the persisted-incident UI is implemented.

## Findings

### INT-005-01 [P1] [Universal on MySQL] Settlement audit ENUMs are missing from executable schema and migrations

- Design expects settlement actions and target type: `docs/design/db-schema.md:52-58`, `docs/design/db-schema.md:685-686`.
- Java emits them: `PaymentOperationAuditAction.java:3-19`, `PaymentOperationAuditTargetType.java:3-8`, `AdminPaymentSettlementService.java:93-108`, `AdminPaymentSettlementService.java:213-220`, `AdminPaymentSettlementService.java:237-253`.
- JPA persists both as strings: `PaymentOperationAuditLog.java:44-50`.
- Executable DDL omits `PAYMENT_SETTLEMENT_IMPORTED`, `PAYMENT_SETTLEMENT_RECONCILED`, `PAYMENT_SETTLEMENT_IGNORED`, and `PAYMENT_SETTLEMENT`: `schema.sql:797-815`.
- The settlement patch creates `payment_settlements` but never alters the audit ENUMs: `20260615_align_payment_whitelist_schema.sql:239-285`; static search returned no audit/settlement-action match in either manual patch.
- Operational consequence: under strict MySQL mode, import/reconcile/ignore can fail when the same transaction flushes the audit row, rolling back the settlement operation; non-strict handling can persist an invalid/empty ENUM and corrupt audit evidence.
- Disposition: immediate fix. Update fresh DDL and add an ordered existing-DB patch for both columns; verify on MySQL, not H2.

### INT-005-02 [P1] [Production/concurrent execution] Payment idempotency is not serialized in the local ledger

- Contract requires idempotency by `orderId` and renewal period: `payment-integration-design.md:536-545`.
- Initial confirmation performs a state read, provider key issue, provider charge, local payment save, and final state update without an order/agreement row lock or optimistic version: `BillingAgreementApplicationService.java:161-175`, `BillingAgreementApplicationService.java:212-243`; `PaymentOrderRepository.java:17-33`; `BillingAgreementRepository.java:16-35`.
- Renewal selects due agreements without a lock/claim and then calls the provider before saving the local payment: `RecurringRenewalService.java:89-105`, `RecurringRenewalService.java:135-160`, `RecurringRenewalService.java:199-228`.
- `subscription_payments.payment_order_id` is nullable and non-unique: `schema.sql:518-538`.
- Provider-side mitigation exists: recurring charge sends a stable `Idempotency-Key`: `TossBillingProvider.java:257-274`. The frontend also suppresses duplicate effect execution within one mount: `SubscriptionPaymentPage.tsx:47-50`, `SubscriptionPaymentPage.tsx:84-90`.
- Operational consequence: concurrent callback retries or duplicate scheduler execution can race local order/agreement/subscription state and can create duplicate local payment evidence; provider idempotency reduces duplicate money movement but does not serialize billing-key issuance or local ledger writes.
- Disposition: architecture decision followed by immediate hardening. Add a DB claim/row lock or optimistic version, a renewal-period uniqueness key, and a local uniqueness invariant for finalized payment per order.

### INT-005-03 [P1] [Production-only, conditional on DB age] Existing-DB migration chain is not reproducible from the repository

- Runtime defaults to schema validation: `src/main/resources/application.yml:16-20`; fresh schema explicitly says it is manual/reference only: `schema.sql:9-15`.
- The current settlement/whitelist patch requires earlier payment migrations or a rebuild: `20260615_align_payment_whitelist_schema.sql:18-23`.
- `db-schema.md` repeats the prerequisite: `docs/design/db-schema.md:11-19`.
- Repository inventory contains only four SQL files (fresh schema, seed, and two manual patches). Manual patches create only `payment_settlements` among nine current billing/payment tables; no Flyway/Liquibase dependency was found.
- The v13 certification patch does exist and matches current schema/entity shape: `20260618_company_certification_documents.sql:5-22`, `schema.sql:162-178`, `CompanyCertificationDocument.java:8-42`.
- Operational consequence: an older retained DB cannot be upgraded deterministically from repository artifacts; deployment may fail Hibernate validation or invite ad hoc DDL/rebuild decisions with data-loss risk.
- Disposition: deployment blocker until actual DB baseline is identified. Restore the ordered migration chain or approve a migration framework and baseline procedure.

### INT-005-04 [P2] [Production-only] Scheduler topology, batch isolation, and timezone are deployment prerequisites rather than enforced controls

- Scheduling is globally enabled for every application instance: `AtStudioApplication.java:5-8`.
- Cron expressions have no explicit zone and renewal/expiry run in outer transactions: `SubscriptionScheduler.java:32-60`; reconciliation is also scheduled transactionally at `PaymentReconciliationService.java:48-53`.
- Documentation explicitly assumes one server: `docs/payment/system-overview.md:142-151`, `docs/payment/known-limits-and-next-steps.md:49-55`, `payment-operations-runbook.md:227-249`.
- Renewal loads all due agreements and processes them in one transaction: `RecurringRenewalService.java:89-105`. A decrypt failure throws unchecked: `BillingKeyCrypto.java:53-74`.
- Operational consequence: multiple replicas execute the same jobs; a JVM timezone mismatch shifts billing dates; one corrupt key/unexpected failure can abort the remaining batch, while prior external calls rely on provider idempotency/reconciliation for recovery.
- Disposition: enforce exactly one scheduler instance and JVM/business zone now; design distributed claim/lock plus per-agreement transaction isolation before horizontal scaling.

### INT-005-05 [P2] [Production volume] Reconciliation has a non-exhaustive 100-order window and an unbounded agreement scan

- Local and provider order checks read only page 0, size 100: `PaymentReconciliationService.java:56-73`, `PaymentReconciliationService.java:107-178`.
- Active agreements are loaded without paging and each triggers an active-subscription lookup: `PaymentReconciliationService.java:75-91`, `BillingAgreementRepository.java:34-35`.
- The API describes this as a recent-order diagnostic and examples report 100 checked orders: `api-spec.md:2032-2054`.
- Webhook is intentionally deferred, not counted as an accidental defect: `known-limits-and-next-steps.md:42-46`, `payment-operations-runbook.md:251-255`.
- Operational consequence: older unresolved mismatches can be displaced by newer orders and never become incidents, while agreement cost grows without a bound and exhibits N+1 behavior.
- Disposition: add cursor/time-window backfill and paged agreement processing; record scan watermark/completeness in the result.

### INT-005-06 [P2] [Universal at scale] High-value indexes do not match scheduler/reconciliation queries

- Query patterns: `PaymentOrderRepository.java:28-33`, `SubscriptionPaymentRepository.java:31-38`, `SubscriptionScheduler.java:41-47`, `AdminPaymentSettlementService.java:167-169`, `AdminPaymentSettlementService.java:259-267`.
- `payment_orders` has only the unique order key and `(user_id,status)` index; it lacks `(status,expires_at)` and a latest-created index: `schema.sql:485-513`.
- `subscription_payments` has no explicit index for `(payment_status,created_at)` or `pg_transaction_id`: `schema.sql:518-538`.
- Operational consequence: stale-order expiry, latest-order reconciliation, period scans, and up to 1,000 settlement-row fallback lookups can degrade into repeated scans/filesorts.
- Disposition: run `EXPLAIN` on a staging copy, then add the minimum composite/index set through a reviewed migration.

### INT-005-07 [P2] [Universal operations/traceability] `schema.sql` metadata is stale at v12 / 38 while executable content is v13 / 39

- Stale header/source: `schema.sql:2-4`; stale footer: `schema.sql:1014-1017`.
- Static count result: `CREATE_TABLE_COUNT=39`.
- Current design source is v13 and records 38 -> 39: `db-schema.md:1-4`, `db-schema.md:23-29`; complete list says 39: `db-schema.md:1061-1105`.
- Indexes also claim 39: `docs/design/index.md:26-30`, `docs/index.md:68-72`.
- Operational consequence: the 39th DDL exists, so this comment drift alone does not omit a table; however, operators can misclassify schema version and skip or repeat manual patches, amplifying INT-005-03.
- Disposition: immediate metadata-only correction to v13 / 39, with patch inventory cross-check.

### INT-005-08 [P2] [Universal/production volume] Whitelist export has no claim/lock and is unbounded

- Export loads every row for one status, creates a batch, accumulates rows/items in memory, marks pending channels exported, and saves all items: `AdminWhitelistChannelService.java:104-157`.
- Repository returns an unpaged list: `WhitelistChannelRepository.java:47-48`.
- Export ledger allows the same channel in multiple batches and has no claim uniqueness invariant: `schema.sql:257-294`.
- Operational consequence: concurrent admins can create overlapping batches for the same pending channels, and a large status set creates a long transaction/large CSV payload.
- Disposition: claim rows atomically into a batch, page/chunk export, and expose deterministic batch re-download/recovery.

### INT-005-09 [P2] [Production deployment] Billing-key secret validation and rotation recovery are incomplete

- Encryption uses a single `v1` envelope and derives the key only from the current configured secret: `BillingKeyCrypto.java:22-47`, `BillingKeyCrypto.java:91-110`.
- Missing/changed secret is detected lazily during encrypt/decrypt, not at startup: `BillingKeyCrypto.java:53-74`, `BillingKeyCrypto.java:105-110`.
- Renewal does not isolate decrypt exceptions per agreement: `RecurringRenewalService.java:99-105`, `RecurringRenewalService.java:147-156`.
- Runbook requires the secret but provides no old-key/key-ID/re-encryption rotation procedure: `payment-operations-runbook.md:227-249`.
- Operational consequence: a missing value or uncoordinated secret rotation can stop renewal processing for all remaining due agreements in the batch.
- Disposition: add startup validation, key ID/version support, dual-read rotation, and an operator rehearsal/rollback runbook.

### INT-005-10 [P2] [Local-only verification gap] Local tests mask MySQL schema and migration failures

- Test profile disables `schema.sql` and lets Hibernate create H2 schema: `src/test/resources/application.yml:1-7`.
- Local example defaults to `ddl-auto=update` and MOCK provider: `application-local.example.yml:13-15`, `application-local.example.yml:59-72`.
- Settlement tests mock repositories/audit service rather than flushing MySQL ENUMs: `AdminPaymentSettlementServiceTest.java:75-118`.
- Operational consequence: unit/build success can coexist with INT-005-01 and INT-005-03, producing a production-only first failure.
- Disposition: add disposable MySQL/Testcontainers schema-application and migration tests; keep live provider calls mocked.

## Positive Controls / Verified Recovery Features

- Settlement import uses deterministic SHA-256 keys and the DB has a unique deduplication key: `AdminPaymentSettlementService.java:311-366`, `AdminPaymentSettlementService.java:481-509`, `schema.sql:543-585`.
- Refund execution locks the refund row and reuses a persisted provider idempotency key: `PaymentRefundRepository.java:45-47`, `AdminPaymentRefundService.java:147-203`, `TossBillingProvider.java:291-304`.
- Receipt evidence is requested in the payment transaction and persisted after commit in a new transaction; failures are logged without rolling back payment: `PaymentReceiptEvidenceService.java:42-67`.
- Certification file writes register rollback deletion for new files and post-commit deletion for replaced files: `CompanyCertificationService.java:266-305`.
- Company-document static resources and API downloads are admin-guarded: `SecurityConfig.java:80`, `SecurityConfig.java:111-117`.

## Environment Classification

| Class | Findings |
|---|---|
| Local-only | INT-005-10: H2/MOCK/local `ddl-auto=update` mask MySQL/provider deployment defects. |
| Production-only / conditional | INT-005-02 concurrent execution, INT-005-03 retained older DB, INT-005-04 scheduler replicas/timezone, INT-005-09 secret rotation. |
| Universal code/schema | INT-005-01, INT-005-05, INT-005-06, INT-005-07, INT-005-08; impact increases on MySQL and with production data volume. |

## Commands & Outputs

Executed read-only/static commands only:

- `git status --short`, `git branch --show-current`, `git rev-parse HEAD`
  - Result: branch/HEAD above; pre-existing dirty files remained untouched.
- `rg --files src/main/resources/db src/main/java frontend/src/api`
  - Result: 2 manual patches, 39 entities, 20 frontend API modules.
- PowerShell `Select-String '^CREATE TABLE' src/main/resources/schema.sql`
  - Result: 39 statements; footer says 38.
- Static table-name comparator (`@Entity/@Table` vs `CREATE TABLE`)
  - Result: `SCHEMA_COUNT=39`, `ENTITY_COUNT=39`, missing on either side = none.
- Static audit-enum comparator
  - Result: missing action values = `PAYMENT_SETTLEMENT_IMPORTED`, `PAYMENT_SETTLEMENT_RECONCILED`, `PAYMENT_SETTLEMENT_IGNORED`; missing target = `PAYMENT_SETTLEMENT`.
- SQL inventory and payment-table patch coverage comparator
  - Result: manual patches create `payment_settlements` but not the other eight current billing/payment tables; no migration framework match.
- Controller mapping count
  - Result: 148 mapped methods, 147 REST APIs after excluding SPA forward.
- Targeted `rg -n`/numbered `Get-Content` for contracts, queries, indexes, schedulers, provider headers, and tests.
  - Result: evidence pointers recorded above.

No secret values were captured in either deliverable.

## Tests

- Not run by design. The handoff requires static inspection now, and this WI is read-only outside its two outputs. Gradle/npm tests would write build/cache artifacts; SQL and provider calls were forbidden.
- Existing relevant tests inspected statically:
  - `BillingAgreementApplicationServiceTest.java:197-250` verifies sequential success and provider idempotency key, not concurrency.
  - `RecurringRenewalServiceTest.java:167-188` verifies a later run skips an already-DONE order, not simultaneous workers.
  - `AdminPaymentSettlementServiceTest.java:75-118` verifies service collaboration with mocks, not MySQL ENUM flush.
  - `AdminPaymentRefundServiceTest.java:139-204` covers persisted idempotency/pending provider confirmation.

Recommended later verification in a separately approved write-capable test WI:

1. `gradlew.bat test --tests "com.atstudio.atstudio.service.BillingAgreementApplicationServiceTest" --tests "com.atstudio.atstudio.service.RecurringRenewalServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentSettlementServiceTest"`
2. Add a MySQL Testcontainers test that applies `schema.sql`, writes every `PaymentOperationAuditAction`/`PaymentOperationAuditTargetType`, and exercises import/reconcile/ignore transaction commit.
3. Add two-worker concurrency tests for initial confirm, recurring renewal, and whitelist export; assert one provider attempt key, one finalized payment per order/period, and one export claim per channel.
4. On a copied staging DB only, run schema validation and `EXPLAIN` for expiry, latest-order, payment-period, provider-key, and whitelist-export queries.
5. Rehearse billing-secret rotation with old/new key IDs and rollback; never use live billing keys in tests.

## Risks / Rollback

- Audit limitation: no live DB/provider/environment access; actual production SQL mode, DB baseline, replica count, JVM zone, secret presence, and data volume remain unverified.
- No SQL or provider mutation occurred.
- Rollback: only remove this WI's two newly created files, and only if explicitly requested.

## Follow-ups

- `se` + `re`: fix INT-005-01 and add MySQL integration coverage.
- `sa` + `se` + `re`: define locking/uniqueness/claim model for confirm, renewal, and whitelist export.
- `sa` + `docops`: restore ordered DB migrations or approve Flyway/Liquibase baseline/migration policy.
- `qa`/operations: verify scheduler replica count, JVM zone, billing-secret lifecycle, and copied-DB schema baseline before release.
- `se`: add cursor/paging/backfill and query indexes after staging `EXPLAIN` evidence.
- `docops`: correct `schema.sql` v13/39 metadata and payment patch references.
- Inputs for blocked next phase: WI-006/007/008 should consume INT-005-01 through INT-005-10 and the verified contract matrix.
