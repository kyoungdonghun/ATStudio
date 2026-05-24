---
version: 1.0
last_updated: 2026-05-25
project: ATS
owner: SA
category: design
status: draft
source_req: REQ-20260525-ATS-002
---

# Payment Refund, Receipt, Settlement, and Tax Invoice Policy

> Scope: ATStudio subscription payment operations after recurring billing checkout.
> This document defines operating policy and future implementation boundaries. It does not implement refund, receipt, settlement, tax invoice, or admin mutation APIs.

## 1. Purpose

ATStudio recurring subscription payment is now recurring-first:

- New subscription uses Toss billing auth, then immediately performs the first charge.
- Upgrade charges the remaining-period difference through the active billing agreement.
- Downgrade and billing-cycle-only changes are scheduled for the next renewal.
- Renewal is run by ATStudio scheduler, not by Toss.
- Reconciliation incidents are detected, persisted, and triaged through `/admin/payments`.

The next production concern is financial operations after a charge has already happened. Refund, receipt, settlement, and tax invoice work must be designed before admin mutation APIs are added because these operations can affect customer money, subscription access, tax evidence, and accounting records.

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
| Charge ledger | `payment_orders` records attempts and stores provider transaction ID plus sanitized provider payload. |
| Finalized payment | `subscription_payments` stores successful subscription payment records. |
| Provider lookup | Reconciliation compares recent orders with Toss state by `orderId` when lookup is configured. |
| Admin view | `/admin/payments` lists orders, billing agreements, subscription payments, and reconciliation incidents. |
| Mutation boundary | Admin payment screens are intentionally read-only except incident workflow status/note updates. |
| Refund state | `PaymentStatus` has `REFUND`, but no refund ledger or provider cancel implementation exists. |
| Receipt state | No dedicated receipt URL, cash receipt key, or tax invoice record exists. |
| Settlement state | No settlement import, settlement reconciliation, fee, VAT, or payout-date record exists. |

### Current data gap

`PaymentOrder.pgTransactionId` is used as the provider transaction identifier after Toss billing charge. In Toss billing charge responses, this value may be the `paymentKey`. That is enough for support lookup, but future refund/receipt/settlement features should not rely on parsing a generic transaction field or sanitized JSON payload.

Future implementation should introduce explicit operation ledgers for refund, receipt evidence, settlement, tax invoice requests, and admin actions.

## 4. Policy Principles

| Principle | Policy |
|---|---|
| Provider money movement must be idempotent | Refund/cancel requests must use an idempotency key and persist the request before provider execution. |
| Local subscription access is not changed by provider data alone | Provider lookup, webhook, or refund result must be reconciled with local policy before mutating `user_subscriptions`. |
| Refund is exceptional support operation | Normal user cancellation stops next renewal and preserves paid access until `expiresAt`; it is not the same as refund. |
| Evidence records are append-only first | Refund/receipt/settlement/tax invoice operations need audit records before destructive or financial mutation. |
| Separate financial evidence types | Card receipt, cash receipt, settlement, and tax invoice are different evidence types and must not be collapsed into one status. |
| No raw secrets | No raw billing key, auth key, customer key, Toss secret key, raw card data, or raw provider payload in responses, logs, screenshots, or documents. |
| Tax handling requires external confirmation | Tax invoice automation must not be enabled until business type, VAT treatment, and evidence duplication policy are confirmed. |

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

The provider refund and local entitlement correction must be separate audited steps. A later implementation may offer a guided operation that executes both, but it must still record each step independently.

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

ATStudio future implementation should:

- Persist a refund request before calling Toss.
- Generate idempotency key as `ATS-REFUND-{refundRequestId}` or another stable unique key.
- Use the same idempotency key for retry of the same refund request.
- Never change the idempotency key to bypass an unclear provider error.
- Store provider cancel transaction key if returned.
- Re-query provider payment status after ambiguous timeout or provider failure.
- Mark refund request as `PENDING_PROVIDER_CONFIRMATION` when the provider result is unknown.

### 5.6 Refund state model candidate

Future table candidate: `payment_refunds`

| Column | Notes |
|---|---|
| `id` | Internal refund request ID |
| `payment_order_id` | Original order |
| `subscription_payment_id` | Original finalized payment |
| `reconciliation_incident_id` | Nullable incident linkage |
| `provider` | `TOSS_BILLING` first |
| `provider_payment_key` | Provider payment key, not a secret |
| `provider_cancel_transaction_key` | Provider cancellation transaction key if available |
| `amount` | Refund amount |
| `currency` | `KRW` |
| `status` | `REQUESTED`, `APPROVED`, `PROCESSING`, `SUCCEEDED`, `FAILED`, `PENDING_PROVIDER_CONFIRMATION`, `CANCELLED` |
| `reason_code` | Controlled enum |
| `reason_note` | Operator note |
| `idempotency_key` | Unique per provider refund request |
| `requested_by`, `approved_by`, `executed_by` | Admin user IDs |
| `provider_payload` | Sanitized response metadata only |
| `created_at`, `updated_at`, `approved_at`, `executed_at` | Audit timeline |

## 6. Receipt and Cash Receipt Policy

### 6.1 Receipt categories

| Type | Meaning | ATStudio current status |
|---|---|---|
| Card/payment receipt | Provider evidence for a completed card payment | Not stored in a dedicated field |
| Cash receipt | Korean cash receipt for cash-like transactions | Not primary for current card-only recurring billing |
| Tax invoice | B2B tax document issued by HomeTax/ASP/manual accounting flow | Not implemented |

### 6.2 Current recurring billing method

`TossBillingProvider` currently prepares billing auth with method `CARD`. Therefore the current subscription path is card recurring billing first.

Policy:

- User-facing subscription payment history should eventually expose a provider receipt URL when available.
- The receipt URL should be stored explicitly, not only inside sanitized provider payload.
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

### 6.4 Receipt evidence model candidate

Future table candidate: `payment_receipts`

| Column | Notes |
|---|---|
| `id` | Receipt evidence ID |
| `payment_order_id` | Related order |
| `subscription_payment_id` | Related payment |
| `type` | `CARD_RECEIPT`, `CASH_RECEIPT`, `TAX_INVOICE_REFERENCE` |
| `provider` | Provider |
| `provider_payment_key` | Provider payment key |
| `receipt_key` | Cash receipt key if applicable |
| `receipt_url` | Provider or external evidence URL |
| `status` | `ISSUED`, `IN_PROGRESS`, `FAILED`, `CANCELLED`, `PARTIAL_CANCELLED` |
| `masked_identity` | Masked cash receipt identity if applicable |
| `issued_at`, `cancelled_at` | Evidence lifecycle |
| `provider_payload` | Sanitized metadata only |

## 7. Settlement Policy

### 7.1 Distinguish settlement from payout

ATStudio current subscription payment settlement means PG-to-merchant settlement for ATStudio's own subscription revenue.

It does not mean:

- Creator royalty settlement.
- Seller payout.
- Open-market payout service.

If ATStudio later pays creators or sellers, that is a separate settlement/payout domain with additional contract, tax, and identity requirements.

### 7.2 Settlement reconciliation

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

### 7.3 Settlement model candidate

Future table candidate: `payment_settlements`

| Column | Notes |
|---|---|
| `id` | Internal settlement row |
| `provider` | Provider |
| `provider_payment_key` | Provider payment key |
| `order_id` | Merchant order ID |
| `payment_order_id` | Nullable local order mapping |
| `subscription_payment_id` | Nullable local finalized payment mapping |
| `gross_amount` | Original amount |
| `refund_amount` | Refunded amount included in settlement calculation |
| `fee_amount` | Provider fee if available |
| `vat_amount` | VAT/tax amount if available |
| `net_settlement_amount` | Amount expected to be paid to merchant |
| `settlement_base_date` | Provider settlement sales/base date |
| `settlement_payout_date` | Expected or actual payout date |
| `status` | `IMPORTED`, `MATCHED`, `MISMATCHED`, `IGNORED` |
| `reconciled_at` | Internal reconciliation timestamp |
| `provider_payload` | Sanitized provider settlement metadata |

### 7.4 Settlement incident types

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

Policy:

- ATStudio should not auto-issue electronic tax invoices until tax treatment is confirmed.
- Initial production policy should be manual issuance through HomeTax or an approved e-tax invoice ASP.
- The application may collect and track tax invoice requests, but issuance itself can remain external in the first release.
- Tax invoice automation requires separate REQ/SR and tax operator approval.

### 8.2 Request eligibility candidate

Future tax invoice request can be considered for:

- Business users.
- Corporate/company-certified users.
- Payments where the business requests tax invoice evidence.
- Cases approved by accounting operations.

The request should require:

- Business registration number.
- Company name.
- Representative name if needed by the issuer.
- Business email for invoice delivery.
- Payment/order reference.
- Supply amount, VAT, total amount.
- Requested issue month.

ATStudio already has company certification flows, but tax invoice issuance must not assume those records are complete for tax evidence. A future implementation should explicitly validate required tax invoice fields.

### 8.3 Tax invoice model candidate

Future table candidate: `tax_invoice_requests`

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

If a payment is refunded after tax invoice issuance:

- The system must not silently hide or delete the original invoice record.
- Accounting must decide whether a cancellation, correction, or separate negative evidence is required.
- ATStudio should create an operations task linked to the refund request.
- Future automation may produce a required action queue rather than directly issuing correction invoices.

## 9. Admin Operation Design

### 9.1 Recommended implementation order

| Phase | Scope | Why first |
|---|---|---|
| P2-A | Admin audit ledger and receipt evidence storage | Safest foundation; no provider mutation required. |
| P2-B | Refund request workflow without automatic entitlement mutation | Adds provider cancellation safely with idempotency and approval. |
| P2-C | Entitlement correction workflow linked to refund | Money movement and access mutation remain auditable separately. |
| P2-D | Settlement import and settlement reconciliation | Accounting visibility without touching user access. |
| P2-E | Tax invoice request tracking | Enables manual HomeTax/ASP operation with internal traceability. |
| P2-F | Tax invoice automation or ASP integration | Requires tax review and external integration decision. |

### 9.2 API candidate map

These are future candidates, not implemented endpoints.

| API | Purpose | Mutation risk |
|---|---|---|
| `GET /api/admin/payment-operations/refund-preview/{subscriptionPaymentId}` | Show refundable amount and entitlement impact preview | Read-only |
| `POST /api/admin/payment-operations/refunds` | Create refund request | Local audit mutation |
| `POST /api/admin/payment-operations/refunds/{id}/approve` | Approve refund request | Local audit mutation |
| `POST /api/admin/payment-operations/refunds/{id}/execute` | Execute provider refund/cancel with idempotency key | Provider money movement |
| `POST /api/admin/payment-operations/refunds/{id}/entitlement-correction` | Apply local subscription correction after approval | Local access mutation |
| `GET /api/users/me/payment-receipts` | User receipt list | Read-only |
| `GET /api/admin/payment-operations/settlements` | Admin settlement records | Read-only |
| `POST /api/admin/payment-operations/settlements/import` | Import provider settlement records | Local accounting mutation |
| `POST /api/tax-invoice-requests` | User requests tax invoice evidence | Local request mutation |
| `PUT /api/admin/tax-invoice-requests/{id}/status` | Admin updates manual issuance status | Local workflow mutation |

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
- Tax invoice requests should show workflow status, not raw accounting internals.

### Admin-facing

- Show financial action previews before execution.
- Separate "request", "approve", "execute provider refund", and "correct entitlement" actions.
- Disable provider execution if required identifiers are missing.
- Use explicit status labels such as `Pending provider confirmation` for ambiguous results.
- Never offer an automatic "refund and fix everything" button without showing the exact local and provider actions.

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

Business registration numbers, representative names, and invoice emails are operationally necessary for tax invoice workflows but must be treated as sensitive business data:

- mask in list views
- restrict to admin/accounting roles
- avoid logs
- do not include in public screenshots

## 12. Acceptance Checklist for Future Implementation REQs

Before implementing refund/receipt/settlement/tax invoice features, confirm:

- [ ] Which admin roles can request, approve, and execute refunds.
- [ ] Whether two-person approval is required for refunds above a threshold.
- [ ] Whether full refund should automatically create an entitlement-correction task.
- [ ] Whether receipt URLs are available in the Toss billing charge response for the active API version.
- [ ] Whether cash receipt is needed for current card-only recurring billing.
- [ ] Which business users can request tax invoices.
- [ ] Whether tax invoice issuance is manual HomeTax, ASP, or automated API integration.
- [ ] Whether settlement data is imported from Toss API or uploaded manually by operations.
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
| PAYOPS-D07 | Tax invoice issuance starts as manual HomeTax/ASP-backed operations tracking until tax review approves automation. | Accepted |

## Related Documents

- [SR-93 Toss Recurring Payment Production Readiness](../SR/SR-93.md)
- [Payment Operations Runbook](payment-operations-runbook.md)
- [Payment Integration Design](payment-integration-design.md)
- [API Specification](api-spec.md)
- [DB Schema](db-schema.md)
- [Payment Final Acceptance Checklist](../../deliverables/user/PAYMENT-FINAL-ACCEPTANCE-CHECKLIST-20260525.md)
