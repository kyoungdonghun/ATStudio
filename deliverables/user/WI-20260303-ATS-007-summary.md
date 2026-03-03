[WI-007 SUMMARY]
WI ID: WI-20260303-ATS-007
REQ: REQ-20260303-ATS-003
Agent: se
Date: 2026-03-04

## Result
Status: PASS
Tests: 556 passed / 556 total, 0 failures

## Changes

### M-1: AlbumTrackRepository @EntityGraph N+1 fix (track.user)
- `src/main/java/com/atstudio/atstudio/repository/AlbumTrackRepository.java:16`
  - `@EntityGraph(attributePaths = "track")` -> `@EntityGraph(attributePaths = {"track", "track.user"})`
  - AlbumTrackItemResponse.from() accesses track.user.nickname; without eager fetch, each track triggers an additional User SELECT.

### M-2: countByAlbum() replaces findAll().size() for count-only queries
- `src/main/java/com/atstudio/atstudio/repository/AlbumTrackRepository.java:21`
  - Added: `long countByAlbum(Album album);`
- `src/main/java/com/atstudio/atstudio/service/AlbumService.java:65,99,125`
  - 3 call sites replaced: `findAllByAlbumOrderByTrackOrder(album).size()` -> `(int) countByAlbum(album)`
  - Generates `SELECT COUNT(*)` instead of loading all AlbumTrack + Track entities into memory.
- `src/test/java/com/atstudio/atstudio/service/AlbumServiceTest.java:75-76,124-125,160`
  - Mock stubs updated from `findAllByAlbumOrderByTrackOrder` -> `countByAlbum` (returning 0L)
  - addTrack test retains both mocks: countByAlbum for nextOrder + findAllByAlbumOrderByTrackOrder for getAlbum() detail call.
