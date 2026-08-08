---
version: 1.0
last_updated: 2026-08-08
project: ATS
owner: qa-integ
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260808-ATS-008-handoff.md
    reason: Approved Work Item scope, runtime evidence, and output contract
  - path: ../user/REQ-20260808-ATS-003.md
    reason: Approved request and acceptance criteria
  - path: ../user/WI-20260808-ATS-008-summary.md
    reason: User-facing duration investigation summary
---
# Evidence Pack: WI-20260808-ATS-008

## Summary (one-liner)

- Confirmed across all three public tracks that persisted duration is the exact result of a 128-Kib/s file-size estimate rather than media metadata, mapped the propagation and browser correction paths, identified missing duration recomputation on audio replacement, and defined exact extraction, backfill, and regression requirements for SR-99.

## Scope / DoD Check

- [x] Compared public detail/list API duration with browser media duration for Tracks 1-3.
- [x] Verified stream object sizes without downloading the full files by using one-byte Range requests.
- [x] Reproduced every persisted value with the current 128-Kib/s formula and calculated the approximately 320-kbps average bitrate from actual playback length.
- [x] Distinguished facts, runtime observations, calculations, and proposed controls.
- [x] Traced create, audio-replacement, entity, DTO, API, home, track-list, player, and download-history paths.
- [x] Identified current tests that codify the faulty estimate and the absence of audio-replacement duration coverage.
- [x] Defined exact extraction, failure, audit/backfill, rollback, and cross-layer test requirements.
- [x] Changed only this WI's user summary and Evidence Pack; product code, SR files, indexes, DB, and public data were not changed.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Constitution, platform integrity, transparency, and approved execution boundary |
| 0 | `docs/standards/development-standards.md` | Spring service, transaction, testing, and failure-handling standards |
| 1 | `docs/policies/quality-gates.md` | Cross-layer verification and evidence requirements |
| 2 | `docs/design/api-spec.md` | Current REST contract context |
| 2 | `docs/design/usecase/sound-track.md` | SOUND track duration and create/update behavior contract |
| Context | `deliverables/user/REQ-20260808-ATS-003.md` | Approved three-SR scope and quality gates |
| Context | `deliverables/agent/WI-20260808-ATS-008-handoff.md` | WI-specific scope, constraints, runtime observations, and output contract |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `qa-integ`
- Task type: `testing`, `integration`, `review`
- `agent_required_tiers`: `[0, 1]`
- Workspace tag source: `.claude/config/workspace.json` -> `ATS`

## Findings: Facts, Observations, Inferences, and Proposals

### Confirmed Runtime Facts

| Track | API title | API duration | API display | Range total bytes | Current formula result |
| --- | --- | ---: | ---: | ---: | ---: |
| 1 | 시골마을의 전학생 | 229s | 3:49 | 3,756,312 | `floor(3,756,312 / 16,384) = 229` |
| 2 | 시골마을의 전학생 | 229s | 3:49 | 3,756,312 | `floor(3,756,312 / 16,384) = 229` |
| 3 | Space Oddity - T0Ro, MELE - 경동훈 (2) | 1,090s | 18:10 | 17,863,782 | `floor(17,863,782 / 16,384) = 1,090` |

- `GET /api/tracks/{1|2|3}` returned the duration values above on 2026-08-08.
- `GET /api/tracks/{id}/stream` with `Range: bytes=0-0` returned `206 Partial Content` and `Content-Range` totals of 3,756,312, 3,756,312, and 17,863,782 bytes.
- `GET /api/tracks?page=1&size=3&sort=latest` returned Track 3, Track 2, Track 1 with the same 1,090, 229, 229 duration values used by Home.
- The stream endpoint exposes the stored resource byte length and does not derive or modify duration: `src/main/java/com/atstudio/atstudio/service/TrackService.java:149-159`, `src/main/java/com/atstudio/atstudio/controller/TrackController.java:94-139`.

### Recorded Browser Media Observations

The approved WI handoff records the following `HTMLAudioElement`/player total-time observations:

| Track | Browser media duration | Display |
| --- | ---: | ---: |
| 1 | 93s | 1:33 |
| 2 | 93s | 1:33 |
| 3 | 446s | 7:26 |

- These are runtime observations supplied through `deliverables/agent/WI-20260808-ATS-008-handoff.md`, not values inferred from the persisted duration.
- The browser observation and Range total are independent inputs to the bitrate calculation below.

### Reproducible Calculations

The production formula is `file.getSize() / (128 * 1024 / 8)`, so its divisor is 16,384 bytes per second.

| Track | Formula raw seconds | Stored integer seconds | Actual average bitrate (`bytes*8/seconds/1000`) | Stored/actual ratio | Error |
| --- | ---: | ---: | ---: | ---: | ---: |
| 1 | 229.267 | 229 | 323.12kbps | 2.462x | +136s |
| 2 | 229.267 | 229 | 323.12kbps | 2.462x | +136s |
| 3 | 1,090.319 | 1,090 | 320.43kbps | 2.444x | +644s |

- The exact equality between all three formula results and API values establishes that the fixed-rate code path produced the persisted values.
- The actual average bitrate is approximately 320kbps for all three files. File size plus duration does not establish whether a file is CBR or VBR, so no CBR/VBR claim is made for these specific objects.
- The cross-check disproves the hypothesis that only Track 3 is affected: Tracks 1 and 2 have the same defect, with 3:49 stored versus 1:33 played.

### Code Facts: Creation and Extraction

1. `src/main/java/com/atstudio/atstudio/service/TrackService.java:63-69` computes duration and waveform before storage writes during create.
2. `src/main/java/com/atstudio/atstudio/service/TrackService.java:83-93` writes the computed duration and waveform into the new Track entity.
3. `src/main/java/com/atstudio/atstudio/service/TrackService.java:291-305` routes WAV to a RIFF parser, but routes MP3 to a fixed 128-Kib/s file-size estimate and returns zero for other formats.
4. `src/main/java/com/atstudio/atstudio/entity/Track.java:40-47` persists non-null integer `duration` and optional waveform data.
5. MP3 waveform extraction already decodes the audio through Java Sound SPI at `src/main/java/com/atstudio/atstudio/service/TrackService.java:370-394`; the fallback frame estimate at lines 387-390 is not a duration calculation, and `peaksFromPcmStream` ignores that estimate while reading the stream at lines 397-405.

### Code Facts: Audio Replacement

1. `src/main/java/com/atstudio/atstudio/service/TrackService.java:163-179` replaces an uploaded audio object and recomputes waveform data.
2. That block never calls `extractDuration` and the entity exposes no duration update method at `src/main/java/com/atstudio/atstudio/entity/Track.java:73-94`.
3. Therefore, an audio replacement can retain the previous file's duration even if the replacement is longer or shorter.
4. `src/main/java/com/atstudio/atstudio/controller/TrackController.java:155-165` exposes that replacement path through ADMIN `PUT /api/tracks/{trackId}`.

### Code Facts: API Propagation

- Public and ADMIN detail/create/update responses copy `track.getDuration()`: `src/main/java/com/atstudio/atstudio/dto/track/TrackResponse.java:10-27,37-56`.
- Public list responses copy the same column: `src/main/java/com/atstudio/atstudio/dto/track/TrackListItemResponse.java:10-40`.
- ADMIN list responses copy the same column: `src/main/java/com/atstudio/atstudio/dto/track/AdminTrackListItemResponse.java:10-40`.
- Download history copies the current Track duration: `src/main/java/com/atstudio/atstudio/dto/download/DownloadHistoryItemResponse.java:16-45`.
- The routes carrying those DTOs are `POST/GET/PUT /api/tracks`, `GET /api/tracks/admin`, `GET /api/tracks/admin/{id}`, and `GET /api/downloads/history`: `src/main/java/com/atstudio/atstudio/controller/TrackController.java:32-79,155-165`, `src/main/java/com/atstudio/atstudio/controller/DownloadController.java:30-41`.
- The sound-track use case defines `duration` as seconds without describing it as an estimate: `docs/design/usecase/sound-track.md:67-75`.

### Code Facts: UI Propagation and Media Correction

1. Home requests the latest Track list at `frontend/src/pages/public/HomePage.tsx:52-64` and formats each stored duration at `frontend/src/pages/public/HomePage.tsx:306-324`.
2. Track list requests the same API at `frontend/src/pages/public/TrackListPage.tsx:207-213`; `TrackRow` formats the stored value at `frontend/src/components/track/TrackRow.tsx:108-112`.
3. Track detail maps API duration into the player Track at `frontend/src/pages/public/TrackDetailPage.tsx:124-142`.
4. PlayerBar falls back to `currentTrack.duration` until runtime media duration is available: `frontend/src/layouts/PlayerBar.tsx:154-165`.
5. The player store replaces its duration with `audio.duration` on `loadedmetadata` and every `timeupdate`: `frontend/src/store/playerStore.ts:279-297`.
6. This explains why the Home/list values are wrong while the actively playing track eventually shows the correct media duration.
7. Download history maps its API duration into list context and direct play objects at `frontend/src/pages/subscriber/DownloadHistoryPage.tsx:123-153,273-297`; it does not visibly render a duration column at lines 394-463, but the incorrect value is still an initial PlayerBar/queue fallback.
8. `AlbumTrackItemResponse` and `PlaylistTrackItemResponse` do not include duration: `src/main/java/com/atstudio/atstudio/dto/album/AlbumTrackItemResponse.java:7-23`, `src/main/java/com/atstudio/atstudio/dto/playlist/PlaylistTrackItemResponse.java:7-21`. Their abbreviated playback/waveform contract is handled by WI-010 rather than this WI.

### Current Test Contract

- `src/test/java/com/atstudio/atstudio/service/TrackServiceAudioProcessingTest.java:146-153` explicitly names and expects the 128-kbps estimate: 32,768 fake MP3 bytes become 2 seconds.
- All helpers in this class exercise creation through `createTrack`: `src/test/java/com/atstudio/atstudio/service/TrackServiceAudioProcessingTest.java:198-204`.
- Focused search found no audio-replacement test asserting duration recomputation.
- The targeted test class passed unchanged. Passing confirms current implementation consistency, not media-duration correctness.

### Inferences

- **Root cause:** the API/UI mismatch is caused by persisted metadata derived from an invalid fixed-bitrate assumption, not by Home formatting and not by the stream endpoint.
- **Population risk:** every uploaded MP3 whose actual average bitrate is not the assumed 128Ki-bps is susceptible. The three available tracks all reproduce the defect; the affected database population cannot be bounded to Track 3 without a full audit.
- **Replacement risk:** correcting only `extractDuration` for new creates leaves replaced files and old rows stale.
- **UX consequence:** a buyer sees one duration in discovery and another after playback metadata loads, which weakens catalog trust and can distort initial seek accessibility values.

### Proposed SR-99 Requirements

1. **One authoritative audio analysis result**
   - Replace file-size estimation with frame/container metadata or decoded PCM frame counting that supports CBR MP3, VBR MP3, ID3 metadata, and WAV.
   - Prefer a single analysis result such as `(durationSeconds, waveformPeaks)` so duration and waveform cannot describe different files.
   - Define and test the integer-second rounding rule. Never replace 128 with another fixed bitrate.

2. **Fail closed on metadata extraction**
   - A create or audio replacement with unparseable/unsupported media must return a typed validation error and must not store an estimated or zero duration.
   - The old file, path, duration, and waveform must remain intact when replacement analysis or storage mutation fails.

3. **Create/replace parity and atomicity**
   - Run the same analyzer before create and before audio replacement.
   - Commit the file path, exact duration, and waveform as one logical mutation; metadata-only edits retain the existing audio analysis.
   - Add an entity/service operation that prevents independently replacing only one of those values.

4. **Existing-data audit and backfill**
   - Run a read-only dry run over active and inactive retained Track files with columns: Track ID, storage key/status, old duration, extracted duration, delta, parser result, and proposed action.
   - Define a tolerance, require explicit approval for mutation, backfill in resumable batches, preserve old values for rollback, and isolate failures without silently setting zero.
   - Re-query public list/detail, ADMIN list/detail, and download history after the batch and reconcile changed/error counts.

5. **Observability**
   - During a bounded post-release period, compare server duration with browser media duration when metadata loads and record only Track ID, both numeric durations, delta, and client media error code. Do not record URLs containing secrets or user identifiers.

## Required Future Test Matrix

| Layer | Scenario | Required result |
| --- | --- | --- |
| Analyzer unit | 128kbps CBR MP3 | Extracted duration matches fixture metadata within approved tolerance |
| Analyzer unit | 320kbps CBR MP3 | Same duration accuracy; result does not scale with the old assumption |
| Analyzer unit | VBR MP3 with Xing/VBRI | Correct duration independent of average bitrate |
| Analyzer unit | MP3 with ID3v2/album art | Tags do not inflate duration |
| Analyzer unit | Existing 8/16/24-bit WAV cases | Duration and waveform regressions remain green |
| Analyzer unit | Truncated, mislabeled, unreadable media | Typed failure; no guessed/zero success result |
| Service create | Valid MP3 | Exact duration and waveform persisted from the same analysis result |
| Service replace | New audio has different length | File, duration, and waveform all change together |
| Service replace | Analyzer/storage failure | Old file and all old metadata remain unchanged |
| Service update | Title/tags only | Existing duration and waveform remain unchanged |
| API integration | Public list/detail and ADMIN list/detail | Same corrected duration returned everywhere |
| API integration | Download history | Corrected current duration propagated into playback object |
| Frontend | 93s and 446s fixtures | Home/TrackRow render 1:33 and 7:26 |
| Frontend/player | Before and after `loadedmetadata` | No material duration jump; seek max/progress remain correct |
| Backfill integration | Dry run, partial parser failure, rerun, rollback | Deterministic counts, no zero overwrite, resumable and reversible |

## Evidence Pointers

### Files Changed

- `deliverables/user/WI-20260808-ATS-008-summary.md` — user-facing finding, impact, recommendation, and required tests.
- `deliverables/agent/WI-20260808-ATS-008-evidence-pack.md` — reproducible calculations, source pointers, and SR-99 requirements.

### Key Locations

- `src/main/java/com/atstudio/atstudio/service/TrackService.java:63-98` — create analysis and persistence.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:163-202` — audio replacement omits duration recomputation.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:291-305` — 128-Kib/s MP3 size estimate.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:370-405` — MP3 decode/waveform path and ignored frame estimate.
- `src/main/java/com/atstudio/atstudio/entity/Track.java:40-47,73-94` — persisted duration and no duration update method.
- `src/main/java/com/atstudio/atstudio/dto/track/TrackResponse.java:10-56` — detail/create/update duration propagation.
- `src/main/java/com/atstudio/atstudio/dto/track/TrackListItemResponse.java:10-40` — public list duration propagation.
- `src/main/java/com/atstudio/atstudio/dto/track/AdminTrackListItemResponse.java:10-40` — ADMIN list duration propagation.
- `src/main/java/com/atstudio/atstudio/dto/download/DownloadHistoryItemResponse.java:16-45` — download history duration propagation.
- `frontend/src/pages/public/HomePage.tsx:52-64,306-324` — latest list request and direct duration display.
- `frontend/src/components/track/TrackRow.tsx:108-112` — public Track list display.
- `frontend/src/store/playerStore.ts:279-297` — browser metadata overrides the stored fallback.
- `frontend/src/layouts/PlayerBar.tsx:154-165` — runtime duration or stored fallback selection.
- `src/test/java/com/atstudio/atstudio/service/TrackServiceAudioProcessingTest.java:146-153,198-204` — faulty estimate encoded in create-only tests.

## Commands & Outputs

| Command | Result |
| --- | --- |
| `GET https://.../api/tracks/{1\|2\|3}` | API durations: 229, 229, 1,090 seconds; titles and active records confirmed |
| `curl.exe -D - -o NUL -H "Range: bytes=0-0" https://.../api/tracks/{id}/stream` | Three `206 Partial Content` responses; totals 3,756,312, 3,756,312, 17,863,782 bytes; no full-file download |
| `GET https://.../api/tracks?page=1&size=3&sort=latest` | Latest order 3, 2, 1 and the same duration values used by Home |
| PowerShell calculation of `floor(bytes/(128*1024/8))` and `bytes*8/actualSeconds/1000` | Exact persisted values and average bitrates 323.12, 323.12, 320.43kbps |
| `rg -n "extractDuration\|duration\|loadedmetadata" frontend/src src/main/java src/test/java` plus numbered reads | Located the full create/update/DTO/UI/player/test path |
| `.\gradlew.bat test --tests "com.atstudio.atstudio.service.TrackServiceAudioProcessingTest"` | PASS in 18 seconds; existing 128-kbps expectation remains green |

One convenience `Invoke-WebRequest` attempt could not set the restricted PowerShell `Range` header; the evidence was re-run successfully with `curl.exe` and the failed command made no state change.

## Tests

- Ran: `.\gradlew.bat test --tests "com.atstudio.atstudio.service.TrackServiceAudioProcessingTest"`
- Result: PASS, process exit code 0, 18 seconds.
- Interpretation: the current test suite verifies the implementation's documented 128-kbps estimate; it does not verify actual MP3 duration and has no replacement-duration assertion.
- No new product tests were added because this WI is documentation-only.

## Risks / Rollback

### Risks

- Java Sound SPI duration/frame metadata behavior must be proven against VBR, tags, malformed files, and production-size uploads. If decoded PCM byte counting is selected, memory and processing limits must be verified because waveform extraction already reads the full stream.
- A third-party metadata library would require a separate dependency/security/license decision; this WI specifies the accuracy contract rather than selecting a library.
- Existing-data backfill reads storage and mutates database rows, so it requires its own approval, dry run, batching, failure isolation, and rollback evidence.
- Integer rounding can differ by one second from browser formatting; a single documented rounding/tolerance policy is required.
- Runtime telemetry must avoid exposing storage keys, signed URLs, or user data.

### Rollback

- Documentation-only output: remove only `deliverables/user/WI-20260808-ATS-008-summary.md` and `deliverables/agent/WI-20260808-ATS-008-evidence-pack.md` if this investigation is abandoned.
- The targeted test created only normal ignored build output. No application, SR, index, database, storage object, public runtime, or external state requires rollback.

## Follow-ups

- `WI-20260808-ATS-011` should consume this Evidence Pack when drafting `docs/SR/SR-99.md`.
- A later approved implementation REQ should separate analyzer design, backend mutation parity, data backfill, frontend regression, and independent QA.
- SR-99 cannot be closed by changing 128 to 320; CBR/VBR-safe extraction and existing-row audit are required.

## Related Documents

- [WI-008 Handoff](WI-20260808-ATS-008-handoff.md)
- [WI-008 User Summary](../user/WI-20260808-ATS-008-summary.md)
- [Approved REQ](../user/REQ-20260808-ATS-003.md)
