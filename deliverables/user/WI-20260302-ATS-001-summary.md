# WI-20260302-ATS-001 Summary

## WI Info
- **REQ:** REQ-20260302-ATS-011
- **Agent:** se
- **Date:** 2026-03-02

## Change Summary

### CR-A-004 -- PlaylistService Ownership Check
- **Status:** Already implemented (no code change needed).
- PlaylistService already enforces ownership via the `getOwnedPlaylist()` helper, which is called by `updatePlaylist()`, `deletePlaylist()`, `getPlaylistDetail()`, `addTrack()`, `reorderTracks()`, and `removeTrack()`.
- Added 2 new tests to verify ownership rejection on update and delete.

### CR-C-006 -- NoticeService Ownership Check
- **Status:** Fixed.
- `updateNotice()` and `deleteNotice()` now require `CustomUserDetails` parameter and validate that the caller is either the notice author or has ADMIN role.
- Non-owner non-ADMIN callers receive `RESOURCE_NOT_ACCESS` (HTTP 403).
- ADMIN users can update/delete any notice (bypass).
- NoticeController updated to pass `@AuthenticationPrincipal CustomUserDetails` to both methods.
- NoticeControllerTest mock stubs updated for new method signatures.
- Added 4 new service-level tests:
  - ADMIN can update other user's notice
  - Non-ADMIN cannot update other user's notice (403)
  - ADMIN can delete other user's notice
  - Non-ADMIN cannot delete other user's notice (403)

### CR-C-008/CR-P-003 -- TestController Removal
- **Status:** Fixed.
- `TestController.java` deleted entirely. The `/test` and `/health` endpoints are no longer exposed.
- No SecurityConfig changes needed (the endpoints fell through to `anyRequest().permitAll()` because they were not under `/api/`).

## Risk Assessment
- **Low risk.** All changes are additive security checks or file removal.
- PlaylistService was already correct; only test coverage was added.
- NoticeService ownership check is defense-in-depth (SecurityConfig already restricts PUT/DELETE to ADMIN via `hasRole('ADMIN')`).
- TestController removal has zero side effects -- no other code references it.

## Verification
- Modified test classes (NoticeServiceTest, PlaylistServiceTest, NoticeControllerTest): **BUILD SUCCESSFUL**, all tests pass.
- Full test suite: 490 tests, 3 failures (pre-existing AuthService refresh token tests, unrelated to this WI).
