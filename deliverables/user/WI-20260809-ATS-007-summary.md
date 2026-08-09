# WI-20260809-ATS-007 Independent Frontend Review Summary

## TL;DR

The independent frontend review is complete with changes required. The review confirmed **0 BLOCKER, 3 MAJOR, and 0 MINOR** findings. `WI-20260808-ATS-029` remains **BLOCKED** until these findings and the backend MAJOR findings from WI-006 are repaired and covered by focused regression tests.

## Review Status

- **Status:** Complete - changes required
- **Reviewer role:** `cr`
- **Review date:** 2026-08-09
- **Scope:** Handoff-listed frontend media, tag, image, PlayableTrack, player, and catalog paths, plus exact backend DTO/controller signatures required for wire-contract comparison
- **Out of scope:** Backend internals, schema, data, external services, secrets, the intentional ZIP, commit, and push
- **Product changes:** None
- **Focused verification:** PASS, 4 test files / 49 tests / 0 failures

## Reviewed Components And Contracts

| Area | Reviewed frontend surface | Compared contract |
|---|---|---|
| Tag management and discovery | `tagName`, `TagManagePage`, tag/track API wrappers, `HomePage`, `TrackListPage`, `TagFilterModal`, `FilterChip` | Four repeated tag query families, `dataList/pageInfo`, raw Usage values with display-only `#` |
| Track image selection | `trackThumbnail`, `TrackThumbnailField`, `TrackUploadPage`, `TrackEditPage` and focused tests | JPEG/PNG, 10 MB, exact 1:1 selection, square center-cover preview, optional replacement |
| PlayableTrack and persistence | Shared types/mapper, track batch API, `playerStore`, `PlayerBar`, history and queue surfaces | Bounded public batch hydration, complete duration/waveform metadata, stale-result fencing |
| Catalog integration | Album, playlist, like, download-history, track-list, and track-detail playback entry points | Aggregate DTO nullable serialization and common PlayableTrack conversion |
| Policy boundary | Public stream URL and subscriber download calls | Full-track streaming remains public; download entitlement and limit controls remain separate |

## Decision

The reviewed implementation supports the intended four-tag query model, image-selection contract, delayed buffering feedback, public streaming boundary, and bounded batch hydration. Three frontend contract defects remain significant enough to block WI-029.

## Confirmed Findings

### MAJOR-001 - Nullable aggregate fields are omitted on the wire but the frontend mapper requires their keys

- **File:line:** `frontend/src/utils/playableTrack.ts:22-33`; `src/main/java/com/atstudio/atstudio/dto/album/AlbumTrackItemResponse.java:6-14`; `src/main/java/com/atstudio/atstudio/dto/playlist/PlaylistTrackItemResponse.java:6-16`; `src/main/java/com/atstudio/atstudio/dto/like/LikeResponse.java:8-18`; `frontend/src/pages/public/AlbumDetailPage.tsx:65-70`; `frontend/src/pages/subscriber/PlaylistDetailPage.tsx:74-88`; `frontend/src/pages/subscriber/LikeListPage.tsx:77-110`
- **Impact:** An album, playlist, or liked Track with no thumbnail can throw `TypeError` while publishing player context or when Play/Play All is selected. A null waveform can arrive as an absent property and become `undefined`, violating the declared PlayableTrack and persisted-state contract.
- **Reasoning:** The aggregate response records use `@JsonInclude(NON_NULL)`, so null `thumbnail`/`thumbnailUrl` and `waveformData` members are omitted from JSON. `toPlayableTrack` rejects a source unless a thumbnail key exists and copies `waveformData` without normalizing an omitted value. Current TypeScript interfaces and tests model explicit `null`, not the actual omitted wire shape.
- **Missing test:** API-shaped album, playlist, and like objects with omitted nullable keys, followed by context publication, individual play, Play All, queue persistence, and reload.
- **Recommended fix:** Model the actual wire fields as optional and normalize them at the API/mapper boundary to explicit `null`. Preserve positive-ID validation, but replace key-presence rejection with a typed source discriminator or `source.thumbnail ?? source.thumbnailUrl ?? null`; normalize `waveformData` with `?? null`. Add component regressions using omitted keys, not explicit nulls.

### MAJOR-002 - Track changes retain the previous Track duration until new media metadata arrives

- **File:line:** `frontend/src/store/playerStore.ts:519-538`; `frontend/src/layouts/PlayerBar.tsx:159-175`; `frontend/src/layouts/PlayerBar.tsx:595-600`
- **Impact:** After Track A has loaded metadata, selecting Track B can temporarily render Track A's duration and progress scale for Track B. During a slow metadata load, waveform clicks, keyboard seeking, timestamps, and accessibility range values target the wrong duration.
- **Reasoning:** `play()` resets `currentTime` but does not update or clear the store's `duration`. `PlayerBar` gives the nonzero store duration precedence over `currentTrack.duration`, so the prior value remains authoritative until `loadedmetadata` or `timeupdate` fires for the new source.
- **Missing test:** Load metadata for a first Track, switch to a second Track with a different duration, and assert the immediate displayed duration, seek target, progress ratio, and ARIA maximum before the second `loadedmetadata` event.
- **Recommended fix:** Set `duration` from the selected PlayableTrack in the same state transition as `currentTrack` and `currentTime`. Continue replacing it with the browser's actual duration when current-source metadata arrives, with source/Track identity protection.

### MAJOR-003 - Active URL/API tag filters disappear from the selected-chip UI when taxonomy loading fails or a tag is absent

- **File:line:** `frontend/src/pages/public/TrackListPage.tsx:69-80`; `frontend/src/pages/public/TrackListPage.tsx:142-162`; `frontend/src/pages/public/TrackListPage.tsx:186-198`; `frontend/src/pages/public/TrackListPage.tsx:488-614`
- **Impact:** A bookmarked/deep-linked filter, a recently deleted tag, or one failed taxonomy request can leave genre, mood, instrument, or Usage values active in the URL and track API while no corresponding selected chip is visible. Usage can disappear as an entire row. The result list can therefore be filtered or empty without accurately exposing the active criteria.
- **Reasoning:** URL values are sent directly to `fetchTracks`, but chip rendering is derived only from the four fetched taxonomy arrays. The taxonomy calls share one `Promise.all`; one rejection is silently ignored and leaves every array empty. There is no fallback chip model for active URL values absent from the taxonomy response.
- **Missing test:** Start with active repeated filters for each of the four types, then reject one taxonomy call or omit an active tag from the response; assert that URL, API arrays, visible selected chips, raw Usage values, reset, and result-state messaging remain consistent.
- **Recommended fix:** Track taxonomy loading/error state and merge active URL values into the visible chip model with stable synthetic keys until taxonomy succeeds. Keep raw values in URL/API state, apply `#` only when rendering Usage, and provide a scoped retry/error state instead of silently hiding active filters.

## No Additional Confirmed Findings

- Usage values remain raw in create/update/search requests; `#` is added only in reviewed display paths.
- The selected thumbnail preview is a stable 1:1 center-cover surface, invalid or pending selections block submission, and unchanged legacy thumbnails are not resent.
- Buffering remains hidden before 2,000 ms, recovery cancels pending state, and real media/play errors remain separate and higher priority.
- Persisted player/history hydration uses one bounded batch call rather than one detail request per Track; reviewed aggregate entry points use embedded PlayableTrack metadata.
- Playback uses the public `/api/tracks/{id}/stream` route. Download actions continue to use the entitlement-controlled download API and were not treated as equivalent policy.

## Residual Risks

- Real-browser network throttling, native media event ordering, timer throttling, and canvas pixels were not exercised in this read-only review.
- `playerStore.ts:130-141` installs an un-tokened one-shot `loadedmetadata` fallback if assigning persisted `currentTime` throws before metadata. Whether current supported browsers enter that branch is unverified; a browser/focused test should prove that an old listener cannot seek a newer Track.
- The selected image preview and stored canonical JPEG preserve the reviewed square/center-cover shape contract, but browser decoding versus backend JPEG re-encoding was not visually compared for color, orientation, or compression differences.
- Production-sized waveform payload, render cost, maximum batch memory, and MySQL execution characteristics were not measured.
- Cross-tab writes to player/history localStorage are outside the process-local generation fences.

## Verification

The following focused command was run from `frontend`:

```text
npm test -- src/utils/playableTrack.test.ts src/store/playerStore.test.ts src/pages/public/TrackListPage.test.tsx src/pages/creator/TrackThumbnailField.test.tsx
```

Result: PASS, Vitest 4.1.4, 4 files and 49 tests passed, 0 failed, 7.48 seconds.

Targeted `rg`, `Get-Content`, `git status`, and scoped `git diff` inspection was used. No full suite, browser session, external call, backend-internal review, or write outside these two WI deliverables was performed. The passing focused tests do not cover the three missing cases described above.

## Rollback

Rollback is documentation-only: remove this summary and the corresponding WI-007 evidence pack. No product code, tests, schema, data, configuration, dependency, generated ZIP, or Git history was changed.

## WI-029 Block Status

`WI-20260808-ATS-029` is **BLOCKED**. Clear the block only after the three frontend MAJOR findings above and the two backend MAJOR findings recorded by WI-006 are repaired, covered by focused regressions, and independently re-reviewed.
