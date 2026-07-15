# Evidence Pack: WI-20260715-ATS-002

## Summary (one-liner)

- Completed Package B on Package A commit `103fdf4`: F-01 renewal identity, exact candidates, canonical payment-command locks, persisted upgrade target, cleanup/stale projections, and reconciliation-safe success/finalizer entry points.

## Scope / DoD Check

- [x] `nextBillingAt` remains the immutable unresolved renewal period and `renewalRetryAt` is used only as the exact failed-order retry gate.
- [x] A deterministic retry on a later date reuses one order, command key, and billing period while advancing provider attempt identity.
- [x] Fresh/stale `PROCESSING` and `PENDING_PROVIDER_CONFIRMATION` are excluded from automatic charging.
- [x] Exact purpose, renewal period, command key, amount, relationships, and upgrade target cycle are validated before claim/finalization.
- [x] B-owned multi-row phases follow `BillingAgreement -> UserSubscription -> PaymentOrder -> SubscriptionPayment`.
- [x] Existing payment rows and provider transaction ownership use pessimistic write locks before finalization.
- [x] Package C cleanup/unresolved and stale-lease projections are exact and bounded.
- [x] Package F command lock, reconciliation candidate, reconciled-success, and persisted-target finalizer contracts are exposed.
- [x] Focused tests, impacted regressions, Java compilation, and diff whitespace checks pass.
- [x] No retained/local/production database, live Toss endpoint, or preview/public server was accessed or changed.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Platform integrity, approval, financial traceability, and transparency |
| 0 | `docs/standards/development-standards.md` | Java/JPA, transaction, testing, and evidence standards |
| 0 | `docs/standards/documentation-standards.md` | Completion artifact and pointer conventions |
| 0 | `docs/standards/glossary.md` | Canonical ATStudio and WI terminology |
| 1 | `docs/policies/security-policy.md` | Billing key, provider evidence, secret, and DB boundaries |
| 1 | `docs/policies/quality-gates.md` | High-criticality validation and rollback requirements |
| Context | `deliverables/user/REQ-20260714-ATS-001.md` | Approved P1 remediation and forbidden live/retained operations |
| Context | `docs/design/p1-payment-integrity-remediation-design.md` | F-01, lock order, retry, cleanup, and reconciliation contracts |
| Evidence | `deliverables/agent/WI-20260714-ATS-036-evidence-pack.md` | Approved remediation design and Package A-G ownership |
| Evidence | `deliverables/agent/WI-20260714-ATS-023-evidence-pack.md` | Source findings F-01 through F-05 |
| Evidence | `deliverables/agent/WI-20260715-ATS-001-evidence-pack.md` | Package A entity/schema foundation and downstream obligations |

**Injection Rules Applied**:

- Handoff: `deliverables/agent/WI-20260715-ATS-002-handoff.md`
- Assignee: `se`
- Task type: payment-integrity implementation
- Ownership: Package B production files, focused tests, and WI-002 completion artifacts only

## Evidence Pointers

Production:

- `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java:23-65` - exact fresh/retry/finalize-only due projection with keyset paging and a three-day retry window.
- `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java:75-104` - exact unresolved withdrawal cleanup and bounded stale cleanup lease projections.
- `src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java:25-80` - non-locking ancestor ID projection, order write lock, exact renewal-period lock, and reconciliation candidates.
- `src/main/java/com/atstudio/atstudio/repository/SubscriptionPaymentRepository.java:46-59` - locked existing-order payment and provider transaction owner lookups.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:234-333` - immutable-period renewal claim and exact retry gating.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:377-481` - reconciliation-safe provider success and canonical renewal failure locks.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:484-691` - canonical initial/upgrade/renewal finalizers using persisted target cycle and locked payment evidence.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:735-955` - exact renewal/upgrade order creation and identity validation.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:1078-1208` - ancestor projection revalidation, payment/transaction ownership locks, and purpose-specific reconciliation validation.
- `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:58-79` - no-suspension `Propagation.NEVER` orchestration and bounded exact candidate scan.
- `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:129-209` - finalize-only, provider-success, deterministic-failure, and ambiguous-result routing.

Focused tests:

- `src/test/java/com/atstudio/atstudio/service/RecurringRenewalCommandIntegrationTest.java:161-231` - two-date same-order retry and next-day ambiguous exclusion.
- `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java:59-208` - bounded keyset calls, provider attempt key forwarding, no blind replay, and `Propagation.NEVER` contract.
- `src/test/java/com/atstudio/atstudio/service/PaymentCommandIndependentVerificationIntegrationTest.java:110-204` - persisted upgrade target and exact `PAYMENT_ORDER_INVALID_STATE` race loser.
- `src/test/java/com/atstudio/atstudio/service/PaymentProviderSuccessRecoveryIntegrationTest.java:102-127` - reconciled provider success enters finalize-only flow without another charge.
- `src/test/java/com/atstudio/atstudio/repository/BillingAgreementRepositoryTest.java:61-225` - exact due states, cleanup/stale projections, and pessimistic lock annotations.

No production change was required in `UserSubscriptionRepository`; its existing `findByIdForUpdate` and `findByUserIDForUpdate` contracts already provide the canonical subscription locks and are covered by the focused static lock test.

## Retry Identity Example

| Scheduler date | Persisted period/order identity | Provider attempt identity | Result |
|---|---|---|---|
| `2026-08-17` | `billingPeriodStart=2026-08-17`; generated order ID retained; `RENEWAL:{agreementID}:{subscriptionID}:2026-08-17` | `providerAttempt=1`; `renewal-{orderID}-attempt-1` | Deterministic `FAILED`; `nextBillingAt=2026-08-17`; `renewalRetryAt=2026-08-18` |
| `2026-08-18` | Same database order ID, provider order ID, command key, and `billingPeriodStart` | `providerAttempt=2`; `renewal-{sameOrderID}-attempt-2` | Success/finalization; one payment row; next period advances to `2026-09-17` |

The test rejects a second order/command and asserts the two attempt keys differ. A separate ambiguous result remains one `PENDING_PROVIDER_CONFIRMATION` order and produces no day-two provider call.

## Commands & Outputs

- `git status --short --branch` and `git log -1 --oneline`
  - Baseline: branch `codex/p1-acceptance-hardening`, HEAD `103fdf4 fix: 결제 무결성 스키마 기반 보강`.
- `./gradlew.bat test --tests "com.atstudio.atstudio.service.RecurringRenewalCommandIntegrationTest" --tests "com.atstudio.atstudio.service.RecurringRenewalServiceTest" --tests "com.atstudio.atstudio.service.PaymentCommandIndependentVerificationIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentProviderSuccessRecoveryIntegrationTest" --tests "com.atstudio.atstudio.repository.BillingAgreementRepositoryTest"`
  - Final result: PASS, 24 tests, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESSFUL in 24s`.
  - Initial red/repair run: 21/22 passed; the sole failure was an overlong test nickname, not production behavior. The fixture was shortened and the complete final run passed.
- `./gradlew.bat test --tests "com.atstudio.atstudio.service.SubscriptionUpgradeCommandIntegrationTest" --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest" --tests "com.atstudio.atstudio.service.BillingAgreementApplicationServiceTest" --tests "com.atstudio.atstudio.service.BillingAgreementFailurePersistenceIntegrationTest" --tests "com.atstudio.atstudio.entity.PaymentDatabaseIntegrityContractTest" --tests "com.atstudio.atstudio.repository.SubscriptionPaymentRepositoryLockContractTest"`
  - Result: PASS, all six impacted regression classes, `BUILD SUCCESSFUL in 22s`.
- `./gradlew.bat compileJava`
  - Result: PASS, `BUILD SUCCESSFUL in 1s`.
- `git diff --check`
  - Result: PASS with no whitespace diagnostics; only repository LF-to-CRLF working-copy notices were emitted.
- `git diff --no-index --check -- NUL <WI-002 completion artifact>` for the new summary and Evidence Pack
  - Result: PASS with no whitespace diagnostics; exit code `1` is the expected no-index content difference.

## Tests

- Focused Package B tests: PASS, 24/24.
- Covered behaviors: two-date retry identity, exact due states, pending/processing exclusion, canonical race loser, persisted upgrade target, finalization recovery, cleanup/stale projections, lock annotations, and provider no-transaction boundary.
- Impacted regression classes: PASS.
- Not run by scope/constraint: full backend suite, disposable MySQL/InnoDB concurrency proof, retained/copied DB rehearsal, live Toss, or preview/public server smoke tests.

## Risks / Rollback

Risks:

- H2 proves state transitions and orchestration but not InnoDB lock ordering, deadlock behavior, isolation, or exact production-engine race convergence. Package G remains required for F-05 closure.
- The Package A entity exposes no clear-only transition for a consumed retry gate. During a retry that is `PROCESSING` or becomes `PENDING_PROVIDER_CONFIRMATION`, the consumed `renewalRetryAt` value may remain stored. Exact status predicates prevent it from authorizing another charge; strict null-state parity requires a Package A-owned entity follow-up.
- The four-argument `finalizeUpgrade` overload remains temporarily for Package D source compatibility. It ignores the caller cycle and delegates to the persisted-order finalizer; Package D should migrate callers to the three-argument API and then remove the overload.
- The full Gradle suite was not run; verification was intentionally focused plus the named impacted regression classes.

Rollback:

- Pause renewal and payment mutation entry points before rolling back application behavior.
- Revert only the Package B production/test files and the two WI-002 completion artifacts listed here; do not revert Package E files or unrelated logs/deliverables.
- Preserve Package A additive columns, indexes, ENUM values, command keys, payment rows, and audit/Incident evidence. Do not contract payment schema during an incident.
- No DB patch or provider mutation was executed by this WI, so there is no database/provider rollback for this work session.

## Follow-ups

1. Package C can consume the bounded unresolved/stale cleanup projections without editing B repositories.
2. Package D should move to the three-argument persisted-target upgrade finalizer and remove the temporary compatibility overload.
3. Package F can consume the command lock projection, reconciliation candidates, reconciled-success transition, and purpose-specific finalizers.
4. Package G must provide disposable MySQL 8/InnoDB race proof with exact business losers and no accepted deadlock/timeout/arbitrary exception.
