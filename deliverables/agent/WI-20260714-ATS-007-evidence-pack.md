# Evidence Pack: WI-20260714-ATS-007

## Summary (one-liner)
- Implemented renewal command identity, keyset due scanning, per-agreement transaction isolation, stale/no-blind-replay handling, malformed Provider-result handling, and focused tests.

## Scope / DoD Check
- DoD items:
  - [x] Renewal lookup includes agreement, user subscription, purpose, and billing period start.
  - [x] Candidate IDs use bounded ascending keyset pages.
  - [x] Each agreement claim/outcome/finalize commits independently through short `REQUIRES_NEW` methods.
  - [x] `SubscriptionScheduler.processRecurringRenewals()` no longer owns a batch transaction.
  - [x] Stale `PROCESSING`, null Provider result, and blank success transaction ID do not blind replay; they become `PENDING_PROVIDER_CONFIRMATION` or fail closed.
  - [x] First agreement success remains committed when later agreements fail.
  - [x] Focused compile, tests, and diff checks passed.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Financial traceability and approval boundary |
| 0 | `docs/standards/development-standards.md` | Spring transaction and test standards |
| 1 | `docs/policies/quality-gates.md` | Verification and evidence expectations |
| 2 | `deliverables/user/REQ-20260714-ATS-001.md` | Approved remediation scope |
| 2 | `docs/design/p1-payment-db-integrity-design.md` | Renewal identity, transaction boundary, stale recovery, and test contract |
| 2 | `deliverables/agent/WI-20260714-ATS-004-evidence-pack.md` | Entity/schema command fields prerequisite |
| 2 | `deliverables/agent/WI-20260714-ATS-005-evidence-pack.md` | Shared command lifecycle prerequisite |
| 2 | `deliverables/agent/WI-20260714-ATS-006-evidence-pack.md` | Upgrade command helper extension prerequisite |
| 2 | `deliverables/agent/WI-20260714-ATS-007-handoff.md` | WI-specific scope, DoD, and output contract |

**Injection Rules Applied**:
- Rule source: `AGENTS.md` / WI handoff packet
- Assignee: `se`
- Task type: backend implementation / payment integrity
- agent_required_tiers: `[0, 1, 2]`

## Evidence Pointers (required)
- Files changed:
  - `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:35` - bounded keyset page size.
  - `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:71` - due IDs scanned by `lastSeenID` + `PageRequest`.
  - `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:100` - contract-level catch keeps the batch moving after local exceptions.
  - `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:163` - null Provider result becomes pending confirmation.
  - `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:172` - blank success transaction ID becomes pending confirmation.
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:224` - `claimRenewal` locks agreement/subscription and claims exact-period command.
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:381` - renewal Provider failure/pending result persists in a new transaction.
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:561` - `finalizeRenewal` creates one payment, advances period, and marks order done.
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:651` - creates/reloads exact-period renewal order only.
  - `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java:42` - bounded keyset due-ID query.
  - `src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java:45` - exact renewal period pessimistic lookup.
  - `src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java:35` - locked active-subscription lookup.
  - `src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java:33` - recurring renewal scheduler entry has no transaction annotation.
- Tests changed/added:
  - `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java:64` - keyset page scan test.
  - `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java:107` - stale processing does not blind replay Provider.
  - `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java:123` - contract-level catch continues after finalize-only failure.
  - `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java:148` - null Provider result records pending and continues.
  - `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java:174` - blank success transaction ID records pending.
  - `src/test/java/com/atstudio/atstudio/service/RecurringRenewalCommandIntegrationTest.java:79` - old period order is not reused.
  - `src/test/java/com/atstudio/atstudio/service/RecurringRenewalCommandIntegrationTest.java:127` - first agreement success survives later agreement failure.
  - `src/test/java/com/atstudio/atstudio/service/RecurringRenewalCommandIntegrationTest.java:159` - null/blank Provider results persist as pending and later success continues.
  - `src/test/java/com/atstudio/atstudio/service/BillingAgreementCommandIntegrationTestSupport.java:209` - focused Provider fixture can queue null/malformed results.

## Commands & Outputs
- Commands executed:
  - `.\gradlew.bat compileTestJava`
  - `.\gradlew.bat test --tests "com.atstudio.atstudio.service.RecurringRenewalServiceTest" --tests "com.atstudio.atstudio.service.RecurringRenewalCommandIntegrationTest" --tests "com.atstudio.atstudio.service.SubscriptionSchedulerTest" --tests "com.atstudio.atstudio.repository.BillingAgreementRepositoryTest" --tests "com.atstudio.atstudio.service.PaymentCommandKeyFactoryTest"`
  - `git diff --check -- <WI-007 owned tracked files>`
  - `git diff --no-index --check -- NUL <WI-007 owned untracked files>`
- Outputs:
  - `compileTestJava`: `BUILD SUCCESSFUL`
  - Focused tests: `BUILD SUCCESSFUL`; 18 tests completed, 0 failed.
  - `git diff --check`: no whitespace diagnostics; Git printed only LF-to-CRLF working-copy warnings.
  - `git diff --no-index --check`: no whitespace diagnostics for untracked WI-owned files; exit code 1 is expected for no-index differences.

## Tests
- `RecurringRenewalServiceTest`: keyset scan, successful provider/finalize path, stale processing no blind replay, contract-level catch, null Provider pending, blank-success pending.
- `RecurringRenewalCommandIntegrationTest`: old-period non-reuse, first-success/later-failure commit isolation, null/blank Provider durable pending with later agreement success.
- `SubscriptionSchedulerTest`: scheduler delegation remains intact after removing transaction from recurring-renewal entry point.
- `BillingAgreementRepositoryTest`: existing due-ID and lock repository behavior still passes.
- `PaymentCommandKeyFactoryTest`: renewal command and attempt key formats still pass.

## Risks / Rollback
- Risks:
  - Focused concurrency and isolation evidence uses local Spring/H2 tests, not disposable MySQL/InnoDB; downstream MySQL proof remains in later WI scope.
  - Provider lookup/reconciliation API is still unavailable, so malformed/stale renewal outcomes are parked as `PENDING_PROVIDER_CONFIRMATION` rather than auto-replayed.
  - Existing DB patch application and live Toss validation remain outside this WI.
- Rollback:
  - Revert WI-007 changes in `RecurringRenewalService`, `SubscriptionScheduler`, renewal additions in `PaymentCommandTransactionService`, renewal repository methods, and WI-007 tests.
  - Do not revert WI-004/005/006 command fields, initial/upgrade command work, refund, storage, acceptance, auth, or image changes owned by other WIs.
  - If additive DB changes have been separately applied, keep command columns/statuses/unique constraints per design rollback policy.

## Follow-ups
- Blocks released for:
  - `WI-20260714-ATS-018` payment integration/concurrency tests after remaining prerequisites.
  - `WI-20260714-ATS-023` payment/transaction reviewer pass.
  - `WI-20260714-ATS-025` cross-layer reviewer pass.
