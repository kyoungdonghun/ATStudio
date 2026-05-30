---
version: 1.0
last_updated: 2026-05-30
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
---

# Payment Documentation Pack

> Purpose: Provide a readable entry point for the ATStudio payment system, connecting implemented behavior, operations, client-facing explanation, acceptance testing, and future extension points.

---

## 1. Scope

This directory explains the ATStudio payment system as of 2026-05-30.

The current payment system is recurring-subscription first:

- Users subscribe through Toss billing-key based automatic payment.
- The first subscription charge happens immediately after billing-key registration.
- Upgrades are charged immediately for the remaining-period difference.
- Downgrades and billing-cycle-only changes are scheduled for the next renewal.
- Admins can review payment ledgers, reconciliation incidents, receipt evidence, refund workflow, entitlement correction workflow, and settlement reconciliation from `/admin/payments`.

This directory is a guide layer. Detailed source-of-truth design documents remain in `docs/design/`.

## 2. Reading Order

| Reader Goal | Start Here | Then Read |
| :-- | :-- | :-- |
| Understand what was added | [Feature Inventory](feature-inventory.md) | [System Overview](system-overview.md) |
| Understand user behavior | [User Flows](user-flows.md) | [Acceptance Test Checklist](acceptance-test-checklist.md) |
| Operate admin payment workflows | [Admin Operations Guide](admin-operations-guide.md) | [Payment Operations Runbook](../design/payment-operations-runbook.md) |
| Explain the feature to a client | [Client Brief](client-brief.md) | [Acceptance Test Checklist](acceptance-test-checklist.md) |
| Plan the next payment work | [Known Limits and Next Steps](known-limits-and-next-steps.md) | [SR-93](../SR/SR-93.md) |

## 3. Document List

| Document | Description | Status |
| :-- | :-- | :-- |
| [Feature Inventory](feature-inventory.md) | Complete current feature list grouped by user, backend, admin, operations, and future extension area. | stable |
| [System Overview](system-overview.md) | Technical architecture, core tables, APIs, provider boundaries, schedulers, and security rules. | stable |
| [User Flows](user-flows.md) | User-facing subscription, billing method, plan change, cancellation, reactivation, and failure flows. | stable |
| [Admin Operations Guide](admin-operations-guide.md) | Admin `/admin/payments` tab guide and operational usage boundaries. | stable |
| [Acceptance Test Checklist](acceptance-test-checklist.md) | Current acceptance checklist for local and client-adjacent payment testing. | stable |
| [Client Brief](client-brief.md) | Client-facing draft explanation of the payment system without internal implementation noise. | draft |
| [Known Limits and Next Steps](known-limits-and-next-steps.md) | Planned, deferred, and out-of-scope payment capabilities. | stable |

## 4. Maintenance Rule

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

### Reference Documents

- [API Spec](../design/api-spec.md): REST API source of truth.
- [DB Schema](../design/db-schema.md): Payment table source of truth.
- [Original Final Acceptance Checklist](../../deliverables/user/PAYMENT-FINAL-ACCEPTANCE-CHECKLIST-20260525.md): Historical checklist that this pack normalizes into current guide form.
