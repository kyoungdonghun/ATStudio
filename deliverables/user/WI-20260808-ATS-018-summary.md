# WI-20260808-ATS-018 Completion Summary

## Status

WI-20260808-ATS-018 is **complete** on `codex/v1-release-rehearsal-fixes`. Native `waiting` and `stalled` events now remain hidden for the first 2,000 ms, while sustained buffering becomes a retryable nonfatal state that remains distinct from actual media and `play()` errors.

No backend, schema, data, payment, audio-analysis, tag, dependency, root configuration, stored archive, Git state, or external system was changed. No commit was created. PlayableTrack, duration, and waveform hydration remain unchanged for WI-019.

## Delivered Behavior

- `waiting` or `stalled` starts one module-local pending timer without immediately setting `isStalled`.
- The threshold is exactly 2,000 ms. State remains non-stalled at 0 ms, 1,800 ms, and 1,999 ms; it becomes stalled when the timer reaches 2,000 ms.
- Threshold expiry changes only `isStalled`. It preserves `isPlaying` and requires `playbackError` to remain `null`.
- Repeated `waiting` and `stalled` events during one pending period neither restart the clock nor create another timer.
- `timeupdate`, `canplay`, and `playing` cancel pending buffering and clear a visible stall.
- `pause()`, resume/retry initialization, `play(track)`, media error, synchronous or asynchronous `play()` rejection, and `clearQueue()` cancel pending buffering and clear a visible stall.
- Successful `play()` resolution clears pending or visible buffering before recording successful play history.
- Real `audio.error` and `play()` rejection continue to set `playbackError`; neither can later surface as `isStalled`.
- Short buffering performs no API, storage, analytics, failure, or error-recording action.
- The existing PlayerBar copy and behavior remain unchanged: sustained buffering uses `role="status"` with polite announcements, while real errors use `role="alert"` with assertive announcements and take precedence.

## Timer And Generation Design

- `BUFFERING_THRESHOLD_MS` is a module-local `2_000` constant beside the singleton `Audio` state.
- `bufferingTimer` represents the only active pending period. `beginBuffering()` returns when a timer already exists or a sustained stall is already visible.
- `bufferingGeneration` increments when a period begins and whenever buffering is canceled.
- Each timer captures its buffering generation, current `playbackAttempt`, and Track ID.
- Before setting `isStalled`, the callback rechecks all three captured values and confirms `playbackError === null`.
- `cancelBuffering()` clears the timer handle, invalidates its generation, and clears visible stall state together with any lifecycle state update supplied by the caller.
- Generation and attempt checks are independent of `clearTimeout`. Focused tests replace `clearTimeout` with a no-op and prove stale callbacks still cannot affect a paused, retried, or newly selected Track.

## Changed Files

Production:

- `frontend/src/store/playerStore.ts`

Tests:

- `frontend/src/store/playerStore.test.ts`
- `frontend/src/layouts/PlayerBar.test.tsx`

WI deliverables:

- `deliverables/user/WI-20260808-ATS-018-summary.md`
- `deliverables/agent/WI-20260808-ATS-018-evidence-pack.md`

`frontend/src/layouts/PlayerBar.tsx` was inspected but not changed because its existing feedback precedence and live-region semantics already satisfy WI-018.

## Verification

| Command                                                                                                                                                                                                                     | Result                              |
| --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------- |
| `npm test -- src/store/playerStore.test.ts`                                                                                                                                                                                 | PASS, 1 file / 30 tests / 0 failed  |
| `npm test -- src/layouts/PlayerBar.test.tsx`                                                                                                                                                                                | PASS, 1 file / 13 tests / 0 failed  |
| `npm test -- src/store/playerStore.test.ts src/layouts/PlayerBar.test.tsx`                                                                                                                                                  | PASS, 2 files / 43 tests / 0 failed |
| `npm run typecheck`                                                                                                                                                                                                         | PASS, `tsc --noEmit`, exit code 0   |
| `npx eslint src/store/playerStore.ts src/store/playerStore.test.ts src/layouts/PlayerBar.test.tsx --max-warnings 0`                                                                                                         | PASS, 0 errors / 0 warnings         |
| `npx prettier --check src/store/playerStore.ts src/store/playerStore.test.ts src/layouts/PlayerBar.test.tsx ../deliverables/user/WI-20260808-ATS-018-summary.md ../deliverables/agent/WI-20260808-ATS-018-evidence-pack.md` | PASS, all 5 files matched           |
| `git diff --check`                                                                                                                                                                                                          | PASS, no whitespace errors          |

## Cancellation And Race Proof

| Lifecycle                     | Proof                                                                                                                                              |
| ----------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| Short pending period          | Fake timers assert no visible stall at 0 ms and 1,800 ms.                                                                                          |
| Exact threshold               | Fake timers assert false at 1,999 ms and true after the next 1 ms, with playing state preserved and no error.                                      |
| Duplicate native events       | Timer count remains one after `waiting` followed by `stalled`; recovery leaves no late duplicate.                                                  |
| Media recovery                | Parameterized tests cover `timeupdate`, `canplay`, and `playing` before threshold; a separate test clears an already visible stall.                |
| User lifecycle                | Focused tests cover pause, resume/retry, Track change, and stop/clear.                                                                             |
| Actual failures               | Focused tests cover media error and asynchronous `play()` rejection as error-only states; the existing synchronous rejection test remains passing. |
| Stale timer after pause/retry | With `clearTimeout` mocked to do nothing, generation and attempt validation prevent the old callback from setting stall.                           |
| Stale Track timer/rejection   | With `clearTimeout` mocked to do nothing, the old Track timer and old rejected promise cannot alter the successfully playing next Track.           |
| PlayerBar semantics           | Tests cover no pending message, polite sustained status, assertive real error, and error precedence.                                               |

## Residual Risks

- Timing and event-order behavior is verified with Vitest fake timers and a controlled singleton `Audio` implementation, not a throttled real browser and network. A later acceptance or frontend QA pass should rehearse short and sustained buffering under browser network throttling.
- Browser timer throttling can delay the callback beyond 2,000 ms. The contract guarantees that the warning is not shown before the threshold; it cannot guarantee exact wall-clock rendering under a suspended tab.
- Pending buffering remains intentionally internal and has no spinner or public store field. This matches WI-018 but provides no direct UI indication during the first two seconds.
- Duration and waveform completeness are intentionally unresolved here and remain owned by WI-019.

## Rollback

1. Revert only the three WI-018 frontend files and its two deliverables while preserving all unrelated dirty WI-014 through WI-017 changes.
2. Restore the prior immediate `waiting`/`stalled` listeners together with the prior tests.
3. No backend, schema, data, dependency, or external-system rollback is required.

## WI Chain

- WI-016 and WI-018 are complete, so WI-019 is unblocked.
- WI-019 owns PlayableTrack DTO hydration, duration, waveform, queue, and persisted-history completeness. None of that work was pulled into WI-018.
