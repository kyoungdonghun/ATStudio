# Evidence Pack: WI-20260809-ATS-008

## Summary (one-liner)

- Repaired correction actor serialization and durable request/approval
  rejection auditing, with 59 focused tests passing and no external provider
  action.

## Scope / DoD Check

- DoD items:
  - [x] Request, approval, and execution recheck the actor through the shared
    pessimistic User-row lock at the privileged mutation boundary.
  - [x] Lock order is explicit and compatible with withdrawal.
  - [x] Request and approval `BusinessException` rejections use durable,
    phase-specific `REQUIRES_NEW` audit records.
  - [x] Audit failure preserves the original stable business error.
  - [x] Audit state is bounded and excludes request bodies and raw secrets.
  - [x] Local correction still performs zero provider/payment/email calls.
  - [x] Focused service, audit transaction, controller, and repository contract
    tests pass.
  - [x] Current-state SR/API/use-case documentation is updated.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Project constitution and language policy |
| 0 | `docs/standards/development-standards.md` | Service, transaction, exception, and test rules |
| 1 | `docs/policies/security-policy.md` | Minimal audit and sensitive-data constraints |
| 1 | `docs/policies/access-control-policy.md` | Least privilege and separation of duties |
| Context | `deliverables/user/REQ-20260808-ATS-004.md` | Approved requirement baseline |
| Context | `deliverables/user/WI-20260809-ATS-004-summary.md` | Reviewer findings and repair targets |
| Context | `deliverables/agent/WI-20260809-ATS-004-evidence-pack.md` | Detailed reviewer evidence |
| Context | `docs/SR/SR-96.md` | Administrator role/withdrawal contract |
| Context | `docs/SR/SR-97.md` | Local subscription-correction contract |
| Handoff | `deliverables/agent/WI-20260809-ATS-008-handoff.md` | Authoritative scope and output contract |

**Injection Rules Applied:**

- Assignee: `se`
- Task type: privileged backend correction repair
- Execution boundary: current branch only; direct focused implementation and
  verification; no full suite, schema/data operation, external call, commit, or
  push.

## Evidence Pointers

### Actor Lock and Mutation Boundary

- `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:180`
  - Request captures actor ID, takes the existing domain/correction locks, then
    calls the actor lock immediately before saving the correction.
- `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:240`
  - Approval locks the correction, validates state/note, then locks the actor
    immediately before the transition.
- `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:263`
  - Execution takes agreement/subscription/target/correction locks, revalidates
    state and note, then locks the actor immediately before local mutation.
- `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:511`
  - Shared `UserRepository.findByIdForUpdate` lookup and active ADMIN recheck.

### Rejection Audit

- `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:541`
  - Request rejection state selection and safe audit invocation.
- `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:568`
  - Approval rejection state selection and safe audit invocation.
- `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:598`
  - Original exception preservation and bounded warning metadata.
- `src/main/java/com/atstudio/atstudio/service/AdminOperationRejectionAuditService.java:83`
  - `REQUIRES_NEW` request rejection persistence.
- `src/main/java/com/atstudio/atstudio/service/AdminOperationRejectionAuditService.java:101`
  - `REQUIRES_NEW` approval rejection persistence.
- `src/main/java/com/atstudio/atstudio/service/AdminOperationAuditState.java:56`
  - Bounded correction state serializer.
- `src/main/java/com/atstudio/atstudio/entity/enums/AdminOperationAuditAction.java:7`
  - Phase-specific request and approval actions.
- `src/main/java/com/atstudio/atstudio/entity/enums/AdminOperationAuditTargetType.java:6`
  - Correction audit target type.

### Focused Tests

- `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java:272`
  - Request lock order through actor lock and save.
- `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java:335`
  - Request rejects a demoted write-locked actor.
- `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java:367`
  - Request audit failure preserves the original error.
- `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java:393`
  - Approval correction-to-actor lock order.
- `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java:441`
  - Approval rejects a demoted write-locked actor.
- `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java:473`
  - Approval audit failure preserves the original error.
- `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java:534`
  - Execution rejects a demoted actor before local mutation.
- `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java:823`
  - Shared User-row `PESSIMISTIC_WRITE` annotation contract.
- `src/test/java/com/atstudio/atstudio/service/AdminOperationAuditTransactionIntegrationTest.java:211`
  - Request rejection audit survives outer rollback.
- `src/test/java/com/atstudio/atstudio/service/AdminOperationAuditTransactionIntegrationTest.java:237`
  - Approval rejection audit survives outer rollback.

### Current-State Documentation

- `docs/SR/SR-96.md:85` - cross-workflow actor/withdrawal lock contract.
- `docs/SR/SR-97.md:105` - current repair and audit semantics.
- `docs/design/api-spec.md:63` - mutation-boundary actor reload and stable error contract.
- `docs/design/usecase/user-subscription.md:80` - exact lock order, audit content, and provider boundary.

## Exact Changed Files

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

## Lock Order

- Withdrawal: BillingAgreement -> UserSubscription -> active ADMIN User rows.
- Request: BillingAgreement -> UserSubscription -> target Subscription ->
  non-terminal correction rows -> actor User.
- Approval: correction -> actor User.
- Execution: BillingAgreement -> UserSubscription -> target Subscription ->
  correction -> actor User.
- Cycle argument: correction takes no domain/correction lock after actor; role
  demotion locks active ADMIN rows without subsequently requesting correction
  domain rows; withdrawal takes the same target domain locks before ADMIN rows.

## Audit Semantics

- Request action/target:
  `USER_SUBSCRIPTION_CORRECTION_REQUEST` / `USER_SUBSCRIPTION`.
- Approval action/target:
  `USER_SUBSCRIPTION_CORRECTION_APPROVAL` /
  `ADMIN_SUBSCRIPTION_CORRECTION`.
- Common fields: actor ID when available, target ID, `REJECTED`, unchanged
  bounded before/after state, and stable `BUSINESS_ERROR.name()` reason code.
- Explicit exclusions: request body, reason note, approval note, token, billing
  key, provider customer key, and raw secret values.
- Failure handling: audit exception is suppressed on the original
  `BusinessException`; warning logs contain phase, numeric target ID, stable
  error enum, and exception class only.

## Commands and Outputs

- Initial focused implementation check:

```powershell
.\gradlew.bat test `
  --tests "com.atstudio.atstudio.service.AdminSubscriptionCorrectionServiceTest" `
  --tests "com.atstudio.atstudio.service.AdminOperationAuditTransactionIntegrationTest"
```

  - Result: `BUILD SUCCESSFUL` in 50 seconds; 48 tests passed.

- Final focused verification:

```powershell
.\gradlew.bat test `
  --tests "com.atstudio.atstudio.service.AdminSubscriptionCorrectionServiceTest" `
  --tests "com.atstudio.atstudio.service.AdminOperationAuditTransactionIntegrationTest" `
  --tests "com.atstudio.atstudio.controller.AdminUserSubscriptionCorrectionControllerTest" `
  --tests "com.atstudio.atstudio.repository.AdminSubscriptionCorrectionRepositoryContractTest"
```

  - Result: `BUILD SUCCESSFUL` in 48 seconds.
  - `AdminSubscriptionCorrectionServiceTest`: 37 passed.
  - `AdminOperationAuditTransactionIntegrationTest`: 11 passed.
  - `AdminUserSubscriptionCorrectionControllerTest`: 10 passed.
  - `AdminSubscriptionCorrectionRepositoryContractTest`: 1 passed.
  - Total: 59 passed, 0 failed, 0 errors, 0 skipped.

- Scoped whitespace check:

```powershell
$tracked = @(
  'docs/SR/SR-96.md',
  'docs/SR/SR-97.md',
  'docs/design/api-spec.md',
  'docs/design/usecase/user-subscription.md'
)
$untracked = @(
  'src/main/java/com/atstudio/atstudio/entity/enums/AdminOperationAuditAction.java',
  'src/main/java/com/atstudio/atstudio/entity/enums/AdminOperationAuditTargetType.java',
  'src/main/java/com/atstudio/atstudio/service/AdminOperationAuditState.java',
  'src/main/java/com/atstudio/atstudio/service/AdminOperationRejectionAuditService.java',
  'src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java',
  'src/test/java/com/atstudio/atstudio/service/AdminOperationAuditTransactionIntegrationTest.java',
  'src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java',
  'deliverables/user/WI-20260809-ATS-008-summary.md',
  'deliverables/agent/WI-20260809-ATS-008-evidence-pack.md'
)
git diff --check -- $tracked
foreach ($file in $untracked) {
  git diff --no-index --check -- /dev/null $file
}
```

  - Result: pass; no whitespace errors in all 13 WI-008 files. Exit code `1`
    from each `--no-index` call means the compared file differs from
    `/dev/null`; only output or an exit code greater than `1` is treated as a
    check failure.

## Tests Not Run

- No full suite was run, per the handoff.
- `AdminSubscriptionCorrectionMysqlConcurrencyIntegrationTest` was not run.
  It is opt-in, requires a disposable external MySQL database, and performs
  destructive fixture cleanup. This WI used runnable lock annotation and
  invocation-order contract coverage instead.
- No external provider, payment, refund, billing-key deletion, or email call
  was made.

## Risks / Rollback

- Risks:
  - Live InnoDB scheduling for correction versus actor demotion/withdrawal is a
    residual runtime risk until the opt-in disposable-MySQL race is executed.
  - New enum values fit existing string columns, but retained-environment
    migration is outside this fresh-only V1 scope and was not performed.
- Rollback:
  - Apply an inverse patch to only the actor lock, request/approval rejection
    actions/target/state methods, focused tests, and four current-state document
    additions listed above.
  - Do not delete or restore whole untracked files; they contain earlier
    approved WI-014/WI-015 work that must remain intact.

## WI-20260808-ATS-028 Status

- **Remains blocked.** WI-008 repairs reviewer BLOCKER-001 and MAJOR-002.
- Remaining explicit blockers are MAJOR-001 preview evidence, MAJOR-003
  free-text sensitive-data control, and MINOR-001 index/access-path repair or
  accepted MySQL evidence. Those items were forbidden or out of scope here.
