# Evidence Pack: WI-20260714-ATS-006

## Summary (one-liner)
- Implemented charged subscription-upgrade payment command lifecycle with deterministic command reuse, persisted provider attempts, no-transaction Provider charge execution, and idempotent local finalization.

## Scope / DoD Check
- DoD items:
  - [x] Agreement/subscription/order locks are acquired through `PaymentCommandTransactionService` and state is revalidated before claim/finalize.
  - [x] Stable upgrade command key reuses one order; deterministic failed explicit retry increments the persisted provider attempt.
  - [x] `PROVIDER_SUCCEEDED` retry finalizes locally without another Provider call.
  - [x] Duplicate request while the command is `PROCESSING` does not create another Provider charge.
  - [x] Approved proration and plan-change policy are preserved.
  - [x] Focused service tests, integration tests, compile, and diff check pass.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution / financial traceability |
| 0 | `docs/standards/development-standards.md` | Java/Spring transaction and test standards |
| 1 | `docs/policies/quality-gates.md` | Verification and evidence expectations |
| 2 | `deliverables/user/REQ-20260714-ATS-001.md` | Approved remediation scope |
| 2 | `docs/design/p1-payment-db-integrity-design.md` | Payment command lifecycle, lock order, retry/finalize contract |
| 2 | `deliverables/agent/WI-20260714-ATS-004-evidence-pack.md` | Entity/schema command fields and constraints prerequisite |
| 2 | `deliverables/agent/WI-20260714-ATS-005-evidence-pack.md` | Shared command helper/key factory prerequisite |
| 2 | `deliverables/agent/WI-20260714-ATS-006-handoff.md` | WI-specific scope, DoD, and output contract |

**Injection Rules Applied**:
- Rule source: `AGENTS.md` / WI handoff packet
- Assignee: `se`
- Task type: backend implementation / payment integrity
- agent_required_tiers: `[0, 1, 2]`

## Evidence Pointers (required)
- Files changed:
  - `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:156` - charged upgrade now delegates to command lifecycle instead of direct order/payment mutation.
  - `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:337` - charged upgrade orchestrator claims, calls Provider through executor, records outcome, then finalizes.
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:69` - `claimUpgrade` locks/revalidates current state, reuses/creates the canonical command, and persists attempt key.
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:371` - `finalizeUpgrade` idempotently creates one payment, applies one plan transition, and marks order `DONE`.
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:472` - command-key lookup converges retries/races to an existing order.
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:792` - `UpgradeClaim` carries committed Provider-call inputs and finalize-only action.
  - `src/main/java/com/atstudio/atstudio/service/SubscriptionUpgradePaymentExecutor.java:27` - Provider charge runs with `Propagation.NOT_SUPPORTED`.
  - `src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java:35` - pessimistic command-key lookup added.
  - `src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java:919` - WI-006 helper completed for charged-upgrade unit tests.
  - `src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java:90` - Provider success/local rollback retry evidence.
  - `src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java:133` - failed explicit retry attempt increment evidence.
  - `src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java:169` - duplicate processing request Provider call-count evidence.

## Commands & Outputs
- Commands executed:
  - `.\gradlew.bat compileTestJava`
  - `.\gradlew.bat test --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest" --tests "com.atstudio.atstudio.service.SubscriptionUpgradeCommandIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentCommandKeyFactoryTest"`
  - `git diff --check -- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java src/main/java/com/atstudio/atstudio/service/SubscriptionUpgradePaymentExecutor.java src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java`
  - `.\gradlew.bat compileJava compileTestJava`
- Outputs:
  - `compileTestJava`: `BUILD SUCCESSFUL`
  - Focused tests: `BUILD SUCCESSFUL`
  - `compileJava compileTestJava`: `BUILD SUCCESSFUL`
  - `git diff --check`: exit code 0; only LF-to-CRLF working-copy warnings.

## Tests
- `UserSubscriptionServiceTest`: PASS in focused Gradle run.
- `SubscriptionUpgradeCommandIntegrationTest`: PASS in focused Gradle run.
  - Provider call count: duplicate while `PROCESSING` leaves exactly one `charge` call.
  - Retry/finalize-only: first call records `PROVIDER_SUCCEEDED`; retry finalizes with no second `charge`.
  - Failed retry: deterministic failure leaves one order at attempt 1; explicit retry reuses order and reaches attempt 2.
  - Rollback: forced finalization failure leaves no payment/plan transition while retaining committed `PROVIDER_SUCCEEDED`.
- `PaymentCommandKeyFactoryTest`: PASS in focused Gradle run.
- H2 test context emitted schema drop logs during shutdown only. No local MySQL, disposable MySQL, production DB, or live Toss access was performed.

## Risks / Rollback
- Risks:
  - Concurrency proof is focused H2/Spring integration evidence, not disposable MySQL/InnoDB proof; MySQL proof remains downstream WI scope.
  - Existing retained DB application and command-column deployment ordering remain outside WI-006.
  - Receipt evidence is still called inside finalize transaction, matching existing WI-005 behavior; a failure intentionally proves `PROVIDER_SUCCEEDED` recovery.
- Rollback:
  - Revert only WI-006 code/test/deliverable changes: `UserSubscriptionService`, `PaymentCommandTransactionService` upgrade additions, `SubscriptionUpgradePaymentExecutor`, `PaymentOrderRepository` command-key lookup, and WI-006 tests.
  - Do not revert storage, acceptance, auth, refund, initial billing, schema, or manual DB patch changes owned by other WIs.
  - If code is rolled back after DB patch application, keep additive payment command columns/status values per design rollback policy.

## Follow-ups
- Next WI candidates:
  - `WI-20260714-ATS-007` renewal command lifecycle/isolation.
  - `WI-20260714-ATS-018` disposable MySQL concurrency proof.
  - `WI-20260714-ATS-023` payment/transaction reviewer pass.
