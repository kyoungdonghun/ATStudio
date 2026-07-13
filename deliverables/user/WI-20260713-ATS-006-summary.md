# WI-20260713-ATS-006 Protected Media Reliability Summary

## Outcome

- Independently reviewed the current WI-003 protected-media implementation and its focused tests.
- Found and fixed one boundary defect: a one-byte original fallback exposed its complete resource. Such resources now expose zero public bytes and return `416` from the stream endpoint.
- Added endpoint-level checks for public/admin metadata separation, suffix Range handling, malformed and multiple Range rejection, and repeated out-of-bound requests.
- Confirmed that dedicated preview Range behavior, static original-route denial, and authenticated subscriber download remain functional.

## Verification

- Focused backend command:
  - `.\gradlew.bat test --no-daemon --max-workers=1 --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.controller.SecurityFilterChainTest" --tests "com.atstudio.atstudio.service.DownloadServiceTest"`
  - Result: `BUILD SUCCESSFUL in 53s`; 70 tests passed, 0 failed, 0 errors, 0 skipped.
- `npm run typecheck`: passed.
- `npm run lint`: passed with zero warnings.
- `npm test -- --run`: 14 test files and 51 tests passed.
- `npx prettier --check "src/api/tracks.ts"`: passed.
- Scoped `git diff --check`: passed.

The backend tests used mocks and in-memory H2. No stored file was moved, no live database data was mutated, no runtime log was edited, and no external service was called.

## WI-006 Changed Paths

- `src/main/java/com/atstudio/atstudio/service/TrackService.java`
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`
- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java`
- `deliverables/user/WI-20260713-ATS-006-summary.md`
- `deliverables/agent/WI-20260713-ATS-006-evidence-pack.md`

## Residual Risk

- Original-backed compatibility previews estimate elapsed time from byte ratio, so variable-bitrate media can differ from the intended time boundary. The byte boundary still prevents full-original retrieval.
- Existing originals remain under the current storage root; the explicit Spring Security denial remains the immediate static-route enforcement boundary.

## Rollback

- Revert only the one-byte fallback correction, the WI-006 test additions, and these two WI-006 outputs. Do not revert the underlying WI-003 implementation or any mail, billing, documentation, or runtime-log paths.
