# WI Summary: WI-20260715-ATS-005

## Status

- **COMPLETE** - Package D charged-upgrade orchestration is implemented and focused verification passes.

## Delivered

- `UserSubscriptionService.changeSubscription()` is now a no-transaction orchestrator guarded by `Propagation.NEVER`.
- Subscription lookup, change classification, and local-only mutations run in one short `TransactionTemplate` transaction. The charged path leaves that transaction with IDs only.
- Upgrade claim, provider result persistence, and finalization continue through Package B's short `REQUIRES_NEW` phases.
- `SubscriptionUpgradePaymentExecutor.charge()` now uses `Propagation.NEVER`, so an accidental caller transaction is rejected instead of suspended.
- Both live-command and finalize-only callers now use the three-argument upgrade finalizer, whose target cycle comes from the locked `PaymentOrder.upgradeTargetBillingCycle`.

## Behavior Preserved

- Provider success is persisted as `PROVIDER_SUCCEEDED` before local entitlement and payment-ledger finalization.
- A local finalization failure is recoverable by finalize-only retry without another provider charge.
- Deterministic provider failure remains retryable on the same order and increments the provider attempt.
- Ambiguous provider failure remains `PENDING_PROVIDER_CONFIRMATION` and cannot trigger a blind charge retry.
- Removed billing-key failure still expires the local issued key and returns `BILLING_AGREEMENT_REAUTH_REQUIRED`.
- Zero-amount upgrades remain local-only and do not enter the payment provider flow.

## Verification

- Focused tests: **PASS**, 34 tests, 0 failures, 0 errors, 0 skipped.
  - `UserSubscriptionServiceTest`: 29 tests.
  - `SubscriptionUpgradeCommandIntegrationTest`: 5 tests.
- Java compilation: **PASS** (`compileJava`).
- Owned-file whitespace diff check: **PASS**.
- No retained/local/production database, live Toss endpoint, or preview/public server was accessed or changed.

After the Package D pass, a latest-worktree rerun was blocked during `compileJava` by concurrent Package F files: `PaymentReconciliationTransactionService` currently calls a missing `ProviderPaymentLookupResult.currency()` accessor and declares a conflicting `EvidenceAssessment.exactDone()` record factory. Package D files were not implicated or changed in response.

## Residual Risk

- Package B's deprecated four-argument `finalizeUpgrade` compatibility overload remains because it is outside Package D ownership. Package D has no remaining caller of that overload.
- Full-suite and disposable-MySQL verification remain outside this focused Package D run.
- The combined worktree cannot be reported green until Package F completes its in-progress contract edits and reruns compilation.
- This WI unblocks `WI-20260715-ATS-007` for the MA's next chain step.
