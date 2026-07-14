# Evidence Pack: WI-20260714-ATS-005

## Summary (one-liner)
- Completed takeover review and closure for initial billing-confirm command persistence, recovery, cleanup evidence, and focused verification.

## Scope / DoD Check
- DoD items:
  - [x] Separate Spring bean applies `REQUIRES_NEW` claim/outcome/finalize methods.
  - [x] Initial issue/charge failure reloads as durable failure in a new transaction.
  - [x] Provider success/local failure reloads as `PROVIDER_SUCCEEDED` and retry finalizes locally only.
  - [x] Cleanup failure is recoverable evidence and does not erase the failed command.
  - [x] Focused tests, compile, and diff check pass.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution / traceability / financial integrity |
| 0 | `docs/standards/development-standards.md` | Java/Spring transaction and test standards |
| 1 | `docs/policies/security-policy.md` | Secret and sensitive payment-data handling |
| 1 | `docs/policies/quality-gates.md` | Verification and evidence expectations |
| 2 | `deliverables/user/REQ-20260714-ATS-001.md` | Approved remediation scope |
| 2 | `docs/design/p1-payment-db-integrity-design.md` | Required payment command lifecycle and transaction design |
| 2 | `deliverables/agent/WI-20260714-ATS-005-handoff.md` | WI-specific scope, DoD, and output contract |

**Injection Rules Applied**:
- Rule source: `AGENTS.md` / WI handoff packet
- Assignee: `se`
- Task type: implementation takeover / review / verification
- agent_required_tiers: `[0, 1, 2]`

## Evidence Pointers (required)
- Files reviewed / confirmed:
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:154` (`confirmBillingAgreement` runs with `Propagation.NOT_SUPPORTED`)
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:159` (claims billing confirm command before provider calls)
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:177` (billing-key issue provider call after claim)
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:219` (issued billing key stored before charge)
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:253` (initial charge uses persisted provider idempotency key)
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:300` (provider success recorded before finalize)
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:497` (billing-key cleanup runs after failed charge outcome)
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:55` (`claimBillingConfirm` uses `REQUIRES_NEW` and `noRollbackFor = BusinessException.class`)
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:76` (`PROVIDER_SUCCEEDED` retry returns finalize-only)
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:80` (15-minute stale `PROCESSING` check)
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:120` (issued billing-key storage is `REQUIRES_NEW`)
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:139` (provider success recording is `REQUIRES_NEW`)
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:161` (provider failure recording is `REQUIRES_NEW`)
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:191` (initial finalize is `REQUIRES_NEW`)
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:279` (cleanup failure incident recording is `REQUIRES_NEW`)
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandKeyFactory.java:12` (canonical billing confirm key)
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandKeyFactory.java:40` (initial billing attempt idempotency key)
  - `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java:72` (persisted command key)
  - `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java:112` (persisted provider attempt)
  - `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java:115` (persisted provider idempotency key)
  - `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java:118` (processing start timestamp)
  - `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java:160` (claim transition)
  - `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java:185` (provider success transition)
  - `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java:205` (unknown provider outcome transition)
  - `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java:216` (stale processing predicate)
  - `src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java:27` (agreement lookup before lock ordering)
  - `src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java:31` (payment order pessimistic lock)
  - `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java:44` (billing agreement pessimistic lock)
  - `src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java:51` (subscription lock by user for finalize)
  - `src/main/java/com/atstudio/atstudio/repository/SubscriptionPaymentRepository.java:44` (idempotent payment lookup by order)
- Tests reviewed / confirmed:
  - `src/test/java/com/atstudio/atstudio/service/BillingAgreementCommandIntegrationTestSupport.java:241` (provider confirm asserts no active transaction)
  - `src/test/java/com/atstudio/atstudio/service/BillingAgreementCommandIntegrationTestSupport.java:252` (provider charge asserts no active transaction)
  - `src/test/java/com/atstudio/atstudio/service/BillingAgreementCommandIntegrationTestSupport.java:264` (provider cleanup cancel asserts no active transaction)
  - `src/test/java/com/atstudio/atstudio/service/BillingAgreementFailurePersistenceIntegrationTest.java:45` (stale processing blocks blind replay)
  - `src/test/java/com/atstudio/atstudio/service/BillingAgreementFailurePersistenceIntegrationTest.java:73` (billing-key issue failure persists)
  - `src/test/java/com/atstudio/atstudio/service/BillingAgreementFailurePersistenceIntegrationTest.java:95` (charge failure commits before cleanup)
  - `src/test/java/com/atstudio/atstudio/service/BillingAgreementFailurePersistenceIntegrationTest.java:125` (cleanup failure preserves key and creates incident)
  - `src/test/java/com/atstudio/atstudio/service/PaymentProviderSuccessRecoveryIntegrationTest.java:45` (provider success survives finalize failure and retry does not charge again)

## Commands & Outputs (if any)
- Commands executed:
  - `.\gradlew.bat test --tests "com.atstudio.atstudio.service.BillingAgreementApplicationServiceTest" --tests "com.atstudio.atstudio.service.BillingAgreementFailurePersistenceIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentProviderSuccessRecoveryIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentCommandKeyFactoryTest"`
  - `.\gradlew.bat compileJava compileTestJava`
  - `git diff --check`
- Outputs:
  - Focused test command: `BUILD SUCCESSFUL in 16s`
  - Compile command: `BUILD SUCCESSFUL in 1s`
  - Diff check: exit code 0; only line-ending conversion warnings were printed.

## Tests (if any)
- `BillingAgreementApplicationServiceTest`: 9 tests, 0 failures, 0 errors.
- `BillingAgreementFailurePersistenceIntegrationTest`: 4 tests, 0 failures, 0 errors.
- `PaymentCommandKeyFactoryTest`: 2 tests, 0 failures, 0 errors.
- `PaymentProviderSuccessRecoveryIntegrationTest`: 1 test, 0 failures, 0 errors.
- Total focused scope: 16 tests, all passing.

## Risks / Rollback
- Risks:
  - Focused verification used the local Gradle/JPA test environment, not live Toss, live DB, SMTP, or server execution.
  - Disposable MySQL and concurrency proof remain in downstream WI scope.
  - Upgrade/renewal/refund execution changes are outside WI-005 and were not modified in this takeover.
- Rollback:
  - Revert only `deliverables/user/WI-20260714-ATS-005-summary.md` and `deliverables/agent/WI-20260714-ATS-005-evidence-pack.md` for this takeover layer.
  - If rolling back inherited WI-005 code, revert the payment-command service/entity/repository/test changes as a separate reviewed operation; do not touch unrelated storage, acceptance, auth, or refund changes.

## Follow-ups (optional)
- Next WI candidates:
  - `WI-20260714-ATS-006` for upgrade command lifecycle.
  - `WI-20260714-ATS-007` for renewal command lifecycle/isolation.
  - `WI-20260714-ATS-018` / `WI-20260714-ATS-021` for disposable MySQL/concurrency proof and retained-DB rehearsal.
