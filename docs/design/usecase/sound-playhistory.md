---
version: 2.0
last_updated: 2026-07-16
project: ATS
owner: docops
category: design
status: stable
dependencies:
  - path: ../api-spec.md
    reason: Server compatibility API contract
  - path: ../db-schema.md
    reason: Retained play_histories table contract
  - path: ../../../frontend/src/store/playerStore.ts
    reason: Active SPA play-history source of truth
  - path: ../../../frontend/src/pages/subscriber/PlayHistoryPage.tsx
    reason: Active screen behavior
---

# Sound - Play History Use Cases

## Current Boundary

The active React SPA uses browser `localStorage`, not the server API, for the play-history screen.

- Storage key: `playHistory`.
- Maximum: 100 tracks.
- A track is recorded only after playback starts successfully.
- Replaying a track moves the de-duplicated item to the newest position.
- Login is not required for recording; the `/play-history` route itself is authenticated.
- Data is browser/profile/device local and may be lost when browser storage is cleared.
- There is no synchronization with `play_histories` or `/api/play-histories`.

The backend controller, entity, and table remain compatibility surfaces for legacy callers. They must not be described as the active SPA screen source until a separately approved synchronization design exists.

## SOUND-004: Record Browser-Local Play History

| Field | Value |
|---|---|
| Code | SOUND-004 |
| Actor | Listener, React player store |
| Trigger | Track playback starts successfully |
| Source of truth | Browser `localStorage` |

Flow:

1. The player starts the selected track.
2. The store removes an older entry for the same track ID.
3. The store prepends the new entry and caps the list at 100.
4. The store persists the list under `playHistory`.

No server play-history request is sent by the active SPA.

## SOUND-009: View Browser-Local Play History

The authenticated `/play-history` screen reads the current browser-local list in newest-first order. Empty, malformed, or unavailable storage falls back safely to an empty list.

## SOUND-015: Delete Browser-Local Play History

The screen removes one local item or clears the local list through the player store. This does not delete retained server-side `play_histories` rows.

## Retained Server Compatibility API

| Endpoint | Retained behavior | Active SPA use |
|---|---|---|
| `POST /api/play-histories` | Creates a server row and increments `tracks.play_count` | None |
| `GET /api/play-histories` | Returns authenticated user's server history | None |
| `DELETE /api/play-histories` | Deletes selected/all server rows | None |

Removal or synchronization requires a separate approved requirement covering migration, privacy, count semantics, and client compatibility.
