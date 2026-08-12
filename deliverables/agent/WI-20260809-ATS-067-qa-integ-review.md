---
version: 1.2
last_updated: 2026-08-13
project: ATS
owner: QA-INTEG
category: audit
status: accepted
dependencies:
  - path: WI-20260809-ATS-067-handoff.md
    reason: Approved scope and acceptance contract
  - path: WI-20260809-ATS-067-decision-register.md
    reason: Exact approved DG-067 dispositions
  - path: WI-20260809-ATS-056-evidence-pack.md
    reason: Preserved attempt, duplicate, count, recovery, and no-effect invariants
---

# Final QA-INTEG Re-review: WI-20260809-ATS-067

## Current Verdict

**ACCEPT**

No blocking finding remains at the repository and embedded-H2 evidence boundary.
The five v1.0 findings and the v1.1 parser P1 are resolved in current source and
focused executable tests. DG-067-01 through DG-067-09A pass. DG-067-09B remains
**HELD: `NOT RUN - NOT APPROVED`** and is not covered by this acceptance.

## Current Findings

No open P1 or P2 finding.

Non-blocking status note: the handoff correctly separates
`APPROVED_FOR_IMPLEMENTATION` from final QA acceptance, but its narrative still
describes the now-superseded v1.1 `REJECT`. This v1.2 review is the current
QA-INTEG verdict. The review's authorized write scope did not permit a handoff
edit.

## v1.1 Finding Resolution

| v1.1 finding | Resolution evidence | Status |
|---|---|---|
| P1 - Nonblank over-limit records were materialized before the row ceiling. | The parser now retains at most the header plus 1,000 nonblank logical data records. `addNonBlankRecord()` checks `MAX_RETAINED_RECORDS` before constructing the next `LogicalRecord`, `finishRecord()` propagates the limit signal, and `parseRecords()` returns immediately at the 1,001st data record (`PaymentSettlementCsvParser.java:20-21,95-239`). The dense 500,000-record suffix test proves the parser reports the row-limit error without reaching a later malformed record (`PaymentSettlementCsvParserTest.java:157-170`). | RESOLVED |

The early stop preserves the approved CSV semantics. Current tests still cover
strict UTF-8, exactly one leading BOM, quote/escaped-quote rules, quoted CRLF/LF
newlines, bare-CR and malformed/unbalanced-quote rejection, blank physical and
logical records, normalized/unique/known headers, exact row width, physical
line numbers, 1,000-at-limit, and 1,001-one-over behavior. A malformed 1,001st
record and an invalid header retain their prior error precedence
(`PaymentSettlementCsvParserTest.java:20-181`).

## Original Finding History

| v1.0 finding | Resolution evidence | Status |
|---|---|---|
| P1 - Durable-equal amount forms produced different deduplication hashes. | Amount parsing creates exact scale-2 values used unchanged by persistence and the deduplication basis (`AdminPaymentSettlementService.java:473-494,564-581`). The H2 different-key replay proves omitted and lexical zero/amount variants produce one Settlement, one import audit, one duplicate, conserved attempts, and scale-2 durable amounts (`AdminPaymentSettlementImportIntegrationTest.java:171-223`). | RESOLVED |
| P1 - Canonical/evidence values were trimmed before raw length validation. | Provider, identifier, money, currency, and date paths reject edge whitespace; identifier length is checked on the raw value before whitespace rejection, accepted case/value is preserved, and no identity field is truncated (`AdminPaymentSettlementService.java:459-557,617-628`). Tests cover raw one-over values containing edge spaces, controls, exact limits, every canonical/evidence field, and zero persistence for rejected rows (`AdminPaymentSettlementServiceTest.java:678-860`). | RESOLVED |
| P2 - Blank records could amplify a 5 MiB file into millions of retained objects. | Blank logical records are discarded before insertion, while physical line counting remains exact (`PaymentSettlementCsvParser.java:227-262`). The near-5 MiB blank-record test retains one data row with its exact line number (`PaymentSettlementCsvParserTest.java:99-129`). The v1.1 nonblank variant is independently resolved above. | RESOLVED |
| P2 - Reconciliation with row failures was announced as success. | The UI reloads first, treats reload failure as error, reports any failed/error/omitted result as warning, and emits success only for a clean aggregate (`PaymentOperationsPage.tsx:879-903`). Tests cover capped errors, failed-only, omitted-only, clean success, and reload failure with explicit no-success assertions (`PaymentOperationsPage.test.tsx:948-1128`). | RESOLVED |
| P2 - Handoff approval text conflicted and `omittedErrorCount` lacked a serialized contract guard. | The handoff separates implementation approval, QA verdict, and held MySQL execution (`WI-20260809-ATS-067-handoff.md:1-21`). The response record, MockMvc JSON assertion, TypeScript contract, and UI all carry `omittedErrorCount` (`AdminPaymentSettlementImportResponse.java:6-14`; `AdminPaymentControllerTest.java:224-263`; `frontend/src/api/admin.ts:397-405`). | RESOLVED |

## DG Audit

| Gate | Result | Independent conclusion |
|---|---|---|
| DG-067-01 | PASS | Filename, case-insensitive `.csv`, approved MIME family, and declared/actual 5 MiB boundaries reject before attempt claim; UI preflight remains advisory and aligned. |
| DG-067-02 | PASS | Strict UTF-8 rejects malformed/unmappable bytes; exactly one leading BOM is removed without replacement decoding. |
| DG-067-03 | PASS | Approved delimiter, quote, escape, newline, blank, header, width, line-number, and 1,000/1,001 logical-row semantics are deterministic; retained logical records are bounded and parsing stops at the established over-limit record. |
| DG-067-04 | PASS | Only exact `TOSS` is accepted; raw lengths, controls, edge whitespace, case preservation, deduplication input, and durable evidence stay aligned without truncation or silent rewrite. |
| DG-067-05 | PASS | Plain nonnegative amounts fit exact `DECIMAL(15,2)` scale 2 without rounding; durable-equal lexical forms canonicalize equally. Exact `KRW`, strict dates, payout ordering, and durable arithmetic mismatch evidence are enforced. |
| DG-067-06 | PASS | Import 1,000/1,001, reconciliation 30/90/91 days, and 5,000/5,001 rows are covered. Reconciliation queries IDs ascending with a 5,001 probe and rejects overflow before any row or audit mutation. |
| DG-067-07 | PASS | Execution remains synchronous with no new cursor, progress ledger, reconciliation operation key, automatic retry, or polling. Import returns its bounded row errors; reconciliation returns deterministic first-200 errors and the exact omitted count. |
| DG-067-08 | PASS | Optional `note` binds only as multipart; query-only note does not bind, the frontend emits no query parameter, and the existing 500-character behavior remains. |
| DG-067-09A | PASS | Current source count is derived as 42; predecessor manifest expectations are absent; pending-manifest Create/Validate fails before credentials, while Observe remains an explicit separately approved exact-target cleanup path. No database execution is claimed. |
| DG-067-09B | HELD | `NOT RUN - NOT APPROVED`. No MySQL or external datasource action was performed. |

## WI-056 Preservation

- Envelope validation precedes attempt claim; parsing follows the successful
  claim. Same-key POST does not parse or process again.
- Recovery remains header-only and owner-scoped; ADMIN list/numeric detail
  remains global and actor-attributed.
- Exact attempt and Settlement constraint classification, winner confirmation,
  unrelated-integrity fail-closed behavior, row transaction isolation, count
  conservation, and status-count conservation remain covered and passing.
- Partial-result context, full-success reset timing, bounded response-only row
  errors, and IGNORE authority, note, and immutability remain intact.
- Import, reconciliation, list/detail/recovery, and IGNORE introduce no payment,
  refund, subscription, billing-agreement, receipt, mail, or Provider effect.

## Verification Evidence

- Focused backend/embedded-H2 run: parser 12, service 47, controller 19,
  import integration 7, and IGNORE integration 2 -> **87 tests, 0 failures,
  0 errors, 0 skipped**.
- Focused frontend run: `adminContracts.test.ts` and
  `PaymentOperationsPage.test.tsx` -> **2 files, 109 tests, all passed**.
- Frontend `npm run typecheck` -> exit 0.
- Non-database `scripts/database/test-bootstrap-guards.ps1` -> **all 18 named
  checks passed**; no credentials were loaded and no connection was attempted.
- `git diff --check` -> exit 0.
- No MySQL/external system, retained data, secret, or repository `output/`
  artifact was accessed; no Git state was mutated by this review.

## Residual MySQL Gate

Current source derives 42 `CREATE TABLE` statements, but no current MySQL
table/column/index/foreign-key/plan manifest or hash exists. Embedded H2 and
non-database guard evidence do not prove MySQL behavior. DG-067-09B requires a
new immediate destructive/test approval with exact disposable names and
create/apply/validate/concurrency/drop scope before any database execution.
