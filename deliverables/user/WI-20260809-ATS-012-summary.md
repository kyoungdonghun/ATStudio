# WI-20260809-ATS-012 Summary

Completed on 2026-08-09 with focused backend verification.

This work item removes automatic operator free-text duplication from rejected
administrator role changes and rejected local subscription-correction
executions. Stable action, target, actor, error-code, and bounded-state evidence
is unchanged. The existing dirty worktree was preserved.

## Delivered Behavior

### Role change and withdrawal rejection audits

- All role-change rejection branches use one helper that passes a null
  `reasonNote` to the independent rejection-audit service.
- Self-demotion, last-ADMIN removal, stale actor authority, and missing required
  reason continue to return their original stable `BusinessException` codes.
- Last-ADMIN withdrawal rejection already had no operator-note input and
  continues to persist a null `reasonNote` with stable withdrawal evidence.
- Successful role changes still pass the normalized operator reason to the
  successful audit context.

### Local subscription correction rejection audits

- Request and approval rejection audits remain phase-specific and note-free.
- Execution rejection now passes a null `reasonNote` instead of copying the
  correction request reason into another durable audit row.
- The correction workflow row still retains its required request reason and its
  optional approval/execution notes.
- Successful correction audit still receives the correction and retains its
  approved operator reason.
- Audit-write failure remains suppressed on the original execution
  `BusinessException`; the original stable error remains the result.

### Adversarial regression coverage

- Focused tests use reserved `.test` email values and explicitly fake
  Bearer/JWT-shaped text as operator notes.
- Role-change rejection tests prove the audit mock receives null while the
  original self-demotion error is returned.
- Correction execution tests prove the workflow reason remains present, the
  rejection audit receives null, and the original invalid-state error survives
  even when rejection-audit persistence throws.
- Transaction integration tests prove role-change, ADMIN-withdrawal, and
  correction rejection rows persist null `reasonNote` while stable audit fields
  remain present. Success audit tests prove approved operator text is retained.

No free-text DLP, sanitization, schema change, or retention change was added.

## Changed Files

Production:

- `src/main/java/com/atstudio/atstudio/service/UserService.java`
- `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java`

Tests:

- `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/AdminOperationAuditTransactionIntegrationTest.java`

Directly affected current-state documentation:

- `docs/design/api-spec.md`
- `docs/design/db-schema.md`
- `docs/design/usecase/user-info.md`
- `docs/design/usecase/user-subscription.md`
- `docs/policies/security-policy.md`

Deliverables:

- `deliverables/user/WI-20260809-ATS-012-summary.md`
- `deliverables/agent/WI-20260809-ATS-012-evidence-pack.md`

Several listed files already contained predecessor-WI changes in the substantial
dirty worktree. This list identifies WI-012 touch points and does not attribute
unrelated existing hunks to this work item.

## Verification

Focused Gradle command:

```text
.\gradlew.bat test --tests "com.atstudio.atstudio.service.UserServiceTest" --tests "com.atstudio.atstudio.service.AdminSubscriptionCorrectionServiceTest" --tests "com.atstudio.atstudio.service.AdminOperationAuditTransactionIntegrationTest"
```

Result: PASS, `BUILD SUCCESSFUL` in 45 seconds.

| Suite | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| `UserServiceTest` | 41 | 0 | 0 | 0 |
| `AdminSubscriptionCorrectionServiceTest` | 37 | 0 | 0 | 0 |
| `AdminOperationAuditTransactionIntegrationTest` | 11 | 0 | 0 | 0 |
| **Total** | **89** | **0** | **0** | **0** |

`git diff --check`: PASS with exit code 0 and no whitespace errors. Git emitted
working-copy CRLF-to-LF normalization warnings; no file was reformatted to
resolve unrelated line-ending state.

No full test suite, full build, MySQL concurrency proof, documentation-wide
validation, or external acceptance run was performed because the requested
verification was focused.

## Constraints Observed

- Current branch only; no branch switch.
- No schema or retained-data operation.
- No external call, secret access, or ZIP access.
- No dependency change, commit, push, staging, or unrelated cleanup.
- Existing dirty changes were not reverted or overwritten.

## Risks And Rollback

- Focused H2/JPA transaction coverage proves persistence semantics but is not a
  retained MySQL or full-suite result.
- The policy intentionally permits approved operator text in workflow and
  successful audit contexts; free-text DLP remains out of scope.
- Rollback must inverse only the WI-012 hunks in the listed product, test, and
  documentation files and remove the two WI-012 deliverables. Whole-file
  replacement is unsafe because the files contain pre-existing dirty changes.
- No database, schema, dependency, external-state, or Git-history rollback is
  required.

## WI-028 Status

WI-012 resolves and regression-tests `WI-20260808-ATS-028` MAJOR-001, the raw
operator-text duplication in rejection audits. It does not address MAJOR-002,
the frontend mutation-outcome reconciliation finding. WI-028 therefore remains
pending its other repair and an independent reviewer rerun; this Evidence Pack
does not mark WI-028 complete.
