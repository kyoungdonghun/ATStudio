---
version: 1.0
last_updated: 2026-08-13
project: ATS
owner: docops
category: evidence-pack
status: accepted
dependencies:
  - path: WI-20260809-ATS-067-handoff.md
    reason: Approved work item and output contract
  - path: WI-20260809-ATS-067-decision-register.md
    reason: Approved DG-067 policies and MySQL execution record
  - path: WI-20260809-ATS-067-qa-integ-review.md
    reason: Accepted integration review boundary
  - path: WI-20260809-ATS-067-pg-review.md
    reason: Accepted privacy and security review boundary
---

# Evidence Pack: WI-20260809-ATS-067

## Summary

- Completed strict settlement CSV and bounded reconciliation hardening, recorded
  and independently proved the current fresh-MySQL baseline, preserved WI-056
  idempotency and no-external-effect invariants, and passed final backend,
  frontend, documentation, and whitespace quality gates.

## Scope / DoD Check

- [x] DG-067-01 through DG-067-08 have explicit approved and implemented
  filename, MIME, byte, encoding, CSV, field, financial, date, row, response,
  execution, and operator-note policies.
- [x] DG-067-09A records only values emitted by the approved MySQL observation;
  the predecessor 41-table manifest is not an active expectation.
- [x] DG-067-09B is `RUN-PASS-CLEANED` with separate observation and proof
  targets, exact manifest evidence, isolated MySQL concurrency tests, and exact
  cleanup.
- [x] WI-056 same-key no-reprocess, owner-scoped recovery, exact duplicate
  classification, count conservation, bounded evidence, and no-external-effect
  invariants remain covered.
- [x] QA-INTEG v1.2 and PG v1.1 returned `ACCEPT` with no open P1/P2 finding at
  their reviewed repository/non-database boundary.
- [x] H2 import and IGNORE regressions, opt-in MySQL proof, default skip behavior,
  and final backend/frontend quality suites passed as recorded below.
- [x] Current English documents, this Evidence Pack, and the Korean user summary
  are complete.
- [x] No manual/client acceptance item is marked complete; SR-93 remains OPEN
  for retained-data, live Toss, deployment/operations, client acceptance, and
  explicit release approval.

## Reference Documents (Tier 0-2)

**Injected Context from the WI Handoff Packet:**

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and traceability |
| 0 | `docs/standards/development-standards.md` | Backend and test standards |
| 0 | `docs/standards/documentation-standards.md` | Deliverable and language contract |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/security-policy.md` | Secret, header, free-text, and proof boundaries |
| 1 | `docs/policies/access-control-policy.md` | ADMIN authorization boundary |
| 1 | `docs/policies/quality-gates.md` | Closeout quality evidence |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React implementation guidance |
| 2 | `docs/standards/frontend-standards.md` | Frontend contract standards |
| 2 | `docs/standards/dto-standards.md` | Response DTO rules |
| 2 | `docs/design/api-spec.md` | Current HTTP contract |
| 2 | `docs/design/db-schema.md` | Fresh-only schema and manifest contract |
| 2 | `docs/design/payment-settlement-import-design.md` | Settlement design |
| 2 | `docs/design/payment-operations-runbook.md` | Operator procedure |
| 2 | `docs/design/payment-refund-receipt-settlement-policy.md` | Settlement policy |
| 2 | `docs/payment/admin-operations-guide.md` | ADMIN workflow |
| 2 | `docs/ui/screen-flow.md` | UI flow and feedback |

**Injection Rules Applied:**

- Assignee role: `docops` for closeout; implementation and independent reviews
  were supplied by `se`, `qa-integ`, and `pg` under the handoff.
- Task type: documentation closeout with implementation, security, integration,
  and destructive-test evidence pointers.
- Evidence policy: bounded outputs and file/test pointers over raw logs; no
  credentials, raw CSV rows, Provider payloads, or exact payment identifiers.

## Approval and Decision Evidence

| Item | Recorded disposition |
|---|---|
| Parent requirement | `REQ-20260809-ATS-001` approved |
| Implementation decisions | DG-067-01 through DG-067-09A approved and implemented on 2026-08-13 |
| MySQL execution approval | Separate one-use approval for exact loopback targets `ats_disposable_20260813_wi067obs` and `ats_disposable_20260813_wi067prf` |
| MySQL scope | Observe/create, schema then seed apply, manifest validation, isolated concurrency proof, and exact Drop only |
| Residual state | Both exact disposable databases absent; approval exhausted |
| WI status | Completed; DG-067-09B `RUN-PASS-CLEANED` |

## Decision Matrix

| Gate | Implemented decision | Primary evidence |
|---|---|---|
| DG-067-01 | Case-insensitive `.csv`, filename at most 255 characters, approved/blank CSV MIME, nonempty file at most 5 MiB, pre-claim rejection | `AdminPaymentSettlementService.java`; controller/service envelope tests |
| DG-067-02 | Strict UTF-8 only; remove at most one leading BOM; reject malformed bytes | `PaymentSettlementCsvParser.java`; parser tests |
| DG-067-03 | Comma/double-quote/doubled-quote dialect, quoted LF/CRLF, normalized unique allowlisted headers, exact row width, 1,000 logical rows | `PaymentSettlementCsvParser.java:20-239`; 12 parser tests |
| DG-067-04 | Exact `TOSS`; untruncated bounded identifiers; reject controls, U+2028/U+2029, and edge whitespace; preserve case | `AdminPaymentSettlementService.java:459-581`; service tests |
| DG-067-05 | Plain nonnegative exact scale-2 amounts fitting `DECIMAL(15,2)`, exact `KRW`, strict dates, ordered payout date | `AdminPaymentSettlementService.java:473-581`; H2 durable-equality regression |
| DG-067-06 | 1,000 import rows; default 30 and maximum 90 inclusive reconcile days; 5,000 selected rows with a 5,001 probe | service/repository boundary tests |
| DG-067-07 | Synchronous execution, no cursor/progress/retry/polling, import errors bounded by row ceiling, reconciliation first 200 plus exact omitted count | response DTO, service, API, and UI tests |
| DG-067-08 | Optional trimmed note is multipart-only; no query fallback; existing 500-character behavior retained | `AdminPaymentController.java:126-170`; `frontend/src/api/admin.ts:724-774` |
| DG-067-09A | Active source count 42 and current observed manifest recorded; guard suite 20/20 and `RECORDED` preflight pass | `DisposableMysqlBootstrap.java:49-57,576-604`; guard script |
| DG-067-09B | Separate observation and proof databases; exact manifest, independent Validate, three MySQL concurrency cases, exact cleanup | bootstrap commands and `AdminPaymentSettlementMysqlConcurrencyIntegrationTest.java:54-260` |

## Implementation Evidence Pointers

| Lane | Pointer and behavior |
|---|---|
| HTTP and response | `AdminPaymentController.java:126-170`; `AdminPaymentSettlementImportResponse.java`: multipart-only note and additive `omittedErrorCount` contract |
| Parser and envelope | `PaymentSettlementCsvParser.java:20-239`; `AdminPaymentSettlementService.java:90-180`: strict bounded parsing and pre-claim envelope rejection |
| Field and durable identity | `AdminPaymentSettlementService.java:459-628`: raw bounds, evidence-character checks, exact amounts/dates, durable-equal deduplication basis |
| Row transaction | `AdminPaymentSettlementRowTransactionService.java:42-55`: isolated Settlement and audit persistence preserving exact duplicate handling |
| Reconciliation selection | `SubscriptionPaymentRepository.java`; `AdminPaymentSettlementService.java`: deterministic 5,001 probe and pre-mutation ceiling rejection |
| Frontend transport and feedback | `frontend/src/api/admin.ts:724-774`; `PaymentOperationsPage.tsx:879-903`: no query note, bounded result contract, warning for partial/failed/omitted outcomes |
| Bootstrap expectation | `scripts/database/DisposableMysqlBootstrap.java:49-57,152-160,576-604`: source count, recorded manifest, action guard, exact match including plan keys and forbidden objects |
| Bootstrap safety | `scripts/database/bootstrap-disposable-mysql.ps1`; `scripts/database/test-bootstrap-guards.ps1`: loopback/exact-name guards, credentials outside arguments, bounded output, exact cleanup |
| MySQL races | `AdminPaymentSettlementMysqlConcurrencyIntegrationTest.java:54-260`: MySQL 8/InnoDB, `REPEATABLE-READ`, `ddl-auto=validate`, and three isolated races |

## Request, Persistence, Audit, and UI Boundaries

| Boundary | Proven result |
|---|---|
| Request target | Operation key remains header-only. Optional import note is a multipart part and has no query compatibility fallback. |
| Import attempt | One explicit action claims one durable attempt after envelope validation. Same-key POST does not parse or process again. |
| Persistence | Accepted values are untruncated and aligned with the deduplication basis and durable shape. Invalid file/row evidence fails before its prohibited persistence/audit lane. |
| Count conservation | Normal import/reconciliation results preserve `totalRows = importedRows + skippedDuplicateRows + failedRows`; status counts describe imported rows only. |
| Error retention | Import row errors are response-only. Reconciliation returns at most 200 details and the exact `omittedErrorCount`. |
| UI | Partial, failed, or omitted outcomes use warning/error feedback; success is reserved for a clean aggregate after reload. |
| External effects | Import, recovery, reconciliation, and IGNORE cause no Provider, payment, refund, subscription, billing-agreement, receipt, or mail mutation/invocation. |

## MySQL Manifest and Execution Evidence

### Observation Pass

| Field | Observed value |
|---|---:|
| Exact database | `ats_disposable_20260813_wi067obs` |
| Schema / seed apply | PASS / PASS |
| Tables | 42 |
| Columns | 506 |
| Index rows | 173 |
| Foreign keys | 90 |
| Plans / plan keys | 6 / 6 |
| Forbidden tables / columns | 0 / 0 |
| SHA-256 | `acf28c935bf6107a8f2af431c971ebe0cd3539dba1aa1a941d966dde4a2a7a65` |
| Fail-closed result | Expected `MYSQL_MANIFEST_EXPECTATION_UNRECORDED` |
| Automatic cleanup | `cleanupAfterFailure=PASS` |
| Follow-up exact Drop | PASS |

### Recorded Expectation and Proof Pass

- The tooling recorded only the emitted table, column, index-row, foreign-key,
  plan, and hash values. Plan keys must equal the six plans; forbidden tables and
  columns must remain zero.
- `scripts/database/test-bootstrap-guards.ps1` -> 20/20 PASS.
- `Preflight` -> PASS with `mysql.manifest.expectation=RECORDED`.
- Exact proof database `ats_disposable_20260813_wi067prf` -> `Create` PASS,
  independent `Validate` PASS, exact manifest match.
- Hibernate `ddl-auto=validate` MySQL test -> 3 tests, 0 failures, 0 errors,
  0 skips:
  - Different operation keys with the same deduplication key.
  - Same owner and same operation key.
  - Concurrent `IGNORE`.
- Exact proof database `Drop` -> PASS.
- No existing database, real Provider, payment, refund, or mail path was
  accessed. No secret was printed.

## Reproduction Commands and Results

Credentials were supplied through the approved process environment or external
bundle, never command arguments. The one-use database names below must not be
reused; a future run requires new immediate approval and new exact names.

| Command | Result |
|---|---|
| `.\scripts\database\test-bootstrap-guards.ps1` | 20/20 PASS |
| `.\scripts\database\bootstrap-disposable-mysql.ps1 -Action Preflight -DatabaseName ats_disposable_20260813_wi067obs -HostName 127.0.0.1` | PASS; unrecorded before observation |
| Same entry point with `-Action Observe`, the observation name, and the external bundle | Emitted the exact manifest; expected fail-closed result; automatic cleanup PASS |
| Same entry point with `-Action Drop` and the observation name | PASS |
| Preflight after recording the observation | PASS; `mysql.manifest.expectation=RECORDED` |
| Same entry point with `-Action Create` then independent `-Action Validate` for `ats_disposable_20260813_wi067prf` | PASS; exact manifest match |
| `ATSTUDIO_SETTLEMENT_MYSQL_PROOF_ENABLED=true` plus proof-scoped datasource environment, then `.\gradlew.bat test --tests "com.atstudio.atstudio.service.AdminPaymentSettlementMysqlConcurrencyIntegrationTest" --rerun-tasks --console=plain` | 3 passed; 0 failure/error/skip |
| Same entry point with `-Action Drop` and the proof name | PASS |
| `.\gradlew.bat test --tests "com.atstudio.atstudio.service.AdminPaymentSettlementImportIntegrationTest" --tests "com.atstudio.atstudio.service.AdminPaymentSettlementIgnoreIntegrationTest" --rerun-tasks --console=plain` | H2 import 8 + IGNORE 2 passed |
| `.\gradlew.bat test --tests 'com.atstudio.atstudio.service.PaymentSettlementCsvParserTest' --tests 'com.atstudio.atstudio.service.AdminPaymentSettlementServiceTest' --tests 'com.atstudio.atstudio.controller.AdminPaymentControllerTest' --tests 'com.atstudio.atstudio.service.AdminPaymentSettlementImportIntegrationTest' --tests 'com.atstudio.atstudio.service.AdminPaymentSettlementIgnoreIntegrationTest' --rerun-tasks --console=plain` | BUILD SUCCESSFUL; 89 tests, 0 failures, 0 skipped |
| Default environment execution of `AdminPaymentSettlementMysqlConcurrencyIntegrationTest` | 3 skipped as designed because the opt-in variable was absent |

## Final Quality Evidence

| Gate | Command | Result |
|---|---|---|
| Backend full suite, coverage, and assemble | `.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification assemble --rerun-tasks --console=plain` | BUILD SUCCESSFUL; 1,542 tests, 0 failures, 19 skipped |
| Backend coverage | JaCoCo report and verification above | Instruction 86.72%, branch 71.877%, line 87.04%, method 84.56%, class 94.799% |
| Frontend tests and coverage | `npm run test:coverage` | 73 files, 827 tests, 0 failures; statements 88.28%, branches 79.53%, functions 87.88%, lines 90.5% |
| Frontend type safety | `npm run typecheck` | PASS |
| Frontend lint | `npm run lint` | PASS |
| Frontend format | `npm run format` | PASS |
| Frontend build | `npm run build` | PASS |
| Documentation | `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS; Tier 0, internal links, 557 traceability IDs, and document index |
| Whitespace | `git diff --check` | PASS; no whitespace error |

### Command Correction

- `npm run format:check` was attempted once based on an incorrect script-name
  assumption and failed because that script does not exist. The repository's
  actual format command, `npm run format`, was then run immediately and passed.
  This was a command correction, not a product, code, test, or formatting
  failure.

`git diff --check` emitted informational working-tree line-ending warnings for
three existing CRLF-managed documents; it returned exit 0 and reported no
whitespace error.

## Review Evidence

- `WI-20260809-ATS-067-qa-integ-review.md` v1.2: `ACCEPT`; all earlier P1/P2
  findings resolved. Its recorded DG-067-09B gap was subsequently closed by the
  separately approved MySQL proof above.
- `WI-20260809-ATS-067-pg-review.md` v1.1: `ACCEPT`; no open security/privacy
  P1/P2 finding. Its residual MySQL evidence gate was subsequently closed.
- Residual operational controls remain: endpoint multipart parsing occurs under
  broader shared request limits; infrastructure must suppress sensitive headers
  and unsolicited query capture; future spreadsheet exports must address
  formula-leading text.

## Documentation Evidence Pointers

- `scripts/database/README.md`: current manifest, completed two-pass proof,
  one-use approval exhaustion, and future reproduction boundary.
- `docs/design/db-schema.md`: current and historical manifests separated.
- `docs/design/api-spec.md`, `payment-settlement-import-design.md`,
  `payment-operations-runbook.md`, and
  `payment-refund-receipt-settlement-policy.md`: implementation and proof state.
- `docs/payment/acceptance-test-checklist.md`: automated technical evidence only
  is checked; manual/client rows remain unchecked.
- `docs/SR/SR-93.md`: WI-067 complete while overall production readiness remains
  OPEN.
- `deliverables/user/WI-20260809-ATS-067-summary.md`: Korean user closeout.

## Risks / Rollback

### Residual Risks

- The MySQL proof covers a fresh disposable schema and three targeted
  concurrency cases. It does not prove retained-data migration, every deadlock
  or infrastructure failure mode, production topology, or backup/restore.
- No live Toss, payment, refund, mail, deployment, or client acceptance action
  occurred. SR-93 remains OPEN for those production gates.
- Application-level omission does not prove reverse-proxy, access-log, tracing,
  or APM suppression of `Idempotency-Key` or unsolicited query text.
- Operator notes are bounded and warned but have no generic free-text DLP.

### Rollback

- Documentation-only closeout can be rolled back by reverting the WI-067
  documentation and deliverable files listed in the Git diff.
- Implementation rollback must revert the WI-067 source/test/tooling set as one
  coordinated change so frontend/backend DTO, parser, persistence, tests, and
  manifest expectations do not diverge.
- No database rollback is required for this proof: both exact disposable
  databases were dropped. A future source-manifest change requires a new
  approved observation/proof cycle; do not restore the predecessor constants.

## Follow-ups

- No next WI is blocked by WI-067 in the handoff chain.
- Continue SR-93 for retained-data strategy, live Toss verification,
  production deployment/operations, manual client acceptance, and explicit
  release approval.
