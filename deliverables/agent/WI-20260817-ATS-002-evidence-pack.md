---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: QA-INTEG
category: evidence-pack
status: stable
related_wi: WI-20260817-ATS-002
dependencies:
  - path: WI-20260817-ATS-002-handoff.md
    reason: Verification scope and output contract
  - path: WI-20260817-ATS-001-evidence-pack.md
    reason: Implementation evidence under independent review
---

# Evidence Pack: WI-20260817-ATS-002

## Change Summary

- Independently verified the temporary non-square Track thumbnail contract in
  the client-acceptance worktree and its local/public runtime reflection.
- Result: **PASS**, with BLOCKER 0 / MAJOR 0 / MINOR 1.
- Created only this Evidence Pack and the paired user-facing summary; product
  code, tests, existing documents, database rows, stored files, tunnel state,
  and public data were not mutated by this WI.

## Scope / DoD Check

- [x] Frontend accepts decoded non-square JPEG/PNG Track thumbnails and omits
      the `1:1 필수` guidance.
- [x] Backend Track policy accepts non-square input while retaining the other
      image defenses and canonicalization.
- [x] Existing non-square warning and square `cover` preview remain.
- [x] Album, Playlist, schema, database, and public-data scope is unchanged.
- [x] No new request, N+1 path, dependency, or duplicate image decode was added.
- [x] Ports 5173 and 8080 resolve to the client-acceptance worktree and not the
      development worktree.
- [x] Local/public frontend and Track API health checks return HTTP 200.
- [x] Public Vite source reflects the temporary frontend flag and guidance.
- [x] Compiled backend bytecode reflects a false square-policy argument;
      DevTools restart classloaders exist and post-compile health is green.
- [x] Focused frontend 17 and backend 48 tests pass independently.
- [x] Typecheck, ESLint, Prettier, Java compile, and diff check pass.
- [x] Development branch HEAD and tracked state remain untouched.
- [ ] Authenticated public file-selection UI was not exercised because the
      public ADMIN route correctly redirected the unauthenticated browser to Login.

## Reference Documents (Tier 0-2)

| Tier | Document                                                  | Reason                                            |
| ---- | --------------------------------------------------------- | ------------------------------------------------- |
| 0    | `docs/standards/core-principles.md`                       | Approval, isolation, and transparent evidence     |
| 0    | `docs/standards/development-standards.md`                 | Java/React testing and quality expectations       |
| 1    | `docs/policies/quality-gates.md`                          | Regression and rollback gates                     |
| 1    | `docs/standards/evidence-pack-standard.md`                | Evidence metadata and reproducibility contract    |
| 2    | `docs/standards/frontend-standards.md`                    | Active SPA, FormData, and object URL conventions  |
| 2    | `docs/design/api-spec.md`                                 | Current Track and Album/Playlist API boundaries   |
| SR   | `docs/SR/SR-98.md`                                        | Long-term square policy temporarily deviated from |
| REQ  | `deliverables/user/REQ-20260817-ATS-001.md`               | Approved branch-only temporary change             |
| WI   | `deliverables/agent/WI-20260817-ATS-001-handoff.md`       | Implementation scope and constraints              |
| WI   | `deliverables/agent/WI-20260817-ATS-001-evidence-pack.md` | Implementation claims under review                |
| WI   | `deliverables/user/WI-20260817-ATS-001-summary.md`        | User-facing implementation summary                |
| WI   | `deliverables/agent/WI-20260817-ATS-002-handoff.md`       | Independent verification contract                 |

**Injection rules applied:** assignee `qa-integ`; testing and cross-layer
verification; Tier 0 constitution, Tier 1 quality policy, Tier 2 frontend/API
context, approved REQ, predecessor WI, and exact implementation/test pointers.

## Evidence Pointers

Production contract:

- `frontend/src/pages/creator/TrackThumbnailField.tsx:13-15` - JPEG/PNG input
  accept value and temporary frontend restoration flag set to `false`.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:39-47` and `:92-115` -
  format and 10 MB guards remain before preview decode.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:118-145` - stale/decode
  guards remain; only aspect mismatch is gated and decoded non-square input
  reaches `valid`.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:178-220` - required
  guidance omits exact-square wording; recommendation, preview, and JPEG/PNG
  picker contract remain.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:224-240` - current-file
  rendering and existing non-square warning remain.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:31-47` -
  unchanged image bounds/constants, generic thumbnail route, Track route, and
  temporary backend restoration flag set to `false`.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:50-67` -
  size, signature, MIME, APNG, and canonical JPEG flow remain.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:130-175` -
  metadata/decode bounds, frame count, decoded dimensions, and policy-gated
  square rejection.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:177-228` -
  maximum bounds, aspect-preserving no-upscale rendering, and JPEG output.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:248-256` -
  `ANY_ASPECT_RATIO` unchanged and `SQUARE_TRACK` consuming only the temporary
  restoration flag.

Tests:

- `frontend/src/pages/creator/TrackThumbnailField.test.tsx:63-111` - guidance,
  square preview, and non-square valid selection.
- `frontend/src/pages/creator/TrackThumbnailField.test.tsx:138-167` - unsupported
  and oversized negative selections plus existing-warning behavior.
- `frontend/src/pages/creator/TrackUploadPage.test.tsx:43-82` - pending blocks;
  decoded non-square file is submitted into mocked FormData.
- `frontend/src/pages/creator/TrackEditPage.test.tsx:76-134` - existing warning,
  pending replacement, and non-square replacement behavior.
- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java:69-193` -
  no-upscale, non-square scaling/acceptance, generic behavior, corrupt input,
  APNG, bounds, MIME, and oversized defenses.
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java:109-170` and
  `:521-580` - non-square create/update success and invalid-image no-partial-
  mutation behavior.

Scope diff:

- Tracked diff contains exactly seven files: two product files and five focused
  tests.
- `git diff --name-status` contains no Album, Playlist, schema, migration, or DB
  path and no unexpected tracked file.
- Product diff adds only two constant flags, two gated aspect-policy uses, and
  one guidance edit. It adds no request, decode, persistence, or dependency.

## Commands & Outputs

All commands ran from
`C:\Users\jm991\AppData\Local\ATStudio\worktrees\v1-client-acceptance-20260817`
unless a frontend workdir is stated.

| Command                                                                                                                                                             | Result                                                                             |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| `git worktree list --porcelain` plus branch/HEAD/status for both worktrees                                                                                          | Target and development worktrees remain distinct; expected branches and HEADs      |
| `git diff --stat`, `git diff --name-status`, focused `git diff`                                                                                                     | 7 tracked files, `+52/-72`, no unexpected path                                     |
| `npm test -- src/pages/creator/TrackThumbnailField.test.tsx src/pages/creator/TrackUploadPage.test.tsx src/pages/creator/TrackEditPage.test.tsx`                    | PASS, 3 files / 17 tests                                                           |
| `.\gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.service.image.CanonicalImageServiceTest" --tests "com.atstudio.atstudio.service.TrackServiceTest"` | PASS, BUILD SUCCESSFUL in 26s, all five tasks executed                             |
| XML result inspection                                                                                                                                               | CanonicalImageServiceTest 14 + TrackServiceTest 34 = 48; failures/errors/skipped 0 |
| `npm run typecheck`                                                                                                                                                 | PASS, `tsc --noEmit`, exit 0                                                       |
| `npm run lint`                                                                                                                                                      | PASS, errors 0 / warnings 0, exit 0                                                |
| `npx prettier --check` on four changed frontend files                                                                                                               | PASS                                                                               |
| `.\gradlew.bat compileJava compileTestJava`                                                                                                                         | PASS, BUILD SUCCESSFUL                                                             |
| `git diff --check`                                                                                                                                                  | PASS                                                                               |
| `Get-NetTCPConnection` plus `Win32_Process` for 5173/8080                                                                                                           | PIDs 16616/15080; both command paths contain only target worktree                  |
| local/public frontend and `/api/tracks` GET                                                                                                                         | HTTP 200 for all four                                                              |
| SHA-256 comparison of local/public bodies                                                                                                                           | Frontend pair identical; API pair identical                                        |
| local/public Vite module GET                                                                                                                                        | HTTP 200; identical SHA-256; disabled flag and changed guidance present            |
| `javap -c -p ...CanonicalImageService$ThumbnailPolicy`                                                                                                              | `SQUARE_TRACK` static initialization uses `iconst_0` (`false`)                     |
| `jcmd 15080 VM.classloaders`                                                                                                                                        | Active JVM includes DevTools `RestartClassLoader` instances                        |
| `jcmd 15080 VM.class_hierarchy ...CanonicalImageService`                                                                                                            | CanonicalImageService loaded through restart classloaders                          |
| Browser GET of public `/admin/tracks/upload`                                                                                                                        | Correct redirect to `/login?returnTo=%2Fadmin%2Ftracks%2Fupload`; no submission    |

Response fingerprints captured without body disclosure:

- local/public frontend: status 200, 786 bytes,
  `6269a0ab540324bee5bf16a88752a233f8c8c5c6221e2101949f15005302f027`
- local/public Track API: status 200, 18,420 bytes,
  `53e4801a2c9eec200bac78670d35426cab2581d80c238c8d98d2be06ec653fc4`
- local/public Vite module: status 200, 31,680 bytes,
  `15fb4c8a60e487b0dd90a85c4a4879cc6eb59c265d1818d22b2f5b14e63d5826`

Backend XML pointers:

- `build/test-results/test/TEST-com.atstudio.atstudio.service.image.CanonicalImageServiceTest.xml`
  - 14 tests, 0 failures/errors/skipped.
- `build/test-results/test/TEST-com.atstudio.atstudio.service.TrackServiceTest.xml`
  - 34 tests, 0 failures/errors/skipped.

## Results / Findings

### BLOCKER: 0

- None.

### MAJOR: 0

- None.

### MINOR: 1 — runtime lifecycle evidence is unmanaged

- `scripts/acceptance/status.ps1` reports `not-started` because no runtime
  manifest exists, even though 5173 and 8080 are listening from the correct
  target worktree.
- An attached DevTools stdout/stderr log was therefore unavailable. Reflection
  was established through target-only command paths, actual class recompilation,
  false compiled enum bytecode, active restart classloaders, and green local and
  public health after compilation.
- Recommendation: on the next authorized server restart, use the acceptance
  lifecycle so the manifest and stdout/stderr pointers attest ownership and
  restart events. No process or tunnel action was taken in this WI.

### Bounded browser limitation (not a defect)

- The public ADMIN route redirected an unauthenticated browser to Login as
  required. Authentication was not bypassed, and no user file or public request
  body was submitted. Actual public file selection remains unexecuted; focused
  component/page tests and the served Vite module cover the temporary frontend
  behavior.

## Development Worktree Isolation

- Development path: `C:\Users\jm991\Desktop\project\ATStudio`.
- Branch/HEAD: `codex/v1-release-rehearsal-fixes` /
  `3e1123416bd106fbeb8b1ad2b3f8bf343a54100e`.
- Tracked diff count: 0.
- Its pre-existing untracked status was not modified by this WI.
- Read-only source inspection still shows `1:1 필수` in
  `TrackThumbnailField.tsx:179` and `SQUARE_TRACK(true)` in
  `CanonicalImageService.java:248`.

## Risks / Rollback

Risks:

- Non-square sources remain cropped in square `cover` cards; the purpose of this
  temporary branch is to expose that behavior for client evaluation.
- Runtime lifecycle ownership is not represented by the acceptance manifest.
- Public authenticated file selection was not exercised without an available
  authenticated session.

Rollback:

1. Set `TRACK_THUMBNAIL_SQUARE_REQUIRED` to `true` at
   `frontend/src/pages/creator/TrackThumbnailField.tsx:15`.
2. Set `REQUIRE_SQUARE_TRACK_THUMBNAIL` to `true` at
   `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:40`.
3. Restore the `1:1 필수` guidance and focused square-rejection expectations.
4. Re-run focused tests, typecheck, lint, Prettier, Java compile, and diff check.
5. Recompile target classes and verify local/public frontend/API health.

No database, stored-file, schema, dependency, tunnel, or public-data rollback is
required.
