---
version: 3.0
last_updated: 2026-08-13
project: ATS
owner: SA
category: design
status: stable
dependencies:
  - path: api-spec.md
    reason: Current ADMIN endpoint and response contracts
  - path: db-schema.md
    reason: Current Settlement and import-attempt persistence contract
  - path: payment-operations-runbook.md
    reason: Operator procedure and recovery boundary
  - path: ../policies/security-policy.md
    reason: Idempotency-Key, logging, and operator-note controls
---

# Payment Settlement Import and Reconciliation Design

> Purpose: Define the implemented CSV settlement import, durable attempt,
> duplicate classification, reconciliation, and recovery contracts.

## 1. Scope

This design covers ATStudio's PG-to-merchant subscription settlement evidence
at the current WI-20260809-ATS-067 repository boundary:

- ADMIN CSV import through `CSV_MANUAL`.
- The exact file envelope, strict UTF-8 decoder, CSV dialect, header, row,
  identifier, financial, currency, and date rules.
- Durable CSV import-attempt evidence and response-loss recovery.
- Atomic Settlement-row duplicate classification.
- Bounded `SYSTEM_RECONCILIATION` review rows and bounded returned errors.
- Aggregate count conservation and deterministic orderless local-payment
  classification.

It does not cover creator royalty settlement, seller payout, tax invoice or
cash-receipt mutation, bank statement import, retained-data migration, or Toss
Settlement API automation. `TOSS_API` remains a future adapter.

DG-067-01 through DG-067-09B are implemented. QA-INTEG v1.2 and PG v1.1
accepted the repository and embedded-H2 boundary, and the separately approved
DG-067-09B fresh-MySQL proof is `RUN-PASS-CLEANED`. No deployment, retained-data
migration, external Provider behavior, or client acceptance is claimed.

## 2. Current Source Adapters

| Source | Status | Current behavior |
|---|---|---|
| `CSV_MANUAL` | Implemented | ADMIN uploads CSV evidence; accepted rows are reconciled against local ledgers. |
| `SYSTEM_RECONCILIATION` | Implemented | A selected date range generates review rows for finalized local payments without imported provider evidence. |
| `TOSS_API` | Future | No current provider call or automated settlement adapter exists. |

Settlement reconciliation is accounting visibility. It never changes user
access, payment/refund status, billing agreements, receipts, mail, or Provider
state.

## 3. Multipart and File Envelope

`POST /api/admin/payments/settlements/import` consumes
`multipart/form-data` with these inputs:

| Input | Contract |
|---|---|
| `file` | Required multipart file part. |
| `note` | Optional multipart text part. A query-only `note` does not bind and there is no query-parameter compatibility fallback. |
| `Idempotency-Key` | Required header containing one canonical lowercase UUIDv4. |

The server validates the complete file envelope before claiming an import
attempt:

| Envelope field | Accepted rule |
|---|---|
| Filename | Present, nonblank, at most 255 characters, and ending in `.csv` with case-insensitive extension matching. |
| Part media type | Missing or blank, or exactly `text/csv`, `application/csv`, `text/comma-separated-values`, or `application/vnd.ms-excel` after trim and lowercase normalization. Other values are rejected. |
| Declared and actual bytes | Nonempty and at most 5 MiB (5,242,880 bytes). The service reads at most 5 MiB plus one byte to enforce the actual-byte ceiling. |

The SPA mirrors these checks as advisory preflight, uses
`accept=".csv,text/csv"`, and keeps the server authoritative. An envelope
failure returns the existing invalid-argument contract and creates no import
attempt, Settlement, or row audit.

## 4. UTF-8 and CSV Dialect

The importer accepts UTF-8 only. Decoding reports malformed and unmappable
input; it never inserts replacement characters or heuristically selects a
legacy encoding. One leading UTF-8 BOM is removed. CP949 and other legacy
encodings are not accepted.

The implemented dialect is intentionally defined by executable rules rather
than a broad RFC claim:

- The delimiter is comma and the quote character is double quote.
- A quote may open only at the beginning of a cell. A doubled quote inside a
  quoted cell represents one literal quote.
- Quoted commas and quoted LF or CRLF newlines are supported. A bare CR is
  rejected both inside and outside quoted cells.
- After a closing quote, only a comma, an LF/CRLF record separator, or end of
  file is valid. Malformed and unbalanced quotes fail the file.
- A trailing record separator is accepted. Physical or logical records whose
  cells are all blank are ignored. The row number reported for a retained
  logical record is its starting physical line.
- The parser retains at most the header plus 1,000 nonblank logical data
  records and stops at the 1,001st data record. Header and blank records do not
  count; malformed-width, field-invalid, and duplicate rows do count. The
  ceiling is detected when the 1,001st logical record closes, so malformed
  grammar while forming that record remains a file grammar error.

The first nonblank logical record is the header. Header names are trimmed and
lowercased. Header order is free, but names must be unique after normalization,
all required headers must exist, and every header must be in the allowlist.
Unknown and duplicate headers fail the file.

Required headers:

- `provider`
- `order_id`
- `gross_amount`
- `net_settlement_amount`
- `settlement_base_date`

Complete allowed header set:

- `provider`, `provider_payment_key`, `provider_settlement_id`, `order_id`
- `gross_amount`, `refund_amount`, `fee_amount`, `vat_amount`,
  `net_settlement_amount`, `currency`
- `settlement_base_date`, `settlement_payout_date`, `provider_status`, `note`

Every data record must have exactly the header width. A missing, extra, or
trailing cell that changes the width becomes a row error and does not stop
other rows. Decoder, grammar, header, and 1,001-row violations fail the entire
file before any Settlement row is processed. Because parsing occurs after a
successful attempt claim, that claimed attempt becomes `FAILED` with bounded
failure code `CSV_READ_FAILED`.

## 5. Row Validation and Canonicalization

The importer validates financial evidence before deduplication,
reconciliation, persistence, or audit use.

| Field | Current rule |
|---|---|
| `provider` | Required and exactly `TOSS`. No case folding is performed. The enum/provider-neutral architecture remains available for future approved adapters. |
| `order_id` | Required, 1-64 characters. |
| `provider_payment_key` | Optional; empty becomes null; maximum 200 characters. |
| `provider_settlement_id` | Optional; empty becomes null; maximum 200 characters. |
| `provider_status` | Optional; empty becomes null; maximum 100 characters. |
| Evidence characters | Provider and identifier/status values reject ISO control characters, Unicode line separator U+2028, Unicode paragraph separator U+2029, and leading/trailing whitespace. Accepted case and content, including ordinary internal whitespace and Korean text, are preserved. Values are never silently truncated. |
| Amount fields | ASCII digits with an optional decimal point and one or two fraction digits. Signs, grouping commas, exponent notation, `.5`, `1.`, edge whitespace, negative values, and excess scale are rejected. |
| Amount range | At most 13 significant integer digits and two fraction digits, matching `DECIMAL(15,2)`; maximum 9,999,999,999,999.99. Values are rejected rather than rounded. |
| Amount defaults | Missing or empty `refund_amount`, `fee_amount`, and `vat_amount` become `0.00`. `gross_amount` and `net_settlement_amount` are required. |
| Amount canonicalization | Leading integer zeros are removed and every accepted value is converted to exact scale 2 before deduplication and persistence. Durable-equal forms such as `1`, `1.0`, `1.00`, and `001.00` therefore use the same value and deduplication basis. |
| `currency` | Exactly `KRW`; missing or empty becomes `KRW`. No case folding is performed. |
| Dates | Strict `yyyy-MM-dd`. Payout date is optional but must not precede the settlement base date. No oldest-date or future-date boundary is implemented for CSV rows. |

An arithmetically inconsistent but structurally valid row is not discarded.
For example, a net value unequal to gross minus refund, fee, and VAT persists
as `MISMATCHED` evidence for review. CSV `note` remains optional operator text;
the multipart note takes precedence when nonblank, otherwise the trimmed CSV
note is used, and the persisted Settlement note is bounded to 500 characters.

## 6. Durable CSV Import Attempt

`payment_settlement_import_attempts` records every successfully claimed,
nonempty CSV import operation, including all-duplicate and orchestration-failed
attempts.

| Field | Contract |
|---|---|
| `id` | Numeric durable identity; public batch value is `ATS-SETTLE-ATTEMPT-{id}`. |
| `key_digest` | Unique 64-character owner-scoped opaque digest; the raw key is never stored. |
| `actor_user_id` | Authenticated active ADMIN that claimed the attempt. |
| `state` | `PROCESSING`, `COMPLETED`, or `FAILED`. |
| count fields | `total_rows`, `imported_rows`, `duplicate_rows`, `failed_rows`. |
| `operator_note` | Optional operator-supplied text, normalized and bounded to 500 characters for the attempt row. |
| `failure_code` | Bounded internal orchestration code, never a raw exception message. |
| timestamps | Creation/update, terminal completion time. |

A `COMPLETED` row can represent full success, partial success, or an
all-duplicate file. Entity logic and the database check constraint both require:

```text
total_rows = imported_rows + duplicate_rows + failed_rows
```

The attempt ledger does not store operation type/source columns because this
table is CSV-import-specific. It also does not store file bytes, raw rows,
filename, Provider payloads, Provider credentials, per-row error text, or
`statusCounts`. Parsed Settlement rows retain their existing structured source
fields and batch linkage; the attempt remains the file-operation ledger.

The complete filename/MIME/byte envelope, invalid ADMIN principal, and invalid
Idempotency-Key are rejected before an attempt is claimed. A claimed file that
cannot be decoded, parsed, or orchestrated reaches terminal `FAILED` with a
bounded failure code.

## 7. Idempotency-Key and Recovery

`POST /api/admin/payments/settlements/import` requires a canonical lowercase
UUIDv4 in `Idempotency-Key`. The value is not trimmed or normalized. The server
derives:

```text
SHA-256("settlement-csv-import\0v1\0" + adminUserId + "\0" + rawKey)
```

Only the 64-character digest is persisted. Including the ADMIN ID makes the
same raw key independent across owners. The raw key is absent from the database,
application logs, request URL, and query string. The browser keeps the pending
raw key in `sessionStorage` only for same-attempt recovery.

The attempt claim occurs before CSV parsing. A duplicate claim for the same
owner/key never processes the file again and returns HTTP `409` with a stable
business error for the durable state:

| Existing state | POST result | Required next action |
|---|---|---|
| `PROCESSING` | `SETTLEMENT_IMPORT_ATTEMPT_IN_PROGRESS` | Read recovery with the same key. |
| `COMPLETED` | `SETTLEMENT_IMPORT_ATTEMPT_COMPLETED` | Read the durable aggregate with the same key. |
| `FAILED` | `SETTLEMENT_IMPORT_ATTEMPT_FAILED` | Read the failure, then use a new key only for a new explicit action. |

No file fingerprint or note/file equivalence comparison is performed. A key
identifies one explicit operation, not file content.

ADMIN-only read APIs are:

| API | Result |
|---|---|
| `GET /api/admin/payments/settlement-import-attempts` | Paged latest-first list under `dataList` and `pageInfo`. |
| `GET /api/admin/payments/settlement-import-attempts/{attemptId}` | Numeric-ID detail under `data`. |
| `GET /api/admin/payments/settlement-import-attempts/recovery` | Owner-scoped lookup using only the `Idempotency-Key` header. |

Recovery returns aggregate durable evidence only. Per-row errors remain
response-only because retaining them would retain imported row context.

## 8. Transaction and Constraint Classification

The import orchestrator is not one outer database transaction:

1. Claim the attempt in `REQUIRES_NEW` through
   `uq_payment_settlement_import_attempts_key_digest`.
2. Parse the file in the orchestrator.
3. Reconcile and `saveAndFlush` each usable Settlement plus its row audit in a
   separate `REQUIRES_NEW` row transaction.
4. Catch a row constraint exception only after that row transaction rolls back.
5. Complete or fail the attempt in another `REQUIRES_NEW` transaction guarded
   by a pessimistic lock and terminal-state check.

This boundary prevents a losing duplicate insert from leaving the caller in a
rollback-only transaction or erasing an unrelated successful row.

Constraint translation is fail-closed:

- Settlement duplicate classification accepts the exact Hibernate constraint
  name `uq_payment_settlements_deduplication_key`, a MySQL duplicate-entry
  message naming that exact key, or H2 SQLState `23505` identifying
  `payment_settlements(deduplication_key)`. A post-rollback read must also find
  the exact deduplication winner.
- Attempt replay classification accepts the exact Hibernate constraint name
  `uq_payment_settlement_import_attempts_key_digest`, MySQL SQLState `23000`
  plus error `1062` and the exact key reference, or H2 SQLState `23505` plus the
  exact constraint/table/column signature.
- An unrelated foreign-key, check, not-null, or differently named unique
  violation is never translated to duplicate/replay. Import marks the durable
  attempt failed and returns the orchestration error; reconciliation classifies
  that selected row as failed.

The MySQL signatures above are covered by synthetic exception-classification
tests. The approved fresh-MySQL proof additionally ran the different-operation
key deduplication race, same-owner/same-operation-key race, and concurrent
`IGNORE` race against MySQL 8/InnoDB at `REPEATABLE-READ`; all three passed.
This bounded proof does not establish every possible deadlock or infrastructure
failure mode.

## 9. Outcome and Error Contracts

For every normal import response:

```text
totalRows = importedRows + skippedDuplicateRows + failedRows
sum(statusCounts.values) = importedRows
```

`statusCounts` describes only newly persisted Settlement rows. Duplicate and
invalid rows do not inflate status counts. Every imported row appends exactly
one `PAYMENT_SETTLEMENT_IMPORTED` audit in the same successful row transaction;
the duplicate path appends no second import audit.

The response additionally carries `omittedErrorCount`:

| Operation | `errors` | `omittedErrorCount` |
|---|---|---:|
| CSV import | Every row error within the 1,000-row ceiling, in input order. | Always `0`. |
| Missing-settlement reconciliation | The first 200 row errors in deterministic payment-ID processing order. | `max(0, failedRows - errors.size())`. |

Errors contain only row number and bounded fixed-format text; they do not copy
raw row values. Import row errors remain response-only. The attempt ledger
keeps aggregate counts but no error details. A same-key POST never recreates a
Settlement, audit, or attempt.

## 10. Missing-Settlement Reconciliation

Reconciliation remains synchronous and separate from the CSV attempt ledger.
It has no Idempotency-Key, operation identity, cursor, progress ledger,
automatic retry, polling, or recovery API.

The server applies these exact bounds:

- When both dates are omitted, the inclusive range is today minus 29 days
  through today (30 calendar days). Each individually omitted boundary uses
  that same default boundary.
- `baseDateFrom` must not be after `baseDateTo`; the inclusive span is at most
  90 calendar days. A 90-day range passes and a 91-day range fails.
- There is no separate oldest-date or future-date rejection.
- The query selects `DONE` `subscription_payments` whose `createdAt` falls
  within the selected days, orders by numeric ID ascending, and probes at most
  5,001 rows. A 5,001-row result rejects the whole request before any
  Settlement or audit mutation; at most 5,000 rows are processed.

Each selected row is classified exactly once:

- imported: a new `PROVIDER_SETTLEMENT_NOT_FOUND` review row and one
  `PAYMENT_SETTLEMENT_RECONCILED` audit are persisted;
- duplicate: provider evidence or an exact deduplication winner already exists;
- failed: the payment is orderless/unusable or row persistence fails.

An orderless finalized payment increments `failedRows` once and returns a
bounded error without creating a Settlement or row audit. Reconciliation row
writes use separate `REQUIRES_NEW` transactions. Every normal response satisfies
the same total-count invariant, and its `statusCounts` sum equals `importedRows`.

## 11. Frontend Behavior

The Settlement tab creates a secure UUIDv4 only after explicit confirmation,
stores one pending record under
`ats.admin.settlement-import-attempt.v1`, and sends one POST with authentication
replay disabled. The frontend trims a nonblank operator note and appends it as
the optional multipart `note` part; it does not add query parameters.

- A transport error triggers one read-only recovery GET with the same key.
- `PROCESSING` retains the key, selected `File`, DOM input, and note and exposes
  a manual recovery button. There is no polling or automatic second POST.
- A pending stored attempt blocks a new import. Corrupt stored state fails
  closed and also blocks import/recovery until the browser session is cleared.
- Terminal recovery clears the pending key. `FAILED` requires a new explicit
  action; `COMPLETED` reloads the Settlement list.
- Only a completed zero-failure outcome plus successful list reload clears the
  selected file and keyed DOM input. Partial outcomes retain correction context.
- Per-row errors cannot be reconstructed from recovery because they are not
  persisted; the screen states that limitation.
- A reconciliation result is reported as partial when `failedRows`, returned
  `errors`, or `omittedErrorCount` is nonzero. The result panel displays the
  omitted count and returned details; clean success is shown only after the
  Settlement-list reload succeeds.

The optional operator-note textarea has `maxLength=500` and a visible warning
not to enter PII, credentials, payment keys, or other sensitive information.
The application stores the operator's text as local evidence; it does not
derive secrets from it and does not claim that free text can never contain a
secret.

## 12. Security and Side-Effect Boundary

- Application code and tests do not log the raw Idempotency-Key or operator
  note. Infrastructure owners must separately configure access logs, reverse
  proxies, tracing, and APM not to collect or record `Idempotency-Key`.
- The raw key is header-only for import and recovery. It is never accepted in a
  path or query parameter.
- The request-level operator note is multipart-only. Unsolicited query text
  does not bind; the separate allowlisted CSV `note` cell remains row input.
  Infrastructure owners must still suppress or redact unexpected query strings
  and request capture on this route.
- Imported files, raw rows, raw Provider payloads, credentials, and secrets are
  not retained in the attempt ledger.
- Import/recovery may read local order, finalized-payment, and succeeded-refund
  evidence. Intended writes are limited to the import attempt, Settlement, and
  Settlement row audit ledgers.
- Reconciliation writes only Settlement rows and row audits. Import,
  reconciliation, list, detail, recovery, and IGNORE do not charge, refund,
  cancel, mutate subscription/billing-agreement/payment state, create receipt
  or mail effects, or call a Provider.

## 13. MySQL Proof Boundary

Current `schema.sql` contains 43 derived `CREATE TABLE` statements, and the
current entity source contains 43 JPA entities. The disposable bootstrap's
source preflight requires exactly 43. The live/disposable MySQL manifest is
`RECORDED` from guarded fresh disposable evidence; guarded `Create` and
independent `Validate` compare the current manifest exactly. This does not
prove retained-data migration or production readiness.

### Historical WI-067 Evidence (Superseded 42-Table Source Snapshot)

The following values are preserved only as WI-067 historical evidence. They
are not a current manifest and do not provide a runnable current `Create` or
`Validate` path:

| Field | Historical WI-067 value |
|---|---:|
| Tables / columns | 42 / 506 |
| Index rows / foreign keys | 173 / 90 |
| Plans / plan keys | 6 / 6 |
| Forbidden tables / columns | 0 / 0 |
| SHA-256 | `acf28c935bf6107a8f2af431c971ebe0cd3539dba1aa1a941d966dde4a2a7a65` |

`ats_disposable_20260813_wi067obs` applied schema/seed, emitted those values,
failed closed as designed while the expectation was unrecorded, and passed
cleanup plus follow-up exact Drop. After the tooling recorded only the emitted
values, all 20 guards and `Preflight` passed. The distinct proof database
`ats_disposable_20260813_wi067prf` passed `Create`, independent `Validate`, exact
manifest comparison, three Hibernate `ddl-auto=validate` concurrency tests,
and exact `Drop`. Neither target remains.

## 14. Verification and Review Boundary

QA-INTEG v1.2 and PG v1.1 both returned `ACCEPT` with no open P1 or P2 finding
at the reviewed repository/non-database boundary. QA-INTEG recorded 87 focused
backend/embedded-H2 tests, 109 focused frontend tests, frontend typecheck, and
18 non-database bootstrap guard checks passing. PG separately recorded 60
focused parser/service tests, 120 focused frontend tests, and the same 18 guard
checks passing.

The later MySQL lane recorded 3 tests passed with zero failures, errors, or
skips. The H2 import and IGNORE regression suites passed 8 and 2 tests,
respectively. In the default environment, the three opt-in MySQL tests skipped
as designed. No existing database, Provider, payment, refund, mail, or secret
output was involved.

Those reviews cover strict decoding/parsing, envelope and field boundaries,
scale-2 deduplication alignment, 1,000/1,001 import rows, 30/90/91 reconciliation
days, 5,000/5,001 selected payments, first-200 error retention, multipart-only
note transport, UI partial-result handling, preserved WI-056 invariants, and
zero forbidden external effects.

Final closeout passed the backend test/JaCoCo/assemble command with 1,542 tests,
zero failures, and 19 skips. Frontend coverage passed 73 files and 827 tests
with zero failures; typecheck, lint, repository format, and build also passed.
This does not certify retained-data migration, exhaustive MySQL failure modes,
live Provider behavior, production logging configuration, deployment,
operator/client acceptance, or production readiness.

## Related Documents

- [API Specification](api-spec.md): Current endpoint and response contract.
- [DB Schema](db-schema.md): Current fresh-only DDL and entity contract.
- [Payment Operations Runbook](payment-operations-runbook.md): Operator procedure.
- [Security Policy](../policies/security-policy.md): Key, logging, and free-text controls.
