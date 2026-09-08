---
version: 1.9
last_updated: 2026-09-09
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
- Historical disposable MySQL 8/InnoDB schema validation and required
  concurrency-race evidence for the prior 42-table source snapshot.
- Bounded local/provider reconciliation keyset batches, capped API issue details with full mismatch counters, and query-aligned fresh-schema indexes.
- Billing-key V2 key-ID envelopes with fail-closed active/retained V2 key-ring validation.
- Explicit configurable payment cron zone with `Asia/Seoul` default under the approved single-server deployment.

This is not production-readiness closure or a full financial back-office suite.
V1 has a fresh-only source schema with 43 derived `CREATE TABLE` statements and
43 JPA entities. Its 2026-08-17 guarded disposable MySQL manifest records 43 tables,
511 columns, 175 indexes, 91 foreign keys, 6 plans, 6 plan keys, zero forbidden
tables/columns, and SHA-256
`b177b34780fabc75ea8b4608a0d210167a81d414d2778cc1d1dc5c0e39c8fea4`.
Retained-data migration is not supplied. The 2026-09-08 approved development
round and its automated quality gates are complete; accepted user/Toss TEST/Gmail
cases are listed in the [dated acceptance record](acceptance-test-checklist.md#2026-09-08-acceptance-record).
Target-production decisions remain in [SR-93](../SR/SR-93.md#remaining-production-gates).

Use the [central source/runtime snapshot](index.md#2026-09-09-source-and-runtime)
for the current source checkpoint and applied TEST-product boundary.

### 2026-09-09 Superseding Status

Product `2d47504` is applied to the named TEST runtime; MA confirmed `main=origin/main` at documentation checkpoint `1c467d1` before this follow-up. The [supplied post-restart UI check](acceptance-test-checklist.md#2026-09-09-post-restart-ui-check) closes the requested logged-in, DELUXE YEARLY paid-access and download-UI follow-up only. No new live, DB, downloaded-file or Provider checks were made; production SR-93 remains **HOLD**.

The maintenance record below preserves the earlier WI009/WI010 checkpoints. Its untested-mail statements are superseded only within the later [WI011/WI012 mail evidence](index.md#wi011wi012-mail-and-documentation-closeout-2026-09-08): three Korean messages received/readable in Inbox, plus website reset-mail receipt and reset-page opening. Future Inbox placement, scheduler execution and an SPF/DKIM root cause remain unproved. This follow-up makes no new SMTP claim and does not reset earlier accepted TEST/Gmail cases.

### 2026-09-08 Maintenance Candidates

The callback hint, paid-upgrade confirmation, registration charge-history
preservation and explicit correction-expiry safeguards are completed fixes,
not deferred work. These candidates are not approved feature additions.

| Candidate | Current disposition |
| :-- | :-- |
| Correction/reconciliation Incident terminology | Source-complete in WI007: both admin entries display `구독 이용권 조정` and require `구독 이용권 조정 실행`; incident display is `결제 점검 이슈`. No route, API or canonical-term rename; old confirmation wording is rejected. |
| Mail wording and deliverability | Korean payment/reconciliation copy is source-complete in WI006, with UTF-8 MIME and escaping tests. The earlier English mail in Gmail spam remains dated evidence; updated-mail delivery, inbox placement and an SPF/DKIM root cause are not proved. |
| Receipt display after refund | Source-complete in WI007: `증빙 상태` and `발급 기록` clarify stored original-charge evidence, separate from refund state. Known receipt statuses are localized; unknown values and safe URL/reference fallback remain. No refund aggregate or new API was added. |
| Correction edit/cancel workflow | No correction edit/cancel API exists. A controlled workflow is larger future scope, not implicit authorization for manual data changes. |
| Storage-integrity pagination | Consider bounded pagination when scale warrants it; the 10 historical missing media references need a separate data/media decision, not a UI waiver. |

The small copy implementations are complete under
[REQ002](../../deliverables/user/REQ-20260908-ATS-002.md); final aggregate,
synthetic-browser and documentation gates passed at the source boundary in
[WI008](../../deliverables/agent/WI-20260908-ATS-008-evidence-pack.md).
[WI009](../../deliverables/agent/WI-20260908-ATS-009-evidence-pack.md) subsequently
confirmed restart and HTTP adoption of that tested artifact, plus basic
unauthenticated public-page rendering. The changed mail is in the running
artifact; fresh SMTP delivery and inbox placement remain untested. WI010 has
MA's real authenticated admin orders/nine-tab rendering, localized receipt
rows and guarded correction controls at both admin entry points with no
mutations, plus focused frontend and formatting PASS. This covers observed
desktop surfaces, not new subscriber mutation acceptance. MA's verified
70-path source/test/documentation commit/push `7eae086` completes WI010 limited
work; [WI010 evidence](../../deliverables/agent/WI-20260908-ATS-010-evidence-pack.md)
separates that result from this later, not-yet-committed documentation receipt.
Prior full-suite totals are not new runs, and these development observations
do not close external/production gates.

An actual critical money or security defect remains a release blocker regardless
of implementation size. New PGs, webhooks and inactive OAuth providers are not
mandatory additions to this card-recurring/password-login release scope.

## 2. Planned Features

| Candidate | Why It Exists | Suggested Timing |
| :-- | :-- | :-- |
| Toss Settlement API adapter | Current settlement import is CSV/manual. Direct Toss adapter can reduce manual import when operation volume grows. | After CSV process is accepted and real operation volume justifies automation. |
| Toss webhook hardening | Webhook can provide auxiliary provider event visibility. It should complement, not replace, ATStudio-owned renewal and reconciliation state. | After current provider reconciliation is stable. |
| Multi-PG expansion | Provider interfaces allow a future adapter, but only Toss recurring billing is implemented now. | When a business requirement selects another provider. |
| Admin receipt/audit polish | Admin UI can be refined around current receipt evidence and audit operation needs. | After client acceptance if operators need a more polished back-office workflow. |

## 3. On-Hold Items

| Item | Reason |
| :-- | :-- |
| Cash receipt issue/cancel automation | Current recurring subscription policy is card-only. Receipt evidence capture exists, but cash receipt mutation is held until a cash-like payment method is approved. |
| Tax invoice request/admin workflow | Current recurring subscription policy is card-only, where provider/card receipt evidence is the normal first evidence path. Reopen only if ATStudio adds B2B invoice, bank-transfer, postpaid, or contract purchase scope. |
| Multi-server scheduler lock | Current deployment assumption is single server. Add locking only if ATStudio runs multiple backend instances that can execute the same scheduler. |
| Manual withdrawal cleanup endpoint | Not implemented. Current recovery is the targeted daily retry; add a controlled operator trigger only under a separately approved operations requirement. |
| Creator royalty settlement / seller payout | Current settlement import compares PG-to-ATStudio payment settlement evidence. Creator payout is a different business process. |

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
