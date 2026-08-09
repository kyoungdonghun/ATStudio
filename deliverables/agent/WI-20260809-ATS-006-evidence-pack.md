# WI-20260809-ATS-006 Evidence Pack

## 1. Work Item

| Field | Value |
|---|---|
| WI | `WI-20260809-ATS-006` |
| Parent requirement | `REQ-20260808-ATS-004` |
| Role | `cr` - independent backend reviewer |
| Date | 2026-08-09 |
| Authoritative handoff | `deliverables/agent/WI-20260809-ATS-006-handoff.md` |
| Review result | `0 BLOCKER / 2 MAJOR / 1 MINOR` |
| Product modifications | None |
| Downstream gate | `WI-20260808-ATS-029` remains blocked |

## 2. Scope And Method

The review was limited to the handoff-listed backend media/tag symbols, directly affected album/playlist/like/download aggregation paths, security matchers needed to verify unchanged playback/download policy, and focused tests. Frontend code was not reviewed.

The review used targeted symbol search, bounded file reads, and scoped diffs. It stopped when the implementation paths, test coverage, and three confirmed contract gaps were sufficiently evidenced. No external service, secret, archive, live database, or live storage was accessed.

## 3. Reviewed Symbols

### Tag And Search

- `TagController`
- `TagService`
- `TagNamePolicy`
- `TagNameConstraintTranslator`
- `TagCreateRequest`
- `TagRepository`
- `Tag`, `TagType`
- `TrackController` search path
- `TrackService` search/filter path
- `TrackSpecification`
- `TrackSearchRequest`, `RequestDTO`

### Audio And Thumbnail Processing

- `TrackService` create/update media paths
- `Track` audio metadata mutation
- `AudioAnalysisService`
- `AudioAnalysisResult`, `AudioFormat`, `AudioAnalysisException`
- `AdminTrackAudioAnalysisController`
- `AdminTrackAudioAnalysisService`
- Admin audio-analysis request/response DTOs
- `CanonicalImageService`
- `StorageMutationCoordinator` transaction callbacks referenced by the reviewed media paths

### PlayableTrack And Affected Aggregates

- `TrackController` PlayableTrack batch path
- `PlayableTrackService`
- `PlayableTrackBatchRequest`, `PlayableTrackResponse`
- `TrackRepository`, `TrackTagRepository`
- `AlbumService`, `AlbumRepository`, `AlbumTrackRepository`
- `PlaylistService`, `PlaylistTrackRepository`
- `LikeService`, `LikeRepository`
- `DownloadService`, `TrackDownloadRepository`
- Album, playlist, like, and download response DTOs
- `SecurityConfig` stream, batch, and download matcher behavior

### Focused Tests Reviewed

- `TagNamePolicyTest`
- `TagNameConstraintTranslatorTest`
- `TagServiceTest`
- `TagServiceBranchCoverageTest`
- `TagServiceAvailableTagsIntegrationTest`
- `TagControllerTest`
- `TrackControllerTest`
- `AudioAnalysisServiceTest`
- `TrackServiceTest` media update coverage
- Storage transaction coordination tests used by track upload/replacement
- `AdminTrackAudioAnalysisServiceTest`
- `CanonicalImageServiceTest`
- `PlayableTrackServiceTest`
- `PlayableTrackQueryCountTest`
- Focused album, playlist, like, download, and security tests referenced by the changed paths

## 4. Commands And Evidence Sources

Representative commands executed during review:

```powershell
git status --short --branch
git diff --name-only
git diff --stat
rg -n "<target symbol or invariant>" <scoped backend/test paths>
Get-Content -Encoding utf8 <handoff, standard, source, or focused-test file>
git diff -U80 -- <scoped backend/test file list>
```

Documents inspected for review context and prior verification evidence:

- `deliverables/agent/WI-20260809-ATS-006-handoff.md`
- `deliverables/user/REQ-20260808-ATS-004.md`
- Relevant Tier 0 standards named by the handoff
- Focused predecessor evidence for `WI-20260808-ATS-023` through `WI-20260808-ATS-027`
- `deliverables/agent/WI-20260808-ATS-029-handoff.md`
- Relevant SR acceptance documents for tag normalization, search/filter, media analysis, thumbnail handling, and PlayableTrack behavior

No new test command was executed. Existing focused tests were inspected, and prior `WI-20260808-ATS-027` evidence records a passing backend build with 1,357 tests and 13 skipped tests. That prior gate is supporting evidence, not a substitute for the missing regression tests identified below.

## 5. Confirmed Findings

### MAJOR-001 - Public track search accepts unbounded or invalid page sizes

**File:line**

- `TrackController.java:49-52`
- `TrackService.java:100-118`
- `RequestDTO.java:15-19`

**Impact**

The public search endpoint can be asked for an arbitrarily large page containing track metadata, tags, and waveform arrays. This creates an unauthenticated database, heap, serialization, and response-size amplification path. Non-positive values can also reach pagination construction and produce an implementation exception rather than a stable client error.

**Reasoning**

The controller model-binds the request without effective pagination constraints. The shared request DTO constrains the keyword but not `page` or `size`, and `TrackService` directly supplies the caller-controlled values to `PageRequest.of(...)`. The approved tag-field whitelist protects query meaning but does not constrain result cardinality.

**Missing test**

A parameterized controller/service test for `page = 0`, negative page, `size = 0`, negative size, `size = 101`, and a very large integer. It should assert the stable invalid-argument 4xx contract and verify that the repository is not called.

**Recommended fix**

Validate at the service boundary so all callers receive the same rule: `page >= 1` and a bounded size such as `1..100`. Translate violations to the established domain error and use only validated values in both the query and `pageInfo`.

### MAJOR-002 - Playlist detail hides inactive tracks while count and reorder require them

**File:line**

- `PlaylistService.java:79-114`
- `PlaylistService.java:218-232`
- `PlaylistService.java:324-348`
- `PlaylistTrackRepository.java:18-30`
- `TrackService.java:194-196`

**Impact**

A playlist containing a track that later becomes inactive has mutually inconsistent public/owner behavior: detail omits the track, list count includes it, and reorder rejects the visible ID set because the validator requires the hidden membership. The playlist can become impossible to reorder through the visible contract.

**Reasoning**

Detail retrieval uses an active-track predicate. Count aggregation and the locked membership load used by reorder include every persisted `PlaylistTrack`. Track deactivation does not remove playlist membership, so this is a reachable steady state rather than a transient race.

**Missing test**

An integration-style service test with one active and one inactive member, asserting the list count, detail response IDs, and owner reorder outcome after deactivation.

**Recommended fix**

Make user-facing playlist count, detail, and reorder operate on the same active membership set. If inactive associations must remain for later republishing, preserve them outside the visible reorder contract and reconcile positions deterministically without requiring hidden IDs from the client.

### MINOR-001 - Public album count and track-count sorting include inactive tracks

**File:line**

- `AlbumService.java:76-107`
- `AlbumTrackRepository.java:30-44`
- `AlbumRepository.java:22-28`
- `TrackService.java:194-196`

**Impact**

Album list counts and `trackCount` ordering can disagree with the active tracks returned by album detail. This is a public consistency defect and can produce surprising ordering, although it does not expose the inactive media itself.

**Reasoning**

Album detail filters track membership by active status, while the count projection and sorting subquery include all `AlbumTrack` rows. Deactivation leaves those rows present.

**Missing test**

A mixed active/inactive album fixture covering public count, detail IDs, and ascending/descending `trackCount` sorting.

**Recommended fix**

Apply the active-track predicate to the public count projection and the public sorting expression. Introduce a separate all-membership count only if an administrative use case explicitly needs it.

## 6. No-Finding Evidence By Approved Invariant

### Tag Canonicalization And Duplicate Races

- `TagNamePolicy.java:17-39` trims supported Unicode spacing, collapses spacing, applies NFC normalization, and validates the final stored form against the approved character set. `#` is not a stored-name character.
- `TagService.java:31-46` and `TagService.java:57-72` apply the same canonicalization to create and update, flush inside the operation, and route duplicate failures through the constraint translator.
- `TagNameConstraintTranslator.java:26-67` maps only the named tag uniqueness constraint for the supported database providers; unrelated integrity failures are rethrown.
- The reviewed policy, translator, service, integration, and controller tests cover normalization, final-form validation, collisions, and stable duplicate errors.

### Search And Available-Tag Filtering

- `TagController.java:36-59` accepts repeated values only for the known tag fields and returns `dataList`.
- `TagService.java:88-159` binds fixed tag types, canonicalizes and de-duplicates values, preserves AND semantics across requested types, and filters available values to active tracks.
- `TrackService.java:100-135` and `TrackService.java:311-321` preserve `dataList`/`pageInfo`, use the approved four tag types, and retain AND semantics. MAJOR-001 is limited to pagination bounds, not field/type authorization or response shape.

### One-Pass Audio Analysis And Atomic Metadata

- `AudioAnalysisService.java:26-58` validates supported resource/file input before decoding.
- `AudioAnalysisService.java:113-190` derives duration and waveform from one PCM traversal; the bounded accumulator at `AudioAnalysisService.java:244-308` limits retained waveform samples.
- `TrackService.java:61-97` analyzes upload media before persistence and stores duration/waveform from the same result.
- `TrackService.java:161-207` analyzes replacement media before mutating the track and updates the storage key, duration, and waveform together; metadata-only updates retain existing audio analysis.
- `Track.java:80-88` exposes one complete audio-metadata mutation rather than independent partial setters.
- Existing storage transaction coordination covers promotion cleanup and rollback paths referenced by upload/replacement.

### Thumbnail Canonicalization Without Backfill

- `CanonicalImageService.java:48-65` validates size, signature/MIME agreement, APNG exclusion, and canonical JPEG output.
- `CanonicalImageService.java:128-165` validates decoded dimensions and the required square shape.
- `CanonicalImageService.java:175-226` applies bounded downscaling without upscaling and re-encodes canonical output.
- `TrackService.java:304-309` invokes processing only when a new thumbnail is supplied; no bulk or destructive legacy-image rewrite was introduced.

### PlayableTrack Query And Policy Preservation

- `PlayableTrackBatchRequest.java:10-14` limits batch size to 100 positive, non-null IDs.
- `PlayableTrackService.java:29-56` preserves first-request order after de-duplication, queries active tracks in one batch, loads tags in one batch, and omits missing/inactive entries.
- `TrackRepository.java:27-29` applies active availability and creator loading; `TrackTagRepository.java:22-23` performs one tag query for the batch.
- `PlayableTrackQueryCountTest.java:56-107` asserts fixed batch query count, order, and active filtering; aggregate query-count cases at `PlayableTrackQueryCountTest.java:109-236` guard against per-row fan-out.
- `SecurityConfig.java:73-78` retains public list/batch/stream access while licensed download remains authenticated.
- `DownloadService.java:43-95` retains active-track, authentication, subscription/license, and quota enforcement. No reviewed media/tag change broadened playback or download policy.

## 7. Residual Risks

These items were not promoted to findings because the available evidence does not establish a defect:

- Production MySQL duplicate races may differ by collation, driver exception shape, or constraint metadata. Existing tests use provider-shaped failures rather than a concurrent production-engine race.
- Unusual MP3/VBR/container variants and the complete existing media corpus were not analyzed during this review. A bounded dry-run should precede any backfill decision.
- Full audio decoding consumes work proportional to duration. Production telemetry is needed to establish acceptable limits for long media and admin dry-run pages.
- Storage rollback tests do not simulate process termination at every transaction synchronization boundary; crash recovery remains dependent on the existing storage reconciliation design.
- Query-count assertions run under the test database and prove fixed call count, not production MySQL plans, row inflation, or maximum-batch memory behavior.
- Existing noncanonical tag names and noncanonical thumbnails remain untouched intentionally. Compatibility is not equivalent to migration completeness.
- Waveform-bearing collection responses can still be large within otherwise valid collections. Album/playlist collection-size policy and production payload measurements were not sufficient to confirm a separate defect.
- The prior full gate recorded 13 skipped tests; their skip reasons were outside this focused review.

## 8. Rollback

This WI changed documentation only. Rollback consists of removing or reverting:

- `deliverables/user/WI-20260809-ATS-006-summary.md`
- `deliverables/agent/WI-20260809-ATS-006-evidence-pack.md`

No application code, test code, schema, data, configuration, dependency, commit, or remote branch requires rollback.

## 9. WI-029 Block Status

`WI-20260808-ATS-029` is **BLOCKED** by this review result.

Minimum unblock evidence:

1. Resolve MAJOR-001 with bounded service-level pagination and stable invalid-input behavior.
2. Resolve MAJOR-002 with one consistent active-membership contract across playlist count, detail, and reorder.
3. Add and pass the focused regression tests specified by both MAJOR findings.
4. Re-run the focused media/tag/PlayableTrack tests and the downstream integration gate.

MINOR-001 should be corrected or explicitly accepted with an owner and follow-up before release closure.
