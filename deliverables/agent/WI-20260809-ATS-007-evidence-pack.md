# Evidence Pack: WI-20260809-ATS-007

## TL;DR

Independent frontend media/tag/playback review completed with **0 BLOCKER / 3 MAJOR / 0 MINOR**. Product code was not changed. `WI-20260808-ATS-029` remains blocked.

## Identity

| Field | Value |
|---|---|
| WI | `WI-20260809-ATS-007` |
| REQ | `REQ-20260808-ATS-004` |
| Agent | `cr` |
| Depends on | `WI-20260809-ATS-006` |
| Blocks | `WI-20260808-ATS-029` |
| Review date | 2026-08-09 |
| Change class | Read-only independent review; two deliverables only |

## Scope / DoD Check

- [x] Reviewed the frontend portions of SR-94, SR-95, SR-98, SR-100, and SR-101.
- [x] Reviewed URL/API/UI consistency for genre, mood, instrument, and Usage.
- [x] Reviewed Usage display-only `#` handling.
- [x] Reviewed image selection, preview, pending/invalid state, and unchanged-image behavior.
- [x] Reviewed PlayableTrack aggregate conversion, batch hydration, local persistence, history, queue, and catalog entry points.
- [x] Reviewed buffering threshold, real-error separation, duration, waveform, and seek state.
- [x] Checked frontend HTTP fan-out for row-by-row hydration patterns.
- [x] Compared only exact backend controller/DTO signatures needed for the frontend wire contract.
- [x] Recorded confirmed findings, missing tests, recommended fixes, no-findings, residual risks, rollback, and WI-029 status.
- [x] Made no code, test, data, schema, secret, ZIP, commit, push, or external-service change.

## Reference Documents

| Tier | Document | Review use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Language, transparency, active React SPA, reviewer critique baseline |
| 0 | `docs/standards/development-standards.md` | Evidence pointers, two-set deliverables, focused test quality |
| 0 | `docs/standards/documentation-standards.md` | English documentation and evidence structure |
| 0 | `docs/standards/glossary.md` | Public Listening and download-policy distinction |
| 1 | `.claude/agents/cr.md` | Evidence-backed review classification and output contract |
| 2 | `deliverables/agent/WI-20260809-ATS-007-handoff.md` | Authoritative scope, constraints, acceptance criteria, and outputs |
| 2 | `deliverables/user/REQ-20260808-ATS-004.md` | Approved invariants and work-plan boundary |
| 2 | `docs/SR/SR-94.md`, `docs/SR/SR-95.md` | Tag duplicate/error and display-state requirements |
| 2 | `docs/SR/SR-98.md` | Square thumbnail preview/canonicalization contract |
| 2 | `docs/SR/SR-100.md` | Four-type tag navigation and repeated-parameter contract |
| 2 | `docs/SR/SR-101.md` | Buffering, PlayableTrack, waveform, duration, and N+1 contract |
| 2 | `deliverables/user/WI-20260809-ATS-006-summary.md` | Independent backend review decision and downstream block status |
| Guideline | `.agents/skills/create-wi-evidence-pack/SKILL.md` | Reproducible evidence-pack structure |
| Guideline | `.agents/skills/react-best-practices/SKILL.md` | Client request fan-out and render-performance review |

## Reviewed Components And Contracts

### Frontend Production

- Tag management/discovery: `frontend/src/utils/tagName.ts`, `frontend/src/pages/admin/TagManagePage.tsx`, `frontend/src/api/tags.ts`, `frontend/src/api/tracks.ts`, `frontend/src/pages/public/HomePage.tsx`, `frontend/src/pages/public/TrackListPage.tsx`, `frontend/src/components/filter/TagFilterModal.tsx`, `frontend/src/components/ui/FilterChip.tsx`.
- Image selection/preview: `frontend/src/pages/creator/trackThumbnail.ts`, `frontend/src/pages/creator/TrackThumbnailField.tsx`, `frontend/src/pages/creator/TrackThumbnailField.module.css`, `frontend/src/pages/creator/TrackUploadPage.tsx`, `frontend/src/pages/creator/TrackEditPage.tsx`.
- Player contract: `frontend/src/types/index.ts`, `frontend/src/utils/playableTrack.ts`, `frontend/src/store/playerStore.ts`, `frontend/src/layouts/PlayerBar.tsx`, `frontend/src/components/player/HistoryModal.tsx`, `frontend/src/components/player/PlaylistDrawer.tsx`.
- Catalog entry points: `frontend/src/api/albums.ts`, `frontend/src/api/playlists.ts`, `frontend/src/api/downloads.ts`, `frontend/src/pages/public/AlbumDetailPage.tsx`, `frontend/src/pages/public/TrackDetailPage.tsx`, `frontend/src/pages/subscriber/PlaylistDetailPage.tsx`, `frontend/src/pages/subscriber/LikeListPage.tsx`, `frontend/src/pages/subscriber/DownloadHistoryPage.tsx`, `frontend/src/pages/subscriber/PlayHistoryPage.tsx`.

### Exact Backend Signatures Compared

- `src/main/java/com/atstudio/atstudio/controller/TrackController.java:49-63,85-109`
- `src/main/java/com/atstudio/atstudio/controller/TagController.java:36-59`
- `src/main/java/com/atstudio/atstudio/dto/track/TrackSearchRequest.java:9-24`
- `src/main/java/com/atstudio/atstudio/dto/track/PlayableTrackBatchRequest.java:10-14`
- `src/main/java/com/atstudio/atstudio/dto/track/PlayableTrackResponse.java:9-18`
- `src/main/java/com/atstudio/atstudio/dto/album/AlbumTrackItemResponse.java:6-14`
- `src/main/java/com/atstudio/atstudio/dto/playlist/PlaylistTrackItemResponse.java:6-16`
- `src/main/java/com/atstudio/atstudio/dto/like/LikeResponse.java:8-18`
- `src/main/java/com/atstudio/atstudio/dto/download/DownloadHistoryItemResponse.java:16-27`

No backend service, repository, entity, media-analysis, authorization, or persistence internals were re-reviewed.

## Confirmed Findings

### MAJOR-001 - Aggregate nullable wire fields conflict with mapper key requirements

- **Evidence pointers:**
  - `frontend/src/utils/playableTrack.ts:22-33`
  - `src/main/java/com/atstudio/atstudio/dto/album/AlbumTrackItemResponse.java:6-14`
  - `src/main/java/com/atstudio/atstudio/dto/playlist/PlaylistTrackItemResponse.java:6-16`
  - `src/main/java/com/atstudio/atstudio/dto/like/LikeResponse.java:8-18`
  - `frontend/src/pages/public/AlbumDetailPage.tsx:65-70,133-139,185-194`
  - `frontend/src/pages/subscriber/PlaylistDetailPage.tsx:74-88,119-125`
  - `frontend/src/pages/subscriber/LikeListPage.tsx:77-110`
  - `frontend/src/utils/playableTrack.test.ts:25-51`
- **Impact:** Common no-thumbnail aggregate rows can throw before or during playback. Omitted null waveform values can enter PlayableTrack as `undefined`, making runtime persistence validation and waveform state inconsistent.
- **Reasoning:** `@JsonInclude(NON_NULL)` omits nullable keys, while the frontend wire types assume explicit null. The mapper rejects absent thumbnail keys and does not normalize an absent waveform key.
- **Missing test:** Contract-shaped omitted-key objects across album, playlist, like, context, Play All, individual play, queue persistence, and reload.
- **Recommended fix:** Make wire fields optional, normalize omitted nullable values to explicit null at the boundary, retain ID validation, and add omitted-key regressions.

### MAJOR-002 - Previous duration remains authoritative after Track selection changes

- **Evidence pointers:**
  - `frontend/src/store/playerStore.ts:519-538`
  - `frontend/src/layouts/PlayerBar.tsx:159-175`
  - `frontend/src/layouts/PlayerBar.tsx:595-600,713-729`
  - `frontend/src/store/playerStore.test.ts:156-190,232-249`
- **Impact:** A newly selected Track can temporarily display and seek on the preceding Track's duration, affecting waveform progress, timestamps, keyboard/click seek, and ARIA range values.
- **Reasoning:** The selection state transition resets time but omits duration. `PlayerBar` prefers the existing nonzero store duration over the new PlayableTrack duration until browser metadata arrives.
- **Missing test:** Track A metadata -> Track B selection before Track B metadata, with different durations and assertions for state, progress, seek, and ARIA values.
- **Recommended fix:** Commit `duration: track.duration` with the new current Track, then update from current-source browser metadata under identity protection.

### MAJOR-003 - Taxonomy failure or omission hides active URL/API tag state

- **Evidence pointers:**
  - `frontend/src/pages/public/TrackListPage.tsx:69-80`
  - `frontend/src/pages/public/TrackListPage.tsx:142-162`
  - `frontend/src/pages/public/TrackListPage.tsx:186-198`
  - `frontend/src/pages/public/TrackListPage.tsx:260-323`
  - `frontend/src/pages/public/TrackListPage.tsx:488-614`
  - `frontend/src/pages/public/TrackListPage.test.tsx:393-442,505-590`
- **Impact:** Results remain filtered by hidden values while selected chips disappear; Usage can lose its entire row. Deep links, deleted tags, and transient taxonomy failures therefore produce misleading result state.
- **Reasoning:** Track queries use raw URL arrays, but visible chips use only fetched taxonomy. One failed call rejects the shared `Promise.all` and is silently ignored. No active-value fallback is rendered.
- **Missing test:** Active repeated values for all four types with one taxonomy rejection or an omitted active tag, including visible selection, raw Usage query values, reset, and empty/error result state.
- **Recommended fix:** Expose taxonomy load/error state, merge active URL values into the chip model, provide scoped retry, and apply `#` only at Usage rendering.

## No Additional Confirmed Findings

- Four tag families use repeated query parameters and preserve special-character values through `URLSearchParams`.
- Reviewed Usage display paths add `#` only at render time; create/update/search payloads remain raw.
- Thumbnail selection uses JPEG/PNG and size checks, actual decoded dimensions, exact-square blocking, stable object-URL cleanup, and a 1:1 center-cover preview.
- Existing thumbnails are not included in update multipart requests unless a replacement is selected.
- Buffering uses one 2,000 ms pending timer with generation/attempt/Track checks; recovery and real errors cancel it.
- Real playback errors remain distinct from nonfatal sustained buffering in `PlayerBar` live-region semantics.
- Persisted player and history restoration use one batch request capped at 100 IDs. No row-by-row detail HTTP hydration was found in reviewed entry points.
- Public playback uses `/api/tracks/{id}/stream`; download remains a separate entitlement-controlled call.

## Commands And Outputs

### Static Review

- `git status --short` -> confirmed the approved shared dirty worktree and no staged changes.
- `git branch --show-current` -> `codex/v1-release-rehearsal-fixes`.
- Scoped `git diff --name-status` and targeted `git diff --unified=... -- <handoff files>` -> isolated relevant frontend changes and exact DTO/controller signatures.
- Targeted `rg -n` -> located mapper call sites, waveform/duration flows, tag URL/API/chip state, tests, and policy boundaries.
- Numbered `Get-Content` -> captured the file-line evidence listed above.
- No secret search output, environment values, external request, live API, browser session, ZIP access, commit, or push occurred.

### Focused Test Execution

Working directory: `frontend`

```text
npm test -- src/utils/playableTrack.test.ts src/store/playerStore.test.ts src/pages/public/TrackListPage.test.tsx src/pages/creator/TrackThumbnailField.test.tsx
```

- Exit code: 0
- Vitest: 4.1.4
- Test files: 4 passed / 4
- Tests: 49 passed / 49
- Failures: 0
- Reported duration: 7.48 seconds

The focused pass confirms existing intended-path coverage. It does not invalidate the findings because the missing cases use omitted nullable wire keys, a second Track before metadata, and failed/omitted taxonomy with active URL state.

### Prior Gate Evidence Inspected

- `WI-20260808-ATS-024`: frontend coverage gate recorded 70 files / 579 tests passing.
- `WI-20260808-ATS-025`: TypeScript `tsc --noEmit` passing.
- `WI-20260808-ATS-026`: ESLint and Prettier checks passing.
- `WI-20260808-ATS-027`: frontend production build passing.

These prior gates are supporting evidence only; no full suite or build was rerun for WI-007.

## Residual Risks

- Real-browser throttling, native `waiting`/`stalled`/`playing` ordering, timer throttling, and canvas rendering were not exercised.
- `frontend/src/store/playerStore.ts:130-141` has a one-shot metadata fallback without a Track/generation token. Actual exposure depends on browser pre-metadata seek behavior, so it remains a residual risk pending a focused browser or simulated-throw test.
- Browser preview pixels were not compared against the backend's canonical JPEG output; shape/crop behavior is aligned, but orientation, color, and compression differences remain unverified.
- Production waveform payload size, canvas cost, maximum batch memory, and MySQL query plans were not measured.
- Cross-tab localStorage changes do not participate in the process-local generation fences.
- Existing noncanonical legacy tags and non-square thumbnails remain unchanged by approved policy.

## Files Changed

- `deliverables/user/WI-20260809-ATS-007-summary.md` - user-facing independent review decision.
- `deliverables/agent/WI-20260809-ATS-007-evidence-pack.md` - this evidence pack.

No product, test, schema, data, configuration, dependency, secret, ZIP, generated artifact, or Git-history change was made.

## Rollback

Remove only the two WI-007 deliverables above. No product rollback, data repair, schema rollback, test restoration, build cleanup, commit reversal, or external-system action is required.

## Follow-Up And WI Chain

- `WI-20260809-ATS-007` is complete as an independent review with changes required.
- `WI-20260808-ATS-029` remains **BLOCKED** by MAJOR-001 through MAJOR-003 and the two backend MAJOR findings from WI-006.
- Required clearance: repair each finding, add the named focused regressions, rerun scoped frontend quality checks, and perform independent re-review before WI-029 or the final integration gate proceeds.
