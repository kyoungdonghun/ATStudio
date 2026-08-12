# Evidence Pack: WI-20260809-ATS-023

## Summary (one-liner)

- Completed the frozen public catalog and shared playback audit, recording ten confirmed defects, one documentation drift, two fixture/data gaps, one static risk, one automation blocker, and one review item with no product or fixture change.

## Scope / DoD Check

- DoD items:
  - [x] Classified every owned row (`PUB-01` through `PUB-06`, `SH-02`, `SH-06`) as `FAIL`, with no unclassified row.
  - [x] Separated UI observation, request/response evidence, canonical API state, browser-local player state, and unavailable fixture boundaries.
  - [x] Verified core Home/Track/Album rendering, valid AND filter URLs, modal apply/Escape, reload, back, and forward behavior.
  - [x] Recorded full-media timing, Track identity, Album ordering, responsive captures, accessibility findings, and source pointers.
  - [x] Preserved the intentional demo ZIP and performed no product/test/config/runtime/data/Git edit.

### Owned Row Classification and Screenshot Totals

| Row                       | Result | Valid screenshot evidence | Notes                                                                                        |
| ------------------------- | ------ | ------------------------: | -------------------------------------------------------------------------------------------- |
| `PUB-01` Home             | `FAIL` |                         1 | Core render passed; direct playback remains a review item.                                   |
| `PUB-02` Track list       | `FAIL` |                         2 | AND URL behavior passed; invalid pagination, play visibility, and Usage fixture gaps remain. |
| `PUB-03` Track detail     | `FAIL` |                         2 | Full-media timing passed; missing-ID recovery and metadata presentation failed.              |
| `PUB-04` Album image list | `FAIL` |                         2 | View and image evidence captured; async, pagination, and entry semantics failed.             |
| `PUB-05` Album list       | `FAIL` |                         0 | Raw 400 and mouse-only row behavior observed without a dedicated capture.                    |
| `PUB-06` Album detail     | `FAIL` |                         1 | Ordered tracks rendered; raw order and stale context findings recorded.                      |
| `SH-02` PlayerBar         | `FAIL` |                         2 | Full Track 3 playback, waveform/time, and seek passed; mobile/context risks remain.          |
| `SH-06` TagFilterModal    | `FAIL` |                         0 | Apply/Escape passed; label, clear, and failed-availability announcement gaps remain.         |

Totals: `PASS 0`, `FAIL 8`, `BLOCKED 0`, `N/A 0`. The directory contains `12` PNGs: `10` valid owned-row captures, `1` valid cross-shell Header capture (`SH-01_VM360_menu-open.png`), and `1` known full-page capture artifact (`PUB-03_VM_track-detail.png`). This leaves `11` valid captures overall. The valid Track-detail viewport capture is `PUB-03_VM_track-detail-viewport.png`; both Track-detail files are intentionally preserved.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier       | Document                                                      | Reason                                                            |
| ---------- | ------------------------------------------------------------- | ----------------------------------------------------------------- |
| Repository | `AGENTS.md`                                                   | Repository language, scope, and no-side-effect rules              |
| Handoff    | `deliverables/agent/WI-20260809-ATS-023-handoff.md`           | Approved WI scope, constraints, and output contract               |
| Matrix     | `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md` | Owned rows, evidence levels, fixtures, and viewport contract      |
| Prior WI   | `deliverables/agent/WI-20260809-ATS-022-evidence-pack.md`     | Established evidence-pack format and frozen-audit reporting style |
| Skill      | `.agents/skills/create-wi-evidence-pack/SKILL.md`             | Required Evidence Pack headings and pointer conventions           |

The handoff-declared Tier 0-2 documents remain the governing context. No broad documentation rescan or test rerun was performed during closeout.

## Evidence Pointers (required)

### Runtime and Fixture Preflight

- Runtime boundary: frozen public catalog; browser restored to Home with no current Track.
- Fixture alias: anonymous seeded public catalog; no credentials or secrets recorded.
- Data boundary: read-only requests and playback only. Authenticated mutation/download, inactive/null media, linked Usage, large pagination, and buffering/error injection remained unavailable.
- Screenshot directory: `output/ui-ux-audit/20260809/WI-023/`.

### Scenario Matrix

| Scenario | Result | Evidence pointer                                                                                                                                                                                                                            |
| -------- | ------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `PUB-01` | `FAIL` | Home render and tag discovery in `PUB-01_VD_home.png`; valid filter/reload/history behavior recorded in the browser session.                                                                                                                |
| `PUB-02` | `FAIL` | Filtered Track list `PUB-02_VD_tracks-filtered.png`; smallest viewport `PUB-02_VM360_tracks.png`; Track API shape in `frontend/src/api/tracks.ts:48-71`.                                                                                    |
| `PUB-03` | `FAIL` | Playing Track `PUB-03_VD_track-playing.png`; valid mobile viewport `PUB-03_VM_track-detail-viewport.png`; load/error source `frontend/src/pages/public/TrackDetailPage.tsx:39-67,105-118`.                                                  |
| `PUB-04` | `FAIL` | Album grid `PUB-04_VD_albums-grid.png`; 1024 viewport `PUB-04_VT1024_albums.png`; route switch works but discards sort/page and changes the page-size projection; source `frontend/src/pages/public/AlbumListImagePage.tsx:11,33-50,95-99`. |
| `PUB-05` | `FAIL` | Browser route `/albums/list`; page-size/fetch and row navigation source `frontend/src/pages/public/AlbumListPage.tsx:12,33-51,120-165`.                                                                                                     |
| `PUB-06` | `FAIL` | Album detail `PUB-06_VD_album-detail.png`; ordered projection and context source `frontend/src/pages/public/AlbumDetailPage.tsx:25-34,65-70,169-215`.                                                                                       |
| `SH-02`  | `FAIL` | Collapsed `SH-02_VM_player-collapsed.png`; expanded `SH-02_VM_player-expanded.png`; waveform/time source `frontend/src/layouts/PlayerBar.tsx:159-175,685-730`.                                                                              |
| `SH-06`  | `FAIL` | Browser modal interaction; input/clear/apply source `frontend/src/components/filter/TagFilterModal.tsx:132-149,245-258`; availability failure source `frontend/src/pages/public/TrackListPage.tsx:400-455`.                                 |

### Browser, API, and Canonical-State Separation

- UI: core Home/Track/Album rendering, valid AND filter URLs, modal apply/Escape, reload, back, and forward passed. Missing-ID errors, invalid pagination recovery, raw Album 400, mouse-only Album entries, raw order `0`, hidden Track play buttons, hidden mobile controls, and TagFilter accessibility gaps failed.
- Request/API: Track list/detail clients use `dataList`/paged responses and expose `duration`/`waveformData` (`frontend/src/api/tracks.ts:6-23,48-80`). Album list uses `/albums` with page sizes `24` and `20` (`frontend/src/api/albums.ts:28-53`).
- Canonical state: Track 3 stored duration `1090` and Track 2 `229` are known pre-SR-99 drift; current analyzer and dry-run are read-only. Seeded Usage is prefixed and has no active Track link. No canonical state was changed.
- Browser-local state: Track 3 waveform/time advanced and seek worked; full media showed `7:26`. Track 2 actual media showed `1:33`. Queue/current Track state was restored to Home/no Track. Storage inspection was prohibited.

### Waveform and Full-Duration Timing

- Track 3: waveform/time advanced, seek worked, and full media displayed `7:26`.
- Track 2: actual media displayed `1:33`.
- Stored API durations: Track 3 `1090` seconds (`18:10`), Track 2 `229` seconds (`3:49`). This is existing SR-99 data drift, not evidence that the current analyzer is incorrect.
- Source corroboration: `frontend/src/layouts/PlayerBar.tsx:159-175,713-730`; `frontend/src/store/playerStore.ts:519-540,636-641`.

### Track Identity and Album Order Comparisons

- Same Track identity remained addressable through public Track and PlayerBar routes; PlayerBar links use the current Track ID (`frontend/src/layouts/PlayerBar.tsx:627-650`).
- Album detail maps each `trackId` through `toPlayableTrack` and publishes the ordered list as player context (`frontend/src/pages/public/AlbumDetailPage.tsx:65-70,176-215`).
- First Album detail row visibly showed raw order `0`; this is recorded as `F-UI-023-005`.
- Home direct playback and Album-detail per-Track download are expectations in the approved matrix/handoff, but are not clearly grounded in a canonical product document; they therefore remain `REVIEW`, not confirmed product defects.

### Viewport Evidence

- `VD`: Home, Track list, Track playing, Album grid, Album detail captures present.
- `VN`: Album grid capture present at 1024 width.
- `VM`: Track detail valid viewport and PlayerBar collapsed/expanded captures present.
- `VS`: Track list capture present at 360 width; mobile Header capture is `SH-01_VM360_menu-open.png`.
- Screenshot accounting: `12` PNGs total, comprising `10` valid owned-row captures, `1` valid cross-shell Header capture, and `1` known artifact; `11` captures are valid overall.
- Artifact note: retain both `PUB-03_VM_track-detail.png` and `PUB-03_VM_track-detail-viewport.png`; only the latter is the valid Track-detail viewport image.

## Commands & Outputs

- Source checks were limited to the handoff-listed primary code files and the named WI/matrix files.
- Screenshot inventory: `Get-ChildItem -File output/ui-ux-audit/20260809/WI-023` -> expected named captures present; no deletion performed.
- Git status preflight showed existing untracked deliverables/output, including the intentional demo ZIP; no Git operation was performed.
- No broad docs scan, build, test, runtime, provider, storage, DB, or data command was run for closeout.

## Tests

- Frontend focused result recorded from the audit: 7 files / 90 tests -> `PASS`.
- Backend focused result recorded from the audit: `TrackControllerTest`, `PlayableTrackQueryCountTest`, `TrackServiceAudioProcessingTest`, `TrackServiceTest`, and `AlbumServiceTest` -> `BUILD SUCCESSFUL`.
- These results were not rerun during documentation closeout.

## Risks / Rollback

- Risks: known duration data drift, Usage fixture mismatch, stale Album async loads, stale player context, hidden mobile controls, and unclamped persisted progress remain open.
- Blocked boundaries: authenticated like/add/download, inactive/null media, linked Usage, large pagination, buffering/error injection, and other mutation/provider paths.
- Effects and cleanup: no application mutation, download, DB/schema/data/file/provider/mail/payment/account/secret/storage operation occurred. Browser-local player/URL state was restored to Home/no Track.
- Rollback: no product rollback is required. Documentation rollback is limited to removing these three WI-023 reports; do not remove the demo ZIP or either Track mobile capture.

## Follow-ups

- WI-023 is complete as a documentation-only audit.
- Trigger WI-024 for the approved follow-up path, carrying `F-UI-023-001` through `F-UI-023-010`, `D-UI-023-001`, `G-UI-023-001` through `G-UI-023-002`, `R-UI-023-001`, `B-UI-023-001`, and `R-UI-023-002`.
- WI-024 must obtain approved fixtures before converting blocked boundaries into product conclusions.
