---
version: 1.0
last_updated: 2026-05-30
project: ATS
owner: docops
category: guide
status: stable
dependencies:
  - path: index.md
    reason: Payment documentation navigation
  - path: ../design/payment-integration-design.md
    reason: Current payment implementation source
  - path: ../design/payment-refund-receipt-settlement-policy.md
    reason: Current payment operations policy source
---

# Payment Feature Inventory

> Purpose: List every payment-related capability added to ATStudio so product, engineering, operations, and client-facing documentation can share the same understanding.

---

## 1. Status Legend

| Status | Meaning |
| :-- | :-- |
| Implemented | Exists in current backend/frontend or documented operation workflow. |
| Implemented with constraint | Exists, but only for the current approved scope. |
| Planned | Recognized as a future feature candidate. |
| On hold | Intentionally deferred unless the payment policy changes. |

## 2. User Subscription Payment

| Capability | Status | Notes |
| :-- | :-- | :-- |
| Toss recurring subscription checkout | Implemented | User-facing subscription purchase uses Toss billing-key flow through `/subscriptions/checkout`. |
| Billing-key registration | Implemented | Toss `authKey` and `customerKey` are exchanged server-side for a billing key. |
| Immediate first charge | Implemented | A new subscription charges the first period immediately after billing-key registration succeeds. |
| Billing method re-registration | Implemented | Existing subscribers can register a new automatic payment method without changing the current plan during registration. |
| Mock payment provider separation | Implemented with constraint | Mock provider remains for legacy/non-subscription test paths. User-facing subscription checkout does not fall back to mock payment. |
| One-time subscription payment | Implemented with constraint | Legacy prepare/confirm paths are blocked for subscription `SUBSCRIBE` and `UPGRADE` purposes. |
| Direct subscription creation endpoint | Implemented with constraint | `POST /api/user-subscriptions` is blocked with `SUBSCRIPTION_CHECKOUT_REQUIRED`. |

## 3. Plan Change and Cancellation

| Capability | Status | Notes |
| :-- | :-- | :-- |
| Upgrade immediate charge | Implemented | Upgrade charges the remaining-period price difference through the active billing agreement. |
| Upgrade immediate access | Implemented | After charge success, the higher plan is applied immediately. |
| Upgrade with billing-cycle change | Implemented | The higher plan applies immediately; a different next billing cycle is stored as a pending renewal change. |
| Downgrade scheduling | Implemented | Lower-tier changes are scheduled for the next renewal without immediate payment. |
| Billing-cycle-only scheduling | Implemented | Cycle-only changes are scheduled for the next renewal. |
| Pending change cancellation | Implemented | Selecting the current plan/cycle clears pending plan or cycle changes. |
| Subscription cancellation | Implemented | User cancellation stops future renewal while preserving access until `expiresAt`. |
| Cancellation reactivation | Implemented | A cancelled grace-period subscriber can reactivate before expiration if the billing agreement is reusable. |
| Automatic renewal cancellation as a separate UX action | Retired | The user-facing action is "cancel subscription"; it means stop future renewal while preserving paid access. |

## 4. Renewal, Failure, and Recovery

| Capability | Status | Notes |
| :-- | :-- | :-- |
| Daily recurring renewal scheduler | Implemented | Runs at 00:00 server time through `SubscriptionScheduler.processRecurringRenewals()`. |
| Stale payment order expiration | Implemented | Runs at 00:10 and expires `READY` or `IN_PROGRESS` payment orders past `expiresAt`. |
| Subscription expiration scheduler | Implemented | Runs at 00:30 after renewal/grace handling. |
| Renewal failure grace period | Implemented | 3-day grace period. |
| Renewal retry policy | Implemented | Up to 3 retry attempts. |
| Renewal failure email notice | Implemented | Email notification is attempted for renewal failure and final failure guidance. |
| Removed billing-key recovery | Implemented | Provider errors indicating removed/invalid billing key mark the local agreement expired and require re-registration. |

## 5. Payment Ledgers and Evidence

| Capability | Status | Notes |
| :-- | :-- | :-- |
| Payment order ledger | Implemented | `payment_orders` records prepare, billing auth, initial charge, upgrade, renewal, and legacy states. |
| Subscription payment ledger | Implemented | `subscription_payments` records finalized subscription charges. |
| Billing agreement ledger | Implemented | `billing_agreements` stores provider customer key, encrypted billing key, fingerprint, masked method, failure count, and next billing date. |
| Receipt evidence ledger | Implemented | `payment_receipts` stores safe receipt/cash-receipt evidence returned by successful provider charges. |
| Operation audit log | Implemented | `payment_operation_audit_logs` records admin and system payment-operation transitions. |
| Raw billing key storage | Not allowed | Billing key is encrypted at rest and never returned to frontend/admin APIs. |
| Raw provider payload exposure | Not allowed | Stored/returned payload is sanitized. Raw card numbers, billing keys, auth keys, customer keys, and Toss secret keys are excluded. |

## 6. Admin Payment Operations

| Capability | Status | Notes |
| :-- | :-- | :-- |
| Payment order list | Implemented | `/admin/payments` orders tab and `GET /api/admin/payments/orders`. |
| Billing agreement list | Implemented | Agreements tab and `GET /api/admin/payments/billing-agreements`. |
| Subscription payment list | Implemented | Payments tab and `GET /api/admin/payments/subscription-payments`. |
| Reconciliation incident list | Implemented | Incidents tab and `GET /api/admin/payments/reconciliation-incidents`. |
| Incident status workflow | Implemented | Admins can update incident status to operationally track investigation. |
| Receipt list | Implemented | Receipts tab and `GET /api/admin/payments/receipts`. |
| Operation audit list | Implemented | Audit tab and `GET /api/admin/payments/operation-audit-logs`. |
| Refund workflow UI | Implemented | Refund tab supports preview, request, approve, and execute. |
| Entitlement correction UI | Implemented | Correction tab supports preview, request, approve, and execute after refund success. |
| Settlement import/reconciliation UI | Implemented | Settlement tab supports CSV import, missing-provider scan, filtering, and ignore workflow. |

## 7. Reconciliation and Settlement

| Capability | Status | Notes |
| :-- | :-- | :-- |
| Local ledger reconciliation | Implemented | Admin/API and scheduled process can compare local payment ledger consistency. |
| Toss provider reconciliation | Implemented | Recent Toss billing payment orders can be checked against provider lookup by order ID when configured. |
| Persistent reconciliation incidents | Implemented | `payment_reconciliation_incidents` stores deduped incident state, severity, occurrence count, and operator workflow. |
| Scheduled reconciliation | Implemented | Runs daily at 01:00 server time. |
| Optional operator notification | Implemented with constraint | Sends notification only when explicitly configured. |
| Settlement CSV import | Implemented | Admin uploads CSV settlement evidence into `payment_settlements`. Excel sources must be exported to CSV first. |
| Settlement mismatch detection | Implemented | Compares gross, refund, fee, VAT, and net settlement amounts against local payment/refund ledgers. |
| Missing provider settlement scan | Implemented | Generates `PROVIDER_SETTLEMENT_NOT_FOUND` review rows for finalized local payments without imported provider evidence. |
| Settlement ignore workflow | Implemented | Admin can ignore a row with an operator note. |
| Toss Settlement API adapter | Planned | Ledger and UI are designed to accept a future adapter without replacing the current model. |

## 8. Refund and Entitlement Correction

| Capability | Status | Notes |
| :-- | :-- | :-- |
| Refund preview | Implemented | Admin can check refundable amount before creating a refund request. |
| Refund request ledger | Implemented | `payment_refunds` stores request, reason, target payment, amount, actor, and status. |
| Refund approval | Implemented | Approval is separate from request creation. |
| Toss cancel API execution | Implemented | Execute step calls provider cancel API with persisted idempotency key. |
| Provider uncertainty handling | Implemented | Unknown/failed provider responses are recorded in the refund ledger for operator follow-up. |
| Automatic entitlement rollback after refund | Not implemented by design | Refund changes payment state only. Access correction is separate and explicit. |
| Entitlement correction preview | Implemented | Admin previews target subscription state after a succeeded refund. |
| Entitlement correction request/approval/execution | Implemented | `payment_entitlement_corrections` records explicit local subscription state correction workflow. |
| Provider billing-key deletion during entitlement correction | Not implemented by design | Entitlement correction can optionally cancel local billing agreement state only. |

## 9. Deferred or Separate Features

| Capability | Status | Notes |
| :-- | :-- | :-- |
| Cash receipt issue/cancel automation | On hold | Current recurring subscription policy is card-only. Receipt evidence capture exists, but cash receipt mutation is not in scope. |
| Tax invoice request/admin workflow | On hold | Current payment scope is card-only recurring subscription. Reopen only when B2B invoice, bank-transfer, postpaid, or contract purchase scope is approved. |
| Toss webhook handling | Planned | Optional auxiliary hardening. It must not become the sole source of truth for subscription access. |
| Multi-PG expansion | Planned | Provider abstractions exist, but current user-facing provider is Toss billing. |
| Multi-server scheduler lock | On hold | Current deployment assumption is single server. Revisit only if deployment topology changes. |
| Creator royalty settlement or seller payout | Out of scope | Current settlement work is PG-to-ATStudio payment settlement evidence only. |

## Related Documents

### Required References

- [Payment Documentation Pack](index.md): Reading order and maintenance rule.
- [Payment Integration Design](../design/payment-integration-design.md): Detailed current payment design.
- [Payment Refund, Receipt, Settlement, and Tax Invoice Policy](../design/payment-refund-receipt-settlement-policy.md): Detailed operations policy.

### Reference Documents

- [System Overview](system-overview.md): Technical structure for this feature inventory.
- [Known Limits and Next Steps](known-limits-and-next-steps.md): Future feature queue.
