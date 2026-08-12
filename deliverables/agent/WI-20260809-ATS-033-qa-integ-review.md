# WI-20260809-ATS-033 QA-INTEG Re-Review

**Date:** 2026-08-10

## Verdict

**APPROVE WITH CONDITIONS**

The user-approved revised handoff closes the prior cross-layer blockers. It
authorizes the required `Idempotency-Key` header, frontend attempt-key lifetime,
owner-scoped durable claim and post-commit retry, pure Provider descriptor, and
fresh disposable MySQL 8/InnoDB proof. No unresolved QA-INTEG blocker remains at
contract level; implementation evidence is still required before final
acceptance.

## Superseded Gaps

| Previous gap                            | Approved resolution                                                                                                                                   | Status              |
| --------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------- |
| Attempt identity versus expired history | One opaque key per attempt; only an explicit new attempt after expired/terminal result rotates it; historical `command_key` remains immutable         | Resolved/superseded |
| Frontend/reload excluded                | Session-scoped key survives StrictMode remount, reload, network retry, and same-attempt retry                                                         | Resolved/superseded |
| API key location and tuple conflict     | Required HTTP header only; no body fallback; same key with changed authoritative tuple returns stable HTTP `409` before effects                       | Resolved/superseded |
| First/existing agreement race           | No `User`-first lock; canonical aggregate order plus committed claim and bounded post-commit loser reread                                             | Resolved/superseded |
| Provider replay safety                  | Prepare is an explicitly pure, deterministic, side-effect-free descriptor outside the local transaction; a non-compliant future Provider fails closed | Resolved/superseded |
| H2-only proof                           | H2 is supplemental; fresh disposable MySQL 8/InnoDB is mandatory for convergence and commit visibility                                                | Resolved/superseded |

The handoff's approval record and revised Scope In supersede its stale
evidence-section statements that WI-033 changes no UI contract or that Toss V1
configuration behavior is merely assumed. Those stale statements must not be
used as implementation or evidence criteria.

## Implementation-Verifiable Matrix

| Case                                               | UI/control proof                                                 | API/server proof                                                                                                                                     | Provider proof                                                                                                                 | Durable/concurrency proof                                                                                                                                           |
| -------------------------------------------------- | ---------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| StrictMode remount                                 | One attempt creates one session-scoped key; remount reuses it    | Required header carries the same key and authoritative tuple                                                                                         | Any descriptor call is deterministic, side-effect-free, and outside the transaction                                            | One immutable command claim and one order                                                                                                                           |
| Reload                                             | Reload restores the in-progress prepare key                      | Same key replays the same valid order and authoritative response                                                                                     | No external mutation; record exact invocation count and transaction state                                                      | Post-commit reread returns the same order; this is prepare replay only                                                                                              |
| Network/same-attempt retry                         | Every retry reuses the original key                              | Same key plus same tuple returns the same `orderId` and response without financial-intent mutation                                                   | Record deterministic descriptor behavior; zero external effects                                                                | Sequential and concurrent retries converge to one claim/order                                                                                                       |
| Explicit new attempt after expired/terminal result | Rotation occurs only through the explicit new-attempt action     | Old key is not reused for replacement; new key is independently validated                                                                            | Only the new prepare may obtain a descriptor; still no external mutation                                                       | Old row/key remains immutable history; one new key may create one new order                                                                                         |
| Missing/malformed key                              | No replacement key is silently invented                          | Missing, blank, malformed, oversized, or control-character header fails before repository/DB or Provider work                                        | Zero calls                                                                                                                     | Zero claim/order mutation                                                                                                                                           |
| Same key, changed tuple                            | UI does not repurpose an existing attempt key                    | Changed user scope, purpose, exact plan/audience, or cycle returns stable HTTP `409` before effects                                                  | Zero calls                                                                                                                     | Zero mutation; original claim/order remains unchanged                                                                                                               |
| Cross-user isolation                               | A key copied into another authenticated session grants no access | Ownership and full authoritative tuple are checked before selection, disclosure, mutation, or reuse                                                  | Zero calls for the conflicting user                                                                                            | No cross-user read-through or mutation; original row remains unchanged                                                                                              |
| Existing agreement race                            | Concurrent controls send the same key and tuple                  | Preserve `BillingAgreement -> UserSubscription -> PaymentOrder -> SubscriptionPayment -> PaymentRefund`; no `User`-first lock                        | Descriptor remains pure and outside the transaction                                                                            | Independent transactions converge after winner commit; loser fully rereads owner, tuple, and lifecycle                                                              |
| New agreement race                                 | Concurrent controls send the same key and tuple                  | Committed claim and bounded post-commit retry are used without a `User`-first inversion                                                              | Record exact descriptor calls; all are pure and outside transactions                                                           | One agreement/claim/order; no leaked uniqueness error, deadlock, or generic 5xx                                                                                     |
| Pure descriptor outside transaction                | No real SDK or financial action is initiated by prepare UI       | Server invokes prepare only outside the local transaction                                                                                            | Test double proves deterministic equality, no side effect, and inactive transaction state; non-compliant Provider fails closed | Durable row count is reported separately and is not Provider-purity evidence                                                                                        |
| H2 supplemental                                    | Focused lifecycle and negative tests may use H2-backed flows     | Validate orchestration, response equality, conflict, and effect-free rejection                                                                       | Test double records count/order/state                                                                                          | Label all H2 results supplemental; do not claim InnoDB lock proof                                                                                                   |
| Disposable MySQL 8/InnoDB                          | No retained or shared environment is used                        | Exercise same-key/same-tuple and same-key/changed-tuple independent transactions                                                                     | Test Provider only; no real external effect                                                                                    | Prove canonical lock order, one committed claim/order, winner-commit visibility, bounded loser reread, convergence, and no deadlock/5xx; tear down the fresh schema |
| WI-032 rejection                                   | Rejected flows do not initiate prepare behavior                  | Authentication, subscription state, purpose, audience, exact plan, and cycle checks fail before replay selection, mutation, or descriptor invocation | Zero calls                                                                                                                     | Zero claim/order mutation                                                                                                                                           |
| WI-034 boundary                                    | WI-033 reload/retry preserves only the prepare-attempt key       | WI-033 returns/reconstructs prepare order and descriptor only; it does not decide callback or financial outcome                                      | No confirm, charge, authorization, cancellation, refund, or recovery call                                                      | Callback response-loss, unknown outcome, and financial outcome reconciliation remain exclusively WI-034                                                             |

For every implemented case, evidence must report UI behavior, API/server result,
Provider invocation count/order/transaction state, and post-commit durable state
separately. Equal responses do not prove Provider purity, and one durable row
does not prove the absence of Provider effects.

## Approval Conditions

1. Implement only the approved required-header contract. Do not add an API-body,
   query, optional, or server-generated fallback and do not introduce a schema
   change.
2. Derive and persist only the approved versioned opaque owner-scoped digest;
   keep `command_key` immutable and ignore legacy null-key rows.
3. Enforce full user, server-authoritative purpose, exact plan/audience, cycle,
   and lifecycle validation before reuse; changed tuple and cross-user cases
   must remain effect-free.
4. Preserve the canonical aggregate lock order, committed winner boundary, and
   bounded post-commit loser reread for both existing- and new-agreement races.
5. Prove every Provider descriptor invocation is pure, deterministic,
   side-effect-free, and outside the local transaction. Fail closed for a future
   Provider that cannot satisfy this prepare contract.
6. Treat H2 as supplemental and provide fresh disposable MySQL 8/InnoDB evidence
   for race convergence, lock order, winner-commit visibility, and loser reread.
7. Keep prepare replay strictly separate from WI-034 callback and financial
   outcome recovery. Use no real Provider/SDK, retained database, ZIP, secret,
   deployment, charge, refund, cancellation, or mail action.

Any required schema change or additional API, security, dependency, or
side-effecting Provider decision is outside this approval and must stop and
escalate. Subject to the conditions above, WI-033 may proceed to SE TDD
implementation.
