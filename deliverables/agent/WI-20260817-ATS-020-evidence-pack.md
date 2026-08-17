---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: QA Integration
category: evidence-pack
status: stable
related_wi: WI-20260817-ATS-020
dependencies:
  - path: WI-20260817-ATS-020-handoff.md
    reason: Independent verification scope and output contract
  - path: WI-20260817-ATS-019-evidence-pack.md
    reason: Implementation evidence subject to independent verification
  - path: ../../docs/SR/SR-98.md
    reason: Track thumbnail shape-policy context
---

# Evidence Pack: WI-20260817-ATS-020

## Summary (one-liner)

- Independently verified the frontend Track-thumbnail resolution guard, exact
  backend-aligned boundaries, Upload/Edit blocking and recovery, served runtime
  modules, and client/development worktree isolation with no verification code changes.

## Scope / DoD Check

- [x] Confirmed exact `4096px` per-dimension and `16,777,216`-pixel comparisons and the Korean field message.
- [x] Confirmed the real `DSC_2446.JPG` metadata is `6048×4032`, `24,385,536` pixels, and `7.815MiB`, so it fails both resolution bounds while passing 10 MB.
- [x] Confirmed `4096×4096` passes at the exact boundary.
- [x] Confirmed bounded non-square inputs pass while the square-policy flag is false.
- [x] Confirmed the resolution and square-policy branches are independent and ordered.
- [x] Confirmed Upload and Edit block oversized selections and submit a valid bounded replacement.
- [x] Confirmed no added decode, request, object URL, effect, or dependency.
- [x] Confirmed no WI-019/020 backend, Album behavior, Playlist behavior, DB/storage, or development-worktree change.
- [x] Re-ran focused Vitest, TypeScript typecheck, ESLint, changed-file Prettier, and diff check with exit 0.
- [x] Confirmed final local/public frontend and API health is HTTP 200.
- [x] Confirmed local/public served modules contain the same constants, guard, flag, guidance, and error message.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Approved scope and transparent evidence |
| 0 | `docs/standards/development-standards.md` | React/TypeScript review and test standards |
| 1 | `docs/policies/quality-gates.md` | Regression and traceability gates |
| 1 | `docs/standards/evidence-pack-standard.md` | Evidence metadata and reproducibility contract |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React event and performance guidance |
| 2 | `docs/standards/frontend-standards.md` | Active SPA validation conventions |
| 2 | `docs/design/api-spec.md` | Active REST and frontend/backend contract context |
| REQ | `deliverables/user/REQ-20260817-ATS-001.md` | Approved client-acceptance branch deviation |
| WI | `deliverables/agent/WI-20260817-ATS-019-handoff.md` | Implementation acceptance criteria |
| WI | `deliverables/user/WI-20260817-ATS-019-summary.md` | Implementer-reported result under review |
| WI | `deliverables/agent/WI-20260817-ATS-019-evidence-pack.md` | Implementation evidence under review |
| SR | `docs/SR/SR-98.md` | Track thumbnail preview and long-term shape policy |

**Procedure references:**

- `.agents/skills/create-wi-evidence-pack/SKILL.md`
- `.agents/skills/test/SKILL.md`
- `.agents/skills/typecheck/SKILL.md`
- `.agents/skills/eslint/SKILL.md`
- `.agents/skills/prettier/SKILL.md`

**Assignee:** `qa-integ`

**Task type:** independent frontend/backend contract and runtime integration verification

## Evidence Pointers

### Product and contract

- `frontend/src/utils/validation.ts:35-38` — shared `4096` and `16_777_216`
  values; Album exports alias the same values without a numerical behavior change.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:18-21` — independent
  temporary square flag remains `false` while the pixel label derives from the shared bound.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:124-164` — the existing
  selected-preview `load` callback reads natural dimensions once; decode, resolution,
  square, and valid branches remain ordered and separate.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:141-152` — strict
  greater-than comparisons and exact Korean resolution error.
- `frontend/src/pages/creator/TrackThumbnailField.tsx:197-203` — explicit
  4096px guidance and shape-neutral 2048px long-edge recommendation.
- `frontend/src/pages/creator/TrackUploadPage.tsx:304-305,574` — pending or invalid
  Track thumbnails disable Upload.
- `frontend/src/pages/creator/TrackEditPage.tsx:164-175,453` — pending or invalid
  replacement thumbnails block Edit submission.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:35-40`
  — backend bounds are `4096` and `16_777_216L`; square restoration remains independent.
- `src/main/java/com/atstudio/atstudio/service/image/CanonicalImageService.java:142-156`
  — backend checks encoded and decoded bounds before its square-policy branch.

### Focused tests

- `frontend/src/pages/creator/TrackThumbnailField.test.tsx:100-139` — bounded
  non-square acceptance, reported `6048×4032` rejection, and exact `4096×4096` acceptance.
- `frontend/src/pages/creator/TrackThumbnailField.test.tsx:141-195` — stale-load,
  object URL cleanup, file format, 10 MB, and existing-image warning regression coverage.
- `frontend/src/pages/creator/TrackUploadPage.test.tsx:43-96` — Upload blocks the
  oversized JPEG and then submits a bounded `800×600` non-square replacement exactly once.
- `frontend/src/pages/creator/TrackEditPage.test.tsx:117-148` — Edit blocks the
  oversized JPEG and then submits a bounded `900×600` non-square replacement exactly once.

### Diff and isolation

- Product/test diff contains exactly five frontend files: two production files
  and three focused test files, total `+100/-19` before WI-020 deliverables.
- `git diff -- src/main/java` is empty; no backend file is in the WI-019/020 diff.
- No Album or Playlist component is in the diff. Album numerical exports still
  resolve to the same `4096` and `16_777_216` values.
- Development worktree snapshot: branch `codex/v1-release-rehearsal-fixes`, HEAD
  `3e1123416bd106fbeb8b1ad2b3f8bf343a54100e`, tracked diff count `0`.
- Verification created only this Evidence Pack and
  `deliverables/user/WI-20260817-ATS-020-summary.md`; it did not modify product/test code.

### Runtime reflection

- Port 5173 listener: Node PID `36108`, command path contains the client-acceptance
  worktree and does not contain the development worktree.
- Port 8080 listener: Java PID `18060`, command path contains the client-acceptance
  worktree and does not contain the development worktree.
- Runtime log root (pointer only):
  `C:\Users\jm991\AppData\Local\ATStudio\acceptance-client-detached-20260817\20260817T142747Z`.
- Local/public root SHA-256 match:
  `1417ceba867e90506d0663476f3a8f76da48bcd2fa813ad41d8ae5374293cd4e`.
- Local/public `/api/tracks` SHA-256 match:
  `53e4801a2c9eec200bac78670d35426cab2581d80c238c8d98d2be06ec653fc4`.
- Local/public served `validation.ts` SHA-256 match:
  `1c528f6264623fbbaf41841b23dbc2233781b8f67ec1d7c1ebcf9bcf4c1bbe79`.
- Local/public served `TrackThumbnailField.tsx` SHA-256 match:
  `53682dc5dfa4bca25ed10e7e701635f83aec23f20075345d21101440d5dfdecd`.
- Served modules contain `4096`, `16777216`, both greater-than guards, the
  pixel-count guard, the exact Korean error text, the new guidance, and
  `TRACK_THUMBNAIL_SQUARE_REQUIRED = false`.

## Commands & Outputs

Commands ran from
`C:\Users\jm991\AppData\Local\ATStudio\worktrees\v1-client-acceptance-20260817`
unless `frontend/` is stated.

| Command | Workdir | Result |
| --- | --- | --- |
| `npm test -- src/pages/creator/TrackThumbnailField.test.tsx src/pages/creator/TrackUploadPage.test.tsx src/pages/creator/TrackEditPage.test.tsx` | `frontend/` | PASS, 3 files / 19 tests / 0 failed, exit 0, 5.53s Vitest duration |
| `npm run typecheck` | `frontend/` | PASS, `tsc --noEmit`, exit 0 |
| `npm run lint` | `frontend/` | PASS, ESLint errors 0 / warnings 0, exit 0 |
| `npx prettier --check src/utils/validation.ts src/pages/creator/TrackThumbnailField.tsx src/pages/creator/TrackThumbnailField.test.tsx src/pages/creator/TrackUploadPage.test.tsx src/pages/creator/TrackEditPage.test.tsx` | `frontend/` | PASS, all 5 files formatted, exit 0 |
| `git diff --check` | repository root | PASS, exit 0 |
| `System.Drawing.Image::FromFile('C:\Users\jm991\Downloads\DSC_2446.JPG')` plus file metadata | read-only | `6048×4032`, `24,385,536` pixels, `8,194,214` bytes / `7.815MiB` |
| `Invoke-WebRequest` for local/public root and `/api/tracks` | read-only | Four HTTP 200 responses; paired hashes match |
| `Invoke-WebRequest` for local/public served `validation.ts` and `TrackThumbnailField.tsx` | read-only | Four HTTP 200 responses; paired hashes and required content match |
| `netstat -ano` plus sanitized process-path membership check | read-only | 5173/8080 target `true`, development `false` |

## Tests

- **Status:** PASS
- **Mode:** JS/Vitest
- **Files:** 3 passed / 3
- **Tests:** 19 passed / 19; failed 0; skipped 0
- **Focused behavior:** reported photo rejection, exact boundary acceptance,
  bounded non-square acceptance, Upload/Edit request blocking and replacement,
  format/size/decode/stale-load/object-URL/current-image regressions.

## Results / Findings

### BLOCKER: 0

- None.

### MAJOR: 0

- None.

### MINOR: 0

- None.

### Operational observation

- At the first runtime check there were no 5173/8080 listeners and the public
  endpoint returned 502. The MA restored detached target-worktree services without
  changing the WI-019 product diff. The final independent recheck documented above
  passed for both local and public routes and served modules.

### Verification limitation

- No authenticated public-admin upload was submitted because the WI forbids public
  data mutation and no safely reusable authenticated session was provided. The
  pre-submit behavior is covered by component, Upload-page, and Edit-page tests and
  the exact code is present in the live served module.

## Performance

- Resolution validation performs two comparisons and one multiplication in the
  existing selected-preview image `load` callback.
- No second `Image`, `createImageBitmap`, canvas, fetch/Axios call, effect,
  object URL, package dependency, or backend request was introduced.
- Existing selection-version stale-event fencing and effect-owned object URL
  cleanup remain intact.

## Risks / Rollback

Risks:

- Browser decoding remains only a preflight UX check. Backend signature, MIME,
  frame-count, Java ImageIO decode, decoded bounds, and canonicalization remain authoritative.
- Bounded non-square images remain subject to the intentionally unchanged square
  `cover` presentation crop.

Rollback:

1. Remove only the `pixelCount` and shared-bound invalid branch from
   `TrackThumbnailField.tsx`.
2. Restore the former Track guidance and `2048x2048px` recommendation.
3. Remove `IMAGE_MAX_DIMENSION` and `IMAGE_MAX_PIXELS`, then restore the two Album
   numeric literals in `validation.ts`.
4. Revert only the WI-019 expectations/scenarios in the three focused test files.
5. Preserve `TRACK_THUMBNAIL_SQUARE_REQUIRED = false` and all WI-001 temporary
   non-square behavior.

No backend, DB, storage, service, tunnel, dependency, or public-data rollback is required.

## Follow-ups

- None required for WI-020 completion.
