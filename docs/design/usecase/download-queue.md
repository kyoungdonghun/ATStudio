# User — Download Queue Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 11 (Download Queue)
> **DB Reference**: `docs/design/db-schema.md` Section 8 (`download_queue`)
>
> **Download Queue Concept**: Collects multiple tracks so the frontend can call the individual download API (SOUND-011) sequentially. Since there is no purchase concept, this is defined as a "download queue" rather than a "cart".

---

## DLQ-001: Add to Queue [New]

| Field | Value |
|-------|-------|
| **Code** | DLQ-001 |
| **Version** | 26-02-20 |
| **Description** | A logged-in member adds a specific track to the download queue. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Target track exists in DB (is_active=1). |
| **Trigger** | User clicks the 'Add to Queue' button on the track list or detail screen. |
| **Related UC** | DLQ-002 (view queue), SOUND-011 (download track) |

**Main Flow**
1. Frontend sends a request with trackId to the backend.
2. Backend verifies the track exists.
3. Backend checks whether it has already been added (composite PK duplicate check on download_queue).
4. Backend creates a download_queue record and returns 201 Created.

**Exception / Alternative Flow**
- Track already in the queue: 409 Conflict.

**Postconditions**
- A (user_id, track_id) record is created in the download_queue table.

---

## DLQ-002: View Download Queue [New]

| Field | Value |
|-------|-------|
| **Code** | DLQ-002 |
| **Version** | 26-02-20 |
| **Description** | A logged-in member views their download queue. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. |
| **Trigger** | User accesses the 'Download Queue' screen. |
| **Related UC** | DLQ-001 (add), DLQ-003 (remove), SOUND-011 (download track) |

**Main Flow**
1. Frontend sends a request with the auth token to the backend.
2. Backend extracts userId from the JWT and queries the download_queue list.
3. Backend returns the queued track information (trackId, title, bpm, tonality, thumbnail, createdAt).

**Postconditions**
- Download queue track list displayed on screen.
- User can call SOUND-011 sequentially on all or individual tracks to download them.

> **Frontend Note**: On page exit during download, a `beforeunload` event displays a warning.

---

## DLQ-003: Remove from Queue [New]

| Field | Value |
|-------|-------|
| **Code** | DLQ-003 |
| **Version** | 26-02-20 |
| **Description** | A logged-in member removes a specific track from the download queue. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Target track exists in the queue. |
| **Trigger** | User clicks the 'Remove' button for a specific track in the download queue. |
| **Related UC** | DLQ-001 (add) |

**Main Flow**
1. Frontend sends a delete request with trackId to the backend.
2. Backend verifies the (user_id, track_id) record exists.
3. Backend deletes the download_queue record and returns 204 No Content.

**Exception / Alternative Flow**
- Track not in the queue: 404 response.

**Postconditions**
- The (user_id, track_id) record is deleted from the download_queue table.
