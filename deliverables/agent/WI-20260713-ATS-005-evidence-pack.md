# Evidence Pack: WI-20260713-ATS-005

## Summary

- Implemented local-first withdrawal billing cancellation, ID-only after-commit Provider cleanup, durable deduplicated retry incidents, and deleted-user renewal defenses without a schema change.

## Scope / DoD Check

- [x] Authenticates withdrawal before changing billing state.
- [x] Cancels a non-terminal Toss billing agreement and ACTIVE subscription locally before user deletion.
- [x] Publishes only the billing-agreement ID when cleanup is required.
- [x] Handles Provider cleanup after commit in an agreement-specific new transaction.
- [x] Retains encrypted key material and local cancellation when Provider deletion fails or throws.
- [x] Reuses `LOCAL_DONE_PROVIDER_NOT_DONE` with agreement-ID dedupe, `WARNING` severity, `CANCELLED` local status, and `BILLING_KEY_DELETE_FAILED` Provider status.
- [x] Resolves the matching incident and clears issued-key fields after successful cleanup.
- [x] Limits daily retry selection to deleted users with `CANCELLED` agreements and retained encrypted key material.
- [x] Excludes deleted users in the due-renewal query and before key decryption/order creation in the service.
- [x] Adds focused transaction-order, success/failure/retry, incident lifecycle, query-filter, and zero-charge tests.
- [x] Avoids schema, auto-refund, live Toss, production-data, media, and mail changes.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Transaction traceability and reliable financial operations |
| 0 | `docs/standards/development-standards.md` | Spring service, transaction, repository, and test standards |
| 0 | `docs/standards/documentation-standards.md` | Evidence and output documentation baseline |
| 0 | `docs/standards/glossary.md` | Canonical WI and subscription terminology |
| 1 | `docs/policies/security-policy.md` | Billing-key secret handling and log minimization |
| 1 | `docs/policies/quality-gates.md` | HIGH-criticality regression and evidence requirements |
| 2 | `docs/design/p0-release-blocker-remediation-design.md` | Canonical withdrawal, cleanup, incident, retry, and renewal contract |
| 2 | `docs/design/payment-integration-design.md` | Existing billing-agreement states and Provider abstraction |
| 2 | `docs/design/payment-operations-runbook.md` | Existing incident lifecycle and no-auto-refund boundary |
| Context | `deliverables/user/REQ-20260713-ATS-001.md` | Approved P0 scope and success criteria |
| Context | `deliverables/agent/WI-20260713-ATS-002-evidence-pack.md` | Approved design-contract evidence |
| Handoff | `deliverables/agent/WI-20260713-ATS-005-handoff.md` | Ownership, DoD, output, and rollback contract |

Injection source: WI-005 handoff `INPUT POINTERS` plus mandatory Tier 0 documents; assignee `se`; task type `security/implementation`.

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/service/UserService.java:123-146`
  - Cancels agreement/subscription, publishes the ID-only event while local billing is already non-chargeable, then deletes transient rows and marks the user deleted.
- `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupRequestedEvent.java:3-5`
  - Event payload contains exactly one field: `billingAgreementID`.
- `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupCoordinator.java:19-68`
  - Uses `AFTER_COMMIT`, runs the daily 01:15 single-server retry, and continues across agreement-specific failures without logging exception messages.
- `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupService.java:44-95`
  - Selects targeted retry IDs, opens `REQUIRES_NEW` per agreement, invokes the registered Provider, retains keys on failure, and clears keys/resolves incidents on success.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java:63-108`
  - Creates/increments and resolves the existing issue type using an agreement-scoped deterministic dedupe key.
- `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java:25-38`
  - Excludes deleted users from due renewals and selects only deleted/CANCELLED/key-retaining cleanup candidates.
- `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:114-125`
  - Cancels and skips a deleted user before key validation, subscription lookup, order creation, decryption, or charge.
- `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java:64-126`
  - Asserts password-first behavior and verifies agreement/subscription cancellation at event publication time while the user is not yet marked deleted.
- `src/test/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupServiceTest.java:53-128`
  - Verifies `REQUIRES_NEW`, Provider success, failure retention, secret-free exception handling, and eligibility guards.
- `src/test/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupCoordinatorTest.java:24-66`
  - Verifies the one-field event, `AFTER_COMMIT`, daily schedule, and retry continuation.
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentServiceTest.java:146-232`
  - Verifies incident creation fields, dedupe/reopen with occurrence increment, and resolution.
- `src/test/java/com/atstudio/atstudio/repository/BillingAgreementRepositoryTest.java:47-114`
  - Verifies database-boundary deleted-user exclusion and exact cleanup-candidate filtering.
- `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java:268-291`
  - Verifies zero key-decrypt, order-save, subscription-lookup, and Provider-charge invocations for a deleted user.
- `build/test-results/test/TEST-com.atstudio.atstudio.service.UserServiceTest.xml`
- `build/test-results/test/TEST-com.atstudio.atstudio.service.RecurringRenewalServiceTest.xml`
- `build/test-results/test/TEST-com.atstudio.atstudio.repository.BillingAgreementRepositoryTest.xml`
- `build/test-results/test/TEST-com.atstudio.atstudio.service.PaymentReconciliationIncidentServiceTest.xml`
- `build/test-results/test/TEST-com.atstudio.atstudio.service.WithdrawalBillingCleanupServiceTest.xml`
- `build/test-results/test/TEST-com.atstudio.atstudio.service.WithdrawalBillingCleanupCoordinatorTest.xml`

## Commands & Outputs

- Focused Gradle command:
  - `gradlew.bat test --tests "com.atstudio.atstudio.service.UserServiceTest" --tests "com.atstudio.atstudio.service.RecurringRenewalServiceTest" --tests "com.atstudio.atstudio.repository.BillingAgreementRepositoryTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationIncidentServiceTest" --tests "com.atstudio.atstudio.service.WithdrawalBillingCleanupServiceTest" --tests "com.atstudio.atstudio.service.WithdrawalBillingCleanupCoordinatorTest"`
- Initial run:
  - `BUILD FAILED in 16s`; 52 tests executed, 2 fixture failures.
  - `UserServiceTest`: Mockito selected the `ApplicationEvent` overload during stubbing instead of `publishEvent(Object)`.
  - `BillingAgreementRepositoryTest`: one fixture nickname exceeded the entity's 20-character limit.
  - Product and test compilation succeeded; both fixture defects were corrected without changing product behavior.
- Second run:
  - `BUILD SUCCESSFUL in 16s`; 52 tests passed after fixture correction.
- Final run after null-Provider-result hardening and billing-incident creation coverage:
  - `BUILD SUCCESSFUL in 17s`; 53 tests passed, 0 failures, 0 errors, 0 skipped.
  - Suite counts: UserService 28, RecurringRenewalService 10, BillingAgreementRepository 3, PaymentReconciliationIncidentService 6, WithdrawalBillingCleanupService 4, WithdrawalBillingCleanupCoordinator 2.
  - JUnit execution time from XML totals: 11.228 seconds.
- `git diff --check`
  - Exit code 0; no whitespace errors. Output contained LF-to-CRLF working-copy warnings only.

Provider tests use a mocked `RecurringPaymentProvider`; no Toss endpoint was called. Repository tests use the test-only H2 lifecycle; no user or production data was mutated.

## Risks / Rollback

- Risk: The daily retry is intentionally single-server and has no distributed scheduler lock, matching the current operations runbook boundary.
- Risk: As with any external call plus local commit, a Provider success followed by a local database commit failure relies on the retained key and a later repeat-delete attempt; no schema/outbox expansion was allowed in this WI.
- Rollback: Revert the listed product/test files and the two WI-005 outputs. No schema or data rollback is needed.

## Follow-ups

- WI-20260713-ATS-008 can independently verify withdrawal transaction ordering, incident lifecycle, retry targeting, and zero-charge behavior.
