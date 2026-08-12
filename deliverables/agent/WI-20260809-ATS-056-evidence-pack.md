---
version: 1.0
last_updated: 2026-08-12
project: ATS
owner: docops
category: evidence
status: completed
dependencies:
  - path: WI-20260809-ATS-056-handoff.md
    reason: Approved work item contract
  - path: WI-20260809-ATS-056-schema-api-decision.md
    reason: Approved schema and API revision
  - path: WI-20260809-ATS-056-qa-integ-review.md
    reason: Final integration review
  - path: WI-20260809-ATS-056-pg-review.md
    reason: Final privacy and security review
---

# Evidence Pack: WI-20260809-ATS-056

## Summary

- Closed `CR-031-117` / `F-INTEG-029-B07` and `CR-031-119` /
  `F-INTEG-029-B09` at the repository/H2 boundary with a durable CSV-import
  attempt ledger, exact duplicate classification, conserved counters,
  orderless reconciliation evidence, manual header-only recovery, and no
  payment or external mutation.

## Scope / DoD Check

- [x] Concurrent same-row imports leave one Settlement and one row audit with
  complementary imported/duplicate outcomes.
- [x] Each claimed non-empty CSV import has one actor-attributed durable
  attempt, including all-duplicate and failed attempts. Header recovery is
  owner-scoped; ADMIN audit list and numeric detail are global.
- [x] Same-key POST never parses or processes the file again.
- [x] Import and reconciliation conserve total counts; status counts describe
  imported Settlement rows only.
- [x] Orderless finalized payments are classified once as failed with bounded
  response evidence.
- [x] The SPA supports one POST, one immediate read-only recovery after
  uncertain transport, and explicit manual recovery without polling/resubmit.
- [x] Import/recovery/reconciliation do not mutate payment, refund,
  subscription, billing-agreement, receipt, Provider, or mail state.
- [x] QA-INTEG and PG reviews both conclude `APPROVE`.
- [x] WI-067 parser/date/range/ceiling hardening remains held and out of scope.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and traceability rules |
| 0 | `docs/standards/documentation-standards.md` | Documentation contract |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/security-policy.md` | Secret, header, and free-text boundary |
| 1 | `docs/policies/quality-gates.md` | Closeout quality evidence |
| 2 | `docs/design/api-spec.md` | HTTP contract |
| 2 | `docs/design/db-schema.md` | Source schema contract |
| 2 | `docs/design/payment-settlement-import-design.md` | Settlement import design |
| 2 | `docs/design/payment-operations-runbook.md` | Operator workflow |
| 2 | `docs/payment/admin-operations-guide.md` | Admin-facing operations |
| 2 | `docs/ui/screen-flow.md` | Manual recovery state |

## Implementation Evidence

| Lane | Pointer and proven behavior |
|---|---|
| HTTP | `AdminPaymentController.java:126-169`: import requires the header; global ADMIN audit list, global numeric detail, and owner-scoped header recovery are separate GETs. List/detail expose the recorded actor. Reconciliation has no attempt key. |
| Attempt transaction | `AdminPaymentSettlementAttemptTransactionService.java:32-112`: claim, complete, and fail use independent durable transactions. List and numeric detail are global ADMIN audit reads; header recovery includes the current ADMIN ID in the digest lookup. |
| Import orchestration | `AdminPaymentSettlementService.java:74-172,556-604`: claim precedes parsing; replay conflicts by state; counts complete/fail the attempt. |
| Row transaction | `AdminPaymentSettlementRowTransactionService.java:41-121`: Settlement and row audit share one `REQUIRES_NEW` unit; a unique loser is handled after rollback and exact-winner lookup. |
| Constraint classification | `PaymentSettlementConstraintTranslator.java:10-103`: exact Hibernate/MySQL/H2 signatures are accepted; unrelated integrity errors fail closed. |
| Digest | `PaymentCommandKeyFactory.java:27-44`: namespace + ADMIN ID + canonical lowercase UUIDv4 produce an opaque 64-hex SHA-256 digest. |
| Schema | `PaymentSettlementImportAttempt.java:25-108`; `schema.sql:557-624`: 13-column ledger, unique digest, owner FK, count check, and Settlement import-batch index. |
| Reconciliation | `AdminPaymentSettlementService.java:197-296`: every selected finalized payment enters exactly one counter; orderless rows become bounded failures. |
| Frontend | `frontend/src/api/admin.ts:723-770`; `PaymentOperationsPage.tsx:710-830,1707-1832`: header transport, pending session state, no auth replay/polling/resubmit, manual recovery, retained correction context, and note warning. |
| No external effects | `AdminPaymentSettlementImportIntegrationTest.java:293-340`; `AdminPaymentSettlementServiceTest.java:300-367`: zero mutation/invocation across protected repositories, Provider, and mail. |

## Before / After

| Concern | Before | Current WI-056 contract |
|---|---|---|
| Concurrent duplicate | Existence check followed by insert could surface an unclassified unique failure. | Exact constraint translation after isolated row rollback returns deterministic imported/duplicate outcomes. |
| Response loss/all duplicate | Batch identity could be response-only and no new Settlement might remain. | A durable attempt records owner, digest, state, counts, note/failure code, and timestamps. |
| Same-key replay | No operation identity protected a retry from reprocessing. | Claim occurs before parsing; existing states return conflict and are recovered read-only. |
| Reconciliation orderless row | Included in total but could escape imported/duplicate/failed. | Counted once as failed with bounded error evidence. |
| UI uncertainty | Transport failure had no durable manual recovery contract. | One POST and one immediate recovery GET; unresolved attempts retain context and expose a manual recovery command. |

## Count and Race Evidence

```text
totalRows = importedRows + skippedDuplicateRows + failedRows
sum(statusCounts.values) = importedRows

COMPLETED attempt:
total_rows = imported_rows + duplicate_rows + failed_rows
```

- `AdminPaymentSettlementImportIntegrationTest.java:86-133`: same-row race,
  imported `1/0`, duplicate `0/1`, one Settlement, one row audit, two conserved
  completed attempts.
- `AdminPaymentSettlementImportIntegrationTest.java:136-167`: second-key
  all-duplicate result `0/1/0`, no second Settlement/audit, durable attempt.
- `AdminPaymentSettlementImportIntegrationTest.java:169-205`: same-key replay
  does not reprocess and list/detail/recovery agree.
- `AdminPaymentSettlementImportIntegrationTest.java:342-427`: multi-row race
  preserves unrelated valid rows and conserves both attempts.
- `AdminPaymentSettlementServiceTest.java:553-594`: reconciliation result
  imported `1`, duplicate `1`, failed `1`; imported-only status count `1`.

## Documentation Updated

- `docs/design/api-spec.md`: current endpoints, header-only recovery, response
  fields, counts, constraints, no-mutation boundary, and WI-067 hold.
- `docs/design/db-schema.md`: attempt table/source count and explicit current
  MySQL verification gap without an invented manifest or hash.
- `docs/design/payment-settlement-import-design.md`: implemented end-to-end
  contract and boundaries.
- `docs/design/payment-operations-runbook.md` and
  `docs/payment/admin-operations-guide.md`: operator flow and recovery rules.
- `docs/design/payment-refund-receipt-settlement-policy.md` and
  `docs/payment/system-overview.md`: settlement policy/system inventory.
- `docs/ui/screen-flow.md`: manual recovery and retained-state behavior.
- `docs/policies/security-policy.md`: canonical raw-key/logging and operator-note
  responsibilities.
- `docs/design/index.md`, `docs/index.md`,
  `docs/registry/project-registry.md`, `docs/client/_internal-feature-map.md`,
  and `docs/payment/feature-inventory.md`: calculated current source inventory.

## Calculated Inventory

- Controller source: 25 controller classes and 149 method mappings: 74 GET,
  41 POST, 20 PUT, 14 DELETE, 0 PATCH. `AdminPaymentController` has 27 mappings.
- Schema source: 42 `CREATE TABLE` statements and 42 JPA entities.
- No current MySQL manifest/hash was generated. The existing 41-table,
  493-column, 168-index-row, 89-foreign-key manifest and its hash predate
  WI-056 and are not represented as current proof.

## Recorded Quality Evidence

The code suites were not rerun during this documentation-only closeout. These
approved final results were supplied to DocOps:

| Gate | Recorded result |
|---|---|
| Backend | 1,503 tests, 0 failures, 16 skipped |
| JaCoCo | line 86.841%, method 84.29%, branch 71.432% |
| Frontend | 73 files, 815 tests, 0 failures |
| Frontend coverage | statements 88.22%, branches 79.43%, functions 87.88%, lines 90.43% |
| Static/build | Typecheck, ESLint, Prettier, backend build, and frontend build passed |
| H2 | Focused transaction/concurrency and persistence proof passed |
| MySQL | Rehearsal not performed; no current manifest/hash |

## DocOps Verification

- `python .agents\skills\validate-docs\scripts\validate_docs.py` -> exit 0;
  Tier 0, internal links, 557 traceability IDs, and document index passed.
- `git diff --check` -> exit 0; no whitespace error. Git emitted existing
  CRLF-to-LF normalization warnings for several working-tree files.
- Full backend/frontend code tests were deliberately not rerun in this
  documentation-only closeout, as required by the work instruction.

## Reviews

- `WI-20260809-ATS-056-qa-integ-review.md`: **APPROVE**; initial
  transaction, durable recovery, counter, and UI concerns resolved.
- `WI-20260809-ATS-056-pg-review.md`: **APPROVE**; initial raw-key,
  recovery-owner isolation, note, and retained-data concerns resolved, subject
  to operational controls. Global ADMIN list/detail remain actor-attributed
  audit views.

## Risks / Rollback

- H2 does not prove MySQL InnoDB lock timing, deadlocks, isolation, or real
  Connector/J exception text. MySQL signatures are synthetic-test evidence.
- Infrastructure access-log/proxy/tracing/APM header suppression and query
  redaction were specified but not inspected.
- `DisposableMysqlBootstrap` still carries predecessor schema expectations and
  must be updated in an authorized code/tooling WI before a current rehearsal.
- Browser `sessionStorage` carries the pending raw key; existing XSS and shared
  workstation controls remain relevant.
- Roll back this DocOps work by reverting only the listed documentation and
  WI-056 closeout files. Schema/data rollback is outside this WI and requires
  separate approval.

## Follow-up

- `WI-20260809-ATS-067`: held CSV parser, encoding/dialect/header/row-width,
  financial/provider bounds, date/range/ceiling, batching, and retry policy.
- Run a separately approved disposable MySQL rehearsal after updating its
  expected manifest; record a new real manifest/hash instead of reusing the
  predecessor evidence.
