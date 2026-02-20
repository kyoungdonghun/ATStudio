# Sound -- Play History Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 4 (Play History)
> **DB Reference**: `docs/design/db-schema.md` Section 6.2 (`play_histories`)
>
> **Original UC codes**: SOUND-004 (save play history), SOUND-009 (view), SOUND-015 (delete)
> Originally labeled as "playlog" in the source; corrected to "play history (play_histories)" per the API spec.

---

## SOUND-004: Save Play History

| Field | Value |
|-------|-------|
| **Code** | SOUND-004 |
| **Version** | 26-02-20 |
| **Description** | When track playback starts in QueBar, the frontend explicitly calls this endpoint to save play history and increment play_count. Members only. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. |
| **Trigger** | After SOUND-010 (play track), frontend detects QueBar playback start and calls automatically. |
| **Related UC** | SOUND-010 (play track), SOUND-009 (view play history) |

**Main Flow**
1. Frontend detects that playback has started in QueBar.
2. Frontend calls `POST /api/play-histories` with auth token and trackId.
3. Backend creates a (user_id, track_id, played_at) record in the play_histories table.
4. Backend increments tracks.play_count by 1.
5. Backend returns a 201 response.

**Exception / Alternative Flow**
- Non-member: Frontend does not call this endpoint. No play history recorded, no play_count increment.

**Postconditions**
- Record added to play_histories. tracks.play_count incremented.

---

## SOUND-009: View Play History

| Field | Value |
|-------|-------|
| **Code** | SOUND-009 |
| **Version** | 26-02-20 |
| **Description** | Logged-in user views their own play history list in reverse chronological order. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. |
| **Trigger** | User navigates to the play history screen. |
| **Related UC** | SOUND-015 (delete play history) |

**Main Flow**
1. Frontend sends a request including auth token and page parameters to the backend.
2. Backend returns the user's play_histories paginated in reverse chronological order.

**Postconditions**
- Play history (track info, played_at) displayed in reverse chronological order on screen.

---

## SOUND-015: Delete Play History

| Field | Value |
|-------|-------|
| **Code** | SOUND-015 |
| **Version** | 26-02-20 |
| **Description** | Logged-in user selectively deletes or bulk-deletes their own play history. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. |
| **Trigger** | User clicks the 'Delete' button on the play history screen. |
| **Related UC** | SOUND-009 (view play history) |

**Main Flow A -- Selective Delete**
1. User selects records to delete via checkboxes.
2. Clicks the 'Delete' button.
3. Frontend sends a delete request including historyIds array to the backend.
4. Backend deletes the corresponding play_histories records and returns 204 No Content.

**Main Flow B -- Delete All**
1. User clicks the 'Delete All' button.
2. Frontend sends a delete request with historyIds=[] to the backend.
3. Backend deletes all play_histories for the user and returns 204 No Content.

**Postconditions**
- Selected or all play_histories records deleted.
