# Evidence Pack: WI-20260809-ATS-013

## Summary

- Repaired frontend mutation failure classification and retained the unknown
  request fence across initial and repeated null/204 reconciliation reads.

## Scope / DoD Check

- [x] HTTP 4xx responses preserve stable errors without reconciliation.
- [x] No-response, network, timeout, and HTTP 5xx failures reconcile once.
- [x] Request null/204 reconciliation remains unknown and duplicate-fenced.
- [x] Draft and preview state survive inconclusive reconciliation.
- [x] Exactly one read-only status-retry action remains visible.
- [x] A repeated null/204 retry remains unknown and does not repeat mutation.
- [x] Known-ID approval/execution detail recovery, including terminal state,
      remains successful.
- [x] Focused test, typecheck, lint, scoped Prettier, component audit, role
      boundary check, and whitespace checks were completed.
- [x] Only affected frontend/current-state docs and required deliverables were
      changed.

## Reference Documents

| Tier    | Document                                                  | Reason                                                      |
| ------- | --------------------------------------------------------- | ----------------------------------------------------------- |
| 0       | `docs/standards/core-principles.md`                       | Constitution, language, approval, and transparency boundary |
| 0       | `docs/standards/development-standards.md`                 | Frontend test and evidence expectations                     |
| 0       | `docs/standards/documentation-standards.md`               | English current-state and deliverable format                |
| 0       | `docs/standards/glossary.md`                              | Canonical Local Subscription Correction term                |
| Persona | `.claude/agents/qa-fe.md`                                 | Frontend quality gates and scoped role audit                |
| Context | `deliverables/agent/WI-20260809-ATS-013-handoff.md`       | Authoritative WI scope, constraints, and output contract    |
| Context | `deliverables/user/WI-20260808-ATS-028-summary.md`        | MAJOR-002 evidence and required repair                      |
| Context | `deliverables/agent/WI-20260808-ATS-028-evidence-pack.md` | Reviewer evidence pointers and residual boundary            |
| Context | `deliverables/user/WI-20260809-ATS-012-summary.md`        | Separate MAJOR-001 repair and WI-028 status                 |
| Skill   | `.agents/skills/react-best-practices/SKILL.md`            | React module-scope helper and rendering review              |

## Failure Classification

| Evidence                             | Result                   | Reconciliation                            |
| ------------------------------------ | ------------------------ | ----------------------------------------- |
| Cancelled/superseded request         | Cancelled                | None                                      |
| Response status 400-499              | Definite                 | None; stable response message is retained |
| No response, network, or timeout     | Ambiguous                | One bounded read                          |
| Response status 500 or greater       | Ambiguous                | One bounded read                          |
| Other non-cancelled rejected promise | Conservatively ambiguous | One bounded read                          |

## Evidence Pointers

### Product

- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:200-212`
  - Shared Axios-like failure classifier and stable definite-error extraction.
- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:419-438`
  - Request open-state versus known-ID detail reads.
- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:455-520`
  - Successful detail recovery, null/204 unknown retention, and shared mutation
    failure handling.
- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:579-654`
  - Request, approval, and execution use the shared classification path.
- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:748-759`
  - One explicit read-only status-retry action.
- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:1068-1090`
  - Preview/request actions remain disabled while unknown.

### Tests

- `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:638-670`
  - Definite 422 response preserves its stable message, performs no
    reconciliation read, and creates no unknown fence.
- `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:672-707`
  - ERR_NETWORK and ECONNABORTED no-response failures reconcile committed
    request state.
- `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:709-746`
  - HTTP 503 approval failure reconciles through known-ID detail.
- `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:748-789`
  - Initial and repeated null/204 reads keep one retry, draft/preview state, and
    one total request mutation.
- `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:791-824`
  - Existing known-ID terminal execution recovery remains successful.

### Current-State Documentation

- `docs/ui/atstudio-front-list.md:99-110`
- `docs/ui/modal-list.md:47-57`
- `docs/design/usecase/user-subscription.md:79-89`

## Commands And Outputs

1. Branch/worktree inspection: `git status --short --branch`
   - Current branch: `codex/v1-release-rehearsal-fixes`.
   - Existing tracked/untracked dirty work was preserved.
2. RED focused test:
   `npm test -- src/pages/admin/UserSubscriptionManagePage.test.tsx`
   - Result: 1 file failed; 17 passed and 2 new tests failed.
   - Confirmed the definite-4xx and request-null defects before implementation.
3. GREEN/final focused test:
   `npm test -- src/pages/admin/UserSubscriptionManagePage.test.tsx`
   - Result: 1 file passed; 19 tests passed.
4. TypeScript: `npm run typecheck`
   - Passed with zero errors.
5. ESLint: `npm run lint`
   - Passed with `--max-warnings 0`.
6. Scoped frontend formatting:
   `npx prettier --check src/pages/admin/UserSubscriptionCorrectionModal.tsx src/pages/admin/UserSubscriptionManagePage.test.tsx`
   - First check found the newly added parameterized-test wrapping.
   - `npx prettier --write src/pages/admin/UserSubscriptionManagePage.test.tsx`
     changed only the scoped test formatting.
   - Final check passed for both frontend files.
7. Current-state document formatting:
   `npx prettier --check ../docs/ui/atstudio-front-list.md ../docs/ui/modal-list.md ../docs/design/usecase/user-subscription.md`
   - `user-subscription.md` matched Prettier.
   - The two previously disclosed dirty UI inventory files still report
     whole-file formatting differences. They were not rewritten over prior WI
     changes.
8. Scoped whitespace check:
   `git diff --check -- frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx docs/ui/atstudio-front-list.md docs/ui/modal-list.md docs/design/usecase/user-subscription.md`
   - Passed.
9. Scoped component/role audit:
   - No new Hook, `any`, `@ts-ignore`, inline style, timer, subscription, or
     listener issue in the changed component.
   - `frontend/src/router/index.tsx:209-231` keeps the affected screen under the
     `/admin` `adminOnly` layout.

## Focused Test Matrix

| Scenario                                     | Expected evidence                                | Result |
| -------------------------------------------- | ------------------------------------------------ | ------ |
| HTTP 422 with stable message                 | No read, no unknown fence, message retained      | Pass   |
| ERR_NETWORK without response                 | One open read restores `REQUESTED`               | Pass   |
| ECONNABORTED timeout                         | One open read restores `REQUESTED`               | Pass   |
| HTTP 503 approval response                   | One detail read restores `APPROVED`              | Pass   |
| Ambiguous request plus initial null/204      | Draft/preview retained; mutation blocked         | Pass   |
| Explicit retry plus repeated null/204        | Unknown and one retry remain; no mutation replay | Pass   |
| Lost execution response plus terminal detail | `SUCCEEDED` restored and list refreshed          | Pass   |

## Constraints And External Effects

- No backend, schema, data, dependency, or API contract change.
- No real external/provider call, secret access, or ZIP access.
- No polling or backend correlation protocol.
- No full suite, coverage, build, commit, or push.

## Risks / Rollback

- Risk: mocked frontend API boundaries do not prove deployed transport-loss
  behavior.
- Risk: without correlation or polling, repeated request 204 reads intentionally
  remain unknown and operator-visible.
- Formatting residual: two co-located dirty UI inventory documents retain their
  previously disclosed whole-file Prettier differences; scoped whitespace is
  clean.
- Rollback: inverse only the WI-013 hunks in the Evidence Pointers and remove
  the two WI-013 deliverables. Preserve all unrelated dirty and untracked work.

## WI-20260808-ATS-028 Status

- WI-013 resolves and focused-tests MAJOR-002.
- WI-012 separately reports MAJOR-001 resolved and focused-tested.
- WI-028 is ready for an independent reviewer rerun and final disposition. This
  Evidence Pack does not mark WI-028 complete.
