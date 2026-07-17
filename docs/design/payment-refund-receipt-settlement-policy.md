---
version: 1.2
last_updated: 2026-07-17
project: ATS
owner: SA
category: design
status: stable
source_req: REQ-20260525-ATS-002, REQ-20260525-ATS-004, REQ-20260525-ATS-005, REQ-20260526-ATS-001
---

# Payment Refund, Receipt, Settlement, and Tax Invoice Policy

> Scope: ATStudio subscription payment operations after recurring billing checkout.
> This document defines operating policy and implementation boundaries for refund, receipt, settlement, and tax invoice operations. Receipt evidence, operation audit logging, admin refund ledger/provider cancel APIs, refund-linked entitlement correction APIs, settlement import/reconciliation APIs/UI, and first-class admin receipt/audit/refund/entitlement/settlement UI are implemented. Tax invoice workflow and cash receipt issue/cancel automation are on hold under the current card-only recurring subscription scope.

## 1. Purpose

ATStudio recurring subscription payment is now recurring-first:

- New subscription uses Toss billing auth, then immediately performs the first charge.
- Upgrade charges the remaining-period difference through the active billing agreement.
- Downgrade and billing-cycle-only changes are scheduled for the next renewal.
- Renewal is run by ATStudio scheduler, not by Toss.
- Reconciliation incidents are detected, persisted, and triaged through `/admin/payments`.

The production concern after a charge has already happened is financial operations around the current card-only recurring subscription model. Refund, receipt, and settlement work must stay explicit because these operations can affect customer money, subscription access, evidence, and accounting records. Tax invoice workflow is documented as a future boundary only, not as a next implementation for the current card-only scope.

## 2. External Basis

This policy is based on the current Toss Payments and Korean National Tax Service public documentation as of 2026-05-25:

| Area | Source |
|---|---|
| Payment cancellation/refund | [Toss Payments cancel payment guide](https://docs.tosspayments.com/guides/v2/cancel-payment) |
| Idempotency and secret-key handling | [Toss Payments authorization and headers](https://docs.tosspayments.com/reference/using-api/authorization) |
| Payment result email and cash receipt behavior | [Toss Payments payment results guide](https://docs.tosspayments.com/guides/v2/learn/payment-results) |
| Cash receipt, settlement API references | [Toss Payments API reference](https://docs.tosspayments.com/reference) |
| PG settlement model | [Toss Payments settlement glossary](https://docs.tosspayments.com/resources/glossary/settlement) |
| Toss API key boundary | [Toss Payments API keys guide](https://docs.tosspayments.com/reference/using-api/api-keys) |
| Electronic tax invoice issuing routes | [National Tax Service e-tax invoice guide](https://www.nts.go.kr/nts/cm/cntnts/cntntsView.do?cntntsId=7788&mi=2462) |

Tax invoice policy in this document is a system policy baseline, not tax advice. Before live operation, the final tax invoice process must be reviewed by the company accountant, tax agency, or another qualified tax operator.

## 3. Current ATStudio Baseline

| Area | Current implementation |
|---|---|
| Charge ledger | `payment_orders` records attempts and stores the exact provider transaction ID as a protected server-side operation field plus sanitized provider payload. ADMIN responses expose only a masked support reference. |
| Finalized payment | `subscription_payments` stores successful subscription payment records. |
| Provider lookup | Reconciliation compares recent orders with Toss state by `orderId` when lookup is configured. |
| Admin view | `/admin/payments` lists orders, billing agreements, subscription payments, and reconciliation incidents. |
| Mutation boundary | Admin payment screens expose incident workflow status/note updates, receipt/audit views, and separate refund plus entitlement-correction operation tabs. Refund and entitlement-correction mutations remain separate admin-confirmed workflows, and destructive execution requires typed confirmation. Ordinary subscription status/cycle/expiration edits remain in the user subscription admin screen. |
| Operation audit | `payment_operation_audit_logs` stores reconciliation incident status changes, system-created receipt evidence audit rows, admin refund workflow transitions, and admin entitlement correction workflow transitions. |
| Refund state | `payment_refunds` stores admin refund request, approval, provider execution, idempotency, provider cancel transaction, and failure/pending-confirmation state. |
| Entitlement correction state | `payment_entitlement_corrections` stores refund-linked local access correction requests, before/target snapshots, approvals, execution actor, and result state. |
| Receipt state | `payment_receipts` stores provider receipt/cash receipt evidence after successful charges. Actionable URLs must be absolute HTTPS without credentials or non-standard ports; unsafe URLs are suppressed at ingestion and read mapping. |
| Settlement state | `payment_settlements` stores CSV/manual settlement evidence, generated missing-provider review rows, amount/refund/fee/VAT/net comparisons, ignore state, and support-safe source payload. |

### Current data gap

`PaymentOrder.pgTransactionId` is used as the provider transaction identifier after Toss billing charge. In Toss billing charge responses, this value may be the `paymentKey`. This value is copied into `payment_receipts.provider_payment_key` when receipt evidence exists and into `payment_refunds.provider_payment_key` when a refund request is created. These exact values are protected server/entity operation fields; ADMIN DTOs and UI expose only deterministic masked `REF-*` support references.

Current implementation introduced explicit ledgers for receipt evidence, refund workflow, refund-linked entitlement correction, settlement reconciliation, and payment operation audit logs. Tax invoice request ledgers should be introduced only if ATStudio later approves B2B invoice, bank-transfer, postpaid, or contract purchase payments.

## 4. Policy Principles

| Principle | Policy |
|---|---|
| Provider money movement must be idempotent | Refund/cancel requests must use an idempotency key and persist the request before provider execution. |
| Local subscription access is not changed by provider data alone | Provider lookup, webhook, or refund result must be reconciled with local policy before mutating `user_subscriptions`. |
| Refund is exceptional support operation | Normal user cancellation stops next renewal and preserves paid access until `expiresAt`; it is not the same as refund. |
| Evidence records are append-only first | Refund/receipt/settlement/tax invoice operations need audit records before destructive or financial mutation. |
| Separate financial evidence types | Card receipt, cash receipt, settlement, and tax invoice are different evidence types and must not be collapsed into one status. |
| No raw secrets | No raw billing key, auth key, customer key, Toss secret key, exact provider transaction ID, raw card data, raw provider payload, full reconciliation issue object, or transport exception message/stack in application logs, screenshots, or documents. |
| Tax handling requires external confirmation and a matching payment scope | Tax invoice workflow must not be enabled for the current card-only recurring subscription scope by default. Reopen it only after business type, VAT treatment, evidence duplication policy, and B2B invoice/payment scope are confirmed. |

## 5. Refund Policy

### 5.1 Definitions

| Term | Meaning |
|---|---|
| User cancellation | User stops future renewal. Paid access remains until the current `expiresAt`. |
| Provider refund/cancel | ATStudio requests Toss to cancel all or part of a completed payment. |
| Entitlement correction | Local subscription period/plan/access is changed after a financial correction. |
| Compensation case | Provider success plus local failure, duplicate charge, incorrect amount, or support-approved reversal. |

### 5.2 Default subscription refund stance

ATStudio subscription cancellation is not refund by default.

- If a user cancels subscription, the next renewal stops.
- The current paid period remains usable until `expiresAt`.
- No provider refund is requested automatically.

Refund is reserved for exceptional cases:

- Duplicate charge.
- Wrong amount.
- Provider success with local entitlement failure.
- Customer support approval.
- Legal/contractual obligation.

### 5.3 Full refund

Full refund should be allowed only through an audited admin operation.

Required evidence:

- `paymentOrderId`
- `subscriptionPaymentId`
- provider
- provider payment key
- amount and currency
- refund reason code
- human-readable note
- operator ID
- approval ID or linked incident ID
- idempotency key

Default entitlement policy:

| Case | Entitlement policy |
|---|---|
| Charge succeeded but entitlement was never delivered | Refund without granting entitlement. |
| First subscription payment fully refunded after activation | Expire or revoke the current subscription only through a separate entitlement correction action. |
| Upgrade charge fully refunded | Roll back the upgraded plan only through a separate entitlement correction action. |
| Renewal charge fully refunded | Reverse the renewal extension only through a separate entitlement correction action. |

The provider refund and local entitlement correction are separate audited steps. Current entitlement correction APIs require an admin to explicitly provide the target access state instead of inferring a previous plan rollback from payment history.

### 5.4 Partial refund

Partial refund should not automatically change plan, period, or pending changes.

Allowed examples:

- Support compensation.
- Incorrect overcharge correction.
- Manual goodwill adjustment.

Required policy:

- Partial refund amount must be less than or equal to refundable remaining amount.
- Multiple partial refunds must accumulate against the original payment.
- Remaining refundable amount must be computed locally and compared with provider data.
- Local `subscription_payments` should not be overwritten; refund rows should be append-only.

### 5.5 Provider request policy

Toss payment cancellation supports full and partial cancellation by `paymentKey`, and Toss recommends an idempotency key for safe duplicate prevention.

ATStudio implementation does the following for admin refund execution:

- Persist a refund request before calling Toss.
- Generate a stable `ATS-REFUND-*` idempotency key for each refund request.
- Use the same idempotency key for retry of the same refund request.
- Never change the idempotency key to bypass an unclear provider error.
- Store provider cancel transaction key if returned.
- Mark refund request as `PENDING_PROVIDER_CONFIRMATION` when the provider result is unknown.
- Leave provider re-query/reconciliation of ambiguous refund results as an operator runbook task until a dedicated refund reconciliation workflow is approved.

### 5.6 Refund state model

Implemented table: `payment_refunds`

| Column | Notes |
|---|---|
| `id` | Internal refund request ID |
| `payment_order_id` | Original order |
| `subscription_payment_id` | Original finalized payment |
| `reconciliation_incident_id` | Nullable incident linkage |
| `provider` | `TOSS` |
| `provider_payment_key` | Provider payment key, not a secret |
| `provider_refund_transaction_id` | Provider cancellation transaction key if available |
| `amount` | Refund amount |
| `currency` | `KRW` |
| `status` | `REQUESTED`, `APPROVED`, `PROCESSING`, `SUCCEEDED`, `FAILED`, `PENDING_PROVIDER_CONFIRMATION`, `CANCELLED` |
| `reason_code` | Controlled enum |
| `reason_note` | Operator note |
| `idempotency_key` | Unique per provider refund request |
| `requested_by`, `approved_by`, `executed_by` | Admin user IDs |
| `provider_payload` | Sanitized response metadata only |
| `created_at`, `updated_at`, `approved_at`, `executed_at` | Audit timeline |

Implemented admin APIs:

| API | Purpose | Mutation risk |
|---|---|---|
| `GET /api/admin/payments/refund-preview/{subscriptionPaymentId}` | Show refundable amount and provider readiness | Read-only |
| `GET /api/admin/payments/refunds` | List local refund ledger records | Read-only |
| `GET /api/admin/payments/refunds/{refundId}` | Show one local refund ledger record | Read-only |
| `POST /api/admin/payments/refunds` | Create refund request and idempotency key | Local audit mutation |
| `POST /api/admin/payments/refunds/{refundId}/approve` | Approve refund request | Local audit mutation |
| `POST /api/admin/payments/refunds/{refundId}/execute` | Execute provider cancel/refund with persisted idempotency key | Provider money movement |

The implemented refund APIs do not automatically mutate `user_subscriptions`, `billing_agreements`, or `subscription_payments.payment_status`.

### 5.7 Entitlement correction state model

Implemented table: `payment_entitlement_corrections`

| Column | Notes |
|---|---|
| `id` | Internal entitlement correction ID |
| `payment_refund_id` | Succeeded refund that justifies the correction |
| `payment_order_id` | Original order |
| `subscription_payment_id` | Original finalized payment |
| `user_subscription_id` | Local access row to correct |
| `user_id` | Payment/access owner |
| `provider` | Provider context, currently `TOSS` for executable refund-linked corrections |
| `status` | `REQUESTED`, `APPROVED`, `PROCESSING`, `SUCCEEDED`, `FAILED`, `CANCELLED` |
| `action` | Current value: `SET_SUBSCRIPTION_STATE` |
| `before_*` fields | Snapshot of current subscription plan, cycle, status, expiration, and pending change before execution |
| `target_*` fields | Explicit target plan, cycle, status, and expiration date |
| `clear_pending_change` | Whether pending plan/cycle fields should be cleared |
| `cancel_billing_agreement` | Whether local billing agreement should be marked cancelled |
| `before_billing_agreement_status`, `after_billing_agreement_status` | Local agreement status snapshot/result |
| `requested_by`, `approved_by`, `executed_by` | Admin user IDs |
| `created_at`, `updated_at`, `approved_at`, `executed_at` | Audit timeline |

Implemented admin APIs:

| API | Purpose | Mutation risk |
|---|---|---|
| `POST /api/admin/payments/entitlement-correction-preview` | Show current state and explicit target outcome | Read-only |
| `GET /api/admin/payments/entitlement-corrections` | List local correction ledger records | Read-only |
| `GET /api/admin/payments/entitlement-corrections/{id}` | Show correction ledger detail | Read-only |
| `POST /api/admin/payments/entitlement-corrections` | Create correction request | Local audit mutation |
| `POST /api/admin/payments/entitlement-corrections/{id}/approve` | Approve correction request | Local workflow mutation |
| `POST /api/admin/payments/entitlement-corrections/{id}/execute` | Apply explicit local subscription state and optional local billing agreement cancellation | Local access mutation |

Execution rules:

- Only `SUCCEEDED` refund records can be used for correction creation.
- The target subscription must be active and must match the user's type.
- `EXPIRED` target status cannot use a future expiration date.
- `ACTIVE` or `CANCELLED` target status cannot use a past expiration date.
- No-op correction targets are rejected.
- Local billing agreement cancellation does not call provider billing-key delete/cancel APIs.
- Unexpected local execution failure rolls back the transaction so the approved correction can be retried after investigation.

## 6. Receipt and Cash Receipt Policy

### 6.1 Receipt categories

| Type | Meaning | ATStudio current status |
|---|---|---|
| Card/payment receipt | Provider evidence for a completed card payment | Stored in `payment_receipts` when the provider returns safe receipt metadata |
| Cash receipt | Korean cash receipt for cash-like transactions | Evidence capture exists in `payment_receipts` when provider metadata is returned; issue/cancel mutation is not implemented for current card-only recurring billing |
| Tax invoice | B2B tax document issued by HomeTax/ASP/manual accounting flow | On hold for current card-only recurring billing |

### 6.2 Current recurring billing method

`TossBillingProvider` currently prepares billing auth with method `CARD`. Therefore the current subscription path is card recurring billing first.

Policy:

- User-facing subscription payment history should eventually expose a provider receipt URL when available.
- Receipt evidence is stored explicitly in `payment_receipts` when Toss returns safe receipt metadata; it is not kept only inside sanitized provider payload.
- A receipt URL is safe only when it is an absolute HTTPS URL, has no embedded credentials, and uses the default HTTPS port or explicit port 443. The rule is provider-neutral and does not impose a Toss-only host allowlist.
- Unsafe new URLs are not retained as actionable receipt URLs. Existing unsafe rows are suppressed by ADMIN response mapping and rendered as a non-clickable reference state rather than rewritten silently.
- If Toss sends customer payment result emails through `customerEmail`, that email is provider notification, not ATStudio's own receipt ledger.

### 6.3 Cash receipt boundary

Toss documentation distinguishes cash receipt handling for Toss-transacted payments and cash receipt API requests.

Policy:

- For current card-only recurring subscription billing, cash receipt issuance is not part of the first implementation.
- If ATStudio later supports account transfer, virtual account, or a separate cash receipt request flow, it must store:
  - `receiptKey`
  - `receiptUrl`
  - issue status
  - request type
  - customer identity type and masked identity
  - cancellation status
- If a cash receipt is issued through a standalone API rather than provider-linked payment cancellation, ATStudio must explicitly cancel or partially cancel the cash receipt when the related payment is refunded.

### 6.4 Receipt evidence model

Implemented table: `payment_receipts`

| Column | Notes |
|---|---|
| `id` | Receipt evidence ID |
| `payment_order_id` | Related order |
| `subscription_payment_id` | Related payment |
| `type` | `PAYMENT_RECEIPT`, `CASH_RECEIPT` |
| `provider` | Provider |
| `provider_payment_key` | Provider payment key |
| `receipt_key` | Cash receipt key if applicable |
| `receipt_url` | Provider or external evidence URL |
| `status` | `ISSUED`, `FAILED`, `CANCELLED`, `PARTIAL_CANCELLED` |
| `issued_at`, `cancelled_at` | Evidence lifecycle |
| `evidence_payload` | Minimal sanitized metadata only |

Current capture behavior:

- Initial subscription charge, upgrade charge, and renewal charge publish a receipt evidence event after the local payment transaction commits.
- The receipt listener stores a `PAYMENT_RECEIPT` row when provider payload includes `receipt.url`.
- It stores a `CASH_RECEIPT` row when provider payload includes a cash receipt key or URL.
- Duplicate receipt type rows for the same payment order are skipped.
- Evidence payload is reduced to payment key, order ID, status, method, amount, timestamp, receipt type, receipt URL, and receipt key. It must not store raw card data, billing key, auth key, customer key, or raw provider payload.

### 6.5 Operation audit ledger

Implemented table: `payment_operation_audit_logs`

Current action coverage:

| Action | Actor | Target | Notes |
|---|---|---|---|
| `RECONCILIATION_INCIDENT_STATUS_UPDATE` | Admin user | `payment_reconciliation_incidents` | Records before/after status, reason code, note, order/provider references. |
| `RECEIPT_EVIDENCE_CREATED` | System (`NULL`) | `payment_receipts` | Records receipt evidence creation for support traceability. |
| `PAYMENT_REFUND_REQUESTED` | Admin user | `payment_refunds` | Records local refund request creation. |
| `PAYMENT_REFUND_APPROVED` | Admin user | `payment_refunds` | Records local refund approval. |
| `PAYMENT_REFUND_PROCESSING` | Admin user | `payment_refunds` | Records provider execution start. |
| `PAYMENT_REFUND_SUCCEEDED` | Admin user | `payment_refunds` | Records successful provider cancel result. |
| `PAYMENT_REFUND_FAILED` | Admin user | `payment_refunds` | Records deterministic provider/local failure. |
| `PAYMENT_REFUND_PENDING_PROVIDER_CONFIRMATION` | Admin user | `payment_refunds` | Records ambiguous provider result requiring manual confirmation. |
| `PAYMENT_ENTITLEMENT_CORRECTION_REQUESTED` | Admin user | `payment_entitlement_corrections` | Records local access correction request creation. |
| `PAYMENT_ENTITLEMENT_CORRECTION_APPROVED` | Admin user | `payment_entitlement_corrections` | Records local access correction approval. |
| `PAYMENT_ENTITLEMENT_CORRECTION_PROCESSING` | Admin user | `payment_entitlement_corrections` | Records local access correction execution start. |
| `PAYMENT_ENTITLEMENT_CORRECTION_SUCCEEDED` | Admin user | `payment_entitlement_corrections` | Records successful local access correction. |
| `PAYMENT_ENTITLEMENT_CORRECTION_FAILED` | Admin user | `payment_entitlement_corrections` | Reserved for failed local correction tracking. |
| `PAYMENT_SETTLEMENT_IMPORTED` | Admin user | `payment_settlements` | Records imported settlement evidence rows. |
| `PAYMENT_SETTLEMENT_RECONCILED` | Admin user | `payment_settlements` | Records generated missing-provider settlement review rows. |
| `PAYMENT_SETTLEMENT_IGNORED` | Admin user | `payment_settlements` | Records operator ignore decisions. |

Future tax invoice request workflows should add explicit action values instead of reusing these actions, but only after a matching B2B invoice, bank-transfer, postpaid, or contract purchase payment scope is approved. Cash receipt issue/cancel actions remain conditional on future cash-like payment support.

## 7. Settlement Policy

### 7.1 Distinguish settlement from payout

ATStudio current subscription payment settlement means PG-to-merchant settlement for ATStudio's own subscription revenue.

It does not mean:

- Creator royalty settlement.
- Seller payout.
- Open-market payout service.

If ATStudio later pays creators or sellers, that is a separate settlement/payout domain with additional contract, tax, and identity requirements.

### 7.2 Settlement source strategy

Settlement data can come from more than one provider-facing source. ATStudio uses a source adapter boundary so the first implementation can start with manually uploaded settlement evidence while keeping a future Toss Settlement API adapter open.

| Source | Status | Policy |
|---|---|---|
| `CSV_MANUAL` | First implementation | Admin uploads a CSV settlement file using the ATStudio template. Excel files should be exported to CSV first. |
| `SYSTEM_RECONCILIATION` | First implementation | System-generated review rows show local finalized payments that lack imported provider settlement evidence for the selected period. |
| `TOSS_API` | Future adapter | Toss Settlement API lookup may be added later without replacing the local settlement ledger, reconciliation rules, or admin UI. |

The provider source is evidence for accounting reconciliation. It is not a subscription entitlement source and must not automatically mutate payment, refund, billing agreement, or subscription state.

### 7.3 Settlement reconciliation

Toss settlement information can be used to compare provider settlement records with ATStudio internal payment records.

Policy:

- `subscription_payments` is the internal sales ledger for subscription charges.
- Toss settlement lookup is the provider settlement evidence.
- A settlement record must not be treated as subscription entitlement source.
- Settlement reconciliation should compare:
  - provider payment key
  - `orderId`
  - gross amount
  - refund amount
  - fee
  - VAT/tax fields if provided
  - settlement base date
  - payout date
  - provider settlement status

### 7.4 Settlement model

Implemented table: `payment_settlements`

| Column | Notes |
|---|---|
| `id` | Internal settlement row |
| `source` | `CSV_MANUAL`, `SYSTEM_RECONCILIATION`, future `TOSS_API` |
| `provider` | Provider |
| `provider_settlement_id` | Provider settlement row identifier if available |
| `provider_payment_key` | Provider payment key |
| `order_id` | Merchant order ID |
| `payment_order_id` | Nullable local order mapping |
| `subscription_payment_id` | Nullable local finalized payment mapping |
| `user_id` | Nullable payment owner mapping |
| `import_batch_key` | Import or generated scan batch identifier |
| `source_file_name`, `source_row_number` | CSV/source traceability |
| `gross_amount` | Original amount |
| `refund_amount` | Refunded amount included in settlement calculation |
| `fee_amount` | Provider fee if available |
| `vat_amount` | VAT/tax amount if available |
| `net_settlement_amount` | Amount expected to be paid to merchant |
| `currency` | 3-letter currency code, default `KRW` |
| `settlement_base_date` | Provider settlement sales/base date |
| `settlement_payout_date` | Expected or actual payout date |
| `provider_status` | Provider status text when available |
| `status` | `IMPORTED`, `MATCHED`, `MISMATCHED`, `LOCAL_PAYMENT_NOT_FOUND`, `PROVIDER_SETTLEMENT_NOT_FOUND`, `IGNORED` |
| `mismatch_reason` | Review reason when reconciliation is not matched |
| `operator_note`, `ignored_by`, `ignored_at` | Operator workflow state |
| `reconciled_at` | Internal reconciliation timestamp |
| `source_payload` | Allowlisted source metadata only |

Detailed implementation design is tracked in [Payment Settlement Import and Reconciliation Design](payment-settlement-import-design.md).

### 7.5 Settlement incident types

Future reconciliation incident types:

- `SETTLEMENT_PROVIDER_NOT_FOUND`
- `SETTLEMENT_LOCAL_PAYMENT_NOT_FOUND`
- `SETTLEMENT_AMOUNT_MISMATCH`
- `SETTLEMENT_FEE_MISMATCH`
- `SETTLEMENT_REFUND_MISMATCH`
- `SETTLEMENT_PAYOUT_DATE_MISMATCH`

## 8. Tax Invoice Policy

### 8.1 Boundary

Tax invoices are not the same as Toss receipt URLs or cash receipts.

Current decision:

- ATStudio's current subscription payment scope is card-only recurring billing.
- Card payments normally produce provider/card receipt evidence, which ATStudio already captures when safe metadata is returned.
- ATStudio does not currently support B2B invoice, bank-transfer, postpaid, or contract purchase payments that would justify a first-class tax invoice request workflow.

Policy:

- ATStudio should not auto-issue electronic tax invoices until tax treatment is confirmed.
- Initial production policy for any future tax invoice scope should be manual issuance through HomeTax or an approved e-tax invoice ASP.
- The application may collect and track tax invoice requests only after a matching B2B invoice, bank-transfer, postpaid, or contract purchase scope is approved.
- Tax invoice automation requires separate REQ/SR and tax operator approval.

### 8.2 Request eligibility candidate

Future tax invoice request can be considered only if one of these product/payment scopes is approved:

- B2B invoice or contract purchase.
- Bank-transfer or postpaid payment.
- Institution purchase that explicitly requires tax invoice operation.
- Accounting-approved exception where card receipt evidence is not sufficient.

If reopened, the request should require:

- Business registration number.
- Company name.
- Representative name if needed by the issuer.
- Business email for invoice delivery.
- Payment/order reference.
- Supply amount, VAT, total amount.
- Requested issue month.

ATStudio already has company certification flows, but tax invoice issuance must not assume those records are complete for tax evidence. A future implementation should explicitly validate required tax invoice fields.

### 8.3 Tax invoice model candidate

Future table candidate after scope approval: `tax_invoice_requests`

| Column | Notes |
|---|---|
| `id` | Request ID |
| `user_id` | Requesting user |
| `payment_order_id` | Related order |
| `subscription_payment_id` | Related payment |
| `business_registration_number` | Sensitive business identifier; mask in UI |
| `company_name` | Business name |
| `representative_name` | Optional depending on issuer process |
| `invoice_email` | Delivery email |
| `supply_amount` | Supply amount |
| `vat_amount` | VAT amount |
| `total_amount` | Total amount |
| `status` | `REQUESTED`, `REVIEWING`, `ISSUED`, `REJECTED`, `CANCELLED`, `CORRECTED` |
| `external_invoice_id` | HomeTax/ASP/external reference |
| `issued_at`, `cancelled_at` | Lifecycle timestamps |
| `admin_note` | Operator note |

### 8.4 Tax invoice cancellation/correction

If a future non-card/B2B payment is refunded after tax invoice issuance:

- The system must not silently hide or delete the original invoice record.
- Accounting must decide whether a cancellation, correction, or separate negative evidence is required.
- ATStudio should create an operations task linked to the refund request.
- Future automation may produce a required action queue rather than directly issuing correction invoices.

## 9. Admin Operation Design

### 9.1 Recommended implementation order

| Phase | Scope | Why first |
|---|---|---|
| P2-A | Admin audit ledger and receipt evidence storage | Implemented. No provider mutation added. |
| P2-B | Refund request workflow without automatic entitlement mutation | Implemented as backend admin APIs with provider cancellation, idempotency, and approval. |
| P2-C | Entitlement correction workflow linked to refund | Implemented as backend admin APIs; money movement and access mutation remain auditable separately. |
| P2-D | Settlement import and settlement reconciliation | Implemented as accounting visibility without touching user access. First implementation is CSV/manual source adapter; Toss Settlement API remains a future adapter. |
| P2-E | Tax invoice request tracking | On hold for current card-only recurring billing. Reopen only for approved B2B invoice, bank-transfer, postpaid, or contract purchase scope. |
| P2-F | Tax invoice automation or ASP integration | Requires tax review and external integration decision. |

### 9.2 API map

The refund, entitlement correction, and settlement APIs below are implemented. Tax invoice entries remain future candidates only after a matching non-card/B2B payment scope is approved.

| API | Purpose | Mutation risk |
|---|---|---|
| `GET /api/admin/payments/refund-preview/{subscriptionPaymentId}` | Show refundable amount and provider readiness | Read-only |
| `GET /api/admin/payments/refunds` | List refund ledger records | Read-only |
| `GET /api/admin/payments/refunds/{id}` | Show refund ledger detail | Read-only |
| `POST /api/admin/payments/refunds` | Create refund request | Local audit mutation |
| `POST /api/admin/payments/refunds/{id}/approve` | Approve refund request | Local audit mutation |
| `POST /api/admin/payments/refunds/{id}/execute` | Execute provider refund/cancel with idempotency key | Provider money movement |
| `POST /api/admin/payments/entitlement-correction-preview` | Preview explicit local entitlement correction target | Read-only |
| `GET /api/admin/payments/entitlement-corrections` | List entitlement correction ledger records | Read-only |
| `GET /api/admin/payments/entitlement-corrections/{id}` | Show entitlement correction ledger detail | Read-only |
| `POST /api/admin/payments/entitlement-corrections` | Create entitlement correction request | Local audit mutation |
| `POST /api/admin/payments/entitlement-corrections/{id}/approve` | Approve entitlement correction request | Local workflow mutation |
| `POST /api/admin/payments/entitlement-corrections/{id}/execute` | Apply explicit local subscription correction after approval | Local access mutation |
| `GET /api/admin/payments/receipts` | Admin receipt evidence list | Read-only |
| `GET /api/admin/payments/settlements` | Admin settlement records | Read-only |
| `POST /api/admin/payments/settlements/import` | Import provider settlement CSV evidence | Local accounting mutation |
| `POST /api/admin/payments/settlements/reconcile` | Generate missing-provider settlement evidence review rows | Local accounting mutation |
| `PUT /api/admin/payments/settlements/{id}/ignore` | Mark a settlement row ignored with an operator note | Local workflow mutation |
| `POST /api/tax-invoice-requests` | Future-only user tax invoice evidence request candidate | Not part of current card-only API surface |
| `PUT /api/admin/tax-invoice-requests/{id}/status` | Future-only admin manual issuance status candidate | Not part of current card-only API surface |

### 9.3 Required admin audit fields

Every operation that touches money, evidence, or subscription access must record:

- actor ID
- action type
- target payment/refund/settlement/tax invoice ID
- before status
- after status
- reason code
- note
- linked incident ID if applicable
- request timestamp
- provider result code/message if applicable

## 10. User and Admin UX Policy

### User-facing

- Show receipt link only after payment is finalized.
- Show refund status only after an admin refund request exists.
- Do not expose internal provider payload or admin notes.
- If a refund changes access, show the access end date and reason in plain language.
- Future tax invoice requests, if approved, should show workflow status, not raw accounting internals.

### Admin-facing

- Show financial action previews before execution.
- Separate "request", "approve", "execute provider refund", and "correct entitlement" actions.
- Disable provider execution if required identifiers are missing.
- Use explicit status labels such as `Pending provider confirmation` for ambiguous results.
- Never offer an automatic "refund and fix everything" button without showing the exact local and provider actions.
- Keep the admin screen boundary clear: general local subscription editing belongs to `사용자 구독 관리`; payment-backed refund, audit, incident, and refund-linked entitlement correction belongs to `결제 운영`.
- Require typed confirmation before provider refund execution or local entitlement correction execution.

## 11. Security and Privacy Boundary

Never store or display:

- raw billing key
- Toss secret key
- raw card number, CVC, expiry
- raw `authKey`
- raw provider payload

Allowed support-safe fields:

- `orderId`
- provider
- payment purpose
- local status
- provider status
- amount
- currency
- payment key
- cancel transaction key
- masked method
- receipt URL
- sanitized failure code/message

Business registration numbers, representative names, and invoice emails would be operationally necessary for a future tax invoice workflow but must be treated as sensitive business data:

- mask in list views
- restrict to admin/accounting roles
- avoid logs
- do not include in public screenshots

## 12. Acceptance Checklist for Remaining Implementation REQs

Before implementing remaining payment operation features, confirm:

- [x] Which admin roles can request, approve, and execute refunds. Current backend boundary is `ADMIN`.
- [ ] Whether two-person approval is required for refunds above a threshold.
- [x] Whether full refund should automatically create an entitlement-correction task. Current decision: no automatic creation; admin creates explicit target-state correction after support approval.
- [ ] Whether receipt URLs are available in the Toss billing charge response for the active API version.
- [ ] Whether a future cash-like payment method or standalone cash receipt request flow requires cash receipt issue/cancel automation.
- [ ] Whether ATStudio has approved B2B invoice, bank-transfer, postpaid, or contract purchase scope that justifies tax invoice request tracking.
- [ ] Which business users can request tax invoices if that scope is approved.
- [ ] Whether future tax invoice issuance is manual HomeTax, ASP, or automated API integration.
- [x] Whether settlement data is imported from Toss API or uploaded manually by operations. Current decision: CSV/manual upload first; Toss Settlement API adapter remains future.
- [ ] Which accounting system, if any, must receive exported data.

## 13. Current Decision Record

| ID | Decision | Status |
|---|---|---|
| PAYOPS-D01 | User cancellation is not refund; it stops next renewal and preserves paid access. | Accepted |
| PAYOPS-D02 | Refund implementation must be admin-only, audited, and idempotent. | Accepted |
| PAYOPS-D03 | Provider refund and entitlement correction are separate audited operations. | Accepted |
| PAYOPS-D04 | Receipt evidence must use explicit fields/tables, not only sanitized provider payload. | Accepted |
| PAYOPS-D05 | Current card-only recurring billing does not require cash receipt automation in the first implementation. | Accepted |
| PAYOPS-D06 | Settlement means PG-to-ATStudio merchant settlement, not creator payout. | Accepted |
| PAYOPS-D07 | Tax invoice workflow is on hold for current card-only recurring billing. If B2B invoice, bank-transfer, postpaid, or contract purchase scope is approved later, issuance starts as manual HomeTax/ASP-backed operations tracking until tax review approves automation. | Accepted |
| PAYOPS-D08 | Refund ledger/provider cancel is implemented as admin backend APIs and first-class admin UI while keeping request, approval, and provider execution separate. | Accepted |
| PAYOPS-D09 | Refund-linked entitlement correction is implemented as an explicit target-state admin backend workflow and first-class admin UI while keeping it separate from provider refund. | Accepted |
| PAYOPS-D10 | Settlement import starts with CSV/manual source adapter while preserving a future Toss Settlement API adapter path. | Accepted and implemented |

## Related Documents

- [SR-93 Toss Recurring Payment Production Readiness](../SR/SR-93.md)
- [Payment Operations Runbook](payment-operations-runbook.md)
- [Payment Integration Design](payment-integration-design.md)
- [API Specification](api-spec.md)
- [DB Schema](db-schema.md)
- [Payment Final Acceptance Checklist](../../deliverables/user/PAYMENT-FINAL-ACCEPTANCE-CHECKLIST-20260525.md)
