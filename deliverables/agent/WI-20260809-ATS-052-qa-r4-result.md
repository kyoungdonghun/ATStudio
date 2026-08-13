---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: qa-integ
category: qa-result
status: complete
dependencies:
  - path: WI-20260809-ATS-052-qa-r4-handoff.md
    reason: Final independent review scope and output contract
  - path: WI-20260809-ATS-052-qa-r3-result.md
    reason: QA-052-004 under final closure review
  - path: WI-20260809-ATS-052-remediation-r3-handoff.md
    reason: Required backend-shape correction contract
---

# Independent QA R4 Result: WI-20260809-ATS-052

## Verdict

**PASS**

The complete uncommitted WI-052 diff has zero open P0-P3 findings. All four
historical QA findings are closed, including `QA-052-004` against the actual
backend pending-change response shape. PASS is permitted.

## Severity Counts

| Severity | Open |
| -------- | ---: |
| P0       |    0 |
| P1       |    0 |
| P2       |    0 |
| P3       |    0 |

## Closure Matrix

| Finding | Result | Final independent evidence |
| ------- | ------ | -------------------------- |
| `QA-052-001` | CLOSED | Reactivation amount uses pending plan and pending cycle independently, matching renewal order pricing in `PaymentCommandTransactionService`. No pending target, pending plan, pending cycle, both pending, and unresolved different target behavior are covered. |
| `QA-052-002` | CLOSED | Standalone and reconciliation Billing Agreement reads share one generation owner. Reconciliation retires an older retry, and both late retry success and failure remain unable to replace the canonical post-cancellation state. |
| `QA-052-003` | CLOSED | A cancelled Agreement quotes the Subscription `expiresAt` passed to backend `resume`; an already active Agreement quotes its retained `nextBillingAt`. Grace-extended, active-date, and missing-date branches are covered. |
| `QA-052-004` | CLOSED | The real cycle-only shape uses `pendingSubscriptionId == subscription.id`. The frontend now resolves that ID from the embedded current plan even when the plan is absent from the active public list; a genuinely different unresolved target remains fail-closed. |

Key pointers:

- Frontend amount/date resolution: `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:239-257,952-958,1324-1400`
- Frontend concurrency ownership: `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:388-414,462-561,623-668`
- Reactivation regressions: `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx:840-1019`
- Backend response and pending creation: `src/main/java/com/atstudio/atstudio/dto/subscription/UserSubscriptionResponse.java:8-33`, `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:182-183`
- Backend reactivation and renewal: `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:241-257`, `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:784-797,1073-1082`

## Acceptance Matrix

| Canonical root | Result | R4 evidence |
| -------------- | ------ | ----------- |
| `CR-031-085` | PASS | Plan and Subscription reads have abort plus generation ownership. Only exact `403 NO_ACTIVE_SUBSCRIPTION` becomes absence; loading, empty, error, retry, and retired-audience behavior pass. |
| `CR-031-086` | PASS | Only exact `404 BILLING_AGREEMENT_NOT_FOUND` becomes absence. Auth, server, other 404, and network failures remain retryable; preview retry/selection ownership and retry-versus-reconciliation ownership pass. |
| `CR-031-087` | PASS | Checkout and callback values are single-valued and allowlisted. Missing, blank, malformed, unsupported, unsafe, partial, or duplicate required state makes zero prepare or confirmation calls as applicable. |
| `CR-031-088` | PASS | Prepare failure is terminal `ERROR` with bounded copy and retry. Fail callbacks ignore Provider message text, and the initial CTA names payment-method registration plus the first charge. |
| `CR-031-089` | PASS | Reactivation cancel and approve are separate, rapid submission is fenced, amount follows renewal precedence, date follows backend Agreement status branches, and missing canonical data remains non-actionable. |

## Required Behavior Matrix

| Area | Result | Evidence |
| ---- | ------ | -------- |
| Plan | PASS | Exact absence, loading, empty, retryable error, and audience-race cases are distinct and covered. |
| Billing Agreement | PASS | Typed absence, auth/server/network error, retry, and stale retry ownership are covered without fabricated absence. |
| Preview | PASS | Failure stays visible and non-actionable; retry succeeds; a retired selection cannot overwrite the latest preview. |
| Checkout | PASS | Required route, callback, and return state is validated before mutation; prepare and confirm zero-call boundaries, terminal retry, bounded fail copy, and first-charge CTA pass. |
| Reactivation | PASS | Cancel, approve, duplicate action, current/pending target combinations, inactive current plan, unresolved different target, Agreement date branches, grace extension, and missing date pass. |
| Amount/date contract | PASS | Displayed amount matches backend renewal plan/cycle precedence, and displayed date matches the current backend reactivation branch. |

## Tests And Commands

Executed once from `frontend/`:

```text
npm test -- src/api/domainApis.test.ts src/pages/public/SubscriptionPlanPage.test.tsx src/pages/subscriber/SubscriptionPaymentPage.test.tsx src/pages/subscriber/SubscriptionManagePage.test.tsx --reporter=verbose
```

Result: exit 0; 4 test files passed; 194 tests passed; 0 failed; 0 skipped;
duration 11.53 seconds. The focused suites mock frontend API and Toss SDK
behavior; no real Provider or backend call was made.

The `pending cycle only` fixture now uses `pendingSubscriptionId: 1`, matching
the backend response. A separate regression removes current plan `1` from the
active plan list and still proves the embedded yearly price of 99,000. The
unresolved different target uses ID `999` and remains disabled.

## Effect Boundary Confirmation

- Reviewed all 14 tracked WI-052 implementation, test, and documentation diffs,
  plus only the current backend source contracts needed for the required matrix.
- Ran only the four focused mocked frontend test files.
- Performed no real Provider, payment, refund, mail, export, download, API, or
  database effect.
- Did not inspect protected output paths, the protected ZIP, ignored secrets,
  or local environment values.
- Provider response and durable runtime state were not observed. Cross-layer
  conclusions come from current backend source contracts and mocked frontend
  assertions.

## Residual Risks

- Checkout and callback tests cover representative invalid combinations rather
  than every possible encoded query permutation.
- Browser focus, Escape/backdrop behavior, and modal visual layout were not
  exercised by the focused jsdom suites.
- No full frontend gate, backend suite, browser session, live Provider run, or
  durable-state inspection was performed in this focused final review.
