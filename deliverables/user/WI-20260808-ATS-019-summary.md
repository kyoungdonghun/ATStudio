# WI-20260808-ATS-019 Completion Summary

## Status

WI-20260808-ATS-019 is **complete** on `codex/v1-release-rehearsal-fixes`. The prior PlayableTrack implementation was preserved, and the verified backend fixture, empty-state hydration, persisted-player race, and play-history race gaps were repaired.

WI-20260808-ATS-020 is **unblocked**. No commit was created.

## Delivered Repair

- `PlayableTrackQueryCountTest` now assigns each saved User a unique valid nickname such as `PlayableArtist1`, preserving all query-count assertions.
- A normalized persisted player state with `currentTrackId=null` and no queue IDs becomes `ready` without calling the batch API.
- Persisted player hydration captures a module-local state generation. Explicit `play`, `playAll`, add, remove, reorder, clear, and seek mutations advance that generation and discard the old pending snapshot.
- Stale hydration success and failure callbacks verify both generation and snapshot identity before changing state. Newer explicit actions therefore remain authoritative even though the request itself is not canceled.
- A genuine batch failure retains its pending snapshot, sets `persistedHydration='error'`, and can be retried successfully.
- Play-history hydration maps its one batch response against the latest pending local history instead of the request-start snapshot. In-flight save, delete, and clear actions retain their latest order and intent.
- History save/delete paths preserve unresolved ID-only legacy entries until hydration. The 100-entry maximum and newest-first order remain enforced without row-by-row requests.

## Preserved Backend Contract

- Raw `PlayableTrackBatchRequest.ids` remains `@NotEmpty` and `@Size(max = 100)` with positive non-null IDs.
- Service hydration remains bounded to 100 distinct IDs, preserves first-requested order, excludes inactive or missing Tracks, and performs one Track query plus one Tag query for either one or many Tracks.
- Public batch data still contains duration and waveform metadata but no original `audioFile` storage key.
- Album, Playlist, TrackLike, and Download History mapping query counts remain independent of result count in the focused query test.
- No schema, data, provider, payment, storage, or other external call was made.

## Narrow Repair Changed Files

Production:

- `frontend/src/store/playerStore.ts`

Tests:

- `frontend/src/store/playerPersistence.test.ts`
- `src/test/java/com/atstudio/atstudio/repository/PlayableTrackQueryCountTest.java`

WI deliverables:

- `deliverables/user/WI-20260808-ATS-019-summary.md`
- `deliverables/agent/WI-20260808-ATS-019-evidence-pack.md`

No other file was edited by this narrow repair. Existing WI-014 through WI-018 changes and `output/client-demo-screenshots-20260716-140514.zip` were preserved.

## Verification

| Command                                                                                                                                                                                                                                                                                                                                                                                                             | Result                                                                            |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------- |
| `.\gradlew.bat test --tests "com.atstudio.atstudio.repository.PlayableTrackQueryCountTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.service.AlbumServiceTest" --tests "com.atstudio.atstudio.service.PlaylistServiceTest" --tests "com.atstudio.atstudio.service.LikeServiceTest" --tests "com.atstudio.atstudio.service.DownloadServiceTest" --console=plain` | PASS, 6 classes / 80 tests / 0 failed / 0 errors / 0 skipped                      |
| `npm test -- src/store/playerPersistence.test.ts src/store/playerStore.test.ts src/utils/playableTrack.test.ts src/components/catalogComponents.test.tsx src/components/player/playerComponents.test.tsx src/layouts/PlayerBar.test.tsx src/pages/public/TrackDetailPage.test.tsx src/pages/subscriber/DownloadHistoryPage.test.tsx`                                                                                | PASS, 8 files / 86 tests / 0 failed                                               |
| `npm run typecheck`                                                                                                                                                                                                                                                                                                                                                                                                 | PASS, `tsc --noEmit`, exit code 0                                                 |
| `npx eslint src/store/playerStore.ts src/store/playerPersistence.test.ts --max-warnings 0`                                                                                                                                                                                                                                                                                                                          | PASS, 0 errors / 0 warnings                                                       |
| `npx prettier --check src/store/playerStore.ts src/store/playerPersistence.test.ts ../deliverables/user/WI-20260808-ATS-019-summary.md ../deliverables/agent/WI-20260808-ATS-019-evidence-pack.md`                                                                                                                                                                                                                  | PASS, all 4 files matched                                                         |
| `git diff --check`                                                                                                                                                                                                                                                                                                                                                                                                  | PASS, no whitespace errors; existing CRLF conversion warnings on dirty Java files |

The final Gradle count was read from the six generated `TEST-*.xml` reports: `4 + 25 + 12 + 23 + 6 + 10 = 80`. The full backend suite, full frontend suite, coverage, and build were intentionally not run because this repair was restricted to the focused commands above.

## Race Proof

| Scenario                                 | Expected result                         | Focused proof                                 |
| ---------------------------------------- | --------------------------------------- | --------------------------------------------- |
| Empty persisted queue                    | Ready, zero HTTP requests               | Batch mock remains uncalled                   |
| In-flight response after `play`          | Explicit Track and queue remain         | Deferred success and failure tests            |
| In-flight response after `clearQueue`    | Empty player remains                    | Deferred success test                         |
| In-flight response after queue mutations | Latest add/remove/reorder order remains | Combined mutation test                        |
| In-flight response after `playAll`       | Explicit current Track and queue remain | Deferred success test                         |
| Genuine fetch failure                    | Error is retryable                      | Reject once, then resolve successfully        |
| In-flight history save                   | New entry remains first                 | Latest-pending merge test                     |
| In-flight history delete                 | Deleted Track does not return           | Latest-pending merge test                     |
| In-flight history clear                  | Storage remains cleared                 | Null-storage preservation test                |
| Save against 100 pending entries         | New entry plus first 99 remain          | One batch request, 100-result order assertion |

## Residual Risks

- The race tests use mocked batch responses and a controlled `Audio` implementation. Browser acceptance should still rehearse reload, quick player actions, and history actions under throttled networking.
- The generation fence is process-local. Another browser tab can write the same localStorage keys without advancing this tab's generation; cross-tab synchronization is not part of WI-019.
- A stale request is ignored rather than aborted, so it can still consume network and server work before its response is discarded.
- Focused backend query-count evidence uses the configured test database. Production-MySQL query plans and payload sizes remain final QA concerns.
- Full-suite, coverage, build, documentation, and cross-layer release gates remain assigned to later WIs and were not inferred from these focused passes.

## Rollback

1. Revert only the three narrow repair code/test files and the two WI-019 deliverables while preserving unrelated WI-014 through WI-018 changes.
2. Restore the prior request-start history snapshot behavior and unfenced persisted hydration as one frontend unit only if the accompanying race tests are also reverted.
3. No schema, data, provider, payment, storage, dependency, or external-system rollback is required.

## WI Chain

- WI-019 acceptance gaps are closed by focused backend, frontend, type, lint, format, and whitespace evidence.
- WI-020 is unblocked and may proceed under its existing approved handoff flow.
