---
version: 1.0
last_updated: 2026-05-30
project: ATS
owner: qa
category: guide
status: stable
dependencies:
  - path: index.md
    reason: Payment documentation navigation
  - path: user-flows.md
    reason: User-facing flow definitions
  - path: admin-operations-guide.md
    reason: Admin flow definitions
---

# Payment Acceptance Test Checklist

> Purpose: Provide the current payment-system acceptance checklist for local verification, staging rehearsal, and client-adjacent review.

---

## 1. Test Preparation

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Backend and frontend are running against the intended local or staging environment. | User can open `/subscriptions` and admin can open `/admin/payments`. | [ ] |
| Toss test client key and secret key are configured for recurring billing. | Checkout opens Toss billing auth instead of provider-not-configured error. | [ ] |
| Billing-key encryption secret is configured. | Billing-key confirmation does not fail due to missing encryption secret. | [ ] |
| Test user has no active subscription for new-subscription tests. | New checkout starts from a clean state. | [ ] |
| Admin account can access `/admin/payments`. | Payment operations tabs are visible. | [ ] |

## 2. New Subscription

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Select a plan and billing cycle on `/subscriptions`. | User is routed to `/subscriptions/checkout`. | [ ] |
| Start Toss card registration. | Toss billing auth opens and returns to success/fail callback. | [ ] |
| Complete Toss test billing auth. | Backend confirms billing key and charges first period. | [ ] |
| Return to `/subscriptions/manage`. | Current plan, cycle, start date, expiration, payment method, and next billing date are visible. | [ ] |
| Inspect admin payment orders and subscription payments. | `payment_orders` and `subscription_payments` show the completed charge. | [ ] |

## 3. Billing Method Re-registration

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Use an active subscription with missing/expired/unusable billing agreement. | Manage page shows payment method re-registration guidance. | [ ] |
| Click payment method registration. | Checkout opens with `purpose=BILLING_AGREEMENT`. | [ ] |
| Complete billing auth. | Billing agreement becomes usable without charging the card in this step. | [ ] |
| Return to manage page. | User can retry upgrade or wait for renewal using the new billing method. | [ ] |

## 4. Upgrade

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Select a higher-tier plan in manage page. | Preview shows upgrade and immediate charge amount. | [ ] |
| Confirm upgrade with reusable billing method. | Remaining-period difference is charged through the billing agreement. | [ ] |
| Verify active plan. | Higher-tier plan is active immediately. | [ ] |
| Upgrade while selecting a different billing cycle. | Higher plan applies now; next renewal cycle is shown as pending. | [ ] |
| Inspect admin payments. | Upgrade order/payment and receipt evidence appear without raw sensitive fields. | [ ] |

## 5. Downgrade and Billing Cycle Change

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Select a lower-tier plan. | Preview shows scheduled change and no immediate payment. | [ ] |
| Confirm downgrade. | Current plan remains active until expiration; pending plan is shown. | [ ] |
| Select a different cycle for the same plan. | Cycle-only change is scheduled for next renewal. | [ ] |
| Select current plan/cycle while pending change exists. | Pending change is cleared. | [ ] |

## 6. Cancellation and Reactivation

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Cancel subscription. | Status becomes cancelled, future renewal stops, access remains until `expiresAt`. | [ ] |
| Confirm subscriber-only access before expiration. | Cancelled grace-period subscriber can still use subscription-gated features. | [ ] |
| Reactivate before expiration. | Status returns to active and renewal can continue if billing method is reusable. | [ ] |
| Let subscription pass expiration. | Subscription becomes expired and subscriber-only access is blocked. | [ ] |

## 7. Renewal and Failure

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Trigger or wait for due renewal. | Scheduler charges through billing agreement and extends subscription on success. | [ ] |
| Simulate provider failure. | Order records failure; failure count increases; retry is scheduled within grace period. | [ ] |
| Simulate repeated failure or grace expiration. | Billing agreement is suspended and subscription expires after grace as appropriate. | [ ] |
| Confirm failure email behavior. | Email attempt occurs with safe guidance, or failure is logged without exposing secrets. | [ ] |

## 8. Admin Payment Operations

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Open orders tab. | Payment orders are paginated and support-safe. | [ ] |
| Open automatic payment tab. | Billing agreements show masked method only. | [ ] |
| Open payments tab. | Finalized charges are visible and refund preview can be started. | [ ] |
| Open incidents tab. | Reconciliation incidents can be listed and status can be updated. | [ ] |
| Open receipts tab. | Receipt evidence is visible without raw provider payload. | [ ] |
| Open audit tab. | Operation audit events appear for incident, receipt, refund, correction, and settlement actions. | [ ] |
| Import settlement CSV. | `payment_settlements` rows are created and classified without changing payment/subscription/provider state. | [ ] |
| Run settlement missing-provider scan. | Missing settlement evidence rows are generated for selected period. | [ ] |
| Ignore a settlement row. | Row becomes `IGNORED` with operator note. | [ ] |

## 9. Refund and Entitlement Correction

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Preview a refundable successful payment. | Refundable amount and reason are shown. | [ ] |
| Create refund request. | `payment_refunds` row is created; provider is not called yet. | [ ] |
| Approve refund request. | Status moves to approved. | [ ] |
| Execute refund with required confirmation text. | Toss cancel/refund is called with persisted idempotency key and result is recorded. | [ ] |
| Confirm subscription access after refund. | Access is unchanged until entitlement correction is executed. | [ ] |
| Preview entitlement correction from succeeded refund. | Target local subscription state is shown. | [ ] |
| Execute entitlement correction with required confirmation text. | Local subscription state changes and audit log is recorded. | [ ] |

## 10. Security and Data Boundary

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| Inspect user checkout/manage screens. | Raw billing key, `authKey`, `customerKey`, Toss secret, and raw card number are not visible. | [ ] |
| Inspect admin payment screens. | Only support-safe identifiers and masked payment method appear. | [ ] |
| Inspect server logs around payment flow. | Secrets and raw card numbers are not logged. | [ ] |
| Inspect settlement import payload behavior. | Stored source payload contains allowlisted support-safe fields only. | [ ] |

## 11. Final Acceptance Gate

| Check | Expected Result | Done |
| :-- | :-- | :-- |
| User subscription flows passed. | New subscription, re-registration, upgrade, downgrade, cancel, reactivate, and renewal scenarios are accepted. | [ ] |
| Admin operation flows passed. | Incidents, receipts, audits, refund, correction, and settlement operations are accepted. | [ ] |
| Deferred scope is understood. | Tax invoice workflow is on hold under the current card-only recurring subscription scope. Toss Settlement API adapter, webhook, multi-PG, and cash receipt mutation are not treated as current defects. | [ ] |

## Related Documents

### Required References

- [User Flows](user-flows.md): User-facing flow definitions.
- [Admin Operations Guide](admin-operations-guide.md): Admin operation flow definitions.

### Reference Documents

- [Original Final Acceptance Checklist](../../deliverables/user/PAYMENT-FINAL-ACCEPTANCE-CHECKLIST-20260525.md): Historical acceptance source.
- [Known Limits and Next Steps](known-limits-and-next-steps.md): Deferred scope list.
