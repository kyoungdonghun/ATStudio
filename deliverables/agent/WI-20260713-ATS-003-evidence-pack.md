# Evidence Pack: WI-20260713-ATS-003

## Summary

- Removed public original-audio key and route exposure while preserving bounded preview Range behavior and protected subscriber download.

## Scope / DoD Check

- [x] Public track detail returns `audioFile: null`.
- [x] Admin create, update, and detail responses retain the original storage key.
- [x] Anonymous, USER, and ADMIN requests cannot retrieve `/uploads/tracks/audio/**`.
- [x] Dedicated preview resources retain normal Range behavior.
- [x] Original-backed fallback uses the smaller of 30 seconds and 50 percent of duration; unknown duration uses 25 percent.
- [x] Multi-byte originals always retain at least one byte outside the public boundary.
- [x] No-Range requests return only the bounded prefix; out-of-bound Range starts return `416`.
- [x] Range computation is O(1) and does not copy a complete media resource into memory.
- [x] Subscriber download tests preserve original-resource resolution and existing checks.
- [x] Focused backend tests and relevant frontend checks pass.
- [x] No stored-file move, database mutation, runtime-log edit, or external service call occurred.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and protected marketplace asset baseline |
| 0 | `docs/standards/development-standards.md` | Java, React, test, and traceability standards |
| 0 | `docs/standards/documentation-standards.md` | Evidence document format baseline |
| 0 | `docs/standards/glossary.md` | Canonical Track, Subscription, and License terminology |
| 1 | `docs/policies/security-policy.md` | Default-deny protected-resource policy |
| 1 | `docs/policies/quality-gates.md` | Regression and reproducibility requirements |
| 2 | `docs/design/p0-release-blocker-remediation-design.md` | Canonical public/admin media and bounded fallback contract |
| 2 | `docs/design/api-spec.md` | Existing Track API and Range baseline |
| 2 | `docs/design/usecase/sound-track.md` | Existing play and subscriber-download flow baseline |
| Context | `deliverables/user/REQ-20260713-ATS-001.md` | Approved P0 scope and success criteria |
| Context | `deliverables/agent/WI-20260713-ATS-002-evidence-pack.md` | Approved design evidence and follow-up pointer |
| Handoff | `deliverables/agent/WI-20260713-ATS-003-handoff.md` | Ownership, DoD, output, and rollback contract |

Injection source: WI-003 handoff `INPUT POINTERS` plus repository-mandatory Tier 0 documents; assignee `se`; task type `security/implementation`.

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/dto/track/TrackResponse.java:29-51`
  - Defines explicit public and admin factories; only the admin factory copies `Track.audioFile`.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:130-154`
  - Uses the public DTO factory for public detail and returns a resource with its public representation length.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:257-271`
  - Computes the proportional fallback in O(1), including the unknown-duration quarter and one-private-byte invariant.
- `src/main/java/com/atstudio/atstudio/controller/TrackController.java:97-165`
  - Builds `200`, `206`, and `416` responses against the public length and clamps Range ends to that boundary.
- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:80`
  - Denies every method under the original-audio static route before `anyRequest().permitAll()`.
- `frontend/src/api/tracks.ts:6-28`
  - Models public `audioFile` as nullable and admin detail as non-null.
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java:229-349`
  - Covers public/admin DTO separation, dedicated preview length, both duration branches, unknown duration, and private-byte preservation.
- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java:139-196`
  - Covers preview Range preservation, bounded no-Range body, `416`, and end clamping.
- `src/test/java/com/atstudio/atstudio/controller/SecurityFilterChainTest.java:89-110`
  - Covers anonymous, USER, and ADMIN static original-audio denial.
- `src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java:50-71`
  - Confirms the successful subscriber path still loads `tracks/audio/test.mp3` after entitlement, history, and license operations.
- `build/test-results/test/TEST-com.atstudio.atstudio.service.TrackServiceTest.xml`
  - Focused result: 23 tests, 0 failures, 0 errors, 0 skipped.
- `build/test-results/test/TEST-com.atstudio.atstudio.controller.TrackControllerTest.xml`
  - Focused result: 15 tests, 0 failures, 0 errors, 0 skipped.
- `build/test-results/test/TEST-com.atstudio.atstudio.controller.SecurityFilterChainTest.xml`
  - Focused result: 15 tests, 0 failures, 0 errors, 0 skipped.
- `build/test-results/test/TEST-com.atstudio.atstudio.service.DownloadServiceTest.xml`
  - Focused result: 10 tests, 0 failures, 0 errors, 0 skipped.

## Commands & Outputs

- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.controller.SecurityFilterChainTest" --tests "com.atstudio.atstudio.service.DownloadServiceTest"`
  - RED result: `compileTestJava` failed on 8 expected unresolved `TrackService.StreamResource` references before production implementation.
  - First GREEN attempt: production and tests compiled, but a concurrent Gradle run removed an in-progress binary result file and caused `NoSuchFileException`; no test assertion failed.
- `.\gradlew.bat test --no-daemon --max-workers=1 --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.controller.SecurityFilterChainTest" --tests "com.atstudio.atstudio.service.DownloadServiceTest"`
  - Final result: `BUILD SUCCESSFUL in 31s`; 63 passed, 0 failed, 0 errors, 0 skipped.
  - Suite counts: TrackService 23, TrackController 15, SecurityFilterChain 15, DownloadService 10.
- `npm run typecheck`
  - Final result: passed (`tsc --noEmit`, exit 0).
- `npm run lint`
  - Final result: passed (`eslint src --ext .ts,.tsx --max-warnings 0`, exit 0).
- `npm test`
  - Result: 14 test files passed; 51 tests passed; duration 6.63s.
- `npx prettier --check "src/api/tracks.ts"`
  - Initial result: reported the changed file required formatting.
  - After formatting only that owned file, final result: all matched files use Prettier code style.
- Scoped `git diff --check` over the nine owned product/test paths
  - Result: exit 0 with no whitespace errors; Git emitted only configured LF-to-CRLF conversion warnings.

The Spring tests used H2 and mocks. They did not move or rewrite media files and did not call SMTP, Toss, or another external service.

## Risks / Rollback

- Risk: Original-backed compatibility preview estimates elapsed time from byte ratio; variable-bitrate media can differ from the estimated duration boundary. It still prevents complete-original retrieval.
- Residual scope: Existing original files remain under the current storage root. Security denial is the immediate enforcement boundary until a separately approved physical migration.
- Rollback: Revert only the nine owned product/test files and the two WI-003 output files. No stored-file or database rollback is needed.

## Follow-ups

- WI-20260713-ATS-006 can independently verify protected-media behavior and cross-role regression coverage.
- The documentation WI should update the stale full-original fallback wording in `api-spec.md` and `sound-track.md` to match the canonical P0 design.
