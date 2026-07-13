# Evidence Pack: WI-20260713-ATS-010

## Summary

- Reviewed local-first withdrawal, after-commit Provider cleanup, retry convergence, incident lifecycle, and renewal stop behavior.

## Transaction Findings

| Boundary | Result |
|---|---|
| Password authentication | Completes before billing mutation. |
| Local agreement/subscription cancellation | Occurs inside the withdrawal transaction. |
| Cleanup event payload | Contains only `billingAgreementID`. |
| Provider delete | Runs through an `AFTER_COMMIT` listener and `REQUIRES_NEW` service transaction. |
| Provider failure | Keeps agreement CANCELLED and encrypted key available for retry. |
| Already-removed response | Converges to success, clears key, and resolves Incident. |
| Retry selection | Deleted user + CANCELLED agreement + nonblank key only. |
| Renewal scheduler | Repository and service guard both exclude deleted users. |

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/service/UserService.java`
- `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupRequestedEvent.java`
- `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupCoordinator.java`
- `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupService.java`
- `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java`
- `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java`
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java`
- Focused tests in the corresponding repository/service test files.

## Commands and Results

- Combined focused Gradle run: exit 0.
- Aggregate: 11 suites, 133 tests, 0 failures, 0 errors, 0 skipped.
- Schema compatibility check: `LOCAL_DONE_PROVIDER_NOT_DONE` exists in Java enum and `schema.sql` MySQL ENUM.
- No live Provider, DB data, refund, or schema mutation was executed.

## Review Process Note

- The assigned CR agent was stopped after repeated timeouts and produced no final output. MA completed the transaction review from the current merged code, prior WI-005/WI-008 evidence, and fresh combined tests.

## Residual Risk / Rollback

- Single-server scheduler only, as approved.
- External success followed by local failure relies on idempotent repeated deletion; `ALREADY_REMOVED_BILLING_KEY` is explicitly convergent.
- Rollback is code/test-only and requires no schema or data rollback.
