# Evidence Pack: WI-20260823-ATS-010

## Scope / DoD

- [x] Removed the auth-to-player queue-clear coupling.
- [x] Preserved token and user-specific like-state cleanup.
- [x] Preserved explicit `clearQueue()` persistence behavior.
- [x] Added direct auth-expiry, logout, paused-progress, and explicit-clear
  regression coverage.
- [x] Focused Vitest and static frontend quality commands passed.
- [ ] Final real-browser refresh regression remains outstanding.

## Root Cause Evidence

- `frontend/src/store/authStore.ts:172-182`: `clearSession()` had coupled
  auth cleanup to `usePlayerStore.getState().clearQueue()`.
- `frontend/src/store/playerStore.ts:761-775`: explicit `clearQueue()` stops
  playback and persists an empty queue, explaining the erased hydration input.
- `frontend/src/store/playerStore.ts:567-582`: paused playback now records a
  clamped media position in the persisted player state.
- `frontend/src/store/playerStore.ts:498-525`: persisted PlayerBar hydration
  uses the bounded public batch endpoint and restores the current track and
  time.

## Changed Files

- `frontend/src/store/authStore.ts`
- `frontend/src/store/playerStore.ts`
- `frontend/src/store/authStore.test.ts`
- `frontend/src/store/playerStore.test.ts`
- `deliverables/user/WI-20260823-ATS-010-summary.md`
- `deliverables/agent/WI-20260823-ATS-010-evidence-pack.md`

## Commands And Results

| Command | Result |
| --- | --- |
| `npm test -- src/store/authStore.test.ts src/store/playerPersistence.test.ts src/store/playerStore.test.ts src/api/client.test.ts` | PASS: 4 files, 100 tests, 0 failures. |
| `npm run typecheck` | PASS: `tsc --noEmit` exited 0. |
| `npm run lint` | PASS: ESLint exited 0 with zero warnings allowed. |
| `npm run format` | PASS: Prettier reported all matched files formatted. |
| `npm run build -- --outDir "$env:TEMP\\atstudio-wi-20260823-ats-010-build"` | PASS: `tsc -b` and Vite production build exited 0. |
| `git diff --check -- frontend/src/store/authStore.ts frontend/src/store/playerStore.ts frontend/src/store/authStore.test.ts frontend/src/store/playerStore.test.ts` | PASS: no diagnostics. |
| Public batch probe for ID 4 | PASS: HTTP 200, one playable record, duration 7. |

## Browser Evidence And Blocker

The in-app browser on `http://127.0.0.1:5173/tracks/4` showed the populated
PlayerBar after public play. A prior refresh attempt showed the empty bar. The
server served the edited `authStore` module without `playerStore` or
`clearQueue` references, and the public batch request returned a valid record.
Storage values were deliberately not inspected, as required by the handoff.

The final paused-progress correction was not followed by a complete browser
reproduction before the user requested conclusion. This is the sole remaining
blocker to the browser acceptance criterion; automated code and quality gates
are green.

## Rollback

Revert this WI's four source/test files as one independent code change. The
two WI deliverables can be removed with the same rollback. No backend, DB,
external provider, local configuration, client-acceptance worktree, or running
process was changed.
