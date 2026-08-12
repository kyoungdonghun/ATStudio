---
version: 1.1
last_updated: 2026-08-13
project: ATS
owner: docops
category: evidence-pack
status: accepted
dependencies:
  - path: ../user/REQ-20260809-ATS-001.md
    reason: Approved parent requirement
  - path: WI-20260809-ATS-067-handoff.md
    reason: Decision-gate and implementation contract
  - path: WI-20260809-ATS-056-evidence-pack.md
    reason: Invariants preserved by this decision
  - path: WI-20260809-ATS-067-qa-integ-review.md
    reason: Accepted repository and embedded-H2 review boundary
  - path: WI-20260809-ATS-067-pg-review.md
    reason: Accepted privacy and security review boundary
---

# Decision Register: WI-20260809-ATS-067

## Approval Evidence

| Field | Recorded value |
|---|---|
| Decision status | `APPROVED_AND_EXECUTED` |
| Approval date | `2026-08-13` |
| Approval source | Current user conversation |
| Scope | DG-067-01 through DG-067-09B |
| MySQL execution status | `RUN-PASS-CLEANED` |
| MySQL approval scope | Exact loopback targets `ats_disposable_20260813_wi067obs` and `ats_disposable_20260813_wi067prf`; observe/create/apply/validate/concurrency/drop only |

## Approved Decisions

| Gate | Approved policy | Conservative implementation interpretation | Traceability and affected layers |
|---|---|---|---|
| DG-067-01 | Accept only filenames ending in `.csv`, with case-insensitive extension matching, and a maximum filename length of 255 characters. Accept the CSV MIME family and missing or blank MIME; reject other MIME values. Maximum file size is 5 MiB. Envelope violations are rejected before an import-attempt claim. | 5 MiB means 5,242,880 bytes. A missing filename or a filename without the accepted extension fails the envelope. Browser `accept` is advisory; server validation is authoritative. Generic non-CSV MIME values are not implicitly accepted. | `CR-031-115` / `F-INTEG-029-B05`; UI file guidance, multipart controller, service envelope validation, and HTTP/service boundary tests. |
| DG-067-02 | Accept UTF-8 only. Accept and remove one leading UTF-8 BOM. Malformed or unmappable bytes fail the entire file. Do not accept CP949 or any other legacy encoding. | Strict decoding must report a bounded file-level validation failure; replacement characters and heuristic encoding detection are prohibited. | `CR-031-115` / `F-INTEG-029-B05`; decoder, import orchestration, error contract, and decoder tests. |
| DG-067-03 | Use comma delimiter, double-quote quoting, doubled-quote escaping, quoted newlines, and CRLF or LF records. Normalize headers with trim plus lowercase. Reject duplicate headers after normalization and reject unknown headers. Require exact row width. Ignore blank physical and blank logical records. | Required headers remain required. Header order is not significant; values map by normalized header position. Missing, extra, and trailing cells that make row width differ from the header are rejected. Malformed or unbalanced quoting fails the file rather than altering evidence. No parser dependency or broader RFC claim is approved by this decision. | `CR-031-115` / `F-INTEG-029-B05`; parser, normalized row, row numbering/counting, error response, and dialect conformance tests. |
| DG-067-04 | V1 CSV imports accept only the canonical Provider value `TOSS`, while preserving the existing enum and multi-PG architecture. `order_id` has maximum length 64. Provider identifiers have maximum length 200. `provider_status` has maximum length 100. Reject control/newline characters and overlength values; do not truncate. Preserve identifier case. | `TOSS` is the importer allowlist, not a removal of other enum values from the architecture. No additional identifier case folding or silent rewriting is approved. The same validated value must be used for deduplication, persistence, ADMIN display, and audit evidence. | `CR-031-116` / `F-INTEG-029-B06`; normalized row, service validation, deduplication basis, Settlement persistence, row audit, ADMIN response, and boundary tests. |
| DG-067-05 | Accept plain decimal notation only: no exponent and no grouping comma. Amounts must be nonnegative, have scale at most 2, and fit `DECIMAL(15,2)`; reject rather than round. Accept `KRW` only. Dates use strict `yyyy-MM-dd`. Payout date must not precede base date. A structurally valid arithmetic mismatch remains persisted and reconciled as `MISMATCHED` evidence. | Plain decimal is digits with an optional decimal point followed by one or two digits; signs, exponent notation, grouping, non-digits, excess scale, and values above 9,999,999,999,999.99 are rejected. No additional oldest-date or future-date boundary is introduced by this decision. | `CR-031-116` / `F-INTEG-029-B06`; field validation, deduplication basis, Settlement persistence/reconciliation, status counts, audit, and exact-boundary tests. |
| DG-067-06 | Maximum import size is 1,000 logical data rows. Header and blank records do not count. Every nonblank logical data record, including a rejected or duplicate record, counts. Reconciliation defaults to 30 days, allows at most 90 inclusive calendar days, and selects at most 5,000 rows. If the selected-row ceiling is exceeded, reject the whole reconcile request before mutation. | A default 30-day inclusive range ends on the effective current date and begins 29 days earlier. Inclusive span is `daysBetween(from, to) + 1`; 90 passes and 91 fails. The 1,001st counted import record rejects the file. Reconciliation must establish the selected count before any Settlement or audit mutation. | `CR-031-115`, `CR-031-118` / `F-INTEG-029-B05`, `F-INTEG-029-B08`; parser count, reconciliation request/query/service, transaction boundary, aggregate response, and at-limit/one-over tests. |
| DG-067-07 | Processing remains synchronous. Add no cursor, progress ledger, or reconciliation operation key. Add no automatic retry or polling. Import returns all row errors up to the 1,000-row ceiling. Reconciliation returns the first 200 error details and an explicit omitted count with the existing aggregate summary. | Minimal compatible DTO change: retain the existing `errors` list and aggregate fields, and add additive integer field `omittedErrorCount`. For import it is `0`; for reconciliation it is `max(0, failedRows - errors.size())`, with `errors` in deterministic processing order and capped at 200. Preserve WI-056's single immediate read-only recovery after uncertain import transport; it is not repeated polling, and no second POST is automatic. | `CR-031-118` / `F-INTEG-029-B08`; service execution, response DTO, frontend API/result display, bounded error memory, and synchronous/no-retry tests. |
| DG-067-08 | Move optional `note` from the query parameter to an optional multipart form part. Retain the existing 500-character behavior. Provide no query-parameter compatibility fallback unless separately approved later. | The multipart part remains named `note`; omission remains valid. Frontend and backend change together, and tests must prove the note is absent from the request target/query. This decision does not broaden note content, visibility, retention, or logging policy. | `CR-031-115` / `F-INTEG-029-B05`; frontend multipart request, controller binding, OpenAPI/current docs during implementation, bounded note validation, and contract tests. |
| DG-067-09A | Replace the predecessor active constants only with values emitted by the approved MySQL observation. | The recorded expectation is 42 tables, 506 columns, 173 index rows, 90 foreign keys, 6 plans, plan-key equality at 6, zero forbidden tables/columns, and SHA-256 `acf28c935bf6107a8f2af431c971ebe0cd3539dba1aa1a941d966dde4a2a7a65`. The predecessor 41/493/168/89 manifest and `c581...` hash remain historical only. Guard checks passed 20/20 and `Preflight` reported `RECORDED`. | `CR-031-115`, `CR-031-116`, `CR-031-118`; disposable-MySQL bootstrap tooling and non-destructive tooling tests. |
| DG-067-09B | Separately approve one exact observation target and one exact proof target on loopback MySQL, with exact-target cleanup and no external effects. | `ats_disposable_20260813_wi067obs` applied schema/seed, emitted the manifest, failed closed as expected while unrecorded, and passed cleanup plus follow-up exact Drop. `ats_disposable_20260813_wi067prf` passed Create, independent Validate, exact manifest comparison, three Hibernate `ddl-auto=validate` MySQL concurrency tests, and exact Drop. No existing database, Provider, payment, refund, mail, or secret output was involved. Status is `RUN-PASS-CLEANED`; the one-use approval is exhausted. | `CR-031-115`, `CR-031-116`, `CR-031-118`; separately approved MySQL evidence lane. |

## WI-056 Preservation Contract

Implementation under these decisions must preserve all WI-056 invariants:

- One explicit CSV import action claims one durable import attempt, while DG-067-01 envelope failures occur before claim.
- A same-key POST never parses or processes the file again. Recovery remains header-only and owner-scoped; ADMIN list and numeric detail remain global actor-attributed audit views.
- Exact attempt and Settlement duplicate-constraint classification, winner confirmation, and unrelated-integrity fail-closed behavior remain unchanged.
- Every normal result preserves `totalRows == importedRows + skippedDuplicateRows + failedRows` and `sum(statusCounts) == importedRows`.
- Raw file bytes, raw CSV rows, Provider payloads, credentials, and secrets are not retained. Import row errors remain response-only.
- Import, reconciliation, list, detail, recovery, and IGNORE continue to cause zero payment, refund, subscription, billing-agreement, receipt, mail, or Provider mutation/invocation.

## Implementation Boundary

This register records the approved implementation and the separately approved,
completed DG-067-09B proof. It does not approve a new dependency,
schema/architecture beyond the stated additive DTO contract, retained-data
migration/backfill, production or existing-database access, external effects,
Git mutation, reuse of either disposable name, or another database execution.
Final closeout passed the backend test, JaCoCo report/verification, and assemble
command with 1,542 tests, zero failures, and 19 skips. Frontend coverage passed
73 files and 827 tests with zero failures; typecheck, lint, repository `format`,
and build also passed.
