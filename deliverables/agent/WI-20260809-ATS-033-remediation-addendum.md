---
version: 1.0
last_updated: 2026-08-12
project: ATS
owner: SA
category: agent
status: accepted
dependencies:
  - path: ../../docs/standards/core-principles.md
    reason: Approved execution and platform-integrity rules
  - path: ../../docs/standards/development-standards.md
    reason: Transaction, implementation, and test standards
  - path: ../../docs/policies/security-policy.md
    reason: Payment isolation and no-secret/no-real-effect boundary
  - path: ../user/REQ-20260809-ATS-001.md
    reason: Approved release-rehearsal remediation scope
  - path: WI-20260809-ATS-033-handoff.md
    reason: Approved WI-033 execution contract
  - path: WI-20260809-ATS-033-pg-review.md
    reason: Privacy and security pre-review conditions
  - path: WI-20260809-ATS-033-qa-integ-review.md
    reason: Cross-layer pre-review matrix
  - path: WI-20260809-ATS-033-re-review.md
    reason: Independent BLOCK findings remediated by this addendum
---

# WI-20260809-ATS-033 Remediation Addendum

> Purpose: Freeze the smallest architecture contract that resolves the independent RE BLOCK without changing WI-034 ownership or authorizing real external effects.

## TL;DR

**SA decision: APPROVED FOR REMEDIATION IMPLEMENTATION; WI-033 remains blocked from acceptance until the required evidence is green.**

- A non-null prepare `command_key` is the immutable logical command identity and
  survives confirmation unchanged. `provider_idempotency_key` remains the
  provider-attempt fence.
- The raw UUID is namespaced by authenticated owner before hashing. The same raw
  UUID under a different owner is an independent attempt namespace and cannot
  discover the first owner's use.
- Same-owner tuple mismatch remains `409 PAYMENT_PREPARE_ATTEMPT_CONFLICT` and is
  not replaceable. Expiry and safe terminal history receive distinct codes.
- The frontend accepts only lowercase canonical UUIDv4 values. It never repairs
  or rotates corrupt state automatically, but an explicit recovery action may
  discard the one corrupt context record and generate a fresh attempt.
- First-agreement creation uses a committed unique insert claim, followed by a
  fresh-transaction reread and normal aggregate locking. It must not use a
  missing-row `SELECT ... FOR UPDATE` as the creation mutex.

This addendum is authoritative only for the contradictions and remediation
points named below. All non-conflicting WI-033 handoff, PG, and QA-INTEG clauses
remain in force.

## 1. Reviewed Baseline

- Branch: `codex/v1-release-rehearsal-fixes`
- Reviewed HEAD: `e343c2085fbc82c66b44fb8e5edde35bf920980f`
- The review included the current shared-worktree WI-033 changes, including
  untracked prepare transaction and concurrency test files.
- Primary current evidence:
  - `PaymentOrder.claimProviderAttempt` rejects replacement of a non-null
    `commandKey` and separately advances `providerIdempotencyKey`
    (`src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java:167-188`).
  - `PaymentCommandTransactionService.claimBillingConfirm` currently derives
    `billingConfirm(orderID)` unconditionally, which conflicts with a prepare
    digest already stored on the order
    (`src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:214-217`).
  - `PaymentCommandKeyFactory.billingAgreementPrepare` already creates a
    versioned owner-scoped digest from a lowercase canonical UUIDv4
    (`src/main/java/com/atstudio/atstudio/service/PaymentCommandKeyFactory.java:17-35`).
  - Prepare currently maps tuple mismatch, expiry, and terminal lifecycle to one
    `PAYMENT_PREPARE_ATTEMPT_CONFLICT` response
    (`src/main/java/com/atstudio/atstudio/service/BillingAgreementPrepareTransactionService.java:191-216,302-303`).
  - The frontend UUID regex is case-insensitive and its replacement set includes
    generic prepare conflict plus a backend code that does not yet exist
    (`frontend/src/utils/checkoutPrepareAttempt.ts:28-35,94-102` and
    `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:227-239,274-283`).
  - The first-agreement path performs a locking read for an agreement that may
    not exist, then inserts in the same transaction; the current MySQL test only
    synchronizes the application-call boundary
    (`src/main/java/com/atstudio/atstudio/service/BillingAgreementPrepareTransactionService.java:178-188`,
    `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java:47-53`,
    and `src/test/java/com/atstudio/atstudio/service/MysqlRaceTestSupport.java:26-44`).

## 2. Superseded Clauses

The following earlier clauses are superseded. Historical files remain unchanged
as audit records.

| Earlier clause or assumption                                                                            | Authoritative remediation resolution                                                                                                                                                                                              |
| ------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Reusing the same raw key with a changed `user` scope must return 409.                                   | Owner identity is the digest namespace, not a tuple field inside a global namespace. Another owner cannot match the first digest and proceeds through that owner's own authoritative validation.                                  |
| Same key plus a changed `user`, purpose, plan/audience, or cycle is one conflict class.                 | Only same-owner changes to purpose, exact plan, validated audience, or cycle are tuple conflicts. Cross-owner reuse is isolated before lookup and is never evidence that another owner used the raw UUID.                         |
| A new frontend key is permitted only after an expired or terminal server result.                        | Expired and safe terminal results still require explicit replacement. A corrupt or noncanonical local record also permits an explicit discard-and-create action, while automatic repair or rotation remains forbidden.            |
| `PAYMENT_PREPARE_ATTEMPT_CONFLICT` covers changed tuple, expiry, and terminal state and is replaceable. | The code is reserved for same-owner tuple conflict and is not replaceable. Expiry and safe terminal history have separate response codes.                                                                                         |
| `PAYMENT_ORDER_TERMINAL` may be used only as a frontend string even though the backend never emits it.  | Add one backend `PAYMENT_ORDER_TERMINAL` business error because existing `PAYMENT_ORDER_INVALID_STATE` must remain non-replaceable for in-flight, successful, and unknown-outcome states. No response-shape change is authorized. |
| Legacy null `command_key` rows are never assigned a key under any path.                                 | Prepare still ignores and never backfills null-key history. Confirm may assign the existing `billingConfirm(orderID)` key only when the specifically selected order has a truly null key.                                         |
| Confirm may derive and claim `billingConfirm(orderID)` for every initial order.                         | Confirm reuses every non-null order `command_key` verbatim. It derives `billingConfirm(orderID)` only for a selected legacy null-key order.                                                                                       |
| A missing agreement row can be serialized by the current `SELECT ... FOR UPDATE` before insert.         | Missing-row locking is not the creation mutex. First creation uses a unique insert in a short transaction and a post-commit loser reread in a new transaction.                                                                    |

The cross-owner 409 wording in the WI handoff acceptance criteria and the
QA-INTEG changed-user matrix is therefore superseded. PG's required isolation
outcome remains satisfied: no second owner may select, disclose, mutate, or
confirm the original order.

## 3. Frozen Command And Provider Fences

### 3.1 Prepare command identity

- The accepted request key grammar is exactly lowercase canonical UUIDv4:
  `^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$`.
- No trimming, lowercasing, case folding, parsing-and-reserializing, or fallback
  generation is allowed at the API boundary.
- The stored prepare claim remains
  `BILLING_PREPARE:v1:<64-lowercase-hex-sha256>`, derived from the version,
  authenticated owner ID, and raw canonical UUID. The stored value contains no
  plaintext raw UUID or owner ID.
- Once `PaymentOrder.commandKey` is non-null, it is immutable for prepare,
  confirm, charge, callback, reconciliation, failure, retry, and cleanup paths.

### 3.2 Confirm claim selection

The initial confirm transaction must select its command fence as follows:

```java
String commandKey = order.getCommandKey();
if (commandKey == null) {
    commandKey = keyFactory.billingConfirm(orderID);
}

int providerAttempt = order.getProviderAttempt() + 1;
String providerIdempotencyKey = keyFactory.billingInitialAttempt(orderID, providerAttempt);
order.claimProviderAttempt(commandKey, providerIdempotencyKey, claimedAt);
```

This requires no weakening of `PaymentOrder.claimProviderAttempt`:

- A prepare-created order passes its existing digest, so the immutable-key guard
  remains active and the value is unchanged.
- A selected legacy null-key order may transition once from null to the existing
  `BILLING_CONFIRM:<orderID>` value.
- A non-null value is never replaced, including a legacy non-prepare value.
- `provider_idempotency_key`, not `command_key`, identifies the concrete
  Provider attempt and continues to advance with `providerAttempt` under the
  existing state machine.

Prepare lookup must continue to ignore all null `command_key` rows. The confirm
exception is order-ID scoped and is not a backfill, scan, or migration.

## 4. Backend Prepare Decision Order

The prepare path must classify in this order after authentication, role checks,
header validation, and authoritative request resolution:

1. Derive the owner-scoped digest from the authenticated owner and accepted raw
   UUID.
2. Resolve purpose, exact plan, plan audience, billing cycle, active
   Subscription, and Billing Agreement state from server data.
3. If an order exists for that owner-scoped digest, compare the full stored and
   authoritative tuple before lifecycle classification.
4. Return `PAYMENT_PREPARE_ATTEMPT_CONFLICT` only when the same owner's purpose,
   exact plan ID, validated audience, billing cycle, agreement identity, or
   active Subscription identity differs.
5. For an exact tuple, classify expiry before all other order lifecycle states.
6. For an exact, unexpired tuple, apply the status groups in Section 5.
7. Reuse only `READY` or `IN_PROGRESS` with a `READY` Billing Agreement and all
   authoritative eligibility checks still satisfied.
8. Create only when no non-null owner-scoped command claim exists and all
   authoritative checks pass.

A precondition catch must not translate every unrelated business error to tuple
conflict merely because a claim exists. Translation to 409 requires an observed
same-owner tuple difference. Invalid ownership, eligibility, Provider
configuration, or Billing Agreement state retains its own existing error.

## 5. Exact Backend And Frontend State Matrix

`PAYMENT_ORDER_TERMINAL` is the only new business error authorized by this
addendum. It uses HTTP 409 and means that the exact old prepare order is in a
safe-to-replace terminal state. Existing error codes are reused everywhere they
are semantically sufficient.

| Request and durable state                                                                                                           | API result                                                                                                                                                       | Durable and Provider result                                                                                          | Frontend key/control result                                                                                                             |
| ----------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| Missing, blank, uppercase, malformed, non-v4, wrong-variant, oversized, or control-character header                                 | 400 `PAYMENT_PREPARE_IDEMPOTENCY_KEY_INVALID`                                                                                                                    | No repository/Provider call and no mutation                                                                          | Never auto-generate. An explicit recovery action may replace the local context key.                                                     |
| No owner-scoped claim; authoritative tuple is valid                                                                                 | 201 with a new order                                                                                                                                             | One own agreement claim/order as needed; pure prepare descriptor outside the transaction                             | Persist and keep the generated key for this attempt.                                                                                    |
| Same owner/key, exact tuple, `READY` or `IN_PROGRESS`, unexpired, agreement `READY`                                                 | 201 with the same `orderId` and equal authoritative response                                                                                                     | No new order and no command-key change. A repeated pure descriptor call is allowed and must remain side-effect-free. | Reuse the same key across remount, reload, network retry, and same-attempt retry.                                                       |
| Same owner/key with changed purpose, exact plan, audience, or cycle                                                                 | 409 `PAYMENT_PREPARE_ATTEMPT_CONFLICT`                                                                                                                           | No mutation and zero Provider calls; original order is unchanged                                                     | Not replaceable. Keep the key; ordinary same-attempt retry or navigation may occur, but no new-attempt control is enabled by this code. |
| Same raw UUID under another authenticated owner                                                                                     | Normal independent namespace; commonly 201 when the second owner's tuple is valid                                                                                | Cannot match or expose the first order. May create/reuse only the second owner's agreement/order and digest.         | Treat as that owner's own attempt; never disclose that the UUID exists elsewhere.                                                       |
| Same owner/key, exact tuple, `expiresAt <= now` or status `EXPIRED`                                                                 | 400 `PAYMENT_ORDER_EXPIRED`                                                                                                                                      | No reuse, no Provider call, no command-key rewrite; old row remains history                                          | Explicit new-attempt control enabled; no automatic rotation.                                                                            |
| Same owner/key, exact tuple, unexpired status `FAILED` or `CANCELLED`, and existing Billing Agreement state permits a fresh prepare | 409 `PAYMENT_ORDER_TERMINAL`                                                                                                                                     | No reuse, no Provider call, no command-key rewrite; old row remains history                                          | Explicit new-attempt control enabled; no automatic rotation.                                                                            |
| Same owner/key, exact tuple, status `PROCESSING`, `PROVIDER_SUCCEEDED`, or `PENDING_PROVIDER_CONFIRMATION`                          | 400 `PAYMENT_ORDER_INVALID_STATE`                                                                                                                                | No prepare replacement or Provider call. Existing confirm/reconciliation ownership remains intact.                   | Not replaceable. Do not generate a second attempt; WI-034 recovery owns the outcome.                                                    |
| Same owner/key whose order is `DONE`, or whose agreement is active/otherwise authoritatively unusable                               | Existing `PAYMENT_ORDER_INVALID_STATE`, `BILLING_AGREEMENT_ALREADY_ACTIVE`, `BILLING_AGREEMENT_INVALID_STATE`, or authoritative Subscription error as applicable | No prepare replacement, disclosure, or Provider call                                                                 | Not replaceable; existing completed/current-state UX applies.                                                                           |
| Legacy null-key order exists during prepare                                                                                         | It is not a replay claim; a valid new key follows the normal no-claim path                                                                                       | Legacy row remains null and unchanged; a new own order may be created                                                | Normal new attempt for the valid context.                                                                                               |
| Confirm selects an order with a non-null prepare digest                                                                             | Existing confirm response/state contract                                                                                                                         | Preserve the digest; create only the next provider-attempt key                                                       | No prepare-key rotation or callback-policy change.                                                                                      |
| Confirm selects a truly null-key legacy order                                                                                       | Existing confirm response/state contract                                                                                                                         | Assign `BILLING_CONFIRM:<orderID>` once, then preserve it; provider-attempt key remains separate                     | No prepare storage change.                                                                                                              |

Future statuses are non-replaceable by default. Adding one to the safe terminal
group requires an explicit lifecycle review; it must not be inferred from the
enum name alone.

## 6. Frontend Attempt Contract

### 6.1 Canonical validation and storage

- Remove the case-insensitive regex flag. Stored and newly generated keys must
  pass the same lowercase canonical UUIDv4 predicate.
- Reject uppercase rather than normalizing it. Reject malformed JSON, wrong
  version, context mismatch, unsafe plan ID, and invalid UUID in the same local
  corruption category.
- `getOrCreateCheckoutPrepareAttempt` may create a key only when the exact
  context record is absent. It must not overwrite a present invalid record.
- Validate `crypto.randomUUID()` output before writing or sending it. An invalid
  generator result writes nothing and is not normalized.
- The explicit new-attempt operation may overwrite only the exact
  purpose/plan/audience/cycle session record selected by the current page.

### 6.2 Replacement classification

The prepare-page explicit replacement set is exactly:

```text
PAYMENT_PREPARE_IDEMPOTENCY_KEY_INVALID
PAYMENT_ORDER_EXPIRED
PAYMENT_ORDER_TERMINAL
```

`CorruptCheckoutPrepareAttemptError` is the equivalent local-only recovery
signal. `PAYMENT_PREPARE_ATTEMPT_CONFLICT`, `PAYMENT_ORDER_INVALID_STATE`, an
arbitrary HTTP 409, network errors, and Provider/configuration errors are not in
the set. All replacement signals enable a user control only; none performs a
rotation inside an error handler or retry effect.

## 7. Deterministic First-Agreement Claim

### 7.1 InnoDB risk analysis

The current absent-row path cannot rely on
`BillingAgreementRepository.findByUserIDAndProviderForUpdate` as a mutex. When
there is no record, there is no existing aggregate row to lock. Under an InnoDB
locking-read access path, the scanned range may be protected by a gap or
next-key lock. Gap locks can coexist, while later inserts require insert
intention and unique-index locks. Two contenders can therefore both observe
absence and later enter a lock cycle or produce a deadlock victim instead of the
intended named unique-claim loser.

The MySQL manual confirms that locking reads can lock scanned ranges, that gap
locks coexist and inhibit inserts, that inserts take insert-intention locks, and
that duplicate-key locking can itself participate in deadlocks:

- [InnoDB Locking](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking.html)
- [Locks Set by Different SQL Statements](https://dev.mysql.com/doc/refman/8.0/en/innodb-locks-set.html)
- [Deadlocks in InnoDB](https://dev.mysql.com/doc/refman/8.0/en/innodb-deadlocks.html)

Changing the database isolation level is not part of this remediation and does
not replace a sound claim algorithm.

### 7.2 Production algorithm

Use two local transaction phases before the pure Provider descriptor:

1. **Ensure a committed Billing Agreement claim.** In a short `REQUIRES_NEW`
   transaction, perform a non-locking unique probe for `(user_id, provider)`.
   If present, return its ID. If absent, insert and flush a new agreement without
   first issuing a missing-row locking read, then commit.
2. **Handle the unique loser outside the failed transaction.** Catch only the
   named `uq_billing_agreements_user_provider` violation after the transaction
   has rolled back. Start a fresh bounded `REQUIRES_NEW` attempt and reread the
   now-committed agreement. If the winner rolled back and no row exists, retry
   the insert within the same existing bound. No sleep or unbounded polling is
   allowed.
3. **Claim the prepare order.** In a separate `REQUIRES_NEW` transaction, lock
   the existing Billing Agreement first, then the active User Subscription,
   then the Payment Order command claim. Validate owner, full tuple, lifecycle,
   and eligibility before reuse or creation.
4. **Handle an order unique loser identically.** Only the named
   `uq_payment_orders_command_key` violation is retryable. The failed transaction
   must end before a new transaction locks and rereads the committed order.
5. **Commit before Provider prepare.** Return the committed claim to the
   non-transactional application service, invoke only the pure deterministic
   test/V1 descriptor, then finalize under the same aggregate order.

The explicit write-lock order remains:

```text
BillingAgreement -> UserSubscription -> PaymentOrder -> SubscriptionPayment -> PaymentRefund
```

Loading `User` for ownership or a foreign-key association is not an explicit
`User FOR UPDATE` lock. No path may call `UserRepository.findByIdForUpdate` as a
first-agreement mutex. A committed `READY` agreement with no order after a
process interruption is recoverable local state and must be reusable by the
next phase; it is not cleaned up destructively.

Deadlock, lock timeout, connection failure, an unnamed integrity error, or a
generic 5xx is not an accepted idempotency loser. The WI-033 bounded retry is
limited to the two named unique claims.

### 7.3 Deterministic MySQL synchronization

Replace application-boundary-only start synchronization with transaction-phase
barriers in the fresh disposable MySQL test. A test spy or package-private test
probe may observe repository phases; it must not alter production decisions.

1. Start two workers on separate threads and connections. Assert each claim
   transaction is active and independent.
2. Barrier both workers after their non-locking agreement probes have returned
   empty and before either insert proceeds.
3. Let the first real `saveAndFlush` complete its agreement insert, then hold
   that transaction before method return/commit.
4. Let the second worker enter its real insert path. Record that it had already
   observed absence. Release the first transaction only after the second insert
   path is entered.
5. Capture one exact `DataIntegrityViolationException` whose most specific
   cause names `uq_billing_agreements_user_provider`. The exception is an
   observed test event even though the application converts it into a bounded
   retry.
6. Record that the loser starts a new transaction after the unique exception,
   rereads the committed winner agreement ID, then returns the same command
   order ID after the canonical locks and full validation.
7. Assert exactly one agreement, one command-key order, equal successful
   responses, one immutable command key, no SQLState deadlock/timeout, and only
   pure Provider-double calls outside transactions.

Use latches/barriers with strict failure timeouts, not sleeps. Invocation counts
alone are insufficient: evidence must include both the named unique loser event
and the fresh-transaction reread of the winner's IDs. H2 remains supplemental.

## 8. Required Remediation Tests

### 8.1 Backend command and lifecycle tests

- [ ] A real application-service prepare followed by a real
      `PaymentCommandTransactionService` confirm uses a test Provider only,
      succeeds through the intended local state transition, preserves the
      prepare digest byte-for-byte, and uses
      `billing-initial-<orderID>-attempt-1` only as the Provider fence.
- [ ] A selected legacy null-key order confirms with the existing
      `BILLING_CONFIRM:<orderID>` fallback, while a non-null legacy or prepare
      key cannot be replaced.
- [ ] A legacy null-key row is ignored by prepare lookup and remains null after
      a valid new key creates a different order.
- [ ] Exact replay returns the same order/response for `READY` and
      `IN_PROGRESS` without another durable order.
- [ ] Each same-owner purpose, plan, audience, and cycle change returns
      `409 PAYMENT_PREPARE_ATTEMPT_CONFLICT`, performs zero mutation/Provider
      calls, and is not marked replaceable.
- [ ] Exact expiry returns `PAYMENT_ORDER_EXPIRED`; an explicitly supplied fresh
      canonical key creates a different order while preserving the old row and
      key.
- [ ] Exact `FAILED` and `CANCELLED` history returns
      `PAYMENT_ORDER_TERMINAL`; an explicitly supplied fresh key creates a new
      order only when existing Billing Agreement and Subscription policy allows
      it.
- [ ] `PROCESSING`, `PROVIDER_SUCCEEDED`, and
      `PENDING_PROVIDER_CONFIRMATION` return non-replaceable
      `PAYMENT_ORDER_INVALID_STATE`; no new prepare intent or Provider call is
      created.
- [ ] `DONE` and active/agreement-invalid paths remain non-replaceable and use
      their existing authoritative errors.
- [ ] The same raw UUID for two eligible owners creates two different digests
      and own orders. Owner B cannot fetch, confirm, disclose, or mutate owner
      A's order, and a failed cross-owner order-ID action invokes no Provider.

### 8.2 Frontend tests

- [ ] Helper tests reject uppercase UUIDv4, malformed UUID, malformed JSON,
      wrong version, and context mismatch without calling `randomUUID` or
      modifying the stored bytes.
- [ ] Generated UUID output is validated as lowercase canonical v4 before
      storage and transport.
- [ ] Page tests show explicit recovery for corrupt/uppercase local state,
      prove no prepare API call and no storage write before the click, then
      prove one new canonical key is stored after the click.
- [ ] Replacement classification is exactly invalid-key, expired, and terminal;
      tuple conflict, invalid state, arbitrary 409, and network failure retain
      the existing key and do not expose a new-attempt action.
- [ ] API transport tests use a realistic lowercase canonical UUIDv4 and prove
      header-only transport.
- [ ] StrictMode remount, reload, and network/same-attempt retry continue to use
      one key.

### 8.3 MySQL and regression tests

- [ ] The strengthened first-agreement MySQL test records both the named unique
      loser and post-commit reread described in Section 7.3.
- [ ] Existing-agreement same-key concurrency still serializes through the
      agreement lock and converges on one order.
- [ ] Same-owner changed-tuple concurrency produces one winner and one exact
      tuple conflict with no Provider call for the loser.
- [ ] Fresh disposable MySQL 8/InnoDB guards, exact schema-name validation, and
      teardown remain intact. No retained schema is used.
- [ ] Focused entity, controller, prepare, confirm, Provider, frontend, and
      adjacent WI-032 tests pass before the full quality gates run.

Every test conclusion must report UI behavior, API/server result, Provider
invocation/state, and durable state separately. A row count does not prove
Provider safety, and response equality does not prove commit visibility.

## 9. WI-034 And No-Real-Effects Boundary

- WI-033 owns prepare attempt identity, local claim/reuse, and the preservation
  of that identity into confirm claim setup.
- WI-034 remains the sole owner of callback response loss, unknown Provider
  outcome, reload recovery after confirm starts, and reconciliation semantics.
- This remediation must not make `PROCESSING`, `PROVIDER_SUCCEEDED`, or
  `PENDING_PROVIDER_CONFIRMATION` replaceable merely to unblock the prepare UI.
- Tests may use deterministic Provider doubles for prepare, billing-key issue,
  and charge responses. They must not invoke a real Provider/SDK, real charge,
  refund, cancellation, billing authorization, mail delivery, retained DB,
  deployment, or secret/configuration source.
- No schema change, backfill, destructive cleanup, or retained-data operation is
  authorized.

## 10. Stop Conditions

Stop remediation and return to the main agent without broadening the patch if:

- preserving a non-null prepare digest requires weakening the immutable
  `command_key` guard or reusing it as the Provider-attempt fence;
- cross-owner 409 requires a global raw-key lookup, an unscoped digest, a new
  index, or any existence disclosure;
- safe terminal replacement cannot remain distinct from in-flight or unknown
  Provider outcome without a larger response-schema change;
- first-agreement convergence requires a `User`-first write lock, isolation
  change, schema change, table lock, unbounded retry, or sleep-based proof;
- the mandatory MySQL run produces a deadlock, lock timeout, unnamed integrity
  error, generic 5xx, or cannot observe both the named unique loser and the
  post-commit reread;
- a fresh disposable MySQL 8/InnoDB target cannot be provided without reading
  ignored configuration or exposing credentials;
- a real Provider, mail, retained database, deployment, destructive action, or
  WI-034 recovery decision becomes necessary;
- implementation would modify files outside the approved WI-033 product, test,
  and later closeout-document inventory.

Until the real prepare-to-confirm regression and deterministic disposable MySQL
evidence pass, the independent RE decision remains **BLOCK**.

## Related Documents

### Required References

- [WI-033 Handoff](WI-20260809-ATS-033-handoff.md): Approved base contract
- [WI-033 PG Review](WI-20260809-ATS-033-pg-review.md): Security conditions
- [WI-033 QA-INTEG Review](WI-20260809-ATS-033-qa-integ-review.md): Cross-layer conditions
- [WI-033 RE Review](WI-20260809-ATS-033-re-review.md): Independent BLOCK findings

### Reference Documents

- [Core Principles](../../docs/standards/core-principles.md): System constitution
- [Development Standards](../../docs/standards/development-standards.md): Implementation and test standards
- [Security Policy](../../docs/policies/security-policy.md): Payment and no-secret boundary
