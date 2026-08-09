# WI-20260809-ATS-008 Subscription Correction Repair Summary

## Outcome

WI-20260809-ATS-008 is implemented and focused verification is complete.
Subscription-correction request, approval, and execution now serialize their
final privilege check with administrator demotion and withdrawal. Request and
approval business rejections now produce durable, phase-specific minimal audit
records.

## Implemented Repairs

- Replaced ordinary actor reads in all three correction mutation paths with the
  shared `UserRepository.findByIdForUpdate` pessimistic row lock.
- Captured the authenticated actor ID before domain locking, then reloaded and
  rechecked the actor only after existing domain/correction locks and
  immediately before mutation.
- Added `USER_SUBSCRIPTION_CORRECTION_REQUEST` and
  `USER_SUBSCRIPTION_CORRECTION_APPROVAL` rejection actions.
- Added request and approval rejection persistence through the existing
  separate `AdminOperationRejectionAuditService` `REQUIRES_NEW` bean.
- Preserved the original `BusinessException` when audit persistence fails by
  attaching the audit failure as a suppressed exception.
- Added focused actor-demotion, lock-order, audit-content, rollback-survival,
  and audit-failure tests.
- Kept preview receipts, free-text DLP, schema/index work, and provider actions
  outside this WI.

## Lock Order

- Withdrawal: BillingAgreement -> UserSubscription -> active ADMIN User rows.
- Correction request: BillingAgreement -> UserSubscription -> target
  Subscription -> non-terminal correction rows -> actor User.
- Correction approval: correction -> actor User.
- Correction execution: BillingAgreement -> UserSubscription -> target
  Subscription -> correction -> actor User.

Correction never acquires another domain lock after the actor row. This keeps
the shared actor lock compatible with withdrawal and avoids a new lock-order
cycle.

## Rejection Audit Semantics

- Request target: `USER_SUBSCRIPTION` plus the requested UserSubscription ID.
- Approval target: `ADMIN_SUBSCRIPTION_CORRECTION` plus the correction ID.
- Actor: authenticated actor ID when available, including a stale principal
  whose current row no longer has ADMIN privilege.
- Reason: stable `BUSINESS_ERROR.name()` value.
- State: bounded subscription state for request when available; bounded
  UserSubscription ID and correction status for approval.
- Excluded: request bodies, approval notes, operator reasons, tokens, billing
  keys, provider customer keys, and other raw secrets.
- Durability: rejection persistence uses `REQUIRES_NEW`; an audit failure does
  not replace the original business error.

## Changed Files

- `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java`
- `src/main/java/com/atstudio/atstudio/service/AdminOperationRejectionAuditService.java`
- `src/main/java/com/atstudio/atstudio/service/AdminOperationAuditState.java`
- `src/main/java/com/atstudio/atstudio/entity/enums/AdminOperationAuditAction.java`
- `src/main/java/com/atstudio/atstudio/entity/enums/AdminOperationAuditTargetType.java`
- `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/AdminOperationAuditTransactionIntegrationTest.java`
- `docs/SR/SR-96.md`
- `docs/SR/SR-97.md`
- `docs/design/api-spec.md`
- `docs/design/usecase/user-subscription.md`
- `deliverables/user/WI-20260809-ATS-008-summary.md`
- `deliverables/agent/WI-20260809-ATS-008-evidence-pack.md`

## Verification

Focused Gradle command:

```powershell
.\gradlew.bat test `
  --tests "com.atstudio.atstudio.service.AdminSubscriptionCorrectionServiceTest" `
  --tests "com.atstudio.atstudio.service.AdminOperationAuditTransactionIntegrationTest" `
  --tests "com.atstudio.atstudio.controller.AdminUserSubscriptionCorrectionControllerTest" `
  --tests "com.atstudio.atstudio.repository.AdminSubscriptionCorrectionRepositoryContractTest"
```

Result: `BUILD SUCCESSFUL`; 59 tests passed, 0 failed, 0 errors, 0 skipped.

Scoped whitespace verification: `git diff --check` passed for all 13 WI-008
code, test, current-state documentation, and deliverable files. The opt-in
MySQL race suite was not run
because it requires a disposable external database and destructive fixture
cleanup. Explicit pessimistic-lock annotation and invocation-order contracts
were run instead. No full suite was run, as required.

## External Effects and Constraints

- Local correction provider/payment/email call count remains zero.
- No schema, index, migration, retained data, preview receipt/token, free-text
  DLP, controller route, secret, ZIP, commit, or push change was made.
- Existing dirty worktree changes were preserved.

## Risks and Rollback

- Residual risk: live InnoDB timing for correction versus actor
  demotion/withdrawal remains unexecuted in this WI. Focused tests prove the
  shared pessimistic lock contract and call order, not a live MySQL schedule.
- Rollback: apply an inverse patch only to the WI-008 actor-lock, rejection
  action/target, audit helper, focused-test, and current-state documentation
  additions listed above. Do not delete or restore whole untracked files,
  because they also contain prior approved WI work.

## WI-20260808-ATS-028 Status

`WI-20260808-ATS-028` remains blocked. This WI repairs reviewer BLOCKER-001 and
MAJOR-002, but MAJOR-001 (server-bound preview evidence), MAJOR-003 (free-text
sensitive-data controls), and MINOR-001 (active-admin lock access path/index or
accepted MySQL evidence) remain explicitly outside WI-008.
