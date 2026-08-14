---
version: 2.3
last_updated: 2026-08-14
project: ATS
owner: docops
category: design
status: stable
dependencies:
  - path: ../api-spec.md
    reason: Official Download and Download History API contract
  - path: ../db-schema.md
    reason: Track download and License persistence contract
  - path: ../../../frontend/src/pages/subscriber/DownloadHistoryPage.tsx
    reason: Active subscriber screen
---

# User - Official Download History

> The file path is retained to avoid breaking existing document links. Its
> current subject is Download History, not a queue.

## DOWNLOAD-001: View Download History

- Route: `/downloads`.
- API: `GET /api/downloads/history`.
- Actor: subscriber.
- Result: paginated completed Official Download records in `dataList` plus
  `pageInfo`. Playback maps the returned Track data to the shared
  `PlayableTrack` contract; missing waveform data is hydrated in one bounded
  batch rather than one detail request per row.
- The read is keyed by authenticated owner (user ID and token identity), page,
  search, sort, and role. A key change clears prior rows, page metadata,
  download count, and selection, aborts retired work, and permits only the
  current generation to render or commit data, error, loading, or player
  context. The opaque key is never displayed or logged.
- Single and bulk downloads keep the initiating key and abort signal through
  ID preparation, confirmation, each iteration, blob trigger, feedback, count
  refresh, and cleanup. Owner retirement suppresses all remaining effects.
- Single and bulk starts share a synchronous `{readKey, trackId}` claim
  registry. A Track currently claimed by either path is not requested again;
  distinct Track IDs continue. Exact-owner cleanup releases only its own claim,
  so a stale completion cannot release newer work and a later retry can claim
  the Track. Bulk skips existing work before success/failure accounting; an
  all-skipped run emits no competing result or count refresh.

## DOWNLOAD-002: Read Downloaded Track IDs

- API: `GET /api/downloads/history/track-ids`.
- Actor: subscriber.
- Result: Track IDs already downloaded by the current user.

## Official Download Boundary

`GET /api/tracks/{trackId}/download` remains SOUND-011.

- First download requires current subscription entitlement and plan quota.
- The backend serializes first-download decisions per user.
- A first download issues one License and records `track_downloads`.
- Entitled re-download remains available without consuming first-download
  quota.
- Track download counts update atomically.

There is no temporary queue model, queue route, or queue API.

The shared registry prevents duplicate invocation only. It does not decide a
bulk-download ceiling or whether cancellation/ownership should outlive a route;
those policies remain held outside WI-055.
