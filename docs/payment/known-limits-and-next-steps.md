---
version: 1.3
last_updated: 2026-07-16
project: ATS
owner: docops
category: guide
status: stable
dependencies:
  - path: index.md
    reason: Payment documentation navigation
  - path: feature-inventory.md
    reason: Current implemented feature list
  - path: ../SR/SR-93.md
    reason: Production payment SR and remaining hardening notes
---

# Payment Known Limits and Next Steps

> Purpose: Separate current payment-system limitations, deferred features, and future extension candidates from implemented behavior.

---

## 1. Current Delivery Boundary

The following capabilities are implemented and code/test verified for the current local payment scope:

- Toss billing-key recurring subscription checkout.
- Initial charge after billing-key registration.
- Upgrade, downgrade, billing-cycle change, cancellation, and reactivation policy.
- Renewal scheduler, 3-day grace period, and 3 retry attempts.
- Admin payment operations screen.
- Reconciliation incidents.
- Receipt evidence storage.
- Refund ledger and Toss cancel/refund execution.
- Entitlement correction workflow after refund.
- CSV/manual settlement import and settlement reconciliation review.
- Local-first account-withdrawal billing stop, after-commit Provider cleanup, daily retry, Incident resolution, and no-auto-refund separation.
- Stable payment command identity, strict Provider transaction boundaries, retry-gate consumption, refund lease fencing, and exact finalize-only reconciliation.
- Fresh disposable MySQL 8/InnoDB schema validation and all seven required concurrency races.
- Bounded local/provider reconciliation keyset batches, capped API issue details with full mismatch counters, and query-aligned fresh-schema indexes.
- Billing-key v2 key-ID envelopes with legacy v1 decryption compatibility and `TOSS_BILLING` startup key-ring validation.
- Explicit configurable payment cron zone with `Asia/Seoul` default under the approved single-server deployment.

This is not production-readiness closure or a full financial back-office suite. The 2026-07-16 reconciliation index patch and `EXPLAIN` templates have source/static contract evidence, not retained-database query-plan evidence. Retained-database migration rehearsal, live Toss configuration, production deployment/monitoring, client acceptance, non-payment gates, and final release approval remain open under `ATS020-X-01`. See [P1 Payment Integrity Closure](../audit/p1-payment-integrity-closure-20260715.md) and [SR-93](../SR/SR-93.md).

Dependency boundary: the active development branch resolves Vite 6.4.3 and both production-only and unfiltered npm audits return 0. The frozen `codex/client-demo-stable` branch was inspected read-only and remains at Vite 6.4.1 with 5 production findings and 13 total findings. No file on that branch was changed; public dev-server use remains blocked until user-approved remediation or a controlled-access alternative.

## 2. Planned Features

| Candidate | Why It Exists | Suggested Timing |
| :-- | :-- | :-- |
| Toss Settlement API adapter | Current settlement import is CSV/manual. Direct Toss adapter can reduce manual import when operation volume grows. | After CSV process is accepted and real operation volume justifies automation. |
| Toss webhook hardening | Webhook can provide auxiliary provider event visibility. It should complement, not replace, ATStudio-owned renewal and reconciliation state. | After current provider reconciliation is stable. |
| Multi-PG expansion | Provider interfaces allow future KakaoPay/NaverPay-like adapters, but only Toss recurring billing is implemented now. | When a business requirement selects another provider. |
| Admin receipt/audit polish | Admin UI can be refined around current receipt evidence and audit operation needs. | After client acceptance if operators need a more polished back-office workflow. |

## 3. On-Hold Items

| Item | Reason |
| :-- | :-- |
| Cash receipt issue/cancel automation | Current recurring subscription policy is card-only. Receipt evidence capture exists, but cash receipt mutation is held until a cash-like payment method is approved. |
| Tax invoice request/admin workflow | Current recurring subscription policy is card-only, where provider/card receipt evidence is the normal first evidence path. Reopen only if ATStudio adds B2B invoice, bank-transfer, postpaid, or contract purchase scope. |
| Multi-server scheduler lock | Current deployment assumption is single server. Add locking only if ATStudio runs multiple backend instances that can execute the same scheduler. |
| Manual withdrawal cleanup endpoint | Not implemented. Current recovery is the targeted daily retry; add a controlled operator trigger only under a separately approved operations requirement. |
| Creator royalty settlement / seller payout | Current settlement import compares PG-to-ATStudio payment settlement evidence. Creator payout is a different business process. |
| User-facing one-time subscription payment | Subscription payment is recurring-first. One-time subscription prepare/confirm is blocked for current scope. |

## 4. Future Documentation Update Rule

When one of the planned items is implemented:

1. Move it from this file to [Feature Inventory](feature-inventory.md).
2. Add flow details to [User Flows](user-flows.md) or [Admin Operations Guide](admin-operations-guide.md).
3. Add acceptance checks to [Acceptance Test Checklist](acceptance-test-checklist.md).
4. Update [Client Brief](client-brief.md) if the behavior is client-visible.
5. Link the new REQ/SR or design document from [Payment Documentation Pack](index.md).

## 5. Suggested Next REQ Candidates

| Candidate REQ | Suggested Scope |
| :-- | :-- |
| Toss Settlement API adapter | Provider API import adapter, idempotent import, comparison with CSV/manual rows, admin source filter update. |
| Toss webhook auxiliary receiver | Secure endpoint, signature validation if applicable, event ledger, reconciliation trigger, support-safe admin visibility. |
| Multi-PG provider expansion | Provider selection policy, adapter contract, provider-specific checkout/callback differences, test matrix. |

Tax invoice request/admin workflow is intentionally not listed as a next REQ while ATStudio remains card-only recurring billing. Reopen it only after the product scope includes B2B invoice, bank-transfer, postpaid, or contract purchase payments.

## Related Documents

### Required References

- [Feature Inventory](feature-inventory.md): Current implemented feature list.
- [SR-93](../SR/SR-93.md): Production payment SR and remaining hardening context.
- [P1 Payment Integrity Closure](../audit/p1-payment-integrity-closure-20260715.md): Current code/test closure and residual risks.

### Reference Documents

- [Payment Refund, Receipt, Settlement, and Tax Invoice Policy](../design/payment-refund-receipt-settlement-policy.md): Policy source for deferred tax/receipt scope.
- [Payment Settlement Import Design](../design/payment-settlement-import-design.md): Current settlement import and future Toss API adapter path.
