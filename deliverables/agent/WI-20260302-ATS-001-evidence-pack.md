# WI-20260302-ATS-001 Evidence Pack

## WI Info
- **REQ:** REQ-20260302-ATS-011
- **Agent:** se
- **Blocks:** WI-20260302-ATS-004
- **Date:** 2026-03-02

## Change Manifest

### Files Modified

| File | Lines Changed | Change Type |
|------|--------------|-------------|
| `src/main/java/com/atstudio/atstudio/service/NoticeService.java` | L13 (import), L70-97 (methods + helper) | Modified |
| `src/main/java/com/atstudio/atstudio/controller/NoticeController.java` | L54-71 (update/delete endpoints) | Modified |
| `src/main/java/com/atstudio/atstudio/controller/TestController.java` | Entire file | Deleted |
| `src/test/java/com/atstudio/atstudio/service/NoticeServiceTest.java` | L57, L103-170 (tests + helpers) | Modified |
| `src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java` | L189-210, L258-272 (new tests) | Modified |
| `src/test/java/com/atstudio/atstudio/controller/NoticeControllerTest.java` | L114, L143 (mock stubs) | Modified |

### Files NOT Modified (Already Correct)

| File | Reason |
|------|--------|
| `src/main/java/com/atstudio/atstudio/service/PlaylistService.java` | `getOwnedPlaylist()` already enforces ownership at L198-208 |
| `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java` | No change needed -- `/test` and `/health` were under `anyRequest().permitAll()` (non-`/api/` paths), not explicit permitAll rules |
| `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java` | `RESOURCE_NOT_ACCESS` (HTTP 403) already exists at L17-20 |

## Patch Rationale

### CR-A-004 (PlaylistService)
The WI handoff described a missing ownership check in PlaylistService. Upon inspection, the `getOwnedPlaylist()` private helper (L198-208) already validates `playlist.getUser().getId().equals(userId)` and throws `RESOURCE_NOT_ACCESS`. Both `updatePlaylist()` (L128) and `deletePlaylist()` (L184) call this helper. No production code change was needed. Two tests were added to explicitly verify this behavior for update and delete scenarios.

### CR-C-006 (NoticeService)
`updateNotice()` and `deleteNotice()` had no caller identity check. While SecurityConfig restricts these endpoints to ADMIN role at the HTTP layer (`hasRole('ADMIN')`), the service layer lacked defense-in-depth. The fix:
1. Added `CustomUserDetails userDetails` parameter to both methods.
2. Created `validateNoticeOwnership()` helper that:
   - Returns immediately if `userDetails.getRole() == UserRole.ADMIN` (ADMIN bypass).
   - Throws `RESOURCE_NOT_ACCESS` if `notice.getUser().getId()` does not match `userDetails.getId()`.
3. Updated `NoticeController` to pass `@AuthenticationPrincipal CustomUserDetails` to both endpoints.
4. Updated `NoticeControllerTest` mock stubs to match new 3-arg signatures.

### CR-C-008/CR-P-003 (TestController)
`TestController` exposed `GET /test` and `GET /health` without authentication. These paths are not under `/api/` so they matched `anyRequest().permitAll()` in SecurityConfig (the Thymeleaf static resource catch-all). The entire file was deleted. No SecurityConfig rule cleanup was needed since there were no explicit rules for these paths.

## Ownership Check Snippets

### NoticeService.validateNoticeOwnership()
```java
private void validateNoticeOwnership(Notice notice, CustomUserDetails userDetails) {
    if (userDetails.getRole() == UserRole.ADMIN) {
        return;
    }
    if (!notice.getUser().getId().equals(userDetails.getId())) {
        throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
    }
}
```

### PlaylistService.getOwnedPlaylist() (pre-existing, unchanged)
```java
private Playlist getOwnedPlaylist(Long playlistId, Long userId) {
    Playlist playlist = playlistRepository.findById(playlistId)
            .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    if (!playlist.getUser().getId().equals(userId)) {
        throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
    }
    if (!playlist.isActive()) {
        throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND);
    }
    return playlist;
}
```

## Tests Added

| Test Class | Test Method | Scenario |
|-----------|-------------|----------|
| `PlaylistServiceTest` | `updatePlaylist_notOwner()` | Non-owner update -> RESOURCE_NOT_ACCESS |
| `PlaylistServiceTest` | `deletePlaylist_notOwner()` | Non-owner delete -> RESOURCE_NOT_ACCESS |
| `NoticeServiceTest` | `updateNotice_adminCanUpdateOtherNotice()` | ADMIN updates other's notice -> success |
| `NoticeServiceTest` | `updateNotice_nonAdminCannotUpdateOtherNotice()` | Non-ADMIN updates other's notice -> RESOURCE_NOT_ACCESS |
| `NoticeServiceTest` | `deleteNotice_adminCanDeleteOtherNotice()` | ADMIN deletes other's notice -> success |
| `NoticeServiceTest` | `deleteNotice_nonAdminCannotDeleteOtherNotice()` | Non-ADMIN deletes other's notice -> RESOURCE_NOT_ACCESS |

## Test Results
- **Target test classes:** NoticeServiceTest (11 tests), PlaylistServiceTest (14 tests), NoticeControllerTest (10 tests) -- all PASSED.
- **Full suite:** 490 tests, 3 pre-existing AuthService failures (unrelated).

## Audit ID Mapping Note
The WI handoff packet referenced CR-A-004 and CR-C-006 for Playlist/Notice ownership, but the backend-audit-report.md maps these IDs differently:
- CR-A-004 in audit = "Soft-delete track does not physically delete track_tags records" (TrackService)
- CR-C-006 in audit = "CompanyCertification.process() applies status transitions without validation"

The MEMORY.md tracks the Playlist/Notice ownership issues under different labels. This WI addressed the ownership issues as described in the WI handoff packet regardless of audit ID discrepancy.

## Follow-up
- WI-20260302-ATS-004 is unblocked (this WI was a dependency).
