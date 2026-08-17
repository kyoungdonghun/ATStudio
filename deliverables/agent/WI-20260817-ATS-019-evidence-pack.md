---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: SE
category: evidence-pack
status: stable
related_wi: WI-20260817-ATS-019
dependencies:
  - path: WI-20260817-ATS-019-handoff.md
    reason: Approved implementation scope and output contract
  - path: WI-20260817-ATS-002-evidence-pack.md
    reason: Verified predecessor contract and runtime isolation baseline
  - path: ../../docs/SR/SR-98.md
    reason: Track thumbnail preview and long-term square-policy context
---

# Evidence Pack: WI-20260817-ATS-019

## Change Summary

- Added a frontend Track-thumbnail dimension guard matching the backend's
  4096px-per-dimension and 16,777,216-pixel bounds.
- Reused the existing preview image `load` dimensions without another decode or
  request, kept the temporary square switch independent and false, and revised
  guidance so it no longer presents a square as the only recommended shape.

## Scope / DoD Check

- [x] A decoded Track thumbnail becomes invalid when either natural dimension exceeds 4096px or total pixels exceed 16,777,216.
- [x] Simulated `DSC_2446.JPG` dimensions `6048×4032` produce the exact Korean field error before Upload or Edit submission.
- [x] `4096×4096` at the exact accepted dimension/pixel boundary becomes valid.
- [x] Ordinary non-square images within bounds remain valid while `TRACK_THUMBNAIL_SQUARE_REQUIRED` is false.
- [x] Resolution validation is a separate branch before the square-policy branch and therefore remains active if square validation is restored.
- [x] JPEG/PNG, 10 MB, decode failure, stale-load fencing, object URL cleanup, square `cover` preview, and current-image warning behavior remain intact.
- [x] Guidance has no `1:1 필수` copy and now recommends a 2048px long edge rather than only `2048x2048`.
- [x] Create and Edit inherit the shared field behavior with no second decode, request, or dependency.
- [x] Focused Vitest, TypeScript typecheck, ESLint, changed-file Prettier, and `git diff --check` pass.
- [x] No backend, Album/Playlist behavior, DB/storage data, service, tunnel, development-worktree, or commit mutation occurred.

## Reference Documents (Tier 0-2)

| Tier | Document                                                  | Reason                                                        |
| ---- | --------------------------------------------------------- | ------------------------------------------------------------- |
| 0    | `docs/standards/core-principles.md`                       | Approved scope, transparency, and sustainable implementation  |
| 0    | `docs/standards/development-standards.md`                 | React/TypeScript implementation and test standards            |
| 1    | `docs/policies/quality-gates.md`                          | Regression, traceability, and rollback gates                  |
| 1    | `docs/standards/evidence-pack-standard.md`                | Evidence metadata and reproducibility contract                |
| 2    | `.agents/skills/react-best-practices/AGENTS.md`           | React event, state, and performance guidance                  |
| 2    | `docs/standards/frontend-standards.md`                    | Active SPA validation and object URL lifecycle conventions    |
| REQ  | `deliverables/user/REQ-20260817-ATS-001.md`               | Approved client-acceptance branch deviation                   |
| WI   | `deliverables/user/WI-20260817-ATS-002-summary.md`        | Independent predecessor verification summary                  |
| WI   | `deliverables/agent/WI-20260817-ATS-002-evidence-pack.md` | Runtime/worktree isolation and retained-defense evidence      |
| SR   | `docs/SR/SR-98.md`                                        | Track thumbnail preview and long-term shape-policy context    |
| WI   | `deliverables/agent/WI-20260817-ATS-019-handoff.md`       | Exact frontend-only acceptance criteria and forbidden actions |

Supplemental procedure references:

- `.agents/skills/create-wi-evidence-pack/SKILL.md`
- `.agents/skills/test/SKILL.md`
- `.agents/skills/typecheck/SKILL.md`
- `.agents/skills/eslint/SKILL.md`
- `.agents/skills/prettier/SKILL.md`
- `.agents/skills/react-best-practices/SKILL.md`
- `docs/standards/documentation-standards.md`
- `docs/standards/glossary.md`

**Assignee:** `se`

**Task type:** frontend Track thumbnail validation alignment with focused regression tests

## Evidence Pointers

Production:

- `frontend/src/utils/validation.ts:35-38` - shared 4096px and 16,777,216-pixel constants; existing Album exports alias the same values without behavior change.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:14-21` - imports the shared bounds while retaining the independent temporary square flag set to `false`.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:126-158` - the existing image `load` event supplies natural dimensions; invalid decode, resolution, and square checks execute as separate ordered branches.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:141-151` - exact dimension/pixel calculation and stable Korean field error.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:199-203` - updated maximum-dimension guidance and shape-neutral long-edge recommendation.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:205-260` - unchanged preview event ownership, JPEG/PNG input, object URL-driven selected image, and existing-image warning rendering.

Read-only backend contract:

- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:35-36` - authoritative `MAX_DIMENSION = 4096` and `MAX_PIXELS = 16_777_216L` values copied into frontend constants.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:150-154` - decoded bounds are checked before the independent square-policy branch.
- The backend file has no WI-019 diff.

Tests:

- `frontend/src/pages/creator/TrackThumbnailField.test.tsx:63-74` - maximum-dimension and shape-neutral recommendation copy.
- `frontend/src/pages/creator/TrackThumbnailField.test.tsx:100-110` - ordinary non-square image remains valid.
- `frontend/src/pages/creator/TrackThumbnailField.test.tsx:113-127` - `6048×4032` `DSC_2446.JPG` error and blocked command.
- `frontend/src/pages/creator/TrackThumbnailField.test.tsx:129-139` - exact `4096×4096` boundary acceptance.
- `frontend/src/pages/creator/TrackThumbnailField.test.tsx:141-196` - stale selection, object URL cleanup, format/10 MB, and current-image warning regressions remain covered.
- `frontend/src/pages/creator/TrackUploadPage.test.tsx:43-93` - Upload blocks oversized JPEG, then submits a replacement non-square PNG within bounds.
- `frontend/src/pages/creator/TrackEditPage.test.tsx:76-103` - existing non-square warning remains non-blocking and guidance is updated.
- `frontend/src/pages/creator/TrackEditPage.test.tsx:117-153` - Edit blocks oversized JPEG, then submits a replacement non-square PNG within bounds.

Tracked diff before deliverable creation:

- `frontend/src/utils/validation.ts` (`+4/-2`)
- `frontend/src/pages/creator/TrackThumbnailField.tsx` (`+24/-3`)
- `frontend/src/pages/creator/TrackThumbnailField.test.tsx` (`+33/-3`)
- `frontend/src/pages/creator/TrackUploadPage.test.tsx` (`+19/-5`)
- `frontend/src/pages/creator/TrackEditPage.test.tsx` (`+20/-6`)
- Total: 5 frontend files, `+100/-19`.

## Reproduction / Verification

Commands ran in
`C:\Users\jm991\AppData\Local\ATStudio\worktrees\v1-client-acceptance-20260817`
unless the frontend workdir is stated.

| Command                                                                                                                                                                                                                     | Workdir         | Result                                 |
| --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- | -------------------------------------- |
| `npm test -- src/pages/creator/TrackThumbnailField.test.tsx src/pages/creator/TrackUploadPage.test.tsx src/pages/creator/TrackEditPage.test.tsx`                                                                            | `frontend/`     | PASS, 3 files / 19 tests / 0 failed    |
| `npm run typecheck`                                                                                                                                                                                                         | `frontend/`     | PASS, `tsc --noEmit`, exit 0           |
| `npm run lint`                                                                                                                                                                                                              | `frontend/`     | PASS, errors 0 / warnings 0, exit 0    |
| `npx prettier --check src/utils/validation.ts src/pages/creator/TrackThumbnailField.tsx src/pages/creator/TrackThumbnailField.test.tsx src/pages/creator/TrackUploadPage.test.tsx src/pages/creator/TrackEditPage.test.tsx` | `frontend/`     | PASS, all five changed files formatted |
| `git diff --check`                                                                                                                                                                                                          | repository root | PASS, exit 0                           |
| `git diff --stat`, `git diff --numstat`, `git diff --name-status`                                                                                                                                                           | repository root | 5 frontend files, `+100/-19`           |

## Results / Findings

### BLOCKER: 0

- None.

### MAJOR: 0

- None.

### MINOR: 0

- None.

### Contract observation

- With the present constants, satisfying both 4096px dimension limits also
  guarantees at most `4096×4096 = 16,777,216` pixels. The explicit pixel check
  is still retained to mirror the backend contract and remain correct if either
  limit changes independently later.
- The client check is preflight UX. Backend byte signature, MIME, APNG/frame,
  true decode, and storage canonicalization remain authoritative.

## Performance Check

- The new calculation reads `naturalWidth` and `naturalHeight` from the existing
  selected-preview `load` callback and performs constant-time multiplication and
  comparisons.
- No `Image`, `createImageBitmap`, canvas, object URL, fetch/Axios call, effect,
  dependency, or detail query was added.
- Existing selection-version stale-event fencing and effect-owned object URL
  cleanup remain unchanged.

## Risks / Rollback

Risks:

- Browser-decodable input may still fail authoritative Java ImageIO or byte-level
  defenses; the frontend message covers only the known resolution mismatch.
- Non-square images within bounds remain cropped by the intentionally unchanged
  square `cover` presentation.
- Authenticated public file selection and served-module reflection are deferred
  to WI-20260817-ATS-020.

Rollback:

1. Remove only the `pixelCount` and shared-bound invalid branch from
   `TrackThumbnailField.tsx`.
2. Restore the previous guidance and `2048x2048px` recommendation.
3. Remove `IMAGE_MAX_DIMENSION` and `IMAGE_MAX_PIXELS`, then restore the two
   Album constant literals in `validation.ts`.
4. Revert only the WI-019 expectations and scenarios in the three focused test files.
5. Preserve `TRACK_THUMBNAIL_SQUARE_REQUIRED = false` and all WI-001 temporary
   non-square behavior.

No backend, DB, storage, service, tunnel, dependency, or public-data rollback is
required.

## Follow-ups

- WI-20260817-ATS-020 is unblocked for independent diff review, authenticated
  browser/API validation where available, and controlled reflection in the
  client-review server.
