---
version: 1.2
last_updated: 2026-07-16
project: ATS
owner: docops
category: guide
status: stable
dependencies:
  - path: index.md
    reason: Payment documentation navigation
  - path: system-overview.md
    reason: Technical structure behind these flows
  - path: ../design/payment-integration-design.md
    reason: Detailed payment flow design
---

# Payment User Flows

> Purpose: Explain how users move through subscription payment, plan change, cancellation, reactivation, account withdrawal, and billing recovery flows.

---

## 1. New Subscription

Entry points:

- `/subscriptions`
- `/subscriptions/checkout?planId={ID}&userType={INDIVIDUAL|BUSINESS}&billingCycle={MONTHLY|YEARLY}&purpose=SUBSCRIBE`

Flow:

1. User chooses an exact plan and billing cycle.
2. Frontend accepts one allowlisted value for every required checkout query
   field. Missing, malformed, unsupported, or duplicate state causes zero
   prepare calls.
3. The initial CTA states both payment-method registration and the immediate
   first charge, then the frontend calls
   `POST /api/payments/billing-agreements/prepare`.
4. Backend creates a `payment_orders` row with purpose `SUBSCRIBE`.
5. Frontend opens Toss billing auth through the Toss Payments SDK.
6. Toss redirects to `/subscriptions/checkout/success` with `authKey`,
   `customerKey`, and order context.
7. Frontend calls `POST /api/payments/billing-agreements/confirm`.
8. Backend exchanges `authKey` for billing key.
9. Backend stores encrypted billing key in `billing_agreements`.
10. Backend immediately charges the first subscription period.
11. Backend creates or updates `user_subscriptions` and records
    `subscription_payments`.
12. Frontend moves the user to `/subscriptions/manage`.

Expected result:

- Subscription is active only after server-side billing-key issue and first charge both succeed.
- A successful charge may create receipt evidence.
- Raw Toss keys and card numbers are not displayed.

## 2. Interrupted Card Registration

Entry point:

- `/subscriptions/checkout`

Flow:

1. User starts card registration.
2. User leaves the screen, closes browser, or fails Toss auth.
3. Existing `payment_orders` may stay `READY` or `IN_PROGRESS` until expiration.
4. User can start a new checkout attempt.
5. Stale orders are expired by the scheduled order expiration job.

Prepare failure is shown as terminal `ERROR` with bounded copy and a retry. A
fail callback never displays raw or blank Provider query text.

Expected result:

- Stale callback URLs are not reused.
- The user can retry from the subscription page.
- Interrupted registration does not grant subscription access.

## 3. Billing Method Re-registration

Entry point:

- `/subscriptions/manage`
- `/subscriptions/checkout?planId={ID}&userType={INDIVIDUAL|BUSINESS}&billingCycle={MONTHLY|YEARLY}&purpose=BILLING_AGREEMENT`

Flow:

1. Active or cancelled grace-period subscriber has no reusable billing agreement.
2. Manage page shows payment method re-registration guidance.
3. Frontend routes to checkout with `purpose=BILLING_AGREEMENT`.
4. Backend creates a zero-amount `payment_orders` row.
5. Toss billing auth runs and returns to the checkout callback.
6. Backend stores a new encrypted billing key.
7. Backend does not charge the card during re-registration.
8. User returns to subscription management and can retry the intended plan change if needed.

Expected result:

- Current subscription period and plan remain unchanged during re-registration.
- The next renewal or future upgrade can use the new billing key.

## 4. Upgrade

Entry point:

- `/subscriptions/manage`

Flow:

1. User selects a higher-tier plan.
2. Frontend shows a change preview.
3. User confirms the change.
4. Backend calculates the remaining-period price difference using the current billing cycle.
5. Backend charges the difference through the active billing agreement.
6. If charge succeeds, backend applies the higher plan immediately.
7. If the requested billing cycle is different from the current cycle, backend schedules that cycle for the next renewal.

Expected result:

- Higher plan access starts immediately after successful charge.
- Current expiration date remains unchanged.
- Next billing date remains the current period end.
- A different next billing cycle is shown as pending and applies only on renewal.

## 5. Downgrade

Entry point:

- `/subscriptions/manage`

Flow:

1. User selects a lower-tier plan.
2. Frontend shows a scheduled-change preview.
3. User confirms the change.
4. Backend stores `pending_subscription` and `pending_billing_cycle`.
5. No immediate provider charge occurs.
6. Renewal applies the pending plan and cycle if the renewal charge succeeds.

Expected result:

- Current paid access remains until the current period end.
- Lower plan starts at the next renewal.
- No negative or refund amount is sent to Toss.

## 6. Billing Cycle Change

Entry point:

- `/subscriptions/manage`

Flow:

1. User keeps the same plan but changes monthly/yearly cycle.
2. Backend stores the requested cycle as pending.
3. No immediate provider charge occurs.
4. Renewal charges the pending cycle price and applies it if successful.

Expected result:

- The current period is not recalculated mid-period.
- Billing-cycle fairness is based on the next renewal boundary.

## 7. Pending Change Cancellation

Entry point:

- `/subscriptions/manage`

Flow:

1. User has a pending downgrade or billing-cycle change.
2. User selects the current plan/cycle state.
3. Backend clears the pending change.

Expected result:

- Current plan and billing cycle continue.
- No provider charge occurs.

## 8. Subscription Cancellation

Entry point:

- `/subscriptions/manage`

Flow:

1. User clicks cancel subscription.
2. Backend marks the user subscription `CANCELLED`.
3. Backend cancels the local billing agreement state for future renewal.
4. Paid access remains available until `expiresAt`.

Expected result:

- Future automatic renewal stops.
- Subscriber-only access remains until the already-paid period expires.
- The user can reactivate before expiration when billing agreement state is reusable.

## 9. Cancellation Reactivation

Entry point:

- `/subscriptions/manage`

Flow:

1. User has `CANCELLED` subscription with `expiresAt` not passed.
2. User chooses to keep/reactivate subscription.
3. Frontend shows a confirmation with the next billing date and amount. A
   cancelled Billing Agreement uses the Subscription `expiresAt`; an already
   active agreement uses its retained canonical `nextBillingAt`. Missing
   canonical date input keeps reactivation disabled.
4. Canceling the confirmation makes zero reactivation calls; approving it sends
   one request and disables repeat submission while in flight.
5. Backend returns status to `ACTIVE`.
6. If the local Billing Agreement was cancelled but still has usable issued-key
   metadata, it is resumed with the Subscription `expiresAt`; an already active
   agreement retains its existing `nextBillingAt`.

Expected result:

- Subscription access remains continuous.
- Future renewal can continue.

## 10. Renewal Success

Flow:

1. Daily scheduler finds active billing agreements whose `nextBillingAt` is due.
2. Backend creates or reuses a renewal `payment_orders` row.
3. Backend charges Toss with an idempotency key.
4. On success, backend creates `subscription_payments`, extends access, clears pending changes, and records receipt evidence.
5. Billing agreement `nextBillingAt` is advanced.

Expected result:

- Pending downgrade or cycle change applies only after renewal charge succeeds.
- The subscription remains active for the new period.

## 11. Renewal Failure

Flow:

1. Toss recurring charge fails.
2. Backend records failure code/message on the order.
3. Billing agreement failure count increases.
4. Next retry date is scheduled within the 3-day grace window.
5. Email failure guidance is attempted.
6. After 3 attempts or grace expiration, billing agreement is suspended and the subscription may expire after grace.

Expected result:

- User gets a grace period before access ends.
- Renewal failure does not silently apply pending downgrade or cycle changes.

## 12. Account Withdrawal

Entry point:

- Profile/account settings through `DELETE /api/users/me`

Flow:

1. User confirms withdrawal with the account password.
2. Backend cancels a non-terminal local billing agreement and ACTIVE subscription before marking the account deleted.
3. Backend publishes an ID-only cleanup request when encrypted billing-key material exists.
4. Account soft deletion commits independently of Provider cleanup.
5. After commit, Backend asks the registered Provider to remove the billing key.
6. If cleanup fails, local renewal stays blocked, the key remains encrypted for retry, and an agreement-scoped Incident is created or updated.
7. A daily 01:15 retry processes only deleted users with `CANCELLED` agreements and retained keys.
8. Provider success or an already-removed response clears local issued-key fields and resolves the Incident.

Expected result:

- The withdrawn account is never charged by the renewal scheduler; deleted users are excluded before decryption, order creation, or Provider charge.
- Withdrawal does not create a refund. A refund requires the separate admin request, approval, and execution workflow for a specific payment.
- Social-only withdrawal remains `POLICY-PENDING`. The current password-confirmation flow is not a substitute for fresh provider reauthentication and linked provider-ID matching.

## 13. Refund and Entitlement Correction

Refund is an admin operation, but its result affects user support.

Flow:

1. Admin previews a specific subscription payment.
2. Admin creates a refund request.
3. Admin approves the refund request.
4. Admin executes provider cancel/refund.
5. After a succeeded refund, admin can create and execute entitlement correction if access should change.

Expected result:

- Refund does not automatically mutate subscription access.
- Access correction is explicit, previewed, approved, audited, and executed separately.

## Related Documents

### Required References

- [System Overview](system-overview.md): Technical structure behind these flows.
- [Admin Operations Guide](admin-operations-guide.md): Admin refund, correction, settlement, and incident operations.

### Reference Documents

- [Acceptance Test Checklist](acceptance-test-checklist.md): Testable scenarios corresponding to these flows.
- [Payment Integration Design](../design/payment-integration-design.md): Detailed flow design.
