---
version: 1.1
last_updated: 2026-07-17
project: ATS
owner: SA
category: design
status: stable
source_req: REQ-20260526-ATS-001
---

# Payment Settlement Import and Reconciliation Design

> Scope: ATStudio subscription payment settlement import and reconciliation.
> This implemented design covers ATStudio's own PG-to-merchant CSV settlement evidence. It does not cover creator royalty settlement, seller payout, tax invoice issuance, cash receipt mutation, bank statement import, or Toss Settlement API automation.

## 1. Purpose

ATStudio already records subscription charges, refunds, receipt evidence, reconciliation incidents, and payment operation audit logs. The next accounting-facing operation is to compare provider settlement evidence with ATStudio's local payment/refund ledgers.

This feature answers:

- Did a provider settlement row map to a local subscription payment?
- Did a local subscription payment appear in settlement evidence?
- Do gross amount, refund amount, fee, VAT, and net settlement amount look consistent?
- Which settlement rows need operator review?

Settlement reconciliation must not mutate subscription access, payment status, refund status, billing agreements, or provider state.

## 2. Source Adapter Strategy

Settlement source is modeled as an adapter boundary.

| Source | Status | Notes |
|---|---|---|
| `CSV_MANUAL` | First implementation | Admin uploads a CSV settlement file using the ATStudio settlement template. Excel files should be exported to CSV first. |
| `TOSS_API` | Future adapter | Toss Settlement API can be added later without replacing the ledger, reconciliation rules, or admin UI. |
| `SYSTEM_RECONCILIATION` | First implementation | System-generated review rows for local finalized payments that have no imported provider settlement evidence in the selected period. |

The application should normalize all sources into the same internal settlement row model. CSV import is not a one-off shortcut; it is the first source adapter.

## 3. CSV Template

Initial template columns:

| Column | Required | Meaning |
|---|---|---|
| `provider` | yes | `TOSS` for current recurring subscription settlement evidence. |
| `provider_payment_key` | conditional | Toss payment key or equivalent provider payment identifier. Required when available. |
| `order_id` | yes | Merchant order ID used by ATStudio payment orders. |
| `provider_settlement_id` | optional | Provider settlement row identifier if available. |
| `gross_amount` | yes | Original payment amount included in settlement evidence. |
| `refund_amount` | no | Refunded amount included in settlement evidence. Defaults to `0`. |
| `fee_amount` | no | Provider fee. Defaults to `0` if not provided. |
| `vat_amount` | no | VAT/tax amount for fee/settlement evidence when provided. |
| `net_settlement_amount` | yes | Amount expected to be paid to ATStudio after refund/fee/tax adjustment. |
| `settlement_base_date` | yes | Provider settlement sales/base date. |
| `settlement_payout_date` | no | Expected or actual payout date. |
| `provider_status` | no | Provider settlement status text. |
| `currency` | no | Defaults to `KRW`. |
| `note` | no | Operator import note. |

Validation rules:

- Header names are stable and case-sensitive in the first implementation.
- Amount fields must be integer KRW values or decimal-compatible numeric strings.
- `gross_amount`, `refund_amount`, `fee_amount`, `vat_amount`, and `net_settlement_amount` cannot be negative.
- `order_id` must be present and at most 64 characters.
- `provider_payment_key` and `provider_settlement_id` are support-safe identifiers, not secrets.
- Unknown extra columns are ignored unless an import adapter explicitly maps them later.

## 4. Ledger Model

Primary table: `payment_settlements`

Core fields:

- source: `CSV_MANUAL`, `SYSTEM_RECONCILIATION`, future `TOSS_API`
- provider
- provider settlement ID
- provider payment key
- order ID
- matched payment order ID
- matched subscription payment ID
- import batch ID
- source file name
- source row number
- gross amount
- refund amount
- fee amount
- VAT amount
- net settlement amount
- currency
- settlement base date
- settlement payout date
- provider status
- reconciliation status
- mismatch reason
- reconciled at
- ignored at/by/note
- sanitized source payload
- created/updated timestamps

Optional future table: `payment_settlement_import_batches`

This table stores import-level metadata:

- file name
- source
- provider
- imported by
- total rows
- imported rows
- skipped duplicate rows
- failed rows
- created timestamp

The first implementation keeps import batch metadata in the service response and per-row `import_batch_key` only. If batch history becomes useful in admin UI, prefer adding this table later rather than overloading `payment_settlements`.

## 5. Status Model

Settlement reconciliation status candidates:

| Status | Meaning |
|---|---|
| `IMPORTED` | Row was imported but not reconciled yet. |
| `MATCHED` | Local payment/refund data and settlement evidence are consistent enough for current policy. |
| `MISMATCHED` | A local record exists, but amount/refund/fee/net settlement values need review. |
| `LOCAL_PAYMENT_NOT_FOUND` | Provider settlement evidence exists but local subscription payment/order was not found. |
| `PROVIDER_SETTLEMENT_NOT_FOUND` | Local finalized payment has no corresponding settlement evidence for the selected period. |
| `IGNORED` | Operator intentionally excludes this row from active review. |

`PROVIDER_SETTLEMENT_NOT_FOUND` can be represented as a generated reconciliation issue rather than an imported row if there is no provider row. The UI should still show it as a settlement reconciliation issue.

## 6. Matching Rules

Primary matching:

1. Match by `order_id` to `payment_orders.order_id`.
2. Match by `provider_payment_key` to `payment_orders.pg_transaction_id` or `subscription_payments.provider_transaction_id` if available.
3. Resolve `subscription_payments` by linked `payment_order_id` or `order_id`.

Amount checks:

- `gross_amount` should match `subscription_payments.amount`.
- `refund_amount` should match the sum of succeeded `payment_refunds.amount` for that subscription payment.
- `net_settlement_amount` should equal `gross_amount - refund_amount - fee_amount - vat_amount` when all values are present and provider policy matches that formula.
- `fee_amount` and `vat_amount` are evidence fields; mismatch policy is warning-first because provider contract rules can vary.

Date checks:

- `settlement_base_date` is not assumed to be the local payment date.
- `settlement_payout_date` is not used to mutate revenue recognition or payment state.

## 7. Admin APIs

Initial backend API shape:

| API | Purpose |
|---|---|
| `POST /api/admin/payments/settlements/import` | Import settlement CSV evidence and reconcile imported rows. |
| `GET /api/admin/payments/settlements` | Paginated settlement row list with optional status/source/date filters. |
| `POST /api/admin/payments/settlements/reconcile` | Scan local finalized payments for selected date range and create missing-provider evidence review rows. |
| `PUT /api/admin/payments/settlements/{settlementId}/ignore` | Mark a settlement row as ignored with an operator note. |

The import endpoint should return:

- total row count
- imported count
- skipped duplicate count
- failed row count
- status counts
- row-level validation errors when import fails partially

## 8. Admin UI

Recommended UI location: `/admin/payments` settlement tab.

The first UI should provide:

- CSV import form
- template guidance through the documented CSV header set
- import result summary
- settlement row table
- filters: status, source, settlement base date, payout date
- columns: status, order ID, provider payment key, gross/refund/fee/VAT/net amounts, base date, payout date, matched local IDs, mismatch reason
- ignore action with a note for rows intentionally excluded from active review

## 9. Audit and Security Boundary

Allowed support-safe fields:

- order ID
- provider
- provider payment key
- provider settlement ID
- settlement dates
- amounts
- status
- mismatch reason
- source file name
- source row number

Forbidden:

- raw card number
- CVC or expiry
- billing key
- authKey
- customerKey
- Toss secret key
- raw provider payload
- bank account secrets

Audit expectations:

- Settlement import should create an operation audit row or batch history row.
- Ignore/status actions should create operation audit rows.
- Audit rows should not contain the full imported file.

## 10. Acceptance Checklist

- Importing a valid template creates settlement rows.
- Re-importing the same rows does not create duplicates.
- A row matching a local completed subscription payment is marked `MATCHED` when amounts align.
- A row with a different gross/refund/net amount is marked `MISMATCHED`.
- A row with no local payment match is marked `LOCAL_PAYMENT_NOT_FOUND`.
- A local payment without settlement evidence can be reported as `PROVIDER_SETTLEMENT_NOT_FOUND` for a selected period.
- Ignored rows remain auditable and can be filtered separately from active review rows.
- No settlement action changes user subscription access, billing agreements, payment status, refund status, or provider state.
