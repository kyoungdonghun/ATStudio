# Evidence Pack: WI-20260823-ATS-001

## Work Item

- WI: `WI-20260823-ATS-001`
- REQ: `REQ-20260823-ATS-001`
- Branch: `codex/v1-release-rehearsal-fixes`
- Baseline: `3ea2781`

## Summary

Implemented the approved client-feedback remediation within the assigned workspace.
The implementation reuses existing player and likes behavior, retains existing
`companyName` storage, and does not change out-of-scope persistence, provider, payment,
mail, or playlist-policy behavior.

## Definition of Done

| Requirement | Status | Evidence |
| --- | --- | --- |
| Multi-mood tags stay selectable and query values repeat | Complete | `frontend/src/pages/public/TrackListPage.tsx:240,710`; `frontend/src/pages/public/TrackListPage.test.tsx` |
| Responsive question FAB avoids PlayerBar | Complete | `frontend/src/pages/subscriber/QuestionListPage.tsx:268`; `frontend/src/pages/subscriber/QuestionListPage.module.css:223,272,278` |
| BUSINESS uses existing combined `companyName`; INDIVIDUAL keeps job | Complete | `frontend/src/pages/auth/SignupPage.tsx`; `frontend/src/pages/auth/SocialCompleteProfilePage.tsx`; `frontend/src/pages/subscriber/ProfilePage.tsx`; `frontend/src/pages/admin/UserManagePage.tsx` |
| Nickname internal spaces and edge trimming are consistent | Complete | `frontend/src/utils/validation.ts:6,84,89`; `frontend/src/api/auth.ts:103,128`; `src/main/java/com/atstudio/atstudio/service/UserService.java:77,121,229,272,413,420`; `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java:347,685,798` |
| Playlist Play all uses existing player playAll and keeps add-queue | Complete | `frontend/src/pages/subscriber/PlaylistDetailPage.tsx:241,299`; `frontend/src/pages/subscriber/PlaylistDetailPage.test.tsx` |
| Likes opens existing drawer next to history in desktop/mobile expanded player | Complete | `frontend/src/layouts/PlayerBar.tsx:573,628,649,869,1100,1132`; `frontend/src/components/player/PlaylistDrawer.tsx:56,62,67,70`; `frontend/src/layouts/PlayerBar.test.tsx` |
| Billing-key example aligns safely to keyring configuration | Complete | `application-local.example.yml:69,70` |

## Changed Paths

### Frontend implementation

- `frontend/src/api/auth.ts`
- `frontend/src/components/player/PlaylistDrawer.tsx`
- `frontend/src/layouts/PlayerBar.tsx`
- `frontend/src/pages/admin/UserManagePage.tsx`
- `frontend/src/pages/auth/SignupPage.tsx`
- `frontend/src/pages/auth/SocialCompleteProfilePage.tsx`
- `frontend/src/pages/public/TrackListPage.tsx`
- `frontend/src/pages/subscriber/PlaylistDetailPage.tsx`
- `frontend/src/pages/subscriber/ProfilePage.tsx`
- `frontend/src/pages/subscriber/QuestionListPage.tsx`
- `frontend/src/pages/subscriber/QuestionListPage.module.css`
- `frontend/src/utils/validation.ts`

### Backend implementation

- `src/main/java/com/atstudio/atstudio/common/validation/ValidationConstants.java`
- `src/main/java/com/atstudio/atstudio/dto/user/RegisterRequest.java`
- `src/main/java/com/atstudio/atstudio/dto/user/CompleteProfileRequest.java`
- `src/main/java/com/atstudio/atstudio/dto/user/UpdateProfileRequest.java`
- `src/main/java/com/atstudio/atstudio/entity/User.java`
- `src/main/java/com/atstudio/atstudio/service/UserService.java`
- `application-local.example.yml`

### Focused tests

- `frontend/src/utils/validationHelpers.test.ts`
- `frontend/src/pages/auth/SignupPage.test.tsx`
- `frontend/src/pages/auth/SocialCompleteProfilePage.test.tsx`
- `frontend/src/pages/subscriber/ProfilePage.test.tsx`
- `frontend/src/pages/public/TrackListPage.test.tsx`
- `frontend/src/pages/subscriber/QuestionListPage.test.tsx`
- `frontend/src/pages/subscriber/PlaylistDetailPage.test.tsx`
- `frontend/src/layouts/PlayerBar.test.tsx`
- `frontend/src/pages/admin/UserManagePage.test.tsx`
- `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java`

## Commands and Results

| Command | Result |
| --- | --- |
| `npm run test -- src/utils/validationHelpers.test.ts src/pages/auth/SignupPage.test.tsx src/pages/auth/SocialCompleteProfilePage.test.tsx src/pages/subscriber/ProfilePage.test.tsx src/pages/public/TrackListPage.test.tsx src/pages/subscriber/QuestionListPage.test.tsx src/pages/subscriber/PlaylistDetailPage.test.tsx src/layouts/PlayerBar.test.tsx src/pages/admin/UserManagePage.test.tsx` | Passed: 9 files, 131 tests. |
| `./gradlew.bat test --tests "com.atstudio.atstudio.service.UserServiceTest" --console=plain` | Passed: `BUILD SUCCESSFUL in 9s`. |
| `git diff --check` | Passed; no whitespace errors. |

## Deferred Checks

At the user's explicit request for an immediate focused-test wrap-up, the following were
not run in this WI and have no observed failure: `npm run typecheck`, `npm run lint`,
changed-file Prettier check, full frontend/backend suites, integration suites, and
manual browser visual checks.

## Scope and Safety Evidence

- No ignored `application-local.yml`, secret, database schema/data/storage, external
  provider, mail, payment/refund, repeat/default-playlist/plan policy file was edited.
- The existing client-acceptance worktree was not modified.
- Existing dirty `frontend/src/pages/public/HomePage.tsx` and
  `frontend/src/pages/public/HomePage.test.tsx`, plus pre-existing untracked
  deliverables/output/scripts, remain outside this WI and were not changed.

## Rollback

No commit was created. Roll back only the WI-owned paths listed above against baseline
`3ea2781`; do not use a repository-wide reset or restore because the worktree contains
unrelated dirty and untracked files. The user summary and this evidence pack are also
WI-owned paths.

## Follow-up WIs

- `WI-20260823-ATS-002` and `WI-20260823-ATS-003` remain downstream items blocked by
  this implementation per the handoff packet.
