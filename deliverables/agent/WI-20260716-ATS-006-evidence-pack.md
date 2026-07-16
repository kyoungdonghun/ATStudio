# Evidence Pack: WI-20260716-ATS-006

## Summary (one-liner)

- Replaced unbounded local payment reconciliation with bounded keyset batches, added key-ID billing-key rotation compatibility and startup validation, aligned payment cron zones and reconciliation indexes, and preserved the environment-only database proof boundary.

## Scope / DoD Check

- [x] Every eligible local order and agreement is scanned through configurable ID-keyset batches without a fixed recent-100 query or an unbounded entity list.
- [x] More than 100 candidates, final and empty pages, and non-contiguous IDs are covered without duplicate cursor use.
- [x] Scheduled incidents are persisted per bounded batch while API issue details use an independent explicit cap and full counters.
- [x] Fresh schema, JPA indexes, and an additive manual patch align with both keyset queries.
- [x] The manual patch contains guarded preflight/inventory and reproducible MySQL 8 `EXPLAIN FORMAT=JSON` statements.
- [x] New ciphertext uses a v2 key-ID envelope; legacy v1 decrypt, retained-key rotation, and unknown/removed-key failure are covered.
- [x] `TOSS_BILLING` validates the legacy secret, active key ID, and complete decryption key ring at startup without secret output; MOCK behavior is preserved.
- [x] All five payment cron methods use a configurable `Asia/Seoul` default zone; no distributed lock was added under the single-server invariant.
- [x] Focused tests, related payment tests, schema contract tests, docs validation, and diff check pass.
- [x] No live Toss or retained/production MySQL call was made; `ATS020-X-01` remains environment-conditional.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and approved execution boundary |
| 0/1 | `docs/standards/development-standards.md` | Software engineering and verification standards |
| 1 | `docs/policies/security-policy.md` | Secret handling and fail-closed requirements |
| 1 | `docs/policies/quality-gates.md` | Test, evidence, rollback, and environment-proof gates |
| 2 | `docs/design/remaining-remediation-design-20260716.md` | P2-03, P2-04, P2-18, and X-01 decisions |
| 2 | `docs/design/payment-integration-design.md` | Billing key, reconciliation, and scheduler architecture |
| 2 | `docs/design/payment-operations-runbook.md` | Operator configuration and retained-DB procedure |
| 2 | `docs/design/p1-payment-db-integrity-design.md` | Fresh/existing schema and migration proof model |
| 2 | `docs/design/db-schema.md` | Canonical table and index contract |
| 2 | `docs/design/api-spec.md` | Admin reconciliation response contract |
| 2 | `docs/payment/system-overview.md` | Current payment subsystem behavior |
| 2 | `docs/payment/known-limits-and-next-steps.md` | Explicit environment-only limits |
| Context | `deliverables/user/REQ-20260716-ATS-002.md` | Approved scope and acceptance criteria |
| Context | `deliverables/user/WI-20260716-ATS-004-summary.md` | Predecessor remediation decisions |
| Context | `deliverables/user/WI-20260715-ATS-020-summary.md` | Source payment findings and X-01 boundary |

The handoff packet and every INPUT POINTER were read before implementation. Existing WI-005 security edits in shared files were preserved; client worktree/runtime-log paths were excluded.

## Evidence Pointers

- Bounded configuration and key-ring model: `src/main/java/com/atstudio/atstudio/config/PaymentProperties.java:18-72`; committed defaults: `src/main/resources/application.yml:109-136`.
- Order/agreement ID-keyset repository queries: `src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java:76-92`, `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java:101-113`.
- Per-batch local transactions and empty-page cursor contract: `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionService.java:43-88`, `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionService.java:298-330`.
- Full-scan orchestration, bounded detail aggregation, and scheduled per-batch incident persistence: `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java:31-103`, `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java:183-214`, `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java:69-81`.
- API full-count/truncation fields: `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentReconciliationResponse.java:15-62`.
- v2 key-ID envelope, v1 compatibility, key-ring validation, and secret-safe failures: `src/main/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCrypto.java:24-220`; recurring-provider startup guard: `src/main/java/com/atstudio/atstudio/config/AcceptanceStartupGuard.java:43-53`.
- Explicit cron zones: `src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java:32-60`, `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java:42-47`, `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupCoordinator.java:31-37`.
- JPA/fresh-schema indexes: `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java:32-44`, `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java:43-55`, `src/main/resources/schema.sql:492`, `src/main/resources/schema.sql:533`.
- Additive existing-DB patch and EXPLAIN reproduction: `src/main/resources/db/manual/20260716_payment_reconciliation_indexes.sql:1-106`.
- Reconciliation batch/cap tests: `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationServiceTest.java:66-139`, `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionServiceTest.java:48-97`; constructor compatibility: `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentServiceTest.java:324-369`.
- Crypto/startup/binding tests: `src/test/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCryptoTest.java:31-156`, `src/test/java/com/atstudio/atstudio/config/AcceptanceStartupGuardTest.java:209-245`.
- Cron/schema tests: `src/test/java/com/atstudio/atstudio/service/SubscriptionSchedulerTest.java:42-56`, `src/test/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupCoordinatorTest.java:42-50`, `src/test/java/com/atstudio/atstudio/entity/PaymentDatabaseIntegrityContractTest.java:349-395`.
- API and DB contracts: `docs/design/api-spec.md:21-29`, `docs/design/api-spec.md:2095-2135`, `docs/design/db-schema.md:1-11`, `docs/design/db-schema.md:638-644`.
- Design/runbook/current-state contracts: `docs/design/payment-integration-design.md:38-46`, `docs/design/payment-integration-design.md:302-308`, `docs/design/payment-integration-design.md:718-724`, `docs/design/payment-operations-runbook.md:38-50`, `docs/design/payment-operations-runbook.md:266-298`, `docs/design/p1-payment-db-integrity-design.md:378-422`, `docs/payment/system-overview.md:45-51`, `docs/payment/system-overview.md:149-166`, `docs/payment/known-limits-and-next-steps.md:37-44`.

## Commands & Outputs

- `.\gradlew.bat test --tests "*BillingKeyCryptoTest.environmentBindingAcceptsIndexedKeyRing"` -> PASS, proving Spring indexed environment binding for key-ring entries.
- `.\gradlew.bat test --tests "*PaymentReconciliation*" --tests "*BillingKeyCrypto*" --tests "*SubscriptionScheduler*" --tests "*WithdrawalBillingCleanupCoordinator*" --tests "*AcceptanceStartupGuard*" --tests "*PaymentDatabaseIntegrityContract*"` -> PASS, 69 tests, 0 failures/errors/skips.
- `.\gradlew.bat test --tests "*Payment*" --tests "*Billing*" --tests "*SubscriptionScheduler*" --tests "*RecurringRenewal*" --tests "*WithdrawalBillingCleanup*"` -> PASS, 214 tests, 0 failures/errors, 8 skips. The skipped classes are gated by `ATSTUDIO_MYSQL_PROOF_ENABLED`, so no external MySQL proof was attempted.
- `python .agents/skills/validate-docs/scripts/validate_docs.py` -> PASS: Tier 0, internal links, 399 traceability IDs, and document index.
- `git diff --check` -> exit 0; output contained line-ending conversion warnings only.
- Read-only repository inspection and tests did not call live Toss. No manual SQL was executed against an actual database.

## Risks / Rollback

- Risks: `ATS020-X-01` is open because the new index patch and query plans have only source/static contract evidence. Representative retained-database cardinality, DDL runtime/locking, chosen keys, rows estimates, and Hibernate validation remain unproven.
- Risks: Removing the legacy v1 secret or a retained v2 decryption key before ciphertext inventory proves it unused makes affected billing agreements undecryptable. Rotation execution requires a separate approved operator procedure.
- Risks: Scheduler coordination assumes one application server. A multi-server deployment requires a separately approved lock/leader design.
- Rollback: revert only the WI-006 code, tests, configuration additions, schema/manual patch, and payment documentation pointers listed above. Do not revert WI-005 security edits or client/runtime artifacts.
- Rollback: before deployment, omit the additive index patch and revert source/schema metadata together. After deployment, use an approved DBA change to remove only the two WI-006 indexes if necessary; do not delete ledger data.
- Rollback: retain every old decryption key and the legacy secret while any v1/v2 ciphertext depends on them, even if new v2 writes are temporarily disabled.

## Follow-ups

- WI-006 unblocks WI-011, WI-012, and WI-013 subject to their remaining dependency gates.
- A named environment owner must rehearse `20260716_payment_reconciliation_indexes.sql` on an approved retained-database copy and capture secret-free EXPLAIN/Hibernate evidence before closing `ATS020-X-01`.
