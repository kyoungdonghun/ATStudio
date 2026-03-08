# WI-20260308-ATS-034 Evidence Pack

## WI Reference

- **WI ID:** WI-20260308-ATS-034
- **REQ:** REQ-20260308-ATS-011
- **Agent:** SE
- **Status:** COMPLETE

## Change Pointers

### 1. TrackCreateRequest.java

- **Path:** `src/main/java/com/atstudio/atstudio/dto/track/TrackCreateRequest.java`
- **Change:** Removed `MultipartFile audioFile` (line 34), `MultipartFile thumbnail` (line 36), and `org.springframework.web.multipart.MultipartFile` import (line 10)
- **Result:** DTO now contains only structural data fields (title, bpm, tonality, description, tagIds)

### 2. TrackUpdateRequest.java

- **Path:** `src/main/java/com/atstudio/atstudio/dto/track/TrackUpdateRequest.java`
- **Change:** Removed `MultipartFile audioFile` (line 28), `MultipartFile thumbnail` (line 30), and `org.springframework.web.multipart.MultipartFile` import (line 8)
- **Result:** DTO now contains only structural data fields (title, bpm, tonality, description, tagIds, isActive)

### 3. TrackController.java

- **Path:** `src/main/java/com/atstudio/atstudio/controller/TrackController.java`
- **Change (createTrack):** Added `@RequestPart MultipartFile audioFile` and `@RequestPart(required = false) MultipartFile thumbnail` as separate parameters. Updated service call to `trackService.createTrack(request, audioFile, thumbnail, userDetails)`.
- **Change (updateTrack):** Added `@RequestPart(required = false) MultipartFile audioFile` and `@RequestPart(required = false) MultipartFile thumbnail` as separate parameters. Updated service call to `trackService.updateTrack(trackId, request, audioFile, thumbnail)`.
- **Added import:** `org.springframework.web.multipart.MultipartFile`

### 4. TrackService.java

- **Path:** `src/main/java/com/atstudio/atstudio/service/TrackService.java`
- **Change (createTrack):** Signature changed from `(TrackCreateRequest, CustomUserDetails)` to `(TrackCreateRequest, MultipartFile audioFile, MultipartFile thumbnail, CustomUserDetails)`. Body updated: `request.getAudioFile()` -> `audioFile`, `request.getThumbnail()` -> `thumbnail`.
- **Change (updateTrack):** Signature changed from `(Long, TrackUpdateRequest)` to `(Long, TrackUpdateRequest, MultipartFile audioFile, MultipartFile thumbnail)`. Body updated: `request.getAudioFile()` -> `audioFile`, `request.getThumbnail()` -> `thumbnail`.
- **Added import:** `org.springframework.web.multipart.MultipartFile`
- **Constraint:** `storageService.store()` / `storageService.delete()` call logic unchanged.

### 5. TrackControllerTest.java

- **Path:** `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java`
- **Change (line 65):** `given(trackService.createTrack(any(), any()))` -> `given(trackService.createTrack(any(), any(), any(), any()))`

### 6. TrackServiceTest.java

- **Path:** `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`
- **Changes:**
  - `createTrack_success_audioFileOnly`: Removed `request.setAudioFile()`, created local `MultipartFile audioFile`, call changed to `createTrack(request, audioFile, null, userDetails)`
  - `createTrack_success_withThumbnailAndTags`: Removed `request.setAudioFile()` and `request.setThumbnail()`, created local variables, call changed to `createTrack(request, audioFile, thumbnail, userDetails)`
  - `createTrack_fail_userNotFound`: Same pattern as above
  - `updateTrack_success_metadataOnly`: Call changed to `updateTrack(1L, request, null, null)`
  - `updateTrack_success_withNewAudioFile`: Removed `request.setAudioFile()`, created local `MultipartFile newAudioFile`, call changed to `updateTrack(1L, request, newAudioFile, null)`
  - `updateTrack_success_withNewTagIds`: Call changed to `updateTrack(1L, request, null, null)`

## Acceptance Criteria Verification

| Criterion | Status |
|-----------|--------|
| TrackCreateRequest has no MultipartFile fields | PASS |
| TrackUpdateRequest has no MultipartFile fields | PASS |
| TrackController.createTrack() uses @RequestPart MultipartFile audioFile + @RequestPart(required=false) MultipartFile thumbnail | PASS |
| TrackController.updateTrack() uses @RequestPart(required=false) for both files | PASS |
| TrackService.createTrack() accepts MultipartFile as separate params | PASS |
| TrackService.updateTrack() accepts MultipartFile as separate params | PASS |
| ./gradlew test passes with 0 failures | PASS |

## Test Evidence

- **Command:** `./gradlew test`
- **Result:** BUILD SUCCESSFUL in 45s (5 actionable tasks: 2 executed, 3 up-to-date)
- **Failures:** 0

## Pattern Consistency Check

| Controller | DTO file fields | @RequestPart for files |
|------------|----------------|----------------------|
| PlaylistController | None | `@RequestPart(required = false) MultipartFile thumbnail` |
| AlbumController | None | `@RequestPart(required = false) MultipartFile thumbnail` |
| TrackController (after) | None | `@RequestPart MultipartFile audioFile` + `@RequestPart(required = false) MultipartFile thumbnail` |

All three controllers now follow the same file upload pattern.

## Reproduction Steps

1. Run `./gradlew test` to verify all tests pass
2. Inspect TrackCreateRequest.java and TrackUpdateRequest.java -- no MultipartFile fields
3. Inspect TrackController.java createTrack/updateTrack -- @RequestPart parameters present
4. Inspect TrackService.java createTrack/updateTrack -- MultipartFile separate parameters
