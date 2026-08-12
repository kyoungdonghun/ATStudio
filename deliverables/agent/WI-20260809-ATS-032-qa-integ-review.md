---
version: 1.0
last_updated: 2026-08-10
project: ATS
owner: qa-integ
category: agent
status: accepted_with_conditions
dependencies:
  - path: deliverables/agent/WI-20260809-ATS-032-handoff.md
    reason: Bounded acceptance contract and source/test pointers
  - path: deliverables/agent/WI-20260809-ATS-032-pg-review.md
    reason: Fail-closed security conditions
---

# QA-INTEG Review: WI-20260809-ATS-032

## Decision

**APPROVE WITH CONDITIONS. No schema, architecture, or policy blocker is identified.**

The bounded implementation may proceed only with the contract, ordering, and
test evidence below. This is a pre-implementation cross-layer review. The
current baseline is not approved behavior: checkout selects a plan by name,
the prepare request lacks an explicit intent, and the SDK callback uses
URL-derived purpose rather than a validated prepare response.

This review covers only CR-031-081 and CR-031-082. It does not add prepare
idempotency (WI-033), callback/unknown-outcome recovery (WI-034), live Toss
activity, runtime/database inspection, or a payment policy change.

## Required Contract

### Checkout route

Every non-callback checkout entry must require these query parameter names:

```
/subscriptions/checkout?planId=<positive-integer>&userType=<INDIVIDUAL|BUSINESS>&billingCycle=<MONTHLY|YEARLY>&purpose=<SUBSCRIBE|BILLING_AGREEMENT>
```

- `planId`, `userType`, `billingCycle`, and `purpose` are all required route
  context. `planName` is display-only and must never select a plan.
- The entry page must resolve `planId` exactly, verify the resolved plan
  `userType` equals the route `userType`, and verify both equal the
  authenticated user's `userType` before prepare.
- A missing, malformed, unsupported, or inconsistent route value is a local
  recoverable error: clear actionable checkout state, keep billing auth
  disabled, offer return to plan/manage, and do not call prepare or the SDK.
- Any action-oriented return context for payment-method re-registration must
  carry immutable return plan identity and audience as well. A return name is
  display-only; WI-032 must not execute the upgrade itself.

### Prepare API request

`POST /api/payments/billing-agreements/prepare` must accept exactly this
payment-intent input for this boundary:

```json
{
  "subscriptionId": 123,
  "billingCycle": "MONTHLY",
  "purpose": "SUBSCRIBE"
}
```

- `purpose` is **required**. Its accepted values are only `SUBSCRIBE` and
  `BILLING_AGREEMENT`; missing, malformed, and `UPGRADE` values must fail
  before service side effects.
- `userType` is **not required and should not be accepted in this API body**.
  It is a route/UI consistency claim, while the authenticated principal and
  server-loaded plan are the authoritative audience sources. Omitting it from
  the DTO avoids representing a duplicate client-controlled authority.
- Do not accept `amount`, plan name, callback URLs, raw payment credentials,
  or client checkout metadata. The client has no amount authority.
- The server must load the exact `subscriptionId`, validate its `userType`
  against the authenticated user, find the service-enabled subscription state,
  derive the expected purpose, then compare that result with the requested
  `purpose` before agreement preparation, order persistence, or recurring
  Provider preparation.

### Authoritative server decision

The server decision sequence is:

1. Resolve the authenticated USER principal and exact plan ID.
2. Reject plan audience mismatch against the authenticated principal.
3. Resolve a service-enabled subscription: `ACTIVE`, or `CANCELLED` with
   `expiresAt >= today`, is an active/grace-period subscription.
4. Derive expected purpose: no such subscription -> `SUBSCRIBE`; otherwise
   -> `BILLING_AGREEMENT`.
5. Compare requested purpose with expected purpose. For
   `BILLING_AGREEMENT`, also require the current plan ID and current billing
   cycle. Apply existing subscription preconditions, including BUSINESS
   certification, before mutation.
6. Derive amount from the server-loaded plan and cycle: full plan price for
   `SUBSCRIBE`, exactly `0` for `BILLING_AGREEMENT`; set provider `TOSS` and
   currency `KRW` server-side.
7. Only after all preceding checks: prepare the billing agreement, persist the
   payment order, and invoke the isolated recurring Provider prepare operation.

Rejected cases must prove no agreement write/mutation, no payment-order save,
and no recurring Provider prepare invocation. Valid cases must prove the
checks occur before those effects and use test doubles only.

### Prepare-response gate and callback construction

The frontend may retain a prepare response as actionable only when all checks
pass:

- nonblank order ID;
- response `subscriptionId` equals the exact route/resolved plan ID;
- response `billingCycle` equals requested `billingCycle`;
- response `purpose` equals requested purpose;
- `SUBSCRIBE` amount equals the resolved exact plan price for that cycle, and
  `BILLING_AGREEMENT` amount is exactly zero;
- response provider is `TOSS` and currency is `KRW`;
- checkout type is `TOSS_BILLING_AUTH`, method is `CARD`, and `clientKey`,
  `customerKey`, `successUrl`, and `failUrl` are present and usable as the
  server-issued checkout metadata.

The local plan price is a consistency assertion only; checkout copy and SDK
arguments must use the validated response purpose and amount. The callback
URLs must be built from the validated response's `orderId`, `amount`, and
`purpose`, never a URL-only purpose. Do not log raw credentials or payment
authorization values.

On a response-gate failure, clear `paymentOrder`, leave the billing-auth
control disabled, show a recoverable preparation error, and prove
`requestBillingAuth` was not invoked. This UI proof does not prove that a
server prepare order was absent: a response-mismatch test is a frontend test
double case after the prepare API was called.

## UI To Server Matrix

| Case | UI and route context | API invocation | Authoritative server decision and response | SDK/callback | Provider and durable evidence required |
| --- | --- | --- | --- | --- | --- |
| Valid new INDIVIDUAL | Selected INDIVIDUAL card routes with its exact `planId`, `userType=INDIVIDUAL`, selected cycle, and `purpose=SUBSCRIBE`. | One prepare request with exact ID, cycle, and `SUBSCRIBE`; no amount or user type in the body. | Principal and exact plan are INDIVIDUAL; no active/grace subscription; derive `SUBSCRIBE`; return that purpose, full server price, `TOSS`, `KRW`, and CARD checkout metadata. | Show validated full response amount; enabled control may invoke one mocked `requestBillingAuth` with response purpose/amount. | Service test-double proves one Provider prepare after agreement/order path. Repository assertions prove expected test persistence only; no live/provider/runtime claim. |
| Valid new BUSINESS | Selected BUSINESS card routes with its exact BUSINESS `planId`, `userType=BUSINESS`, selected cycle, and `purpose=SUBSCRIBE`. | Same request shape, with BUSINESS plan ID. | Principal and plan match BUSINESS; existing certification precondition passes; no active/grace subscription; derive `SUBSCRIBE` and server price. | Same response gate and CARD SDK call as INDIVIDUAL. | Test Provider only. Assert no INDIVIDUAL plan lookup or name-first selection; durable evidence is isolated repository interaction only. |
| Valid active re-registration | Manage page routes the current plan ID, authenticated audience, current cycle, and `purpose=BILLING_AGREEMENT`. | One prepare request with current plan ID/cycle and `BILLING_AGREEMENT`. | `ACTIVE` subscription resolves; current ID/cycle match; derive `BILLING_AGREEMENT`; return zero amount. | Render zero immediate payment only after response validation; callback includes validated `purpose=BILLING_AGREEMENT` and `amount=0`. | Mocked Provider prepare may occur once after checks. Order capture proves zero and billing-agreement purpose in test only. |
| Valid CANCELLED grace re-registration | The same manage action is available while `CANCELLED` and `expiresAt >= today`; route remains current exact ID/audience/cycle with `BILLING_AGREEMENT`. | Same as active re-registration. | Repository's service-enabled query treats the grace subscription as current; current ID/cycle match; derive `BILLING_AGREEMENT`, amount zero. | Same validated zero-amount CARD authorization path. | Test fixture must explicitly use CANCELLED grace status. No real subscription reactivation, charge, or database action is evidence for this WI. |
| Mismatch: non-subscriber requests billing agreement | Route/request claims `BILLING_AGREEMENT`; UI must not relabel a subscription flow as zero payment without a validated response. | Direct API negative test sends `BILLING_AGREEMENT`; frontend test may send it only where route is otherwise valid. | No active/grace subscription derives `SUBSCRIBE`; reject requested-purpose mismatch before agreement/order/Provider operations. | No actionable response; control disabled; SDK not invoked. | Backend verifies zero agreement writes, zero order saves, zero Provider prepares. Frontend verifies no SDK invocation; neither claims durable runtime absence. |
| Mismatch: active or grace subscriber requests subscribe | Route/request claims `SUBSCRIBE` for a current ACTIVE or CANCELLED-grace subscription. | Direct API negative test sends `SUBSCRIBE`. | Current service-enabled subscription derives `BILLING_AGREEMENT`; reject before agreement/order/Provider operations. | No actionable response; no SDK call. | Same no-side-effect interaction assertions as the opposite direction. Include both ACTIVE and CANCELLED-grace state coverage across focused/adjacent tests. |
| Same-name cross-audience: INDIVIDUAL user versus BUSINESS plan | Duplicate name is allowed as display text, but a route that claims BUSINESS for an INDIVIDUAL principal fails UI audience cross-check before prepare. A crafted direct request can name the BUSINESS plan ID. | UI: no prepare. API: direct request contains only that exact BUSINESS plan ID, cycle, and purpose. | Server validates exact plan `userType` against authenticated INDIVIDUAL principal and rejects before state/side effects. | No SDK call. | UI test proves API and SDK non-invocation. Service test proves no agreement/order/Provider interaction. Do not infer one from the other. |
| Same-name cross-audience: BUSINESS user versus INDIVIDUAL plan | Symmetric route/API case with the same display name and INDIVIDUAL plan ID. Valid BUSINESS selection must instead retain its BUSINESS ID. | UI: no prepare for mismatch; valid BUSINESS route prepares only its exact ID. | Server rejects INDIVIDUAL plan for BUSINESS principal before effects; valid BUSINESS flow remains covered separately. | No SDK call for mismatch. | Same split evidence as the reverse direction. This closes first-name-match selection rather than relying on list order. |

## Response-Mismatch Matrix

Each row below is a frontend prepare-response test-double scenario. The
prepare request may already have reached the server; therefore the required
non-invocation proof is for the Toss SDK, not a claim that no server order or
Provider prepare could have occurred.

| Mismatched response field | UI/API response gate | SDK and recovery | Durable/Provider evidence boundary |
| --- | --- | --- | --- |
| `subscriptionId` | Response ID differs from the exact route/resolved plan ID. Clear actionable state. | Disabled control; `requestBillingAuth` not called; recover to plan/manage. | Mocked frontend response only. No durable-state conclusion. |
| `billingCycle` | Response cycle differs from requested cycle. Clear actionable state. | Disabled control; no SDK call. | Mocked frontend response only. |
| `purpose` | Response purpose differs from the required request purpose. Never render copy from the URL to override it. | Disabled control; no SDK call; no callback is built. | Mocked frontend response only. |
| `amount` | Nonzero billing-agreement amount, zero/nonmatching subscribe amount, non-numeric, or unsafe amount fails. | Disabled control; no SDK call. | Mocked frontend response only; server amount remains authoritative, not a client input. |
| `provider` or `currency` | Provider is not `TOSS` or currency is not `KRW`. | Disabled control; no SDK call. | Mocked frontend response only; no live Provider assertion. |
| Checkout metadata | Type is not `TOSS_BILLING_AUTH`, method is not `CARD`, or any required checkout field is absent/unusable. | Disabled control; do not load/invoke the SDK; show recoverable preparation error. | Mocked frontend response only. Do not log keys, callback payloads, or credentials. |

## Focused And Adjacent Test Inventory

### Focused frontend

- `SubscriptionPlanPage.test.tsx`: INDIVIDUAL and BUSINESS selections emit
  exact `planId`, `userType`, `billingCycle`, and `purpose=SUBSCRIBE`; duplicate
  names never determine the selected ID.
- `SubscriptionManagePage.test.tsx`: ACTIVE and CANCELLED-grace
  re-registration emit the current exact plan ID/audience/cycle and
  `purpose=BILLING_AGREEMENT`; preserved return context uses identity rather
  than a plan name for an actionable preview.
- `SubscriptionPaymentPage.test.tsx`: route validation and audience mismatch
  stop before prepare; exact ID resolution is used; body includes required
  `purpose` but excludes amount and user type; all valid rows above validate
  response/copy/callbacks; every response-mismatch row disables SDK auth.
- `domainApis.test.ts`: exact POST body contract, including required purpose
  and absence of client amount/user type.

### Focused backend/controller

- `PaymentControllerTest`: missing/malformed/unsupported purpose, including
  `UPGRADE`, returns validation failure before service invocation; accepted
  JSON contract serializes the prepare response fields used by the client.
- `BillingAgreementApplicationServiceTest`: valid new INDIVIDUAL/BUSINESS and
  valid ACTIVE/CANCELLED-grace re-registration assert exact purpose, plan ID,
  cycle, amount, `TOSS`, and `KRW`.
- The same service test must cover both purpose/state mismatch directions,
  wrong current plan/cycle for re-registration, and both same-name
  cross-audience directions. Rejected cases verify no agreement repository
  save/mutation, no payment-order save, and no recurring Provider prepare.
- Valid service tests use interaction ordering to prove validation precedes
  agreement/order/Provider effects and use only the recurring Provider test
  double.

### Adjacent regression lanes

- `SubscriptionPaymentReplay.test.tsx` remains an adjacent callback-history
  regression only. Do not extend it into WI-034 response-loss or unknown-outcome
  recovery.
- Existing payment authorization/controller role-denial tests remain adjacent:
  unauthenticated and ADMIN/mixed-role requests must still be rejected before
  controller/service work.
- Existing subscription manage preview/change tests remain adjacent: a
  payment-method re-registration returns to the selected preview but does not
  execute the upgrade in this WI.

No tests, build, runtime, database, Provider, or Git actions were executed for
this review. Completion evidence must separately report UI control/copy, API
request and response, server decision ordering, Provider test-double
interaction, and isolated durable test evidence.

## Source Basis

- `frontend/src/pages/public/SubscriptionPlanPage.tsx` currently routes by
  `plan` name.
- `frontend/src/pages/subscriber/SubscriptionManagePage.tsx` currently routes
  payment-method re-registration by plan name and URL purpose.
- `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx` currently
  selects the first case-insensitive plan-name match, sends no purpose in the
  prepare body, and uses URL purpose for callback construction.
- `BillingAgreementPrepareRequest` currently has only `subscriptionId` and
  `billingCycle`; `BillingAgreementApplicationService` derives purpose from
  current subscription state before its present agreement/order/Provider path.
- `UserSubscriptionRepository.findActiveByUser` treats `ACTIVE` and
  non-expired `CANCELLED` subscriptions as service-enabled, which is the
  required grace-period state for this boundary.
