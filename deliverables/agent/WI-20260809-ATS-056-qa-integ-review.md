---
version: 1.0
last_updated: 2026-08-12
project: ATS
owner: QA-INTEG
category: audit
status: accepted
dependencies:
  - path: WI-20260809-ATS-056-handoff.md
    reason: Approved scope, acceptance criteria, and held boundaries
  - path: WI-20260809-ATS-056-schema-api-decision.md
    reason: Approved revised schema and API direction
  - path: WI-20260809-ATS-056-pg-review.md
    reason: Final privacy and security review
---

# QA-INTEG Review: WI-20260809-ATS-056

## Final Decision

**APPROVE**

The final source, test, frontend, and documentation contract closes
`CR-031-117` / `F-INTEG-029-B07` and `CR-031-119` /
`F-INTEG-029-B09` at the repository/H2 boundary. No open P0, P1, or P2
cross-layer finding remains in WI-056 scope.

This approval is not a MySQL or production approval. No current MySQL manifest,
hash, lock, deadlock, isolation, or retained-data rehearsal was run.

## Finding History

| Priority | Initial review concern | Final correction and evidence | Status |
|---|---|---|---|
| P1 | A pre-check plus insert could misclassify a concurrent unique collision or roll back unrelated rows. | Each row uses `REQUIRES_NEW`; translation names the exact deduplication constraint/signature and confirms the winner after rollback. H2 races produce one Settlement/one row audit and complementary outcomes; unrelated valid rows survive. | RESOLVED |
| P1 | A response-only batch key could not prove an all-duplicate or response-lost operation, and same-key POST could process twice. | The dedicated attempt ledger is claimed before parsing with an owner-scoped digest. Same-key POST returns state-specific conflict without parsing; ADMIN list, numeric detail, and header-only recovery expose durable aggregate evidence. | RESOLVED |
| P1 | Import/reconciliation counters could omit unusable rows or inflate status counts. | Completed attempt and response counts conserve totals. Status counts sum to imported rows only. Orderless finalized payments are counted once as failed with bounded error evidence. | RESOLVED |
| P2 | UI transport recovery could accidentally resubmit or discard correction context. | The SPA stores one pending key, disables auth replay, makes one read-only recovery GET after POST failure, exposes manual recovery, blocks new import while pending/corrupt, and never polls or sends a second POST. | RESOLVED |

## Cross-Layer Evidence

| Lane | Verified result |
|---|---|
| HTTP and authorization | Import requires `Idempotency-Key`; list, numeric detail, and recovery are ADMIN-only. Recovery takes the key in the header, with no path/query key (`AdminPaymentController.java:126-169`; `AdminPaymentControllerTest.java:99-184`). |
| Client API | Import and recovery send the same key only as `Idempotency-Key`; list and detail use the exact numeric paths (`frontend/src/api/admin.ts:723-770`; `frontend/src/api/adminContracts.test.ts:223-282`). |
| Durable attempt | `payment_settlement_import_attempts` stores actor, digest, state, four counts, bounded note/failure code, and timestamps. Entity and DDL enforce completed count conservation (`PaymentSettlementImportAttempt.java:25-108`; `schema.sql:557-583`). |
| Recovery owner scope | Digest input includes operation namespace, ADMIN ID, and canonical key. Concurrent ADMIN owners using the same raw key produce different attempts/digests and recover only their own outcome. ADMIN audit list and numeric detail are global and expose the recorded actor; they are not owner-scoped (`PaymentCommandKeyFactory.java:27-44`; `AdminPaymentSettlementImportIntegrationTest.java:209-290`). |
| Same-key no-reprocess | Attempt unique claim occurs before parsing. Completed replay returns `SETTLEMENT_IMPORT_ATTEMPT_COMPLETED`; one Settlement and one row audit remain, and recovery/list/numeric detail agree (`AdminPaymentSettlementService.java:74-104,556-580`; `AdminPaymentSettlementImportIntegrationTest.java:169-205`). |
| Row transaction | Settlement save/flush and its row audit share one `REQUIRES_NEW` transaction. A losing collision is handled after rollback and exact winner read (`AdminPaymentSettlementRowTransactionService.java:41-121`; `AdminPaymentSettlementService.java:104-159`). |
| Exact constraints | Attempt replay accepts exact Hibernate/MySQL/H2 signatures; row duplicate accepts the exact named/H2 signature and winner confirmation. Unrelated integrity violations fail closed (`PaymentSettlementConstraintTranslator.java:10-103`; `AdminPaymentSettlementServiceTest.java:185-297`). |
| Race counts | Same-row H2 race returns imported counts `1/0`, duplicate counts `0/1`, one Settlement, one row audit, and two conserved completed attempts. Multi-row race returns imported `2/1`, duplicate `0/1`, preserves both unrelated rows, and conserves both attempts (`AdminPaymentSettlementImportIntegrationTest.java:86-133,342-427`). |
| All duplicate | A second-key all-duplicate import returns `0/1/0`, keeps one Settlement/one row audit total, and adds a durable completed attempt with the note (`AdminPaymentSettlementImportIntegrationTest.java:136-167`). |
| Reconciliation | Three selected rows resolve to imported `1`, duplicate `1`, failed `1`; the orderless row supplies the bounded error and `PROVIDER_SETTLEMENT_NOT_FOUND` status count is `1` (`AdminPaymentSettlementServiceTest.java:553-594`). |
| UI state | Pending/corrupt browser records block new import; terminal recovery permits a fresh key; transport uncertainty uses one POST and same-key read-only recovery while retaining File/DOM/note (`PaymentOperationsPage.tsx:710-830`; `PaymentOperationsPage.test.tsx:682-891`). |
| External effects | Import/recovery write only attempt, Settlement, and row-audit evidence. Repository/provider/mail assertions record zero mutation/invocation for payment, refund, subscription, billing agreement, receipt, Provider, and mail lanes (`AdminPaymentSettlementImportIntegrationTest.java:293-340`; `AdminPaymentSettlementServiceTest.java:300-367`). |

## Count Invariants

For every normal import and reconciliation response:

```text
totalRows = importedRows + skippedDuplicateRows + failedRows
sum(statusCounts.values) = importedRows
```

For every durable completed CSV attempt:

```text
total_rows = imported_rows + duplicate_rows + failed_rows
```

The database check is a final backstop for completed attempt rows. Duplicate and
failed/unusable rows do not inflate Settlement status counts.

## Recorded Quality Gates

The following final results were supplied for closeout. DocOps did not rerun the
full code suites in this documentation-only pass.

| Gate | Recorded result |
|---|---|
| Backend | 1,503 tests; 0 failures; 16 skipped |
| JaCoCo | line 86.841%; method 84.29%; branch 71.432% |
| Frontend | 73 files; 815 tests; 0 failures |
| Frontend coverage | statements 88.22%; branches 79.43%; functions 87.88%; lines 90.43% |
| Static/build | Typecheck, ESLint, Prettier, backend build, and frontend build passed |

## Limits and Residual Risk

- H2 proves the exercised Spring transaction boundaries, durable rows/audits,
  and application-level race outcomes. It does not prove MySQL InnoDB timing,
  deadlock handling, isolation, or driver message behavior under a real race.
- MySQL exception signatures are unit-tested with synthetic driver exceptions;
  they were not produced by a WI-056 MySQL rehearsal.
- Current source is 42 tables/42 entities. The prior 41-table MySQL manifest and
  hash predate WI-056 and are not current. The disposable bootstrap validator's
  predecessor expectations remain a tooling follow-up before rehearsal.
- CSV parser hardening under `CR-031-115`, `CR-031-116`, and `CR-031-118`
  remains held and out of scope for WI-20260809-ATS-067.
- Browser-to-live-server acceptance, infrastructure logging configuration,
  retained-data migration, live Provider/data, and production deployment were
  not performed.

## Related Documents

- [WI-056 Handoff](WI-20260809-ATS-056-handoff.md)
- [WI-056 Schema/API Decision](WI-20260809-ATS-056-schema-api-decision.md)
- [WI-056 PG Review](WI-20260809-ATS-056-pg-review.md)
- [Settlement Import Design](../../docs/design/payment-settlement-import-design.md)
