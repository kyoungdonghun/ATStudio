# Evidence Pack: WI-20260715-ATS-004

## Summary (one-liner)

- Completed Package C with durable cancellation/withdrawal cleanup claim and fenced result phases, strict no-transaction provider deletion, bounded stale detection, and Incident/audit recovery evidence.

## Scope / DoD Check

- [x] Cancellation and withdrawal persist a cleanup claim before provider deletion and a fenced result afterward.
- [x] Provider success and `ALREADY_REMOVED_BILLING_KEY` clear local key material once.
- [x] Deterministic failure retains ciphertext with `FAILED`; unknown provider outcome retains ciphertext with `PENDING_PROVIDER_CONFIRMATION`.
- [x] Fresh cleanup competition returns the exact `IN_PROGRESS` loser without a second provider call.
- [x] Stale `PROCESSING` cleanup is bounded, detect-only, Incident-backed, audited, and never automatically replayed.
- [x] A delayed result from the old stale lease is fenced and cannot clear key material.
- [x] User cancellation remains durable even when provider cleanup fails.
- [x] Package B repository projections are consumed without repository or `PaymentCommandTransactionService` edits.
- [x] Focused and impacted regression tests, production/test compilation, and whitespace diff checks pass.
- [x] No retained/local/production DB, live Toss, or preview/public server was accessed or changed.

## Reference Documents (Tier 0-2)

**Injected Context** (from the WI handoff packet):

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Platform integrity, approval, financial traceability, and transparency |
| 0 | `docs/standards/development-standards.md` | Java/Spring transaction, testing, and evidence standards |
| 0 | `docs/standards/documentation-standards.md` | Two-set deliverable and pointer conventions |
| 0 | `docs/standards/glossary.md` | Canonical WI and ATStudio terminology |
| 1 | `docs/policies/security-policy.md` | Billing-key, provider evidence, secret, DB, and logging boundaries |
| 1 | `docs/policies/quality-gates.md` | High-criticality validation and rollback expectations |
| Context | `deliverables/user/REQ-20260714-ATS-001.md` | Approved P1 remediation scope and forbidden live/retained operations |
| Context | `docs/design/p1-payment-integrity-remediation-design.md` | Package C claim/provider/result, lease, stale, and lock-order contract |
| Evidence | `deliverables/agent/WI-20260714-ATS-036-evidence-pack.md` | Approved remediation design and disjoint Package A-G ownership |
| Evidence | `deliverables/agent/WI-20260715-ATS-001-evidence-pack.md` | Package A cleanup entity/lease foundation |
| Evidence | `deliverables/agent/WI-20260715-ATS-002-evidence-pack.md` | Package B bounded cleanup/stale projections and lock contracts |

**Injection Rules Applied**:

- Handoff: `deliverables/agent/WI-20260715-ATS-004-handoff.md`
- Assignee: `se`
- Task type: payment-integrity implementation and focused verification
- Ownership: Package C services/tests and WI004 completion artifacts only

## Evidence Pointers

Production:

- `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:327-354` - no-transaction cancellation orchestrator and claim/provider/result ordering.
- `src/main/java/com/atstudio/atstudio/service/BillingAgreementCleanupProviderExecutor.java:39-84` - `Propagation.NEVER` provider deletion, safe result classification, and already-removed convergence.
- `src/main/java/com/atstudio/atstudio/service/BillingAgreementCleanupTransactionService.java:60-100` - canonical agreement-then-subscription cancellation claim and durable local cancellation.
- `src/main/java/com/atstudio/atstudio/service/BillingAgreementCleanupTransactionService.java:102-141` - withdrawal claim and fenced cancellation/withdrawal result phases.
- `src/main/java/com/atstudio/atstudio/service/BillingAgreementCleanupTransactionService.java:144-178` - stale conversion and exact fresh/stale claim classification.
- `src/main/java/com/atstudio/atstudio/service/BillingAgreementCleanupTransactionService.java:181-245` - lease-fenced result transitions plus Incident and operation-audit updates.
- `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupService.java:31-91` - bounded B-owned projections and no-transaction withdrawal orchestration.
- `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupCoordinator.java:32-68` - scheduled stale detection before unresolved cleanup processing.

Focused tests:

- `src/test/java/com/atstudio/atstudio/service/BillingAgreementCancellationTransactionIntegrationTest.java:44-139` - claim visibility at provider time, deterministic/unknown persistence, no replay, stale fencing, Incident, and audit assertions.
- `src/test/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupTransactionIntegrationTest.java:37-118` - already-removed convergence, fresh loser, bounded stale detect-only behavior, and unknown outcome persistence.
- `src/test/java/com/atstudio/atstudio/service/BillingAgreementCleanupProviderExecutorTest.java:43-103` - strict `NEVER`, decryption failure classification, already-removed handling, and safe exception evidence.
- `src/test/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupServiceTest.java:46-145` - phase ordering, exact outcomes, and bounded projection consumption.
- `src/test/java/com/atstudio/atstudio/service/BillingAgreementCleanupIntegrationTestSupport.java:45-226` - fake provider transaction observation and Package C H2 fixtures.
- `src/test/java/com/atstudio/atstudio/service/BillingAgreementCommandIntegrationTestSupport.java:51-55` - C collaborator mocks required by existing billing-command data slices.

## Claim / Provider / Result Evidence

| Boundary | Durable state / exact outcome | Proof |
|---|---|---|
| Cancellation claim | Agreement and subscription are `CANCELLED`; cleanup is `PROCESSING`; ciphertext remains | Provider probe reloads committed rows before deletion |
| Withdrawal claim | Deleted user, cancelled agreement, retained ciphertext, `NONE/REQUIRED -> PROCESSING` | Provider probe reloads committed agreement |
| Provider call | No actual Spring transaction is active; plaintext exists only for the command call | Fake provider fails on active transaction and records observed state |
| Success/already removed | Active lease required; key material cleared; cleanup becomes `NONE`; Incident resolved and audited when present | Success and already-removed integration tests |
| Deterministic failure | Active lease required; cleanup becomes `FAILED`; key retained; Incident/audit open | Cancellation deterministic failure test |
| Unknown outcome | Active lease required; cleanup becomes `PENDING_PROVIDER_CONFIRMATION`; key retained; exception class only | Cancellation/withdrawal unknown-outcome tests |
| Fresh competitor | Exact `IN_PROGRESS`; no provider call | Withdrawal fresh competition test |
| Stale claimant | `PROCESSING -> PENDING_PROVIDER_CONFIRMATION`; no provider replay; Incident/audit open | Cancellation and bounded withdrawal stale tests |
| Delayed old result | Rejected with `BILLING_AGREEMENT_INVALID_STATE`; key remains retained | Cancellation stale-result fencing test |

## Commands & Outputs

- Initial RED: `./gradlew.bat test --tests "com.atstudio.atstudio.service.WithdrawalBillingCleanupServiceTest"`
  - Result: expected failure because the old `cleanup()` still declared `REQUIRES_NEW` instead of `NEVER`.
- Final focused plus impacted regression command: ten explicit test classes covering Package C, billing-command persistence/recovery, and payment controller behavior.
  - Result: PASS, 43 tests, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESSFUL in 28s`.
  - One prior attempt was retried after a concurrent Gradle worker removed an in-progress shared test-result binary; no code failure was reported in that attempt.
- `./gradlew.bat compileJava compileTestJava`
  - Result: PASS, `BUILD SUCCESSFUL in 5s`.
- `git diff --check`
  - Result: PASS with no whitespace diagnostics; only repository LF-to-CRLF working-copy notices were emitted.
- `git diff --no-index --check -- NUL <new Package C file>` for six new source/test files.
  - Result: PASS with no whitespace diagnostics; exit code `1` is the expected content-difference result.

## Tests

- Package C focused suites: PASS, 28/28.
- Impacted billing-command and controller regressions: PASS, 15/15.
- Total final command: PASS, 43/43.
- Covered: no active provider transaction, claim-before-provider durability, result fencing, local cancellation durability, deterministic and ambiguous outcomes, already-removed idempotency, fresh competition, stale detect-only behavior, Incident/audit transitions, bounded repository projection consumption, and delayed-result rejection.
- Not run by scope: full backend suite, disposable MySQL/InnoDB concurrency proof, retained/copied DB rehearsal, live Toss, or preview/public server smoke tests.

## Risks / Rollback

Risks:

- H2 does not prove InnoDB lock ordering, isolation, or production race convergence. Package G remains required for F-05 closure.
- Billing-key deletion has no payment-command idempotency key. `PENDING_PROVIDER_CONFIRMATION` and stale cleanup therefore require explicit operator disposition before moving to `REQUIRED`.
- The scheduled worker processes at most 100 unresolved and 100 stale cleanup IDs per run by design; sustained backlog requires operational monitoring.

Rollback:

- Pause cancellation cleanup and the withdrawal cleanup scheduler before rolling back application behavior.
- Revert only the Package C services/tests and WI004 completion artifacts listed above; preserve Package A fields, Package B repository contracts, Incident rows, and operation-audit evidence.
- Do not clear retained ciphertext for `FAILED` or `PENDING_PROVIDER_CONFIRMATION` rows without verified provider disposition.
- No DB/provider/server mutation was performed by this WI, so this work session has no external state rollback.

## Follow-ups

1. MA should evaluate the `Blocks: WI-20260715-ATS-007` chain after Package D/F completion state is collected.
2. Package G must prove exact concurrency losers and lock ordering on an approved disposable MySQL 8/InnoDB environment.
