# WI-20260823-ATS-004 Summary

## Status

Completed on `codex/v1-release-rehearsal-fixes` without a commit.

## Remediations

- The Playlist Drawer now retains a manually selected Likes tab across a generic close and reopen.
- PlayerBar sends an explicit, versioned tab request for both Likes and Playlists, including when the requested tab value matches an earlier request.
- The public authentication coverage test now expects the approved BUSINESS label, `Company name or industry`, rather than the former company-only wording.
- Prettier was applied only to the four scoped files reported by WI-002:
  `SignupPage.test.tsx`, `SocialCompleteProfilePage.test.tsx`, `TrackListPage.tsx`, and `ProfilePage.test.tsx`.

## Validation

| Command | Result |
| --- | --- |
| `npm run test -- src/components/player/playerComponents.test.tsx -t "prevents an earlier likes response from populating a reopened drawer"` | PASS: 1 passed, 28 skipped |
| `npm run test -- src/layouts/PlayerBar.test.tsx -t "opens the existing drawer at requested tabs"` | PASS: 1 passed, 29 skipped |
| `npm run test -- src/test/coverage/publicAuthShell.coverage.test.tsx` | PASS: 28 passed |
| `npm run test -- src/components/player/playerComponents.test.tsx src/layouts/PlayerBar.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx` | PASS: 3 files, 87 passed |
| Scoped `npx prettier --check` over the four WI-002 files | PASS: all matched files use Prettier code style |
| `git diff --check` over the WI-004 source/test scope | PASS: no output |

## Broad Suite

`npm run test` completed with `110 passed | 1 failed` test files and `1446 passed | 1 failed` tests. The sole failure is the explicitly excluded, pre-existing `src/pages/public/HomePage.test.tsx` text matcher for the existing HomePage copy. No HomePage file was modified in this WI.

## Scope Preserved

No client-acceptance worktree, HomePage file, database/data/storage, secret or ignored local configuration, provider/payment/mail integration, or unrelated source file was modified.
