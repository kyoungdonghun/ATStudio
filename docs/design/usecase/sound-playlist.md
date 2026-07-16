# Sound -- Playlist Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 3 (Playlist)
> **DB Reference**: `docs/design/db-schema.md` Section 5 (`playlists`, `playlist_tracks`)

---

## SOUND-002: Create Playlist

| Field | Value |
|-------|-------|
| **Code** | SOUND-002 |
| **Version** | 26-02-20 |
| **Description** | Member creates a new playlist. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Has a service-enabled subscription (`user_subscriptions.status=ACTIVE`, or `status=CANCELLED` with `expiresAt` still in the future). |
| **Trigger** | User clicks the 'Create Playlist' button. |
| **Related UC** | SOUND-019 (add track), SOUND-008 (view detail) |

**Main Flow**
1. Direct navigation to `/playlists/new` replaces the URL with `/playlists` and opens the existing creation modal; it does not render a second creation page.
2. User enters the playlist title (title, required) and description (optional).
3. Attaches a thumbnail image (optional).
4. Frontend performs validation.
5. Frontend sends data as multipart/form-data to the backend.
6. Backend performs validation.
7. Backend creates a playlists record (is_active=1) and returns a 201 response.

**Exception / Alternative Flow**
- Active playlist count already at the current plan's `subscriptions.max_playlists`: 409 `PLAYLIST_LIMIT_EXCEEDED`. The backend locks the owning user row before checking the active count and creating the playlist, so concurrent creates cannot exceed the plan. Frontend pre-empts this by hiding the 'Create Playlist' button when the active playlist count reaches the subscribed tier limit (client-side guard before API call).

**Postconditions**
- playlists record created. No tracks yet (empty playlist).

---

## SOUND-007: List Playlists

| Field | Value |
|-------|-------|
| **Code** | SOUND-007 |
| **Version** | 26-02-20 |
| **Description** | Logged-in user views their own playlist list. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Has a service-enabled subscription (`ACTIVE`, or `CANCELLED` within grace period). |
| **Trigger** | User navigates to the playlist list screen. |
| **Related UC** | SOUND-008 (view detail) |

**Main Flow**
1. Frontend sends a list request including auth token to the backend.
2. Backend returns the user's playlist list (id, title, thumbnail, track count).

**Postconditions**
- User's playlist list displayed on screen.

---

## SOUND-008: View Playlist Detail

| Field | Value |
|-------|-------|
| **Code** | SOUND-008 |
| **Version** | 26-02-20 |
| **Description** | Logged-in user views playlist detail (included tracks + track order). |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Has a service-enabled subscription (`ACTIVE`, or `CANCELLED` within grace period). Owns the playlist. |
| **Trigger** | User clicks a playlist. |
| **Related UC** | SOUND-013 (update), SOUND-020 (remove track) |

**Main Flow**
1. Frontend sends a detail request including playlistId to the backend.
2. Backend returns the playlist information and included track list (with track order).

**Exception / Alternative Flow**
- Accessing another user's playlist: 403 response.

**Postconditions**
- Playlist detail and included track list (sorted by trackOrder) displayed on screen.

---

## SOUND-019: Add Track to Playlist [New]

| Field | Value |
|-------|-------|
| **Code** | SOUND-019 |
| **Version** | 26-02-20 |
| **Description** | User adds a track to an existing playlist. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Has a service-enabled subscription (`ACTIVE`, or `CANCELLED` within grace period). Must own the playlist. |
| **Trigger** | User clicks the 'Add to Playlist' button on a track. |
| **Related UC** | SOUND-008 (view detail), SOUND-020 (remove track) |

**Main Flow**
1. User clicks the 'Add to Playlist' button on the track list or detail screen.
2. Selects the target playlist.
3. Frontend sends playlistId and trackId to the backend.
4. Backend verifies the playlist belongs to the user.
5. Backend locks the playlist row, creates a record in playlist_tracks (trackOrder = current last order + 1), and returns a 201 response.

The modal treats close/reopen as a new lifecycle generation. Stale list/add responses and delayed success-close timers cannot affect a later modal session, while parent rerenders alone do not restart the playlist query.

**Exception / Alternative Flow**
- Track already in playlist: 409 response.

**Postconditions**
- Record added to playlist_tracks. Playlist track count increased.

---

## SOUND-013: Update Playlist

| Field | Value |
|-------|-------|
| **Code** | SOUND-013 |
| **Version** | 26-02-20 |
| **Description** | User updates playlist metadata (title/description/thumbnail) or changes track order. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Has a service-enabled subscription (`ACTIVE`, or `CANCELLED` within grace period). Owns the playlist. |
| **Trigger** | User clicks the playlist 'Edit' button. |
| **Related UC** | SOUND-008 (view detail) |

**Main Flow A -- Metadata Update**
1. User modifies title, description, thumbnail.
2. Frontend sends playlistId and changed data as multipart/form-data to the backend.
3. Backend updates the playlists record and returns a 200 response.

**Main Flow B -- Track Order Change**
1. User reorders tracks via drag-and-drop.
2. Frontend sends [{trackId, trackOrder}, ...] array to the backend.
3. Backend locks the playlist row, validates that the payload contains every current member exactly once with unique contiguous orders from 0 through n-1, batch-updates track_order in playlist_tracks, and returns a 200 response.

**Postconditions**
- Updated playlist information reflected in DB.

---

## SOUND-020: Remove Track from Playlist [New]

| Field | Value |
|-------|-------|
| **Code** | SOUND-020 |
| **Version** | 26-02-20 |
| **Description** | User removes a specific track from a playlist. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Has a service-enabled subscription (`ACTIVE`, or `CANCELLED` within grace period). Owns the playlist. The track must be in the playlist. |
| **Trigger** | User clicks the 'Remove' button on a track in the playlist detail screen. |
| **Related UC** | SOUND-008 (view detail) |

**Main Flow**
1. User clicks the 'Remove' button on a specific track in the playlist detail.
2. Frontend sends a remove request including playlistId and trackId to the backend.
3. Backend verifies the playlist belongs to the user.
4. Backend locks the playlist row, deletes the record from playlist_tracks, compacts remaining orders, and returns 204 No Content.

**Postconditions**
- Record deleted from playlist_tracks. Playlist track count decreased.

---

## SOUND-017: Delete Playlist

| Field | Value |
|-------|-------|
| **Code** | SOUND-017 |
| **Version** | 26-02-20 |
| **Description** | User deletes their own playlist. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Has a service-enabled subscription (`ACTIVE`, or `CANCELLED` within grace period). Owns the playlist. |
| **Trigger** | User clicks the playlist 'Delete' button. |
| **Related UC** | - |

**Main Flow**
1. User clicks the playlist 'Delete' button.
2. Frontend displays a deletion confirmation dialog.
3. Upon confirmation, frontend sends a delete request including playlistId to the backend.
4. Backend verifies the playlist belongs to the user.
5. Backend locks the playlist row, physically deletes its `playlist_tracks`
   membership rows, soft-deletes the parent through `playlist.deactivate()`, and
   returns 204 No Content.

**Postconditions**
- The parent `playlists` row remains as an inactive soft-deleted record, while
  associated `playlist_tracks` rows are physically deleted.
