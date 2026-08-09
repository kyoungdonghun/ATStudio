# Evidence Pack: WI-20260808-ATS-019

## Summary (one-liner)

- Closed the remaining PlayableTrack query fixture and persisted player/history hydration races while preserving the existing bounded public batch contract.

## Scope / DoD Check

- [x] Query-count fixture Users receive unique valid nicknames without changing query assertions.
- [x] Empty normalized persisted state becomes ready with zero HTTP requests.
- [x] Persisted hydration uses a generation and snapshot-identity fence independent of request cancellation.
- [x] `play`, `playAll`, add, remove, reorder, clear, and seek supersede stale persisted hydration consistently.
- [x] Stale success and stale failure cannot overwrite newer explicit player actions.
- [x] Genuine persisted hydration failure retains retry state and a later retry succeeds.
- [x] History hydration merges against latest pending storage and preserves in-flight save, delete, and clear actions.
- [x] History order, deletion semantics, and the 100-entry cap remain intact with one batch request.
- [x] Raw `@Size(max = 100)`, deduplication, requested order, active-only filtering, query-count bounds, and no-public-audio-key behavior remain passing.
- [x] Focused Gradle and Vitest tests, frontend typecheck, changed-file ESLint, changed-file Prettier, and `git diff --check` pass.
- [x] No full suite, build, coverage, schema/data mutation, external call, commit, deletion, or unrelated WI edit occurred.

## Reference Documents (Tier 0-2)

The user-required documents were read in the specified order before implementation.

| Order | Tier | Document                                            | Reason                                                         |
| ----- | ---- | --------------------------------------------------- | -------------------------------------------------------------- |
| 1     | 0    | `docs/standards/core-principles.md`                 | Approved execution, transparency, scope, and sustainability    |
| 2     | 0    | `docs/standards/development-standards.md`           | Java/TypeScript implementation, testing, and evidence rules    |
| 3     | 1    | `docs/standards/frontend-standards.md`              | Zustand singleton player and local persistence conventions     |
| 4     | REQ  | `deliverables/user/REQ-20260808-ATS-004.md`         | Approved PlayableTrack, batch hydration, and WI chain baseline |
| 5     | SR   | `docs/SR/SR-101.md`                                 | PlayableTrack completeness, batching, and race requirements    |
| 6     | WI   | `deliverables/agent/WI-20260808-ATS-019-handoff.md` | Assignee, scope, DoD, output contract, and WI-020 blocker      |

Supplemental references:

- `docs/standards/documentation-standards.md` - English and pointer-first deliverable rules.
- `docs/standards/glossary.md` - Canonical Track and playback terminology.
- `docs/design/usecase/sound-track.md` - Public listening and original storage-key boundary.

**Assignee:** `se`

**Task type:** backend test fixture and frontend state implementation with focused regression tests

## Design Rationale

1. `playerStateGeneration` is module-local beside the pending snapshot and shared hydration promise. Every explicit persisted-state mutation advances it and clears the pending snapshot.
2. A hydration request captures both generation and snapshot identity. Success and failure callbacks must still match both values before they can restore media state or expose an error.
3. The request remains in flight after supersession. Correctness comes from the token fence, not promise cancellation. The shared promise is cleared only on settlement.
4. A real fetch failure does not clear `pendingPlayerState`; a later `hydratePersistedState()` call therefore retries the same normalized snapshot.
5. Empty normalized snapshots bypass `fetchPlayableTracks`, clear any stale local media state, and become ready synchronously.
6. History hydration uses the response as an ID-to-Track lookup but re-reads pending storage immediately before mapping. Full Tracks from newer saves take precedence, deleted IDs are absent, and a cleared null storage key is not recreated.
7. Save and delete now persist mixed full-Track and ID-only entries, so unresolved legacy entries survive explicit mutations until the single batch completes.

## Evidence Pointers

Narrow repair production:

- `frontend/src/store/playerStore.ts:126` - pending player snapshot, shared promise, and state generation.
- `frontend/src/store/playerStore.ts:190` - mixed pending-history persistence helper.
- `frontend/src/store/playerStore.ts:203` - save retains unresolved entries and enforces newest-first maximum 100.
- `frontend/src/store/playerStore.ts:230` - one-batch history hydration against latest pending storage.
- `frontend/src/store/playerStore.ts:267` - deletion operates on pending and hydrated history entries.
- `frontend/src/store/playerStore.ts:325` - explicit-mutation generation advance and pending snapshot discard.
- `frontend/src/store/playerStore.ts:454` - zero-request empty hydration, captured token, success/failure fences, and retry-preserving failure path.
- `frontend/src/store/playerStore.ts:519` - explicit play supersedes persisted hydration.
- `frontend/src/store/playerStore.ts:635` - seek supersedes persisted current-time restoration.
- `frontend/src/store/playerStore.ts:666` - `playAll` supersedes persisted current/queue state.
- `frontend/src/store/playerStore.ts:675` - add, remove, and reorder mutation fences.
- `frontend/src/store/playerStore.ts:722` - clear/stop mutation fence and ready empty state.

Narrow repair tests:

- `src/test/java/com/atstudio/atstudio/repository/PlayableTrackQueryCountTest.java:41` - per-test User nickname sequence.
- `src/test/java/com/atstudio/atstudio/repository/PlayableTrackQueryCountTest.java:261` - valid unique nickname construction.
- `frontend/src/store/playerPersistence.test.ts:180` - empty persisted state makes no request.
- `frontend/src/store/playerPersistence.test.ts:203` - stale success after explicit play.
- `frontend/src/store/playerPersistence.test.ts:224` - stale failure after explicit play.
- `frontend/src/store/playerPersistence.test.ts:243` - stale success after clear.
- `frontend/src/store/playerPersistence.test.ts:264` - add/remove/reorder mutation fence.
- `frontend/src/store/playerPersistence.test.ts:286` - `playAll` mutation fence.
- `frontend/src/store/playerPersistence.test.ts:306` - genuine failure retry.
- `frontend/src/store/playerPersistence.test.ts:403` - in-flight history save merge.
- `frontend/src/store/playerPersistence.test.ts:422` - in-flight history deletion.
- `frontend/src/store/playerPersistence.test.ts:447` - in-flight history clear.
- `frontend/src/store/playerPersistence.test.ts:470` - one-batch newest-first 100-entry proof.

Preserved backend contract:

- `src/main/java/com/atstudio/atstudio/dto/track/PlayableTrackBatchRequest.java:10` - raw request validation, including `@Size(max = 100)`.
- `src/main/java/com/atstudio/atstudio/dto/track/PlayableTrackResponse.java:9` - public response contains no `audioFile` field.
- `src/main/java/com/atstudio/atstudio/service/PlayableTrackService.java:29` - bounded deduplication, active lookup, Tag batch, and requested-order mapping.
- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java:54` - public response duration/waveform and no-audio-key proof.
- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java:80` - raw 101-ID request rejected before service interaction.
- `src/test/java/com/atstudio/atstudio/repository/PlayableTrackQueryCountTest.java:57` - one-versus-many two-query batch assertion.
- `src/test/java/com/atstudio/atstudio/repository/PlayableTrackQueryCountTest.java:74` - dedupe, requested active order, and service bound.
- `src/test/java/com/atstudio/atstudio/repository/PlayableTrackQueryCountTest.java:110` - album/playlist/like aggregate query invariance.
- `src/test/java/com/atstudio/atstudio/repository/PlayableTrackQueryCountTest.java:214` - Download History two-query invariance.

The exact five-file narrow repair inventory is recorded in `deliverables/user/WI-20260808-ATS-019-summary.md`.

## Commands & Outputs

### Failure Reproduction

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.repository.PlayableTrackQueryCountTest" --console=plain
```

- RED: 4 tests, 1 failed. `aggregateMappingsUseOneQueryForOneOrManyRows()` failed at the User save because all fixtures used duplicate nickname `Playable Artist`.

```powershell
npm test -- src/store/playerPersistence.test.ts --reporter=verbose
```

- RED after adding the initial race tests: 1 file, 16 tests; 7 passed and 9 failed.
- Failures covered empty batch invocation, stale player overwrite after explicit mutations, and stale history overwrite after save/delete/clear.

### Focused Backend Verification

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.repository.PlayableTrackQueryCountTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.service.AlbumServiceTest" --tests "com.atstudio.atstudio.service.PlaylistServiceTest" --tests "com.atstudio.atstudio.service.LikeServiceTest" --tests "com.atstudio.atstudio.service.DownloadServiceTest" --console=plain
```

- PASS: Gradle `BUILD SUCCESSFUL`.
- Exact XML totals: 6 classes, 80 tests, 0 failures, 0 errors, 0 skipped.
- Per class: `PlayableTrackQueryCountTest` 4, `TrackControllerTest` 25, `AlbumServiceTest` 12, `PlaylistServiceTest` 23, `LikeServiceTest` 6, `DownloadServiceTest` 10.

### Focused Frontend Verification

```powershell
npm test -- src/store/playerPersistence.test.ts src/store/playerStore.test.ts src/utils/playableTrack.test.ts src/components/catalogComponents.test.tsx src/components/player/playerComponents.test.tsx src/layouts/PlayerBar.test.tsx src/pages/public/TrackDetailPage.test.tsx src/pages/subscriber/DownloadHistoryPage.test.tsx
```

- PASS: 8 files, 86 tests, 0 failed; Vitest duration 7.65s.

```powershell
npm run typecheck
```

- PASS: `tsc --noEmit`, exit code 0.

```powershell
npx eslint src/store/playerStore.ts src/store/playerPersistence.test.ts --max-warnings 0
```

- PASS: exit code 0, 0 errors, 0 warnings.

```powershell
npx prettier --check src/store/playerStore.ts src/store/playerPersistence.test.ts ../deliverables/user/WI-20260808-ATS-019-summary.md ../deliverables/agent/WI-20260808-ATS-019-evidence-pack.md
```

- PASS: all 4 files matched Prettier style.

```powershell
git diff --check
```

- PASS: exit code 0 and no whitespace errors. Git also emitted existing CRLF-to-LF conversion warnings for dirty Java files; no line-ending rewrite command was run.

## Race And Retry Matrix

| State transition                            | Fence or merge rule                             | Result                                                 |
| ------------------------------------------- | ----------------------------------------------- | ------------------------------------------------------ |
| Persisted response after `play`             | Generation and snapshot mismatch                | Explicit Track, source, queue, and ready status remain |
| Persisted rejection after `play`            | Failure callback uses the same fence            | Ready status remains; stale error is ignored           |
| Persisted response after `clearQueue`       | Generation and pending snapshot invalidated     | Empty state and empty source remain                    |
| Persisted response after add/remove/reorder | Every queue mutator advances generation         | Latest explicit order remains                          |
| Persisted response after `playAll`          | Queue and play both supersede snapshot          | Explicit list and first current Track remain           |
| Persisted request genuinely fails           | Generation and snapshot still match             | Error is exposed; pending snapshot remains retryable   |
| History response after save                 | Re-read latest pending entries; full Track wins | New Track remains first                                |
| History response after delete               | Deleted ID absent from latest pending entries   | Deleted Track cannot return                            |
| History response after clear                | Latest entries empty and key absent             | Result empty; key remains absent                       |
| History response after save at cap          | Save slices latest pending list to 100          | New Track plus prior first 99 remain in one request    |

## Scope Preservation

- Branch was confirmed as `codex/v1-release-rehearsal-fixes` before editing.
- Existing dirty WI-014 through WI-018 files were observed and preserved.
- The intentional untracked `output/client-demo-screenshots-20260716-140514.zip` remains untouched.
- No schema, data, dependency, payment, provider, storage, secret, Git-state, or external-system operation occurred.
- No files were deleted and no commit was created.
- Only the user-authorized focused tests and static checks were run; no full project suite was run.

## Risks / Rollback

Risks:

- Controlled Audio and mocked batch tests do not cover every browser reload/network ordering. Browser acceptance should rehearse fast actions while hydration is delayed.
- Generation is local to one JavaScript process. Cross-tab localStorage writes do not advance another tab's generation.
- Superseded requests are allowed to settle and consume backend work; only state application is fenced.
- Out-of-band writers that add new ID-only history entries during an existing request are outside the explicit save/delete/clear API contract and may need a later batch.
- The focused test boundary excludes full suites, coverage, builds, production-MySQL query plans, and final cross-layer documentation checks.

Rollback:

1. Revert only `frontend/src/store/playerStore.ts`, `frontend/src/store/playerPersistence.test.ts`, `src/test/java/com/atstudio/atstudio/repository/PlayableTrackQueryCountTest.java`, and the two WI-019 deliverables while preserving unrelated dirty files.
2. Revert generation fencing and history latest-pending merging together with their focused tests as one behavioral unit.
3. No schema, data, provider, payment, storage, dependency, or external-system rollback is required.

## Follow-ups

- WI-019 is complete and WI-020 is unblocked.
- Later planned WIs retain full-suite, coverage, build, documentation, security, browser acceptance, and cross-layer release gates.
