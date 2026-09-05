# Sound -- Playlist Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 3 (Playlist)
> **DB Reference**: `docs/design/db-schema.md` Section 5 (`playlists`, `playlist_tracks`)

---

## SOUND-002: Create Playlist

| Field | Value |
|-------|-------|
| **Code** | SOUND-002 |
| **Version** | 26-08-13 |
| **Description** | Member creates a new playlist. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Has a service-enabled subscription (`user_subscriptions.status=ACTIVE`, or `status=CANCELLED` with `expiresAt` still in the future). |
| **Trigger** | User clicks the 'Create Playlist' button. |
| **Related UC** | SOUND-019 (add track), SOUND-008 (view detail) |

**Main Flow**
1. User opens `/playlists` and selects the visible create button or create card, which opens the creation modal without navigating to a second page.
2. User enters the playlist title (title, required) and description (optional).
3. Attaches a thumbnail image (optional).
4. Frontend performs validation.
5. Frontend sends data as multipart/form-data to the backend.
6. Backend performs validation.
7. Backend creates a playlists record (is_active=1) and returns a 201 response.

**Exception / Alternative Flow**
- Active playlist count already at the current plan's `subscriptions.max_playlists` (current limits: `3/10/10`): 409 `PLAYLIST_LIMIT_EXCEEDED`. The backend locks the owning user row before checking the active count and creating the playlist, so concurrent creates cannot exceed the plan. Frontend pre-empts this by hiding the 'Create Playlist' button when the active playlist count reaches the subscribed tier limit (client-side guard before API call).
- Create actions require current-owner playlist data and a positive server
  `maxPlaylists` value. Capacity loading or failure is fail-closed, exposes a
  bounded retry, and never uses a client fallback. Owner change or either
  unknown read resets the modal; submit revalidates both reads.
- A locally selected thumbnail owns one browser object URL. Replacement,
  removal, modal close, authenticated-owner replacement, and unmount revoke
  that local URL exactly once. Backend thumbnail URLs are never revoked.

**Postconditions**
- playlists record created. No tracks yet (empty playlist).

---

## SOUND-007: List Playlists

| Field | Value |
|-------|-------|
| **Code** | SOUND-007 |
| **Version** | 26-08-09 |
| **Description** | Logged-in user views their own playlist list. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Has a service-enabled subscription (`ACTIVE`, or `CANCELLED` within grace period). |
| **Trigger** | User navigates to the playlist list screen. |
| **Related UC** | SOUND-008 (view detail) |

**Main Flow**
1. Frontend sends a list request including auth token to the backend.
2. Backend returns the user's playlist list (id, title, thumbnail, active Track
   count). Persisted memberships whose Track is inactive are not counted.
3. List, capacity, detail, and drawer reads use authenticated-owner and
   generation keys. Relevant route, tab, drawer-session, or owner changes
   abort retired work; stale data, error, loading, dialogs, controls, and
   player context cannot render or commit.
4. Mutation and playback handlers revalidate the current owner and projection.

**Postconditions**
- User's playlist list displayed on screen.

---

## SOUND-008: View Playlist Detail

| Field | Value |
|-------|-------|
| **Code** | SOUND-008 |
| **Version** | 26-08-09 |
| **Description** | Logged-in user views playlist detail (included tracks + track order). |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Has a service-enabled subscription (`ACTIVE`, or `CANCELLED` within grace period). Owns the playlist. |
| **Trigger** | User clicks a playlist. |
| **Related UC** | SOUND-013 (update), SOUND-020 (remove track) |

**Main Flow**
1. Frontend sends a detail request including playlistId to the backend.
2. Backend returns the playlist information and active Track membership rows
   only (with track order and duration, plus nullable thumbnail and waveform
   members when present). Inactive membership rows remain persisted but hidden.
3. Frontend maps every playable row through the shared `PlayableTrack`
   contract for individual play, list context, and queue operations, normalizing
   omitted or null nullable media members to explicit `null`.
4. `Play all` replaces the active player queue with the displayed playlist Track
   order and starts the first Track. `Add all to queue` appends the displayed
   Tracks without starting playback or replacing the current queue.

`Play all` does not change the `3/10/10` playlist-capacity policy, the default
playlist created only after subscription completion, or the Player's three-state
repeat policy (`off`, `all`, `one`).

**Exception / Alternative Flow**
- Accessing another user's playlist: 403 response.
- Detail and edit accept only a canonical ASCII decimal `playlistId` matching
  `[1-9][0-9]*` and a safe integer. Invalid values render fixed list recovery
  locally and send no detail request; a newer valid route retires the old
  projection before it can render or commit.

**Postconditions**
- Playlist detail and included track list (sorted by trackOrder) displayed on screen.

---

## SOUND-019: Add Track to Playlist [New]

| Field | Value |
|-------|-------|
| **Code** | SOUND-019 |
| **Version** | 26-08-13 |
| **Description** | User adds a track to an existing playlist. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Has a service-enabled subscription (`ACTIVE`, or `CANCELLED` within grace period). Must own the playlist. |
| **Trigger** | User clicks the 'Add to Playlist' button on a track. |
| **Related UC** | SOUND-008 (view detail), SOUND-020 (remove track) |

**Main Flow**
1. User clicks the 'Add to Playlist' button on the track list or detail screen.
2. The open modal immediately displays playlist-list loading.
3. User selects the target playlist.
4. Frontend fences duplicate submits and sends playlistId and trackId to the backend.
5. Backend verifies the playlist belongs to the user.
6. Backend locks the playlist row, creates a record in playlist_tracks (trackOrder = current last order + 1), and returns a 201 response.

The modal treats close/reopen and Track replacement as new lifecycle
generations. Stale list/add responses and delayed success-close timers cannot
affect a later modal session, while parent callback rerenders alone do not
restart the playlist query.

**Exception / Alternative Flow**
- Playlist-list failure shows fixed copy and one explicit manual retry. Retry
  loading remains visible and only the current attempt may commit.
- Subscription-required list or add results invoke the supplied parent outcome
  callback. When no callback exists, the modal remains open with an explicit
  subscription-required result instead of closing silently.
- Track already in playlist: 409 response.

**Postconditions**
- Record added to playlist_tracks. Playlist track count increased.

---

## SOUND-013: Update Playlist

| Field | Value |
|-------|-------|
| **Code** | SOUND-013 |
| **Version** | 26-08-09 |
| **Description** | User updates playlist metadata (title/description/thumbnail) or changes track order. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Has a service-enabled subscription (`ACTIVE`, or `CANCELLED` within grace period). Owns the playlist. |
| **Trigger** | User clicks the playlist 'Edit' button. |
| **Related UC** | SOUND-008 (view detail) |

**Main Flow A -- Metadata Update**
1. User modifies title, description, thumbnail.
2. Frontend sends playlistId and changed data as multipart/form-data to the backend.
3. Backend updates the playlists record and returns a 200 response.

A newly selected thumbnail owns one local browser object URL. Replacement,
selection removal, route/owner replacement, and unmount revoke that URL exactly
once. The existing backend thumbnail URL is display-only and is never revoked.

**Main Flow B -- Track Order Change**
1. User reorders tracks via drag-and-drop.
2. Frontend sends [{trackId, trackOrder}, ...] array to the backend.
3. Backend locks the playlist row and validates that the payload contains every
   visible active Track exactly once with unique contiguous orders from 0
   through n-1.
4. Backend applies the requested active order first, then retains every inactive
   membership in deterministic prior order (`trackOrder`, then `trackId`) after
   the active rows. All persisted rows receive unique contiguous orders.

**Postconditions**
- Updated playlist information is reflected in DB. An inactive Track that is
  reactivated later appears after the actively reordered Tracks.

---

## SOUND-020: Remove Track from Playlist [New]

| Field | Value |
|-------|-------|
| **Code** | SOUND-020 |
| **Version** | 26-08-13 |
| **Description** | User removes a specific track from a playlist. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Has a service-enabled subscription (`ACTIVE`, or `CANCELLED` within grace period). Owns the playlist. The track must be in the playlist. |
| **Trigger** | User clicks the 'Remove' button on a track in the playlist detail screen. |
| **Related UC** | SOUND-008 (view detail) |

**Main Flow**
1. User clicks the 'Remove' button on a specific track in the playlist detail.
2. Frontend displays a target-specific confirmation and sends no request before
   explicit confirmation.
3. Confirmation establishes one pending owner and sends the remove request with
   playlistId and trackId. Duplicate confirmation is ignored.
4. Backend verifies the playlist belongs to the user.
5. Backend locks the playlist row, deletes the requested `playlist_tracks` row,
   compacts all retained active and inactive membership orders without
   duplicates, and returns 204 No Content.
6. Success reloads authoritative Playlist detail. Failure keeps the same
   current target visible with fixed Korean copy and one manual retry action.

**Postconditions**
- Record deleted from playlist_tracks. Playlist track count decreased.

---

## SOUND-017: Delete Playlist

| Field | Value |
|-------|-------|
| **Code** | SOUND-017 |
| **Version** | 26-08-13 |
| **Description** | User deletes their own playlist. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Has a service-enabled subscription (`ACTIVE`, or `CANCELLED` within grace period). Owns the playlist. |
| **Trigger** | User clicks the playlist 'Delete' button. |
| **Related UC** | - |

**Main Flow**
1. User clicks the playlist 'Delete' button.
2. Frontend displays a target-specific deletion confirmation dialog and sends
   no request before confirmation.
3. Confirmation establishes one pending owner and sends one delete request
   including playlistId. Duplicate confirmation is ignored.
4. Backend verifies the playlist belongs to the user.
5. Backend locks the playlist row, physically deletes its `playlist_tracks`
   membership rows, soft-deletes the parent through `playlist.deactivate()`, and
   returns 204 No Content.
6. Success closes the retired detail and reloads the authoritative Playlist
   list. Failure retains fixed Korean copy and a retry for the same current
   target. Owner, detail, tab, drawer-session, or close retirement suppresses
   stale completion.

**Postconditions**
- The parent `playlists` row remains as an inactive soft-deleted record, while
  associated `playlist_tracks` rows are physically deleted.
