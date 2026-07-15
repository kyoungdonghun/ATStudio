# WI-20260715-ATS-006 Summary

## Outcome

Package F is implemented. Scheduled reconciliation now performs provider lookup outside a local transaction, accepts only exact provider `DONE` evidence, persists provider success before local finalization, and dispatches only the existing `SUBSCRIBE`, `UPGRADE`, or `RENEWAL` finalizer. It never issues a new provider charge.

## Delivered Behavior

- Candidate paging and local claim/revalidation run in short transactions; the scheduler and provider lookup boundary use `Propagation.NEVER`.
- Lookup evidence now includes provider, exact order ID, status, amount, currency, authoritative transaction ID, and sanitized provider evidence.
- Provider/order/status/amount/currency/transaction conflicts remain Incident-only and do not mutate payment, agreement, subscription, or entitlement state.
- Exact `DONE` evidence opens or refreshes the matching Incident before mutation, records `PROVIDER_SUCCEEDED`, finalizes by purpose, and resolves the Incident after success.
- A local finalizer failure leaves `PROVIDER_SUCCEEDED` and the Incident open. The next reconciliation performs lookup and finalization only, with no second charge.
- Fresh `PROCESSING` commands remain owned by the live command; only stale `PROCESSING`, `PENDING_PROVIDER_CONFIRMATION`, and finalize-only `PROVIDER_SUCCEEDED` candidates are handled.

## Verification

- Focused and impacted regression tests: **PASS, 50 tests, 0 failures, 0 errors, 0 skipped**.
- Covered `SUBSCRIBE`, `UPGRADE`, and `RENEWAL`; strict evidence conflicts; stale/fresh processing; Incident audit/resolve; provider-success retry; and Toss lookup currency/sanitization.
- The transaction-observing fake recorded `false` at every lookup boundary and its `charge()` count remained `0`.
- `gradlew.bat compileJava`: PASS.
- `git diff --check` and no-index checks for new WI006 files: PASS.

## Boundaries Preserved

- No B-owned payment command, entity, or repository file was modified.
- No provider mutation method, live Toss endpoint, retained/copied database, schema, or preview/public server was accessed or changed.
- Package C/D concurrent changes and runtime log files were left untouched.

## Residual Limits

- Recovery remains detect-only when Toss lookup is unavailable or omits exact currency/payment-key evidence.
- H2 proves orchestration and state convergence, not InnoDB lock behavior; Package G remains responsible for disposable MySQL 8 concurrency proof.
- A transient failure after local finalization but during Incident resolution is reported as unresolved and requires operational follow-up.

WI-20260715-ATS-007 is unblocked by this Package F completion.
