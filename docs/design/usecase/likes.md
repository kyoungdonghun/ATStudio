# User — Likes Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 10 (Likes)
> **DB Reference**: `docs/design/db-schema.md` Section 7 (`likes`)

---

## LIKE-001: Add to Likes [New]

| Field | Value |
|-------|-------|
| **Code** | LIKE-001 |
| **Version** | 26-02-20 |
| **Description** | A logged-in member adds a specific track to their likes. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Target track exists in DB (is_active=1). |
| **Trigger** | User clicks the 'Like' button on the track list or detail screen. |
| **Related UC** | LIKE-002 (list), LIKE-003 (remove) |

**Main Flow**
1. Frontend sends a request with trackId to the backend.
2. Backend verifies the track exists.
3. Backend checks whether it has already been liked (composite PK duplicate check on likes).
4. Backend creates a likes record and returns 201 Created.

**Exception / Alternative Flow**
- Track already in likes: 409 Conflict.

**Postconditions**
- A (user_id, track_id) record is created in the likes table.

---

## LIKE-002: List Likes [New]

| Field | Value |
|-------|-------|
| **Code** | LIKE-002 |
| **Version** | 26-02-20 |
| **Description** | A logged-in member views their likes list. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. |
| **Trigger** | User accesses the 'Likes' screen. |
| **Related UC** | LIKE-001 (add), LIKE-003 (remove) |

**Main Flow**
1. Frontend sends a request with the auth token to the backend.
2. Backend extracts userId from the JWT and queries the user's likes list.
3. Backend returns the liked track list (trackId, title, bpm, tonality, thumbnail, createdAt).

**Postconditions**
- Liked track list displayed on screen.

---

## LIKE-003: Remove from Likes [New]

| Field | Value |
|-------|-------|
| **Code** | LIKE-003 |
| **Version** | 26-02-20 |
| **Description** | A logged-in member removes a track from their likes. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Target track exists in the likes table. |
| **Trigger** | User clicks the 'Remove from Likes' button on the likes list or track detail screen. |
| **Related UC** | LIKE-001 (add) |

**Main Flow**
1. Frontend sends a delete request with trackId to the backend.
2. Backend verifies the (user_id, track_id) record exists.
3. Backend deletes the likes record and returns 204 No Content.

**Exception / Alternative Flow**
- Track not in likes: 404 response.

**Postconditions**
- The (user_id, track_id) record is deleted from the likes table.

---

## LIKE-004: Add Album to Likes [New]

| Field | Value |
|-------|-------|
| **Code** | LIKE-004 |
| **Version** | 26-03-29 |
| **Description** | A logged-in member adds a specific album to their likes. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Target album exists in DB (is_active=true). |
| **Trigger** | User clicks the 'Like' button on the album list or detail screen. |
| **Related UC** | LIKE-005 (list album likes), LIKE-006 (remove album like) |

**Main Flow**
1. Frontend sends `POST /api/likes/albums/{albumId}` with auth token to the backend.
2. Backend verifies the album exists and is active.
3. Backend checks whether the album has already been liked (composite PK duplicate check on album_likes).
4. Backend creates an album_likes record, increments `albums.likeCount`, and returns 201 Created.

**Exception / Alternative Flow**
- Album already in likes: 409 Conflict (`DATA_INTEGRITY_VIOLATION`).
- Album not found or inactive: 404 response.

**Postconditions**
- A (user_id, album_id) record is created in the album_likes table.
- `albums.likeCount` incremented by 1.

---

## LIKE-005: List Album Likes [New]

| Field | Value |
|-------|-------|
| **Code** | LIKE-005 |
| **Version** | 26-03-29 |
| **Description** | A logged-in member views their liked albums list. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. |
| **Trigger** | User accesses the 'Liked Albums' screen. |
| **Related UC** | LIKE-004 (add album like), LIKE-006 (remove album like) |

**Main Flow**
1. Frontend sends `GET /api/likes/albums` with auth token to the backend.
2. Backend extracts userId from the JWT and queries the user's album_likes records.
3. Backend returns the liked album list as a `dataList`.

**Response Fields (AlbumLikeResponse)**
| Field | Type | Description |
|-------|------|-------------|
| albumId | Long | Album ID |
| title | String | Album title |
| description | String | Album description |
| thumbnailUrl | String | Thumbnail file path |
| trackCount | Integer | Number of tracks in the album |
| likeCount | Long | Total like count for the album |
| createdAt | LocalDateTime | Timestamp when the user liked the album |

**Postconditions**
- Liked album list displayed on screen.

---

## LIKE-006: Remove Album from Likes [New]

| Field | Value |
|-------|-------|
| **Code** | LIKE-006 |
| **Version** | 26-03-29 |
| **Description** | A logged-in member removes an album from their likes. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Target album exists in the album_likes table for this user. |
| **Trigger** | User clicks the 'Remove from Likes' button on the liked albums list or album detail screen. |
| **Related UC** | LIKE-004 (add album like) |

**Main Flow**
1. Frontend sends `DELETE /api/likes/albums/{albumId}` with auth token to the backend.
2. Backend verifies the (user_id, album_id) record exists in album_likes.
3. Backend deletes the album_likes record, decrements `albums.likeCount`, and returns 204 No Content.

**Exception / Alternative Flow**
- Album not in likes: 404 response (`RESOURCE_NOT_FOUND`).

**Postconditions**
- The (user_id, album_id) record is deleted from the album_likes table.
- `albums.likeCount` decremented by 1 (floor 0).
