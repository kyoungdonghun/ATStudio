# WI-20260809-ATS-033 PG Pre-Implementation Review

**Date:** 2026-08-10

## Decision

**APPROVE WITH CONDITIONS**

Historical blockers PG-B01..PG-B04 are superseded by the approved revised handoff.
This is a contract-level pre-implementation approval; implementation evidence is
still required before final acceptance.

## Resolved Blockers

| Finding                              | Revised handoff resolution                                                                                               | Status              |
| ------------------------------------ | ------------------------------------------------------------------------------------------------------------------------ | ------------------- |
| PG-B01: replacement attempt identity | One opaque key per attempt; new key only through explicit new-attempt action; immutable historical command key           | Resolved/superseded |
| PG-B02: first-agreement race         | Canonical aggregate order, no `User`-first lock, committed claim and bounded post-commit loser reread                    | Resolved/superseded |
| PG-B03: Provider prepare replay      | Pure deterministic side-effect-free descriptor outside the local transaction; Provider mutation begins at confirm/charge | Resolved/superseded |
| PG-B04: H2 evidence limitation       | H2 is supplemental; fresh disposable MySQL 8/InnoDB is mandatory for lock and commit-visibility proof                    | Resolved/superseded |

## Implementation Conditions

1. Validate the required `Idempotency-Key` header, including missing, blank,
   malformed, oversized, and control-character values, before lookup or effects.
2. Persist only a versioned opaque owner-scoped digest; never persist or log the
   raw key, PII, provider keys, card data, callback material, credentials, or secrets.
3. Bind the claim to the full authoritative user, purpose, exact plan ID,
   validated audience, and billing-cycle tuple; changed tuples return stable HTTP
   `409` before mutation or Provider invocation.
4. Enforce cross-user isolation: another authenticated user cannot select,
   disclose, mutate, or reuse the original order.
5. Keep `command_key` immutable. Ignore legacy null-command-key rows and never
   rewrite, backfill, delete, or use them as replay claims.
6. Acquire no `User`-first lock. Preserve
   `BillingAgreement -> UserSubscription -> PaymentOrder -> SubscriptionPayment ->
PaymentRefund`; uniqueness losers retry only after winner commit and reread
   with full owner, tuple, and lifecycle validation.
7. Keep `prepareAgreement` pure, deterministic, side-effect-free, and outside
   the local transaction. A non-compliant future Provider fails closed.
8. Label H2 as supplemental and provide fresh disposable MySQL 8/InnoDB proof
   for canonical lock order, race convergence, commit visibility, and loser reread.
9. Use no real Provider or retained database effects in implementation evidence.

The accepted header grammar and maximum size must come from authoritative current
constraints; this review introduces no new security or policy bound.

## Required Negative Evidence

- Invalid or unauthenticated requests fail before repository or Provider work.
- Same key under another user cannot return or mutate the original order.
- Same key with changed purpose, plan, audience, or cycle returns `409` with zero
  mutation and zero Provider descriptor invocation.
- Expired or terminal orders are not reused; a new key is required for a fresh attempt.
- WI-032 authentication, audience, exact-plan, cycle, purpose, and subscription
  rejection paths remain effect-free.

## Required Concurrency Evidence

- Sequential and concurrent exact replays produce one durable order and equal
  server-authoritative responses.
- Independent transactions prove one committed claim, winner-commit visibility,
  bounded loser retry, and full post-commit validation without deadlock or 5xx.
- MySQL evidence proves the canonical lock order and a same-key/changed-tuple
  conflict with zero mutation and zero Provider descriptor invocation.

## No-Real-Effects Boundary

All tests use a test Provider and isolated disposable state only. No real Provider
or SDK call, charge, refund, cancellation, mail, retained database, deployment,
secret inspection, or preserved ZIP access is permitted.

## Re-Review Gate

RE review is required after SE supplies green focused tests, supplemental H2,
fresh disposable MySQL 8/InnoDB evidence, Provider purity evidence, and the final
bounded diff. Any additional schema, security, policy, dependency, or Provider
flow decision must stop and escalate. WI-034 remains out of scope.
