# WI-20260809-ATS-013 Frontend Mutation Outcome Repair Summary

## Outcome

WI-20260809-ATS-013 is implemented and focused verification is complete. The
ADMIN Local Subscription Correction modal now distinguishes definite HTTP 4xx
rejections from ambiguous transport/server outcomes and retains the request
fence when an ambiguous request is followed by an inconclusive 204 open-state
read.

## Implemented Behavior

- HTTP 4xx mutation responses are definite. The modal keeps the stable response
  message, does not reconcile, and does not enter unknown-outcome state.
- Network, timeout, and other no-response failures are ambiguous. HTTP 5xx
  responses are also ambiguous. Each performs one bounded reconciliation read.
- Request reconciliation uses the non-terminal open-state endpoint. A null/204
  result remains unknown, keeps the draft and preview, blocks duplicate
  mutation, and retains one read-only status-retry action.
- A repeated null/204 status retry remains unknown and does not repeat the
  mutation.
- Approval and execution retain their known correction ID, reconcile through
  detail, and preserve the existing terminal-state recovery path.
- No polling or backend correlation mechanism was added.

## Classification Contract

| Failure evidence                     | Classification         | UI behavior                                                 |
| ------------------------------------ | ---------------------- | ----------------------------------------------------------- |
| Cancelled request                    | Cancelled              | Ignore superseded/cancelled work                            |
| HTTP 4xx response                    | Definite               | Preserve stable error; no reconciliation or unknown fence   |
| No response, network, or timeout     | Ambiguous              | Perform one open/detail read                                |
| HTTP 5xx response                    | Ambiguous              | Perform one open/detail read                                |
| Ambiguous request plus null/204 read | Inconclusive           | Keep unknown fence, draft, preview, and one read-only retry |
| Known-ID approval/execution detail   | Resolved when readable | Restore current or terminal correction state                |

## Changed Files

- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx`
- `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx`
- `docs/ui/atstudio-front-list.md`
- `docs/ui/modal-list.md`
- `docs/design/usecase/user-subscription.md`
- `deliverables/user/WI-20260809-ATS-013-summary.md`
- `deliverables/agent/WI-20260809-ATS-013-evidence-pack.md`

## Focused Verification

- RED run:
  `npm test -- src/pages/admin/UserSubscriptionManagePage.test.tsx`
  - Result: 1 file run; 17 tests passed and 2 new boundary tests failed as
    expected before implementation.
- Final focused Vitest:
  `npm test -- src/pages/admin/UserSubscriptionManagePage.test.tsx`
  - Result: 1 file passed; 19 tests passed.
- `npm run typecheck`: passed with zero TypeScript errors.
- `npm run lint`: passed with zero ESLint errors or warnings.
- Scoped frontend Prettier check for the modal and focused test: passed.
- Current-state document Prettier check: `user-subscription.md` passed; the two
  previously disclosed dirty UI inventory files still report whole-file format
  differences and were not rewritten over co-located changes.
- Scoped `git diff --check`: passed.
- Component audit found no new Hook dependency, `any`, `@ts-ignore`, inline
  style, timer, subscription, or global-listener issue. The affected screen
  remains under the `/admin` `adminOnly` route boundary.

No full suite, coverage, build, backend test, external call, schema/data action,
secret or ZIP access, commit, or push was performed.

## Risks And Rollback

- Reconciliation remains intentionally point-in-time. Without polling or a
  backend correlation protocol, repeated request 204 responses remain unknown
  and require the read-only operator retry path.
- Focused tests mock the API boundary and do not replace deployed browser
  network-loss verification.
- Rollback must inverse only the WI-013 hunks in the listed files and remove the
  two WI-013 deliverables. Whole-file replacement is unsafe because the current
  worktree contains pre-existing dirty changes.

## WI-20260808-ATS-028 Status

WI-013 resolves and regression-tests MAJOR-002 from the WI-028 final re-review.
WI-012 separately reports MAJOR-001 repaired. WI-028 is therefore ready for an
independent reviewer rerun and final disposition; this Evidence Pack does not
mark WI-028 complete.
