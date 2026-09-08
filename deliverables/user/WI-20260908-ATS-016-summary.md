---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: qa-integ
category: work-summary
status: confirmed
---

# WI-20260908-ATS-016: Integration Review Summary

Baseline: main `8161f0a00b088001a37962da719c3ace8fea44ad`. Approved scope: REQ-20260908-ATS-004, review only. **Completed with two P1 release blockers; no P0 found.**

| Finding | Trigger and impact | Primary location |
| --- | --- | --- |
| F1 - P1 | A returning USER with an expired subscription can complete the Provider charge, but the finalizer rejects the retained row. Paid access and the new payment ledger remain absent; replay does not fix it. | `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:527,552,1319-1324`; checkout entry: `frontend/src/pages/public/SubscriptionPlanPage.tsx:162-171,220-225` |
| F2 - P1 | Two upgrade requests for the same plan/current period but different next cycles get distinct command/Provider keys before either finalizes. Both can charge the same difference; the last pending cycle wins. | `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:100-150,635-663,823-849`; per-view UI guard: `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:377-381,580-582` |

Both are confirmed static code paths with concrete preconditions, not executed incidents or claims about deployed production. No implementation was performed. Existing ownership checks, short-transaction locks, same-command idempotency, Provider transaction uniqueness and UI confirmation were checked; they do not close these paths.

## Policy Alignment

- Current annual-period upgrades and next MONTHLY renewal are separately calculated/displayed; explicit paid confirmation and related tests exist.
- Fail callbacks use owner-scoped state reads and bounded hints, not URL claims of payment outcome. Zero-amount registration now preserves monetary charge history.
- Refund and entitlement correction remain separate audited operations. Renewal retry/grace and standard cancellation/access behavior have substantive local tests.
- Free full-song streaming, paid/quota downloads, card-only Toss and single-server policies are unchanged.

## Validation Boundary

This reviewer only inspected source/documents/tests and created two reports. MA reports backend **1,708 total / 1,689 passed / 19 skipped / 0 failed**: **18 environment-gated MySQL + 1 LocalStorage platform test**. Frontend: **112 files / 1,493 tests PASS, 120.35s**. MA independently confirmed F1/F2 source chains. No reviewer test execution or product/runtime/Git/credential action; MA reports no payment/mail/real-database actions. Suite results are MA-reported, not independently rerun here.

Inspected tests cover fresh-user finalize-only recovery and identical upgrade duplicates, but miss F1 returning-user completion and F2 cross-cycle competition. The evidence pack maps defenses, doc/code/tests and bounded test candidates. Prior Toss TEST acceptance remains historical evidence, not production validation.

WI016 is independently complete. MA/WI018 owns central tests, operational/dependency review and final synthesis. Product remediation requires separate scope; SR-93 production gates remain OPEN.

## Related Documents

- [Detailed Evidence and Test Candidates](../agent/WI-20260908-ATS-016-evidence-pack.md)
- [Approved REQ004](REQ-20260908-ATS-004.md)
