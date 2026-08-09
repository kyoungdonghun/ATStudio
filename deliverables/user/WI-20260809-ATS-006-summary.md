# WI-20260809-ATS-006 Independent Backend Review Summary

## Review Status

- **Status:** Complete - changes required
- **Reviewer role:** `cr` (independent code review)
- **Review date:** 2026-08-09
- **Scope:** Backend media and tag symbols named by the authoritative handoff, their affected aggregate paths, and focused tests
- **Out of scope:** Frontend, schema changes, data changes, live services, external calls, and unrelated backend code
- **Product changes:** None

## Decision

The review confirmed **0 BLOCKER, 2 MAJOR, and 1 MINOR** findings. The approved tag, media-analysis, thumbnail, PlayableTrack batch, and playback/download-policy controls were otherwise supported by the reviewed implementation and focused tests.

`WI-20260808-ATS-029` remains **BLOCKED**. The two MAJOR findings must be fixed and covered by focused regression tests before the integration gate proceeds.

## Confirmed Findings

### MAJOR-001 - Public track search accepts unbounded or invalid page sizes

- **File:line:** `TrackController.java:49-52`; `TrackService.java:100-118`; `RequestDTO.java:15-19`
- **Impact:** An unauthenticated caller can request an extremely large page containing track metadata, tags, and waveform payloads, increasing database, heap, serialization, and response costs. Zero or negative page values reach `PageRequest.of(...)` and can escape as an implementation exception instead of a stable client-facing domain error.
- **Reasoning:** The public model-bound request has no page or size bounds, and the service passes the supplied values directly into pagination. Search field/type whitelisting does not limit result cardinality.
- **Missing test:** Public search requests with `page <= 0`, `size <= 0`, `size > 100`, and a very large integer; the test should assert a stable 4xx domain response and no repository invocation.
- **Recommended fix:** Enforce service-boundary pagination limits, for example `page >= 1` and `1 <= size <= 100`, and translate violations to the existing stable invalid-argument domain response. Use the validated effective values in `pageInfo`.

### MAJOR-002 - Playlist detail hides inactive tracks while count and reorder require them

- **File:line:** `PlaylistService.java:79-114`; `PlaylistService.java:218-232`; `PlaylistService.java:324-348`; `PlaylistTrackRepository.java:18-30`; `TrackService.java:194-196`
- **Impact:** After a playlist member becomes inactive, the user-visible detail omits it, the list count still includes it, and a reorder built from the visible IDs is rejected because validation requires the hidden membership. A normal public/owner workflow can therefore become internally inconsistent and unreorderable.
- **Reasoning:** Detail retrieval applies the active-track predicate, while count and reorder load all persisted playlist memberships. Track deactivation does not remove those memberships, so the states can coexist without data corruption elsewhere.
- **Missing test:** A playlist containing one active and one inactive track, covering list count, detail IDs, and owner reorder behavior after deactivation.
- **Recommended fix:** Define one playable-membership contract for user-facing playlist operations. Count only active tracks and make reorder operate on the same active set, while preserving hidden memberships separately if republishing must restore them.

### MINOR-001 - Public album count and track-count sorting include inactive tracks

- **File:line:** `AlbumService.java:76-107`; `AlbumTrackRepository.java:30-44`; `AlbumRepository.java:22-28`; `TrackService.java:194-196`
- **Impact:** Album cards can report and sort by a track count that differs from the playable tracks returned by album detail. This can produce misleading counts and ordering without exposing the inactive track itself.
- **Reasoning:** Public album detail uses active-only membership, but aggregate counts and the track-count sort expression include every persisted album membership.
- **Missing test:** Mixed active/inactive album memberships covering public count, detail, and `trackCount` sorting.
- **Recommended fix:** Apply the active-track predicate to public counts and public track-count sorting. Keep a separate administrative total only if an explicit administrative contract requires it.

## No Additional Confirmed Findings

- Tag names are normalized and stored without a display-only `#`; create/update duplicate races are translated only for the named tag uniqueness constraint.
- Track search and available-tag filtering use the approved tag fields/types, preserve AND semantics, and retain the documented `dataList`/`pageInfo` response shape.
- Upload and audio replacement use one real decode/traversal result for duration and waveform, and persist the related audio metadata together within the existing transaction/storage coordination.
- Thumbnail input is validated and canonicalized only when a new thumbnail is supplied; no destructive backfill was introduced.
- PlayableTrack batching preserves input order after de-duplication, omits unavailable tracks, applies active visibility, and uses fixed batch queries instead of per-row tag lookups.
- Existing stream and licensed download authorization/availability policy was not broadened by the reviewed changes.

## Residual Risks

- Duplicate-race translation is covered with provider-shaped tests, but not by a concurrent test against the production MySQL collation and constraint metadata.
- Audio analysis has not been exercised here against the complete existing media corpus, unusual MP3/VBR variants, or process interruption at every storage transaction callback.
- Full-file audio decoding remains proportional to media duration. The admin dry-run page and waveform-heavy collection responses should be observed with production-sized payloads before any backfill or broad rollout.
- Query-count tests demonstrate fixed query counts under the test database, but do not prove production MySQL execution plans or memory behavior for maximum-size batches.
- Existing noncanonical legacy tags and thumbnails remain untouched by design. Their compatibility should be checked by dry-run/reporting rather than destructive migration.

## Verification And Rollback

No new test command was run during this independent review. The review inspected the focused tests and the prior `WI-20260808-ATS-027` gate evidence, which records a passing backend build with 1,357 tests and 13 skipped tests. The findings above are static contract gaps not covered by that evidence.

Rollback is documentation-only: remove or revert this summary and the corresponding evidence pack. No product code, tests, schema, data, configuration, or Git history was changed.
