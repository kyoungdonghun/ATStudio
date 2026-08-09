# WI-20260809-ATS-012 Evidence Pack

## Identity

| Field | Value |
|---|---|
| Work item | `WI-20260809-ATS-012` |
| REQ | `REQ-20260808-ATS-004` |
| Role | `se` |
| Date | 2026-08-09 |
| Status | Completed with focused verification |
| Authoritative input | `deliverables/agent/WI-20260809-ATS-012-handoff.md` |
| Reviewer input | `deliverables/user/WI-20260808-ATS-028-summary.md` |
| Depends on | `WI-20260808-ATS-028` final re-review |
| Blocks | `WI-20260808-ATS-028` MAJOR-001 rerun disposition |
| Branch policy | Current branch only; no commit or push |

## Outcome

Rejected administrator role changes and local subscription-correction
executions no longer copy operator free text into `admin_operation_audit_logs`.
All rejection paths retain stable action, target, actor, error code, and bounded
state while persisting null `reasonNote`. Approved workflow rows and successful
audit contexts retain their operator text. The three requested focused suites
passed 89 tests with zero failures, errors, or skips.

## Scope / DoD Check

- [x] Role-change rejection audit calls pass null `reasonNote`.
- [x] ADMIN-withdrawal rejection persists null `reasonNote` and stable state.
- [x] Correction request/approval rejection remains note-free.
- [x] Correction execution rejection passes and persists null `reasonNote`.
- [x] Stable action, target, actor, error code, and bounded state remain present.
- [x] Workflow correction reason/approval/execution notes remain unchanged.
- [x] Successful role-change and correction audit text remains unchanged.
- [x] Adversarial email/token-like test notes do not reach rejection rows.
- [x] Original correction `BusinessException` survives audit-write failure.
- [x] Focused service/audit transaction tests and `git diff --check` pass.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, language, traceability, and sensitive-data baseline |
| 0 | `docs/standards/development-standards.md` | Java service, transaction, exception, and focused-test baseline |
| 0 | `docs/standards/documentation-standards.md` | Current-state documentation and evidence structure |
| 0 | `docs/standards/glossary.md` | Canonical Local Subscription Correction term |
| 1 | `docs/policies/security-policy.md` | Sensitive free-text and rejection-audit minimization boundary |
| Context | `deliverables/user/WI-20260808-ATS-028-summary.md` | MAJOR-001 evidence and required repair |
| Handoff | `deliverables/agent/WI-20260809-ATS-012-handoff.md` | Scope, acceptance criteria, constraints, and output contract |

## Implementation Evidence

| Requirement | Evidence |
|---|---|
| Role rejection receives null | `src/main/java/com/atstudio/atstudio/service/UserService.java:367-379` removes the note from the helper boundary and passes null to `recordRoleChangeRejected` before throwing the original error |
| Role success retains text | `src/main/java/com/atstudio/atstudio/service/UserService.java:340-351` still passes normalized `reasonNote` to `recordRoleChangeSuccess` |
| Withdrawal rejection remains note-free | `src/main/java/com/atstudio/atstudio/service/UserService.java:382-389` calls the note-free withdrawal rejection method with stable user state and `LAST_ADMIN_REQUIRED` |
| Correction execution receives null | `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:613-656` preserves the original exception and bounded state, then passes null to execution rejection audit |
| Workflow reason retained | `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:204-226` persists the normalized required request reason in the correction row |
| Execution note and success context retained | `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:294-312` stores the execution note and calls successful audit with the correction |
| Rejection persistence boundary | `src/main/java/com/atstudio/atstudio/service/AdminOperationRejectionAuditService.java:21-115` persists the supplied null or omitted note in independent `REQUIRES_NEW` transactions while preserving stable fields |

## Retained / Omitted Fields

| Audit context | Retained | `reasonNote` |
|---|---|---|
| Rejected role change | `USER_ROLE_CHANGE`, USER target, actor ID, target ID, `REJECTED`, equal bounded user state, stable business error | Null |
| Rejected last-ADMIN withdrawal | `ADMIN_WITHDRAWAL`, USER target, actor/target ID, `REJECTED`, equal bounded user state, `LAST_ADMIN_REQUIRED` | Null |
| Rejected correction request | Phase-specific action, actor when available, UserSubscription target, stable error, equal bounded state | Null |
| Rejected correction approval | Phase-specific action, actor when available, correction target, stable error, equal bounded state | Null |
| Rejected correction execution | `USER_SUBSCRIPTION_CORRECTION`, actor when available, UserSubscription target, stable error, equal bounded subscription/agreement state | Null |
| Successful role change | Before/after role state, actor/target, `ROLE_CHANGED` | Normalized approved role-change reason retained |
| Correction workflow and success audit | Request reason, optional approval/execution notes, actors, snapshots, status, success state | Approved workflow text retained |

No request-body copying, DLP, sanitization, schema, or retention-policy change
was introduced.

## Regression Evidence

| Test pointer | Evidence |
|---|---|
| `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java:52-53,780-800` | Fake `.test` email and fake Bearer/JWT-shaped role reason; self-demotion keeps its original business error and audit receives null |
| `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java:803-870` | Last-ADMIN and stale-actor role rejection calls also receive null |
| `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java:70-71,603-627` | Adversarial correction reason remains on the workflow row; stale execution retains `INVALID_STATE_TRANSITION` and audit receives null |
| `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java:756-785` | Audit persistence failure is suppressed, the original invalid-state error remains, workflow text remains, and the attempted audit argument is null |
| `src/test/java/com/atstudio/atstudio/service/AdminOperationAuditTransactionIntegrationTest.java:85-121` | Role rejection survives outer rollback and persists null note with stable evidence |
| `src/test/java/com/atstudio/atstudio/service/AdminOperationAuditTransactionIntegrationTest.java:135-155` | Last-ADMIN withdrawal rejection persists null note and stable state |
| `src/test/java/com/atstudio/atstudio/service/AdminOperationAuditTransactionIntegrationTest.java:160-220` | Successful correction retains adversarial workflow text; rejected execution persists null note and bounded state |
| `src/test/java/com/atstudio/atstudio/service/AdminOperationAuditTransactionIntegrationTest.java:225-271` | Request and approval rejection audits continue to persist null notes |

## Current-State Documentation

| Path | Update |
|---|---|
| `docs/design/api-spec.md:63-71` | All applicable rejection phases, stable fields, null note, and successful/workflow retention |
| `docs/design/db-schema.md:171-183` | `admin_operation_audit_logs.reason_note` rejection boundary versus approved successful contexts |
| `docs/design/usecase/user-info.md:265-282` | Role rejection evidence omits operator reason while success retains it |
| `docs/design/usecase/user-subscription.md:96-108` | Request/approval/execution rejection minimization and workflow-note retention |
| `docs/policies/security-policy.md:266-277` | Administrator rejection-audit minimization policy and explicit DLP non-scope |

No historical SR body, UI document, schema file, or unrelated current-state
document was changed for WI-012.

## Verification Ledger

### Focused Gradle tests

```text
.\gradlew.bat test --tests "com.atstudio.atstudio.service.UserServiceTest" --tests "com.atstudio.atstudio.service.AdminSubscriptionCorrectionServiceTest" --tests "com.atstudio.atstudio.service.AdminOperationAuditTransactionIntegrationTest"
```

- Result: PASS, `BUILD SUCCESSFUL` in 45 seconds.
- `UserServiceTest`: 41 tests, 0 failures, 0 errors, 0 skipped.
- `AdminSubscriptionCorrectionServiceTest`: 37 tests, 0 failures, 0 errors, 0 skipped.
- `AdminOperationAuditTransactionIntegrationTest`: 11 tests, 0 failures, 0 errors, 0 skipped.
- Total: 89 tests, 0 failures, 0 errors, 0 skipped.
- The transaction suite used the repository's embedded H2/JPA test context; no
  external database or provider was contacted.

### Whitespace check

```text
git diff --check
```

- Result: PASS, exit code 0, no whitespace errors.
- Git reported working-copy CRLF-to-LF normalization warnings. No unrelated
  line-ending rewrite was performed.

## Not Run

- Full Gradle test suite or full build.
- MySQL concurrency or retained-database verification.
- Documentation-wide validation.
- Browser, provider, email, or other external acceptance calls.

These were outside the requested focused scope and were not needed to establish
the rejection-audit data boundary.

## Constraints / Worktree Preservation

- Work remained on `codex/v1-release-rehearsal-fixes`.
- The repository began with a substantial dirty worktree, including existing
  changes in several touched files and untracked predecessor-WI files.
- All edits used scoped patches. No unrelated hunk was reverted, reformatted,
  staged, committed, or pushed.
- No schema/data operation, dependency change, external call, secret access, or
  ZIP access occurred.

## Risks

1. Verification is focused and does not substitute for the full backend suite.
2. Persistence semantics were exercised with embedded H2, not retained MySQL.
3. Approved workflow and successful-audit free text remains intentionally
   retained; free-text DLP is still outside this WI.
4. Co-located predecessor changes require hunk-scoped review and rollback.

## Rollback

Apply an inverse patch only to the WI-012 hunks in the two service files, three
test files, and five current-state documents listed above. Remove the WI-012
Summary and Evidence Pack if the work item is withdrawn. Do not replace whole
files or use destructive Git restoration because unrelated dirty changes share
these paths. No schema, database, dependency, external-state, commit, or push
rollback is required.

## WI-028 Status

WI-012 resolves `WI-20260808-ATS-028` MAJOR-001 and supplies focused regression
evidence for its independent rerun. MAJOR-002, the frontend mutation-outcome
reconciliation finding, is outside WI-012 and remains unresolved by this
Evidence Pack. WI-028 must not be marked complete until that repair and the
independent reviewer disposition are recorded.
