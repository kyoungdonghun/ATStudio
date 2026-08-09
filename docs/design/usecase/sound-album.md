# Sound -- Album Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 15 (Album)
> **DB Reference**: `docs/design/db-schema.md` (`albums`, `album_tracks`)

---

## ALBUM-001: Create Album

| Field | Value |
|-------|-------|
| **Code** | ALBUM-001 |
| **Version** | 26-03-04 |
| **Description** | Admin creates a new curated album. |
| **Actor** | Admin, Backend |
| **Preconditions** | Logged in. Admin role. |
| **Trigger** | Admin clicks the 'Create Album' button. |
| **Related UC** | ALBUM-006 (add track), ALBUM-003 (view detail) |

**Main Flow**
1. Admin enters album title (required) and description (optional).
2. Attaches a thumbnail image (optional).
3. Frontend sends data as multipart/form-data to the backend.
4. Backend performs validation.
5. Backend creates an albums record (is_active=true) and returns a 201 response.

**Exception / Alternative Flow**
- -

**Postconditions**
- albums record created. No tracks yet (empty album).

---

## ALBUM-002: List Albums

| Field | Value |
|-------|-------|
| **Code** | ALBUM-002 |
| **Version** | 26-08-09 |
| **Description** | Any user (including unauthenticated) views the list of active albums. Supports sort parameter. |
| **Actor** | Anyone, Backend |
| **Preconditions** | None. |
| **Trigger** | User navigates to the album list screen. |
| **Related UC** | ALBUM-003 (view detail) |

**Main Flow**
1. Frontend sends a list request with optional `sort` parameter to the backend.
2. Backend retrieves active albums (is_active=true) and applies sort ordering.
3. Backend returns the album list with active Track counts and `likeCount` per
   album. Inactive Track memberships remain persisted but do not contribute to
   the public count.

**Sort Parameter**
| Value | Sort behavior |
|-------|--------------|
| `latest` (default) | `createdAt DESC`, then `id DESC` (DB-level ordering) |
| `trackCount` | Global active Track count DESC, then `createdAt DESC` and `id DESC` before pagination (DB-level aggregate ordering) |

The paged catalog validates positive page/size values and bounds size to 100. It
never loads the full album catalog for in-memory sorting. Administrative
all-membership totals remain separate from this public active-only projection.

**Response Fields (AlbumListItemResponse)**
Includes `likeCount` field (from `albums.like_count`) in addition to id, title, thumbnail, and trackCount.

**Postconditions**
- Active album list displayed on screen.

---

## ALBUM-003: View Album Detail

| Field | Value |
|-------|-------|
| **Code** | ALBUM-003 |
| **Version** | 26-08-09 |
| **Description** | Any user views album detail including the included track list and track order. |
| **Actor** | Anyone, Backend |
| **Preconditions** | None. Album must be active (is_active=true). |
| **Trigger** | User clicks an album. |
| **Related UC** | ALBUM-002 (list) |

**Main Flow**
1. Frontend sends a detail request including albumId to the backend.
2. Backend returns album information and active Track membership rows only,
   sorted by `track_order`. Inactive membership rows remain persisted.

**Exception / Alternative Flow**
- Album not found or inactive: 404 response.

**Postconditions**
- Album detail and included track list (sorted by track_order) displayed on screen.
- Every album Track item used for playback includes duration and is mapped
  through the shared `PlayableTrack` contract. Nullable thumbnail and waveform
  members may be omitted by the API; the mapper normalizes omitted or null
  values to explicit `null` without synthesizing `duration=0`.

---

## ALBUM-004: Update Album

| Field | Value |
|-------|-------|
| **Code** | ALBUM-004 |
| **Version** | 26-03-04 |
| **Description** | Admin updates album metadata (title/description/thumbnail). |
| **Actor** | Admin, Backend |
| **Preconditions** | Logged in. Admin role. Album must exist. |
| **Trigger** | Admin clicks the album 'Edit' button. |
| **Related UC** | ALBUM-003 (view detail) |

**Main Flow**
1. Admin modifies title, description, or thumbnail (all fields optional).
2. Frontend sends albumId and changed data as multipart/form-data to the backend.
3. Backend updates the albums record and returns a 200 response.

**Exception / Alternative Flow**
- Album not found: 404 response.

**Postconditions**
- Updated album information reflected in DB.

---

## ALBUM-005: Delete Album

| Field | Value |
|-------|-------|
| **Code** | ALBUM-005 |
| **Version** | 26-03-04 |
| **Description** | Admin deletes (soft-deletes) an album. Deleted album is no longer visible to users. |
| **Actor** | Admin, Backend |
| **Preconditions** | Logged in. Admin role. Album must exist. |
| **Trigger** | Admin clicks the album 'Delete' button. |
| **Related UC** | - |

**Main Flow**
1. Admin clicks the album 'Delete' button.
2. Frontend displays a deletion confirmation dialog.
3. Upon confirmation, frontend sends a delete request including albumId to the backend.
4. Backend sets is_active=false on the albums record and returns 204 No Content.

**Exception / Alternative Flow**
- Album not found: 404 response.

**Postconditions**
- albums.is_active set to false. Album no longer appears in list/detail responses.
- album_tracks records are retained (data preserved for potential recovery).

---

## ALBUM-006: Add Track to Album

| Field | Value |
|-------|-------|
| **Code** | ALBUM-006 |
| **Version** | 26-03-04 |
| **Description** | Admin adds a track to an existing album. A single track can belong to multiple albums. |
| **Actor** | Admin, Backend |
| **Preconditions** | Logged in. Admin role. Album must exist. Track must exist and be active. |
| **Trigger** | Admin clicks the 'Add Track' button on the album detail screen. |
| **Related UC** | ALBUM-003 (view detail), ALBUM-007 (remove track) |

**Main Flow**
1. Admin selects a track to add from the track list.
2. Frontend sends albumId and trackId to the backend.
3. Backend verifies the album and track exist.
4. Backend locks the album row, then creates a record in album_tracks (track_order = the current 0-based track count) and returns a 200 response with the updated album detail.

**Exception / Alternative Flow**
- Track already in album: 409 RESOURCE_DUPLICATE response.
- Album or track not found: 404 response.

**Postconditions**
- Record added to album_tracks. Album track count increased by 1.

---

## ALBUM-007: Remove Track from Album

| Field | Value |
|-------|-------|
| **Code** | ALBUM-007 |
| **Version** | 26-03-04 |
| **Description** | Admin removes a specific track from an album. Does not delete the track itself. |
| **Actor** | Admin, Backend |
| **Preconditions** | Logged in. Admin role. Album must exist. The track must be in the album. |
| **Trigger** | Admin clicks the 'Remove' button on a track in the album detail screen. |
| **Related UC** | ALBUM-003 (view detail) |

**Main Flow**
1. Admin clicks the 'Remove' button on a specific track in the album detail.
2. Frontend sends a remove request including albumId and trackId to the backend.
3. Backend locks the album row, deletes the album_tracks record, compacts remaining orders, and returns 204 No Content.

**Exception / Alternative Flow**
- Album or track not found: 404 response.

**Postconditions**
- Record deleted from album_tracks. Album track count decreased by 1.

---

## ALBUM-008: Reorder Album Tracks

| Field | Value |
|-------|-------|
| **Code** | ALBUM-008 |
| **Version** | 26-03-04 |
| **Description** | Admin changes the display order of tracks in an album. |
| **Actor** | Admin, Backend |
| **Preconditions** | Logged in. Admin role. Album must exist. |
| **Trigger** | Admin reorders tracks via drag-and-drop on the album detail screen. |
| **Related UC** | ALBUM-003 (view detail) |

**Main Flow**
1. Admin reorders tracks via drag-and-drop.
2. Frontend sends [{trackId, order}, ...] array to the backend.
3. Backend locks the album row, validates that the payload contains every current member exactly once with unique contiguous orders from 0 through n-1, updates track_order for each entry, and returns a 200 response with the updated album detail.

**Exception / Alternative Flow**
- Album not found: 404 response.

**Postconditions**
- track_order updated in album_tracks. Album detail reflects new order.

---

## ALBUM-009: Album Likes [Cross-reference]

| Field | Value |
|-------|-------|
| **Code** | ALBUM-009 |
| **Version** | 26-03-29 |
| **Description** | Logged-in members can like or unlike an album and view their liked albums list. The `albums.likeCount` field reflects the aggregate count. Canonical definitions are in `likes.md`. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. |
| **Trigger** | User clicks the 'Like' / 'Unlike' button on an album. |
| **Related UC** | LIKE-004 (add album like), LIKE-005 (list album likes), LIKE-006 (remove album like) |

**Summary**
| Operation | Endpoint | Response |
|-----------|----------|----------|
| Add like | `POST /api/likes/albums/{albumId}` | 201 Created |
| List liked albums | `GET /api/likes/albums` | 200 OK, dataList of AlbumLikeResponse |
| Remove like | `DELETE /api/likes/albums/{albumId}` | 204 No Content |

**Album Entity: likeCount Field**
- `albums.like_count` (BIGINT, default 0) is incremented on every successful like add and decremented (floor 0) on every like removal.
- `likeCount` is included in all album list and detail responses.

> **Canonical definition**: `docs/design/usecase/likes.md` — LIKE-004, LIKE-005, LIKE-006.
