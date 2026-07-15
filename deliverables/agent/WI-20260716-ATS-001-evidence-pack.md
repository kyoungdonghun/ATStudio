# Evidence Pack: WI-20260716-ATS-001

## Summary (one-liner)

- Restored detail-page waveform propagation and added stale persisted same-Track recovery with focused regression coverage.

## Scope / DoD Check

- [x] Detail-page first playback forwards `track.waveformData` unchanged.
- [x] A same-ID player Track with missing or different waveform data is replaced by the latest detail mapping.
- [x] A same-ID player Track with matching waveform data retains normal resume behavior.
- [x] Missing/null API waveform data continues to use the existing `WaveformCanvas` flat-line fallback.
- [x] Playback, seek, `PlayerBar`/`WaveformCanvas` design, backend, and Official Download behavior remain unchanged.
- [x] Focused and full frontend tests, typecheck, scoped ESLint, owned-file Prettier, and diff checks pass.

## Reference Documents (Tier 0-2)

**Injected Context** (from `deliverables/agent/WI-20260716-ATS-001-handoff.md`):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution and approved-scope boundary |
| 0 | `docs/standards/development-standards.md` | React implementation and verification standards |
| 0 | `docs/standards/documentation-standards.md` | Evidence document format and language policy |
| 0 | `docs/standards/glossary.md` | Public Listening and Official Download terminology |
| 1 | `docs/policies/quality-gates.md` | Required regression and traceability checks |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React implementation guidance |
| 2 | `deliverables/user/REQ-20260715-ATS-001.md` | Approved full-length listening and player correction scope |
| 2 | `deliverables/agent/WI-20260715-ATS-019-evidence-pack.md` | Existing player state correction evidence |
| 2 | `deliverables/agent/WI-20260715-ATS-024-evidence-pack.md` | Browser playback and Range correction evidence |
| 2 | `docs/design/usecase/sound-track.md` | Current Public Listening and Official Download contract |

**Injection rules applied:** assignee `se`; task type `implementation`; read order Tier 0 -> Tier 1 -> Tier 2 -> source and test files.

## Evidence Pointers

### Detail-to-player mapping

- `frontend/src/pages/public/TrackDetailPage.tsx:103-124` - creates the complete player `Track`, including the API-provided `waveformData`.
- `frontend/src/pages/public/TrackDetailPage.tsx:125-126` - normalizes `undefined` and `null` before comparing current and detail waveform values.
- `frontend/src/pages/public/TrackDetailPage.tsx:154-162` - preserves pause/resume only when ID and waveform data match; otherwise rehydrates through `playTrack`.

### Regression coverage

- `frontend/src/pages/public/TrackDetailPage.test.tsx` - verifies the full mapping on first play, stale same-ID recovery, and matching same-ID resume.
- `frontend/src/layouts/PlayerBar.tsx:102-110` - existing read-only JSON peak parsing from `currentTrack.waveformData`.
- `frontend/src/components/player/WaveformCanvas.tsx:32-41` - existing read-only flat-line fallback for an empty peak list.

## Commands & Outputs

| Command | Exit | Result |
|---------|------|--------|
| `npm test -- --run src/pages/public/TrackDetailPage.test.tsx` | 0 | PASS; 1 file, 3 tests |
| `npm run typecheck` | 0 | PASS; no TypeScript errors |
| `npm exec eslint -- src/pages/public/TrackDetailPage.tsx src/pages/public/TrackDetailPage.test.tsx --max-warnings 0` | 0 | PASS; no warnings or errors |
| `npm exec prettier -- --check src/pages/public/TrackDetailPage.tsx src/pages/public/TrackDetailPage.test.tsx` | 0 | PASS after formatting only the owned source file |
| `npm test -- --run` | 0 | PASS; 20 files, 82 tests |
| `git diff --check` | 0 | PASS; no whitespace errors |

## Risks / Rollback

### Risks

- Rehydrating stale same-ID data restarts that Track through the existing `playTrack` path. This is intentional because the store exposes no metadata-only replacement operation and the approved scope forbids changing the player store.
- If the detail API has no waveform data, the normalized comparison preserves normal same-Track resume and the existing flat-line fallback.

### Rollback

1. Remove `waveformData` and stale-comparison logic from `TrackDetailPage.tsx` only if intentionally restoring the defect.
2. Remove `TrackDetailPage.test.tsx` with the mapping rollback.
3. No database, storage, player-store, playback, download, Subscription, quota, history, or License rollback is required.

## Execution Boundaries

- No backend, `PlayerBar`, `WaveformCanvas`, API contract, database, runtime, or product-policy file changed.
- Pre-existing runtime logs and `WI-20260716-ATS-001-handoff.md` were left untouched.
- No file was staged or committed.
