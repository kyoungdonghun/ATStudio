---
version: 3.4
last_updated: 2026-08-12
project: ATS
owner: SA
category: design
status: stable
dependencies:
  - path: api-spec.md
    reason: Current recurring and admin API contract
  - path: db-schema.md
    reason: Current payment persistence contract
  - path: payment-operations-runbook.md
    reason: Operator recovery procedures
  - path: payment-refund-receipt-settlement-policy.md
    reason: Refund, receipt, settlement, and correction policy
  - path: ../../src/main/java/com/atstudio/atstudio/service/payment/provider/
    reason: Current provider interface and Toss adapter source
---

# Payment Integration Design

## V1 Decision

ATStudio V1 uses **Toss card recurring payment only** for subscription purchase,
payment-method registration, upgrade, renewal, cancellation, refund, and
provider lookup.

- Persisted provider identity: `TOSS`.
- Active adapter: `TossBillingProvider`.
- Active user payment API: billing agreement prepare, confirm, read, and cancel.
- Active frontend routes: `/subscriptions/checkout`, success/fail callbacks,
  and `/subscriptions/manage`.
- Direct subscription creation and legacy payment prepare/confirm/cancel
  contracts are removed.
- No alternate or simulated provider is active or represented in the V1 enum
  or schema.

## Provider-Neutral Boundary

The application keeps three provider interfaces:

| Interface                     | Responsibility                                                            |
| ----------------------------- | ------------------------------------------------------------------------- |
| `RecurringPaymentProvider`    | Billing auth, billing-key issue, recurring charge, agreement cancellation |
| `PaymentStatusLookupProvider` | Provider payment lookup for reconciliation                                |
| `PaymentRefundProvider`       | Provider cancel/refund execution                                          |

`TossBillingProvider` implements all three. These interfaces preserve a
multi-PG extension boundary without claiming that another provider exists.
Adding a provider requires an approved cross-layer change covering enum,
schema, adapter selection, startup validation, reconciliation, refund,
security, tests, and documentation.

## Checkout Identity And Prepare Boundary

Checkout identity consists of the exact `planId`, authenticated audience
(`userType`), and `billingCycle`. A plan name is display context only and does
not select or authorize a plan. A non-callback checkout entry also carries one
requested `purpose`:

```text
/subscriptions/checkout?planId=<positive-integer>&userType=<INDIVIDUAL|BUSINESS>&billingCycle=<MONTHLY|YEARLY>&purpose=<SUBSCRIBE|BILLING_AGREEMENT>
```

Before prepare, the SPA requires every value, checks route `userType` against
the authenticated user, loads plans for that audience, resolves the exact
`planId`, and checks the resolved plan audience again. Invalid or inconsistent
route context leaves checkout non-actionable and does not invoke prepare or the
Toss SDK.

The prepare request contains exactly the payment-intent fields below:

```json
{
  "subscriptionId": 123,
  "billingCycle": "MONTHLY",
  "purpose": "SUBSCRIBE"
}
```

Every prepare request also carries a required header-only `Idempotency-Key`.
The key is absent from the body and must be a lowercase canonical UUIDv4. The
API rejects missing, blank, uppercase, malformed, non-v4, wrong-variant,
oversized, or control-character values with
`PAYMENT_PREPARE_IDEMPOTENCY_KEY_INVALID` before repository or Provider work;
it never trims, normalizes, or generates a fallback.

The SPA stores one versioned attempt record in `sessionStorage`, keyed by exact
purpose, plan ID, audience, and billing cycle. That same key survives React
StrictMode remount, browser reload, network retry, and explicit same-attempt
retry. Error handling never rotates it automatically. A user-visible explicit
replacement may overwrite only the current context record after local corrupt
state, `PAYMENT_PREPARE_IDEMPOTENCY_KEY_INVALID`, `PAYMENT_ORDER_EXPIRED`, or
`PAYMENT_ORDER_TERMINAL`. Tuple conflict, arbitrary HTTP `409`, Provider or
network failure, unknown result, and in-flight state retain the existing key.

`purpose` accepts only `SUBSCRIBE` or `BILLING_AGREEMENT`. It is a client
consistency claim, not an authority source. The server loads the authenticated
user and exact plan, verifies the plan audience, resolves the service-enabled
subscription state, derives the authoritative purpose, and compares it with
the request before billing-agreement mutation, payment-order persistence, or
recurring Provider preparation.

- No current service-enabled subscription derives `SUBSCRIBE` and the exact
  server plan price for the requested cycle.
- An `ACTIVE` subscription, or a non-expired `CANCELLED` grace-period
  subscription, derives `BILLING_AGREEMENT`. The requested plan and cycle must
  equal the current subscription, and the server amount is exactly `0`.
- A purpose, current-plan, current-cycle, or audience mismatch fails before the
  three side effects above.

The successful prepare response becomes actionable frontend state only after
all of these values pass validation: nonblank order ID, `TOSS` provider,
requested purpose, `READY` agreement status, exact plan ID, exact cycle, exact
server-derived amount, `KRW`, parseable expiry, `TOSS_BILLING_AUTH` checkout
type, `CARD` method, nonblank client/customer keys, and absolute HTTP(S)
success/fail callback URLs. Checkout copy and callback construction use only
the validated response purpose, amount, and order ID. A response mismatch
clears the actionable order and prevents SDK loading and `requestBillingAuth`.
Because that mismatch is detected after prepare returned, frontend SDK
non-invocation alone does not prove that no server order or test Provider
prepare occurred.

## Prepare Attempt And Claiming

The backend derives
`BILLING_PREPARE:v1:<64-lowercase-hex-sha256>` from the authenticated owner and
accepted raw UUID. Only this opaque, versioned, owner-scoped digest is persisted
as `payment_orders.command_key`; neither the raw key nor owner ID is stored or
logged in the command key.

- Same owner, key, and exact authoritative purpose/plan/audience/cycle,
  Billing Agreement, and active Subscription tuple reuses the same valid,
  unexpired order and returns the same `orderId` and equal response.
- A same-owner tuple mismatch returns HTTP `409`
  `PAYMENT_PREPARE_ATTEMPT_CONFLICT` before mutation or Provider prepare.
- The same raw UUID under another authenticated owner is an independent
  namespace and cannot select, disclose, mutate, or confirm the first owner's
  order.
- Expired history returns `PAYMENT_ORDER_EXPIRED`; safe `FAILED` or `CANCELLED`
  history returns `PAYMENT_ORDER_TERMINAL`. Both require a separately supplied
  new key through the explicit frontend action and preserve the old row/key.
- `PROCESSING`, `PROVIDER_SUCCEEDED`, `PENDING_PROVIDER_CONFIRMATION`, `DONE`,
  and unknown future states are non-replaceable. Existing authoritative errors
  apply, and WI-034 read-only outcome recovery is now the completed user path
  after confirm or charged upgrade starts.

First Billing Agreement creation uses the existing named unique constraint
`uq_billing_agreements_user_provider` as the claim. A short `REQUIRES_NEW`
transaction performs a non-locking probe, inserts and flushes when absent, and
commits. Only a loser that observes that exact named constraint violation may
perform the bounded retry and fresh-transaction reread. It does not use a
missing-row locking read, `User`-first lock, sleep, table lock, or unbounded
polling.

Order claiming then uses a separate transaction and the canonical write-lock
order:

```text
BillingAgreement -> UserSubscription -> PaymentOrder -> SubscriptionPayment -> PaymentRefund
```

Only the named `uq_payment_orders_command_key` loser is retryable after the
failed transaction ends; the next bounded transaction rereads and validates the
committed owner, tuple, and lifecycle. The pure deterministic Provider prepare
descriptor runs outside every local transaction. No prepare call performs a
Provider mutation, and a Provider that does not explicitly attest this contract
fails closed.

## User Flow

### New Subscription

1. The USER selects an exact plan ID, audience, and cycle.
2. Checkout requests `purpose=SUBSCRIBE` and applies the route and prepare
   response gates above.
3. The backend derives `SUBSCRIBE` and creates a full-price billing-agreement
   order only when current subscription state agrees.
4. The frontend opens Toss billing auth only from the validated response.
5. The success callback sends the returned authorization values to the backend.
6. The backend issues and encrypts the billing key.
7. The backend performs the first recurring charge.
8. Subscription access activates only after durable provider success and local
   finalization.

### Payment-Method Registration

1. An existing subscriber carries the exact current plan ID, authenticated
   audience, and current billing cycle into checkout with
   `purpose=BILLING_AGREEMENT`.
2. The server verifies that identity against the current service-enabled
   subscription and returns `amount=0`.
3. The SPA opens billing auth only after validating the zero-amount response.
4. Confirmation replaces the encrypted billing key and masked method.
5. The current plan, period, and access are unchanged. When registration was
   entered from an upgrade preview, immutable return plan identity and audience
   are retained, but the upgrade is not executed by registration.

### Upgrade and Scheduled Change

- An upgrade charges the prorated difference through the active billing
  agreement before changing access.
- Downgrade and cycle-only changes remain pending for the next successful
  renewal.
- Manage recovery uses the mutation response's `changeType` and prorated amount
  as the result source. A stale preview never overrides a successful response.
- There is no fallback checkout path.

### Renewal and Cancellation

- Renewal uses the stored billing agreement.
- User cancellation stops the next renewal while paid access remains through
  `expiresAt`.
- A valid cancelled grace-period subscription may be reactivated.
- Account withdrawal cancels local renewal eligibility before provider cleanup.
  Provider cleanup failure creates retryable Incident evidence and does not
  undo local withdrawal.

## Outcome Recovery And User-State Semantics

The frontend uses one frozen outcome vocabulary after a callback or Manage
mutation starts:

| Outcome | Required proof and UI meaning |
| --- | --- |
| `COMMITTED` | The payment order is `DONE` where a payment command applies, and fresh canonical Subscription plus Billing Agreement reads prove exact target identity and the same `userSubscriptionId` aggregate linkage. Only this state permits a success announcement or success navigation. |
| `FAILED` | A terminal payment order is `FAILED`, `CANCELLED`, or `EXPIRED`, or a local-only cancel/reactivate endpoint returns one of its narrow authoritative terminal business errors. The operation is proved not committed and is no longer ambiguous. |
| `RELOAD_FAILED` | The mutation response explicitly reported success, but the required outcome or canonical aggregate reload failed. The successful mutation context and message are retained; the mutation is never relabeled as failed. |
| `UNKNOWN` | The request may have reached the server or Provider, or available reads do not prove either durable success or terminal failure. The UI warns that processing may already have completed and exposes only read-only status recheck. |

HTTP `2xx`, a success toast, redirect state, stale component state, or payment
order `DONE` without canonical aggregate linkage is insufficient for
`COMMITTED`. A timeout, lost response, HTTP `5xx`, failed reload, unmatched
aggregate, or unavailable read is insufficient for `FAILED`.

### Callback Recovery

The callback page captures `orderId`, `authKey`, `customerKey`, and amount once,
then removes `authKey` and `customerKey` from the visible URL immediately with
history replacement before confirmation settles. The raw values are used only
for the one callback confirmation attempt and are not rendered.

- A success callback with authorization values invokes confirmation once, then
  performs the owner-scoped order-outcome read and canonical Subscription and
  Billing Agreement reads.
- A success callback revisited without authorization values performs only the
  owner-scoped order-outcome and canonical reads.
- A fail callback with `orderId` also performs the owner-scoped outcome read;
  it does not assume failure from the callback route.
- Neither callback path automatically repeats confirmation, billing-key issue,
  charge, upgrade, or another financial mutation. The `status again` control is
  read-only and deduplicated while a recovery read is in flight.

The outcome response is intentionally minimal and excludes Provider payload,
authorization/customer/billing keys, payment-method details, and PII. Outcome
reads make zero Provider calls, zero mutation calls, and zero finalization
calls. An absent or foreign command produces the same not-found result.

### Manage Recovery

Manage freezes the operation context before mutation and gives the successful
mutation response priority over the preview. For `SCHEDULED_CHANGE` and
`DOWNGRADE`, canonical proof requires the same source Subscription aggregate
ID, source plan ID, source billing cycle, and exact pending target plan/cycle.
For charged `UPGRADE`, proof additionally requires the exact deterministic
current-period outcome, `DONE`, its non-null `userSubscriptionId`, equality to
the canonical Subscription aggregate ID, and canonical Billing Agreement
linkage to that same aggregate and target.

Any `UNKNOWN` or `RELOAD_FAILED` Manage state disables change, cancel,
reactivate, and payment-method mutation controls together. Its only recovery
action reruns the bounded reads. Rapid mutations and recovery clicks are fenced
so an older read cannot overwrite newer authoritative state.

`CANCEL` and `REACTIVATE` are local-only state transitions. Only these narrow
response errors are terminal without reconciliation:

| Operation | Authoritative terminal errors |
| --- | --- |
| `CANCEL` | `NO_ACTIVE_SUBSCRIPTION`, `RESOURCE_NOT_FOUND` |
| `REACTIVATE` | `NO_ACTIVE_SUBSCRIPTION`, `RESOURCE_NOT_FOUND`, `BILLING_AGREEMENT_NOT_FOUND`, `BILLING_AGREEMENT_INVALID_STATE` |

Every `CHANGE` error is reconciled, regardless of HTTP class. In particular, a
charged change never treats a broad post-Provider business-error allowlist as
proof of failure; only exact terminal payment-order evidence can make that
financial command `FAILED`.

### ADMIN Refund and Entitlement-Correction Recovery

Refund and refund-linked entitlement correction remain separate state
machines. Each recovery intent owns its domain, durable ID, operation
generation, current outcome, and view request. A correction intent also owns
its linked refund ID. Evidence from one domain never proves the other domain's
result.

Before one typed execute action can mutate state, the SPA reads the exact
existing ADMIN detail endpoint and requires the same durable ID with fresh
`APPROVED` status. It then sends at most one execute POST. The two execute POSTs
opt out of shared authentication replay, so an opted-out `401` cannot enter the
refresh queue or replay the mutation. Any rejected/lost execute response causes
one bounded exact detail GET and no automatic execute retry.

The shared outcome names have these ADMIN predicates:

- `COMMITTED`: the exact refund or correction detail is `SUCCEEDED`.
- `FAILED`: the exact execute/detail result is `FAILED` or `CANCELLED`.
- `RELOAD_FAILED`: execute returned `SUCCEEDED`, but the required exact detail
  or committed-result list reload failed. The successful execute context is
  retained and receives reload-specific feedback.
- `UNKNOWN`: success and terminal failure remain unproved. Refund
  `PROCESSING`/`PENDING_PROVIDER_CONFIRMATION` and correction `PROCESSING` are
  durable in-flight examples.

List hydration restores those durable in-flight rows as exact-ID `UNKNOWN`
intents after reload. `status again` is a deduplicated detail read only. It
cannot approve, execute, call a Provider, or mutate local state. An `UNKNOWN`
intent may unlock from pre-execution only when exact detail returns `REQUESTED`
or `APPROVED`; execution from a later `APPROVED` action still requires typed
confirmation and another exact preflight.

An ambiguous refund locks its own controls and every linked correction
mutation. An ambiguous correction locks its own row, correction intents sharing
the refund, and the linked refund controls. Execute ownership excludes status
reads across preflight, POST, and post-execute recovery. Intent, read, and view
generations ensure stale detail/list responses cannot overwrite newer or
cross-tab authority.

Automatic refund execute retries are zero. Automatic entitlement-correction
execute retries are zero. Recovery reads perform zero mutations and zero
Provider calls.

## Transaction and Recovery Invariants

- Every logical payment command has stable persisted identity. Prepare uses the
  owner-scoped digest above, and a non-null `command_key` is immutable through
  prepare, confirm, charge, callback, reconciliation, failure, retry, and
  cleanup.
- Confirm preserves every non-null prepare `command_key`. It derives the legacy
  `BILLING_CONFIRM:<orderID>` fallback only when the selected order's key is
  truly null. `provider_idempotency_key` remains the separate concrete
  Provider-attempt fence and advances with the persisted attempt number.
- Prepare lookup ignores legacy null-key rows and never rewrites, backfills, or
  deletes them. The confirm fallback is selected-order scoped and is not a
  migration.
- Provider mutation runs outside local database transactions.
- Claim, provider-result persistence, and finalization use separate committed
  phases.
- Provider success is durable before subscription/payment finalization.
- Retrying durable success is finalize-only and must not recharge.
- Refund execution reuses one refund row and idempotency key, with processing
  lease and stale-result fencing.
- Reconciliation mutates only from exact provider evidence; uncertainty remains
  Incident-only.
- Direct ADMIN subscription update/cancel mappings are retired. The separate
  general local correction workflow is explicit, audited, and has no provider
  charge/refund/billing-key-delete side effect.

## Data Model

| Table                              | Responsibility                                                       |
| ---------------------------------- | -------------------------------------------------------------------- |
| `billing_agreements`               | Encrypted key, masked method, agreement state, renewal/cleanup state |
| `payment_orders`                   | Command, claim, provider attempt, and finalization evidence          |
| `subscription_payments`            | Finalized recurring charges                                          |
| `payment_refunds`                  | Refund request, approval, provider execution, lease, and result      |
| `payment_entitlement_corrections`  | Explicit refund-linked local access correction                       |
| `payment_settlements`              | Settlement import/generated review evidence                          |
| `payment_reconciliation_incidents` | Persistent payment and cleanup incidents                             |
| `payment_receipts`                 | Safe receipt evidence                                                |
| `payment_operation_audit_logs`     | Append-only admin/system operation audit                             |

All provider columns use `TOSS` in V1.

## Billing-Key Security

Billing keys use the V2 key-ID AES-GCM envelope only.

- `app.payment.billing.active-key-id`: write key selection.
- `app.payment.billing.encryption-keys`: active and retained V2 key ring.
- Raw billing keys, authorization values, customer identifiers, card numbers,
  and secret keys never enter frontend/admin DTOs or free-text logs.
- Missing, blank, duplicate, unknown, or placeholder key configuration fails
  closed.

## Configuration

Payment configuration is provider-neutral under `app.payment`:

- `toss.client-key`, `toss.secret-key`, endpoint and timeout settings.
- `billing.active-key-id`, `billing.encryption-keys`.
- `billing.auth-success-url`, `billing.auth-fail-url`.
- reconciliation limits and operator-notification settings.
- `scheduler-zone`, defaulting to `Asia/Seoul`.

The base configuration does not import ignored local configuration. Local
values are loaded explicitly by the operator. Acceptance derives callback URLs
from its declared public base URL and remains fail-closed.

## API Boundary

### USER

- `POST /api/payments/billing-agreements/prepare`
- `POST /api/payments/billing-agreements/confirm`
- `GET /api/payments/billing-agreements/me`
- `DELETE /api/payments/billing-agreements/me`
- `GET /api/payments/orders/{orderId}/outcome`
- `GET /api/payments/subscription-upgrades/outcome?subscriptionId=&billingCycle=`
- `GET|PUT|DELETE /api/user-subscriptions/me`
- `POST /api/user-subscriptions/me/reactivate`

### ADMIN

`/api/admin/payments/**` owns payment ledger reads, reconciliation, incidents,
receipts, audit logs, refunds, refund-linked entitlement corrections, and
settlements. General local entitlement correction is separately exposed under
`/api/admin/user-subscription-corrections/**`; direct ADMIN
`PUT|DELETE /api/user-subscriptions/{id}` mappings are retired. The general
local workflow does not charge/refund or delete provider billing keys.

WI-035 reuses the existing exact refund and entitlement-correction detail GETs;
it introduces no endpoint, response schema, or persistence schema. The shared
existing refund DTO's raw idempotency key, actor emails, and failure message,
and the correction DTO's actor emails and failure message, remain pre-existing
non-blocking minimization debt. The recovery UI does not render those fields.
Their omission, masking, or sanitization requires a separate approved contract
change and is not claimed as complete here.

## Verification Boundary

- Fresh MySQL baseline plus Hibernate `ddl-auto=validate`.
- Full backend tests and payment race suites.
- Provider-identity negative search outside historical/archived records.
- Frontend checkout/manage tests.
- Secret scan that does not read ignored local values.

WI-033 prepare replay was verified with automated frontend/backend tests,
deterministic Provider doubles, supplemental H2, and a fresh disposable MySQL
8.0.45 InnoDB schema under `REPEATABLE_READ`. The disposable proof covered the
named unique loser, winner-commit visibility, fresh-transaction reread, lock
order, and convergence, then passed guarded teardown with no residual process
or temporary directory. It did not use a real Toss Provider/SDK, charge,
refund, cancellation, mail, retained database, deployment, or secret.

WI-034 outcome recovery was verified with automated backend and frontend tests,
H2/Test-Provider state, owner-isolation checks, exact command lookup, canonical
aggregate linkage, stale-read fencing, and mutation non-replay assertions. It
did not use a real Toss Provider/SDK, charge, refund, cancellation, mail,
retained database, deployment, or secret. WI-033 changes no payment policy or
schema and does not make in-flight or unknown-outcome orders replaceable.

WI-035 ADMIN recovery was verified with focused and full frontend/backend
automation, H2/Test-Provider state, exact durable reads, cross-domain locks,
authentication replay exclusion, stale-response fencing, and zero automatic
execute replay. Verification did not use a real Toss Provider/SDK, live refund,
external service, retained database, deployment, or secret.

## Related Documents

- [API Specification](api-spec.md)
- [DB Schema](db-schema.md)
- [Payment Operations Runbook](payment-operations-runbook.md)
- [Payment Refund, Receipt, Settlement, and Tax Invoice Policy](payment-refund-receipt-settlement-policy.md)
