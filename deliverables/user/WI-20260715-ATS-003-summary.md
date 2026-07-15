# WI-20260715-ATS-003 Summary

## Status

- Completed Package E refund lease recovery on the Package A baseline (`103fdf4`).

## Delivered

- Added a 15-minute refund processing lease with atomic stale reclaim and a bounded stale-ID repository projection.
- Expanded the claim snapshot to retain the exact refund, provider, payment key, order, amount, currency, reason, idempotency key, and persisted lease.
- Revalidated the complete snapshot before provider mutation and fenced every success, failure, pending, exception, and lookup-only result writer by the exact lease.
- Reused the same refund row and idempotency key for recovery inside the 24-hour Toss replay ceiling.
- Kept elapsed-ceiling recovery lookup-only. Because the current refund provider contract has no exact lookup, the refund returns to `PENDING_PROVIDER_CONFIRMATION`, no provider mutation occurs, and an OPEN Incident is recorded.
- Preserved the existing provider adapter and command contract.

## Validation

- Focused refund tests: PASS, 23 tests, 0 failures/errors/skips.
- Java production and test compilation: PASS.
- `git diff --check`: PASS.
- Provider transaction observation: PASS; the fake provider rejects any invocation with an active local transaction.
- No retained/local/production database, live Toss endpoint, or preview server was accessed or changed.

## Residual Assumptions

- The 24-hour automatic same-key replay ceiling is safe only while it remains inside the verified provider idempotency-retention contract. If that verification changes, mutation recovery must remain disabled and lookup-only.
- The focused integration tests use ephemeral H2. Disposable MySQL/InnoDB concurrency proof remains Package G scope.
- The lease contract assumes the approved single-server orchestration model; multi-server ownership requires a separate design.
