[EVIDENCE PACK — WI-20260302-ATS-010]

## Changes

| File | Lines | Change |
|------|-------|--------|
| `LicenseRepository.java` | L19 | `@EntityGraph(attributePaths = "track")` added to `findAllByUser` |
| `LicenseRepository.java` | L22 | `@EntityGraph(attributePaths = "track")` added to `findAllByUser_Id` |
| `PlaylistTrackRepository.java` | L19-20 | `countByPlaylistIdIn(List<Long>)` JPQL GROUP BY batch query added |
| `PlaylistService.java` | L69-82 | `getMyPlaylists()` — replaced per-playlist `countByIdPlaylistId` loop with batch `countByPlaylistIdIn` + Map |

## Test Results
Command: `gradlew.bat clean test`
Result: BUILD SUCCESSFUL, 0 failures

## Acceptance Criteria Verification
- [x] LicenseRepository.findAllByUser() — @EntityGraph track 즉시 로딩
- [x] LicenseRepository.findAllByUser_Id() — @EntityGraph track 즉시 로딩
- [x] PlaylistService.getMyPlaylists() — 단일 batch count 쿼리 (M+1 → 2 쿼리)
- [x] 기존 License/Playlist 기능 미영향
- [x] BUILD SUCCESSFUL, 0 failures

## Rollback
`git revert <commit-hash>`
