---
version: 2.2
last_updated: 2026-08-13
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
