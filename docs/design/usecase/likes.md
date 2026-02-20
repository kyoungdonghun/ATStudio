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
