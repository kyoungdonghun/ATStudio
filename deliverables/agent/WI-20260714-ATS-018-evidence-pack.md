# Evidence Pack: WI-20260714-ATS-018

## Summary (one-liner)

- Independently verified payment command retry/concurrency contracts with fake providers, and fixed refund execution transaction boundaries for a reproduced in-scope defect.

## Scope / DoD Check

- [x] Provider success plus local failure converges through finalize-only without another Provider call.
- [x] Stale/ambiguous `PROCESSING` or pending commands do not blind replay.
- [x] Parallel billing/renewal/refund attempts converge under command state, locks, and unique constraints in the focused H2/Spring test environment.
- [x] One renewal agreement failure does not stop another agreement path in prior WI-007 evidence; WI-018 adds renewal finalize-only and worker-concurrency proof.
- [x] Focused classes pass.
- [x] Environment limitation is separated from application assertion failures.
- [ ] Current `compileTestJava` rerun is blocked by a non-owned security test compile error after an external concurrent edit; do not count it as a WI-018 application failure.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Financial traceability and approval boundary |
| 0 | `docs/standards/development-standards.md` | Java/Spring transaction and test standards |
| 0 | `docs/standards/documentation-standards.md` | Evidence document conventions |
| 0 | `docs/standards/glossary.md` | Canonical WI/payment terminology |
| 1 | `docs/policies/quality-gates.md` | Focused verification and evidence expectations |
| REQ | `deliverables/user/REQ-20260714-ATS-001.md` | Approved P1 remediation scope |
| Design | `docs/design/p1-payment-db-integrity-design.md` | Payment command and refund transaction contract |
| Evidence | `deliverables/agent/WI-20260714-ATS-004-evidence-pack.md` | Payment command fields and DB constraints prerequisite |
| Evidence | `deliverables/agent/WI-20260714-ATS-005-evidence-pack.md` | Initial billing command lifecycle prerequisite |
| Evidence | `deliverables/agent/WI-20260714-ATS-006-evidence-pack.md` | Upgrade command lifecycle prerequisite |
| Evidence | `deliverables/agent/WI-20260714-ATS-007-evidence-pack.md` | Renewal isolation prerequisite |
| Evidence | `deliverables/agent/WI-20260714-ATS-008-evidence-pack.md` | Refund reservation lock prerequisite |

**Injection rules applied:** assignee `re`; testing/reliability task; Tier 0 first, then quality/design/REQ/evidence pointers from the WI handoff.

## Evidence Pointers

- Production code changed for reproduced refund execution defect:
  - `src/main/java/com/atstudio/atstudio/service/PaymentRefundTransactionService.java:34` - `REQUIRES_NEW` refund execution claim and audit record.
  - `src/main/java/com/atstudio/atstudio/service/PaymentRefundTransactionService.java:65` - result recording entry point for success/failure/pending.
  - `src/main/java/com/atstudio/atstudio/service/PaymentRefundTransactionService.java:100` - Provider exception becomes durable pending confirmation.
  - `src/main/java/com/atstudio/atstudio/service/PaymentRefundTransactionService.java:151` - pending result/audit write.
  - `src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java:149` - refund execute now runs with `Propagation.NOT_SUPPORTED`.
  - `src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java:154` - local claim committed before Provider call.
  - `src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java:159` - Provider cancel call uses the persisted refund idempotency key outside the local transaction.
  - `src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java:167` - Provider exception path records pending outcome.
- Tests added/updated:
  - `src/test/java/com/atstudio/atstudio/service/PaymentCommandIndependentVerificationIntegrationTest.java:69` - concurrent initial billing confirm.
  - `src/test/java/com/atstudio/atstudio/service/PaymentCommandIndependentVerificationIntegrationTest.java:112` - ambiguous upgrade does not blind replay.
  - `src/test/java/com/atstudio/atstudio/service/PaymentCommandIndependentVerificationIntegrationTest.java:140` - renewal finalize-only retry.
  - `src/test/java/com/atstudio/atstudio/service/PaymentCommandIndependentVerificationIntegrationTest.java:172` - concurrent renewal workers.
  - `src/test/java/com/atstudio/atstudio/service/PaymentRefundResilienceIntegrationTest.java:103` - refund Provider exception plus retry on same idempotency key.
  - `src/test/java/com/atstudio/atstudio/service/PaymentRefundResilienceIntegrationTest.java:140` - concurrent refund reservations.
  - `src/test/java/com/atstudio/atstudio/service/PaymentRefundResilienceIntegrationTest.java:350` - fake refund Provider asserts no active local transaction.
  - `src/test/java/com/atstudio/atstudio/service/AdminPaymentRefundServiceTest.java:261` - execute unit test now verifies Provider call command and transaction-service result recording.

## Provider Call Counts and Committed Outcomes

| Scenario | Provider calls | Committed outcome |
|---|---:|---|
| Initial billing concurrent confirm | `confirm=1`, `charge=1` | 1 `PaymentOrder DONE`, 1 `SubscriptionPayment`, 1 `UserSubscription` |
| Upgrade ambiguous `null` result plus retry | `charge=1` | 1 `PaymentOrder PENDING_PROVIDER_CONFIRMATION`, 0 payments |
| Renewal success plus local finalize failure, then retry | `charge=1` total | first run: `PROVIDER_SUCCEEDED`, 0 payments; retry: `DONE`, 1 payment |
| Renewal concurrent workers | `charge=1` | 1 renewal order `DONE`, 1 payment |
| Refund Provider exception plus retry | `cancelPayment=2` | first run: `PENDING_PROVIDER_CONFIRMATION`; retry: `SUCCEEDED`; idempotency key reused twice |
| Refund concurrent reservation | `cancelPayment=0` | 1 `REQUESTED` refund of 6000 KRW; second 6000 KRW request rejected against 9900 KRW source |

## Commands & Outputs

- `.\gradlew.bat compileTestJava`
  - Result: PASS (`BUILD SUCCESSFUL in 2s`).
- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.PaymentCommandIndependentVerificationIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentRefundResilienceIntegrationTest" --tests "com.atstudio.atstudio.service.AdminPaymentRefundServiceTest"`
  - Result: blocked before focused execution by non-owned `src/test/java/com/atstudio/atstudio/service/CompanyCertificationSecurityVerificationTest.java:117` compile error after concurrent external change.
  - Error: `unreported exception Exception; must be caught or declared to be thrown`.
  - Classification: environment/worktree contention, not WI-018 application assertion failure.
- `javac --release 17 -cp <Gradle cache + build classes> -d build/classes/java/test ...AdminPaymentRefundServiceTest.java`
  - Result: PASS; used only because non-owned compile error blocked Gradle test compilation.
- `javac --release 17 -cp <Gradle cache + build classes> -d build/classes/java/test ...PaymentRefundResilienceIntegrationTest.java`
  - Result: PASS; used only because non-owned compile error blocked Gradle test compilation.
- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.PaymentCommandIndependentVerificationIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentRefundResilienceIntegrationTest" --tests "com.atstudio.atstudio.service.AdminPaymentRefundServiceTest" -x compileTestJava`
  - Result: PASS (`BUILD SUCCESSFUL in 17s`).
  - Test counts:
    - `PaymentCommandIndependentVerificationIntegrationTest`: 4 tests, 0 failures, 0 errors.
    - `PaymentRefundResilienceIntegrationTest`: 2 tests, 0 failures, 0 errors.
    - `AdminPaymentRefundServiceTest`: 10 tests, 0 failures, 0 errors.
    - Focused total: 16 tests, 0 failures, 0 errors, 0 skipped.
- `git diff --check -- <WI-018 owned tracked files>`
  - Result: PASS; only LF-to-CRLF working-copy warnings.
- `git diff --no-index --check -- NUL <WI-018 new files>`
  - Result: no whitespace diagnostics; exit code 1 is expected for no-index differences.

## Production Code Fix Record

- Reproduced defect: refund execution had Provider call and local mutation in one `@Transactional` method. A Provider runtime exception could roll back local `PROCESSING` state and leave no durable ambiguous outcome, violating the refund execution contract in `docs/design/p1-payment-db-integrity-design.md`.
- Fix: introduced `PaymentRefundTransactionService` for short `REQUIRES_NEW` claim/result phases and changed `AdminPaymentRefundService.executeRefund()` to run Provider calls outside local transactions.
- Non-goals: did not change refund reservation schema, maker-checker policy, security/frontend/DB rehearsal/runtime logs, live Toss, real DB, or server behavior.

## Risks / Rollback

- Risks:
  - H2 verifies Spring transaction boundaries and application convergence, but does not prove MySQL InnoDB lock wait/deadlock behavior.
  - Current full `compileTestJava` is temporarily blocked by non-owned `CompanyCertificationSecurityVerificationTest.java:117`; Phase 7 should rerun once that owner resolves the security test compile issue.
  - `AdminPaymentRefundService.java` and `AdminPaymentRefundServiceTest.java` already contained WI-008 refund reservation changes before WI-018; this evidence records only the added refund execution boundary and WI-018 tests.
- Rollback:
  - Revert `PaymentRefundTransactionService.java`.
  - Revert the `executeRefund()` delegation changes in `AdminPaymentRefundService.java`.
  - Revert WI-018 focused tests and summary/evidence files.
  - Do not revert WI-008 source-payment lock changes, security/frontend/DB rehearsal/runtime-log changes, or other agents' files.

## Follow-ups

- `WI-20260714-ATS-023`: payment/transaction reviewer should review the new refund execution split with the rest of the payment command lifecycle.
- `WI-20260714-ATS-025`: cross-layer reviewer should include the non-owned compile contention note.
- `WI-20260714-ATS-028` / Phase 7: rerun approved compile/test gate after the security test owner resolves `CompanyCertificationSecurityVerificationTest.java:117`.
