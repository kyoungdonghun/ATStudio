# Evidence Pack: WI-20260809-ATS-017

## Summary

- Repaired one stale coverage assertion so the test protects the current
  ambiguous request outcome and duplicate-mutation fence contract.

## Scope / DoD Check

- [x] Generic no-response request failure is asserted as ambiguous.
- [x] Unknown-outcome warning and exactly one status-retry control are visible.
- [x] Duplicate request creation remains disabled and is not replayed.
- [x] The current subscription row remains visible without a list refresh.
- [x] One bounded reconciliation read follows the failed mutation.
- [x] Dedicated 4xx, 5xx/network, repeated-204, and recovery tests were reviewed
      for semantic consistency and left unchanged.
- [x] Focused Vitest, typecheck, lint, scoped Prettier, and scoped whitespace
      checks pass.
- [x] Product code and unrelated dirty work were not modified.

## Reference Documents

| Tier    | Document                                                  | Reason                                               |
| ------- | --------------------------------------------------------- | ---------------------------------------------------- |
| 0       | `docs/standards/core-principles.md`                       | Approval, language, scope, and transparency baseline |
| 0       | `docs/standards/development-standards.md`                 | Frontend test and evidence requirements              |
| 1       | `docs/policies/quality-gates.md`                          | Regression and traceability gates                    |
| Context | `deliverables/user/REQ-20260808-ATS-004.md`               | Approved frontend quality baseline                   |
| Context | `deliverables/user/WI-20260809-ATS-013-summary.md`        | Current ambiguous mutation policy                    |
| Context | `deliverables/agent/WI-20260809-ATS-013-evidence-pack.md` | Classification and reconciliation evidence           |
| Context | `deliverables/user/WI-20260808-ATS-028-summary.md`        | Independent resolution status for MAJOR-002          |
| Handoff | `deliverables/agent/WI-20260809-ATS-017-handoff.md`       | Authoritative scope, DoD, and output contract        |

## Evidence Pointers

### Changed Test Scenario

- `frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx:663-729`
  - Renames the scenario around ambiguous request outcomes.
  - Clears only prior open-state mock call history between the two existing
    modal flows so the second flow has an explicit request boundary.
  - Protects the warning, one retry control, disabled request action, one
    mutation call, two open-state reads, one list read, and mounted current row.

### Product Contract Reviewed Without Modification

- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:200-212`
  - Generic no-response failures classify as ambiguous; HTTP 4xx is definite.
- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:419-520`
  - Request-stage reconciliation reads open state once and retains an unknown
    fence when the result is null or unavailable.
- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:748-759`
  - Exactly one read-only `상태 다시 확인` action is rendered.
- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:1067-1090`
  - Preview and request actions remain disabled while the unknown fence exists.

### Adjacent Dedicated Tests Reviewed Without Modification

- `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:638-670`
  - Definite HTTP 422 response: stable error, no reconciliation, request enabled.
- `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:672-746`
  - No-response network/timeout and HTTP 503 outcomes reconcile once.
- `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:748-789`
  - Initial and repeated null/204 reads retain one retry and one mutation.

## Old And New Assertions

Retired stale assertion:

```text
보정 요청을 생성하지 못했습니다. 구독 목록은 그대로 유지됩니다.
```

Current assertion:

```text
요청 생성 응답과 서버 상태를 모두 확인하지 못해 결과를 알 수 없습니다. 중복 요청 생성을 차단했습니다.
```

Additional call and state evidence:

- `createAdminSubscriptionCorrection`: exactly 1 call, including after an
  attempted click on the disabled request button.
- `fetchOpenAdminSubscriptionCorrection`: exactly 2 calls after the scenario
  boundary reset: initial open lookup plus one reconciliation call.
- Reconciliation call 2 targets subscription `71` with an `AbortSignal`.
- `fetchAdminUserSubscriptions`: exactly 1 call; no mutation-result list refresh.
- Current row element remains in the document.

## Commands And Outputs

1. RED focused Vitest:
   `npm test -- src/test/coverage/adminSubscriberGaps.coverage.test.tsx`
   - Result: 1 file failed; 23 passed, 1 failed.
   - Failure: line 712 could not find the retired definite-failure message.
   - Actual DOM contained the unknown warning, one retry action, and disabled
     request action.
2. GREEN focused Vitest:
   `npm test -- src/test/coverage/adminSubscriberGaps.coverage.test.tsx`
   - Result: 1 file passed; 24 tests passed.
3. TypeScript: `npm run typecheck`
   - Result: passed; `tsc --noEmit` reported no errors.
4. ESLint: `npm run lint`
   - Result: passed with `--max-warnings 0`.
5. Scoped Prettier:
   `npx prettier --check src/test/coverage/adminSubscriberGaps.coverage.test.tsx ../deliverables/user/WI-20260809-ATS-017-summary.md ../deliverables/agent/WI-20260809-ATS-017-evidence-pack.md`
   - Initial check found formatting differences only in the newly created
     Evidence Pack.
   - `npx prettier --write ../deliverables/agent/WI-20260809-ATS-017-evidence-pack.md`
     formatted only that new deliverable.
   - Final check passed for all three scoped files.
6. Scoped whitespace:
   `git diff --check -- frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx`
   - Result: passed.

## Constraints And External Effects

- No product implementation, dependency, schema, data, secret, ZIP, external
  call, branch, commit, or push action.
- Existing tracked and untracked dirty work was preserved.
- No full coverage suite or build was run; neither was requested by this narrow
  handoff.

## Risks / Rollback

- Risk: mocked API boundaries do not prove deployed transport-loss behavior.
- Risk: full coverage remains an independent final gate even though its known
  stale assertion blocker is repaired.
- Rollback: inverse only the WI-017 additions in
  `adminSubscriberGaps.coverage.test.tsx:663-729` and remove the two WI-017
  deliverables. Do not restore or replace the whole dirty test file.

## Final Frontend Gate Status

- The focused coverage file, typecheck, lint, and scoped format checks pass.
- The known WI-017 blocker is removed, so the final frontend coverage gate is
  unblocked for rerun.
