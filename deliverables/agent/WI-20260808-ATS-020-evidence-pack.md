# Evidence Pack: WI-20260808-ATS-020

## Summary (one-liner)

- Enforced an exact-square contract for new or replacement Track thumbnails from byte-level backend defense through race-safe Upload/Edit previews while preserving every existing Track image.

## Scope / DoD Check

- [x] New or replacement Track thumbnails accept JPEG or PNG up to the existing 10 MB limit.
- [x] Backend validation checks actual signature, client MIME consistency, PNG animation, frame count, bounds, decoded dimensions, and exact square shape before storage.
- [x] Valid inputs reuse non-upscaling, aspect-preserving max-2048 JPEG canonicalization.
- [x] A dedicated Track method leaves the generic canonicalization policy and Album/Playlist behavior unchanged.
- [x] Create and update share one Track thumbnail canonicalization helper and pass only the canonical file to storage.
- [x] Non-square rejection returns stable `TRACK_THUMBNAIL_NOT_SQUARE`, HTTP 400, and a clear client message.
- [x] Rejection performs zero storage writes/replaces and causes no partial Track field or Tag mutation.
- [x] Existing non-square Track thumbnails are neither changed nor deleted.
- [x] Track Edit renders the existing cover and warns only after successful natural-dimension inspection.
- [x] Existing-image load failure does not create a false non-square warning.
- [x] Track Upload and Track Edit show explicit format, shape, size, and recommended-dimension guidance.
- [x] Selected files use a stable square `cover` preview and block submit while pending or invalid.
- [x] Selection generations ignore stale image events, and object URLs are revoked on replacement and unmount.
- [x] Focused backend/frontend tests, final typecheck, full ESLint, full frontend Prettier, deliverable Prettier, and `git diff --check` pass.
- [x] No full suite, build, coverage, schema/data mutation, dependency change, external call, deletion, or commit occurred.

## Reference Documents (Tier 0-2)

The user-required documents were read in the specified order before implementation.

| Order | Tier | Document                                            | Reason                                                             |
| ----- | ---- | --------------------------------------------------- | ------------------------------------------------------------------ |
| 1     | 0    | `docs/standards/core-principles.md`                 | Approved execution, transparency, scope, and sustainability        |
| 2     | 0    | `docs/standards/development-standards.md`           | Java/TypeScript implementation, testing, and evidence rules        |
| 3     | 1    | `docs/standards/frontend-standards.md`              | React local-state, CSS Module, upload, and validation conventions  |
| 4     | REQ  | `deliverables/user/REQ-20260808-ATS-004.md`         | Approved square-thumbnail policy and WI chain baseline             |
| 5     | SR   | `docs/SR/SR-98.md`                                  | Exact 1:1, preview, existing-image preservation, and no-crop scope |
| 6     | SR   | `docs/SR/SR-68.md`                                  | Stable square cover layout and responsive image behavior           |
| 7     | WI   | `deliverables/agent/WI-20260808-ATS-020-handoff.md` | Assignee, scope, DoD, outputs, constraints, and WI-021 blocker     |

Supplemental references:

- `docs/standards/documentation-standards.md` - English and pointer-first evidence rules.
- `docs/standards/glossary.md` - Canonical Track and Upload terminology.
- `.agents/skills/create-wi-evidence-pack/SKILL.md` - Required evidence structure and handoff precondition.
- `.agents/skills/test/SKILL.md` - Focused Gradle and Vitest execution contract.
- `.agents/skills/typecheck/SKILL.md` - TypeScript no-emit verification contract.
- `.agents/skills/eslint/SKILL.md` - Full frontend lint verification contract.
- `.agents/skills/prettier/SKILL.md` - Non-destructive formatting verification contract.
- `.agents/skills/react-best-practices/SKILL.md` - React event, state, and stale-result guidance.

**Assignee:** `se`

**Task type:** backend image validation and React upload/edit implementation with focused regression tests

## Design Rationale

1. `canonicalizeThumbnail` remains the generic policy. `canonicalizeSquareTrackThumbnail` selects a dedicated policy and therefore cannot silently tighten Playlist or other existing callers.
2. Signature, MIME, APNG, frame, and metadata bounds checks run before full decode. The decoded image is then bounds-checked again, and the exact square decision uses decoded natural dimensions.
3. Canonical rendering reuses the existing `Math.min(1.0, 2048 / maxDimension)` scale. A 512 square remains 512; a 3000 square becomes 2048; no image is enlarged.
4. Track create/update call the same private canonicalization helper. Update completes thumbnail validation and optional audio analysis before any entity field, active-state, Tag, or storage mutation.
5. The frontend keeps thumbnail status beside each Track row. `empty` is optional and non-blocking; `pending` and `invalid` block the page-level command; only `valid` files are submitted.
6. The preview component owns each object URL. A monotonically changing selection version fences detached stale image events. Effect cleanup revokes the captured URL and invalidates its version.
7. Existing covers use the same square `cover` container as selected files. Their natural dimensions only set a non-blocking warning; load failure sets an internal error state with no false warning.

## Evidence Pointers

Backend production:

- `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java:260` - stable Track square-domain error, HTTP status, client message, and developer message.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:40` - unchanged generic entry point delegates to the any-aspect policy.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:44` - dedicated square Track entry point.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:48` - shared size/signature/MIME/APNG canonicalization path.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:139` - metadata bounds, single-frame check, and actual decode.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:150` - decoded bounds and exact-square decision.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:186` - non-upscaling, aspect-preserving max-2048 render.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:66` - create canonicalizes before audio/storage and queues the canonical file.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:164` - update canonicalizes before audio analysis and all entity/storage mutation.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:184` - update replacement receives the canonical file.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:303` - shared optional Track thumbnail helper.

Frontend production:

- `frontend/src/pages/creator/trackThumbnail.ts:1` - explicit `empty/pending/valid/invalid` selection contract.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:30` - extension and MIME correspondence policy for JPEG/PNG.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:58` - object URL creation, selection generation, cleanup invalidation, and revocation.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:109` - stale-event fence and natural-dimension square validation.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:149` - existing-image natural-dimension assessment with load-failure separation.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:172` - explicit required/recommended guidance.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:179` - shared selected/existing preview and exact `1:1` marker.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:228` - non-blocking replacement recommendation for a confirmed legacy non-square image.
- `frontend/src/pages/creator/TrackThumbnailField.module.css:34` - fixed 160px/max-100% square preview container.
- `frontend/src/pages/creator/TrackThumbnailField.module.css:45` - `object-fit: cover` and centered image placement.
- `frontend/src/pages/creator/TrackUploadPage.tsx:213` - per-row pending/invalid validation.
- `frontend/src/pages/creator/TrackUploadPage.tsx:265` - only the selected valid file is appended.
- `frontend/src/pages/creator/TrackUploadPage.tsx:298` - any blocked row disables the multi-row submit.
- `frontend/src/pages/creator/TrackEditPage.tsx:158` - defensive submit guard for pending/invalid replacement.
- `frontend/src/pages/creator/TrackEditPage.tsx:238` - existing stored cover is rendered without mutation.
- `frontend/src/pages/creator/TrackEditPage.tsx:381` - Save is disabled only for pending/invalid replacement.

Backend tests:

- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java:70` - small square is not upscaled.
- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java:82` - large square scales to 2048.
- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java:93` - stable non-square 400 error and message.
- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java:109` - generic non-square behavior remains available.
- `src/test/java/com/atstudio/atstudio/service/image/CanonicalImageServiceTest.java:183` - corrupt, MIME-mismatched, and oversized Track inputs remain rejected.
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java:104` - create passes the canonical thumbnail in the batched storage request.
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java:145` - create rejection produces zero audio/storage/persistence actions.
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java:437` - update passes the canonical file to replacement.
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java:470` - update rejection leaves Track fields, active state, storage, and Tags untouched.
- `src/test/java/com/atstudio/atstudio/service/TrackServiceAudioProcessingTest.java:80` - focused audio regression fixture includes the new dependency without behavior change.

Frontend tests:

- `frontend/src/pages/creator/TrackThumbnailField.test.tsx:63` - guidance and exact input accept contract.
- `frontend/src/pages/creator/TrackThumbnailField.test.tsx:74` - square preview CSS and pending-to-valid transition.
- `frontend/src/pages/creator/TrackThumbnailField.test.tsx:97` - visible non-square preview, field error, and blocked command.
- `frontend/src/pages/creator/TrackThumbnailField.test.tsx:112` - stale selection result and replacement/unmount URL revocation.
- `frontend/src/pages/creator/TrackThumbnailField.test.tsx:139` - format and oversize preflight rejection.
- `frontend/src/pages/creator/TrackThumbnailField.test.tsx:153` - existing non-square warning and load-failure no-warning behavior.
- `frontend/src/pages/creator/TrackUploadPage.test.tsx:40` - multi-row pending/invalid/valid submit behavior and canonical FormData file selection.
- `frontend/src/pages/creator/TrackEditPage.test.tsx:76` - legacy non-square warning with non-destructive no-thumbnail submit.
- `frontend/src/pages/creator/TrackEditPage.test.tsx:103` - existing image failure cannot produce a false warning.
- `frontend/src/pages/creator/TrackEditPage.test.tsx:114` - replacement pending/invalid/valid gating and submitted square file.
- `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx:1037` - existing Edit coverage waits for dimension validation before Save.

The exact 17-file WI-020 inventory is recorded in `deliverables/user/WI-20260808-ATS-020-summary.md`.

## Commands & Outputs

### Focused Backend Verification

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.service.image.CanonicalImageServiceTest" --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.service.TrackServiceAudioProcessingTest"
```

- PASS: Gradle `BUILD SUCCESSFUL` in 33.9s.
- Exact XML totals: 3 classes, 47 tests, 0 failures, 0 errors, 0 skipped.
- Per class: `CanonicalImageServiceTest` 14, `TrackServiceTest` 24, `TrackServiceAudioProcessingTest` 9.

### Focused Frontend Verification

```powershell
npm test -- src/pages/creator/TrackThumbnailField.test.tsx src/pages/creator/TrackUploadPage.test.tsx src/pages/creator/TrackEditPage.test.tsx
```

- PASS before and after ESLint remediation.
- Each run: 3 files, 10 tests, 0 failed.
- Total for this repeated command: 20 test executions, 10 unique tests.

```powershell
npm test -- src/test/coverage/publicAuthShell.coverage.test.tsx -t "uploads a validated track|loads and updates a track"
```

- PASS: 1 file, 2 selected tests, 0 failed; 26 unselected tests reported as skipped.

Unique focused tests: 47 backend plus 12 frontend equals 59. Including the 10-test frontend rerun, total test executions were 69.

### Static Verification

```powershell
npm run typecheck
```

- PASS on both executions; the final execution followed the contract-module extraction. `tsc --noEmit` exited 0.

```powershell
npm run lint
```

- Initial run: failed with 0 errors and 2 warnings in the new component (`react-refresh/only-export-components` and `react-hooks/exhaustive-deps`).
- Remediation: moved non-component exports to `trackThumbnail.ts` and invalidated cleanup with the captured selection version.
- Final run: PASS across all `frontend/src`, 0 errors and 0 warnings.

```powershell
npm run format
```

- PASS: full frontend Prettier check.

```powershell
npx prettier --check ../deliverables/user/WI-20260808-ATS-020-summary.md ../deliverables/agent/WI-20260808-ATS-020-evidence-pack.md
```

- PASS: both WI deliverables match Prettier formatting.

```powershell
git diff --check
```

- PASS: no whitespace errors. Existing CRLF-to-LF conversion warnings may be emitted for already dirty Java files; no line-ending rewrite command was run.

The full backend suite, full frontend suite, build, and coverage were intentionally not run under the explicit WI-020 verification boundary.

## Scope Preservation

- Branch was confirmed as `codex/v1-release-rehearsal-fixes` at `c7f779df35e2175405d837230edf61962e2bae42` before editing.
- Existing dirty WI-014 through WI-019 files were observed and preserved.
- The intentional untracked `output/client-demo-screenshots-20260716-140514.zip` remains untouched at 700,703 bytes.
- Album and Playlist service/UI files were inspected but not edited by WI-020.
- Existing Track thumbnail storage keys and files were not enumerated, rewritten, deleted, or migrated.
- No schema, data, dependency, provider, payment, storage, secret, Git-state, external-system, or network operation occurred.
- No files were deleted and no commit was created.

## Risks / Rollback

Risks:

- Browser natural-dimension validation is fast feedback; backend ImageIO decoding remains authoritative. A file accepted by the browser may still fail backend signature, MIME, frame, or decoder checks.
- The object URL preview shows the selected source, while persisted output is canonical JPEG. Geometry and cover placement match, but JPEG encoding can change compression, color profile, or transparent PNG background presentation.
- Existing-image warnings depend on successful browser loading. An unreadable legacy image is preserved and intentionally receives no shape warning.
- Focused tests do not replace full suites, coverage, build, or real-browser visual acceptance assigned to later QA WIs.

Rollback:

1. Revert the 15 production/test files listed in the user summary plus the two WI-020 deliverables while preserving all unrelated dirty files.
2. Revert `TRACK_THUMBNAIL_NOT_SQUARE`, the dedicated canonical method/policy, and both TrackService call sites as one backend unit with their tests.
3. Revert the thumbnail contract module, component/CSS, Upload/Edit connections, and four frontend test changes as one frontend unit.
4. No schema, data, provider, payment, storage, dependency, or external-system rollback is required.

## Follow-ups

- WI-020 is complete and WI-021 is unblocked.
- Later planned WIs retain full-suite, coverage, build, documentation, browser acceptance, security review, and cross-layer release gates.
