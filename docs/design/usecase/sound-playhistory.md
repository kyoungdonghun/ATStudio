---
version: 3.0
last_updated: 2026-07-17
project: ATS
owner: docops
category: design
status: stable
dependencies:
  - path: ../../../frontend/src/store/playerStore.ts
    reason: Browser-local Play History source
  - path: ../../../frontend/src/pages/subscriber/PlayHistoryPage.tsx
    reason: Active screen behavior
---

# Sound - Browser-Local Play History

## Current Boundary

Play History is a React SPA and browser-storage feature.

- Storage key: `playHistory`.
- Maximum: 100 de-duplicated Tracks.
- Recording starts only after playback starts successfully.
- Replaying a Track moves it to the newest position.
- Recording itself does not require login; the `/play-history` screen is
  authenticated.
- Empty, malformed, unavailable, or cleared storage falls back to an empty
  list.
- There is no server API, entity, repository, service, or database table for
  Play History.

## SOUND-004: Record Play History

1. The player starts the selected Track.
2. The store removes an older entry for the same Track ID.
3. The store prepends the current entry and caps the list at 100.
4. The store persists the list in browser storage.

## SOUND-009: View Play History

The authenticated `/play-history` page reads the local list in newest-first
order.

## SOUND-015: Delete Play History

The page removes one local entry or clears the local list through the player
store. No backend request is sent.
