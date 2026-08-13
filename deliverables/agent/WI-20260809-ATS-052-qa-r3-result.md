---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: qa-integ
category: qa-result
status: complete
dependencies:
  - path: WI-20260809-ATS-052-qa-r3-handoff.md
    reason: Round-three independent review scope and output contract
  - path: WI-20260809-ATS-052-qa-r2-result.md
    reason: Prior findings under closure review
  - path: WI-20260809-ATS-052-remediation-r2-handoff.md
    reason: QA-052-003 correction contract
---

# Independent QA R3 Result: WI-20260809-ATS-052

## Verdict

**FAIL**

`QA-052-001`, `QA-052-002`, and `QA-052-003` are closed for their stated
reproductions, but one new P2 cross-layer defect remains. The cycle-only
reactivation test uses a response shape that the backend does not produce and
therefore misses a valid state in which reactivation is incorrectly disabled.
A PASS is not permitted while that finding is open.

## Severity Counts

| Severity | Open |
| -------- | ---: |
| P0       |    0 |
| P1       |    0 |
| P2       |    1 |
| P3       |    0 |

## Finding Closure

### QA-052-001 - Closed

For resolvable renewal targets, the confirmation amount now follows backend
pending-plan and pending-cycle precedence. No pending change, pending plan,
pending cycle, both pending values, and unresolved target fail-closed behavior
are represented in source. `QA-052-004` below is a separate availability and
test-validity defect in the cycle-only edge state.

- Frontend: `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:239-247,952-958,1325-1329`
- Backend renewal: `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:784-797,1073-1082`
- Tests: `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx:951-1000`

### QA-052-002 - Closed

Standalone and reconciliation Billing Agreement reads share one generation
owner. Reconciliation retires and aborts an older standalone retry before it
can commit. Both late success and late failure are covered.

- Frontend: `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:382-414,462-562`
- Tests: `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx:283-327`

### QA-052-003 - Closed

The confirmation date now matches both backend reactivation branches. A
cancelled Agreement quotes the grace-extended Subscription `expiresAt`; an
already active Agreement quotes its retained `nextBillingAt`; a missing active
Agreement date keeps reactivation disabled. The regression uses distinct
pre-grace and grace-extended dates.

- Frontend: `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:250-257,952-958,1373-1396`
- Backend: `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:241-257`
- Tests: `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx:840-949`

## Findings

### P2 - QA-052-004: The cycle-only test uses an impossible pending-plan shape and misses blocked valid reactivation

**Pointers**

- Frontend amount resolution: `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:239-247`
- Frontend disabled control: `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:952-958,1325-1329`
- False-positive fixture: `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx:951-967`
- Backend pending state creation: `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:182-183`
- Entity and response shape: `src/main/java/com/atstudio/atstudio/entity/UserSubscription.java:54-57`, `src/main/java/com/atstudio/atstudio/dto/subscription/UserSubscriptionResponse.java:21-33`
- Active-only plan list: `src/main/java/com/atstudio/atstudio/service/SubscriptionService.java:23-37`
- Backend renewal target: `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:1073-1082`

**Evidence**

The backend creates every scheduled change with
`schedulePendingChange(newPlan, cycle)`. For a cycle-only change, `newPlan` is
the current plan, and the response therefore contains
`pendingSubscriptionId == subscription.id` together with the pending cycle.
The new test instead sets `pendingSubscriptionId` to `null`, so its passing
"pending cycle only" row does not exercise the backend response contract.

`getReactivationAmount` uses the embedded current Subscription only when
`pendingSubscriptionId` is null. For the real cycle-only shape it looks solely
in the public plan list. That list contains active plans only. If the current
plan is deactivated after the cycle-only change was scheduled, the UI already
has the authoritative current plan and prices in `sub.subscription` but treats
the renewal target as unresolved and disables `Keep subscription`. The backend
reactivation branch does not reject an inactive current plan, and renewal still
uses the stored pending Subscription and pending cycle.

**Reproduction**

1. Schedule a cycle-only change while the current plan is active. The backend
   stores the current plan as `pendingSubscription` and returns its ID.
2. Deactivate that plan while the user's paid Subscription remains in force.
3. Cancel the Subscription during its valid grace period with a reusable
   cancelled Billing Agreement.
4. Load Manage. `GET /subscriptions` omits the inactive current plan, while
   `GET /user-subscriptions/me` still embeds that plan and returns its ID as
   `pendingSubscriptionId`.
5. The frontend cannot find the ID in `plans`, computes a null amount, and
   disables `Keep subscription`; no confirmation or reactivation call is
   possible even though the backend state remains reactivatable.

**Impact**

A valid grace-period subscriber can be prevented from restoring automatic
renewal after an administrative plan deactivation. The passing cycle-only test
overstates coverage because it uses a state the backend scheduler path does not
emit.

**Required correction**

Treat `pendingSubscriptionId === sub.subscription.id` as the current embedded
plan when resolving the renewal amount. Replace the cycle-only fixture with the
actual backend response shape and add a regression where the current plan is
absent from the active-plan list.

## Acceptance Matrix

| Canonical root | Result   | Round-three evidence |
| -------------- | -------- | -------------------- |
| `CR-031-085`   | PASS     | Plan and Subscription reads retain abort plus generation ownership; exact `403 NO_ACTIVE_SUBSCRIPTION` remains the only absence result; loading, empty, error, retry, and late-audience tests pass. |
| `CR-031-086`   | PASS     | Exact Billing Agreement absence, retryable errors, preview recovery, selection ownership, and stale retry success/failure after reconciliation pass. |
| `CR-031-087`   | PASS     | Required checkout and return fields remain single-valued and allowlisted before prepare; invalid and duplicate cases retain zero prepare or confirm calls. |
| `CR-031-088`   | PASS     | Prepare failure is terminal and retryable, the initial CTA names registration plus first charge, and fail callbacks suppress raw or blank Provider message text. |
| `CR-031-089`   | **FAIL** | Confirmation cancel/approve, date branches, amount precedence, missing-date fail-closed behavior, and mutation fencing pass, but `QA-052-004` blocks valid cycle-only reactivation and is hidden by an impossible fixture. |

## Focused Behavior Matrix

| Check | Result | Evidence |
| ----- | ------ | -------- |
| Cancelled Agreement with grace-extended expiry | PASS | Confirmation quotes Subscription `expiresAt`, not stale Agreement date. |
| Active Agreement with different Subscription expiry | PASS | Confirmation retains Agreement `nextBillingAt`. |
| Active Agreement with missing date | PASS | Reactivation remains disabled. |
| No pending renewal target | PASS | Current plan and cycle price is quoted. |
| Pending plan and cycle / plan only | PASS | Pending target controls the amount when resolvable. |
| Actual cycle-only response, current plan still active | PASS from source | Current-plan ID resolves through the active plan list, though the test uses the wrong null-ID shape. |
| Actual cycle-only response, current plan inactive | **FAIL** | Embedded current plan is ignored and reactivation is disabled. |
| Stale Billing Agreement retry success/failure | PASS | Older retry cannot replace cancellation reconciliation. |
| Rapid mutation attempts and ambiguous recovery | PASS | Synchronous mutation ownership and recovery blocking prevent replay. |

## Tests And Commands

Executed from `frontend/`:

```text
npm test -- src/api/domainApis.test.ts src/pages/public/SubscriptionPlanPage.test.tsx src/pages/subscriber/SubscriptionPaymentPage.test.tsx src/pages/subscriber/SubscriptionManagePage.test.tsx --reporter=verbose
```

Result: exit 0; 4 test files passed; 193 tests passed; 0 failed; duration
8.78 seconds. The suites mocked frontend APIs and Provider SDK behavior. The
passing result does not invalidate `QA-052-004` because the cycle-only row uses
`pendingSubscriptionId: null` instead of the backend-produced current plan ID.

## Effect Boundary Confirmation

- Reviewed the 14 tracked WI-052 implementation, test, and documentation files
  and only the backend source contracts needed for the five canonical roots.
- Ran only the four focused mocked frontend test files.
- Performed no real Provider, payment, refund, mail, export, download, API, or
  database mutation.
- Did not open, read, hash, or modify protected output paths and did not inspect
  ignored secrets or local environment values.
- Provider response and durable runtime state were not observed. The finding is
  established from current frontend resolution logic and backend source
  contracts, not from a live charge or persisted-state inspection.

## Residual Risks

- Callback and return-query coverage remains representative rather than a
  mathematical enumeration of every encoded input permutation.
- Browser focus, Escape/backdrop behavior, and modal visual layout were not
  exercised in this focused integration review.
- No full frontend gate, backend test suite, browser session, live Provider run,
  or durable-state inspection was performed.
