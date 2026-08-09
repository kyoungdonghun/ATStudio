# WI-20260808-ATS-028 Second Final Re-review Summary

## Decision

- **Result:** `PASS`
- **Findings:** `0 BLOCKER / 0 MAJOR / 0 MINOR`
- **WI-030:** **UNBLOCKED**

The two remaining WI-028 findings are repaired in WI-20260809-ATS-012 and
WI-20260809-ATS-013. Current source, focused tests, and predecessor evidence
support final approval.

## Re-reviewed Findings

### MAJOR-001 - Rejection-audit operator text

**Resolved.**

- Rejected role changes pass a null `reasonNote` to the independent rejection
  audit.
- Rejected correction execution passes a null `reasonNote`; request,
  approval, and ADMIN-withdrawal rejection audits remain note-free.
- Stable action, target, actor, outcome, error code, and bounded before/after
  state remain present.
- The normalized role-change reason remains in successful role-change audit
  context.
- The correction request reason and approval/execution notes remain on the
  authoritative workflow, and successful correction audit retains the approved
  request reason.
- Adversarial operator-text tests confirm rejection rows persist a null note
  while workflow and success contexts retain approved text.

### MAJOR-002 - Mutation outcome classification and request fence

**Resolved.**

- Definite HTTP 4xx mutation responses preserve the stable server error and
  bypass reconciliation.
- No-response, network, timeout, and HTTP 5xx failures use one bounded
  reconciliation read.
- An ambiguous request followed by a null/204 open-state read remains unknown,
  retains its draft and preview, blocks duplicate mutation, and exposes one
  read-only status retry.
- A repeated null/204 retry keeps the same unknown fence and does not replay the
  request mutation.
- Approval and execution retain their known correction ID and recover current
  or terminal state through the detail endpoint.

## Regression Check

Existing WI-008/WI-009 evidence remains applicable and no regression was found:

- Request, approval, and execution retain the shared pessimistic actor-row lock
  and active-ADMIN recheck at the mutation boundary.
- Rejection audits retain independent `REQUIRES_NEW` persistence and preserve
  the original business error if audit persistence fails.
- Backend validation remains authoritative on the injected business
  `Clock`; frontend date checks remain format/calendar-validity only.
- ADMIN list, detail, and assignable-role types remain exact, with `GUEST`
  excluded from assignable roles.

## Focused Verification

Backend:

```text
.\gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.service.UserServiceTest" --tests "com.atstudio.atstudio.service.AdminSubscriptionCorrectionServiceTest" --tests "com.atstudio.atstudio.service.AdminOperationAuditTransactionIntegrationTest"
```

Result: `BUILD SUCCESSFUL`; 89 tests passed, 0 failed, 0 errors, 0 skipped.

Frontend:

```text
npm test -- src/pages/admin/UserSubscriptionManagePage.test.tsx
```

Result: 1 file passed; 19 tests passed.

The first backend invocation completed only as `UP-TO-DATE` and was not counted
as execution evidence; the command above forced the focused suites to rerun.
No full suite, full build, live MySQL timing proof, or external acceptance run
was performed.

## Accepted Residuals

The previously adjudicated residuals remain accepted and were not reopened:

- No server-bound preview receipt/token for V1.
- No V1 free-text DLP.
- No active-ADMIN composite index.
- No live cross-workflow MySQL timing or deployed network-loss proof.
- Point-in-time reconciliation without polling or a backend correlation
  protocol.

## Constraints And Rollback

- Only this Summary and the paired WI-028 Evidence Pack were updated.
- No product, test, schema, data, dependency, external-state, or Git-history
  change was made by this re-review.
- Rollback is limited to restoring the prior versions of these two WI-028
  review artifacts.
