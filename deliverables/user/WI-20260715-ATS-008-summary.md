# WI-20260715-ATS-008 Completion Summary

## Outcome

- Fixed the renewal completion race exposed by disposable MySQL races 4 and 7.
- A repeated renewal finalizer now treats an already `DONE` order as an idempotent no-op only after validating immutable order identity and one matching committed payment row.
- Reconciliation now converges when the normal renewal finalizer wins after provider lookup: the completed order is validated, the existing payment is reused, and the matching Incident can be resolved without another provider charge or entitlement change.

## Safety Behavior

- A `DONE` renewal with no committed payment evidence fails closed with `PAYMENT_ORDER_INVALID_STATE`.
- A `DONE` renewal whose payment amount or ownership evidence differs also fails closed.
- A `PROVIDER_SUCCEEDED` renewal still validates the current unresolved billing period before applying a new subscription period.
- Canonical lock order remains `BillingAgreement -> UserSubscription -> PaymentOrder -> SubscriptionPayment`.
- The analogous upgrade path was reviewed and left unchanged because its finalization validator uses persisted order relationships, amount, currency, and command key rather than mutable `nextBillingAt` state.

## Verification

- Final focused and impacted regression run: **PASS**.
- Requested test classes: **18**.
- JUnit tests including nested suites: **122 passed, 0 failed, 0 errors, 0 skipped**.
- `git diff --check`: **PASS** for all owned files.
- No MySQL runner, retained database, live Toss endpoint, preview worktree, or public acceptance server was changed by this WI.

## Remaining Dependency

- WI-20260715-ATS-007 must rerun its unchanged disposable MySQL/InnoDB proof. That run remains the final evidence that races 4 and 7 converge under production-engine locking semantics.
