# Evidence Pack: WI-20260715-ATS-005

## Summary (one-liner)

- Completed Package D by separating charged-upgrade planning, provider execution, durable result persistence, and persisted-target finalization across explicit transaction boundaries.

## Scope / DoD Check

- [x] Upgrade planning and local-only mutation run in one short local transaction.
- [x] Charged upgrade leaves planning with IDs only and uses Package B claim/result/finalize phases.
- [x] Provider charge runs under `Propagation.NEVER`; an active caller transaction is rejected rather than suspended.
- [x] Provider success commits as `PROVIDER_SUCCEEDED` before local entitlement finalization.
- [x] Finalize-only retry does not charge again and uses the three-argument persisted-target finalizer.
- [x] Deterministic failure, ambiguous failure, and removed billing-key user behavior remain covered.
- [x] Focused tests, Java compilation, and owned-file whitespace checks pass.
- [x] No B-owned production file, database, live Toss endpoint, or preview runtime was changed or accessed.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Financial traceability, approval, and platform-integrity baseline |
| 0 | `docs/standards/development-standards.md` | Java transaction and focused-test standards |
| 0 | `docs/standards/documentation-standards.md` | Completion artifact and evidence-pointer conventions |
| 0 | `docs/standards/glossary.md` | Canonical WI and subscription terminology |
| 1 | `docs/policies/security-policy.md` | Billing-key, secret, provider evidence, and database boundaries |
| 1 | `docs/policies/quality-gates.md` | High-criticality validation and rollback expectations |
| Context | `deliverables/user/REQ-20260714-ATS-001.md` | Approved P1 scope and forbidden live/retained operations |
| Context | `docs/design/p1-payment-integrity-remediation-design.md` | F-02 charged-upgrade phases and persisted-target finalization contract |
| Evidence | `deliverables/agent/WI-20260714-ATS-036-evidence-pack.md` | Package A-G ownership and closure contract |
| Evidence | `deliverables/agent/WI-20260715-ATS-002-evidence-pack.md` | Package B claim/result/finalizer APIs and compatibility-overload risk |

**Injection Rules Applied**:

- Handoff: `deliverables/agent/WI-20260715-ATS-005-handoff.md`
- Assignee: `se`
- Task type: payment-integrity implementation
- Ownership: `UserSubscriptionService`, `SubscriptionUpgradePaymentExecutor`, focused upgrade tests, and WI-005 completion artifacts only

## Evidence Pointers

Production:

- `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:123-140` - `NEVER` orchestration and short `TransactionTemplate` planning phase.
- `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:142-209` - local-only mutation or ID-only charged plan selection.
- `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:358-433` - Package B claim/result phases and three-argument persisted-target finalizer calls.
- `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:521-544` - immutable ID-only/local-response planning result.
- `src/main/java/com/atstudio/atstudio/service/SubscriptionUpgradePaymentExecutor.java:26-36` - provider charge guarded by `Propagation.NEVER`.

Focused tests:

- `src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java:290` - finalize-only skips provider execution and calls only the persisted-target finalizer.
- `src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java:94` - active transaction is rejected before provider execution.
- `src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java:118` - provider success survives local finalization failure and retry performs no second charge.
- `src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java:162` - deterministic retry retains the order and advances the provider attempt.
- `src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java:198` - ambiguous provider failure persists pending evidence and blocks blind retry.
- `src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java:226` - concurrent duplicate does not create another order, ledger, or provider charge.

## Transaction and Recovery Trace

| Phase | Boundary | Durable outcome |
|---|---|---|
| Local planning | `TransactionTemplate` short transaction | Local-only response or user/current/target IDs |
| Upgrade claim | Package B `REQUIRES_NEW` | Exact order claimed as `PROCESSING`, persisted target cycle retained |
| Provider charge | Package D `NEVER` | No active or suspended local transaction |
| Provider result | Package B `REQUIRES_NEW` | `PROVIDER_SUCCEEDED`, `FAILED`, or `PENDING_PROVIDER_CONFIRMATION` committed |
| Upgrade finalization | Package B `REQUIRES_NEW` | Locked order supplies `upgradeTargetBillingCycle`; payment and entitlement finalize idempotently |

Provider-success/local-failure proof:

1. The transaction-observing fake provider accepts one charge with no active transaction.
2. Injected receipt-evidence failure leaves the order `PROVIDER_SUCCEEDED`, no payment row, and the old entitlement.
3. The repeated request receives Package B's `FINALIZE_ONLY` claim.
4. Package D calls the three-argument finalizer and creates one payment/entitlement result without a second charge.

## Commands & Outputs

- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest" --tests "com.atstudio.atstudio.service.SubscriptionUpgradeCommandIntegrationTest"`
  - Final result: **PASS**, 34 tests, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESSFUL in 18s`.
  - Breakdown: 29 unit tests and 5 integration tests.
- `.\gradlew.bat compileJava`
  - Result: **PASS**, `BUILD SUCCESSFUL in 1s`.
- Latest-worktree focused rerun after concurrent Package C/F edits
  - Result: **BLOCKED before Package D tests executed** by Package F-owned compile errors in `PaymentReconciliationTransactionService`: missing `ProviderPaymentLookupResult.currency()` and a record accessor/factory conflict for `EvidenceAssessment.exactDone()`.
  - Package D files were not changed to bypass or repair the cross-package issue.
- `git diff --check -- <four Package D production/test files>`
  - Result: **PASS** with no whitespace diagnostics; only repository LF-to-CRLF working-copy notices were emitted.
- `git status --short --branch` and `git diff --name-only`
  - Result: Package D owns four modified code/test files. Concurrent Package C production/test additions and modifications, plus unrelated server logs/handoffs, were observed and left unchanged.

## Tests

- Transaction boundary: provider fake observes no active transaction; direct executor invocation inside an active transaction throws `IllegalTransactionStateException` before provider execution.
- Persisted target: the successful order retains `upgradeTargetBillingCycle=MONTHLY`; Package D never supplies a cycle to finalization.
- Recovery: injected local finalization failure remains finalize-only recoverable with one provider call.
- Deterministic failure: same order retries with provider attempt `1 -> 2`.
- Ambiguous failure: state remains `PENDING_PROVIDER_CONFIRMATION`; repeated request makes no provider call.
- Removed billing key and zero-amount local upgrade remain covered by `UserSubscriptionServiceTest`.

## Risks / Rollback

Risks:

- The deprecated four-argument `PaymentCommandTransactionService.finalizeUpgrade` overload remains in the B-owned file for external source compatibility. Package D no longer calls it.
- This focused run does not replace the full backend suite or Package G disposable MySQL/InnoDB proof.
- Retained/local/production database applicability and live-provider behavior were intentionally not exercised.
- The latest combined worktree compile remains blocked by concurrent Package F contract work, even though the isolated Package D focused run and explicit compile passed before those files appeared.

Rollback:

- Revert only the two Package D production files, two focused test files, and these WI-005 completion artifacts.
- Do not revert Package C/F work, Package B command/repository changes, additive payment schema, payment evidence, or unrelated runtime files.
- No database or provider rollback is required because this WI performed no schema, retained-data, or live-provider mutation.

## Follow-ups

1. MA should trigger blocked successor `WI-20260715-ATS-007` under the WI chain rule.
2. Package B owner may remove the deprecated four-argument compatibility overload only after all non-Package-D callers are confirmed absent.
