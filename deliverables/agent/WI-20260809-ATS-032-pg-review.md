---
version: 1.0
last_updated: 2026-08-10
project: ATS
owner: pg
category: agent
status: accepted
dependencies:
  - path: deliverables/agent/WI-20260809-ATS-032-handoff.md
    reason: Bounded PG review scope and acceptance contract
  - path: deliverables/agent/WI-20260809-ATS-027-findings.md
    reason: CR-031-081 and CR-031-082 source findings
---

# PG Review: WI-20260809-ATS-032

## Decision

**APPROVE WITH CONDITIONS. No blocker identified.**

The correction is implementable within the named frontend/API/DTO/service/test boundaries and does not require a schema, architecture, or policy change. This approval is for the bounded implementation contract only. The current baseline is not compliant with that contract and must not be treated as approved behavior until the conditions below are evidenced.

## Threat Boundary

The protected operation begins at `POST /api/payments/billing-agreements/prepare` and ends at the frontend decision to invoke `requestBillingAuth`. Client-controlled query parameters, plan names, plan IDs, audience values, purpose values, cycle values, callback values, and displayed amounts are untrusted context. They may select a requested flow, but they must not authorize a different flow, audience, plan, or amount.

The server is authoritative for the authenticated user, target plan, subscription state, purpose, amount, currency, order identity, and checkout metadata. The frontend is responsible for refusing to continue when route context, request intent, selected plan, or the successful server response are inconsistent. A valid UI state or a successful prepare HTTP response alone is not evidence that Provider authorization may proceed.

## Baseline Evidence

- **CR-031-081:** `BillingAgreementPrepareRequest` currently carries only `subscriptionId` and `billingCycle`; the service derives `SUBSCRIBE` versus `BILLING_AGREEMENT` solely from active subscription state. The service then prepares/saves the agreement, saves the order, and calls the recurring Provider (`BillingAgreementApplicationService.java:107-157`). The frontend derives copy and callback purpose from the URL and can invoke billing auth using that URL-derived purpose (`SubscriptionPaymentPage.tsx:21-30,114-186`).
- **CR-031-082:** The plan page and manage re-registration path currently route with a plan name, while checkout fetches an unfiltered plan list and selects the first case-insensitive name match (`SubscriptionPlanPage.tsx:167-183`, `SubscriptionManagePage.tsx:338-353`, `SubscriptionPaymentPage.tsx:120-136`).
- The backend already checks the selected plan's `userType` against the authenticated user before agreement/order/Provider preparation (`BillingAgreementApplicationService.java:111-124,431-435`). That check is necessary but does not repair lost route identity or missing prepare intent.
- Existing focused tests prove the old contracts: the API test sends only `subscriptionId` and `billingCycle`, and the checkout test expects URL purpose to be used in the billing-auth callback (`domainApis.test.ts:235-241`, `SubscriptionPaymentPage.test.tsx:153-168`). They do not close the required mismatch matrix.

## Required Conditions

### 1. Explicit prepare intent

Add an explicit prepare intent/purpose to the prepare request. The accepted values for this boundary are only `SUBSCRIBE` and `BILLING_AGREEMENT`; missing, malformed, or `UPGRADE` intent must fail validation. The request intent is a consistency claim, not an authority source.

Before any agreement preparation, agreement write, order write, or Provider preparation, the server must derive the authoritative intent from current authenticated subscription state and compare it with the requested intent:

- No active or grace-period subscription: only `SUBSCRIBE` is valid.
- Active/grace-period subscription: only `BILLING_AGREEMENT` for the current plan and current billing cycle is valid.
- Both mismatch directions must reject without a billing agreement write, payment-order write, or Provider call.

The server must derive the amount from the exact server-loaded plan and billing cycle: the full server price for `SUBSCRIBE`, and zero for `BILLING_AGREEMENT`. No client amount is accepted as authority.

### 2. Exact plan identity and audience binding

Plan/manage entry points must carry the immutable plan ID and selected audience into checkout. A plan name may remain as display context only; it must never select a plan or determine authorization. Any actionable return context must follow the same rule.

Checkout must select by exact plan ID and verify the fetched plan's audience against the authenticated audience before prepare. It must fail closed when route audience and authenticated audience differ. The backend must independently verify the exact plan's `userType` against the authenticated user; if an audience claim is included in the prepare request, it must also equal both the authenticated audience and the exact plan audience. A same-name plan in the other audience must be rejected even when the requested name matches.

The backend must continue to use the authenticated principal as the access-control authority. Route audience and request audience are claims to cross-check, never a substitute for authentication or server-side plan validation.

### 3. Fail-closed ordering

For every invalid intent, audience, plan, cycle, state, or server-contract condition, the rejection must occur before these effects:

1. Billing-agreement preparation or mutation, including creation of a new agreement or mutation of an existing managed agreement.
2. Payment-order persistence.
3. Recurring Provider preparation.
4. Frontend `requestBillingAuth` invocation.

Read-only lookups needed to make the decision are allowed. For a valid request, evidence must show all state and identity checks complete before agreement/order/Provider side effects, and the Provider prepare call occurs only after the order has the server-derived purpose and amount. No live Provider call is required or permitted for this WI.

### 4. Successful-response gate before billing auth

The frontend must treat the successful prepare response as authoritative and retain it as the only actionable checkout state. Before enabling or invoking billing auth, verify at minimum:

- response purpose equals the requested intent;
- response `subscriptionId` equals the exact selected plan ID;
- response billing cycle equals the requested cycle;
- response amount is the expected server price for `SUBSCRIBE` or exactly zero for `BILLING_AGREEMENT`;
- response currency and Provider are the expected supported values;
- checkout metadata required by the SDK is present and internally consistent; and
- response/selected-plan audience remains consistent with the authenticated audience.

Any mismatch clears or invalidates the actionable order state, leaves the billing-auth control disabled, shows recoverable error state, and proves `requestBillingAuth` was not called. Callback URLs must use the validated response purpose and amount, never the URL-only purpose.

## Required Negative Tests

### Backend and controller/API contract

- Missing, malformed, and unsupported prepare intent, including `UPGRADE`, reject before agreement save, order save, and Provider prepare.
- Non-subscriber requesting `BILLING_AGREEMENT` rejects before all three side effects.
- Active/grace-period subscriber requesting `SUBSCRIBE` rejects before all three side effects.
- Active subscriber requesting a different plan ID or billing cycle rejects before all three side effects.
- Authenticated `INDIVIDUAL` requesting a same-name `BUSINESS` plan ID, and the inverse, rejects before all three side effects.
- A request with a mismatched audience claim rejects even when the plan name is the same.
- Valid `SUBSCRIBE` and valid `BILLING_AGREEMENT` cases assert purpose, exact amount, plan ID, cycle, and one Provider preparation only after the preconditions pass.
- Use interaction/order assertions, not only exception assertions: rejected cases must verify no agreement write, no payment-order write, and no Provider preparation; valid cases must verify the required side-effect order.

### Frontend

- Plan and manage entry actions carry exact plan ID and audience; duplicate names across audiences never cause first-match selection.
- Route audience differing from authenticated audience stops before prepare and never calls `requestBillingAuth`.
- Exact plan ID not found, exact plan audience mismatch, missing required identity, and unsupported intent stop before prepare.
- Prepare request includes the explicit intent and does not include a client-authoritative amount.
- A successful response with mismatched purpose, plan ID, cycle, amount, currency, Provider, or audience leaves billing auth disabled and proves `requestBillingAuth` was not called.
- Correct `SUBSCRIBE` uses the returned full amount; correct `BILLING_AGREEMENT` uses returned zero amount and returned purpose in callback URLs.

## Evidence Separation

The completion evidence must label these lanes separately:

- **UI:** displayed purpose/amount, enabled or disabled control, and recovery state.
- **Request:** exact route context and serialized prepare request, including explicit intent.
- **Server:** authenticated audience, exact plan lookup, derived intent, validation decision, response purpose/amount, and side-effect ordering.
- **Provider:** for negative cases, a test-double non-invocation assertion; for valid focused tests, at most the isolated mocked prepare call. Do not claim Toss or live Provider evidence.
- **Durable state:** repository interaction assertions or isolated test persistence only. Do not claim production or runtime database state, and do not infer durable absence from UI behavior alone.

## Residual Limits and Gate

This review does not cover prepare idempotency/duplicate-order control (`WI-033`), callback unknown-outcome recovery (`WI-034`), live charges, refunds, deployment, runtime, database, or Provider verification. The conditional approval becomes implementation-acceptable only after the focused negative tests and cross-layer evidence above are present and the existing regression lanes pass without real Provider side effects.
