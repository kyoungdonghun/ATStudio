# Evidence Pack: WI-20260823-ATS-004

## Work Item

- WI: `WI-20260823-ATS-004`
- REQ: `REQ-20260823-ATS-001`
- Agent: `se`
- Branch: `codex/v1-release-rehearsal-fixes`
- Depends on: `WI-20260823-ATS-002`
- Result: **Completed; the only broad-suite failure is the documented excluded HomePage test.**

## Scope and Guardrails

- Implemented only the three P2 remediations confirmed in WI-002:
  Drawer reopen tab retention, approved BUSINESS test wording, and scoped Prettier corrections.
- Did not modify `HomePage.tsx`, `HomePage.test.tsx`, the client-acceptance worktree, databases/data/storage, ignored local configuration, secrets, payments, providers, or mail.
- No commit was created.

## Implementation Evidence

### Drawer intent contract

- `frontend/src/components/player/PlaylistDrawer.tsx` replaces the reopen-driven `initialTab` reset with optional `requestedTab` and `tabRequestID` inputs.
- Local tab state remains unchanged when a caller merely toggles `open`, preserving a manually selected Likes tab on generic reopen.
- A new `tabRequestID` applies a requested tab exactly once per explicit parent request, including a repeat request for the same tab.
- `frontend/src/layouts/PlayerBar.tsx` increments that request ID on each Likes or Playlists action and supplies it to every Drawer rendering path.

### Regression coverage

- `frontend/src/components/player/playerComponents.test.tsx` asserts that the reopened generic Drawer still exposes Likes as the pressed tab before the current response resolves, while retaining the stale-response protection.
- `frontend/src/layouts/PlayerBar.test.tsx` verifies explicit desktop Likes, explicit desktop Playlists, repeat Likes close behavior, and the mobile expanded Likes action.
- `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx` uses only the approved `회사명 또는 업종` accessible label and validation message.

### Scoped formatting

Prettier write was restricted to these four WI-owned files identified by WI-002:

- `frontend/src/pages/auth/SignupPage.test.tsx`
- `frontend/src/pages/auth/SocialCompleteProfilePage.test.tsx`
- `frontend/src/pages/public/TrackListPage.tsx`
- `frontend/src/pages/subscriber/ProfilePage.test.tsx`

## Commands and Results

| Command | Result |
| --- | --- |
| `npx prettier --write src/pages/auth/SignupPage.test.tsx src/pages/auth/SocialCompleteProfilePage.test.tsx src/pages/public/TrackListPage.tsx src/pages/subscriber/ProfilePage.test.tsx` | Completed for exactly the four listed files. |
| `npm run test -- src/components/player/playerComponents.test.tsx -t "prevents an earlier likes response from populating a reopened drawer"` | PASS: 1 file passed; 1 passed, 28 skipped. |
| `npm run test -- src/layouts/PlayerBar.test.tsx -t "opens the existing drawer at requested tabs"` | PASS: 1 file passed; 1 passed, 29 skipped. |
| `npm run test -- src/test/coverage/publicAuthShell.coverage.test.tsx` | PASS: 1 file passed; 28 passed. |
| `npx prettier --check src/pages/auth/SignupPage.test.tsx src/pages/auth/SocialCompleteProfilePage.test.tsx src/pages/public/TrackListPage.tsx src/pages/subscriber/ProfilePage.test.tsx` | PASS: `All matched files use Prettier code style!` |
| `npm run test -- src/components/player/playerComponents.test.tsx src/layouts/PlayerBar.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx` | PASS: 3 files passed; 87 passed. |
| `npx prettier --check src/components/player/PlaylistDrawer.tsx src/components/player/playerComponents.test.tsx src/layouts/PlayerBar.tsx src/layouts/PlayerBar.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx` | PASS: `All matched files use Prettier code style!` |
| `git diff --check -- src/components/player/PlaylistDrawer.tsx src/components/player/playerComponents.test.tsx src/layouts/PlayerBar.tsx src/layouts/PlayerBar.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx src/pages/auth/SignupPage.test.tsx src/pages/auth/SocialCompleteProfilePage.test.tsx src/pages/public/TrackListPage.tsx src/pages/subscriber/ProfilePage.test.tsx` | PASS: completed with no output. |
| `npm run test` | BROAD SUITE: 110 passed / 1 failed files; 1446 passed / 1 failed tests. |

## Broad-Suite Exception

The only `npm run test` failure was the explicitly excluded existing test:

- `frontend/src/pages/public/HomePage.test.tsx`
- Test: `uses the creator audience copy in the hero and footer`
- Failure: its exact-text matcher cannot match the line-broken rendered hero subtitle `창작자를 위한 고품질 라이선스 음악.`

This WI did not modify either HomePage file, and WI-002 had already marked them outside the remediation scope. The WI-004 focused suites all passed.

## Rollback

Revert the WI-004 changes in `PlaylistDrawer.tsx`, `PlayerBar.tsx`, their focused tests, the public-auth coverage expectation, and the four scoped formatting-only files. No data, schema, external provider, or secret rollback is required.
