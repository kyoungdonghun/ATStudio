# Evidence Pack: WI-20260713-ATS-006

## Summary

- Independently verified WI-003 protected-media behavior, corrected a one-byte full-original fallback defect, and expanded focused regression coverage.

## Scope / DoD Check

- [x] Public track JSON retains `audioFile` as `null`; admin track JSON retains the original storage key.
- [x] Anonymous, USER, and ADMIN requests cannot retrieve `/uploads/tracks/audio/**`.
- [x] Dedicated `preview_file` resources preserve normal Range behavior.
- [x] Original-backed no-Range responses stop at the bounded public prefix.
- [x] In-bound, open-ended, suffix, malformed, multiple, and out-of-bound Range behavior is verified.
- [x] Repeated requests cannot retrieve bytes at or beyond the public boundary.
- [x] Zero- and one-byte original fallbacks expose no complete original resource.
- [x] Subscriber download still resolves the original after existing entitlement and license checks.
- [x] Focused backend and relevant frontend checks pass.
- [x] No file move, live DB mutation, runtime-log edit, or external service call occurred.

## Independent Findings

### Finding RE-MEDIA-001: one-byte fallback exposed the complete original

- Evidence before correction: `TrackService.calculateOriginalFallbackLength()` returned `resourceLength` when the original length was one byte.
- Impact: a pathological or corrupt one-byte original could be reconstructed completely through the public fallback, violating the WI-006 absolute retrieval invariant.
- RED test: `TrackServiceTest.getStreamResource_oneByteOriginalFallbackExposesNoBytes` failed at the zero-length assertion before the production correction.
- Correction: `src/main/java/com/atstudio/atstudio/service/TrackService.java:257-260` now returns a zero public length for resources of one byte or less.
- Regression: `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java:350-362` locks the fail-closed behavior.

No additional correctness defect was found in the reviewed WI-003 media paths after the correction.

## Reference Documents

| Tier    | Document                                                  | Reason                                                          |
| ------- | --------------------------------------------------------- | --------------------------------------------------------------- |
| 0       | `docs/standards/core-principles.md`                       | Constitution, approval, transparency, and marketplace integrity |
| 0       | `docs/standards/development-standards.md`                 | Java, testing, and traceability standards                       |
| 0       | `docs/standards/documentation-standards.md`               | Evidence document standards                                     |
| 0       | `docs/standards/glossary.md`                              | Canonical Track, Subscription, and License terminology          |
| 1       | `docs/policies/security-policy.md`                        | Protected-resource and default-deny baseline                    |
| 1       | `docs/policies/quality-gates.md`                          | Reliability review and regression requirements                  |
| 2       | `docs/design/p0-release-blocker-remediation-design.md`    | Canonical MEDIA-01 through MEDIA-05 contract                    |
| 2       | `docs/design/api-spec.md`                                 | Existing Track endpoint baseline and known stale media wording  |
| 2       | `docs/design/usecase/sound-track.md`                      | Preview and subscriber-download flow baseline                   |
| Context | `deliverables/user/REQ-20260713-ATS-001.md`               | Approved P0 scope and quality gates                             |
| Context | `deliverables/agent/WI-20260713-ATS-002-evidence-pack.md` | Design decision evidence                                        |
| Context | `deliverables/agent/WI-20260713-ATS-003-evidence-pack.md` | Implementation ownership, prior checks, and risks               |
| Handoff | `deliverables/agent/WI-20260713-ATS-006-handoff.md`       | WI-006 scope, DoD, constraints, and output contract             |

Injection source: WI-006 handoff, nested WI-003 pointers, and repository-mandatory Tier 0 documents; assignee `re`; task type `testing/reliability`.

## Evidence Pointers

- Corrective production edit:
  - `src/main/java/com/atstudio/atstudio/service/TrackService.java:257-260` - zero public length for zero/one-byte original fallbacks.
- WI-006 test additions:
  - `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java:350-362` - one-byte fail-closed regression.
  - `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java:140-163` - public/admin JSON boundary.
  - `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java:220-276` - suffix, malformed, multiple, and repeated Range coverage.
- Reviewed WI-003 enforcement paths:
  - `src/main/java/com/atstudio/atstudio/dto/track/TrackResponse.java:29-55` - explicit public/admin factories.
  - `src/main/java/com/atstudio/atstudio/service/TrackService.java:130-154` - public metadata and stream-resource boundary.
  - `src/main/java/com/atstudio/atstudio/controller/TrackController.java:96-164` - `200`, `206`, and `416` Range responses.
  - `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:80` - deny-all static original route.
  - `src/test/java/com/atstudio/atstudio/controller/SecurityFilterChainTest.java:89-110` - anonymous, USER, and ADMIN denial.
  - `src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java:48-71` - subscriber original-resource regression.
  - `frontend/src/api/tracks.ts:6-28` - nullable public and non-null admin frontend contracts.
- Test result artifacts:
  - `build/test-results/test/TEST-com.atstudio.atstudio.service.TrackServiceTest.xml` - 24 tests, 0 failures/errors.
  - `build/test-results/test/TEST-com.atstudio.atstudio.controller.TrackControllerTest.xml` - 21 tests, 0 failures/errors.
  - `build/test-results/test/TEST-com.atstudio.atstudio.controller.SecurityFilterChainTest.xml` - 15 tests, 0 failures/errors.
  - `build/test-results/test/TEST-com.atstudio.atstudio.service.DownloadServiceTest.xml` - 10 tests, 0 failures/errors.

## Commands & Outputs

- Initial focused backend run:
  - `.\gradlew.bat test --no-daemon --max-workers=1 --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.controller.SecurityFilterChainTest" --tests "com.atstudio.atstudio.service.DownloadServiceTest"`
  - Infrastructure result: Gradle reported `NoSuchFileException` for an in-progress binary result file after another shared-workspace run replaced the test-results directory; no assertion failure was reported. No other process was stopped.
- RED defect reproduction:
  - `.\gradlew.bat test --no-daemon --max-workers=1 --tests "com.atstudio.atstudio.service.TrackServiceTest.getStreamResource_oneByteOriginalFallbackExposesNoBytes"`
  - Result before correction: 1 test completed, 1 failed at `TrackServiceTest.java:361`.
- Final focused backend run:
  - `.\gradlew.bat test --no-daemon --max-workers=1 --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.controller.SecurityFilterChainTest" --tests "com.atstudio.atstudio.service.DownloadServiceTest"`
  - Result: `BUILD SUCCESSFUL in 53s`; 70 passed, 0 failed, 0 errors, 0 skipped.
  - Suites: TrackService 24, TrackController 21, SecurityFilterChain 15, DownloadService 10.
- Frontend checks:
  - `npm run typecheck` - passed (`tsc --noEmit`).
  - `npm run lint` - passed with zero warnings.
  - `npm test -- --run` - 14 files and 51 tests passed in 6.31s.
  - `npx prettier --check "src/api/tracks.ts"` - passed.
- Scoped `git diff --check` over the nine WI-003 product/test paths plus WI-006 corrective paths:
  - Result: exit 0; only configured LF-to-CRLF conversion warnings were emitted.

The Spring checks used mocks and in-memory H2. They did not move or rewrite media files and did not access a live database, SMTP, Toss, or another external service.

## Risks / Rollback

- Risks:
  - Variable-bitrate originals can make proportional byte boundaries differ from the estimated playback duration; the bytes remain bounded.
  - Existing originals remain under the current storage root, so Spring Security denial is still the immediate static-resource boundary until a separately approved migration.
  - Zero/one-byte originals intentionally produce no public stream body because exposing one byte would expose the complete resource.
- Rollback:
  - Restore the prior `resourceLength <= 1` branch only if the absolute full-original invariant is replaced by an approved requirement.
  - Remove only the WI-006 additions in `TrackServiceTest` and `TrackControllerTest` and the two WI-006 output files.
  - Preserve all underlying WI-003 edits and every mail, billing, documentation, and runtime-log path.

## Follow-ups

- WI-20260713-ATS-009 and WI-20260713-ATS-013 are unblocked by this completed reliability review.
- The existing stale full-original fallback wording in `api-spec.md` and `sound-track.md` remains assigned to the documentation WI.
