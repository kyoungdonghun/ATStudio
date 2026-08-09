# Evidence Pack: WI-20260808-ATS-018

## Summary (one-liner)

- Replaced immediate native-media stall warnings with one thresholded, generation-fenced 2,000 ms buffering period while preserving real playback error behavior.

## Scope / DoD Check

- [x] `waiting` and `stalled` begin one hidden pending period.
- [x] No stall is exposed at 0 ms, 1,800 ms, or 1,999 ms.
- [x] A still-valid period sets `isStalled=true` at exactly 2,000 ms without changing `isPlaying` or creating `playbackError`.
- [x] Repeated native events do not reset the threshold or create duplicate timers.
- [x] `timeupdate`, `canplay`, `playing`, pause, resume/retry initialization, Track play/change, media error, `play()` rejection, and stop/clear cancel pending and visible buffering.
- [x] Successful `play()` resolution clears pending or visible buffering.
- [x] Captured generation, playback attempt, and Track identity fence stale callbacks independently of `clearTimeout`.
- [x] Media errors and synchronous/asynchronous `play()` rejection remain `playbackError` states and never become stalled states.
- [x] Short buffering creates no analytics, persistence, failure, or error record.
- [x] PlayerBar renders no pending message, sustained buffering as polite status, and actual errors as assertive alerts with error precedence.
- [x] Focused tests, typecheck, changed-file ESLint, changed-file Prettier, and diff checks pass.
- [x] No PlayableTrack/duration/waveform hydration, backend, schema/data, payment, audio-analysis, tag, dependency, Git, secret, archive, deletion, or external-call work occurred.

## Reference Documents (Tier 0-2)

| Tier | Document                                            | Reason                                                                   |
| ---- | --------------------------------------------------- | ------------------------------------------------------------------------ |
| 0    | `docs/standards/core-principles.md`                 | Approved execution, transparency, scope, and sustainable behavior        |
| 0    | `docs/standards/development-standards.md`           | Implementation, testing, and evidence standards                          |
| 0    | `docs/standards/documentation-standards.md`         | English deliverable and pointer-first documentation rules                |
| 0    | `docs/standards/glossary.md`                        | Canonical Track and playback terminology                                 |
| 1    | `docs/standards/frontend-standards.md`              | Zustand singleton player and React test conventions                      |
| 2    | `docs/SR/SR-101.md`                                 | Buffering problem statement, recovery events, and Track-race requirement |
| REQ  | `deliverables/user/REQ-20260808-ATS-004.md`         | Approved 2-second policy, scope, dependencies, and quality gates         |
| WI   | `deliverables/agent/WI-20260808-ATS-018-handoff.md` | Assignee, buffering-only boundary, DoD, write scope, and WI-019 blocker  |

**Assignee:** `se`

**Task type:** frontend state implementation and focused tests

**Write scope applied:** `playerStore` production/test, focused PlayerBar test, and the two required WI-018 deliverables. PlayerBar production code required no change.

## Design Rationale

1. The threshold, timer handle, and generation live beside the singleton `Audio` and `playbackAttempt`, so every module-level media listener observes the same pending period.
2. `beginBuffering()` returns when there is no Track, a real playback error exists, a timer already exists, or a sustained stall is already visible. This prevents duplicate periods and prevents actual errors from being reclassified as buffering.
3. A new period captures `bufferingGeneration`, `playbackAttempt`, and Track ID. Its callback must match all captured values and still see no playback error before exposing stall state.
4. `cancelBuffering()` increments generation before clearing the timer reference, then clears visible stall state together with caller-provided lifecycle updates. Correctness therefore does not depend solely on successful timer cancellation.
5. `startPlayback()` uses the existing attempt guard before either success or rejection can cancel or mutate buffering. A stale promise cannot clear or corrupt a later Track's state.
6. Timer expiry sets only `isStalled`; the current playing state is retained. Failure paths separately pause and set `playbackError`.
7. The PlayerBar already selects `playbackError` before `isStalled` and maps those states to assertive alert and polite status semantics. Tests were strengthened without changing production markup or copy.

## Evidence Pointers

Production:

- `frontend/src/store/playerStore.ts:7` - exact module-local `2_000` threshold.
- `frontend/src/store/playerStore.ts:248` - atomic timer invalidation and visible-stall clearing helper.
- `frontend/src/store/playerStore.ts:262` - single-period guard and captured generation/attempt/Track ID.
- `frontend/src/store/playerStore.ts:270` - callback validation before setting sustained stall.
- `frontend/src/store/playerStore.ts:286` - media-error cancellation and real-error transition.
- `frontend/src/store/playerStore.ts:296` - resume/retry initialization and guarded play lifecycle.
- `frontend/src/store/playerStore.ts:310` - guarded successful resolution and rejection behavior.
- `frontend/src/store/playerStore.ts:324` - `timeupdate` recovery and state update.
- `frontend/src/store/playerStore.ts:343` - native waiting/stalled begin and canplay/playing recovery listeners.
- `frontend/src/store/playerStore.ts:370` - Track play/change cancellation before source replacement.
- `frontend/src/store/playerStore.ts:394` - pause invalidation and cancellation.
- `frontend/src/store/playerStore.ts:564` - stop/clear invalidation and reset.
- `frontend/src/layouts/PlayerBar.tsx:595` - existing real-error precedence over sustained buffering.
- `frontend/src/layouts/PlayerBar.tsx:608` - existing alert/status and assertive/polite live-region split.

Tests:

- `frontend/src/store/playerStore.test.ts:147` - singleton timer cleanup after every test.
- `frontend/src/store/playerStore.test.ts:254` - hidden state at 0 ms and 1,800 ms.
- `frontend/src/store/playerStore.test.ts:273` - exact 2,000 ms threshold and preserved playing/error state.
- `frontend/src/store/playerStore.test.ts:291` - repeated-event clock and timer-count proof.
- `frontend/src/store/playerStore.test.ts:312` - parameterized pre-threshold recovery for timeupdate/canplay/playing.
- `frontend/src/store/playerStore.test.ts:328` - visible sustained-stall recovery.
- `frontend/src/store/playerStore.test.ts:343` - pause cancellation.
- `frontend/src/store/playerStore.test.ts:359` - resume/retry initialization cancellation.
- `frontend/src/store/playerStore.test.ts:378` - Track-change cancellation.
- `frontend/src/store/playerStore.test.ts:398` - media error remains an error-only state.
- `frontend/src/store/playerStore.test.ts:414` - stop/clear cancellation.
- `frontend/src/store/playerStore.test.ts:436` - asynchronous `play()` rejection remains an error-only state.
- `frontend/src/store/playerStore.test.ts:460` - no-op `clearTimeout` proof after pause and later attempt.
- `frontend/src/store/playerStore.test.ts:481` - no-op `clearTimeout`, old Track timer, and stale rejection proof.
- `frontend/src/layouts/PlayerBar.test.tsx:171` - no pending buffering message.
- `frontend/src/layouts/PlayerBar.test.tsx:179` - assertive actual-error behavior.
- `frontend/src/layouts/PlayerBar.test.tsx:194` - polite sustained-buffering behavior.
- `frontend/src/layouts/PlayerBar.test.tsx:206` - actual-error precedence over stalled state.

Complete changed-file inventory is in `deliverables/user/WI-20260808-ATS-018-summary.md`.

## Commands & Outputs

1. Combined focused regression:

```powershell
npm test -- src/store/playerStore.test.ts src/layouts/PlayerBar.test.tsx
```

- PASS: 2 test files, 43 tests, 0 failed; Vitest duration 5.00s.

2. Exact per-suite focused regressions:

```powershell
npm test -- src/store/playerStore.test.ts
npm test -- src/layouts/PlayerBar.test.tsx
```

- `playerStore.test.ts`: PASS, 1 file, 30 tests, 0 failed; Vitest duration 3.22s.
- `PlayerBar.test.tsx`: PASS, 1 file, 13 tests, 0 failed; Vitest duration 4.96s.

3. TypeScript typecheck:

```powershell
npm run typecheck
```

- PASS: `tsc --noEmit`, exit code 0.

4. Changed-file ESLint:

```powershell
npx eslint src/store/playerStore.ts src/store/playerStore.test.ts src/layouts/PlayerBar.test.tsx --max-warnings 0
```

- PASS: exit code 0, 0 errors, 0 warnings.

5. Changed-file Prettier check:

```powershell
npx prettier --check src/store/playerStore.ts src/store/playerStore.test.ts src/layouts/PlayerBar.test.tsx ../deliverables/user/WI-20260808-ATS-018-summary.md ../deliverables/agent/WI-20260808-ATS-018-evidence-pack.md
```

- PASS: all 5 files matched Prettier style.
- The first check identified only the two new Markdown deliverables; local Prettier formatted those two allowed files, and the exact check then passed.

6. Shared-worktree whitespace verification:

```powershell
git diff --check
```

- PASS: exit code 0, no whitespace errors. Git emitted existing CRLF conversion warnings for unrelated dirty backend files; no line-ending command was run.

## Cancellation And Race Proof

| Trigger                              | Timer action                                              | State outcome                   | Focused evidence                                     |
| ------------------------------------ | --------------------------------------------------------- | ------------------------------- | ---------------------------------------------------- |
| `waiting` / `stalled`                | Start only when no timer exists                           | Hidden until threshold          | 0/1,800/1,999/2,000 ms tests                         |
| Repeated native event                | Reuse existing period                                     | Original clock retained         | Timer count remains one                              |
| `timeupdate` / `canplay` / `playing` | Cancel and invalidate                                     | Pending/visible stall cleared   | Parameterized recovery plus sustained recovery       |
| `pause()`                            | Increment attempt, cancel, invalidate                     | `isPlaying=false`, no stall     | Pause test and no-op `clearTimeout` race test        |
| `resume()` / retry                   | Increment attempt, cancel, invalidate                     | New attempt owns state          | Resume/retry test and no-op `clearTimeout` race test |
| `play(track)`                        | Cancel before source replacement, then create new attempt | New Track owns state            | Track-change and old-Track race tests                |
| `audio.error`                        | Increment attempt, cancel, pause                          | Media `playbackError`, no stall | Media-error test                                     |
| `play()` rejection                   | Guard attempt, cancel current period                      | Play `playbackError`, no stall  | Async and existing sync rejection tests              |
| Successful `play()`                  | Guard attempt, cancel current period                      | Playing, no stall/error         | Existing lifecycle and new resume/Track tests        |
| `clearQueue()`                       | Increment attempt, cancel, clear source                   | Empty non-playing state         | Stop/clear test                                      |

The two no-op `clearTimeout` tests leave the original callback queued. Advancing fake time beyond 2,000 ms executes that callback, but captured generation/attempt/Track checks reject it. This directly proves the race fence rather than inferring correctness from timer removal.

## Scope Preservation

- Baseline branch was confirmed as `codex/v1-release-rehearsal-fixes` before editing.
- Existing dirty WI-014 through WI-017 files were observed and preserved.
- `frontend/src/layouts/PlayerBar.tsx` was not changed.
- No backend, schema/data, root `application-local.yml`, payment, audio-analysis, tag, dependency, ZIP, secret, or Git-state path was changed.
- No files were deleted, no commit was created, and no external call was made.
- No PlayableTrack DTO, duration, waveform, queue hydration, or history hydration implementation was added.

## Risks / Rollback

Risks:

- Fake timers and `ControlledAudio` deterministically prove state transitions and races, but they do not reproduce every browser/network event ordering. Later frontend QA should rehearse browser network throttling around short recovery and sustained buffering.
- Background-tab timer throttling can expose a sustained warning later than 2,000 ms. It will not expose it earlier.
- Pending state is intentionally not public and has no visible spinner. Product changes to pending UX require a later approved WI.
- PlayableTrack data completeness remains outside this WI and is still required for duration/waveform correctness.

Rollback:

1. Revert only `frontend/src/store/playerStore.ts`, `frontend/src/store/playerStore.test.ts`, `frontend/src/layouts/PlayerBar.test.tsx`, and the two WI-018 deliverables while preserving every unrelated dirty change.
2. Restore immediate stalled/waiting handling and its previous focused expectations as one behavioral unit.
3. No schema, data, dependency, backend, or external-system rollback is required.

## Follow-ups

- WI-016 is recorded complete, and WI-018 is complete; therefore WI-019 is unblocked.
- WI-019 owns PlayableTrack DTO hydration, duration/waveform completeness, queue transitions, and persisted play-history hydration.
- Later frontend QA retains browser-level network-throttling rehearsal and repository-wide quality gates.
