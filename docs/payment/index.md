---
version: 1.6
last_updated: 2026-07-17
project: ATS
owner: docops
category: guide
status: stable
dependencies:
  - path: ../design/payment-integration-design.md
    reason: Primary payment integration design source
  - path: ../design/payment-refund-receipt-settlement-policy.md
    reason: Refund, receipt, settlement, and tax invoice policy source
  - path: ../design/payment-operations-runbook.md
    reason: Payment operations and incident response source
  - path: ../SR/SR-93.md
    reason: Production payment checklist and SR handoff source
  - path: ../audit/p1-payment-integrity-closure-20260715.md
    reason: Current payment-integrity code/test closure and remaining gates
---

# Payment Documentation Pack

> Purpose: Provide a readable entry point for the ATStudio payment system, connecting implemented behavior, operations, client-facing explanation, acceptance testing, and future extension points.

---

## 1. Scope

This directory explains the ATStudio payment system as of 2026-07-16.

The current payment system is recurring-subscription first:

- Users subscribe through Toss billing-key based automatic payment.
- The first subscription charge happens immediately after billing-key registration.
- Upgrades are charged immediately for the remaining-period difference.
- Downgrades and billing-cycle-only changes are scheduled for the next renewal.
- Account withdrawal cancels local renewal eligibility before soft deletion, then attempts Provider billing-key cleanup after commit.
- Withdrawal cleanup failure is visible as a deduplicated Incident and retried daily; withdrawal never creates an automatic refund.
- Admins can review payment ledgers, reconciliation incidents, receipt evidence, refund workflow, entitlement correction workflow, and settlement reconciliation from `/admin/payments`.
- Existing subscribers re-register a payment method through a zero-amount `BILLING_AGREEMENT` order; registration itself does not charge or change the current plan.
- V1 starts from the fresh-only `schema.sql` plus the six-plan `seed.sql`, then runs with `ddl-auto=validate`; see [System Overview](system-overview.md) and [DB Schema](../design/db-schema.md).

This directory is a guide layer. Detailed source-of-truth design documents remain in `docs/design/`.

## 2. Current Closure Decision

The three 2026-07-13 P0 behaviors are implemented and focused-test verified in implementation commit `d11c62d`: protected Track media, secret-free mail delivery logs, and post-withdrawal renewal stop. This statement is limited to the P0 remediation slice.

The later payment-integrity findings F-01 through F-05 are closed at the current repository code/test boundary. Packages A-G, the WI-008/WI-011 corrections, WI-012 independent PASS, and the disposable MySQL 7/7 proof are mapped in [P1 Payment Integrity Closure](../audit/p1-payment-integrity-closure-20260715.md).

Production readiness remains OPEN in [SR-93](../SR/SR-93.md), and the broader full-system release verdict remains NO-GO while retained-database, live Toss, deployment, client-acceptance, non-payment, and final quality gates are open.

Closed scope:

- Card-based Toss recurring subscription checkout.
- User subscription lifecycle and plan-change policy.
- Renewal, failure, retry, and expiration handling.
- Stable payment command identity, strict Provider transaction boundaries, refund lease fencing, and finalize-only reconciliation.
- Local-first account-withdrawal cancellation, after-commit Provider cleanup, durable Incident/retry handling, already-removed convergence, and no-auto-refund separation.
- Admin payment operations for ledgers, incidents, receipts, audit logs, refunds, entitlement correction, and CSV/manual settlement review.

Not blockers for closure:

- Toss webhook hardening.
- Toss Settlement API adapter.
- Multi-PG expansion.
- A future provider adapter, if selected by an approved product requirement.
- Additional operator notification channels.

Removed payment aliases and direct-subscription creation are not V1 compatibility paths. The official V1 branch candidate is `codex/p1-acceptance-hardening`; its current frontend install resolves Vite 6.4.3. Public access still requires a newly verified operator-controlled acceptance runtime.

On hold under the current card-only recurring subscription premise:

- Tax invoice request/admin workflow.
- Cash receipt issue/cancel mutation.
- B2B invoice, bank-transfer, postpaid, or contract purchase payment flows.

## 3. Reading Order

| Reader Goal | Start Here | Then Read |
| :-- | :-- | :-- |
| Understand what was added | [Feature Inventory](feature-inventory.md) | [System Overview](system-overview.md) |
| Understand user behavior | [User Flows](user-flows.md) | [Acceptance Test Checklist](acceptance-test-checklist.md) |
| Operate admin payment workflows | [Admin Operations Guide](admin-operations-guide.md) | [Payment Operations Runbook](../design/payment-operations-runbook.md) |
| Explain the feature to a client | [Client Brief](client-brief.md) | [Acceptance Test Checklist](acceptance-test-checklist.md) |
| Plan the next payment work | [Known Limits and Next Steps](known-limits-and-next-steps.md) | [SR-93](../SR/SR-93.md) |
| Verify the payment-integrity decision | [P1 Payment Integrity Closure](../audit/p1-payment-integrity-closure-20260715.md) | [P1 Trace Matrix](../audit/p1-remediation-trace-matrix-20260714.md) |

## 4. Document List

| Document | Description | Status |
| :-- | :-- | :-- |
| [Feature Inventory](feature-inventory.md) | Complete current feature list grouped by user, backend, admin, operations, and future extension area. | stable |
| [System Overview](system-overview.md) | Technical architecture, core tables, APIs, provider boundaries, schedulers, and security rules. | stable |
| [User Flows](user-flows.md) | User-facing subscription, billing method, plan change, cancellation, reactivation, and failure flows. | stable |
| [Admin Operations Guide](admin-operations-guide.md) | Admin `/admin/payments` tab guide and operational usage boundaries. | stable |
| [Acceptance Test Checklist](acceptance-test-checklist.md) | Current acceptance checklist for local and client-adjacent payment testing. | stable |
| [Client Brief](client-brief.md) | Client-facing draft explanation of the payment system without internal implementation noise. | draft |
| [Known Limits and Next Steps](known-limits-and-next-steps.md) | Planned, deferred, and out-of-scope payment capabilities. | stable |

## 5. Maintenance Rule

When a new payment feature is added, update this directory in the same commit or the immediately following documentation commit.

At minimum:

- Add the feature to [Feature Inventory](feature-inventory.md).
- Add or update affected flows in [User Flows](user-flows.md) or [Admin Operations Guide](admin-operations-guide.md).
- Add acceptance checks to [Acceptance Test Checklist](acceptance-test-checklist.md).
- Move the item from [Known Limits and Next Steps](known-limits-and-next-steps.md) to implemented status when complete.

## Related Documents

### Required References

- [Payment Integration Design](../design/payment-integration-design.md): Detailed subscription payment design and implementation decisions.
- [Payment Refund, Receipt, Settlement, and Tax Invoice Policy](../design/payment-refund-receipt-settlement-policy.md): Detailed operating policy for refund, receipt, settlement, and tax invoice scope.
- [Payment Operations Runbook](../design/payment-operations-runbook.md): Production-facing incident and operations procedures.
- [SR-93](../SR/SR-93.md): Production payment checklist and SR tracking source.
- [P1 Payment Integrity Closure](../audit/p1-payment-integrity-closure-20260715.md): Current code/test closure, exact evidence, and open production gates.

### Reference Documents

- [API Spec](../design/api-spec.md): REST API source of truth.
- [DB Schema](../design/db-schema.md): Payment table source of truth.
- [Original Final Acceptance Checklist](../../deliverables/user/PAYMENT-FINAL-ACCEPTANCE-CHECKLIST-20260525.md): Historical checklist that this pack normalizes into current guide form.
