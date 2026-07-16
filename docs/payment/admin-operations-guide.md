---
version: 1.2
last_updated: 2026-07-15
project: ATS
owner: docops
category: guide
status: stable
dependencies:
  - path: index.md
    reason: Payment documentation navigation
  - path: system-overview.md
    reason: API and table structure
  - path: ../design/payment-operations-runbook.md
    reason: Operational procedure source
---

# Admin Payment Operations Guide

> Purpose: Explain how operators should use the `/admin/payments` payment operations screen and where it differs from general user subscription management.

---

## 1. Screen Boundary

Use `/admin/payments` for payment evidence and payment-operation workflows.

Use `/admin/user-subscriptions` for ordinary subscription state management that is not tied to a specific payment operation.

| Need | Use |
| :-- | :-- |
| Review payment orders, charges, billing agreements, provider issues, refund, correction, settlement | `/admin/payments` |
| Manually adjust a user's subscription status, cycle, or expiration without refund evidence | `/admin/user-subscriptions` |
| Explain a user-facing subscription plan list | `/admin/subscriptions` |

## 2. Shared Safety Rules

Operators must treat admin payment operations as audit-sensitive.

- Do not use settlement or reconciliation rows to mutate subscription access.
- Do not run refund execution unless the refund request is approved and the target payment is confirmed.
- Do not run entitlement correction until provider refund has succeeded.
- Do not paste raw billing keys, raw card numbers, Toss secret keys, `authKey`, or `customerKey` into notes.
- Do not paste an exact provider payment key into Incident, refund, settlement, or audit notes. Use the masked/reference fields already shown by the screen.
- Use notes to record support-safe evidence and decision reasons.

## 3. Orders Tab

Purpose:

- Inspect local `payment_orders`.
- Confirm order purpose, status, amount, provider, and linked subscription context.

Use when:

- A checkout callback looked wrong.
- User claims payment did not complete.
- An upgrade or renewal charge needs tracing.

Avoid:

- Treating an `IN_PROGRESS` order as payment success.
- Reusing old Toss redirect URLs from stale orders.

## 4. Automatic Payment Tab

Purpose:

- Inspect `billing_agreements`.
- See provider, status, masked method, next billing date, failure count, and cancellation state.

Use when:

- A user cannot upgrade because billing method is missing or expired.
- A removed billing key error was detected.
- Renewal is not charging as expected.

Expected sensitive-data boundary:

- Masked payment method may appear.
- Raw billing key must not appear.

## 5. Payments Tab

Purpose:

- Inspect finalized `subscription_payments`.
- Start refund preview from a successful payment row.

Use when:

- Support needs to find the exact charge to refund.
- Settlement review requires local finalized charge evidence.

Rule:

- Refundable payment must be `DONE`, provider-backed, linked to a payment order, and have provider payment key metadata.

## 6. Incidents Tab

Purpose:

- Review persisted local/provider reconciliation incidents.
- Update incident status through operational workflow.

Typical statuses:

- `OPEN`
- `ACKNOWLEDGED`
- `RESOLVED`
- `IGNORED`

Use when:

- Scheduled reconciliation found a mismatch.
- On-demand reconciliation finds different local/provider state.
- A withdrawn user's Provider billing-key deletion failed and the daily cleanup retry is pending.

Rule:

- Incident status changes do not change payments, subscriptions, billing agreements, refunds, or provider state.
- Withdrawal cleanup success resolves its matching agreement-scoped Incident automatically. Operators must not restore renewal eligibility or create a refund merely to close this Incident.
- An order in `PROVIDER_SUCCEEDED` is finalize-only. Do not start a replacement charge; keep the Incident open until local finalization converges or an approved disposition is recorded.

## 7. Receipts Tab

Purpose:

- Review safe receipt evidence from successful provider charges.

Use when:

- Support needs receipt URL/key evidence.
- Operators need to confirm whether receipt metadata was captured.

Current scope:

- Receipt evidence capture exists.
- Only normalized absolute HTTPS receipt links without embedded credentials or non-standard ports are clickable.
- A retained unsafe URL appears as a non-clickable support reference or review-needed state. Do not copy it into the address bar or an incident note.
- Cash receipt issue/cancel mutation is not implemented for the current card-only recurring scope.

## 8. Audit Log Tab

Purpose:

- Review append-only payment operation audit events.

Use when:

- Need to know who changed an incident status.
- Need refund/correction/settlement workflow trace.
- Need to confirm receipt evidence creation was logged.

Rule:

- Audit logs are evidence, not workflow controls.

## 9. Settlement Tab

Purpose:

- Import settlement CSV evidence.
- Compare provider settlement evidence with local payment/refund ledgers.
- Generate missing-provider settlement review rows.
- Ignore reviewed rows with a note.

Current source adapters:

| Source | Status |
| :-- | :-- |
| `CSV_MANUAL` | Implemented |
| `SYSTEM_RECONCILIATION` | Implemented |
| `TOSS_API` | Future adapter |

CSV import checks:

- Required fields include provider, order ID, gross amount, net settlement amount, and settlement base date.
- Amount values must be non-negative.
- Excel sources must be exported to CSV before import.

Settlement statuses:

| Status | Meaning |
| :-- | :-- |
| `MATCHED` | Local payment/refund data aligns with settlement evidence. |
| `MISMATCHED` | Local record exists but amount/refund/fee/VAT/net comparison needs review. |
| `LOCAL_PAYMENT_NOT_FOUND` | Provider settlement evidence has no matching local payment. |
| `PROVIDER_SETTLEMENT_NOT_FOUND` | Local finalized payment has no imported provider settlement evidence for the selected period. |
| `IGNORED` | Operator reviewed and intentionally ignored the row. |

Rule:

- Settlement operations do not change subscriptions, billing agreements, payment status, refund status, or provider state.

## 10. Refund Tab

Purpose:

- Preview refundability.
- Create refund request.
- Approve refund request.
- Execute provider cancel/refund through Toss.

Workflow:

1. Enter subscription payment ID and preview.
2. Confirm refundable amount.
3. Create refund request with reason code and note.
4. Approve request.
5. Execute refund with the required confirmation text.
6. Review result status.

Important statuses:

- `REQUESTED`
- `APPROVED`
- `PROCESSING`
- `SUCCEEDED`
- `FAILED`
- `PENDING_PROVIDER_CONFIRMATION`

Rule:

- Refund execution calls provider cancel/refund and records the result. It does not automatically modify subscription access.
- A fresh `PROCESSING` refund is already owned by a claim. A stale or ambiguous row must retain the same refund ID and idempotency key; do not create a replacement request to bypass it.

## 11. Entitlement Correction Tab

Purpose:

- Apply explicit local subscription state after a succeeded refund.

Workflow:

1. Start from a succeeded refund.
2. Preview target subscription, billing cycle, status, expiration, and pending-change behavior.
3. Create correction request.
4. Approve correction request.
5. Execute correction with the required confirmation text.

Rule:

- Entitlement correction is separate from refund.
- Provider billing-key delete is not called by entitlement correction.
- Local billing agreement cancellation can be selected when the support-approved outcome requires it.

## 12. Suggested Support Triage

| User Report | First Check | Next Check |
| :-- | :-- | :-- |
| "I paid but subscription did not activate." | Orders tab | Payments tab, incidents tab |
| "I cannot upgrade." | Automatic payment tab | Manage page billing method re-registration flow |
| "I was charged twice." | Payments tab | Refund preview, orders tab |
| "I cancelled but still have access." | User subscription status | This is expected until `expiresAt` |
| "Payment failed during renewal." | Automatic payment tab | Orders tab, renewal failure email/logs |
| "A renewal retry looks duplicated." | Orders tab; compare billing period and command/order identity | Payments tab; confirm only one finalized payment exists |
| "Provider and local numbers do not match." | Incidents tab | Settlement tab, runbook |
| "A refund is stuck in progress." | Refund tab; keep the same refund row | Incident/audit evidence; do not submit a replacement refund |
| "I withdrew, but automatic payment may still be active." | Automatic payment tab | Incidents tab; verify local `CANCELLED`, deleted user, and cleanup retry state |

## Related Documents

### Required References

- [System Overview](system-overview.md): API and ledger structure.
- [Payment Operations Runbook](../design/payment-operations-runbook.md): Production response procedures.

### Reference Documents

- [Acceptance Test Checklist](acceptance-test-checklist.md): Admin operation acceptance checks.
- [Payment Refund, Receipt, Settlement, and Tax Invoice Policy](../design/payment-refund-receipt-settlement-policy.md): Detailed policy.
- [P1 Payment Integrity Closure](../audit/p1-payment-integrity-closure-20260715.md): Technical proof and open production gates.
