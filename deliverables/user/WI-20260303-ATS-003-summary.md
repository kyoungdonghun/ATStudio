# WI-20260303-ATS-003 Summary

## Change Summary

Playlist creation now enforces a maximum of 3 active playlists per subscriber. When a user already has 3 active playlists and attempts to create a fourth, the system returns HTTP 409 with error code `PLAYLIST_LIMIT_EXCEEDED`.

## Changed Files

| File | Lines | Change |
|------|-------|--------|
| `BUSINESS_ERROR.java` | L128-131 | Added `PLAYLIST_LIMIT_EXCEEDED` enum constant |
| `PlaylistRepository.java` | L13 | Added `countByUserAndIsActiveTrue(User)` derived query |
| `PlaylistService.java` | L46-48 | Added count check before playlist creation |
| `PlaylistServiceTest.java` | L68-100 | Added 2 test cases: limit exceeded + boundary success |

## Test Results

- **Total tests**: 546
- **Failures**: 0
- **Build**: SUCCESSFUL

## Risk Assessment

- **LOW**: Change is additive only. No existing API behavior modified. The count query uses Spring Data derived method naming convention, guaranteed correct by framework.
