---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: qa-integ
category: qa-result
status: complete
dependencies:
  - path: WI-20260809-ATS-052-qa-handoff.md
    reason: Independent review scope and output contract
  - path: WI-20260809-ATS-052-handoff.md
    reason: WI scope and acceptance criteria
---

# Independent QA Result: WI-20260809-ATS-052

## Verdict

**FAIL**

Two open defects remain: one P1 financial-confirmation defect and one P2 stale
read-ownership defect. A PASS is therefore not permitted.

## Findings

### P1 - QA-052-001: Reactivation confirmation can quote the wrong next renewal amount

**Pointers**

- `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:886-909,1311-1324`
- `src/main/java/com/atstudio/atstudio/entity/UserSubscription.java:54-61,84-90`
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:784-797,1073-1082`
- `docs/design/payment-integration-design.md:299-301`
- `docs/design/usecase/user-subscription.md:141-144`
- `docs/payment/user-flows.md:209-213`

**Evidence**

The new confirmation always derives `reactivationAmount` from the current
Subscription plan and current cycle. Cancellation and reactivation preserve a
pending plan/cycle change. The renewal command instead selects the pending plan
and pending cycle when present and prices the renewal from that target. The
three changed documents claim that the confirmation names the next billing
amount, so the documentation is also false for this supported state.

**Reproduction**

1. Return a reusable cancelled Billing Agreement and a `CANCELLED` current
   `STANDARD/MONTHLY` subscription priced at 9,900.
2. On that subscription, return `pendingSubscriptionId` for `DELUXE` and
   `pendingBillingCycle=YEARLY`; return the DELUXE yearly price as 199,000 in
   the plan list.
3. Open `subscriptions/manage` and select `Keep subscription`.
4. The modal states that 9,900 will be charged because it reads the current
   plan/cycle.
5. The next renewal order is priced at 199,000 because the backend uses the
   pending DELUXE/YEARLY target.

**Impact**

The explicit financial confirmation can understate or otherwise misstate the
charge that reactivation enables. This defeats the purpose of the added consent
step.

**Required correction**

Derive the displayed amount from the same pending-plan/pending-cycle precedence
as renewal, and add a regression covering pending plan, pending cycle-only, and
no-pending states.

### P2 - QA-052-002: A Billing Agreement retry can overwrite newer post-mutation truth

**Pointers**

- `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:350-383,433-514,1245-1258`
- `docs/design/payment-integration-design.md:288-297`
- `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx:214-280`

**Evidence**

`loadBillingAgreement()` fences only requests started through that callback.
`reconcileOperation()` performs a separate Billing Agreement read and commits
its canonical result without aborting the retry controller or advancing
`billingReadVersionRef`. Billing-read loading is also absent from
`mutationBlocked`, so cancellation and other permitted mutations can start
while the retry is pending. The existing late-read test covers an audience
reload, not a retry overlapping mutation reconciliation.

**Reproduction**

1. Load an `ACTIVE` subscription and make the initial Billing Agreement GET
   fail with a retryable 5xx.
2. Click the Billing Agreement retry and hold that GET pending with an old
   `ACTIVE` snapshot.
3. While it is pending, cancel the subscription. Let the mutation and its
   canonical reads complete with `CANCELLED` Subscription and Billing Agreement
   state.
4. Resolve the older retry. Its version is still current, so lines 370-373
   replace the newer canonical Billing Agreement with the stale `ACTIVE`
   result. A late retry rejection similarly replaces it with an error state.

**Impact**

The Manage page can contradict the newly committed cancellation/change state
and can expose controls from stale payment-method state. This violates the
changed documentation's latest-owner claim.

**Required correction**

Use one ownership generation for every Billing Agreement projection, or abort
and retire any standalone Billing Agreement read before mutation/reconciliation
commits. Cover stale retry success and failure after a newer canonical read.

No additional P0 or P3 finding was identified.

## Acceptance Matrix

| Canonical root | Result   | Independent evidence                                                                                                                                                                              |
| -------------- | -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `CR-031-085`   | PASS     | Plan and Subscription reads use one abort/version owner; only exact `403 NO_ACTIVE_SUBSCRIPTION` becomes absence; loading, empty, error, and retry states are distinct. Late audience tests pass. |
| `CR-031-086`   | **FAIL** | Exact `404 BILLING_AGREEMENT_NOT_FOUND` handling and preview recovery pass, but QA-052-002 leaves a retry-versus-reconciliation stale completion open.                                            |
| `CR-031-087`   | PASS     | Required checkout and return fields are single-valued and allowlisted before prepare. Static enumeration and focused tests confirm the zero-prepare boundary described below.                     |
| `CR-031-088`   | PASS     | Prepare failure renders terminal `ERROR` copy with retry; initial CTA discloses registration plus first charge; fail callback ignores raw `message` and relies on bounded outcome copy.           |
| `CR-031-089`   | **FAIL** | Cancel and explicit approval paths exist, but QA-052-001 makes the confirmation financially inaccurate when a pending change exists.                                                              |

## Query And Call Boundary

| Input group                        | Missing / blank / invalid / duplicate result                                                                                                                                                    |
| ---------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `planId`                           | Missing, blank, non-digit, zero, unsafe, or duplicate values stop before plan fetch and prepare. An unknown positive ID performs the plan read but makes zero prepare calls.                    |
| `userType`                         | Missing, blank, unsupported, duplicate, or authenticated-audience mismatch makes zero prepare calls.                                                                                            |
| `billingCycle`                     | Missing, blank, unsupported, or duplicate values make zero prepare calls.                                                                                                                       |
| `purpose`                          | Missing, blank, `UPGRADE`/unsupported, or duplicate values make zero prepare calls.                                                                                                             |
| Callback `orderId`                 | Missing or blank cannot confirm; duplicates are malformed. A fail callback without an order ID performs no API call.                                                                            |
| Callback `authKey` / `customerKey` | Blank, partial, or duplicate authentication context makes zero confirm calls. A sanitized revisit with neither key is intentionally read-only.                                                  |
| Callback `amount` / `purpose`      | Missing, blank, negative, fractional, unsafe, unsupported, or duplicate required confirmation state makes zero confirm calls.                                                                   |
| Return context                     | Any partial tuple, blank/invalid/duplicate field, audience mismatch, unknown plan, or use with `SUBSCRIBE` makes zero prepare calls. A valid complete tuple is navigation/display context only. |

No invalid case above loads the Toss SDK. Outcome-only callback recovery is a
read boundary and does not call prepare or confirm.

## Tests And Commands

Executed from `frontend/`:

```text
npm test -- src/api/domainApis.test.ts src/pages/public/SubscriptionPlanPage.test.tsx src/pages/subscriber/SubscriptionPaymentPage.test.tsx src/pages/subscriber/SubscriptionManagePage.test.tsx --reporter=verbose
```

Result: exit 0; 4 test files passed; 185 tests passed; 0 failed; duration
10.30 seconds. All payment/provider functions in these suites were mocked.

The passing suite does not exercise either finding reproduction. It also has no
isolated reactivation-confirm double-click test; the synchronous mutation ref
and generic rapid cross-mutation test provide source-level evidence only.

## Effect Boundary Confirmation

- Reviewed the 14 tracked WI-052 implementation/test/document changes and the
  handoff's required evidence pointers.
- Read only relevant unchanged frontend helpers and backend source needed to
  verify documented error and renewal-pricing contracts; no such code was
  changed.
- Ran mocked focused frontend tests only.
- Performed no real Provider, payment, refund, mail, export, download, API, or
  database mutation.
- Did not inspect protected output paths, the intentional ZIP, ignored secrets,
  or local environment values.
- Provider response and durable runtime state were not observed. The renewal
  mismatch is established from the current server order-construction source,
  not from a live charge.

## Residual Risks

- Callback and return-query combinations were statically enumerated, but the
  focused suite tests representative rather than every blank/invalid/duplicate
  permutation.
- Browser focus, Escape/backdrop behavior, and visual layout of the new modal
  were not exercised; they are outside the functional closure reviewed here.
- No full frontend gate, backend test, browser session, or live acceptance run
  was performed in this independent focused review.
