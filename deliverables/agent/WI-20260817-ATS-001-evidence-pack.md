---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: SE
category: evidence-pack
status: stable
related_wi: WI-20260817-ATS-001
dependencies:
  - path: WI-20260817-ATS-001-handoff.md
    reason: Approved scope and output contract
  - path: ../../docs/SR/SR-98.md
    reason: Long-term square Track thumbnail policy being temporarily relaxed
---

# Evidence Pack: WI-20260817-ATS-001

## Change Summary

- Temporarily disabled only the exact-square rejection for new or replacement
  Track thumbnails on the client-acceptance branch.
- Preserved JPEG/PNG, 10 MB, decode, dimension/pixel, APNG/frame, no-upscale,
  aspect-preserving resize, canonical JPEG, square `cover` preview, and existing
  non-square warning behavior.

## Scope / DoD Check

- [x] Non-square JPEG and PNG selections become `valid` after successful browser decode.
- [x] Track Upload and Track Edit no longer block a decoded image only because its width and height differ.
- [x] Track create and replacement canonicalize non-square images without `TRACK_THUMBNAIL_NOT_SQUARE`.
- [x] JPEG/PNG, 10 MB, signature, MIME, APNG, frame-count, decoded bounds, maximum dimension/pixel, no-upscale, aspect-preserving resize, and JPEG output defenses remain active.
- [x] New/replacement guidance omits `1:1 필수` and retains `2048x2048px 권장 (필수 아님)`.
- [x] The square centered `cover` preview and existing non-square warning remain unchanged.
- [x] No new decode, network request, detail request, dependency, schema, DB, or public-data mutation was introduced.
- [x] Focused frontend/backend tests, frontend typecheck/ESLint/Prettier, Java compilation, and `git diff --check` pass.
- [x] Only the designated client-acceptance worktree was modified; no commit or server-process action occurred.

## Reference Documents (Tier 0-2)

| Order | Tier | Document                                            | Reason                                                          |
| ----- | ---- | --------------------------------------------------- | --------------------------------------------------------------- |
| 1     | 0    | `docs/standards/core-principles.md`                 | Approved execution, scope, transparency, and sustainability     |
| 2     | 0    | `docs/standards/development-standards.md`           | Java/React implementation and test standards                    |
| 3     | 1    | `docs/policies/quality-gates.md`                    | Regression, traceability, and rollback gates                    |
| 4     | 1    | `docs/standards/evidence-pack-standard.md`          | Evidence metadata and reproducibility contract                  |
| 5     | 2    | `.agents/skills/react-best-practices/AGENTS.md`     | React event/state and rendering guidance                        |
| 6     | 2    | `docs/standards/frontend-standards.md`              | Active SPA upload, validation, and object URL conventions       |
| 7     | SR   | `docs/SR/SR-98.md`                                  | Long-term exact-square contract and preview behavior            |
| 8     | REQ  | `deliverables/user/REQ-20260817-ATS-001.md`         | Approved temporary branch-only deviation                        |
| 9     | WI   | `deliverables/agent/WI-20260817-ATS-001-handoff.md` | Assignee, constraints, acceptance criteria, and output contract |

Supplemental references:

- `docs/standards/documentation-standards.md` - documentation language, metadata, and link rules.
- `docs/standards/glossary.md` - canonical Track and Upload terminology.
- `.agents/skills/create-wi-evidence-pack/SKILL.md` - mandatory evidence generation procedure.
- `.agents/skills/test/SKILL.md` - focused Vitest and Gradle test procedure.
- `.agents/skills/typecheck/SKILL.md` - TypeScript and Java compile verification.
- `.agents/skills/eslint/SKILL.md` - frontend lint verification.
- `.agents/skills/prettier/SKILL.md` - non-mutating format verification.
- `.agents/skills/react-best-practices/SKILL.md` - React skill entrypoint.

**Assignee:** `se`

**Task type:** temporary cross-layer Track thumbnail validation change with focused regression tests

## Evidence Pointers

Production:

- `frontend/src/pages/creator/TrackThumbnailField.tsx:13-15` - temporary frontend restoration flag; `true` restores the SR-98 square requirement.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:118-144` - decode completion remains authoritative; invalid natural dimensions still fail while aspect mismatch is gated by the temporary flag.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:181-188` - required guidance omits exact-square wording while recommended dimensions and the 1:1 preview marker remain.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:215-240` - JPEG/PNG input accept contract and existing non-square warning remain.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:39-47` - temporary backend restoration flag and unchanged Track-specific entry point.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:50-65` - size, signature, MIME, APNG, and canonical encode flow remain unchanged.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:137-159` - metadata/decoded bounds and frame checks remain; only the policy-controlled aspect rejection is disabled.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:177-228` - maximum bounds, aspect-preserving no-upscale rendering, and JPEG output remain unchanged.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:247-250` - `SQUARE_TRACK` consumes the temporary restoration flag.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:69` and `:171` - create and update still share the Track-specific canonicalization path through the unchanged helper at `:318-322`.

Tests:

- `frontend/src/pages/creator/TrackThumbnailField.test.tsx:63-70` - revised required/recommended guidance contract.
- `frontend/src/pages/creator/TrackThumbnailField.test.tsx:74-111` - square preview remains stable and decoded non-square selection becomes valid.
- `frontend/src/pages/creator/TrackThumbnailField.test.tsx:136-167` - unsupported/oversized files and existing-image warnings remain covered.
- `frontend/src/pages/creator/TrackUploadPage.test.tsx:43-82` - pending state blocks, then a decoded non-square PNG is submitted in FormData.
- `frontend/src/pages/creator/TrackEditPage.test.tsx:76-101` - existing non-square warning remains non-blocking without replacing the stored image.
- `frontend/src/pages/creator/TrackEditPage.test.tsx:115-134` - pending state blocks, then a decoded non-square PNG is submitted as the replacement.
- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java:69-105` - small square no-upscale, large non-square JPEG scale, and non-square PNG acceptance.
- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java:108-193` - generic behavior plus unsupported, MIME, corrupt, APNG, bounds, oversize, and Track-path defenses.
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java:109-170` - non-square create path stores canonical output; invalid image still fails before analysis/storage/persistence.
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java:521-580` - non-square replacement stores canonical output; invalid image still causes no partial mutation.

Changed files before deliverable creation:

- `frontend/src/pages/creator/TrackThumbnailField.tsx` (`+4/-2`)
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java` (`+3/-1`)
- `frontend/src/pages/creator/TrackThumbnailField.test.tsx` (`+6/-7`)
- `frontend/src/pages/creator/TrackUploadPage.test.tsx` (`+6/-15`)
- `frontend/src/pages/creator/TrackEditPage.test.tsx` (`+6/-15`)
- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java` (`+13/-18`)
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java` (`+14/-14`)

## Reproduction / Verification

Run from `C:\Users\jm991\AppData\Local\ATStudio\worktrees\v1-client-acceptance-20260817` unless a frontend workdir is noted.

| Command                                                                                                                                                                                             | Workdir         | Result                                                                                                |
| --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- | ----------------------------------------------------------------------------------------------------- |
| `npm test -- src/pages/creator/TrackThumbnailField.test.tsx src/pages/creator/TrackUploadPage.test.tsx src/pages/creator/TrackEditPage.test.tsx`                                                    | `frontend/`     | PASS, 3 files / 17 tests / 0 failed                                                                   |
| `.\gradlew.bat test --tests "com.atstudio.atstudio.service.image.CanonicalImageServiceTest" --tests "com.atstudio.atstudio.service.TrackServiceTest"`                                               | repository root | PASS, 2 classes / 48 tests / 0 failures / 0 errors / 0 skipped; final rerun `BUILD SUCCESSFUL in 11s` |
| `npm run typecheck`                                                                                                                                                                                 | `frontend/`     | PASS, `tsc --noEmit`, exit 0                                                                          |
| `npm run lint`                                                                                                                                                                                      | `frontend/`     | PASS, ESLint errors 0 / warnings 0, exit 0                                                            |
| `npx prettier --check src/pages/creator/TrackThumbnailField.tsx src/pages/creator/TrackThumbnailField.test.tsx src/pages/creator/TrackUploadPage.test.tsx src/pages/creator/TrackEditPage.test.tsx` | `frontend/`     | PASS, all matched files formatted                                                                     |
| `.\gradlew.bat compileJava compileTestJava`                                                                                                                                                         | repository root | PASS, `BUILD SUCCESSFUL in 2s`                                                                        |
| `git diff --check`                                                                                                                                                                                  | repository root | PASS, exit 0                                                                                          |

Additional documentation checks:

- `npx prettier --check` for both WI deliverables - PASS.
- `python .agents/skills/validate-docs/scripts/validate_docs.py` - PASS for
  Tier 0, internal links, traceability IDs, and the document index.

Backend test-result pointers:

- `build/test-results/test/TEST-com.atstudio.atstudio.service.image.CanonicalImageServiceTest.xml` - 14 tests, 0 failures/errors/skipped.
- `build/test-results/test/TEST-com.atstudio.atstudio.service.TrackServiceTest.xml` - 34 tests, 0 failures/errors/skipped.

## Results

- Target branch: `codex/v1-client-acceptance-20260817`.
- Initial target HEAD: `18928a702ea5281e535faac855c17ce78166653a`.
- Initial target status contained only the approved untracked REQ and handoff packet.
- Development worktree was inspected read-only at HEAD `3e1123416bd106fbeb8b1ad2b3f8bf343a54100e`; none of its pre-existing untracked files were changed by this WI.
- No commit, process restart, tunnel action, database write, public API mutation, dependency installation, or file deletion occurred.

## Risks / Rollback

Risks:

- Non-square sources remain visibly cropped in square `cover` cards; this WI intentionally exposes that behavior for client review and does not select crop, contain, or padding policy.
- `canonicalizeSquareTrackThumbnail` retains its historical name while its square policy is temporarily false. The explicit restoration constant and branch isolation prevent this from being treated as the long-term contract.
- `TRACK_THUMBNAIL_NOT_SQUARE` remains defined and the guarded code remains compiled, but it is not emitted for Track thumbnails while the two temporary flags are false.
- Full suites, browser file upload, and running-server reflection are intentionally deferred to independent WI-20260817-ATS-002.

Rollback:

1. Set `TRACK_THUMBNAIL_SQUARE_REQUIRED` to `true` at `frontend/src/pages/creator/TrackThumbnailField.tsx:15`.
2. Set `REQUIRE_SQUARE_TRACK_THUMBNAIL` to `true` at `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:40`.
3. Restore `1:1 필수` in the frontend guidance and return the five focused test files to the exact-square expectations.
4. Re-run the focused frontend/backend tests, typecheck, ESLint, Java compilation, Prettier, and `git diff --check`.
5. No schema, data, dependency, storage, or external-system rollback is required.

## Follow-ups

- WI-20260817-ATS-002 is unblocked for independent scope verification, browser/API acceptance checks, and controlled reflection in the client-review server.
