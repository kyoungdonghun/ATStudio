# Evidence Pack: WI-20260713-ATS-008

## Summary

- Independently verified WI-005 withdrawal billing reliability and fixed the already-removed Provider compensation path so local key cleanup and incident resolution converge after retry.

## Scope / DoD Check

- [x] Verified password-first and local-first withdrawal ordering.
- [x] Verified ID-only `AFTER_COMMIT` cleanup dispatch.
- [x] Verified agreement-specific `REQUIRES_NEW` cleanup attempts.
- [x] Verified ordinary Provider failure retains local key material and records a deduplicated incident.
- [x] Added a failing-first compensation test for `ALREADY_REMOVED_BILLING_KEY`.
- [x] Corrected compensation to clear local key material and resolve the matching incident.
- [x] Verified retry selection is limited to deleted users with `CANCELLED` agreements and retained keys.
- [x] Verified the due-renewal query excludes deleted users.
- [x] Verified the service guard produces zero decrypt, order-save, subscription-lookup, and Provider-charge calls for deleted users.
- [x] Preserved schema, auto-refund, Provider adapter, media, mail, unrelated docs, runtime logs, and production data.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and financial traceability baseline |
| 0 | `docs/standards/development-standards.md` | Spring transaction, repository, and test standards |
| 0 | `docs/standards/documentation-standards.md` | Two-set deliverable and evidence baseline |
| 0 | `docs/standards/glossary.md` | Canonical WI and subscription terminology |
| 1 | `.claude/agents/re.md` | Independent verification and regression role contract |
| 1 | `docs/policies/security-policy.md` | Billing-key secret handling and log minimization |
| 1 | `docs/policies/quality-gates.md` | Regression and HIGH-criticality evidence requirements |
| 2 | `docs/design/p0-release-blocker-remediation-design.md` | Withdrawal, compensation, retry, and zero-charge contract |
| 2 | `docs/design/payment-operations-runbook.md` | Incident lifecycle and no-auto-refund boundary |
| Context | `deliverables/agent/WI-20260713-ATS-005-evidence-pack.md` | WI-005 owned paths, claims, commands, and residual risk |
| Handoff | `deliverables/agent/WI-20260713-ATS-008-handoff.md` | Scope, DoD, constraints, and output contract |

Injection source: WI-008 handoff `INPUT POINTERS` plus mandatory Tier 0 documents and the RE persona; assignee `re`; task type `testing/review`.

## Independent Findings

### F-001 - Already-removed compensation did not converge

- Before correction, `WithdrawalBillingCleanupService.cleanup()` treated every `success=false` result as a cleanup failure.
- Reproduction: the new test supplied `BillingAgreementCancelResult.failure("ALREADY_REMOVED_BILLING_KEY", ...)` after a prior Provider-side deletion.
- Observed pre-fix result: `CleanupOutcome.FAILED`; the focused class run failed 1 of 5 tests at the success-outcome assertion.
- Correction: classify exactly `ALREADY_REMOVED_BILLING_KEY` as completed Provider cleanup, then reuse the existing key-clear and incident-resolution path.
- Ordinary failures remain unchanged and retryable.

### Reviewed without further correction

- Transaction ordering: local agreement/subscription cancellation occurs before event publication; listener phase is `AFTER_COMMIT`; cleanup propagation is `REQUIRES_NEW`.
- Query behavior: due renewals require `u.isDeleted = false`; cleanup candidates require deleted user, `CANCELLED` agreement, and nonblank encrypted key.
- Zero-charge behavior: deleted-user service guard runs before key decryption, subscription lookup, order creation, and Provider invocation.

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupService.java:26`
  - Defines the explicit idempotent Provider completion code.
- `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupService.java:49`
  - Agreement cleanup uses `REQUIRES_NEW`.
- `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupService.java:85`
  - Failure recording now occurs only when Provider cleanup is not complete.
- `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupService.java:93`
  - Shared completion path clears key material and resolves the incident.
- `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupService.java:98`
  - Completion predicate accepts success or exact `ALREADY_REMOVED_BILLING_KEY`.
- `src/test/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupServiceTest.java:102`
  - Failing-first compensation regression test verifies successful outcome, full local key clearing, next-billing clearing, incident resolution, and no failure recording.
- `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupCoordinator.java:19`
  - ID-only event is handled after local commit.
- `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java:28`
  - Due-renewal query excludes deleted users.
- `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java:35`
  - Cleanup retry query applies the deleted/cancelled/key-retained boundary.
- `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:118`
  - Service-level deleted-user guard cancels and skips before charging work.
- `src/test/java/com/atstudio/atstudio/repository/BillingAgreementRepositoryTest.java:72`
  - Repository cleanup-candidate boundary test.
- `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java:271`
  - Zero-decrypt, zero-order, zero-subscription-lookup, and zero-charge test.
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentServiceTest.java:213`
  - Matching agreement-scoped incident resolution test.
- Test result XML:
  - `build/test-results/test/TEST-com.atstudio.atstudio.service.UserServiceTest.xml`
  - `build/test-results/test/TEST-com.atstudio.atstudio.service.RecurringRenewalServiceTest.xml`
  - `build/test-results/test/TEST-com.atstudio.atstudio.repository.BillingAgreementRepositoryTest.xml`
  - `build/test-results/test/TEST-com.atstudio.atstudio.service.PaymentReconciliationIncidentServiceTest.xml`
  - `build/test-results/test/TEST-com.atstudio.atstudio.service.WithdrawalBillingCleanupServiceTest.xml`
  - `build/test-results/test/TEST-com.atstudio.atstudio.service.WithdrawalBillingCleanupCoordinatorTest.xml`

## Corrective Diff

- Product: one completion-code constant and one completion predicate in `WithdrawalBillingCleanupService`; no Provider adapter or shared result-contract change.
- Test: one compensation regression case in `WithdrawalBillingCleanupServiceTest`.
- Deliverables: WI-008 user summary and Evidence Pack only.

## Commands & Outputs

- Pre-fix regression reproduction:
  - `.\gradlew.bat test --tests "com.atstudio.atstudio.service.WithdrawalBillingCleanupServiceTest"`
  - `BUILD FAILED in 5s`; 5 tests completed, 1 failed.
  - Failed test: `cleanup_alreadyRemovedBillingKeyConvergesToSuccess`.
- Post-fix focused cleanup verification:
  - Same command.
  - `BUILD SUCCESSFUL in 6s`; 5 tests passed.
- Final focused WI-005 regression suite:
  - `.\gradlew.bat test --tests "com.atstudio.atstudio.service.UserServiceTest" --tests "com.atstudio.atstudio.service.RecurringRenewalServiceTest" --tests "com.atstudio.atstudio.repository.BillingAgreementRepositoryTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationIncidentServiceTest" --tests "com.atstudio.atstudio.service.WithdrawalBillingCleanupServiceTest" --tests "com.atstudio.atstudio.service.WithdrawalBillingCleanupCoordinatorTest"`
  - `BUILD SUCCESSFUL in 14s`; 54 tests, 0 failures, 0 errors, 0 skipped.
  - Suite counts: UserService 28, RecurringRenewalService 10, BillingAgreementRepository 3, PaymentReconciliationIncidentService 6, WithdrawalBillingCleanupService 5, WithdrawalBillingCleanupCoordinator 2.
  - JUnit XML execution-time total: 11.352 seconds.
- `git diff --check`
  - Exit code 0; no whitespace errors. Output contained working-copy line-ending warnings only.

Provider behavior used a mocked `RecurringPaymentProvider`; no Toss endpoint was called. Repository tests used the test-only H2 lifecycle; no user or production data was mutated.

## Risks / Rollback

- Risk: Daily cleanup retry remains single-server without a distributed scheduler lock, matching the current approved operations boundary.
- Risk: No live Toss behavior was exercised by constraint; the compensation contract is verified at the Provider result boundary.
- Rollback: Revert only the two WI-008 code/test edits and the two WI-008 outputs. No schema or data rollback is needed.

## Follow-ups

- WI-20260713-ATS-008 unblocks WI-20260713-ATS-010 and WI-20260713-ATS-013 per its handoff packet.
