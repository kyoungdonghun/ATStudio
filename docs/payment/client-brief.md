---
version: 0.2
last_updated: 2026-07-13
project: ATS
owner: docops
category: guide
status: draft
dependencies:
  - path: index.md
    reason: Payment documentation navigation
  - path: feature-inventory.md
    reason: Implemented feature list
  - path: acceptance-test-checklist.md
    reason: Testable client-facing behavior
---

# ATStudio Payment System Client Brief

> Purpose: Provide a client-facing draft explanation of the ATStudio payment system, focusing on behavior, safety, and review scope.

---

## 1. Summary

ATStudio now includes a recurring subscription payment system based on Toss automatic payment.

The system supports:

- Card registration through Toss billing auth.
- Immediate first charge when a user starts a subscription.
- Automatic renewal based on the user's next billing date.
- Plan upgrade, downgrade, and billing-cycle changes.
- Subscription cancellation with access preserved until the paid period ends.
- Admin payment operation screens for payment review, issue tracking, refund workflow, entitlement correction, and settlement review.

## 2. User-Facing Behavior

### 2.1 New Subscription

Users select a plan and billing cycle, register a card through Toss, and the subscription starts only after ATStudio confirms both card registration and the first charge.

### 2.2 Plan Upgrade

When a user upgrades to a higher plan, ATStudio charges the remaining-period difference immediately through the registered automatic payment method. The higher plan is applied immediately after successful charge.

### 2.3 Downgrade

When a user moves to a lower plan, ATStudio schedules the change for the next billing date. The current paid plan remains available until the current period ends.

### 2.4 Billing Cycle Change

Monthly/yearly cycle changes apply from the next renewal. This keeps billing fair and avoids mid-period negative or partial refund calculations.

### 2.5 Cancellation

Cancelling a subscription stops future renewal. The user can continue using paid access until the current expiration date.

### 2.6 Reactivation

If the user changes their mind before the paid period expires, the subscription can be reactivated.

### 2.7 Account Withdrawal

Account withdrawal stops local automatic renewal before the account is marked deleted. ATStudio then removes the Provider billing key after the local change commits. If Provider cleanup temporarily fails, local renewal remains blocked and the system records and retries the cleanup. Withdrawal does not automatically refund an earlier payment.

## 3. Admin Operation Support

ATStudio includes an admin payment operations screen at `/admin/payments`.

Admins can:

- Review payment orders.
- Review automatic payment method state.
- Review finalized subscription payments.
- Review payment reconciliation incidents.
- Review receipt evidence.
- Review payment operation audit logs.
- Import settlement CSV evidence.
- Review settlement mismatches.
- Create, approve, and execute refund workflow.
- Correct local subscription access after a successful refund when support approval requires it.

## 4. Safety and Auditability

The system is designed to keep payment operation traceable and auditable.

Important safety rules:

- Raw billing keys are encrypted and not shown in user/admin screens.
- Raw card numbers are not stored or displayed.
- Toss secret keys remain server-side only.
- Refund and access correction are separate workflows.
- Settlement import is review evidence and does not change user access.
- Admin operations create audit records where applicable.

## 5. Review Scope for Client Testing

Client testing should focus on:

- New subscription checkout.
- Card registration retry.
- Plan upgrade.
- Downgrade reservation.
- Billing-cycle change reservation.
- Cancellation and reactivation.
- Account withdrawal with confirmation that future renewal stops and no automatic refund is created.
- Admin review of payment evidence.
- Refund and entitlement correction only in safe test/staging conditions.
- Settlement CSV import only with safe test data.

## 6. Current Deferred Scope

The following items are recognized as future or separate work and should not be treated as defects in the current payment delivery:

- Tax invoice request/admin workflow for future B2B invoice, bank-transfer, postpaid, or contract purchase scope.
- Toss Settlement API direct automation.
- Toss webhook hardening.
- Multi-PG expansion beyond Toss recurring billing.
- Cash receipt issue/cancel automation for non-card payment methods.
- Multi-server scheduler lock, unless deployment topology changes.

## Related Documents

### Required References

- [Feature Inventory](feature-inventory.md): Full implemented feature list.
- [Acceptance Test Checklist](acceptance-test-checklist.md): Testable acceptance scenarios.

### Reference Documents

- [User Flows](user-flows.md): Detailed user-facing behavior.
- [Admin Operations Guide](admin-operations-guide.md): Admin screen usage.
