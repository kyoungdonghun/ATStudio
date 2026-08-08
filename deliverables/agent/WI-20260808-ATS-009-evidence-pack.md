---
version: 1.0
last_updated: 2026-08-08
project: ATS
owner: tr
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260808-ATS-009-handoff.md
    reason: Approved Work Item scope and output contract
  - path: ../user/REQ-20260808-ATS-003.md
    reason: Approved request and acceptance criteria
---
# Evidence Pack: WI-20260808-ATS-009

## Summary (one-liner)

- Confirmed that Home exposes only Genre and Mood despite live Instrument and Usage tag types, verified that Instrument filtering works below the page layer while Usage has no active-track assignment, and recommended a Usage-first consolidated discovery module with complete Track List query parity.

## Scope / DoD Check

- [x] Confirmed that Home fetches and renders only `GENRE` and `MOOD` discovery controls.
- [x] Recorded the live public tag inventory: `GENRE=5`, `MOOD=4`, `INSTRUMENT=4`, `USAGE=1`.
- [x] Distinguished registered tags from tags attached to active tracks.
- [x] Verified the public backend Instrument filter against live data and identified the missing public Track List page wiring.
- [x] Verified that the public Track List page already supports Usage URL, request, chip, modal, and reset flows.
- [x] Compared four stacked sections, a consolidated category-tab module, and an untyped mixed tag cloud.
- [x] Recommended `USAGE` as the first discovery category while retaining `INSTRUMENT` without adding four full-height Home sections.
- [x] Included mobile-density, no-tag, no-result, accessibility, and regression-test requirements.
- [x] Kept the completed `SR-04` historical scope separate from proposed `SR-100`.
- [x] Modified only the two WI-009 output files.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Constitution, buyer discovery, simplicity, and evidence transparency |
| 0 | `docs/standards/glossary.md` | Canonical `Tag` and `Usage Guide Tag` meanings |
| 2 | `docs/design/usecase/sound-tag.md` | Four tag types and public tag-list contract |
| 2 | `docs/design/usecase/sound-track.md` | Public search and visible Usage Guide Tag behavior |
| 2 | `docs/SR/SR-04.md` | Completed Mood-section historical boundary |
| 2 | `docs/ui/mockup/main.html` | Original Genre-section layout baseline |
| Context | `deliverables/user/REQ-20260808-ATS-003.md` | Approved SR-99 through SR-101 scope and quality gates |
| Code | `frontend/src/pages/public/HomePage.tsx` | Current Home data loading, navigation, and discovery rendering |
| Code | `frontend/src/pages/public/TrackListPage.tsx` | Current public filter URL and UI behavior |
| Code | `frontend/src/components/filter/TagFilterModal.tsx` | Current full-filter modal categories |
| Code | `frontend/src/api/tags.ts` | Public tag API client |
| Code | `src/main/java/com/atstudio/atstudio/service/TagService.java` | Tag inventory and active-track availability behavior |
| Code | `src/main/java/com/atstudio/atstudio/service/TrackService.java` | Backend tag filter specifications |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Workspace source: `.claude/config/workspace.json`
- Assignee: `tr`
- Task type: research, information architecture
- `agent_required_tiers`: `[0]`
- Additional WI-specific Tier 2 design, UI, SR, and code context came from the handoff packet.

## Evidence Pointers (required)

### Current Home exposure

- `frontend/src/pages/public/HomePage.tsx:35-38`
  - Home owns only `genreTags` and `moodTags` discovery state.
- `frontend/src/pages/public/HomePage.tsx:52-58`
  - Home requests only `fetchTags('GENRE')` and `fetchTags('MOOD')` alongside album and track feeds.
- `frontend/src/pages/public/HomePage.tsx:108-128`
  - Home navigation handlers create only repeated `genre` or `mood` query parameters.
- `frontend/src/pages/public/HomePage.tsx:334-378`
  - Rendered discovery blocks are only `장르별 탐색` and `분위기별 탐색`.
- `frontend/src/pages/public/HomePage.module.css:109-111,284-290,453-487`
  - Each discovery block is a full Home section, tags wrap, and the mobile layout only reduces section padding; four duplicated sections would increase vertical density.
- Runtime snapshot supplied in the WI handoff:
  - The public `/` DOM showed only `장르별 탐색` and `분위기별 탐색`.

### Tag type and domain contracts

- `frontend/src/types/index.ts:98-103`
  - The client type system recognizes `GENRE`, `MOOD`, `INSTRUMENT`, and `USAGE`.
- `docs/design/usecase/sound-tag.md:21-24,40-56`
  - Tag create/list contracts support all four types and define `USAGE` as a visible guide/search tag.
- `docs/standards/glossary.md:75-78`
  - `Usage Guide Tag` describes the expected content use case and must not be treated as a License.
- `docs/design/usecase/sound-track.md:51-55,78`
  - Public discovery explicitly includes Usage filtering and visible Usage hashtags. The use-case text does not list Instrument among the user criteria even though the current API and service support it; this contract gap should be synchronized by the follow-up implementation.

### Search-layer parity and gap

- `frontend/src/api/tracks.ts:32-43,55-65`
  - The shared track API client accepts and forwards both `instrument` and `usage`.
- `src/main/java/com/atstudio/atstudio/dto/track/TrackSearchRequest.java:15-18`
  - Backend request binding contains Genre, Mood, Instrument, and Usage fields.
- `src/main/java/com/atstudio/atstudio/controller/TrackController.java:47-50`
  - Public track listing binds the full `TrackSearchRequest` through `@ModelAttribute`.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:110-120`
  - The backend builds type-specific specifications for all four tag families.
- `frontend/src/pages/public/TrackListPage.tsx:82-89,149-163,192-200`
  - The public list reads Genre, Mood, and Usage from the URL and sends them to `fetchTracks`; Instrument is absent.
- `frontend/src/pages/public/TrackListPage.tsx:313-369,439-532`
  - Public chips, apply, reset, and render behavior cover Genre, Mood, and Usage only.
- `frontend/src/components/filter/TagFilterModal.tsx:7-18,34-58,78-80`
  - The modal input and output contracts have no Instrument collection.
- `frontend/src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx:813-830`
  - Existing public-list regression coverage asserts Genre, Mood, and Usage forwarding, but not Instrument.
- `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx:604-623`
  - Existing Home tests assert only Genre and Mood exploration.

### Active-track availability

- `src/main/java/com/atstudio/atstudio/service/TagService.java:63-103`
  - `/api/tags/available` derives returned tags through active `tracks` and `track_tags`, unlike raw type inventory. Its incoming cross-filter criteria currently cover Genre, Mood, Usage, and BPM, not Instrument.
- `src/main/java/com/atstudio/atstudio/controller/TagController.java:36-46`
  - The available-tags endpoint also omits an Instrument request parameter, so full four-family dynamic filtering requires contract parity work.

### SR boundary

- `docs/SR/SR-04.md:1-15`
  - SR-04 added Mood as a structural copy of Genre when only Genre existed and connected Mood to track discovery.
- `docs/SR/index.md:10`
  - SR-04 is `DONE`; SR-100 should preserve that historical result rather than reopen or redefine it as a failure.

## Live Runtime Evidence

Observed on 2026-08-08 against `https://comparable-indicate-black-guidelines.trycloudflare.com` using read-only public endpoints.

| Request | Result |
| --- | --- |
| `GET /api/tags?type=GENRE` | 5 tags: `특문,띄워쓰기테스트@ @ @`, `genre01`, `genre02`, `genre03`, `genre04` |
| `GET /api/tags?type=MOOD` | 4 tags: `mood01` through `mood04` |
| `GET /api/tags?type=INSTRUMENT` | 4 tags: `instrument01` through `instrument04` |
| `GET /api/tags?type=USAGE` | 1 tag: `#비가오면` |
| `GET /api/tracks?page=1&size=50` | 3 active tracks; track IDs 3 and 2 carry `INSTRUMENT:instrument03`; no active track carries `USAGE` |
| `GET /api/tracks?page=1&size=50&instrument=instrument03` | 2 results, track IDs 3 and 2 |
| `GET /api/tracks?page=1&size=50&usage=%23비가오면` | 0 results |

Interpretation:

- Raw `INSTRUMENT` and `USAGE` master data both exist; neither can be described as absent.
- Instrument is already operational in the public backend and has result-bearing data, but it is not reachable through the public Track List page state/UI.
- Usage is already reachable through the public Track List state/UI, but the current live tag has no active-track association. Exposing it as a normal Home entry would currently lead to an empty result.
- The Cloudflare tunnel URL and its data are ephemeral runtime evidence, not a permanent content baseline.

## Option Comparison

| Option | Benefits | Costs / Failure Modes | Decision |
| --- | --- | --- | --- |
| Four independent stacked sections | Minimal conceptual change; reuses current Genre/Mood block | Repeated controls, longer Home, mobile vertical bloat, empty-section risk, weak Usage priority | Reject |
| One typed discovery module with category tabs | Preserves all four meanings, expresses Usage-first ordering, stable height, one empty-state contract, scalable on mobile | Moderate Home state/refactor work; needs accessible tabs and Track List Instrument parity | Recommend |
| One untyped mixed tag cloud | Smallest visual footprint | Category meaning disappears; identical names become ambiguous; users cannot predict the filter dimension | Reject |

## Recommended SR-100 Requirements

1. **Unified information architecture**
   - Replace duplicated full-height discovery sections with one typed discovery module.
   - Present categories in `USAGE → GENRE → MOOD → INSTRUMENT` order.
   - Keep the canonical display concepts `용도`, `장르`, `분위기`, and `악기`; do not relabel Usage as a License.

2. **Usage-first behavior with content readiness**
   - Make Usage the first category and the default only when it contains at least one result-bearing tag.
   - If Usage exists in the master table but has no active-track association, show an explicit no-content state or fall back to the first result-bearing category while keeping the Usage category visible and understandable.
   - Do not infer that an empty Usage result means the tag master data is absent.

3. **Availability-aware tag source**
   - Home discovery should prefer tags attached to active tracks, using `/api/tags/available` or an equivalent contract, to avoid dead-end navigation.
   - Extend availability filtering to accept and preserve Instrument if dynamic cross-filter availability is used.
   - Define separate UI states for: category has no configured tags, configured tags have no active tracks, request failed, and results are available.

4. **End-to-end query parity**
   - Usage links must continue to produce `/tracks?usage=<encoded-name>` and restore active Usage state.
   - Instrument links must produce `/tracks?instrument=<encoded-name>` and the Track List must read, request, display, preserve, and reset Instrument exactly like other tag families.
   - Include Instrument in request keys, availability requests, active-filter calculation, pagination/sort preservation, modal contracts, and reset-all behavior.
   - Preserve the current repeated-parameter-to-CSV AND semantics for multi-selection.

5. **Mobile and accessibility**
   - Use one horizontally scrollable or equivalently compact category selector on narrow viewports rather than four stacked sections.
   - Cap initially visible chips and provide an explicit `더보기` behavior where needed.
   - Implement category selection with keyboard and screen-reader semantics, visible focus, and a stable selected state.

6. **Tests**
   - Home loads all four category contracts and renders the Usage-first order.
   - Usage defaults when result-bearing; empty Usage falls back or exposes the approved empty state.
   - Tag names containing spaces, commas, `#`, or non-ASCII characters remain correctly encoded and restored.
   - Instrument and Usage both survive Home navigation, Track List restoration, API forwarding, pagination, sorting, modal apply, and reset-all.
   - Empty category, no-result category, API failure, and mobile-width layouts have dedicated tests.
   - Backend integration tests prove single and multiple Instrument/Usage filters retain current AND semantics.

7. **Historical and policy boundaries**
   - Keep SR-04 `DONE`; fold its delivered Mood capability into the unified module without rewriting the historical SR.
   - Treat Usage tag assignment/content seeding as a separate operational prerequisite, not as invented data in the UI.
   - Coordinate tag-name normalization concerns with the existing tag-input policy SR instead of silently rewriting live tag values in this feature.

## Commands & Outputs

| Command | Result |
| --- | --- |
| `rg -n -C 6 "fetchTags\|장르별 탐색\|분위기별 탐색\|INSTRUMENT\|USAGE" frontend/src/pages/public/HomePage.tsx` | Only Genre and Mood are fetched/rendered; Usage is used only as new-track metadata fallback |
| `rg -n -C 5 "instrument\|usage\|genre\|mood" frontend/src/pages/public/TrackListPage.tsx` | Usage is wired end to end in page state; Instrument is absent |
| `rg -n -C 5 "instrument\|usage" frontend/src/api/tracks.ts src/main/java/com/atstudio/atstudio/dto/track src/main/java/com/atstudio/atstudio/service/TrackService.java` | Shared client and backend both support Instrument and Usage queries |
| `rg -n -C 6 "USAGE\|INSTRUMENT\|탐색" docs/design/usecase docs/SR/SR-04.md docs/ui/mockup/main.html` | Four-type tag contract and SR-04 scope confirmed |
| PowerShell `Invoke-RestMethod` loop over `/api/tags?type={GENRE,MOOD,INSTRUMENT,USAGE}` | Counts `5 / 4 / 4 / 1`; names recorded above |
| PowerShell `Invoke-RestMethod` over `/api/tracks` baseline and filtered queries | Three active tracks; Instrument 03 returns IDs 3 and 2; current Usage tag returns zero |

The web-open helper rejected the temporary Cloudflare hostname as an unsafe direct-open target, so runtime verification used read-only HTTPS requests from PowerShell. No authentication material or environment secret was read or printed.

## Tests

- Product test suites were not run because WI-009 is a read-only investigation and does not alter application code.
- Existing tests were inspected for contract coverage:
  - Home exploration currently covers only Genre and Mood.
  - Track List filtering currently covers Genre, Mood, Usage, and BPM, not Instrument.
  - The shared API client test already exercises both Instrument and Usage serialization.
- Live public API smoke verification: PASS for tag inventory and Instrument filtering; Usage returned a valid empty result, consistent with current active-track associations.

## Risks / Rollback

### Risks

- Four independently stacked sections would satisfy label completeness while degrading Home scan length and mobile density.
- Adding only a Home Instrument link would create a misleading URL because the current Track List page drops `instrument`.
- Raw master-tag loading can create dead-end chips; current live Usage data demonstrates this risk.
- Hiding Usage whenever it has no results would conceal the intended product taxonomy and make content-readiness gaps harder to operate.
- Usage names are content guides, not legal rights; wording them as permissions or licenses would violate the glossary contract.

### Rollback

- Remove `deliverables/user/WI-20260808-ATS-009-summary.md` and this Evidence Pack.
- No product code, SR, index, tag data, track data, or runtime state needs restoration because none was changed.

## Follow-ups

- `WI-20260808-ATS-011` should translate these findings into `SR-100`, preserving the verified runtime counts and the raw-data versus result-bearing-data distinction.
- The eventual implementation WI should decide the exact accessible tab component and the approved empty-Usage behavior before changing Home or Track List code.
