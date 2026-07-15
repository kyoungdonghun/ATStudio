# Evidence Pack: WI-20260715-ATS-018

## Summary (one-liner)

- Restored controller-mediated full-resource Track streaming with full-length Range semantics while preserving static-media denial, public DTO redaction, and protected downloads.

## Scope / DoD Check

- [x] Full active Track original resource is loaded through `TrackService` and streamed through `TrackController`.
- [x] Bounded-prefix and dedicated-preview selection logic is removed from the backend stream path.
- [x] No-Range, start/end, open-ended, and suffix requests use the full resource length.
- [x] Invalid and unsatisfiable Ranges return `416` with `Content-Range: bytes */{fullLength}`.
- [x] Public DTO storage-key redaction and direct static original denial remain covered.
- [x] Subscription, quota, download ledger, and license regressions pass without production download changes.
- [x] Focused backend tests and Java main/test compilation pass.

## Reference Documents (Tier 0-2)

**Injected Context** (from the WI handoff packet, read in Tier order):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution and execution authority |
| 0 | `docs/standards/development-standards.md` | Java implementation and testing standards |
| 1 | `docs/policies/security-policy.md` | Protected Track media boundary |
| 1 | `docs/policies/quality-gates.md` | Regression and evidence requirements |
| 2 | `deliverables/user/REQ-20260715-ATS-001.md` | Approved full-listening policy and scope |
| 2 | `docs/design/api-spec.md` | Previous Track stream and download contract |
| 2 | `docs/design/usecase/sound-track.md` | Previous SOUND-010/SOUND-011 behavior |
| 2 | `docs/design/p0-release-blocker-remediation-design.md` | Prior bounded-stream security rationale |
| 2 | `docs/audit/p0-release-blocker-closure-20260713.md` | Prior implementation and regression baseline |

**Injection Rules Applied**:

- Handoff: `deliverables/agent/WI-20260715-ATS-018-handoff.md`
- Assignee: `se`
- Task type: backend implementation and focused regression testing
- Read order: Tier 0 -> Tier 1 -> Tier 2 -> source/test input files
- The handoff pointed to `src/test/java/com/atstudio/atstudio/config/SecurityFilterChainTest.java`; the current class is at `src/test/java/com/atstudio/atstudio/controller/SecurityFilterChainTest.java`, which was reviewed and executed.

## Input Files Reviewed

- `src/main/java/com/atstudio/atstudio/service/TrackService.java`
- `src/main/java/com/atstudio/atstudio/controller/TrackController.java`
- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java`
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`
- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java`
- `src/test/java/com/atstudio/atstudio/controller/SecurityFilterChainTest.java`
- `src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java`

## Evidence Pointers

### Files Changed

- `src/main/java/com/atstudio/atstudio/service/TrackService.java:151-162` - loads the active Track original key through `StorageService` and returns its complete content length; bounded-prefix and preview-selection helpers were removed.
- `src/main/java/com/atstudio/atstudio/controller/TrackController.java:95-144` - streams the complete representation and resolves one valid Range against the full length without the former open-ended 1 MiB cap.
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java:287-335` - verifies original selection and full length regardless of preview metadata, duration, or one-byte size.
- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java:163-295` - verifies no-Range, start/end, overlong end, open-ended, suffix, malformed, multiple, and unsatisfiable Range behavior.
- `deliverables/user/WI-20260715-ATS-018-summary.md` - user-facing result.
- `deliverables/agent/WI-20260715-ATS-018-evidence-pack.md` - this evidence pack.

### Preserved Boundary Evidence

- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java:141-160` - public DTO original key remains `null`; admin response retains its operational key.
- `src/test/java/com/atstudio/atstudio/controller/SecurityFilterChainTest.java:92-130` - anonymous, USER, ADMIN, encoded, and traversal static original paths remain denied.
- `src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java:49-239` - subscription, daily limit, ledger, original-resource load, and license behavior remains covered.
- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:69-84` - public stream routing and static original deny rule remain unchanged.

## Commands & Outputs

### TDD Baseline

- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest"` -> expected red result before production changes: 43 tests, 3 failures, all in the new full-resource service assertions.
- The first green attempt found one test-only Mockito `UnnecessaryStubbingException`; the unused preview stub was removed without changing production behavior.

### Final Focused Tests

- Command: `.\gradlew.bat test --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.controller.SecurityFilterChainTest" --tests "com.atstudio.atstudio.service.DownloadServiceTest"`
- Result: `BUILD SUCCESSFUL in 26s`.
- Count: 70 tests, 0 failures, 0 errors, 0 skipped.
- Suites: TrackServiceTest 21; TrackControllerTest 22; SecurityFilterChainTest 17; DownloadServiceTest 10.
- XML evidence: `build/test-results/test/TEST-com.atstudio.atstudio.*.xml` for the four suites above.

### Java Compilation

- Command: `.\gradlew.bat compileJava compileTestJava`
- Result: `BUILD SUCCESSFUL in 1s`; all three Gradle tasks were up-to-date.

### Diff Integrity

- Command: `git diff --check`
- Result: exit 0; no whitespace errors. Git reported LF-to-CRLF working-copy warnings, including unrelated concurrent frontend files; no frontend file was edited by this WI.

## Risks / Rollback

### Risks

- The approved public full-resource stream can be retained from network responses. This is the explicit REQ boundary and remains separate from official download entitlement and licensing.
- The implementation continues to read the original from its existing public storage root, but only through the controller; direct static routing remains denied.

### Rollback

- Apply a scoped reverse patch only to the stream hunks in `TrackService.java`, `TrackController.java`, `TrackServiceTest.java`, and `TrackControllerTest.java`.
- Restore the prior dedicated-preview selection, bounded original-length calculation, and open-ended chunk cap together with their prior focused assertions.
- Do not use a whole-worktree checkout or revert: preserve concurrent frontend, document, runtime-log, and unrelated backend changes.

## Follow-ups

- WI-20260715-ATS-018 blocks WI-20260715-ATS-021 and WI-20260715-ATS-022. WI-021 also depends on WI-019, so this completion should be reported to MA for normal chain evaluation rather than triggering it independently.
