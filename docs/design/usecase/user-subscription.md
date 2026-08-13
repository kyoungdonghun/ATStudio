---
version: 3.5
last_updated: 2026-08-14
project: ATS
owner: docops
category: design
status: stable
dependencies:
  - path: ../api-spec.md
    reason: Current subscription and payment routes
  - path: ../payment-integration-design.md
    reason: Recurring payment lifecycle
  - path: ../db-schema.md
    reason: Subscription persistence
---

# User - Subscription Use Cases

## PAYMENT-001: Subscribe

1. A USER selects an active plan and cycle.
2. BUSINESS users must have approved company certification.
3. `/subscriptions/checkout` starts Toss billing auth.
4. The backend issues and encrypts the billing key.
5. The first recurring charge succeeds.
6. Only then does the backend create or activate `user_subscriptions`.

There is no direct subscription-creation endpoint or alternate checkout path.

## PAYMENT-002: List Plans

- `GET /api/subscriptions`: public active plans.
- `GET /api/subscriptions/{subscriptionId}`: public plan detail.
- `GET /api/subscriptions/admin`: ADMIN list including inactive plans.

The ADMIN plan table displays the plan name together with its `userType`
audience, monthly and yearly prices, download/channel limits,
`maxPlaylists`, and status. `maxPlaylists = -1` is displayed as unlimited.
Audience remains visible so same-name INDIVIDUAL and BUSINESS plans are
distinguishable.
At widths up to 767 px, the table retains all eight fields behind its existing
horizontal scroll container and a stable minimum table width. Mobile styling
does not positionally hide any price, limit, audience, or status field.

The six runtime baseline plans are owned by `seed.sql`.

## PAYMENT-004: List Member Subscriptions (ADMIN)

`GET /api/user-subscriptions` returns the ADMIN subscription list used by the
management screen. There is no separate ADMIN detail-read endpoint.

## PAYMENT-006: View My Subscription

`GET /api/user-subscriptions/me` returns the current service-enabled
subscription. Service-enabled includes `ACTIVE` and a valid `CANCELLED`
grace period before `expiresAt`.

Only HTTP `403 NO_ACTIVE_SUBSCRIPTION` is displayed as no current
subscription. Other authorization, server, and network failures remain
retryable errors. Audience changes retire earlier plan/subscription reads so a
late completion cannot overwrite the current audience.

## PAYMENT-007: Change My Subscription

`PUT /api/user-subscriptions/me` applies these rules:

- upgrade: charge the prorated difference through the active billing agreement,
  then apply the higher plan;
- downgrade or cycle-only change: store a pending change for the next successful
  renewal;
- current plan/cycle: clear a pending change;
- removed/invalid billing key: expire local agreement metadata and require
  payment-method registration without mutating the subscription.

The management UI treats only HTTP `404 BILLING_AGREEMENT_NOT_FOUND` as no
registered Billing Agreement. Other read failures remain visible and
retryable. Change-preview failures are also visible and retryable; a retired
preview completion cannot replace a newer selection.

## PAYMENT-012: General ADMIN Local Subscription Correction

Direct ADMIN `PUT`/`DELETE /api/user-subscriptions/{id}` mappings are retired.
The management screen uses this explicit local workflow:

1. `POST /api/admin/user-subscription-corrections/preview` validates a complete
   target plan, cycle, status, expiration date, pending-change action, and
   optional local billing-agreement cancellation.
2. `POST /api/admin/user-subscription-corrections` creates a `REQUESTED` row
   with a required operator reason.
3. `POST .../{correctionId}/approve` records explicit approval.
4. `POST .../{correctionId}/execute` reacquires ordered pessimistic locks,
   revalidates snapshots and payment-order safety, applies the local target,
   and records success or failure.
5. `GET .../open?userSubscriptionId={id}` resumes a non-terminal workflow and
   returns 204 when none exists. List/detail reads use `dataList/pageInfo` or
   `data` as appropriate.

The SPA treats an HTTP 4xx mutation response as a definite rejection and keeps
the stable response error without reconciliation. A network, timeout, or other
no-response failure and an HTTP 5xx response have an ambiguous commit outcome,
so the SPA performs one bounded read. Request reconciliation uses the
non-terminal open-state endpoint; its 204 response is inconclusive because a
committed correction may already have advanced. The SPA therefore retains the
draft and preview, keeps the unknown-outcome duplicate fence, and exposes one
read-only status retry. A repeated 204 remains unknown. Approval and execution
use the known correction ID and may converge through detail, including terminal
state. This flow does not poll and adds no backend correlation protocol.

The parent management screen synchronously retains the selected Subscription
row while request, approval, execution, or their bounded recovery read owns the
accepted mutation. Another row cannot replace that target or abort its recovery.
An inconclusive recovery keeps the same owner until an explicit read-only status
retry proves the outcome. All shared close paths stay blocked during the same
ownership interval. Execution alone requires the trimmed exact phrase
`권한 보정 실행`; approval uses the ordinary confirmation and has no
typed-phrase requirement.

One ADMIN may perform all stages; this is explicit single-operator confirmation,
not two-person approval. Mutation locks use these fixed orders:

- request: BillingAgreement -> UserSubscription -> target Subscription ->
  non-terminal correction rows -> actor User;
- approval: correction -> actor User;
- execution: BillingAgreement -> UserSubscription -> target Subscription ->
  correction -> actor User.

The actor is reloaded with the shared pessimistic User-row lock after all
domain/correction locks and immediately before mutation. Withdrawal retains
BillingAgreement -> UserSubscription -> active ADMIN rows. Correction takes no
domain lock after its actor lock, so these paths do not introduce a lock-order
cycle. Duplicate open requests and concurrent execution are rejected or
converge on the persisted workflow state rather than applying the entitlement
twice.

Success audit joins the mutation transaction. Rejection audit uses an
independent transaction and must not replace the original domain error if the
audit write itself fails. Request, approval, and execution `BusinessException`
rejections use phase-appropriate actions and retain only the actor ID when
available, the target UserSubscription or correction ID, the stable error
code, and bounded state. Every rejection row stores a null `reasonNote` and
does not copy request bodies or operator notes. The required request reason and
optional approval/execution notes remain in the authoritative correction
workflow, and the successful correction audit retains its approved operator
reason. The workflow may change local subscription and local billing-agreement
status, but it makes no Toss charge/refund/billing-key-delete or email call.
Refund-linked `payment_entitlement_corrections` remains a separate payment
workflow.

## PAYMENT-010: Cancel My Subscription

`DELETE /api/user-subscriptions/me` stops future renewal while paid access
continues through `expiresAt`.

## PAYMENT-011: Reactivate Grace-Period Subscription

`POST /api/user-subscriptions/me/reactivate` reactivates a valid cancelled
grace-period subscription when its billing agreement is reusable.

The management UI requires a separate confirmation before this mutation. The
confirmation names the next billing date and amount, cancel causes zero
reactivation calls, and the approve command is disabled while its request is in
flight. For a cancelled Billing Agreement, the date is the Subscription
`expiresAt` that the backend passes to `resume`; for an agreement already in
`ACTIVE`, the date is its retained canonical `nextBillingAt`. Missing canonical
date input disables reactivation rather than producing a fallback date.

## Provider Boundary

Persisted provider identity is `TOSS`. Recurring, lookup, and refund
interfaces remain provider-neutral, but V1 has no second active provider.
