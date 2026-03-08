# WI-20260308-ATS-034 Summary

## Change Summary

TrackController file upload pattern refactored to match the majority pattern used by PlaylistController and AlbumController.

**Before:** `TrackCreateRequest` and `TrackUpdateRequest` contained `MultipartFile audioFile` and `MultipartFile thumbnail` fields directly. Files were received via `@ModelAttribute` DTO binding.

**After:** `MultipartFile` fields removed from DTOs. Files are now received as separate `@RequestPart` parameters in controller methods, then passed individually to service methods.

## Files Modified

| File | Change |
|------|--------|
| `TrackCreateRequest.java` | Removed `audioFile`, `thumbnail` fields + `MultipartFile` import |
| `TrackUpdateRequest.java` | Removed `audioFile`, `thumbnail` fields + `MultipartFile` import |
| `TrackController.java` | Added `@RequestPart` parameters for `audioFile`/`thumbnail` in `createTrack()` and `updateTrack()` |
| `TrackService.java` | Updated `createTrack()` and `updateTrack()` signatures to accept `MultipartFile` as separate parameters |
| `TrackControllerTest.java` | Updated mock stubbing to match new 4-arg `createTrack()` signature |
| `TrackServiceTest.java` | Updated all `createTrack()` and `updateTrack()` calls to pass `MultipartFile` as separate args |

## Risk Assessment

- **Low risk.** Pure signature refactoring with no business logic change.
- `storageService.store()` call logic unchanged.
- All existing test assertions preserved.

## Verification

- `./gradlew test` : BUILD SUCCESSFUL, 0 failures
