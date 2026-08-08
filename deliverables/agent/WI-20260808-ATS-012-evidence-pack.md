---
version: 1.0
last_updated: 2026-08-08
project: ATS
owner: qa-integ
category: evidence-pack
status: confirmed
related_wi: WI-20260808-ATS-012
dependencies:
  - path: WI-20260808-ATS-012-handoff.md
    reason: Approved independent validation scope and output contract
  - path: ../user/REQ-20260808-ATS-003.md
    reason: Approved three-SR acceptance criteria
  - path: ../user/WI-20260808-ATS-012-summary.md
    reason: User-facing validation result and findings
---

# Evidence Pack: WI-20260808-ATS-012

## Summary (one-liner)

- Independently validated SR-99 through SR-101 against the public acceptance runtime, current API and code contracts, prerequisite Evidence Packs, indexes, and documentation gates; completed with zero blockers, zero major findings, and two non-blocking wording/unit findings.

## Scope / DoD Check

- [x] Reproduced Home duration displays and all three PlayerBar media durations.
- [x] Re-queried Track APIs and Range totals and reproduced the persisted duration formula.
- [x] Revalidated creation, audio-replacement, duration propagation, and browser metadata correction code paths.
- [x] Revalidated Home Tag exposure, live Tag counts, active associations, Instrument backend support, and Track List Instrument gap.
- [x] Revalidated immediate waiting/stalled transitions, recovery events, Album DTO omission, null mappings, and collection/queue impact.
- [x] Verified SR/index/document counts, statuses, local links, Markdown structure, encoding, trailing whitespace, and EOF.
- [x] Ran `validate-docs`, `git diff --check`, the focused backend test, and focused player tests.
- [x] Created only the WI-012 user summary and this Evidence Pack; no SR, index, product code, DB, storage object, or public data was changed.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Constitution, platform integrity, buyer discovery, and transparency |
| 0 | `docs/standards/development-standards.md` | Cross-layer code, transaction, and testing standards |
| 0 | `docs/standards/documentation-standards.md` | Documentation structure and link rules |
| 0 | `docs/standards/glossary.md` | Canonical Track, Tag, Usage Guide Tag, and playback terms |
| 1 | `docs/policies/quality-gates.md` | Independent regression and traceability checks |
| 1 | `docs/standards/evidence-pack-standard.md` | Reproducible Evidence Pack contract |
| 1 | `docs/standards/frontend-standards.md` | React/Zustand player and API conventions |
| 2 | `docs/design/api-spec.md` | Current REST route boundary |
| 2 | `docs/design/usecase/sound-track.md` | Duration, waveform, listening, and update contracts |
| 2 | `docs/design/usecase/sound-tag.md` | Four Tag types and Usage Guide Tag meaning |
| Context | `deliverables/user/REQ-20260808-ATS-003.md` | Approved scope and quality gates |
| Evidence | `deliverables/agent/WI-20260808-ATS-008-evidence-pack.md` | Duration runtime, formula, code, and test evidence |
| Evidence | `deliverables/agent/WI-20260808-ATS-009-evidence-pack.md` | Tag inventory, associations, and search parity evidence |
| Evidence | `deliverables/agent/WI-20260808-ATS-010-evidence-pack.md` | Buffering and waveform transport evidence |
| Evidence | `deliverables/agent/WI-20260808-ATS-011-evidence-pack.md` | DocOps integration and count evidence |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `qa-integ`
- Task type: documentation, integration, review
- Workspace tag: `ATS`

## Validation Findings

| Severity | Count | Result |
| --- | ---: | --- |
| BLOCKER | 0 | None |
| MAJOR | 0 | None |
| MINOR | 2 | Exact stalled-message quote and bitrate unit notation |

### MINOR-1: SR-101 Exact Message Quote

- `docs/SR/SR-101.md:14` quotes `재생이 지연되고 있습니다. 잠시 기다리거나 다시 시도해 주세요.`
- `frontend/src/layouts/PlayerBar.tsx:16` currently defines `재생이 지연되고 있습니다. 연결을 확인한 뒤 다시 시도해 주세요.`
- Impact: no change to the event condition, root cause, affected screens, or proposed state model; current UI wording should be corrected in the SR before implementation handoff if exact-text traceability is required.

### MINOR-2: SR-99 Bitrate Unit Notation

- `docs/SR/SR-99.md:22,30,34` uses `128Ki-bps`, while lines 41 and 70 use `128kbps`.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:300-302` divides by `128 * 1024 / 8`, exactly 16,384 bytes/s or 128 Kibit/s.
- Impact: no change to the reproduced duration values or root cause. Prefer one notation: `128 Kibit/s (16,384 bytes/s)`; keep calculated real average bitrates in decimal `kbps` when stated as such.

## Cross-Layer Evidence

### SR-99: Duration

| Track | Home/API | Independent PlayerBar | Range total | Formula result |
| --- | ---: | ---: | ---: | ---: |
| 1 | 229s (`3:49`) | `1:33` | 3,756,312 bytes | `floor(bytes/16,384)=229` |
| 2 | 229s (`3:49`) | `1:33` | 3,756,312 bytes | `floor(bytes/16,384)=229` |
| 3 | 1,090s (`18:10`) | `7:26` | 17,863,782 bytes | `floor(bytes/16,384)=1,090` |

- Public browser Home DOM independently showed Track 3 first at `18:10` and Tracks 2/1 at `3:49`.
- Direct play from `/tracks/1`, `/tracks/2`, and `/tracks/3` independently showed `1:33`, `1:33`, and `7:26` after media metadata loaded.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:68-91` calculates and persists duration/waveform on create.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:169-179` replaces audio and waveform without recomputing duration.
- `frontend/src/store/playerStore.ts:279-297` replaces the fallback duration with native media duration; `frontend/src/layouts/PlayerBar.tsx:154-165` uses runtime duration before the stored fallback.

### SR-100: Tag Discovery

| Check | Result |
| --- | --- |
| Public Tag inventory | GENRE 5, MOOD 4, INSTRUMENT 4, USAGE 1 |
| Active Tracks | 3 |
| `instrument03` | Track IDs 3 and 2; filtered total 2 |
| Current Usage association | 0 active Tracks; filtered total 0 |
| Home exposure | Genre and Mood only |

- `frontend/src/pages/public/HomePage.tsx:35-57,334-376` owns, fetches, and renders only Genre and Mood.
- `frontend/src/api/tracks.ts:32-65` and `src/main/java/com/atstudio/atstudio/service/TrackService.java:110-118` support Instrument and Usage.
- `frontend/src/pages/public/TrackListPage.tsx:81-89,148-199` handles Usage but does not read, load, or forward Instrument.
- `src/main/java/com/atstudio/atstudio/controller/TagController.java:36-47` and `TagService.java:63-103` omit Instrument from `/api/tags/available` cross-filter input, matching SR-100's required parity work.

### SR-101: Buffering and Waveform

- `frontend/src/store/playerStore.ts:299-314` sets `isStalled` immediately on native `waiting`/`stalled`, with no elapsed-time threshold, and clears it on `canplay`/`playing`; `timeupdate` also clears at lines 279-282.
- WI-010 records an immediate `/albums/2` warning followed by successful playback within approximately 1.8 seconds. The independent rerun used cached Track 2 and did not emit a visible warning, which is consistent with an event-dependent transient rather than a deterministic failure.
- Public `GET /api/albums/2` Track keys were `trackId,title,artistName,thumbnailUrl,order`; duration and waveform were absent.
- Public `GET /api/tracks/2` returned duration 229 and a 1,201-character waveform string.
- `AlbumTrackItemResponse.java:7-23`, `frontend/src/api/albums.ts:6-12`, and `AlbumDetailPage.tsx:64-86,153-173,228-246` prove the transport omission and all three explicit `0`/`null` playback mappings.
- `frontend/src/components/player/WaveformCanvas.tsx:34-42` renders a flat line only when peak data is absent.
- `frontend/src/store/playerStore.ts:371-446,479-485` consumes context/queue Tracks unchanged for next, previous, and play-all, so the abbreviated object persists across traversal.
- Focused searches confirmed the documented analogous mappings in Playlist detail, Like list, Download History, PlaylistDrawer, and HistoryModal.

## Counts, Links, and Markdown

| Check | Result |
| --- | --- |
| Numbered SR files | 100 |
| Numbered SR index rows | 100 |
| Statuses | DONE 82, OPEN 15, NOT CONFIRMED 2, DROPPED 1 |
| Actual managed docs | 202 |
| `docs/index.md` category sum | 202 |
| New SR local links | 10 checked, 0 broken |
| SR H1 count | One per file |
| Code fences | Balanced; SR-101 has one fenced block |
| Trailing whitespace / replacement characters | 0 / 0 |
| EOF newline | Present in all three SR files |

## Commands & Outputs

| Command / action | Result |
| --- | --- |
| Read-only public `GET /api/tracks/{1,2,3}` | durations 229, 229, 1,090 |
| `curl.exe -H "Range: bytes=0-0" .../api/tracks/{id}/stream` | totals 3,756,312; 3,756,312; 17,863,782 bytes |
| Browser Home and direct Track play checks | Home `3:49/3:49/18:10`; player `1:33/1:33/7:26` |
| Public Tag inventory/filter requests | `5/4/4/1`; Instrument total 2 IDs 3,2; Usage total 0 |
| Public Album 2 and Track 2 requests | Album omits duration/waveform; Track waveform length 1,201 |
| Targeted numbered code reads and `rg` | SR root causes and cross-screen mappings confirmed |
| PowerShell SR/status/category/Markdown checks | 100 SRs, required statuses, 202 docs, structural checks PASS |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS; Tier 0, links, 491 traceability IDs, and index coverage |
| `git diff --check` | PASS; only CRLF-to-LF normalization warnings for two tracked indexes |

## Tests

- Backend command: `.\gradlew.bat test --tests "com.atstudio.atstudio.service.TrackServiceAudioProcessingTest"`
  - PASS, exit code 0, Gradle build successful; the current 128-rate expectation remains the implemented contract and therefore does not prove media accuracy.
- Frontend command: `npm test -- src/store/playerStore.test.ts src/layouts/PlayerBar.test.tsx` from `frontend/`
  - PASS, exit code 0, 2 files and 27/27 tests passed in 3.60 seconds.
  - Current tests cover immediate stalled behavior; threshold/debounce behavior remains future implementation scope.

## Evidence Pointers

### Files Changed

- `deliverables/user/WI-20260808-ATS-012-summary.md` — user-facing independent validation result and finding counts.
- `deliverables/agent/WI-20260808-ATS-012-evidence-pack.md` — this reproducible Evidence Pack.

### Validated Deliverables

- `docs/SR/SR-99.md`
- `docs/SR/SR-100.md`
- `docs/SR/SR-101.md`
- `docs/SR/index.md`
- `docs/index.md`

## Risks / Rollback

### Risks

- The Cloudflare acceptance URL and its data are ephemeral; runtime facts are dated 2026-08-08 and paired with stable code evidence.
- Cache/network state controls whether a brief `waiting` event is emitted. Failure to reproduce the visible warning on a cached rerun does not invalidate the immediate event handler or the recorded uncached 1.8-second observation.
- The two MINOR findings can create exact-text or unit ambiguity if copied directly into implementation tests, but they do not alter feature scope or root-cause conclusions.

### Rollback

- Remove only `deliverables/user/WI-20260808-ATS-012-summary.md` and `deliverables/agent/WI-20260808-ATS-012-evidence-pack.md`.
- Test commands created or reused only normal ignored build/test output. No product, SR, index, database, storage, public runtime, or unrelated user artifact requires rollback.

## Follow-ups

- Before an implementation handoff, correct the SR-101 exact message quote and standardize SR-99's fixed-rate notation.
- Product changes, duration backfill, Tag content assignment, and runtime deployment require separately approved implementation WIs.
