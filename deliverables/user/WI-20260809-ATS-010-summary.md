# WI-20260809-ATS-010 Summary

## Status

Completed on 2026-08-09 with focused verification.

This work item repairs the public Track pagination boundary, aligns Playlist public/detail/reorder behavior to one active-membership contract, and makes Album public counts and `trackCount` ordering active-only. The existing dirty worktree was preserved. No schema or retained-data mutation, external call, membership cleanup, commit, or push was performed.

## Delivered Behavior

### Public Track search

- `TrackService` validates `page >= 1` and `1 <= size <= 100` before constructing `PageRequest` or calling the repository.
- Invalid values use the existing `BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT)` convention.
- The controller remains thin and the response `pageInfo.page` remains 1-based.
- Boundary coverage includes page `0` and negative values; size `0`, negative values, and `101`; and valid size `100`.
- Invalid service requests make no Track repository call.

### Playlist active-membership contract

- Public/owner list counts and playlist detail use active Track memberships only.
- Owner reorder requires exactly the visible active Track IDs.
- Reorder assigns active memberships in requested order first, then retains inactive memberships in deterministic prior order (`trackOrder`, then `trackId`).
- Final membership orders are unique and contiguous. Reactivated hidden Tracks therefore return at the end.
- Inactive membership rows are preserved. Remove/compact behavior remains deterministic across all retained rows.

### Album public aggregate contract

- Public Album counts use active Track memberships only.
- Public `trackCount` sorting counts active Tracks in the repository aggregate query.
- Public Album detail continues to expose active Tracks only.
- Existing all-membership totals used by administrative or mutation paths were not changed.

## Changed Files

Production:

- `src/main/java/com/atstudio/atstudio/service/TrackService.java`
- `src/main/java/com/atstudio/atstudio/repository/PlaylistTrackRepository.java`
- `src/main/java/com/atstudio/atstudio/service/PlaylistService.java`
- `src/main/java/com/atstudio/atstudio/repository/AlbumRepository.java`
- `src/main/java/com/atstudio/atstudio/repository/AlbumTrackRepository.java`
- `src/main/java/com/atstudio/atstudio/service/AlbumService.java`

Tests:

- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java`
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/AlbumServiceTest.java`
- `src/test/java/com/atstudio/atstudio/repository/ActiveMembershipRepositoryTest.java`

Current-state documentation:

- `docs/design/api-spec.md`
- `docs/design/usecase/sound-track.md`
- `docs/design/usecase/sound-playlist.md`
- `docs/design/usecase/sound-album.md`
- `docs/SR/SR-100.md`
- `docs/SR/SR-101.md`

Deliverables:

- `deliverables/user/WI-20260809-ATS-010-summary.md`
- `deliverables/agent/WI-20260809-ATS-010-evidence-pack.md`

Some listed paths already contained unrelated or predecessor-WI changes in the dirty worktree. Those changes were preserved; this list identifies files touched for WI-010 and does not attribute every existing diff hunk to this work item.

## Verification

The final combined focused run passed:

```text
.\gradlew.bat test --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.service.PlaylistServiceTest" --tests "com.atstudio.atstudio.service.AlbumServiceTest" --tests "com.atstudio.atstudio.repository.ActiveMembershipRepositoryTest" --tests "com.atstudio.atstudio.repository.PlayableTrackQueryCountTest" --tests "com.atstudio.atstudio.service.AlbumPlaylistMutationLockContractTest"
BUILD SUCCESSFUL in 50s
111 tests, 0 skipped, 0 failures, 0 errors
```

Scoped `git diff --check` passed. Git emitted line-ending normalization warnings for existing CRLF working-copy files; it reported no whitespace errors. The new repository integration test also passed a separate no-index whitespace check.

An earlier focused Track run failed during test compilation because a new Mockito stub matched overloaded `findAll` methods ambiguously. The test was corrected with an explicit `Pageable` captor and all subsequent focused and combined runs passed. An auxiliary result-count script also initially assumed the wrong package for one test and produced an invalid total; it was rerun from discovered XML paths and confirmed the final total of 111.

No full suite or global documentation validation was run, in accordance with the focused-verification scope. Repository behavior was verified with the existing in-memory H2 `@DataJpaTest` profile; no external or retained local database was contacted.

## Risks And Rollback

- The active aggregate JPQL was exercised through Hibernate/H2, but no production MySQL execution-plan analysis was performed.
- Existing malformed legacy order values are normalized when reorder/remove compaction runs; this WI intentionally performs no backfill.
- Rollback is an inverse patch limited to the WI-touched production, test, and documentation paths, plus removal of the new test and WI deliverables. It must preserve all unrelated dirty-worktree changes. No database rollback is required.

## WI-029 Status

WI-010 addresses the findings that blocked `WI-20260808-ATS-029` and unblocks it for independent reviewer verification. WI-029 is not completed by this engineering work item and remains pending its reviewer rerun and disposition.
