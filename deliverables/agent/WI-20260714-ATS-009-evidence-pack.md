# Evidence Pack: WI-20260714-ATS-009

## Summary

- Implemented canonical Playlist thumbnail validation/re-encoding and safe public thumbnail response headers without adding image libraries.

## Scope / DoD Check

- [x] Valid JPEG/PNG input is verified by signature, decoded with JDK ImageIO, rendered into RGB, optionally downscaled, and re-encoded as fresh JPEG bytes.
- [x] Playlist create/update now sends only canonical JPEG `MultipartFile` objects through `StorageMutationCoordinator`.
- [x] Public Playlist thumbnail responses receive fixed safe headers on `/uploads/playlists/thumbnails/**`.
- [x] Focused tests were added for valid, malicious, corrupted, oversized, excessive-dimension, APNG, MIME mismatch, trailing-payload, coordinator integration, and response-header cases.
- [x] `compileTestJava` and WI-009 scoped tests pass.

## Reference Documents

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | System constitution and traceability rules |
| 0 | `docs/standards/development-standards.md` | Java/Spring implementation and test standards |
| 1 | `docs/policies/security-policy.md` | Upload/public resource security boundary |
| 2 | `deliverables/user/REQ-20260714-ATS-001.md` | Approved P1 remediation scope |
| 2 | `docs/design/p1-security-acceptance-hardening-design.md` | Section 3 untrusted image contract and Section 6 storage lifecycle contract |
| 2 | `deliverables/agent/WI-20260714-ATS-012-evidence-pack.md` | StorageMutationCoordinator dependency and no direct storage bypass constraint |

## Evidence Pointers

- Files changed:
  - `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java` - new canonical image validation/re-encode service.
  - `src/main/java/com/atstudio/atstudio/service/PlaylistService.java` - Playlist thumbnail create/update now canonicalizes before coordinator store/replace.
  - `src/main/java/com/atstudio/atstudio/config/PublicThumbnailHeaderFilter.java` - fixed safe headers for public Playlist thumbnails.
  - `src/main/java/com/atstudio/atstudio/config/WebConfig.java` - static `/uploads/**` serving uses `app.storage.public-path` fallback chain.
  - `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java` - focused byte/signature/canonicalization tests.
  - `src/test/java/com/atstudio/atstudio/config/PublicThumbnailHeaderFilterTest.java` - fixed response header and path-boundary tests.
  - `src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java` - verifies canonicalized file is passed to coordinator.
  - `deliverables/user/WI-20260714-ATS-009-summary.md` - user-facing completion summary.
  - `deliverables/agent/WI-20260714-ATS-009-evidence-pack.md` - this Evidence Pack.

- Key locations:
  - `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:40` - entry point enforcing non-empty upload and 10 MiB size bound.
  - `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:67` - JPEG/PNG magic-byte verification.
  - `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:82` - client MIME mismatch rejection.
  - `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:93` - APNG `acTL` marker rejection.
  - `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:157` - dimension and pixel bounds.
  - `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:190` - JPEG quality 0.90 re-encode with JDK ImageIO.
  - `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:228` - generated canonical `MultipartFile` reports `thumbnail.jpg` and `image/jpeg`.
  - `src/main/java/com/atstudio/atstudio/service/PlaylistService.java:57` - create path uses `StorageMutationCoordinator.store` with canonical file.
  - `src/main/java/com/atstudio/atstudio/service/PlaylistService.java:196` - update path uses `StorageMutationCoordinator.replace` with canonical file and old key.
  - `src/main/java/com/atstudio/atstudio/config/PublicThumbnailHeaderFilter.java:17` - path boundary is `/uploads/playlists/thumbnails/`.
  - `src/main/java/com/atstudio/atstudio/config/PublicThumbnailHeaderFilter.java:26` - fixed `image/jpeg`, `nosniff`, CSP, and CORP headers.
  - `src/main/java/com/atstudio/atstudio/config/WebConfig.java:17` - public root follows `app.storage.public-path`.

## Test Evidence

- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java:30` - valid JPEG with trailing script payload becomes canonical JPEG and strips payload.
- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java:46` - valid PNG with trailing SVG payload becomes canonical JPEG and strips payload.
- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java:57` - valid large image downscales without upscaling.
- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java:67` - SVG, HTML, GIF, and WebP signatures rejected.
- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java:82` - MIME mismatch rejected.
- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java:91` - truncated image rejected.
- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java:97` - APNG marker rejected.
- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java:108` - excessive dimensions rejected before full decode.
- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java:119` - oversized input returns `IO_LARGE`.
- `src/test/java/com/atstudio/atstudio/config/PublicThumbnailHeaderFilterTest.java:18` - fixed response header evidence.
- `src/test/java/com/atstudio/atstudio/config/PublicThumbnailHeaderFilterTest.java:36` - non-playlist upload boundary evidence.
- `src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java:84` - create path canonicalization before coordinator.
- `src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java:356` - update path canonicalization before coordinator.

## Commands & Outputs

- `.\gradlew.bat compileJava`
  - PASS: `BUILD SUCCESSFUL`
- `.\gradlew.bat compileTestJava`
  - PASS.
- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.image.CanonicalImageServiceTest" --tests "com.atstudio.atstudio.config.PublicThumbnailHeaderFilterTest" --tests "com.atstudio.atstudio.service.PlaylistServiceTest"`
  - PASS: `BUILD SUCCESSFUL`
  - `CanonicalImageServiceTest`: 9 tests, skipped 0, failures 0, errors 0.
  - `PublicThumbnailHeaderFilterTest`: 2 tests, skipped 0, failures 0, errors 0.
  - `PlaylistServiceTest`: 22 tests, skipped 0, failures 0, errors 0.
- `git diff --check -- src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java src/main/java/com/atstudio/atstudio/service/PlaylistService.java src/main/java/com/atstudio/atstudio/config/PublicThumbnailHeaderFilter.java src/main/java/com/atstudio/atstudio/config/WebConfig.java src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java src/test/java/com/atstudio/atstudio/config/PublicThumbnailHeaderFilterTest.java src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java`
  - PASS: no whitespace errors; output contained CRLF normalization warnings for existing tracked files.
- `rg -n "[ \t]+$" src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java src/main/java/com/atstudio/atstudio/config/PublicThumbnailHeaderFilter.java src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java src/test/java/com/atstudio/atstudio/config/PublicThumbnailHeaderFilterTest.java deliverables/user/WI-20260714-ATS-009-summary.md deliverables/agent/WI-20260714-ATS-009-evidence-pack.md`
  - PASS: no trailing whitespace matches.

## Risks / Rollback

- Risks:
  - Historical thumbnails already stored under `/uploads/playlists/thumbnails/` are not migrated or re-encoded by this WI.
  - JDK ImageIO support is intentionally limited to baseline JPEG/PNG; GIF/WebP/SVG remain rejected.
- Rollback:
  - Revert `CanonicalImageService`, `PublicThumbnailHeaderFilter`, PlaylistService canonicalization calls, WebConfig public-root adjustment, and the WI-009 tests/deliverables.
  - Disable new Playlist thumbnail uploads before rollback if canonical JPEG-only behavior has already been exposed to testers.
  - Do not delete storage journal rows or public files blindly; WI-012 coordinator remains the storage lifecycle owner.

## Follow-ups

- WI-019/WI-024 should independently run the authored file/security tests after the current payment test compile blocker is resolved.
- WI-010 remains responsible for Company Certification document quarantine and authenticated attachment-only serving.
