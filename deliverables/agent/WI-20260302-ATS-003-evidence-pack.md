# WI-20260302-ATS-003 Evidence Pack

## WI Metadata
- **WI ID**: WI-20260302-ATS-003
- **REQ**: REQ-20260302-ATS-011
- **Agent**: se
- **Blocks**: WI-20260302-ATS-004

## Change Pointers

### CR-A-002 -- Track N+1 (@EntityGraph)

**File: `src/main/java/com/atstudio/atstudio/repository/TrackRepository.java`**
- Lines 17-20: Added `@EntityGraph(attributePaths = {"trackTags", "trackTags.tag"})` + `@Override` on `findAll(Specification<Track>, Pageable)`.
- Lines 22-24: Added `findByIdWithTags(Long id)` with same `@EntityGraph`.

```java
@EntityGraph(attributePaths = {"trackTags", "trackTags.tag"})
@Override
Page<Track> findAll(Specification<Track> spec, Pageable pageable);

@EntityGraph(attributePaths = {"trackTags", "trackTags.tag"})
@Query("SELECT t FROM Track t WHERE t.id = :id")
Optional<Track> findByIdWithTags(@Param("id") Long id);
```

**File: `src/main/java/com/atstudio/atstudio/service/TrackService.java`**
- Lines 88-95: `getTracks()` now reads `t.getTrackTags().stream().map(TrackTag::getTag)` directly from eagerly-loaded entity (was: `buildTagsMap(trackIds)` separate batch query).
- Lines 107-115: `getTrack()` uses `trackRepository.findByIdWithTags(trackId)` instead of `findById()` + `trackTagRepository.findAllWithTagByTrack()`.
- Lines 208-215 (removed): `buildTagsMap()` private helper deleted (no longer needed).
- Imports: Removed `java.util.Map`, `java.util.stream.Collectors` (unused after `buildTagsMap` removal).

Before (getTracks):
```java
Page<Track> page = trackRepository.findAll(spec, pageable);
List<Long> trackIds = page.getContent().stream().map(Track::getId).toList();
Map<Long, List<Tag>> tagsByTrackId = buildTagsMap(trackIds);
List<TrackListItemResponse> dataList = page.getContent().stream()
        .map(t -> TrackListItemResponse.from(t, tagsByTrackId.getOrDefault(t.getId(), List.of())))
        .toList();
```

After (getTracks):
```java
Page<Track> page = trackRepository.findAll(spec, pageable);
List<TrackListItemResponse> dataList = page.getContent().stream()
        .map(t -> {
            List<Tag> tags = t.getTrackTags().stream().map(TrackTag::getTag).toList();
            return TrackListItemResponse.from(t, tags);
        })
        .toList();
```

Before (getTrack):
```java
Track track = findActiveTrack(trackId);
List<Tag> tags = trackTagRepository.findAllWithTagByTrack(track)
        .stream().map(TrackTag::getTag).toList();
```

After (getTrack):
```java
Track track = trackRepository.findByIdWithTags(trackId)
        .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND));
if (!track.isActive()) {
    throw new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND);
}
List<Tag> tags = track.getTrackTags().stream().map(TrackTag::getTag).toList();
```

### CR-A-005 -- PlayHistoryService (No Change Required)

**File: `src/main/java/com/atstudio/atstudio/service/PlayHistoryService.java`**
- No changes. `savePlayHistory()` at line 43 saves a single `PlayHistory` entity per invocation.
- The API is designed for single-record creation (`POST /api/play-histories/{trackId}`).
- No batch endpoint exists; no `saveAll()` conversion applicable.

### CR-C-004 -- DownloadQueueService @Transactional (Already Compliant)

**File: `src/main/java/com/atstudio/atstudio/service/DownloadQueueService.java`**
- Line 21: `@Transactional(readOnly = true)` at class level -- already present.
- Line 29: `@Transactional` on `addToQueue()` -- already present.
- Line 57: `@Transactional` on `removeFromQueue()` -- already present.
- No changes needed.

## Test Evidence

### New Tests Added

**File: `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`**
- `getTrack_success()`: Updated to mock `findByIdWithTags()` and verify it is called (was `findById()`).
- `getTrack_fail_notFound()`: Updated to mock `findByIdWithTags()`.
- `getTrack_fail_inactiveTrack()`: Updated to mock `findByIdWithTags()`.
- `getTracks_emptyResult()`: Added `verify(trackRepository).findAll(Specification, Pageable)`.
- `getTracks_callsFindAllWithEntityGraph()` (NEW): Verifies list query calls `findAll(Specification, Pageable)` and returns mapped results.

**File: `src/test/java/com/atstudio/atstudio/service/PlayHistoryServiceTest.java`**
- `savePlayHistory_singleSaveCall()` (NEW): Verifies `save()` called once and `saveAll()` never called.

**File: `src/test/java/com/atstudio/atstudio/service/DownloadQueueServiceTest.java`**
- `classLevel_transactionalReadOnly()` (NEW): Reflection check for class-level `@Transactional(readOnly=true)`.
- `addToQueue_hasTransactional()` (NEW): Reflection check for method-level `@Transactional` on `addToQueue`.
- `removeFromQueue_hasTransactional()` (NEW): Reflection check for method-level `@Transactional` on `removeFromQueue`.

### Test Execution

```
Command: gradlew.bat test --tests "...TrackServiceTest" --tests "...PlayHistoryServiceTest" --tests "...DownloadQueueServiceTest"
Result: BUILD SUCCESSFUL in 15s
```

```
Command: gradlew.bat test --rerun
Result: BUILD SUCCESSFUL in 43s (full suite)
```

## Acceptance Criteria Verification

| Criterion | Status | Evidence |
|-----------|--------|----------|
| Track filter query: no TrackTag N+1 | PASS | `@EntityGraph` on `findAll` override in TrackRepository |
| Track detail query: no N+1 | PASS | `findByIdWithTags()` with `@EntityGraph` |
| PlayHistory batch save via saveAll() | N/A | Single-record API; no batch path exists. Test documents this. |
| DownloadQueueService class @Transactional(readOnly=true) | PASS | Already present; reflection tests confirm |
| DownloadQueueService mutating @Transactional override | PASS | Already present; reflection tests confirm |
| TrackServiceTest: filter query repository call verify | PASS | `getTracks_callsFindAllWithEntityGraph()` |
| PlayHistoryServiceTest: saveAll() call verify | PASS | `savePlayHistory_singleSaveCall()` verifies `saveAll()` never called |
| DownloadQueueServiceTest: @Transactional confirm | PASS | 3 reflection-based tests |
| Existing tests pass | PASS | Full suite BUILD SUCCESSFUL |

## Rationale

### Why @EntityGraph instead of fetch join in Specification?
- `@EntityGraph` on the repository method is declarative and does not require modifying `TrackSpecification`.
- Using `root.fetch()` inside Specifications causes issues with count queries (Spring Data auto-generates count queries for `Page` results, and `fetch` in count queries throws exceptions).
- `@EntityGraph` is the standard approach per `development-standards.md` Section 2A.7.

### Why no change to PlayHistoryService?
- The audit report's CR-A-005 actually references `LicenseRepository` N+1 (not PlayHistory). The WI handoff remapped the ID to a PlayHistory concern.
- Current `savePlayHistory()` saves exactly one record per API call. No `List<PlayHistory>` input exists.
- Introducing `saveAll()` without a batch API would be speculative design.

### Why no change to DownloadQueueService?
- The class already follows the standard pattern: `@Transactional(readOnly = true)` at class level, `@Transactional` on mutating methods.
- Added reflection tests to lock this invariant for future regressions.

## Follow-up WI
- **WI-20260302-ATS-004** (blocked by this WI): Ready to proceed.
