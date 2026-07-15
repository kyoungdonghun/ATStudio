# Evidence Pack: WI-20260715-ATS-008

## Summary (one-liner)

- Corrected renewal `DONE` idempotency and reconciliation convergence while preserving immutable ownership, payment-ledger, provider-transaction, and current-period validation boundaries.

## Scope / DoD Check

- [x] A second `finalizeRenewal` after the first advances `agreement.nextBillingAt` returns as an idempotent no-op.
- [x] The `DONE` no-op requires exactly one matching locked `SubscriptionPayment` in `DONE` state.
- [x] Missing or mismatched payment evidence fails closed with `PAYMENT_ORDER_INVALID_STATE`.
- [x] Reconciliation can continue through `finalizeByPurpose` and resolve its Incident when the normal renewal finalizer wins after provider lookup.
- [x] `PROVIDER_SUCCEEDED` renewal finalization still validates the current unresolved billing period before applying state.
- [x] The analogous upgrade path was reviewed and was not changed because it has no mutable-period validation dependency.
- [x] No external call, new lock, schema change, or unbounded wait was added.
- [x] Focused Package B/F and impacted regression tests pass.
- [x] Owned-file whitespace validation passes.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Financial integrity, approval, traceability, and transparency |
| 0 | `docs/standards/development-standards.md` | Java/JPA transaction, locking, and test standards |
| 1 | `docs/policies/quality-gates.md` | High-criticality verification and rollback evidence |
| 1 | `docs/policies/security-policy.md` | Provider evidence, secret, database, and logging boundaries |
| 2 | `docs/design/p1-payment-integrity-remediation-design.md` | F-01/F-04 finalization, reconciliation, and MySQL proof contracts |
| Context | `deliverables/user/REQ-20260714-ATS-001.md` | Approved P1 remediation and forbidden live/retained operations |
| Handoff | `deliverables/agent/WI-20260715-ATS-008-handoff.md` | Scope, ownership, acceptance criteria, and WI-007 dependency |
| Evidence | `deliverables/agent/WI-20260715-ATS-002-evidence-pack.md` | Package B command identity and lock-order baseline |
| Evidence | `deliverables/agent/WI-20260715-ATS-006-evidence-pack.md` | Package F reconciliation and Incident baseline |
| Evidence | `deliverables/agent/WI-20260715-ATS-007/mysql-races.log` | Read-only race 4 and race 7 failure evidence |

**Injection Rules Applied**:

- Assignee: `se`
- Task type: payment-integrity defect correction
- Ownership: one payment transaction service, two focused integration tests, and WI-008 completion artifacts only
- Read-only boundaries: every WI-007 file, MySQL proof tests/support, runtime logs, and the preview worktree

## Evidence Pointers

Production:

- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:378-405` - `recordProviderSuccessFromReconciliation` now detects a concurrently completed `DONE` order, validates completed evidence, and returns the normal purpose target without rewriting provider success.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:646-689` - renewal finalization checks status first; `DONE` uses immutable evidence and exits, while `PROVIDER_SUCCEEDED` retains mutable current-period validation before state application.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:860-881` - completed renewal identity derives its command key from persisted `billingPeriodStart`, not advanced `agreement.nextBillingAt`.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:1150-1212` - locked order/payment and provider-transaction ownership validation; payment plan and cycle must also match the order.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:1214-1244` - completed reconciliation validates `DONE`, positive amount, `KRW`, command key, exact provider transaction ID, purpose-specific relationships, and committed payment evidence.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:1278-1287` - one shared immutable reconciliation finalization target builder.

Focused regressions:

- `src/test/java/com/atstudio/atstudio/service/PaymentCommandIndependentVerificationIntegrationTest.java:215-242` - repeated completion after period advance is a no-op with one payment and unchanged next period.
- `src/test/java/com/atstudio/atstudio/service/PaymentCommandIndependentVerificationIntegrationTest.java:245-266` - missing completed payment evidence fails closed.
- `src/test/java/com/atstudio/atstudio/service/PaymentCommandIndependentVerificationIntegrationTest.java:269-292` - mismatched completed payment amount fails closed.
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationRecoveryIntegrationTest.java:255-290` - provider lookup callback lets the normal finalizer win; reconciliation then finalizes idempotently, resolves the Incident, and records zero provider charges.
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationRecoveryIntegrationTest.java:497-556` - single-use provider-lookup callback used to reproduce the claim/lookup/apply race deterministically without a provider mutation.

Upgrade review:

- `PaymentCommandTransactionService.validateUpgradeFinalizationOrder` validates persisted owner, subscription, agreement, amount, currency, and command key. It does not compare against `agreement.nextBillingAt` or another mutable period value, so no analogous ordering defect was proven and no upgrade code was changed.

## Invariant Proof

| Invariant | Enforcement |
|---|---|
| One entitlement transition | `DONE` returns before `startNewSubscription`; only `PROVIDER_SUCCEEDED` reaches the state transition |
| One payment row | Existing order payment and provider transaction owner are locked; `DONE` requires that matching row rather than inserting |
| Exact provider transaction owner | Provider plus `pgTransactionId` owner must be the same payment row and reconciliation evidence must equal the completed order transaction ID |
| Current period checked before new mutation | Only `PROVIDER_SUCCEEDED` calls `validateRenewalOrder(... agreement.getNextBillingAt())` |
| Completed retry independent of advanced period | Completed identity uses `order.billingPeriodStart` for its deterministic renewal command key |
| Incident convergence | Completed reconciliation returns the same finalization target; idempotent finalization succeeds and existing Incident resolution runs |
| Lock order preserved | Agreement, projected subscription, order, then payment/transaction rows; no lock or provider call was added |

## Commands & Outputs

1. Initial focused run:
   - `gradlew.bat test --tests "com.atstudio.atstudio.service.PaymentCommandIndependentVerificationIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationRecoveryIntegrationTest" --console=plain`
   - Result: 10 existing tests passed and 2 new tests failed before exercising production behavior because test nicknames exceeded the persisted 20-character limit.
   - Repair: shortened only the two fixture labels; no production change was made for this transient failure.
2. Focused rerun after fixture repair:
   - Same command.
   - Result: PASS, 12 tests, `BUILD SUCCESSFUL in 19s`.
3. Final Package B/F and impacted regression command:
   - `gradlew.bat test` with 18 explicit `--tests` selectors covering Package B command/repository tests, Package F reconciliation/provider tests, and subscription/upgrade/Incident/audit regressions.
   - Result: PASS, 122 tests across 27 emitted XML suites, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESSFUL in 32s`.
   - `compileJava` and `compileTestJava` completed successfully within the run.
4. Whitespace validation:
   - `git diff --check -- <five owned changed paths>`
   - Result: PASS; only Git's existing LF-to-CRLF working-copy notices were emitted.

## Tests

- Direct renewal idempotency: PASS.
- Missing and mismatched payment evidence: PASS, exact business failure asserted.
- Reconciliation versus normal renewal finalizer: PASS, one payment, one period, resolved Incident, zero charge calls.
- Existing Package B/F focused and impacted tests: PASS, 122/122.
- Not run by WI constraint: WI-007 disposable MySQL runner, retained/local/preview databases, live Toss, and preview/public smoke tests.

## Risks / Rollback

Risks:

- H2 proves state and orchestration behavior but not InnoDB scheduling or deadlock behavior. WI-007 must rerun races 4 and 7 against a newly generated disposable MySQL database.
- A completed order with absent or contradictory ledger evidence intentionally remains unresolved and requires operator investigation; the fix does not synthesize missing financial evidence.

Rollback:

- Revert only:
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java`
  - `src/test/java/com/atstudio/atstudio/service/PaymentCommandIndependentVerificationIntegrationTest.java`
  - `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationRecoveryIntegrationTest.java`
  - `deliverables/user/WI-20260715-ATS-008-summary.md`
  - `deliverables/agent/WI-20260715-ATS-008-evidence-pack.md`
- Preserve all order, payment, Incident, audit, schema, and WI-007 evidence. No database or provider rollback is required because this WI executed only ephemeral H2 tests.

## Follow-ups

1. WI-20260715-ATS-007 must rerun its unchanged disposable MySQL proof and update its own evidence only after all seven races pass and cleanup confirms the generated database is absent.
2. Independent payment/integration review can begin after WI-007 closes.
