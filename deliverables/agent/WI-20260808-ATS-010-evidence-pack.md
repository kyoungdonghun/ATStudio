---
version: 1.0
last_updated: 2026-08-08
project: ATS
owner: qa-fe
category: evidence-pack
status: confirmed
related_wi: WI-20260808-ATS-010
dependencies:
  - path: WI-20260808-ATS-010-handoff.md
    reason: Approved Work Item scope and output contract
  - path: ../user/REQ-20260808-ATS-003.md
    reason: Approved request and acceptance criteria
  - path: ../user/WI-20260808-ATS-010-summary.md
    reason: User-facing findings and recommendations
---

# Evidence Pack: WI-20260808-ATS-010

## Summary (one-liner)

- Confirmed that the transient delay message is an immediate, non-fatal native media-event reaction, while missing album waveform rendering is caused by abbreviated playback DTOs and handcrafted null Track objects across multiple entry points.

## Scope / DoD Check

- [x] Confirmed immediate `isStalled` activation from `waiting` and `stalled`.
- [x] Confirmed recovery from `timeupdate`, `canplay`, `playing`, successful play, pause, retry initialization, and terminal media error.
- [x] Recorded the injected `/albums/2` runtime observation: immediate delay status followed by successful playback within approximately 1.8 seconds.
- [x] Confirmed that the album API omits duration/waveform while the same Track detail currently contains real waveform peaks.
- [x] Confirmed AlbumDetailPage explicitly constructs `duration: 0`, `waveformData: null` Tracks for context, play-all, and row playback.
- [x] Cross-checked playlist, like, download-history, local-history, and player-drawer paths.
- [x] Distinguished missing-peak flat-line fallback from an actual low-amplitude waveform.
- [x] Compared common DTO, batch hydration, on-demand hydration, and rejected per-row N+1 detail-fetch alternatives.
- [x] Ran the two existing focused player suites successfully.
- [x] Changed only this WI's user summary and Evidence Pack.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Constitution and transparent evidence requirements |
| 0 | `docs/standards/development-standards.md` | Cross-layer implementation and testing standards |
| 1 | `docs/policies/quality-gates.md` | Regression, performance, and traceability checks |
| 1 | `docs/standards/frontend-standards.md` | Active React, Zustand player, API, and test conventions |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React performance and data-fetching context supplied by the handoff |
| 2 | `docs/SR/SR-90.md` | Requirement that waveform use real analyzed peak data, not decoration |
| 2 | `docs/design/usecase/sound-track.md` | Public playback, Track list/detail, and waveform contract |
| Context | `deliverables/user/REQ-20260808-ATS-003.md` | Approved three-SR scope and quality gates |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `qa-fe`
- Task type: frontend integration, playback reliability, and read-only investigation
- Injected tiers: Tier 0, relevant Tier 1/2, approved REQ, runtime snapshot, and named code pointers

## Findings

### 1. Delay Status Is Immediate Buffering Feedback, Not Playback Failure

- `frontend/src/store/playerStore.ts:299-314` maps both native `stalled` and `waiting` directly to `isStalled=true`. It performs no elapsed-time check and only requires a current Track.
- `frontend/src/store/playerStore.ts:280-281,305-314` clears the state on `timeupdate`, `canplay`, and `playing`.
- `frontend/src/store/playerStore.ts:252-275` clears stalled/error state when a playback attempt begins, marks playing only after `audio.play()` resolves, and reports a distinct start failure if it rejects.
- `frontend/src/store/playerStore.ts:245-249,316-318` treats the native media `error` event as terminal: pause, `isPlaying=false`, `isStalled=false`, and a media-error message.
- `frontend/src/layouts/PlayerBar.tsx:16,595-613` renders stalled state as a polite retryable `status`; a playback error takes precedence and is rendered as an assertive `alert`.
- `frontend/src/store/playerStore.test.ts:245-274` locks in immediate `stalled`/`waiting` activation and recovery, but has no debounce/threshold test.
- `frontend/src/layouts/PlayerBar.test.tsx:168-191` verifies error and stalled messages separately, but likewise contains no duration condition.

The handoff's browser snapshot for `/albums/2` observed the warning immediately after `전체 재생`, then successful playback at approximately 0:01 within about 1.8 seconds. This is consistent with a short initial `waiting` event followed by one of the existing recovery events. It is evidence of a false-alarm UX, not evidence that playback failed.

### 2. Album Waveform Is Lost at the DTO/Mapping Boundary

- `frontend/src/api/albums.ts:5-12` defines Album Track items with only `trackId`, title, artist, thumbnail, and order.
- `src/main/java/com/atstudio/atstudio/dto/album/AlbumTrackItemResponse.java:7-24` exposes exactly those fields; duration and waveform are absent.
- `frontend/src/pages/public/AlbumDetailPage.tsx:66-83` publishes album rows to `trackListContext` with `duration: 0` and `waveformData: null`.
- `frontend/src/pages/public/AlbumDetailPage.tsx:153-173` constructs the same incomplete objects for `playAll`.
- `frontend/src/pages/public/AlbumDetailPage.tsx:227-247` repeats the same mapping for individual-row play.
- `frontend/src/layouts/PlayerBar.tsx:154-164` parses only `currentTrack.waveformData`; missing/null/invalid data becomes an empty peaks array.
- `frontend/src/components/player/WaveformCanvas.tsx:34-42` explicitly draws a single flat line when peaks are empty. It does not claim that this line is an analyzed waveform.

Read-only public API verification on 2026-08-08:

| Request | Result |
| --- | --- |
| `GET /api/albums/2` | Track item keys: `trackId,title,artistName,thumbnailUrl,order`; no duration and no waveformData |
| `GET /api/tracks/2` | `duration=229`; `waveformData` present, 1,201 characters, prefix `[0.003,0.139,0.222,0.318,...]` |

Therefore Track 2 has real stored peak data; the album path discards it. This is not an upload-time extraction failure and must not be hidden by generating decorative peaks.

### 3. Cross-Screen Impact

| Path | Evidence | Consequence |
| --- | --- | --- |
| Playlist detail context/click/queue | `frontend/src/pages/subscriber/PlaylistDetailPage.tsx:74-96,105-123,154-176` | duration is 0 and waveform is null for all three paths |
| Like list context/click | `frontend/src/pages/subscriber/LikeListPage.tsx:76-99,127-145` | duration is 0 and waveform is null |
| Download history context/click | `frontend/src/pages/subscriber/DownloadHistoryPage.tsx:128-152,279-297` | API duration is kept, waveform is forced null |
| PlaylistDrawer playlist/like replay | `frontend/src/components/player/PlaylistDrawer.tsx:161-180,263-282` | duration is 0; optional waveform field is omitted and parses as empty |
| Local HistoryModal replay | `frontend/src/store/playerStore.ts:133-138,162-174`; `frontend/src/components/player/HistoryModal.tsx:28-47` | local history stores only ID/title/thumbnail; replay lacks duration/waveform |

- `frontend/src/store/playerStore.ts:371-459` selects the next/previous Track directly from `trackListContext` or queue and calls `play` without hydration.
- `frontend/src/store/playerStore.ts:479-485` stores all play-all Track objects as the queue and starts the first unchanged.
- `frontend/src/store/playerStore.ts:18-109,337-357` accepts null/undefined waveform in persisted Track objects and saves the same abbreviated queue/currentTrack. The symptom can therefore survive navigation and reload.
- Healthy comparison: `src/main/java/com/atstudio/atstudio/dto/track/TrackListItemResponse.java:10-23` already includes duration and waveform; `frontend/src/pages/public/TrackListPage.tsx:34-54` forwards both to the player. `TrackResponse.java:10-27` and `TrackDetailPage.tsx:130-180` do the same for detail playback.

### 4. Data-Contract and Performance Alternatives

| Alternative | Benefit | Cost / risk | Assessment |
| --- | --- | --- | --- |
| Extend collection items with a shared `PlayableTrack` projection | One response, deterministic queue, no play-time waterfall; matches existing Track list contract | waveform payload grows linearly with list size | Preferred for bounded album/playlist and current acceptance scope |
| Batch hydration by `trackIds` | Keeps base DTOs small; one query/request for a queue; reusable cache | Adds endpoint and client hydration state | Preferred fallback for large collections |
| On-demand detail hydration on each play, cached by ID | Small initial payload | extra first-play/next latency; prefetch and race handling required | Acceptable only as a deliberate hybrid |
| Loop over `GET /tracks/{id}` for every row | Simple locally | N HTTP calls, N DB/API paths, waterfalls, race complexity | Reject |

Repository evidence supports a bounded-query implementation:

- `AlbumTrackRepository.java:18-20` already loads `track` and `track.user` via `EntityGraph`.
- `PlaylistTrackRepository.java:12-14` already loads `track` via `EntityGraph`.
- `LikeRepository.java:13-15` already loads `track` via `EntityGraph`.
- For those three paths, duration and waveform are scalar Track fields, so extending the mapped DTO does not require a per-item detail query.
- `TrackDownloadRepository.java:27-43` uses a non-fetch join while `DownloadHistoryItemResponse.java:28-47` traverses Track, user, and tags. This path should use a safe Track fetch/projection and batched tag loading rather than adding more lazy per-row access. A paged collection fetch-join must also avoid in-memory pagination.

Recommended contract:

```text
PlayableTrack = id + title + artistName + duration + thumbnail + waveformData
```

Add tags only when the consuming player surface needs them. Keep administrative counts and description outside the minimum playback contract. Centralize one frontend mapper so pages cannot silently invent `0`/`null` values.

### 5. Recommended Buffering State Model

```text
idle/loading -> playing -> buffering-pending -> buffering-visible -> recovered
                              |                    |
                              +------ error <-----+
```

- On `waiting`/`stalled`, capture event kind, track ID, playback generation, and start time.
- Delay visible feedback for approximately 2 seconds. The 1.8-second acceptance recovery would then remain a normal start rather than a false warning.
- Cancel the timer on `timeupdate`, `canplay`, `playing`, pause, track change, retry, or error.
- If the threshold passes, show a non-fatal buffering status. If a longer product-defined timeout passes, emphasize retry/network guidance without marking the media as failed.
- Keep `audio.error` and rejected `audio.play()` as separate terminal/recoverable error states.
- A generation/token check is required so an old Track's timer cannot surface after fast next/previous navigation.

## SR-101 Requirements for DocOps

1. State that the delay text is currently triggered immediately by native `waiting`/`stalled`, not by an elapsed timeout or confirmed failure.
2. Record the `/albums/2` observation as transient: immediate warning, then successful playback within about 1.8 seconds.
3. Require duration-based buffering feedback, timer cancellation, stale-attempt invalidation, and separate terminal error handling.
4. State that Album Track DTO omits duration/waveform and AlbumDetailPage explicitly creates null waveform Tracks in all three playback paths.
5. Record that current Track detail for ID 2 contains actual peak data, so the flat line represents missing transport, not a flat source waveform.
6. Include playlist detail, LikeList, DownloadHistory, HistoryModal, PlaylistDrawer, queue, next/previous, and persisted-player impacts.
7. Require one canonical playback DTO/mapper or one batch hydration contract across every entry point; reject per-row detail N+1.
8. Preserve the SR-90 rule that waveform must use real analyzed peak data, with flat fallback only when peak data truly does not exist.
9. Require fake-timer buffering tests, hydration tests for all listed surfaces, next/previous/play-all tests, persisted-state migration tests, and bounded-query/request-count tests.

## Evidence Pointers

### Files Changed

- `deliverables/user/WI-20260808-ATS-010-summary.md`
  - User-facing condition explanation, affected surfaces, and recommendations.
- `deliverables/agent/WI-20260808-ATS-010-evidence-pack.md`
  - This runtime/code evidence, alternatives, test requirements, and SR-101 input.

No product code, SR, index, database, runtime data, or unrelated user file was modified.

### Key Locations

- `frontend/src/store/playerStore.ts:245-318` — playback attempt, waiting/stalled, recovery, and error state transitions
- `frontend/src/layouts/PlayerBar.tsx:154-164,595-613,704-725` — peak parsing, feedback priority, and waveform rendering input
- `frontend/src/components/player/WaveformCanvas.tsx:34-75` — empty peak flat-line fallback versus real peak bars
- `frontend/src/pages/public/AlbumDetailPage.tsx:66-83,153-173,227-247` — three abbreviated Album Track mappings
- `frontend/src/api/albums.ts:5-12` — abbreviated frontend Album Track contract
- `src/main/java/com/atstudio/atstudio/dto/album/AlbumTrackItemResponse.java:7-24` — abbreviated backend Album Track contract
- `frontend/src/pages/subscriber/PlaylistDetailPage.tsx:74-176` — playlist context, direct play, and queue mappings
- `frontend/src/pages/subscriber/LikeListPage.tsx:76-145` — liked-track context and direct play mappings
- `frontend/src/pages/subscriber/DownloadHistoryPage.tsx:128-152,279-297` — retained duration but discarded waveform
- `frontend/src/components/player/HistoryModal.tsx:28-47` — local-history replay mapping
- `frontend/src/components/player/PlaylistDrawer.tsx:161-180,263-282` — playlist and liked Track replay mappings

## Commands & Outputs

| Command / check | Result |
| --- | --- |
| `rg -n -C 5 "isStalled\|waiting\|stalled\|canplay\|playing\|timeupdate" ...` | Confirmed immediate and recovery event wiring |
| `rg -n "waveformData:\\s*(null\|undefined)\|duration:\\s*0" frontend/src` | Located every explicit abbreviated Track mapping |
| Focused reads of Album/Playlist/Like/Download DTOs and repositories | Confirmed API omissions and existing EntityGraph/query behavior |
| Read-only `GET https://comparable-indicate-black-guidelines.trycloudflare.com/api/albums/2` | Album Track has five abbreviated fields; duration/waveform absent |
| Read-only `GET https://comparable-indicate-black-guidelines.trycloudflare.com/api/tracks/2` | duration 229; waveform present with 1,201-character real peak array |

## Tests

### Existing Focused Test Execution

- Command: `npm test -- src/store/playerStore.test.ts src/layouts/PlayerBar.test.tsx`
- Working directory: `frontend/`
- Framework: Vitest 4.1.4
- Result: PASS — 2 test files, 27 tests passed, 0 failed, duration 3.79s.

### Coverage Gap / Required Follow-up Tests

- Fake timers: recovery before threshold must never render the status; threshold crossing must render it; long buffering must retain a non-terminal state.
- Recovery matrix: `timeupdate`, `canplay`, `playing`, pause, retry, error, and track change each cancel pending feedback.
- Race: a delayed callback from Track A must not mark Track B stalled after rapid next/previous.
- Hydration: album, playlist, like, download, local history, and both Drawer paths deliver the stored waveform and authoritative duration.
- Queue: play-all and next/previous retain each Track's own waveform; cache/batch behavior avoids redundant requests.
- Rendering: real non-empty peaks render bars; only absent peaks render the explicit flat fallback.
- Persistence: old version-1 null-waveform player state is migrated or hydrated before playback.
- Backend performance: assert bounded query counts for album/playlist/like/download collections and no per-item detail lookup.

## Risks / Rollback

### Risks

- The approximately 1.8-second UI recovery is an injected browser snapshot from the current acceptance run; network timing can vary. The code-level absence of a threshold is deterministic.
- Public API evidence is a 2026-08-08 snapshot. Track 2 data may change, but the DTO omission and frontend mapping conclusions are code-backed.
- A fixed two-second threshold should be validated on slow mobile/network profiles; it is an initial recommendation, not an immutable domain constant.
- Extending every list DTO with waveform increases payload size. Large queues need response-size measurement and may favor batch/on-demand hydration.
- Existing local player/history entries can remain abbreviated after a contract fix unless versioning or rehydration is included.

### Rollback

- Remove only `deliverables/user/WI-20260808-ATS-010-summary.md` and `deliverables/agent/WI-20260808-ATS-010-evidence-pack.md` to roll back this read-only investigation output.
- Do not alter the approved REQ, handoff, product code, SR files, indexes, DB, public tunnel, or unrelated user artifacts.

## Follow-ups

- `WI-20260808-ATS-011` should consume the SR-101 requirements above together with WI-008 and WI-009.
- The eventual implementation should use separate frontend/backend WIs with independent query-count and browser-network verification.
