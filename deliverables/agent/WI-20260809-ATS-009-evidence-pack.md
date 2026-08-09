# Evidence Pack: WI-20260809-ATS-009

## Summary

- Repaired ambiguous ADMIN subscription-correction outcomes, browser-date
  authority, persisted-text confirmation, and ADMIN user wire typing with
  focused frontend verification.

## Scope / DoD Check

- [x] Request rejection performs one bounded target open-state read.
- [x] Approval/execution rejection performs one bounded known-ID detail read.
- [x] Advanced server state restores correction/stage and announces sync.
- [x] Failed reconciliation preserves context, blocks duplicate mutation, and
      exposes one explicit status retry.
- [x] Browser time does not block server preview or set date bounds.
- [x] ADMIN list/detail/assignable-role types match backend DTOs; `GUEST` is
      excluded.
- [x] Normalized reason/approval/execution text is visibly confirmed.
- [x] Focused tests, typecheck, lint, and changed-file formatting pass.

## Reference Documents

| Tier    | Document                                            | Reason                                       |
| ------- | --------------------------------------------------- | -------------------------------------------- |
| 0       | `docs/standards/core-principles.md`                 | Constitution and language boundary           |
| 0       | `docs/standards/development-standards.md`           | Frontend implementation and test standards   |
| 0       | `docs/standards/documentation-standards.md`         | Current-state document format                |
| 0       | `docs/standards/glossary.md`                        | Canonical local Subscription Correction term |
| 1       | `docs/policies/security-policy.md`                  | ADMIN and sensitive-output boundary          |
| 1       | `docs/policies/access-control-policy.md`            | Least-privilege boundary                     |
| Context | `deliverables/agent/WI-20260809-ATS-009-handoff.md` | Authorized scope and DoD                     |
| Context | `deliverables/user/WI-20260809-ATS-005-summary.md`  | Reviewer findings                            |
| Context | `deliverables/user/WI-20260809-ATS-008-summary.md`  | Completed backend repair and residual status |
| Context | `docs/SR/SR-96.md`, `docs/SR/SR-97.md`              | Current role/correction requirements         |

## Evidence Pointers

- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx`
  - `readMutationOutcome`: request open-state versus known-ID detail reads.
  - `reconcileMutation` / `markOutcomeUnknown`: one-shot reconciliation,
    unknown lock, and retry state.
  - `handlePreview`: controlled reason normalization without browser-date
    authority.
  - approval/execution confirmation messages: exact quoted outgoing notes.
- `frontend/src/api/admin.ts`
  - Exact `AdminUserListItem`, `AdminUserDetail`, and `AdminAssignableRole`
    contracts.
- `frontend/src/pages/admin/UserManagePage.tsx`
  - ADMIN list DTO state and assignable-role selector.
- `frontend/src/pages/admin/LicenseManagePage.tsx`
  - Direct list-DTO consumer corrected after typecheck evidence.
- `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx`
  - Lost request/approve/execute responses, failed reconciliation and retry,
    browser/server date disagreement, and exact note/payload assertions.
- `frontend/src/api/adminContracts.test.ts`
  - Realistic list/detail fixtures and compile-time assignable-role assertion.
- `frontend/src/pages/admin/UserManagePage.test.tsx`
  - Realistic list rows and runtime absence of a `GUEST` role option.
- `docs/design/api-spec.md`, `docs/ui/atstudio-front-list.md`,
  `docs/ui/modal-list.md`, `docs/SR/SR-96.md`, `docs/SR/SR-97.md`
  - Directly affected current-state API/UI/SR contracts only.

## Commands And Outputs

1. Formatting write before verification:
   `npx prettier --write src/api/admin.ts src/api/adminContracts.test.ts src/pages/admin/UserManagePage.tsx src/pages/admin/UserManagePage.test.tsx src/pages/admin/UserSubscriptionCorrectionModal.tsx src/pages/admin/UserSubscriptionManagePage.module.css src/pages/admin/UserSubscriptionManagePage.test.tsx`
   - Completed on the scoped files.
2. Focused Vitest:
   `npm test -- src/api/adminContracts.test.ts src/pages/admin/UserManagePage.test.tsx src/pages/admin/UserSubscriptionManagePage.test.tsx`
   - Passed: 3 files, 30 tests.
3. First typecheck: `npm run typecheck`
   - Failed: `TS2345` at `LicenseManagePage.tsx`; exact ADMIN list rows were
     assigned to broad `User[]` state.
4. Direct repair: changed `LicenseManagePage` search and selected-user state to
   `AdminUserListItem`.
5. Final typecheck: `npm run typecheck`
   - Passed.
6. Lint: `npm run lint`
   - Passed with `--max-warnings 0`.
7. Scoped code and new-deliverable format check:
   `npx prettier --check <eight scoped frontend paths> ../deliverables/user/WI-20260809-ATS-009-summary.md ../deliverables/agent/WI-20260809-ATS-009-evidence-pack.md`
   - Passed.
8. Broader changed-file Markdown format check:
   - Failed for `docs/design/api-spec.md`, `docs/ui/atstudio-front-list.md`,
     `docs/ui/modal-list.md`, `docs/SR/SR-96.md`, and `docs/SR/SR-97.md`.
   - The files already contain co-located dirty prior-WI changes. Whole-file
     automatic formatting was not applied to avoid rewriting those changes.
9. Scoped whitespace check: `git diff --check -- <WI-009 tracked paths>`
   - Passed.

## Test Evidence

| Scenario                             | Evidence                                                       |
| ------------------------------------ | -------------------------------------------------------------- | ------ |
| Request response lost after commit   | Open-state read restores `REQUESTED`                           |
| Approval response lost after commit  | Detail read restores `APPROVED` and note                       |
| Execution response lost after commit | Detail read restores `SUCCEEDED` and refreshes list            |
| Reconciliation read fails            | Context retained; duplicate execute disabled; one status retry |
| Explicit retry                       | Detail read only; mutation count remains one                   |
| Browser/server date disagreement     | Past browser-relative date reaches preview; no `min`/`max`     |
| Exact persisted strings              | Trimmed reason/payload and quoted confirmation notes asserted  |
| ADMIN wire contract                  | Backend-shaped list/detail fixtures; role is `USER             | ADMIN` |

## Risks / Rollback

- Risks:
  - One-shot reconciliation is a point-in-time read and intentionally does not
    poll.
  - Mocked API tests do not replace deployed browser/network-loss validation.
  - Full suite, coverage, and build were intentionally not run.
  - Whole-file Prettier remains nonconforming in the five disclosed existing
    dirty current-state documents; WI-009 additions passed whitespace checks.
- External effects:
  - No backend/schema/data/provider/external-call/secret/ZIP/commit/push action.
- Rollback:
  - Apply an inverse patch only to the scoped files in Evidence Pointers.
  - Preserve all pre-existing dirty and untracked work.

## WI-20260808-ATS-028 Status

- WI-009 frontend findings are repaired, so this WI no longer blocks WI-028.
- WI-028 remains blocked overall by WI-008's separately recorded backend
  residual items, which were explicitly outside WI-009.
