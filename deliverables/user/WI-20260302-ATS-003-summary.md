# WI-20260302-ATS-003 Summary

## Change Summary

### CR-A-002 -- Track N+1 Query Prevention
- **TrackRepository**: Added `@EntityGraph(attributePaths = {"trackTags", "trackTags.tag"})` on `findAll(Specification, Pageable)` override and new `findByIdWithTags(Long id)` method.
- **TrackService.getTracks()**: Now reads tags directly from eagerly-loaded `track.getTrackTags()` instead of a separate batch query (`buildTagsMap` removed).
- **TrackService.getTrack()**: Uses `findByIdWithTags()` instead of `findById()` + separate `findAllWithTagByTrack()`.
- **Result**: Both list and detail queries load Track + TrackTag + Tag in a single query via `@EntityGraph`.

### CR-A-005 -- PlayHistoryService saveAll() Assessment
- **Finding**: `PlayHistoryService.savePlayHistory()` only saves a single PlayHistory record per invocation. There is no batch save path in the current API design.
- **Action**: No code change needed. Added a test (`savePlayHistory_singleSaveCall`) that explicitly verifies `save()` is called once and `saveAll()` is never called, documenting the design intent.

### CR-C-004 -- DownloadQueueService @Transactional Verification
- **Finding**: `DownloadQueueService` already has `@Transactional(readOnly = true)` at class level (line 21) and `@Transactional` on `addToQueue()` (line 29) and `removeFromQueue()` (line 57).
- **Action**: No code change needed. Added 3 reflection-based tests confirming annotation presence and correctness.

## Risk Assessment
- **Low risk**: `@EntityGraph` on `findAll` with `@OneToMany` may produce Hibernate "HHH90003004" warning about in-memory pagination for large result sets. Acceptable for current page sizes (20-50).
- **No breaking changes**: Method signatures unchanged; all existing tests pass.

## Test Results
- **3 target test classes**: TrackServiceTest, PlayHistoryServiceTest, DownloadQueueServiceTest -- all pass.
- **Full suite**: BUILD SUCCESSFUL with `--rerun` (transient filesystem issue on initial run).
- **New tests added**: 5 tests (2 TrackService, 1 PlayHistoryService, 3 DownloadQueueService).
