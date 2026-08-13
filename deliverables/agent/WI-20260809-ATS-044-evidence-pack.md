---
version: 1.0
last_updated: 2026-08-13
project: ATS
owner: se
category: evidence-pack
status: draft
related_wi: WI-20260809-ATS-044
---

# Evidence Pack - WI-20260809-ATS-044

## Change Summary

- Added localized, application-owned Track/Album detail recovery and bounded list retry without exposing raw transport text.
- Canonicalized public catalog pages, aligned Album grid/list projection to page size 20, preserved compatible query state between views, and redirected beyond-last pages with one bounded follow-up request.
- Added AbortSignal plus generation ownership to Album list/detail reads so retired routes, queries, and views cannot commit stale data, error, empty, or loading state.
- Displayed Album Track positions as one-based without changing stored zero-based order or Track identity.
- Made visible Track-list context owner-scoped so page departure clears only that page context while preserving the active Track, queue, shuffle, and repeat state.
- Clamped restored, seeked, persisted, and rendered playback progress to finite media bounds while preserving complete public Track playback.

## Scope / DoD Check

- [x] `CR-031-024`: missing/error Track and Album detail use localized Retry, Back, and Home recovery with no raw provider/server text.
- [x] `CR-031-025`: malformed, non-integer, zero, negative, and beyond-last catalog pages have one canonical 1-based URL and bounded request behavior.
- [x] `CR-031-026`: Album grid/list switches preserve compatible sort/page query state and both use page size 20.
- [x] `CR-031-028`: Album Track positions display `track_order + 1`; persisted order and IDs are unchanged.
- [x] `CR-031-031`: Album list/detail effects abort retired requests and fence every state commit with latest generation ownership.
- [x] `CR-031-032`: every visible-list publisher owns a cleanup; stale cleanup cannot clear a newer context and durable playback state remains intact.
- [x] `CR-031-036`: restored and seeked progress is finite, non-negative, and clamped to the decoded duration when known; PlayerBar time and waveform share the same bound.
- [x] Full public playback remains unchanged; no preview duration, playback entitlement gate, backend route, schema, or data mutation was introduced.
- [x] Independent QA review, StrictMode remediation, full frontend/backend gates, current-state documentation, and diff validation pass.

## Reference Documents

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `AGENTS.md` | Repository workflow and role rules |
| 0 | `docs/standards/core-principles.md` | Constitution and domain boundaries |
| 0 | `docs/standards/development-standards.md` | React implementation and test expectations |
| 1 | `docs/policies/quality-gates.md` | Required verification gates |
| 1 | `docs/standards/evidence-pack-standard.md` | Evidence and reproducibility contract |
| 1 | `docs/standards/frontend-standards.md` | Current catalog/player contract |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React effect and state guidance |
| 2 | `docs/design/usecase/sound-track.md` | Track list/detail/full playback contract |
| 2 | `docs/design/usecase/sound-album.md` | Album list/detail/order contract |
| 2 | `docs/ui/screen-flow.md` | Current cross-screen behavior |
| 2 | `docs/ui/atstudio-front-list.md` | Current frontend inventory and behavior |
| 2 | `deliverables/user/REQ-20260809-ATS-001.md` | Approved remediation requirement |
| 2 | `deliverables/agent/WI-20260809-ATS-044-handoff.md` | Scope, roots, prohibitions, and DoD |

## Evidence Pointers

### Catalog Page and Request Ownership

- `frontend/src/utils/catalogPagination.ts` - shared page size 20, strict positive-integer normalization, total-page calculation, and query-preserving view target.
- `frontend/src/pages/public/TrackListPage.tsx` - invalid-page replacement before fetch, one bounded beyond-last correction, and owner cleanup for visible Track context.
- `frontend/src/pages/public/AlbumListPage.tsx` and `AlbumListImagePage.tsx` - projection parity, preserved query state, AbortSignal, generation fence, beyond-last correction, and one in-flight retry.
- `frontend/src/api/albums.ts` - optional AbortSignal for public Album list/detail reads.
- `frontend/src/pages/public/AlbumListPages.test.tsx` - view parity, malformed pages, beyond-last correction, stale response rejection, raw-error exclusion, and retry ownership.

### Detail Recovery and Ordering

- `frontend/src/components/catalog/CatalogDetailRecovery.tsx` and module CSS - localized Retry, Back with direct-entry Home fallback, and explicit Home link.
- `frontend/src/pages/public/TrackDetailPage.tsx` and `AlbumDetailPage.tsx` - safe positive IDs, latest-request ownership, localized recovery, and no raw error rendering.
- `frontend/src/pages/public/AlbumDetailPage.tsx` - one-based display only and owner-scoped context cleanup.
- `frontend/src/pages/public/TrackDetailPage.test.tsx` and `AlbumDetailPage.test.tsx` - missing recovery, duplicate retry fencing, stale route response, one-based display, StrictMode ownership, and complete duration playback projection.

### Player Context and Progress

- `frontend/src/store/playerStore.ts:setTrackListContext` - monotonic owner token; only the latest owner cleanup clears visible context.
- `frontend/src/pages/subscriber/DownloadHistoryPage.tsx`, `LikeListPage.tsx`, and `PlaylistDetailPage.tsx` - existing context publishers return the owner cleanup from their effects.
- `frontend/src/utils/playbackProgress.ts` - finite positive duration selection and bounded current-time normalization.
- `frontend/src/store/playerStore.ts` - hydration, metadata, timeupdate, seek, and persistence use bounded progress. Runtime decoded duration takes precedence over stale Track metadata during persistence.
- `frontend/src/layouts/PlayerBar.tsx` - desktop/mobile time, waveform ratio, slider values, and keyboard seek use one bounded current time.
- `frontend/src/store/playerPersistence.test.ts`, `playerStore.test.ts`, and `frontend/src/layouts/PlayerBar.test.tsx` - negative, NaN, Infinity, beyond-duration, decoded-versus-API duration, owner cleanup, queue/shuffle/repeat, and rendering coverage.

### Documentation

- `docs/design/usecase/sound-track.md` - canonical pages, localized detail recovery, full playback, and progress bounds.
- `docs/design/usecase/sound-album.md` - page projection, latest-request ownership, one-based display, and context lifecycle.
- `docs/standards/frontend-standards.md`, `docs/ui/screen-flow.md`, and `docs/ui/atstudio-front-list.md` - current shared implementation and presentation contracts.

## Independent QA and Remediation

- Handoff: `deliverables/agent/WI-20260809-ATS-044-qa-fe-review-handoff.md`.
- Independent QA found no product P1/P2 in request ownership, context cleanup, progress bounds, or full-track playback.
- QA did find four stale assertions in `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx`: Album calls omitted AbortSignal, raw error text was still expected, and Track download failure expected obsolete page-state text instead of the owning toast.
- Remediation handoff: `deliverables/agent/WI-20260809-ATS-044-qa-remediation-handoff.md`.
- The four assertions were corrected without weakening parameters, raw-error exclusion, or toast ownership. A StrictMode Album detail test now proves first-request abort, stale completion rejection, latest context ownership, cleanup, and durable playback preservation.
- MA then found one adjacent progress issue: UI and seek used decoded runtime duration, but persistence reclamped against Track API duration. `persistState` now accepts runtime duration, and a `90`-second API / `120`-second decoded regression proves that position `110` remains valid and persisted.

## Focused and Adjacent Test Evidence

| Stage | Result |
| --- | --- |
| Initial focused implementation | PASS - `10` files, `136` tests |
| Initial adjacent implementation | PASS - `14` files, `187` tests |
| Independent QA full regression | FAIL - `84` files, `974` tests; four stale assertions in one coverage file, no reproduced product defect |
| QA formerly failing suite after remediation | PASS - `1` file, `28` tests |
| QA WI-044 focused group after StrictMode remediation | PASS - `9` files, `128` tests |
| QA full frontend rerun | PASS - `84` files, `975` tests |
| MA decoded-duration persistence focused rerun | PASS - `3` files, `80` tests |

## Final Verification Results

| Command | Result |
| --- | --- |
| `npm run test:coverage` | PASS - `84` files, `976` tests, failures `0`; statements 88.71% (`8109/9141`), branches 80.56% (`5136/6375`), functions 88.52% (`2006/2266`), lines 90.92% (`7456/8200`) |
| `npm run typecheck` | PASS - TypeScript no-emit check |
| `npm run lint` | PASS - full `frontend/src`, zero warnings |
| `npm run format` | PASS - all matched frontend files |
| `npm run build` | PASS - Vite 6.4.3 production build; `284` modules transformed |
| `.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification assemble --rerun-tasks --no-daemon --max-workers=1 --console=plain` | PASS - `1568` tests, failures/errors `0`, skipped `19`, `184` suites; instruction 86.957%, branch 72.251%, line 87.228%, method 84.730%, class 94.824%; assemble PASS |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS - Tier 0, internal links, `583` supported traceability IDs, and document index |
| `git diff --check` | PASS - final source, tests, current-state docs, and WI deliverables |

No authenticated mutation, download/export, provider/payment/refund, mail, database, schema, or ignored-secret action was executed. Protected output artifacts were not touched, inspected, staged, or deleted.

## Risks / Rollback

- Risk: actual browser media metadata behavior is represented by the test Audio double; live browser proof remains acceptance evidence, not a unit-test substitute.
- Risk: all current visible-list publishers must return the cleanup from `setTrackListContext`; future publishers must follow the same contract.
- Risk: the shared public page size is now an explicit frontend contract. A future product change must update Track and both Album views together.
- Rollback: revert catalog API signals, page/recovery components, page normalization utilities, context ownership, progress normalization, tests, current-state docs, and WI deliverables as one patch. No data rollback is required.

## Follow-ups

- `WI-20260809-ATS-059` owns catalog keyboard semantics, visible play control, broken-image fallback, and semantic headings after its remaining dependencies complete.
- `WI-20260809-ATS-073` owns Track-detail documentation and broader operator baseline alignment.
- `WI-20260809-ATS-078` owns authenticated entitlement, real media-error, Usage fixture, and large-pagination acceptance evidence.
