---
version: 2.0
last_updated: 2026-08-12
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

This design covers ATStudio's PG-to-merchant subscription settlement evidence:

- ADMIN CSV import through `CSV_MANUAL`.
- Durable CSV import-attempt evidence and response-loss recovery.
- Atomic Settlement-row duplicate classification.
- Generated `SYSTEM_RECONCILIATION` review rows.
- Aggregate count conservation and orderless local-payment classification.

It does not cover creator royalty settlement, seller payout, tax invoice or
cash-receipt mutation, bank statement import, retained-data migration, or Toss
Settlement API automation. `TOSS_API` remains a future adapter.

CSV filename, MIME, byte-size, encoding, dialect/grammar, duplicate-header,
row-width, financial/provider field bounds, date-span, row-ceiling, batching,
cursoring, and retry-policy hardening remains held and out of scope for
WI-20260809-ATS-067 (`CR-031-115`, `CR-031-116`, `CR-031-118`). The current
parser behavior below is an implementation observation, not completed WI-067
hardening.

## 2. Current Source Adapters

| Source | Status | Current behavior |
|---|---|---|
| `CSV_MANUAL` | Implemented | ADMIN uploads CSV evidence; accepted rows are reconciled against local ledgers. |
| `SYSTEM_RECONCILIATION` | Implemented | A selected date range generates review rows for finalized local payments without imported provider evidence. |
| `TOSS_API` | Future | No current provider call or automated settlement adapter exists. |

Settlement reconciliation is accounting visibility. It never changes user
access, payment/refund status, billing agreements, receipts, mail, or Provider
state.

## 3. Observed CSV Contract

Required headers are case-sensitive:

- `provider`
- `order_id`
- `gross_amount`
- `net_settlement_amount`
- `settlement_base_date`

Current optional fields include provider payment/settlement identifiers,
refund/fee/VAT amounts, payout date, provider status, currency, and row note.
Amounts must parse as non-negative decimal values, `order_id` is bounded to 64
characters, dates use `yyyy-MM-dd`, and currency is a three-character code.
Blank lines are skipped and the current service guard accepts at most 1,000
nonblank data rows. UTF-8 BOM, quoted commas, and escaped double quotes are
handled by the current line parser; unknown columns are ignored.

These observations do not imply support for multiline quoted values or a
complete CSV grammar. Excel files must be exported to CSV. No parser-hardening
item owned by WI-067 is described as implemented.

## 4. Durable CSV Import Attempt

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

An empty multipart file, invalid ADMIN principal, or invalid Idempotency-Key is
rejected before an attempt is claimed. A claimed nonempty file that cannot be
read or orchestrated reaches terminal `FAILED` with a bounded failure code.

## 5. Idempotency-Key and Recovery

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

## 6. Transaction and Constraint Classification

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
tests. WI-056 integration/concurrency proof used H2; no MySQL lock, isolation,
deadlock, or constraint-message rehearsal was run.

## 7. Import Outcome Rules

For every normal import response:

```text
totalRows = importedRows + skippedDuplicateRows + failedRows
sum(statusCounts.values) = importedRows
```

`statusCounts` describes only newly persisted Settlement rows. Duplicate and
invalid rows do not inflate status counts. Every imported row appends exactly
one `PAYMENT_SETTLEMENT_IMPORTED` audit in the same successful row transaction;
the duplicate path appends no second import audit.

The initial response includes all currently returned row-number/message errors.
The attempt ledger keeps only counts. A same-key POST never recreates a
Settlement, audit, or attempt.

## 8. Missing-Settlement Reconciliation

Reconciliation remains separate from the CSV attempt ledger and has no
Idempotency-Key or recovery API in WI-056. It selects finalized local payments
for the requested date range and classifies each selected row exactly once:

- imported: a new `PROVIDER_SETTLEMENT_NOT_FOUND` review row and one
  `PAYMENT_SETTLEMENT_RECONCILED` audit are persisted;
- duplicate: provider evidence or an exact deduplication winner already exists;
- failed: the payment is orderless/unusable or row persistence fails.

An orderless finalized payment increments `failedRows` once and returns a
bounded error without creating a Settlement or row audit. Every normal response
satisfies the same total-count invariant, and its `statusCounts` sum equals
`importedRows`.

## 9. Frontend Recovery State

The Settlement tab creates a secure UUIDv4 only after explicit confirmation,
stores one pending record under
`ats.admin.settlement-import-attempt.v1`, and sends one POST with authentication
replay disabled.

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

The optional operator-note textarea has `maxLength=500` and a visible warning
not to enter PII, credentials, payment keys, or other sensitive information.
The application stores the operator's text as local evidence; it does not
derive secrets from it and does not claim that free text can never contain a
secret.

## 10. Security and Side-Effect Boundary

- Application code and tests do not log the raw Idempotency-Key or operator
  note. Infrastructure owners must separately configure access logs, reverse
  proxies, tracing, and APM not to collect or record `Idempotency-Key`.
- The raw key is header-only for import and recovery. It is never accepted in a
  path or query parameter.
- Imported files, raw rows, raw Provider payloads, credentials, and secrets are
  not retained in the attempt ledger.
- Import/recovery may read local order, finalized-payment, and succeeded-refund
  evidence. Intended writes are limited to the import attempt, Settlement, and
  Settlement row audit ledgers.
- Reconciliation writes only Settlement rows and row audits. Import,
  reconciliation, list, detail, recovery, and IGNORE do not charge, refund,
  cancel, mutate subscription/billing-agreement/payment state, create receipt
  or mail effects, or call a Provider.

## 11. Verification Boundary

Focused H2 evidence proves one durable Settlement and one row audit under a
same-row race, complementary imported/duplicate outcomes, isolated unrelated
rows in a multi-row race, all-duplicate durable attempts, same-key no-reprocess,
owner isolation, count conservation, and zero forbidden side effects.

This does not certify MySQL InnoDB lock timing, deadlock handling, isolation,
current fresh-schema manifest/hash, retained-data migration, live Provider
behavior, production logging configuration, deployment, or operator acceptance.

## Related Documents

- [API Specification](api-spec.md): Current endpoint and response contract.
- [DB Schema](db-schema.md): Current fresh-only DDL and entity contract.
- [Payment Operations Runbook](payment-operations-runbook.md): Operator procedure.
- [Security Policy](../policies/security-policy.md): Key, logging, and free-text controls.
