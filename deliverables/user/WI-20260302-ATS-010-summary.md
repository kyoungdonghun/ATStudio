[WI-010 SUMMARY]
WI ID: WI-20260302-ATS-010
Status: COMPLETED
Completed: 2026-03-03

## Changes

**M-3 Fix — LicenseRepository @EntityGraph**
- `LicenseRepository.java:19,22` — `@EntityGraph(attributePaths = "track")` 추가
  - `findAllByUser(User, Pageable)`: N+1 → 즉시 로딩
  - `findAllByUser_Id(Long, Pageable)`: N+1 → 즉시 로딩

**M-4 Fix — PlaylistService trackCount N+1 → batch query**
- `PlaylistTrackRepository.java:19-20` — `countByPlaylistIdIn(@Param List<Long>)` JPQL 배치 쿼리 추가
- `PlaylistService.java:69-82` — `getMyPlaylists()` 내 M개 단건 count 쿼리 → 단일 GROUP BY batch 쿼리

## Issues Fixed
- M-3: License 목록 조회 시 track LAZY 로딩 N+1 → @EntityGraph 즉시 로딩
- M-4: Playlist 목록 조회 시 trackCount M+1 쿼리 → 단일 배치 쿼리로 개선

## Test Results
BUILD SUCCESSFUL, 534 tests, 0 failures
