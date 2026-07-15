# WI Summary: WI-20260715-ATS-004

## Status

- **COMPLETE** - Package C cancellation and withdrawal billing-key cleanup is implemented and focused verification passes.

## Delivered

- User cancellation and withdrawal cleanup now run as no-transaction orchestrators.
- A short `REQUIRES_NEW` claim commits cancellation and a 15-minute cleanup lease before provider deletion.
- Provider billing-key deletion runs through a strict `Propagation.NEVER` executor.
- A fenced `REQUIRES_NEW` result phase clears local key material only for provider success or `ALREADY_REMOVED_BILLING_KEY`.
- Deterministic failures become `FAILED`; transport, server, empty, or otherwise unknown outcomes become `PENDING_PROVIDER_CONFIRMATION`.
- Fresh competing cleanup returns `IN_PROGRESS`. Stale `PROCESSING` cleanup is detect-only, opens or updates an Incident and audit entry, and is never replayed automatically.
- Withdrawal retry and stale scans consume Package B's bounded repository projections without modifying repository or payment-command files.

## Behavior Preserved

- User subscription cancellation remains authoritative and retains paid access through its existing `CANCELLED` grace-period semantics.
- Provider cleanup failure does not reactivate or roll back the cancelled agreement or subscription.
- Repeated requests with no retained key return the already-cancelled response.
- Repeated `FAILED` or `PENDING_PROVIDER_CONFIRMATION` requests retain stable recovery evidence and do not call the provider again.
- Billing-key plaintext, provider payloads, and raw exception messages are not persisted in Incident or audit evidence.

## Verification

- Focused and impacted regression tests: **PASS**, 43 tests across 10 suites, 0 failures, 0 errors, 0 skipped.
- Java production and test compilation: **PASS** (`compileJava compileTestJava`).
- `git diff --check`: **PASS** for the current tracked worktree.
- No-index whitespace checks: **PASS** for all six new Package C source/test files.
- No retained/local/production database, live Toss endpoint, or preview/public server was accessed or changed.

## Residual Risk

- H2 proves state transitions, durable short phases, and provider transaction boundaries; it does not prove MySQL/InnoDB lock behavior. Package G remains required for production-engine concurrency proof.
- Ambiguous billing-key deletion intentionally requires operator disposition before retry because provider deletion has no money-command idempotency key.
- Full backend-suite and disposable-MySQL verification remain outside this focused Package C run.
- This WI unblocks `WI-20260715-ATS-007` after the MA confirms the parallel Package D/F chain state.
