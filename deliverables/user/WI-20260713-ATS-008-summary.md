# WI-20260713-ATS-008 Withdrawal Billing Reliability Summary

## Outcome

- **PASS after one corrective fix.** Independent review reproduced a compensation defect in the WI-005 cleanup path.
- When Provider deletion had already succeeded but a retry returned `ALREADY_REMOVED_BILLING_KEY`, the cleanup previously retained local key material and left the reconciliation incident unresolved.
- The cleanup now treats only that explicit idempotent Provider response as completed cleanup. It clears encrypted key material and resolves the agreement-scoped incident.
- Other Provider failures still retain the retryable key and create or increment the durable incident.

## Independent Review

- Withdrawal keeps billing cancellation local-first and publishes an ID-only cleanup event before the user is marked deleted. The listener executes only `AFTER_COMMIT`.
- Each cleanup attempt runs in an agreement-specific `REQUIRES_NEW` transaction. A Provider success followed by local commit failure can therefore converge on retry through the already-removed response.
- The due-renewal query excludes deleted users. The renewal service independently cancels and skips a deleted user before key decryption, subscription lookup, order creation, or Provider charge.
- The cleanup retry query selects only deleted users with `CANCELLED` agreements and retained encrypted key material.

## Verification

- Regression reproduction before the product fix:
  - `gradlew.bat test --tests "com.atstudio.atstudio.service.WithdrawalBillingCleanupServiceTest"`
  - Result: `BUILD FAILED`; 5 tests executed, 1 expected compensation-test failure.
- Focused cleanup test after the fix:
  - Same command.
  - Result: `BUILD SUCCESSFUL in 6s`; 5 tests passed.
- Final WI-005 billing regression suite:
  - `gradlew.bat test --tests "com.atstudio.atstudio.service.UserServiceTest" --tests "com.atstudio.atstudio.service.RecurringRenewalServiceTest" --tests "com.atstudio.atstudio.repository.BillingAgreementRepositoryTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationIncidentServiceTest" --tests "com.atstudio.atstudio.service.WithdrawalBillingCleanupServiceTest" --tests "com.atstudio.atstudio.service.WithdrawalBillingCleanupCoordinatorTest"`
  - Result: `BUILD SUCCESSFUL in 14s`; 54 tests passed, 0 failures, 0 errors, 0 skipped.
- `git diff --check` exited 0 with line-ending warnings only.
- Provider behavior used Mockito test doubles. Repository behavior used the test-only H2 lifecycle. No Toss endpoint or production data was used.

## Changed Paths

- `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupService.java`
- `src/test/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupServiceTest.java`
- `deliverables/user/WI-20260713-ATS-008-summary.md`
- `deliverables/agent/WI-20260713-ATS-008-evidence-pack.md`

No schema, refund, Provider adapter, media, mail, or unrelated documentation path was changed.

## Risk / Rollback

- The retry remains intentionally single-server without a distributed scheduler lock, matching the approved design boundary.
- Rollback: revert only the two cleanup source/test edits and the two WI-008 outputs. No schema or data rollback is required.
