# WI-20260713-ATS-005 Withdrawal Billing Stop Summary

## Outcome

- Account withdrawal now cancels the local non-terminal Toss billing agreement and ACTIVE subscription before marking the user deleted.
- Withdrawal publishes an event containing only the billing-agreement ID when encrypted key material remains.
- Provider billing-key deletion runs only after the withdrawal transaction commits and cannot roll back or reactivate the locally cancelled state.
- Provider failure retains encrypted key material and creates or increments one durable `LOCAL_DONE_PROVIDER_NOT_DONE` incident per billing agreement.
- A daily targeted retry processes only deleted users with `CANCELLED` agreements and retained encrypted key material. Success clears the issued key and resolves the matching incident.
- Due-renewal selection excludes deleted users in JPQL, and the renewal service independently cancels any deleted user that reaches its guard before key decryption, order creation, or charge invocation.
- No schema, refund behavior, media path, mail path, or Provider adapter was changed.

## Verification

- Focused command:
  - `gradlew.bat test --tests "com.atstudio.atstudio.service.UserServiceTest" --tests "com.atstudio.atstudio.service.RecurringRenewalServiceTest" --tests "com.atstudio.atstudio.repository.BillingAgreementRepositoryTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationIncidentServiceTest" --tests "com.atstudio.atstudio.service.WithdrawalBillingCleanupServiceTest" --tests "com.atstudio.atstudio.service.WithdrawalBillingCleanupCoordinatorTest"`
  - Final result: `BUILD SUCCESSFUL in 17s`; 53 tests passed, 0 failed, 0 errors, 0 skipped.
- `git diff --check`
  - Result: exit code 0; no whitespace errors. Git reported only expected LF-to-CRLF working-copy warnings.
- Provider behavior was verified with Mockito test doubles; no Toss endpoint was called.
- Repository filtering was verified against the test-only H2 database; no user or production data was mutated.

## Changed Paths

- `src/main/java/com/atstudio/atstudio/service/UserService.java`
- `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java`
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java`
- `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java`
- `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupRequestedEvent.java`
- `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupService.java`
- `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupCoordinator.java`
- `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentServiceTest.java`
- `src/test/java/com/atstudio/atstudio/repository/BillingAgreementRepositoryTest.java`
- `src/test/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupCoordinatorTest.java`
- `deliverables/user/WI-20260713-ATS-005-summary.md`
- `deliverables/agent/WI-20260713-ATS-005-evidence-pack.md`

## Rollback

- Revert only the product/test paths above and the two WI-005 output files.
- No schema or data rollback is required.
