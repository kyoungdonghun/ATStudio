# WI-20260809-ATS-017 Frontend Coverage Assertion Repair Summary

## Outcome

WI-20260809-ATS-017 is complete. The stale full-coverage assertion now follows
the current ambiguous request outcome contract without changing product code.

## Verified Contract

- A generic rejected request with no HTTP response is treated as ambiguous.
- The modal shows the unknown-outcome warning and exactly one read-only
  `상태 다시 확인` action.
- The duplicate `요청 생성` action remains disabled, and an attempted click
  does not issue a second mutation.
- The current subscription row remains mounted and no list refresh is triggered.
- The second modal flow performs one initial open-state read and one bounded
  reconciliation read after the ambiguous mutation result.
- Adjacent dedicated HTTP 4xx, HTTP 5xx/network, repeated 204, and known-ID
  recovery tests remain unchanged and semantically consistent.

## Changed Files

- `frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx`
- `deliverables/user/WI-20260809-ATS-017-summary.md`
- `deliverables/agent/WI-20260809-ATS-017-evidence-pack.md`

No product implementation, dependency, schema, data, secret, ZIP, branch, or
Git-history change was made.

## Verification

- RED focused Vitest: 1 file failed as expected; 23 passed and the stale
  assertion was the only failure.
- GREEN focused Vitest: 1 file passed; 24 tests passed.
- `npm run typecheck`: passed with zero TypeScript errors.
- `npm run lint`: passed with zero ESLint errors or warnings.
- Scoped Prettier check for the coverage test and two WI-017 deliverables: passed.
- Scoped `git diff --check`: passed.

The known stale assertion no longer blocks the final frontend coverage gate.
The full coverage suite was not rerun in this narrow repair WI and remains the
next independent quality-gate action.

## Risks And Rollback

- The test observes mocked API boundaries and does not replace deployed
  transport-loss verification.
- Rollback must inverse only the WI-017 hunk in the affected coverage scenario
  and remove the two WI-017 deliverables. Whole-file replacement is unsafe
  because the worktree contains pre-existing dirty changes.
