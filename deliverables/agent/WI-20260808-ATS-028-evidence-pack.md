# Evidence Pack: WI-20260808-ATS-028

## Summary

- Second final re-review passed with no findings; both remaining MAJOR findings
  are resolved and WI-030 is unblocked.

## Final Disposition

| Field | Result |
|---|---|
| Status | `PASS` |
| Findings | `0 BLOCKER / 0 MAJOR / 0 MINOR` |
| WI-030 | `UNBLOCKED` |
| Repair inputs | `WI-20260809-ATS-012`, `WI-20260809-ATS-013` |

## Scope / DoD Check

- [x] Re-reviewed only the two remaining WI-028 findings.
- [x] Confirmed rejection audits omit raw operator text.
- [x] Confirmed workflow and successful-audit notes remain.
- [x] Confirmed definite 4xx errors bypass reconciliation.
- [x] Confirmed no-response, timeout, network, and 5xx outcomes reconcile.
- [x] Confirmed initial and repeated request null/204 reads retain the unknown
      fence and one read-only retry.
- [x] Confirmed known-ID approval/execution terminal recovery remains.
- [x] Rechecked actor lock, rejection-audit durability, server business date,
      and exact ADMIN types using the existing WI-008/WI-009 evidence boundary.
- [x] Preserved the previously accepted residual-risk adjudications.
- [x] Re-ran only the repair-focused backend and frontend tests.

## Finding Revalidation

### MAJOR-001 - Resolved

| Evidence | Observation |
|---|---|
| `src/main/java/com/atstudio/atstudio/service/UserService.java:340-351` | Successful role change retains the normalized operator reason. |
| `src/main/java/com/atstudio/atstudio/service/UserService.java:367-379` | Every role-change rejection routed through the helper passes null `reasonNote`. |
| `src/main/java/com/atstudio/atstudio/service/UserService.java:382-388` | Last-ADMIN withdrawal rejection remains note-free. |
| `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:204-225` | The authoritative correction workflow retains its required request reason. |
| `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:294-312` | Execution note and successful correction audit context remain. |
| `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:613-656` | Execution rejection preserves the original error and passes null audit text. |
| `src/main/java/com/atstudio/atstudio/service/AdminOperationRejectionAuditService.java:21-115` | Rejection rows retain stable fields and independent `REQUIRES_NEW` persistence. |
| `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java:780-823` | Adversarial/self-demotion and last-ADMIN role rejections expect null audit text. |
| `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java:900-947` | Missing/blank role-change reasons retain stable errors and null rejection notes. |
| `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java:603-700` | Execution rejection cases keep workflow state and pass null rejection text. |
| `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java:756-785` | Audit-write failure does not mask the original business error; attempted note is null. |
| `src/test/java/com/atstudio/atstudio/service/AdminOperationAuditTransactionIntegrationTest.java:87-271` | Persisted role, withdrawal, request, approval, and execution rejection rows have null notes; successful correction retains approved text. |

Conclusion: raw operator text is no longer automatically duplicated into
rejection audits. Approved workflow and successful-audit text remains intact.

### MAJOR-002 - Resolved

| Evidence | Observation |
|---|---|
| `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:200-212` | Shared classifier treats HTTP 4xx as definite and all non-cancelled unresolved/server failures as ambiguous; definite errors retain the server message. |
| `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:419-438` | Request reconciliation uses open state; approval/execution use known-ID detail. |
| `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:455-520` | Detail results resolve state; null/204 and failed reads keep unknown state; definite failures bypass reconciliation. |
| `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:563-654` | Request, approval, and execution share the same failure-classification path and retain the known ID where available. |
| `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:679-688` | Unknown outcome locks the draft and disables request creation. |
| `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:747-759` | Exactly one read-only status-retry action is rendered. |
| `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:1065-1088` | Preview and request actions remain disabled while the outcome is unknown. |
| `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:638-670` | HTTP 422 preserves the stable message, performs no reconciliation, and creates no unknown fence. |
| `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:672-707` | Network and timeout failures reconcile committed request state. |
| `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:709-746` | HTTP 503 approval failure reconciles through known-ID detail. |
| `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:748-789` | Initial and repeated null/204 reads retain draft, preview, unknown fence, one retry, and one total mutation. |
| `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:791-869` | Known-ID execution recovery restores terminal state directly and through explicit retry. |

Conclusion: definite errors remain errors; ambiguous outcomes reconcile once;
inconclusive request reads remain duplicate-fenced; known-ID terminal recovery
is preserved.

## Existing-Control Regression Check

No new broad scan was performed. The current repair locations were checked
against the accepted WI-008/WI-009 evidence:

- Actor lock:
  `AdminSubscriptionCorrectionService.java:176-205`, `:239-255`,
  `:270-300`, and `:511-518` retain ordered domain/correction locks followed
  by the pessimistic actor-row lock and active-ADMIN recheck immediately before
  mutation.
- Rejection audit:
  the phase-specific request/approval paths and execution/role paths retain
  stable bounded fields, `REQUIRES_NEW` durability, and original-error
  preservation.
- Server date:
  `AdminSubscriptionCorrectionService.java:325-388` remains authoritative on
  `LocalDate.now(businessClock)`; frontend
  `UserSubscriptionCorrectionModal.tsx:107-120` performs format and calendar
  validity only.
- Exact ADMIN types:
  `frontend/src/api/admin.ts:27-45` retains exact list/detail and assignable
  role contracts; `GUEST` remains excluded.

## Focused Test Evidence

### Backend

```text
.\gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.service.UserServiceTest" --tests "com.atstudio.atstudio.service.AdminSubscriptionCorrectionServiceTest" --tests "com.atstudio.atstudio.service.AdminOperationAuditTransactionIntegrationTest"
```

- Result: `BUILD SUCCESSFUL` in 52 seconds.
- `UserServiceTest`: 41 tests, 0 failures, 0 errors, 0 skipped.
- `AdminSubscriptionCorrectionServiceTest`: 37 tests, 0 failures, 0 errors,
  0 skipped.
- `AdminOperationAuditTransactionIntegrationTest`: 11 tests, 0 failures,
  0 errors, 0 skipped.
- Total: 89 tests passed.
- A preceding invocation returned only `UP-TO-DATE`; it was not counted as
  current execution evidence.

### Frontend

```text
npm test -- src/pages/admin/UserSubscriptionManagePage.test.tsx
```

- Result: 1 test file passed; 19 tests passed.
- Duration: 6.14 seconds reported by Vitest.

### Not Run

- Full backend or frontend suite.
- Full build, coverage, or documentation-wide validation.
- Live MySQL concurrency/timing proof.
- Browser/deployed network-loss or external provider acceptance.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution, language, traceability, and transparency |
| 0 | `docs/standards/development-standards.md` | Review and focused-test evidence |
| 0 | `docs/standards/documentation-standards.md` | English deliverable and evidence structure |
| 0 | `docs/standards/glossary.md` | Canonical Local Subscription Correction term |
| Persona | `.claude/agents/cr.md` | Findings-first reviewer contract |
| Context | `deliverables/agent/WI-20260808-ATS-028-handoff.md` | Scope and output contract |
| Repair | `deliverables/user/WI-20260809-ATS-012-summary.md` | Rejection-audit repair claim |
| Repair | `deliverables/agent/WI-20260809-ATS-012-evidence-pack.md` | Rejection-audit repair evidence |
| Repair | `deliverables/user/WI-20260809-ATS-013-summary.md` | Reconciliation repair claim |
| Repair | `deliverables/agent/WI-20260809-ATS-013-evidence-pack.md` | Reconciliation repair evidence |
| Existing evidence | `deliverables/agent/WI-20260809-ATS-008-evidence-pack.md` | Actor lock and rejection-audit controls |
| Existing evidence | `deliverables/agent/WI-20260809-ATS-009-evidence-pack.md` | Server date and exact frontend contracts |

## Accepted Residual Risks

The following were already adjudicated and remain accepted:

- Server-bound preview receipt/token is not required for V1.
- V1 free-text DLP is not required; approved workflow/success text remains.
- The active-ADMIN composite index remains a performance residual.
- Live cross-workflow MySQL timing and deployed network-loss behavior remain
  residual verification risks.
- Point-in-time reconciliation without polling or backend correlation remains
  accepted; inconclusive request outcomes stay explicitly unknown and fenced.

## Constraints / External Effects

- Only the WI-028 Summary and this Evidence Pack were updated.
- No product, test, schema, data, dependency, secret, ZIP, external call,
  commit, push, or staging action was performed.
- The substantial pre-existing dirty worktree was preserved.

## Risks / Rollback

- Review evidence is scoped to the two repaired findings and their focused
  tests; it is not a full release qualification.
- Line numbers refer to the current dirty worktree and may move later.
- Rollback is limited to restoring the prior versions of the two WI-028 review
  artifacts.

## Downstream Status

- WI-028: **PASS**
- Findings: `0 BLOCKER / 0 MAJOR / 0 MINOR`
- WI-030: **UNBLOCKED**
