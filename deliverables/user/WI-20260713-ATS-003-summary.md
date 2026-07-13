# WI-20260713-ATS-003 Protected Track Media Summary

## Outcome

- Public track detail responses retain the compatible `audioFile` field but return it as `null`.
- Admin create, update, and detail responses retain the original audio storage key for the existing edit workflow.
- `/uploads/tracks/audio/**` is denied to anonymous, USER, and ADMIN requests before static-resource resolution.
- A dedicated `previewFile` retains normal byte-range behavior.
- When `previewFile` is absent, the stream endpoint exposes only the smaller of 30 seconds or 50 percent of the track duration, estimated from resource length. Unknown durations expose 25 percent.
- Requests without a Range header return only the public prefix, and ranges starting at or beyond that boundary return `416 Range Not Satisfiable`.
- Subscriber downloads continue to use the original resource through the existing authenticated entitlement, quota, history, and license flow.

## Verification

- Focused backend command:
  - `.\gradlew.bat test --no-daemon --max-workers=1 --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.controller.SecurityFilterChainTest" --tests "com.atstudio.atstudio.service.DownloadServiceTest"`
  - Result: `BUILD SUCCESSFUL in 31s`; 63 tests passed, 0 failed, 0 errors, 0 skipped.
- `npm run typecheck`: passed.
- `npm run lint`: passed with zero warnings.
- `npm test`: 14 test files and 51 tests passed.
- `npx prettier --check "src/api/tracks.ts"`: passed.
- Scoped `git diff --check`: passed with no whitespace errors.

No stored file was moved, no database data was mutated, no runtime log was touched, and no external service was called.

## Changed Paths

- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java`
- `src/main/java/com/atstudio/atstudio/dto/track/TrackResponse.java`
- `src/main/java/com/atstudio/atstudio/service/TrackService.java`
- `src/main/java/com/atstudio/atstudio/controller/TrackController.java`
- `frontend/src/api/tracks.ts`
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`
- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java`
- `src/test/java/com/atstudio/atstudio/controller/SecurityFilterChainTest.java`
- `src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java`
- `deliverables/user/WI-20260713-ATS-003-summary.md`
- `deliverables/agent/WI-20260713-ATS-003-evidence-pack.md`

## Rollback

- Revert only the nine product/test files and these two WI-003 outputs. No stored-file or database rollback is required.
