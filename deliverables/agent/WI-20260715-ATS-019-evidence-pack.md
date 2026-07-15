# Evidence Pack: WI-20260715-ATS-019

## Summary

- Aligned Zustand player state and PlayerBar feedback with real `HTMLAudioElement` playback outcomes.

## Scope / DoD Check

- [x] `isPlaying` becomes true only after `audio.play()` resolves.
- [x] Rejected play/resume requests leave a coherent non-playing state.
- [x] Fatal media `error` events stop playback state and expose a useful Korean error.
- [x] Transient `stalled` events preserve native buffering behavior without forcing pause or retry.
- [x] Every manual `next()` stop branch invalidates pending playback attempts.
- [x] PlayerBar forwards playback errors through the existing user-visible toast path without redesign.
- [x] `loadedmetadata`, `timeupdate`, seek, next, repeat-one, and persisted queue behavior remain compatible.
- [x] Focused player tests, typecheck, and scoped ESLint pass.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution and approved execution rules |
| 0 | `docs/standards/development-standards.md` | SE implementation and testing standards |
| 1 | `docs/policies/quality-gates.md` | Traceability, regression, and rollback gates |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React state and effect guidance |
| 2 | `deliverables/user/REQ-20260715-ATS-001.md` | Approved player outcome requirements |
| 2 | `docs/ui/index.md` | UI document entry point |
| 2 | `docs/design/usecase/sound-track.md` | SOUND-010 playback contract |

**Injection order applied:** After reading the handoff packet, its input pointers were consumed in Tier 0, Tier 1, and Tier 2 order before the file and reproduction context. The assignee role was `se` and the task type was frontend implementation.

## Evidence Pointers

### Files Changed

- `frontend/src/store/playerStore.ts:8` - playback-attempt token and Korean failure messages.
- `frontend/src/store/playerStore.ts:146` - guarded fatal media failure and Promise-based playback helpers.
- `frontend/src/store/playerStore.ts:200` - fatal media `error` handling; no fatal `stalled` listener is registered.
- `frontend/src/store/playerStore.ts:221` - play initializes a non-playing pending state and records history only after success.
- `frontend/src/store/playerStore.ts:249` - resume uses the same Promise outcome lifecycle.
- `frontend/src/store/playerStore.ts:265` - visible-list end delegates to the attempt-invalidating pause action.
- `frontend/src/store/playerStore.ts:273` - empty queue delegates to the attempt-invalidating pause action.
- `frontend/src/store/playerStore.ts:293` - repeat-off queue end delegates to the attempt-invalidating pause action.
- `frontend/src/layouts/PlayerBar.tsx:26` - subscribes to `playbackError`.
- `frontend/src/layouts/PlayerBar.tsx:63` - sends playback failure to the existing error toast.
- `frontend/src/store/playerStore.test.ts:147` - pending play resolution and persisted queue/history coverage.
- `frontend/src/store/playerStore.test.ts:179` - play/resume rejection coverage.
- `frontend/src/store/playerStore.test.ts:200` - stale Promise resolution coverage.
- `frontend/src/store/playerStore.test.ts:219` - metadata, time progression, and seek coverage.
- `frontend/src/store/playerStore.test.ts:244` - transient stalled behavior, fatal error state, and successful retry coverage.
- `frontend/src/store/playerStore.test.ts:267` - all three manual `next()` stop branches invalidate pending playback.
- `frontend/src/store/playerStore.test.ts:290` - next and repeat-one compatibility coverage.
- `frontend/src/layouts/PlayerBar.test.tsx:72` - user-visible Korean toast feedback coverage.

## Commands & Outputs

- Red phase: `npm test -- src/store/playerStore.test.ts src/layouts/PlayerBar.test.tsx` - expected failure before implementation, 5 failed and 1 passed.
- Initial green phase: `npm test -- src/store/playerStore.test.ts src/layouts/PlayerBar.test.tsx` - passed, 2 test files and 7 tests.
- Independent-review red phase: `npm test -- src/store/playerStore.test.ts src/layouts/PlayerBar.test.tsx` - expected failure, 4 failed and 6 passed.
- Final green phase: `npm test -- src/store/playerStore.test.ts src/layouts/PlayerBar.test.tsx` - passed, 2 test files and 10 tests.
- `npm run typecheck` - passed with no TypeScript errors.
- `npx eslint src/store/playerStore.ts src/layouts/PlayerBar.tsx src/store/playerStore.test.ts src/layouts/PlayerBar.test.tsx --max-warnings 0` - passed with no warnings or errors.
- `git diff --check -- frontend/src/store/playerStore.ts frontend/src/layouts/PlayerBar.tsx` - exit 0; Git reported only the workspace line-ending notice.

### Formatting Follow-up

- Workspace-local formatter: Prettier `3.8.1`.
- `npx prettier --write src/store/playerStore.ts src/layouts/PlayerBar.tsx` - formatted exactly the two requested source files.
- Behavior-diff guard: the pre-write Prettier output hash and post-write Git blob hash matched exactly for both files:
  - `playerStore.ts`: `8daeff4d228f75f3849846279d664bf300964615`
  - `PlayerBar.tsx`: `529df23c7b7cea4646c2362e4691c321975e0140`
- `npx prettier --check src/store/playerStore.ts src/layouts/PlayerBar.tsx src/store/playerStore.test.ts src/layouts/PlayerBar.test.tsx` - passed for all four files.
- `npm test -- src/store/playerStore.test.ts src/layouts/PlayerBar.test.tsx` - passed after formatting, 2 test files and 10 tests.
- `npx eslint src/store/playerStore.ts src/layouts/PlayerBar.tsx src/store/playerStore.test.ts src/layouts/PlayerBar.test.tsx --max-warnings 0` - passed after formatting with no warnings or errors.

## Risks / Rollback

- Risk: `stalled` remains a browser-managed transient buffering signal; only Promise rejection and fatal media `error` transition the store to a failure state.
- Rollback: Revert only `frontend/src/store/playerStore.ts` and `frontend/src/layouts/PlayerBar.tsx`, then remove the two WI-focused test files. No backend, database, API, dependency, or documentation rollback is required.

## Follow-ups

- WI-20260715-ATS-019 is ready to unblock WI-20260715-ATS-021 and WI-20260715-ATS-022 in the approved REQ work plan.
