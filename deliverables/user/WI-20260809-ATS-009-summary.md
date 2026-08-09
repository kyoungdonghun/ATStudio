# WI-20260809-ATS-009 Frontend Resilience Repair Summary

## Outcome

WI-20260809-ATS-009 is implemented and focused verification is complete. The
ADMIN subscription-correction UI now reconciles rejected mutation promises
against server state, blocks duplicate mutation while the outcome is unknown,
and keeps one explicit status-retry path. Browser time no longer prevents an
authoritative server preview. ADMIN user list/detail/role wire types now match
the backend DTOs.

## Implemented Repairs

- Added one bounded reconciliation read after each non-cancelled correction
  mutation rejection:
  - request rejection reads the target UserSubscription open state;
  - approval and execution rejection read the known correction detail.
- Restored the persisted correction, workflow stage, draft, and server notes
  when the follow-up read shows an advanced state. A synchronized-status message
  identifies the correction ID and current stage.
- When the follow-up read also fails, retained the draft, reason, approval or
  execution note, and known correction ID; disabled duplicate mutation; and
  exposed exactly one `status retry` action. Each retry performs one read only.
- Removed browser-clock status/date comparisons and date input `min`/`max`
  bounds. Required and valid `YYYY-MM-DD` checks remain local; server preview is
  authoritative for Seoul business-date rules.
- Trimmed the reason at the preview boundary and approval/execution notes at the
  confirmation boundary. The exact outgoing strings are displayed using a
  quoted representation before their corresponding mutation.
- Introduced `AdminUserListItem`, `AdminUserDetail`, `AdminAssignableRole`,
  `AdminUserType`, and `AdminUserJob`. `GUEST` is not assignable. Dashboard,
  user management, and license user search now consume the exact list DTO.
- Added focused lost-response, reconciliation failure/retry, date disagreement,
  normalized payload/note, realistic list DTO, and role-boundary tests.

## State Reconciliation Behavior

| Rejected mutation | Automatic read                             | Advanced state                                             | Read failure                                                     |
| ----------------- | ------------------------------------------ | ---------------------------------------------------------- | ---------------------------------------------------------------- |
| Request           | `GET .../open?userSubscriptionId=...` once | Restore correction and stage                               | Keep draft; mark unknown; block duplicate request                |
| Approve           | `GET .../{correctionId}` once              | Restore approval or later stage                            | Keep correction ID/note; mark unknown; block duplicate approval  |
| Execute           | `GET .../{correctionId}` once              | Restore processing/terminal state; refresh list on success | Keep correction ID/note; mark unknown; block duplicate execution |

The explicit status retry repeats only the corresponding read. It does not
repeat the mutation.

## Changed Files

- `frontend/src/api/admin.ts`
- `frontend/src/api/adminContracts.test.ts`
- `frontend/src/pages/admin/LicenseManagePage.tsx`
- `frontend/src/pages/admin/UserManagePage.tsx`
- `frontend/src/pages/admin/UserManagePage.test.tsx`
- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx`
- `frontend/src/pages/admin/UserSubscriptionManagePage.module.css`
- `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx`
- `docs/design/api-spec.md`
- `docs/ui/atstudio-front-list.md`
- `docs/ui/modal-list.md`
- `docs/SR/SR-96.md`
- `docs/SR/SR-97.md`
- `deliverables/user/WI-20260809-ATS-009-summary.md`
- `deliverables/agent/WI-20260809-ATS-009-evidence-pack.md`

## Verification

- Focused Vitest:
  `npm test -- src/api/adminContracts.test.ts src/pages/admin/UserManagePage.test.tsx src/pages/admin/UserSubscriptionManagePage.test.tsx`
  - Result: 3 files passed; 30 tests passed.
- First `npm run typecheck`:
  - Result: failed with `TS2345` in `LicenseManagePage.tsx` because it still
    stored exact list rows as broad `User` values.
  - Repair: changed only that direct consumer to `AdminUserListItem`.
- Final `npm run typecheck`: passed.
- `npm run lint`: passed with zero warnings.
- Scoped code and new-deliverable Prettier check: passed.
- A broader whole-file check that also included the five pre-existing dirty
  current-state documents reported those five documents as nonconforming:
  `docs/design/api-spec.md`, `docs/ui/atstudio-front-list.md`,
  `docs/ui/modal-list.md`, `docs/SR/SR-96.md`, and `docs/SR/SR-97.md`.
  Automatic whole-file rewriting was not applied because it would reformat
  co-located prior-WI changes. Scoped `git diff --check` passed.

No full coverage or build command was run, as required. The five-document
whole-file Prettier finding above remains disclosed. No backend, schema,
data, provider, external real call, secret, ZIP, commit, or push action was
performed.

## Risks And Rollback

- Reconciliation is intentionally point-in-time and bounded; it does not poll.
  A later server transition is recovered by the explicit status retry or by
  reopening the persisted workflow.
- Focused tests use mocked API boundaries. They prove client state behavior but
  are not browser/network-loss evidence from a deployed runtime.
- Rollback must use an inverse patch limited to the files listed above. Do not
  restore whole files or remove untracked files because the worktree contains
  prior approved WI changes.

## WI-20260808-ATS-028 Status

WI-009's two MAJOR and two MINOR frontend findings from WI-005 are repaired and
covered by focused tests, so WI-009 no longer blocks WI-028. WI-028 as a whole
remains blocked by the separate backend residual items explicitly recorded by
WI-008 (server-bound preview evidence, free-text sensitive-data controls, and
active-ADMIN lock-path/index or accepted MySQL evidence); those items were
outside WI-009 and were not modified.
