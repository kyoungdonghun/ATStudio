---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: qa-integ
category: qa-result
status: complete
dependencies:
  - path: WI-20260809-ATS-052-qa-r2-handoff.md
    reason: Round-two independent review scope and output contract
  - path: WI-20260809-ATS-052-qa-result.md
    reason: Round-one findings under closure review
  - path: WI-20260809-ATS-052-remediation-handoff.md
    reason: Required remediation contract
---

# Independent QA R2 Result: WI-20260809-ATS-052

## Verdict

**FAIL**

`QA-052-001` and `QA-052-002` are closed, but one new P2 cross-layer defect
remains in the reactivation confirmation. A PASS is not permitted while that
finding is open.

## Severity Counts

| Severity | Open |
| -------- | ---: |
| P0       |    0 |
| P1       |    0 |
| P2       |    1 |
| P3       |    0 |

## Finding Closure

### QA-052-001 - Closed

The confirmation amount now uses pending plan and pending cycle independently,
matching the backend renewal precedence.

- Frontend: `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:239-247,942-947,1362-1383`
- Backend: `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:784-797,1073-1082`
- Tests: `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx:840-956`

No pending change, pending plan only, pending cycle only, both pending, and an
unresolved pending target are covered. An unresolved target disables
reactivation instead of inventing an amount.

### QA-052-002 - Closed

Standalone and reconciliation Billing Agreement reads now share one generation
owner. Reconciliation retires and aborts an older standalone retry before it can
commit, and stale retry success and failure are both covered.

- Frontend: `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:372-404,452-551`
- Tests: `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx:283-330`

## Findings

### P2 - QA-052-003: Reactivation can quote a stale pre-grace billing date

**Pointers**

- Frontend: `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:947,1362-1367`
- Frontend test gap: `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx:840-885,1938-1957`
- Backend renewal failure: `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:504-516`
- Backend Billing Agreement transitions: `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java:226-250`
- Backend reactivation: `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:241-257`
- Documented claim: `docs/design/payment-integration-design.md:299-301`, `docs/design/usecase/user-subscription.md:140-144`

**Evidence**

The modal prefers `billingAgreement.nextBillingAt` and falls back to
`subscription.expiresAt`. After a failed renewal, the backend extends
`subscription.expiresAt` to the renewal grace end but does not advance
`billingAgreement.nextBillingAt`. A subsequent user cancellation preserves that
old Billing Agreement date. Reactivating the resulting cancelled agreement then
calls `agreement.resume(subscription.getExpiresAt())`, so the backend schedules
the next charge for the grace-end Subscription date, not the date shown by the
modal.

The focused fixtures set both dates equal, so all tests pass without exercising
this valid state.

**Reproduction**

1. Start with an active Subscription and Billing Agreement whose expiry and
   `nextBillingAt` are date D.
2. Record a retryable renewal failure. The backend extends Subscription
   `expiresAt` to D plus the grace period while Billing Agreement
   `nextBillingAt` remains D.
3. Cancel before the grace period ends. The Subscription and Billing Agreement
   become `CANCELLED`; the Billing Agreement still retains D.
4. Open Manage and choose `Keep subscription`. The modal quotes D because the
   Agreement date is non-null.
5. Confirming reactivation resumes the Agreement at the later Subscription
   `expiresAt` date.

**Impact**

The explicit financial confirmation can state the wrong charge date, including
a date already in the past. This contradicts the changed documentation and
weakens the consent boundary added for `CR-031-089`.

**Required correction**

Derive the displayed date from the same backend reactivation branch: a
cancelled Agreement resumes at Subscription `expiresAt`, while an already
active Agreement retains its canonical `nextBillingAt`. Add a regression where
those two input dates differ after a renewal-grace extension.

## Acceptance Matrix

| Canonical root | Result   | Round-two evidence |
| -------------- | -------- | ------------------ |
| `CR-031-085`   | PASS     | Plan and Subscription reads retain abort plus generation ownership; exact `403 NO_ACTIVE_SUBSCRIPTION` is the only absence result; loading, empty, failure, and retry behavior pass. |
| `CR-031-086`   | PASS     | Exact Billing Agreement absence, visible retryable errors, preview retry/selection ownership, and stale standalone retry success/failure after reconciliation pass. |
| `CR-031-087`   | PASS     | Required checkout and return fields are single-valued and allowlisted before prepare; focused missing, blank, malformed, unsupported, unsafe, and duplicate cases make zero prepare calls. |
| `CR-031-088`   | PASS     | Prepare failure is terminal and retryable, the initial CTA names registration plus first charge, and fail callbacks suppress raw or blank Provider message text. |
| `CR-031-089`   | **FAIL** | Cancel and approve are separate, repeat mutation is fenced, and renewal amount precedence is correct, but `QA-052-003` leaves the confirmed next charge date inaccurate in a valid grace-period state. |

## Focused Behavior Matrix

| Check | Result | Evidence |
| ----- | ------ | -------- |
| No pending renewal target | PASS | Current plan and cycle price is quoted. |
| Pending plan only | PASS | Pending plan plus current cycle price is quoted. |
| Pending cycle only | PASS | Current plan plus pending cycle price is quoted. |
| Pending plan and cycle | PASS | Both pending values determine the price. |
| Unresolved pending plan | PASS | Reactivation remains disabled and no amount is invented. |
| Stale standalone retry success | PASS | Older success cannot replace cancellation reconciliation. |
| Stale standalone retry failure | PASS | Older failure cannot replace cancellation reconciliation. |
| Renewal-grace billing date | **FAIL** | The modal can prefer stale Agreement `nextBillingAt` over the date assigned by backend reactivation. |

## Tests And Commands

Executed from `frontend/`:

```text
npm test -- src/api/domainApis.test.ts src/pages/public/SubscriptionPlanPage.test.tsx src/pages/subscriber/SubscriptionPaymentPage.test.tsx src/pages/subscriber/SubscriptionManagePage.test.tsx --reporter=verbose
```

Result: exit 0; 4 test files passed; 191 tests passed; 0 failed; duration
9.72 seconds. The suites mocked frontend APIs and Provider SDK behavior.

## Effect Boundary Confirmation

- Reviewed the 14 tracked WI-052 implementation, test, and documentation files
  while excluding `output/**`.
- Ran only the four focused mocked frontend test files.
- Performed no real Provider, payment, refund, mail, export, download, API, or
  database mutation.
- Did not inspect protected output paths, ignored secrets, or local environment
  values.
- Provider response and durable runtime state were not observed. The open
  finding is established from the current frontend display rule and backend
  renewal, cancellation, and reactivation source contracts.

## Residual Risks

- Callback and return-query coverage remains representative rather than a
  mathematical enumeration of every encoded input permutation.
- Browser focus, Escape/backdrop behavior, and modal visual layout were not
  exercised in this focused integration review.
- No full frontend gate, backend test suite, browser session, live Provider run,
  or durable-state inspection was performed.
