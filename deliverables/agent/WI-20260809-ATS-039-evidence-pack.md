---
version: 1.2
last_updated: 2026-08-13
project: ATS
owner: se
category: evidence-pack
status: complete
dependencies:
  - path: WI-20260809-ATS-039-handoff.md
    reason: Approved work item, scope, and output contract
  - path: ../../docs/policies/security-policy.md
    reason: Current upload and protected-resource security boundary
---

# Evidence Pack: WI-20260809-ATS-039

## Summary

Album create/update canonicalize supplied thumbnails before PUBLIC storage, Album and Playlist thumbnail responses resist downstream static MIME overwrite, and every new Notice attachment operation uses PRIVATE storage with a forced safe public controller download.

## Scope and DoD Check

- [x] Album create and replacement paths pass supplied thumbnails through `CanonicalImageService.canonicalizeThumbnail` before storage.
- [x] Canonical Album objects reach storage as server-generated JPEG bytes with `thumbnail.jpg`, causing generated `.jpg` keys.
- [x] HTML, SVG, MIME-mismatched, oversized, and trailing-payload Album inputs are covered at the Album/canonicalization boundary.
- [x] Album and Playlist public thumbnail paths receive fixed JPEG, `nosniff`, sandboxed Content Security Policy, and same-origin resource-policy headers.
- [x] A response wrapper prevents downstream `setContentType`, `setHeader`, and `addHeader` calls from replacing fixed JPEG, including real MockMvc static-resource handling of retained `.svg` and `.html` fixture names.
- [x] Notice create, update, delete, and download consistently use `StorageRoot.PRIVATE`.
- [x] The public Notice attachment endpoint returns one Resource as a forced octet-stream attachment with the full safe header set.
- [x] PRIVATE Notice objects cannot resolve through the disjoint PUBLIC root in the focused temp-storage proof.
- [x] Notice visibility and current accepted-file behavior remain unchanged; no type/count/size policy was added.
- [x] No dependency, schema, retained-file migration, or production file operation was added.
- [x] Focused and adjacent mock/temp backend tests passed.
- [x] Documentation validation and `git diff --check` passed.
- [x] The first independent PG review completed with `FAIL`; its P1 blocker and P2 test gap were remediated in this amendment.
- [x] Main full backend tests, JaCoCo report/verification, and assemble passed on the isolated rerun.
- [x] Final independent PG re-review passed with no P1 or P2 findings and confirmed no scope expansion.

## Reference Documents

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approved execution, traceability, private/public separation |
| 0 | `docs/standards/development-standards.md` | Java layering, TDD, and evidence requirements |
| 0 | `docs/standards/documentation-standards.md` | Current-document update format |
| 0 | `docs/standards/glossary.md` | Canonical ATStudio terminology |
| 1 | `docs/policies/security-policy.md` | Public/private storage and safe response policy |
| 1 | `docs/policies/quality-gates.md` | HIGH criticality review and validation gates |
| 2 | `docs/design/p1-security-acceptance-hardening-design.md:55` | Historical canonical-image, private-root, and read-boundary design |
| 2 | `docs/design/api-spec.md` | Current public API and serving contract |
| 2 | `docs/design/usecase/sound-album.md` | Album create/update behavior |
| 2 | `docs/design/usecase/user-notice.md` | Notice attachment lifecycle and public download |

Additional required context was read from `deliverables/user/REQ-20260809-ATS-001.md`, `deliverables/agent/WI-20260809-ATS-025-evidence-pack.md:111`, and `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:630,968`.

## Evidence Pointers

### Implementation

- `src/main/java/com/atstudio/atstudio/service/AlbumService.java:46,61,125` - Injects the existing canonical image service and canonicalizes both store and replace inputs.
- `src/main/java/com/atstudio/atstudio/config/PublicThumbnailHeaderFilter.java` - Restricts fixed safe image headers to Album and Playlist prefixes and wraps the response so downstream content-type mutations resolve only to `image/jpeg`.
- `src/main/java/com/atstudio/atstudio/service/NoticeService.java:109-112,134-153,173-177` - Uses PRIVATE for selected delete, Notice delete, load, and new writes.
- `src/main/java/com/atstudio/atstudio/controller/NoticeController.java:86-101` - Keeps the endpoint public, returns a Resource, and forces encoded attachment disposition, octet-stream, private/no-store, no-cache, `nosniff`, sandbox CSP, and same-origin resource policy.

### Focused Tests

- `src/test/java/com/atstudio/atstudio/service/AlbumServiceTest.java:86-190,304-360` - Canonical JPEG/polyglot proof, HTML/SVG/MIME/oversize rejection before storage or persistence, create/update coverage, and unchanged Album state after rejection.
- `src/test/java/com/atstudio/atstudio/service/NoticeServiceTest.java:238-358` - Active-content compatibility under PRIVATE storage and PRIVATE create/delete/load ownership.
- `src/test/java/com/atstudio/atstudio/controller/NoticeControllerTest.java:163-205` - Anonymous public download, exact safe headers, and malicious CRLF/disposition-delimiter filename encoding with no injected header.
- `src/test/java/com/atstudio/atstudio/config/PublicThumbnailHeaderFilterTest.java` - Album/Playlist headers, direct downstream mutation attempts through all three response APIs, and unrelated-upload exclusion.
- `src/test/java/com/atstudio/atstudio/config/PublicThumbnailStaticResourceTest.java` - HTTP-level Album `.svg` and Playlist `.html` static-resource regressions plus unrelated SVG exclusion, using an isolated UUID temp public root.
- `src/test/java/com/atstudio/atstudio/service/storage/LocalStorageServiceTest.java:239-258` - PRIVATE object loads privately but the identical key fails from PUBLIC and has no private static URL.
- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java` - Existing JPEG/PNG, HTML/SVG/GIF/WebP, MIME mismatch, truncation, APNG, dimension, oversize, and trailing-payload coverage reused without modification.

### Current Documentation

- `docs/policies/security-policy.md:350-378` - Canonical public thumbnails, downstream MIME overwrite resistance, PRIVATE Notice ownership, encoded download filename, held policy, and no migration.
- `docs/design/api-spec.md:74-90` - Current API/storage/response contract, including fixed static MIME and encoded attachment filename boundaries.
- `docs/design/db-schema.md:200-211` - Existing columns interpreted as canonical public Album and private Notice keys without schema change.
- `docs/design/usecase/sound-album.md:8-22` - Album thumbnail lifecycle and downstream static MIME serving boundary.
- `docs/design/usecase/user-notice.md:8-23,158-183` - Notice attachment storage/download contract, encoded filename boundary, and ANNOUNCE-006.

## Independent PG Review and Remediation

- First independent PG result: `FAIL`.
- P1 blocker: `PublicThumbnailHeaderFilter` set `Content-Type` before the chain, allowing Spring's static resource handler to overwrite it from a retained `.svg` or `.html` filename. The prior empty-chain unit test did not exercise that behavior.
- P1 remediation: route Album and Playlist thumbnail responses through a wrapper that replaces downstream `setContentType`, `setHeader(Content-Type, ...)`, and `addHeader(Content-Type, ...)` calls with exactly `image/jpeg`; add HTTP-level MockMvc static-resource coverage for Album `.svg`, Playlist `.html`, and an unrelated SVG upload.
- P2 test gap: Notice attachment disposition had no malicious CRLF/delimiter filename regression.
- P2 remediation: add a controller test proving CRLF, colon, quote, semicolon, space, and equals characters remain percent-encoded inside `filename*`, with no injected `X-Evil` response header.
- Final independent PG re-review result: `PASS`, with no P1 or P2 findings. PG verified the response wrapper, actual static-resource regressions, Notice CRLF filename test, and absence of scope expansion.

## Red and Green Proof

### Red

Command:

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.service.AlbumServiceTest" --tests "com.atstudio.atstudio.service.NoticeServiceTest" --tests "com.atstudio.atstudio.controller.NoticeControllerTest" --tests "com.atstudio.atstudio.config.PublicThumbnailHeaderFilterTest" --tests "com.atstudio.atstudio.service.storage.LocalStorageServiceTest"
```

Result before implementation: `61 tests`, `48 passed`, `12 failed`, `1 skipped`; `BUILD FAILED in 30s`. The 12 failures mapped to the missing Album canonicalizer dependency, PUBLIC Notice operations, absent Album static headers, and incomplete Notice download headers.

### Green Focused

The same command after implementation: `61 tests`, `60 passed`, `0 failed`, `0 errors`, `1 skipped`; `BUILD SUCCESSFUL in 31s`.

| Suite | Result |
|---|---|
| `AlbumServiceTest` | 19/19 passed |
| `NoticeServiceTest` | 16/16 passed |
| `NoticeControllerTest` | 12/12 passed |
| `PublicThumbnailHeaderFilterTest` | 3/3 passed |
| `LocalStorageServiceTest` | 10 passed, 1 existing environment-conditional symlink test skipped |

## Focused and Adjacent Regression

Command:

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.service.AlbumServiceTest" --tests "com.atstudio.atstudio.service.NoticeServiceTest" --tests "com.atstudio.atstudio.controller.NoticeControllerTest" --tests "com.atstudio.atstudio.config.PublicThumbnailHeaderFilterTest" --tests "com.atstudio.atstudio.service.image.CanonicalImageServiceTest" --tests "com.atstudio.atstudio.service.storage.LocalStorageServiceTest" --tests "com.atstudio.atstudio.service.PlaylistServiceTest" --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.service.TrackServiceAudioProcessingTest" --tests "com.atstudio.atstudio.service.CompanyCertificationServiceTest" --tests "com.atstudio.atstudio.service.storage.StorageCleanupServiceTest" --tests "com.atstudio.atstudio.service.storage.StorageMutationCoordinatorTest" --tests "com.atstudio.atstudio.service.storage.StorageMutationJournalServiceTest" --tests "com.atstudio.atstudio.service.storage.StorageMutationRecoveryServiceTest" --tests "com.atstudio.atstudio.service.storage.StorageReferenceCheckerBranchCoverageTest"
```

Result: `15 selectors`, `21 JUnit result suites`, `203 tests`, `202 passed`, `0 failed`, `0 errors`, `1 skipped`; `BUILD SUCCESSFUL in 33s`.

The run covered existing Playlist canonicalization, Track storage/audio processing, Company Certification PRIVATE documents, and storage cleanup/coordinator/journal/recovery/reference behavior. It used mocks, the existing test application context, and temporary storage only; no persistent/local/live database, retained file, or external service was accessed.

Final naming-only smoke after clarifying the unrelated-upload test label:

- `.\gradlew.bat test --tests "com.atstudio.atstudio.config.PublicThumbnailHeaderFilterTest"` - `3 tests`, all passed; `BUILD SUCCESSFUL in 5s`.

## PG Amendment Focused Evidence

Command:

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.config.PublicThumbnailHeaderFilterTest" --tests "com.atstudio.atstudio.config.PublicThumbnailStaticResourceTest" --tests "com.atstudio.atstudio.controller.NoticeControllerTest"
```

The first amendment execution stopped in `compileTestJava`: the test source used two SVG constants unavailable in the repository's Spring version. No test executed. The assertions were corrected to use the standard `image/svg+xml` literal without changing production behavior.

First successful remediation result: `20 tests`, `20 passed`, `0 failed`, `0 errors`, `0 skipped`; `BUILD SUCCESSFUL in 27s`.

After explicitly asserting that the unrelated upload also receives no `nosniff` header, the same command was rerun against the final source state: process exit `0` in `42.7s` tool wall time, with JUnit XML again reporting `20 tests`, `20 passed`, `0 failed`, `0 errors`, and `0 skipped`.

| Suite | Result |
|---|---|
| `PublicThumbnailHeaderFilterTest` | 4/4 passed |
| `PublicThumbnailStaticResourceTest` | 3/3 passed |
| `NoticeControllerTest` | 13/13 passed |

The static-resource suite created only a UUID-named directory under the system test temp root and deleted that directory after the run. It did not inspect or modify retained application files. The controller suite used the existing in-memory test context and mocked `NoticeService`; no external service or persistent database was accessed.

## Main Full Backend Gates

The first full-gate attempt reached test execution but then stopped with an infrastructure-only Gradle `NoSuchFileException` for `build/test-results/test/binary/in-progress-results-*.bin`. No test assertion failure was reported. Main stopped the Gradle daemon and performed an isolated rerun; the interruption is not classified as a product or test failure.

Successful rerun command:

```powershell
.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification assemble --rerun-tasks --no-daemon --console=plain
```

Result: `BUILD SUCCESSFUL in 2m28s`; all `10` tasks executed.

- Test HTML: `1,560 tests`, `0 failures`, `19 skipped`.
- JaCoCo instruction coverage: `86.86%` (`6,855` missed of `52,166`).
- JaCoCo branch coverage: `72.02%` (`1,338` missed of `4,783`).
- JaCoCo line coverage: `87.15%` (`1,487` missed of `11,572`).
- JaCoCo method coverage: `84.66%` (`320` missed of `2,086`).
- JaCoCo class coverage: `94.81%` (`22` missed of `424`).
- `jacocoTestCoverageVerification`: PASS.
- `assemble`: PASS.

## Documentation and Diff Validation

- `python .agents/skills/validate-docs/scripts/validate_docs.py` - PASS; Tier 0 documents, internal links, 575 supported traceability IDs, and document indexes passed.
- `git diff --check` - PASS with no whitespace errors. Git reported only the existing working-copy CRLF-to-LF normalization warnings for touched CRLF files.

## Static Bypass Argument

1. `WebConfig` maps `/uploads/**` from `app.storage.public-path` only.
2. `LocalStorageService.init` requires PUBLIC and PRIVATE roots to be disjoint and rejects equal or nested roots.
3. Every new Notice attachment write, load, and delete names `StorageRoot.PRIVATE`.
4. `LocalStorageService.getUrl` rejects PRIVATE objects.
5. The focused temp-storage test writes an HTML attachment to PRIVATE, loads it from PRIVATE, and proves the same relative key cannot load from PUBLIC.

Therefore new Notice objects have no static `/uploads/**` resolution path; public availability is exclusively controller-mediated.

## Unchanged Policy and Safety

- No Notice attachment allowlist, type rule, count limit, or byte limit was invented. WI-066 remains the owner of that decision.
- No Notice authorization/visibility rule changed; the attachment GET remains permit-all.
- No dependency, parser, scanner, schema column, DDL, or data migration was added.
- No retained file was read, moved, deleted, or rewritten. No secret or protected output was inspected.
- No commit or push was performed.

## Residual Risks

- Retained pre-WI Notice objects are not migrated. If a non-fresh environment still has Notice files under the public root, this patch does not remove or relocate them; separate approved migration/containment work is required.
- Active-content Notice files remain accepted until WI-066 defines type/count/size policy. The current mitigation is private storage plus forced safe download headers, not content scanning or a malware-clean verdict.
- The existing symbolic-link branch in `LocalStorageServiceTest` skipped because symbolic-link creation was unavailable in this Windows test environment; the new PRIVATE/PUBLIC boundary test itself passed.
- Main full backend, JaCoCo verification, assemble, and final independent PG re-review all passed. The full test HTML reports `19` skipped tests and no failures.

## Rollback

Revert the Album canonicalization injection/calls, thumbnail filter prefix change, Notice PRIVATE root selections, Notice response headers, focused tests, current docs, and both WI deliverables as one patch. No data rollback is executed because this WI did not access or mutate runtime data. If runtime Notice writes occur before rollback, file ownership must be handled by a separately approved migration plan rather than moving files implicitly.

## Follow-up Chain

WI-039 is complete and no longer gates `WI-20260809-ATS-048`, `WI-20260809-ATS-050`, `WI-20260809-ATS-055`, `WI-20260809-ATS-066`, or `WI-20260809-ATS-071`. Main owns the next-chain trigger; this documentation-only finalization does not create or delegate another handoff.
