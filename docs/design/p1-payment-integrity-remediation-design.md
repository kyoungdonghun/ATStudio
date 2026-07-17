---
version: 1.2
last_updated: 2026-07-17
project: ATS
owner: SA
category: design
status: stable
dependencies:
  - path: ../../deliverables/agent/WI-20260714-ATS-036-handoff.md
    reason: Approved design scope and output contract
  - path: ../../deliverables/agent/WI-20260714-ATS-023-evidence-pack.md
    reason: Blocking findings F-01 through F-05
  - path: p1-payment-db-integrity-design.md
    reason: Existing payment command, DDL, and transaction baseline
---

# P1 Payment Integrity Remediation Design

> Purpose: Close the design gaps identified by `WI-20260714-ATS-023` without
> adding payment features, replaying ambiguous money movement, or applying a
> patch to an existing database.

> **Current implementation status (2026-07-15):** This design is implemented by
> Packages A-G and the WI-008/WI-011 corrections. Independent follow-up review
> passed in WI-012. This status closes the repository payment-integrity
> code/test gate only; retained-database migration is outside the V1 baseline,
> while live-provider, deployment, and client acceptance gates remain open. See
> [P1 Payment Integrity Closure](../audit/p1-payment-integrity-closure-20260715.md).

## 1. Decision Summary

1. `billing_agreements.next_billing_at` is the immutable identity of the
   unresolved renewal period. A new nullable `renewal_retry_at` schedules a
   deterministic retry. Retry scheduling never changes the period, order,
   `command_key`, or logical command identity.
2. Cancellation, withdrawal cleanup, charged upgrade, and scheduled provider
   reconciliation are non-transactional orchestrators. Each uses short
   `REQUIRES_NEW` claim/result phases. Provider calls and lookups run under
   `Propagation.NEVER`, so an accidental outer transaction fails instead of
   being suspended.
3. `payment_refunds.processing_started_at` is a 15-minute lease. A stale claim
   is fenced by its persisted timestamp and may recover only through exact
   lookup or an exact same-idempotency-key replay. It never creates a new
   refund row or key.
4. Provider `DONE` is mutation-capable only when exact provider, order, amount,
   currency, and transaction evidence passes. Recovery first stores
   `PROVIDER_SUCCEEDED`, then dispatches the existing purpose-specific local
   finalizer. Every mismatch is detect-only and leaves an open incident.
5. The canonical lock order is `BillingAgreement -> UserSubscription ->
   PaymentOrder -> SubscriptionPayment -> PaymentRefund`. A transaction may
   lock a subset, but it may never acquire a class to the left after a class to
   the right. Production concurrency closure requires disposable MySQL 8 and
   InnoDB; H2 is not accepted as lock proof.

The scope remains single-server, card-recurring, and one provider command at a
time. It does not introduce distributed ownership, multi-server leases, blind
provider replay, a new payment method, or automatic application to an existing
database.

## 2. Scope and Finding Closure

| Finding | Design contract | Closure evidence |
|---|---|---|
| `F-01` | Immutable `next_billing_at`, additive `renewal_retry_at`, exact current-period order selection, one order and command per period | Two-day deterministic retry test proves the same order ID and command key, attempt `1 -> 2`, and a bounded grace window |
| `F-02` | No outer or suspended transaction in cancellation, withdrawal cleanup, scheduled reconciliation, or charged upgrade | Transaction-boundary tests assert no active transaction at every provider invocation and durable claim/result state across injected failures |
| `F-03` | Refund lease, stale reclaim fencing, exact same-key replay/lookup, and crash recovery | Crash-after-claim, crash-after-provider-success, delayed-old-result, and concurrent stale-reclaim tests |
| `F-04` | Strict provider-evidence gate, `PROVIDER_SUCCEEDED` persistence, and purpose-specific finalize-only dispatch | Recovery tests for `SUBSCRIBE`, `UPGRADE`, and `RENEWAL`; mismatch tests remain incident-only |
| `F-05` | Canonical lock order in every multi-row phase and MySQL/InnoDB race tests with exact loser outcomes | Disposable MySQL concurrency suite; no wildcard exception assertions and no deadlock accepted as success |

## 3. Non-Negotiable Invariants

### 3.1 Renewal identity

- The renewal period is `PaymentOrder.billingPeriodStart` and equals the
  agreement's `nextBillingAt` until that period is finalized or terminally
  suspended.
- The logical command key remains
  `RENEWAL:{agreementId}:{userSubscriptionId}:{billingPeriodStart}`.
- One period has one `PaymentOrder.orderId` and one `commandKey`.
- A deterministic provider failure may open another attempt on that same
  order. Each attempt has its own persisted provider idempotency key, while the
  period-level command key stays unchanged.
- An ambiguous outcome never opens another attempt. It is reconciled by the
  same order ID or remains an incident.

### 3.2 External calls

- Methods that invoke `RecurringPaymentProvider`, `PaymentStatusLookupProvider`,
  or `PaymentRefundProvider` must use `Propagation.NEVER` or an equivalent
  runtime assertion that no local transaction is active.
- A provider method is never reached from a transaction that Spring merely
  suspended. The orchestrator itself owns no transaction.
- Local state before and after a provider call is committed in separate short
  transactions.

### 3.3 Finalization

- Provider success is durable as `PaymentOrder.PROVIDER_SUCCEEDED` before
  subscription or payment-ledger finalization.
- `PROVIDER_SUCCEEDED` is finalize-only. No path from that state calls the
  charge provider.
- One payment order has at most one `SubscriptionPayment`, and one provider
  transaction can belong to at most one local payment.
- A finalizer revalidates all relationships and financial evidence while locks
  are held. It does not trust IDs captured before the transaction.

### 3.4 Detect-only versus mutation-capable recovery

- Provider lookup is detect-only by default.
- Mutation is allowed only by the evidence matrix in Section 7.
- Missing, contradictory, or duplicate evidence creates or updates an Incident
  and does not change payment, subscription, agreement, or refund state.
- Raw provider payloads, billing keys, card data, and secrets are never written
  to an Incident or audit note.

## 4. Renewal Period and Retry Contract

### 4.1 Agreement fields

Add one scheduling field to `BillingAgreement`:

| Java field | Column | Meaning |
|---|---|---|
| `renewalRetryAt` | `renewal_retry_at DATE NULL` | Next date on which the current immutable renewal period may open a new deterministic attempt |

`nextBillingAt` changes only in these cases:

| Event | `nextBillingAt` | `renewalRetryAt` |
|---|---|---|
| Agreement activation | First subscription expiry | `NULL` |
| First claim for a due period | Unchanged | `NULL` |
| Deterministic failure with attempts remaining | Unchanged | `min(today + 1, period + 3 days)` |
| Ambiguous result | Unchanged | `NULL`; scheduler excludes the pending order |
| Provider success awaiting finalization | Unchanged | `NULL` |
| Successful finalization | Next period start/expiry | `NULL` |
| Maximum attempts or grace exhausted | Unchanged | `NULL`; agreement becomes `SUSPENDED` |
| Resume after operator/customer action | New approved due period | `NULL` |

`BillingAgreement.recordFailedCharge(LocalDate)` must be replaced by a method
that increments `failureCount` and sets only `renewalRetryAt`. It must not write
`nextBillingAt`.

### 4.2 Exact due selection

The due-ID query remains keyset-paged by agreement ID. It must select an active
agreement only when `next_billing_at <= :today` and one of these exact cases is
true:

1. No order exists for `(agreement, active subscription, RENEWAL,
   next_billing_at)` and `renewal_retry_at IS NULL`.
2. The exact order is `FAILED`, `renewal_retry_at <= :today`, its
   `provider_attempt` is below 3, and `today <= billing_period_start + 3 days`.
3. The exact order is `PROVIDER_SUCCEEDED`, so local finalization can resume.

Fresh `PROCESSING`, stale `PROCESSING`, and
`PENDING_PROVIDER_CONFIRMATION` orders are excluded from automatic charging.
Stale/ambiguous orders belong to reconciliation. `DONE`, `CANCELLED`, and
`EXPIRED` orders are not reusable.

The query may use an exact `EXISTS`/`NOT EXISTS` subquery rather than a broad
"latest order" lookup. The claim transaction still performs the authoritative
locked lookup and revalidation.

### 4.3 Claim and failure transitions

`PaymentCommandTransactionService.claimRenewal()` must lock agreement,
subscription, and exact period order in canonical order. It derives
`billingPeriodStart` from `agreement.nextBillingAt`, never from
`renewalRetryAt`.

For a deterministic provider result:

1. Lock agreement, subscription, and order in canonical order.
2. Verify the order is the current agreement period and is `PROCESSING`.
3. Mark the order `FAILED` for that attempt.
4. Increment the agreement failure count.
5. If `providerAttempt < 3` and `today < period + 3 days`, set
   `renewalRetryAt = min(today + 1, period + 3 days)`.
6. Otherwise clear `renewalRetryAt`, extend entitlement only to the approved
   grace end if required, and suspend the agreement.

At the next deterministic retry, `PaymentOrder.claimProviderAttempt()` changes
`FAILED -> PROCESSING`, increments `providerAttempt`, and persists a new
attempt key on the same row. `orderId`, `commandKey`, `billingPeriodStart`,
amount, provider, subscription, and agreement are immutable.

## 5. Claim, Provider, and Result Phases

### 5.1 User cancellation

Add a reusable cleanup state to `BillingAgreement`:

| Java field | Column | Values/contract |
|---|---|---|
| `billingKeyCleanupStatus` | `billing_key_cleanup_status` | `NONE`, `REQUIRED`, `PROCESSING`, `PENDING_PROVIDER_CONFIRMATION`, `FAILED` |
| `billingKeyCleanupStartedAt` | `billing_key_cleanup_started_at DATETIME NULL` | Start of the current provider-cleanup claim |

The 15-minute cleanup lease prevents concurrent local claims. Unlike refund
recovery, a stale billing-key deletion is not automatically replayed because
the provider outcome may be unknown and there is no money-command
idempotency key.

`cancelMyBillingAgreement()` becomes an orchestrator with no transaction:

| Phase | Transaction | Contract |
|---|---|---|
| `claimUserCancellation(userId, now)` | `REQUIRES_NEW` | Lock agreement then active subscription; commit local agreement/subscription cancellation; when ciphertext exists set cleanup `PROCESSING` and return encrypted-key snapshot |
| Provider deletion | `NEVER` | Decrypt only for the call; invoke `cancelAgreement`; do not retain plaintext |
| `recordCleanupResult(...)` | `REQUIRES_NEW` | Lock agreement; success or `ALREADY_REMOVED_BILLING_KEY` clears key and cleanup state; deterministic failure becomes `FAILED`; transport/5xx/invalid evidence becomes `PENDING_PROVIDER_CONFIRMATION`; record/resolve Incident |

Local cancellation is authoritative and is not rolled back because provider
cleanup failed. A repeated request behaves as follows:

- `NONE` with no key: return the already-cancelled result.
- `PROCESSING` younger than 15 minutes: report in progress; no provider call.
- stale `PROCESSING`: atomically move to
  `PENDING_PROVIDER_CONFIRMATION`, open an Incident, and do not replay.
- `PENDING_PROVIDER_CONFIRMATION` or `FAILED`: return the stable failure state;
  only an explicit operator disposition may move it to `REQUIRED`.

### 5.2 Withdrawal cleanup

`WithdrawalBillingCleanupService.cleanup()` also becomes a no-transaction
orchestrator:

1. `claimWithdrawalCleanup()` uses `REQUIRES_NEW`, locks the agreement, and
   accepts only a deleted user, `CANCELLED` agreement, nonblank ciphertext, and
   cleanup state `NONE` or `REQUIRED`. It stores `PROCESSING` and returns a
   snapshot.
2. Provider deletion runs under `NEVER`.
3. `recordCleanupResult()` applies the same result contract as user
   cancellation.

`findRetryCandidateIDs()` selects only exact unresolved cleanup (`NONE` or
`REQUIRED` plus deleted user, cancelled agreement, and retained ciphertext).
It does not select `PENDING_PROVIDER_CONFIRMATION`, `FAILED`, or fresh
`PROCESSING`. A separate stale scan converts stale `PROCESSING` to a detect-only
Incident.

### 5.3 Charged upgrade

The charged branch in `UserSubscriptionService.changeSubscription()` must not
own a transaction. Split it into:

1. A short local planning transaction that decides local-only versus charged
   change and returns IDs only.
2. Existing `claimUpgrade()` in `PaymentCommandTransactionService`, with
   `REQUIRES_NEW` and canonical locks.
3. Provider charge in `SubscriptionUpgradePaymentExecutor` under `NEVER`.
4. Existing short provider-result transaction.
5. Existing short `finalizeUpgrade()` transaction.

Remove the outer `@Transactional`/`noRollbackFor` lifetime from the calling
service. Remove reliance on `NOT_SUPPORTED` in the executor; `NEVER` must fail
if a caller accidentally reintroduces an outer transaction. Zero-amount plan
changes remain one short local transaction and do not enter the provider flow.

Add nullable `PaymentOrder.upgradeTargetBillingCycle` /
`upgrade_target_billing_cycle`. It is required for every new `UPGRADE` order
and null for other purposes. Reconciliation must never parse `command_key` to
recover this finalization input.

### 5.4 Scheduled reconciliation

`reconcilePaymentLedgersOnSchedule()` owns no transaction and must not depend
on self-invocation for transaction annotations.

| Phase | Transaction | Contract |
|---|---|---|
| Candidate page | Short `readOnly` transaction | Return order IDs only |
| `claimProviderLookup(orderId)` | `REQUIRES_NEW` | Lock the order and required ancestors in canonical order; return an immutable expected-state snapshot |
| Provider lookup | `NEVER` | Lookup by exact local `orderId`; no local entity is attached |
| `applyProviderLookup(...)` | `REQUIRES_NEW` | Re-lock and revalidate the snapshot; record detect-only issue or persist exact success as `PROVIDER_SUCCEEDED` |
| Purpose finalizer | Existing `REQUIRES_NEW` method | Dispatch only after success evidence commits |
| Incident completion | `REQUIRES_NEW` | Resolve on finalization success; leave open and audit on failure |

The lookup itself is read-only at the provider and does not require a durable
database lease under the single-server constraint. Concurrent manual and
scheduled lookups may duplicate a read, but their result phases serialize on
the canonical locks and are idempotent.

## 6. Refund Lease and Crash Recovery

### 6.1 Field and lease

Add `PaymentRefund.processingStartedAt` mapped to
`payment_refunds.processing_started_at DATETIME NULL` and index
`(status, processing_started_at, id)`.

- Lease duration: 15 minutes.
- Claim timestamps use second precision so Java equality matches MySQL
  `DATETIME` precision.
- `markProcessing(actor, now)` stores the timestamp.
- `markSucceeded`, `markFailed`, and `markPendingProviderConfirmation` clear it.
- The claim returns the persisted timestamp as `leaseStartedAt`.
- Every result writer receives `leaseStartedAt` and rejects the write unless
  the locked row is still `PROCESSING` with the same timestamp. This fences a
  delayed result from an older claimant.

### 6.2 Claim rules

`claimExecution(refundId, actor, note, now)` locks the refund row and applies:

| Current state | Rule |
|---|---|
| `APPROVED` | First claim; store `PROCESSING` and lease time |
| `PENDING_PROVIDER_CONFIRMATION` | Explicit recovery claim; retain the same row and key |
| `PROCESSING`, lease younger than 15 minutes | Reject as in progress |
| `PROCESSING`, lease at least 15 minutes old | Reclaim atomically with a new lease time and mark the audit as stale recovery |
| Terminal/other | Reject |

The claim snapshot contains the persisted provider, payment key, order ID,
amount, currency, reason, idempotency key, and lease time. A recovery request
must exactly equal this snapshot. No caller can replace the reason, amount,
payment key, order ID, refund ID, or idempotency key.

### 6.3 Same-key replay and lookup

Recovery selects one of these provider capabilities:

1. Exact refund lookup by the persisted idempotency key or provider refund
   transaction ID, when available.
2. Otherwise, exact replay of the original refund command with the same
   `Idempotency-Key`, provider payment key, order ID, amount, currency, and
   reason.

For the current Toss card-recurring adapter, same-key replay is the required
path. ATStudio limits automatic stale replay to 24 hours after refund creation;
this local ceiling must remain within the provider's verified idempotency
retention contract. If that provider contract is not verified or the ceiling
has elapsed, recovery is lookup-only. Without exact lookup evidence, the row
returns to `PENDING_PROVIDER_CONFIRMATION` and an Incident remains open.

This is not blind replay: the original key and complete immutable request are
reused. A new key, new row, changed amount, or changed payment key is forbidden.

### 6.4 Required refund crash tests

1. Crash after claim and before provider call: after 15 minutes, reclaim the
   same row/key and complete once.
2. Provider succeeds and the process stops before result persistence: stale
   recovery sends the exact same key, receives the same provider transaction,
   and writes one `SUCCEEDED` refund.
3. Old result arrives after a stale reclaim: the old lease timestamp is
   rejected and cannot overwrite the new claim.
4. Two stale reclaimers race: one obtains the lease; the other receives the
   specific in-progress/invalid-transition result. A timeout, deadlock, or
   arbitrary exception does not satisfy the test.
5. Same key with changed amount/order/payment key is rejected before provider
   invocation.
6. Replay ceiling elapsed and no exact lookup exists: no provider mutation;
   state is pending and an Incident is recorded.

## 7. Provider-DONE Finalize-Only Recovery

### 7.1 Required provider evidence

`ProviderPaymentLookupResult` must expose and the adapter must validate:

- provider type;
- provider order ID exactly equal to `PaymentOrder.orderId`;
- terminal provider status exactly `DONE`;
- non-null total amount exactly equal to `PaymentOrder.amount`;
- currency exactly equal to the local currency (`KRW` in this scope);
- nonblank authoritative provider transaction ID;
- sanitized lookup evidence only.

Before mutation, the result transaction also requires:

- local purpose is `SUBSCRIBE`, `UPGRADE`, or `RENEWAL`;
- local command key is nonblank and uniquely persisted;
- agreement/subscription/order relationships still equal the claim snapshot;
- no other `SubscriptionPayment` owns `(provider, transactionId)`;
- an existing payment for this order, if any, has the same provider,
  transaction ID, amount, and ownership;
- `UPGRADE` has a persisted `upgradeTargetBillingCycle`;
- `RENEWAL` has non-null `billingPeriodStart` equal to the agreement's current
  immutable `nextBillingAt` and the exact subscription row;
- `SUBSCRIBE` has retained billing-key ciphertext/fingerprint and the expected
  initial subscription state.

### 7.2 State and action matrix

| Local order state | Exact provider `DONE` evidence | Action |
|---|---|---|
| stale `PROCESSING` | Pass | Store reconciled success as `PROVIDER_SUCCEEDED`; finalize by purpose |
| `PENDING_PROVIDER_CONFIRMATION` | Pass | Store reconciled success as `PROVIDER_SUCCEEDED`; finalize by purpose |
| `PROVIDER_SUCCEEDED` | Pass and same transaction evidence | Finalize by purpose only |
| `DONE` | Pass and local payment evidence matches | Verify/resolve Incident; no mutation |
| fresh `PROCESSING` | Any | Detect-only; the live command retains ownership |
| `READY`, `IN_PROGRESS`, `FAILED`, `CANCELLED`, or `EXPIRED` | Any | Detect-only contradiction Incident; no automatic state transition |
| Any | Missing/mismatched order, amount, currency, provider, or transaction | Detect-only high-priority Incident |
| Any | Provider transaction belongs to another order/payment | Detect-only high-priority Incident |

Add a dedicated entity transition such as
`markProviderSucceededFromReconciliation(...)`. It may accept only stale
`PROCESSING` or `PENDING_PROVIDER_CONFIRMATION`, and it performs the same exact
transaction-ID idempotency check as normal success. Do not broaden
`markProviderSucceeded()` for normal command callers.

### 7.3 Purpose dispatch

| Purpose | Finalizer | Additional immutable inputs |
|---|---|---|
| `SUBSCRIBE` | `finalizeInitialCharge(userId, agreementId, orderId)` | Order subscription and billing cycle; issued billing key present |
| `UPGRADE` | `finalizeUpgrade(userId, agreementId, orderId)` | Persisted target subscription and `upgradeTargetBillingCycle` |
| `RENEWAL` | `finalizeRenewal(agreementId, orderId)` | Persisted period, subscription, billing cycle, and amount |

`finalizeUpgrade()` must no longer accept a caller-supplied target billing
cycle. It reads the locked order. No generic finalizer may infer intent from a
payload or command-key string.

### 7.4 Incident and audit behavior

1. Provider `DONE` with local non-final state always creates or updates a
   deduplicated reconciliation Incident before mutation is attempted.
2. The provider-success result transaction appends an audit entry through the
   existing reconciliation Incident audit path with old/new local status,
   order ID, purpose, local/provider amount, and sanitized transaction ID.
3. Finalization success resolves the Incident and appends the resolution audit.
4. Finalization failure leaves `PROVIDER_SUCCEEDED` and the Incident open, with
   exception class only. The next reconciliation performs finalize-only.
5. Evidence mismatch or duplicate transaction leaves all financial state
   unchanged and the Incident open. Operator action is required.

## 8. Canonical Lock Order

### 8.1 Rule

The only permitted class order is:

```text
BillingAgreement
  -> UserSubscription
  -> PaymentOrder
  -> SubscriptionPayment
  -> PaymentRefund
```

A phase may omit classes. For example, provider-result persistence may lock
agreement then order, and refund execution may lock only the refund. It may not
lock `PaymentOrder` and then acquire `UserSubscription`.

When a right-hand row ID is needed to lock a left-hand row first, perform a
non-locking ID projection before the transaction or at its beginning. That
projection grants no authority. After all required rows are locked in order,
revalidate every ID, status, amount, provider, and relationship.

### 8.2 Required current-code corrections

- `recordRenewalProviderFailure`: agreement -> subscription -> order, replacing
  agreement -> order -> subscription.
- `finalizeUpgrade`: agreement -> subscription -> order -> existing payment,
  replacing agreement -> order -> subscription.
- `finalizeRenewal`: agreement -> subscription -> order -> existing payment,
  replacing agreement -> order -> subscription.
- Upgrade and renewal claim paths retain agreement -> subscription -> order.
- Initial finalization retains agreement -> subscription -> order -> payment.
- Cancellation uses agreement -> subscription.
- Refund reservation locks source payment before aggregate/read and refund
  insert. It never later locks agreement, subscription, or order.
- Reconciliation purpose finalization uses the same order as its normal
  command finalizer; it does not introduce an alternate lock sequence.

Repository methods used for a locked existing `SubscriptionPayment` or
`PaymentRefund` must use `PESSIMISTIC_WRITE`. Inserts rely on the existing
unique constraints after the parent locks are held.

## 9. Additive Database Contract

### 9.1 Fresh schema delta

The implementation updates `schema.sql` with the equivalent of:

```sql
ALTER TABLE billing_agreements
    ADD COLUMN renewal_retry_at DATE NULL AFTER next_billing_at,
    ADD COLUMN billing_key_cleanup_status ENUM (
        'NONE', 'REQUIRED', 'PROCESSING',
        'PENDING_PROVIDER_CONFIRMATION', 'FAILED'
    ) NOT NULL DEFAULT 'NONE' AFTER cancelled_at,
    ADD COLUMN billing_key_cleanup_started_at DATETIME NULL
        AFTER billing_key_cleanup_status,
    ADD KEY idx_billing_agreements_renewal_retry
        (status, renewal_retry_at, id),
    ADD KEY idx_billing_agreements_cleanup
        (billing_key_cleanup_status, billing_key_cleanup_started_at, id);

ALTER TABLE payment_orders
    ADD COLUMN upgrade_target_billing_cycle ENUM ('MONTHLY', 'YEARLY') NULL
        AFTER billing_cycle;

ALTER TABLE payment_refunds
    ADD COLUMN processing_started_at DATETIME NULL AFTER approved_at,
    ADD KEY idx_payment_refunds_status_processing
        (status, processing_started_at, id);
```

Existing command, renewal-period, payment-order, provider-transaction, and
refund-idempotency unique keys remain unchanged.

### 9.2 V1 fresh-only schema contract

The final V1 baseline is the fail-closed `src/main/resources/schema.sql` applied
once to a verified-empty MySQL 8 database, followed by the six-plan `seed.sql`
and Hibernate `ddl-auto=validate`. No retained-data migration is part of the V1
runtime or operator path. Such a migration requires a separate approved
requirement, explicit data disposition, rehearsal, and rollback.

## 10. Test and Proof Contract

### 10.1 Focused behavior tests

| Test | Required assertion |
|---|---|
| `RecurringRenewalCommandIntegrationTest` | Day 1 deterministic failure and day 2 retry use one order ID, one command key, unchanged period, `providerAttempt = 2`, and distinct persisted attempt keys |
| `RecurringRenewalCommandIntegrationTest` | Ambiguous day 1 result is not selected for a day 2 charge; reconciliation owns it |
| `BillingAgreementCancellationTransactionIntegrationTest` | Local cancellation commits before provider call; provider observes no transaction; failure leaves cleanup Incident without reactivation |
| `WithdrawalBillingCleanupTransactionIntegrationTest` | Claim/result commits are independent; stale cleanup is detect-only and not replayed |
| `ChargedUpgradeTransactionBoundaryIntegrationTest` | No transaction is active at claim return, provider call, or between result/finalize; provider success plus finalize failure remains finalize-only |
| `PaymentRefundResilienceIntegrationTest` | All six lease/crash cases in Section 6.4 |
| `PaymentReconciliationRecoveryIntegrationTest` | Exact provider `DONE` finalizes each supported purpose once; provider is not called as a charge |
| `PaymentReconciliationRecoveryIntegrationTest` | Amount/order/currency/transaction/status conflicts are Incident-only |

Every provider fake records `TransactionSynchronizationManager` state at the
invocation boundary. The expected value is false; a suspended transaction is
not accepted.

### 10.2 Disposable MySQL 8/InnoDB proof

Use the existing JDBC driver and a separately approved empty disposable MySQL
8 schema. Do not add Testcontainers and do not connect to a retained or shared
database.

At test start, record `SELECT VERSION()`, `@@transaction_isolation`, and the
engine for all tested tables. Fail unless the tables use InnoDB.

Required races:

1. Two first claims for the same renewal period: one order/command remains;
   the loser receives the exact in-progress outcome.
2. Day-2 deterministic renewal retry: same row/command; one new attempt only.
3. Upgrade finalizer versus renewal/cancellation on the same agreement and
   subscription: convergence or a documented business-state loser, with no
   deadlock.
4. Two finalizers for the same provider-success order: one payment row and one
   entitlement transition.
5. Two refund reservations for the same source payment: total reservation
   never exceeds payment amount; loser is the exact validation rejection.
6. Two stale refund reclaimers: one lease owner; old/losing result is fenced.
7. Reconciliation finalize-only versus the normal finalizer: one payment row,
   one provider transaction owner, and a resolved Incident.

Tests must not treat an arbitrary exception as the expected loser. SQLState
`40001`, lock timeout, deadlock, assertion timeout, and connection failure fail
the test and print diagnostics. `SHOW ENGINE INNODB STATUS` may be captured on
failure only.

H2 tests remain useful for state transitions and service orchestration. They do
not prove InnoDB lock ordering, MySQL isolation behavior, ENUM compatibility,
DDL implicit commits, or a separately approved retained-data migration.

## 11. Exact Implementation Impact

| File/symbol | Required change |
|---|---|
| `entity/BillingAgreement.java` | `renewalRetryAt`, cleanup state/lease, immutable-period transition methods |
| `entity/PaymentOrder.java` | Persist upgrade target cycle; reconciled-success transition |
| `entity/PaymentRefund.java` | Refund lease field, timestamped claim, terminal lease clearing |
| `entity/enums/BillingKeyCleanupStatus.java` | New cleanup enum |
| `repository/BillingAgreementRepository.java` | Package B owns and provides exact renewal candidates plus cleanup/stale projections; package C consumes the cleanup contract without editing this repository |
| `repository/PaymentOrderRepository.java` | Canonical lock projections and exact reconciliation candidates |
| `repository/SubscriptionPaymentRepository.java` | Locked existing-payment lookup for finalizers |
| `repository/PaymentRefundRepository.java` | Stale lease query and locked claim lookup |
| `service/PaymentCommandTransactionService.java` | Renewal retry semantics, canonical locks, persisted upgrade target, reconciled-success and purpose finalizers |
| `service/RecurringRenewalService.java` | Immutable-period orchestration; no pending-order charge |
| `service/BillingAgreementApplicationService.java` | Non-transactional cancellation orchestrator |
| `service/WithdrawalBillingCleanupService.java` | Non-transactional cleanup orchestrator |
| `service/BillingAgreementCleanupTransactionService.java` | New short cleanup claim/result phases |
| `service/UserSubscriptionService.java` | Remove charged-upgrade outer transaction and split local-only planning |
| `service/SubscriptionUpgradePaymentExecutor.java` | `NEVER` provider boundary; no suspension |
| `service/AdminPaymentRefundService.java` | Lease-aware recovery orchestration and fenced result token |
| `service/PaymentRefundTransactionService.java` | Stale reclaim, same-key snapshot, and result fencing |
| `service/PaymentReconciliationService.java` | No-transaction scheduler and per-order orchestration |
| `service/PaymentReconciliationTransactionService.java` | New claim/result evidence gate and Incident transitions |
| provider lookup result/adapter | Exact provider/order/amount/currency/transaction evidence |
| `schema.sql` | Additive columns/indexes in Section 9.1 |
| `schema.sql` | Complete fresh-only V1 schema; apply once and validate |

## 12. Disjoint Implementation Work Items

The slices below were implemented with disjoint production-file ownership.

| Slice | WI | Commit | Closure |
|---|---|---|---|
| A - Schema and entity foundation | `WI-20260715-ATS-001` | `103fdf4` | Additive entity/fresh-schema/manual-patch foundation and static contract; no retained DB apply |
| B - Payment command core | `WI-20260715-ATS-002` | `77c2ebd` | F-01 behavior, canonical command locks, reconciliation-safe finalizers, and cleanup projections |
| C - Cancellation and withdrawal cleanup | `WI-20260715-ATS-004` | `49e8774` | Cancellation/withdrawal F-02 boundaries and fenced cleanup results |
| D - Charged upgrade orchestration | `WI-20260715-ATS-005` | `45daf18` | Charged-upgrade F-02 boundary and finalize-only recovery |
| E - Refund lease recovery | `WI-20260715-ATS-003` | `f5bbd7b` | F-03 crash recovery, stale reclaim, same-key replay limits, and fencing |
| F - Reconciliation and finalize-only | `WI-20260715-ATS-006` | `d0bc21b` | Scheduled F-02 boundary plus F-04 evidence and purpose finalization |
| G - MySQL independent proof | `WI-20260715-ATS-007` | `830c8dd` | Fresh disposable MySQL 8/InnoDB validation and all seven exact races |

Slices B through F may add only tests for their owned production files. Slice G
does not change production code. Any required cross-slice production change is
returned to its owner rather than edited in the consuming slice.

Post-package evidence:

- `WI-20260715-ATS-008` (`1ecfe5c`) corrected completed-renewal idempotency and reconciliation convergence before the final Package G run.
- `WI-20260715-ATS-009` and `WI-20260715-ATS-010` (`3f18fed`) remain the historical FAIL reviews that found two P1 and two P2 follow-ups.
- `WI-20260715-ATS-011` (`46edd88`) corrected refund `NEVER` enforcement, SUBSCRIBE state gates, retry-gate consumption, and payment-key minimization.
- `WI-20260715-ATS-012` (`14053e6`) independently reviewed those four corrections and returned PASS with one non-blocking P3 test gap.

This implementation and review chain closes F-01 through F-05 at the current
code/test boundary. Retained-database migration remains outside the V1 baseline;
the chain does not unblock live Toss, production deployment, client acceptance,
or non-payment release gates.

## 13. Rollout, Rollback, and Residual Risk

### 13.1 Rollout

1. Implement and validate the additive fresh-schema contract.
2. Run all behavior tests with fake providers.
3. Run the concurrency suite on an empty disposable MySQL 8/InnoDB schema.
4. Apply the fresh-only `schema.sql` and `seed.sql` baseline once to a
   verified-empty MySQL 8 database, then start with `ddl-auto=validate`.
5. Treat any future retained-data migration as a separate approved requirement
   with its own rehearsal and rollback plan.
6. Deploy code with renewal, reconciliation mutation, cleanup worker, charged
   upgrade, and refund execution initially paused; enable after validation.

### 13.2 Rollback

- Roll back application behavior first and pause all payment mutation and
  scheduler entry points. Returning directly to the old renewal code while an
  agreement has `renewal_retry_at` is unsafe because it can change command
  identity again.
- Keep all additive columns, indexes, ENUM members, command keys, payment rows,
  refund rows, and audit/Incident rows.
- Do not contract ENUMs or drop constraints during an incident. MySQL DDL
  implicitly commits.
- A failed copied-DB rehearsal is discarded/restored. A shared database is
  never modified or rolled back by this design WI.
- Refunds left `PROCESSING` after application rollback require operator review;
  do not create a replacement refund or idempotency key.

### 13.3 Residual risks

- The same-key refund replay depends on the provider's verified idempotency
  retention contract. The 24-hour local ceiling must be disabled if it is not
  safely inside that contract.
- Billing-key deletion has no money-command idempotency key; stale cleanup is
  intentionally Incident-only and may require operator action.
- Single-server orchestration avoids distributed ownership. A future
  multi-server deployment requires a separate lease/ownership design.
- Retained database applicability remains unknown until the approved copied-DB
  inventory and row-specific preflight are complete.
