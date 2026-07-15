# WI-20260715-ATS-018 Summary

## Outcome

The public Track stream now serves the complete active Track audio resource through `GET /api/tracks/{trackId}/stream`. The previous dedicated-preview selection, bounded original prefix, and open-ended Range chunk cap were removed.

## Acceptance Results

- [x] Requests without `Range` receive the full resource representation.
- [x] Start/end, open-ended, and suffix Ranges resolve against the full resource length.
- [x] Malformed, multiple, zero-suffix, reversed, overflowing, and out-of-bounds Ranges return `416` with the full resource length.
- [x] Public Track DTOs still return `audioFile: null`.
- [x] Direct `/uploads/tracks/audio/**` access remains denied for anonymous, USER, ADMIN, encoded, and traversal requests.
- [x] Subscriber download, daily limit, history, and license behavior is unchanged.

## Verification

- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.controller.SecurityFilterChainTest" --tests "com.atstudio.atstudio.service.DownloadServiceTest"` -> passed, 70 tests, 0 failures, 0 errors, 0 skipped.
- `.\gradlew.bat compileJava compileTestJava` -> `BUILD SUCCESSFUL`.
- `git diff --check` -> passed; only existing LF-to-CRLF working-copy warnings were reported.

## Scope Boundary

Only Track stream backend behavior, focused backend tests, and the required WI deliverables were changed. Frontend files, current-state documents, storage layout, database schema/data, and download policy were not changed.
