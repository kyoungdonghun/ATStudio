# WI-20260809-ATS-010 Evidence Pack

## Identity

| Field | Value |
|---|---|
| Work item | `WI-20260809-ATS-010` |
| Role | `se` |
| Date | 2026-08-09 |
| Status | Completed with focused verification |
| Authoritative input | `deliverables/agent/WI-20260809-ATS-010-handoff.md` |
| Reviewer evidence | `deliverables/user/WI-20260809-ATS-006-summary.md` |
| Branch policy | Current branch only; no commit or push |

## Outcome

All three required contract repairs are implemented and covered by focused controller, service, and repository tests. The final combined focused test run passed 111 tests with zero failures or errors. Scoped diff checks reported no whitespace errors.

## Requirement Evidence

| Requirement | Implementation evidence | Test evidence |
|---|---|---|
| Public Track pagination validates before `PageRequest` | `TrackService#getTracks` invokes `validatePublicSearchPage`; invalid values throw `INVALID_ARGUMENT` before repository access | `TrackServiceTest` covers page `0`/negative, size `0`/negative/`101`, no repository interactions, and valid size `100`; `TrackControllerTest` covers the same HTTP boundary and 1-based response metadata |
| Playlist list/detail/reorder share the active set | `PlaylistTrackRepository` exposes active count/detail queries; `PlaylistService` validates reorder against active memberships and appends retained inactive rows deterministically | `PlaylistServiceTest` covers mixed count, detail, exact active-ID reorder, inactive rejection, reactivation-at-end, and remove/compact consistency |
| Album public count and `trackCount` sort are active-only | `AlbumTrackRepository#countActiveMapByAlbums` supplies public counts; `AlbumRepository#findAllActiveOrderByTrackCount` filters joined Tracks by active status | `AlbumServiceTest` covers active public mapping/detail; `ActiveMembershipRepositoryTest` verifies mixed active/inactive count and ordering at the repository boundary |
| Admin/all-membership totals remain separate | Existing `countByAlbum`, `countByAlbumIn`, and `countMapByAlbums` paths remain available and unchanged for non-public aggregate semantics | Repository integration coverage asserts the all-membership map remains distinct from the active public map |

## Ordering And Query Semantics

### Track

1. Validate `page >= 1`.
2. Validate `1 <= size <= 100`.
3. Build `PageRequest` with zero-based internal page `page - 1`.
4. Return the documented 1-based value in `pageInfo.page`.

All validation occurs at the service boundary. The controller only delegates and serializes the service result.

### Playlist

- Active visibility is defined by membership rows whose related Track has `isActive = true`.
- Public/owner list count and detail projection use that same active set.
- The all-membership fetch is ordered by `trackOrder ASC, trackId ASC` to make inactive retention deterministic.
- Reorder input must contain each visible active Track ID exactly once and no inactive ID.
- Active rows receive request positions `0..activeCount-1`.
- Inactive rows retain deterministic relative order and receive subsequent positions.
- The resulting order values are unique and contiguous; no inactive membership is deleted.
- Remove compaction uses the same deterministic all-membership ordering for retained rows.

### Album

- Public count maps filter inactive Tracks.
- Public `trackCount` ordering uses an active-filtered left join, preserving active Albums with zero active Tracks.
- Detail projection remains active-only.
- All-membership count methods remain separate for administrative/mutation semantics.

## Changed Files

| Area | Paths |
|---|---|
| Track implementation | `src/main/java/com/atstudio/atstudio/service/TrackService.java` |
| Playlist implementation | `src/main/java/com/atstudio/atstudio/repository/PlaylistTrackRepository.java`; `src/main/java/com/atstudio/atstudio/service/PlaylistService.java` |
| Album implementation | `src/main/java/com/atstudio/atstudio/repository/AlbumRepository.java`; `src/main/java/com/atstudio/atstudio/repository/AlbumTrackRepository.java`; `src/main/java/com/atstudio/atstudio/service/AlbumService.java` |
| Track tests | `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java`; `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java` |
| Playlist tests | `src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java` |
| Album/repository tests | `src/test/java/com/atstudio/atstudio/service/AlbumServiceTest.java`; `src/test/java/com/atstudio/atstudio/repository/ActiveMembershipRepositoryTest.java` |
| Current-state docs | `docs/design/api-spec.md`; `docs/design/usecase/sound-track.md`; `docs/design/usecase/sound-playlist.md`; `docs/design/usecase/sound-album.md`; `docs/SR/SR-100.md`; `docs/SR/SR-101.md` |
| WI outputs | `deliverables/user/WI-20260809-ATS-010-summary.md`; `deliverables/agent/WI-20260809-ATS-010-evidence-pack.md` |

The repository began with a substantial dirty worktree, including pre-existing changes in some touched files. No unrelated changes were reverted, reformatted, staged, committed, or pushed.

## Verification Ledger

### Focused Track verification

```text
.\gradlew.bat test --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest"
```

- Initial result: failed during test compilation because a Mockito `findAll` call was ambiguous across repository overloads.
- Correction: the test now captures an explicitly typed `Pageable`.
- Rerun result: `BUILD SUCCESSFUL in 45s`.

### Focused Playlist verification

```text
.\gradlew.bat test --tests "com.atstudio.atstudio.service.PlaylistServiceTest"
```

- Result: `BUILD SUCCESSFUL in 24s`.

### Focused Album and repository verification

```text
.\gradlew.bat test --tests "com.atstudio.atstudio.service.AlbumServiceTest" --tests "com.atstudio.atstudio.repository.ActiveMembershipRepositoryTest"
```

- Result: `BUILD SUCCESSFUL in 46s`.
- Database scope: existing non-destructive in-memory H2 `@DataJpaTest` profile only.

### Final combined focused verification

```text
.\gradlew.bat test --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.service.PlaylistServiceTest" --tests "com.atstudio.atstudio.service.AlbumServiceTest" --tests "com.atstudio.atstudio.repository.ActiveMembershipRepositoryTest" --tests "com.atstudio.atstudio.repository.PlayableTrackQueryCountTest" --tests "com.atstudio.atstudio.service.AlbumPlaylistMutationLockContractTest"
```

- Result: `BUILD SUCCESSFUL in 50s`.
- Results: 111 tests, 0 skipped, 0 failures, 0 errors.
- Included contract guards: playable-query count and album/playlist mutation-lock tests.

### Diff checks

- Scoped tracked-file `git diff --check`: exit `0`, no whitespace errors.
- Git emitted CRLF-to-LF normalization warnings for existing working-copy files; these were warnings, not check failures.
- New untracked repository test no-index whitespace check: passed. The raw Git exit `1` represented expected content difference from an empty file and was normalized only after confirming no whitespace diagnostics.

### Reporting correction

An auxiliary XML-count command initially assumed the wrong package path for `AlbumPlaylistMutationLockContractTest`, emitted path errors, and printed an invalid total of 114. No test was run or modified by that helper. A corrected command discovered all seven result paths first and confirmed the authoritative total: 111 tests, 0 skipped, 0 failures, 0 errors.

## Not Run

- Full Gradle test suite, prohibited by the focused-verification scope.
- Global documentation validation, outside the requested focused test and diff-check scope.
- External or retained local database verification.
- MySQL execution-plan profiling.

## Risks

1. Active aggregate JPQL behavior is integration-tested through Hibernate/H2, but production MySQL query plans were not profiled.
2. Existing malformed legacy membership orders are normalized only when reorder/remove compaction executes; no data backfill was authorized or performed.
3. The dirty worktree contains co-located predecessor changes, so rollback must be hunk-scoped and must not replace whole files.

## Rollback

Apply an inverse patch only to the WI-010 hunks in the listed production, test, and documentation files. Remove the newly added `ActiveMembershipRepositoryTest` and these two WI deliverables if the WI itself is withdrawn. Preserve all unrelated dirty-worktree content. No schema or data rollback is necessary because this work item performed no schema/data mutation.

## WI-029 Status

The implementation and focused evidence resolve the findings that blocked `WI-20260808-ATS-029`. WI-029 is now unblocked for independent reviewer rerun, but remains pending and must not be marked complete from this SE evidence alone.
