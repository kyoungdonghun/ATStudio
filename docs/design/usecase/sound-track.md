# Sound -- Track Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 1 (Track)
> **DB Reference**: `docs/design/db-schema.md` Section 4.1 (`tracks`, `track_tags`, `track_downloads`)

---

## SOUND-001: Create Track

| Field | Value |
|-------|-------|
| **Code** | SOUND-001 |
| **Version** | 26-06-02 |
| **Description** | Admin registers a new track. After upload, a low-quality preview_file is generated asynchronously. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. At least one tag exists in the tags DB. |
| **Trigger** | Admin clicks the 'Create' button on the track registration page. |
| **Related UC** | SOUND-006 (view track detail), SOUND-012 (update track), SOUND-010 (play track) |

**Main Flow**
1. Admin enters metadata (title, BPM, key, description) and selects tags, including optional visible `USAGE` guide tags.
2. Admin attaches the audio file (audioFile) and thumbnail (optional).
3. Frontend performs client-side validation.
4. Frontend sends metadata and files to the backend as multipart/form-data.
5. Backend performs authorization and server-side validation.
6. Backend saves files to file storage and obtains the paths.
7. Backend creates the track record (is_active=0, play_count=0) and track_tags in the DB.
8. Backend enqueues async low-quality preview_file generation.
9. Backend returns a success response (201 Created).

**Exception / Alternative Flow**
- Async preview_file generation failure: preview_file remains NULL. On streaming request, falls back to audio_file.

**Postconditions**
- Track record (is_active=0) created in DB.
- audioFile saved in file storage. preview_file async generation pending (NULL).
- track_tags linked. Admin must separately set is_active=1 to expose the track to users.

---

## SOUND-005: List Tracks

| Field | Value |
|-------|-------|
| **Code** | SOUND-005 |
| **Version** | 26-06-02 |
| **Description** | User (including non-members) views the track list. Only tracks with is_active=1 are returned. |
| **Actor** | User (including non-members), Backend |
| **Preconditions** | - |
| **Trigger** | User navigates to the track list screen or enters search criteria. |
| **Related UC** | SOUND-010 (play track), SOUND-011 (download track), SOUND-006 (view track detail) |

**Main Flow**
1. User selects/enters search criteria (keyword, genre tag, mood tag, usage guide tag, BPM range, key, sort order). All optional.
2. Frontend sends criteria as query parameters to the backend.
3. Backend searches `keyword` against the track title and associated `USAGE` guide tag names only.
4. Backend returns a paginated list of tracks matching the criteria where is_active=1.
5. Frontend displays the track list on screen and renders `USAGE` tags as a visible hashtag subline below the track title.

**Sort Parameter**
| Value | Sort behavior |
|-------|--------------|
| `latest` (default) | `createdAt DESC` |
| `popular` | `playCount DESC` |
| `likes` | `likeCount DESC` |
| `downloads` | `downloadCount DESC` |

**Response Fields (TrackListItemResponse)**
| Field | Type | Description |
|-------|------|-------------|
| id | Long | Track ID |
| title | String | Track title |
| artistName | String | Uploader nickname |
| duration | Integer | Duration in seconds |
| bpm | Integer | BPM value |
| tonality | String | Key/tonality |
| thumbnail | String | Thumbnail file path |
| playCount | Long | Total play count |
| likeCount | Long | Total like count |
| downloadCount | Long | Total download count |
| tags | List\<TagResponse\> | Associated tags. `USAGE` tags are displayed as visible guide hashtags such as `#쇼츠용` below the track title in list/player contexts. |
| createdAt | LocalDateTime | Track creation timestamp |

**Exception / Alternative Flow**
- No search results: returns an empty content array.

**Postconditions**
- Track list matching criteria (title, tag names, BPM, key, thumbnail, playCount, likeCount, downloadCount, tags) and pageInfo displayed on screen.

---

## SOUND-006: View Track Detail

| Field | Value |
|-------|-------|
| **Code** | SOUND-006 |
| **Version** | 26-06-02 |
| **Description** | User (including non-members) views detailed track information. |
| **Actor** | User (including non-members), Backend |
| **Preconditions** | Target track exists in DB with is_active=1. |
| **Trigger** | User clicks a track or navigates to the track detail URL. |
| **Related UC** | SOUND-010 (play track), SOUND-011 (download track) |

**Main Flow**
1. Frontend sends a detail request including trackId to the backend.
2. Backend retrieves the track record and associated tags, then returns them.
3. Frontend displays the track detail on screen, including visible `USAGE` guide hashtags if linked to the track.

**Exception / Alternative Flow**
- Track not found or is_active=0: 404 response.

**Postconditions**
- Track metadata (title, BPM, key, description, tags, visible usage guide hashtags, playCount, audioFile path, etc.) displayed on screen.

---

## SOUND-010: Play Track

| Field | Value |
|-------|-------|
| **Code** | SOUND-010 |
| **Version** | 26-02-20 |
| **Description** | User (including non-members) streams a track. preview_file (low-quality) is served first; falls back to audio_file if unavailable. Play history recording is handled by the frontend calling SOUND-004 separately. |
| **Actor** | User (including non-members), Backend |
| **Preconditions** | Track exists in DB with is_active=1. audio_file exists in file storage. |
| **Trigger** | User clicks the 'Play' button on a track. |
| **Related UC** | SOUND-005 (list tracks) |

**Main Flow**
1. User clicks the 'Play' button.
2. Frontend sends a streaming request including trackId to the backend.
3. Backend checks whether tracks.preview_file exists.
4. If preview_file exists, streams the low-quality file. If NULL, streams audio_file (fallback).
5. Streaming playback starts in the frontend QueBar.
6. If member: frontend simultaneously calls SOUND-004 (save play history) when QueBar playback starts.

**Exception / Alternative Flow**
- preview_file=NULL: automatic fallback to audio_file. No functional impact.
- File storage error: 503 response.

**Postconditions**
- Track is streaming. Play history and play_count updates are handled in SOUND-004.

---

## SOUND-011: Download Track

| Field | Value |
|-------|-------|
| **Code** | SOUND-011 |
| **Version** | 26-02-20 |
| **Description** | A subscribed member downloads the original track file (audio_file). Download record is saved and license is automatically issued. |
| **Actor** | User (Member, subscriber), Backend |
| **Preconditions** | Logged in. user_subscriptions.status = ACTIVE. Today's download count < plan download_per_day. |
| **Trigger** | User clicks the 'Download' button on a track. |
| **Related UC** | UTIL-006 (check download count), INFO-009 (my license list) |

**Main Flow**
1. User clicks the 'Download' button.
2. Frontend sends a download request including auth token and trackId to the backend.
3. Backend checks subscription status from user_subscriptions.
4. Backend calculates today's download count via COUNT query on track_downloads (DATE(downloaded_at) = CURDATE()) and compares against the plan limit.
5. Backend retrieves the audio_file (original) from file storage.
6. Backend saves a download record in track_downloads.
7. Backend checks for an existing license (user_id, track_id) in the licenses table. If none exists, issues a new UUID-based license. If one exists, skips duplicate issuance.
8. Backend returns the file (Content-Disposition: attachment).

**Exception / Alternative Flow**
- No subscription: 403 `NO_ACTIVE_SUBSCRIPTION`.
- Daily limit exceeded: 403 `DOWNLOAD_LIMIT_EXCEEDED`.

**Postconditions**
- audio_file downloaded to user's device.
- Record added to track_downloads.
- License for the track exists in licenses (newly issued or existing).

---

## SOUND-012: Update Track

| Field | Value |
|-------|-------|
| **Code** | SOUND-012 |
| **Version** | 26-02-20 |
| **Description** | Admin updates track metadata and files. Includes publish/unpublish via is_active change. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Target track exists in DB. |
| **Trigger** | Admin clicks the 'Edit' button on the track detail page. |
| **Related UC** | SOUND-006 (view track detail) |

**Main Flow**
1. Admin modifies fields (title, BPM, key, description, tags, files, is_active).
2. Frontend sends the changed data as multipart/form-data to the backend.
3. Backend performs authorization and validation.
4. If audioFile changed: saves the new file to storage, replaces path. Enqueues preview_file regeneration.
5. If tags changed: updates track_tags + updates tracks.updated_at.
6. Backend updates the DB record and returns the updated track information.

**Exception / Alternative Flow**
- -

**Postconditions**
- Updated track information reflected in DB.
- If files changed, file storage updated.

---

## SOUND-019: Add Track to Playlist [Cross-reference]

| Field | Value |
|-------|-------|
| **Code** | SOUND-019 |
| **Version** | 26-03-07 |
| **Description** | User adds a track to an existing playlist from the track list or track detail screen. Canonical definition is in `sound-playlist.md`. This entry exists as a trigger-side reference for track-context UX flows. |
| **Actor** | User (subscriber), Backend |
| **Preconditions** | Logged in. Has active subscription (user_subscriptions.status=ACTIVE). Must own the target playlist. |
| **Trigger** | User clicks the 'Add to Playlist' button on the track list (Screen 1/3) or track detail screen (B-1). |
| **Related UC** | SOUND-005 (list tracks), SOUND-006 (view track detail), SOUND-008 (view playlist detail) |

**Main Flow**
1. User clicks the 'Add to Playlist' button on a track in the track list or detail screen.
2. Frontend displays a SelectModal showing the user's active playlists.
3. User selects a playlist.
4. Frontend sends a request to the backend. (`POST /api/playlists/{id}/tracks` with `{ "trackId": ... }`)
5. Backend verifies the playlist belongs to the user.
6. Backend creates a record in playlist_tracks and returns 201 Created.
7. Frontend displays a completion toast notification.

**Exception / Alternative Flow**
- Track already in the selected playlist: 409 response.

**Postconditions**
- Record added to playlist_tracks. Playlist track count increased.

> **Canonical definition**: `docs/design/usecase/sound-playlist.md` — SOUND-019.

---

## SOUND-016: Delete Track

| Field | Value |
|-------|-------|
| **Code** | SOUND-016 |
| **Version** | 26-02-20 |
| **Description** | Admin soft-deletes a track. (is_active=0. Actual files in storage are retained.) |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Target track exists in DB. |
| **Trigger** | Admin clicks the 'Delete' button on a track. |
| **Related UC** | - |

**Main Flow**
1. Admin clicks the 'Delete' button on a track.
2. Frontend displays a deletion confirmation dialog.
3. Upon confirmation, frontend sends a delete request to the backend.
4. Backend performs authorization and validation.
5. Backend sets tracks.is_active=0. (soft delete)
6. Backend deletes the tag mapping records for this track from track_tags.
7. Backend returns 204 No Content.

**Exception / Alternative Flow**
- -

**Postconditions**
- tracks.is_active=0 updated (soft delete). Actual files in storage are retained.
- Tag mappings for this track deleted from track_tags.
- Track excluded from track list queries (SOUND-005).

---

## SOUND-021: List Tracks (Admin)

| Field | Value |
|-------|-------|
| **Code** | SOUND-021 |
| **Version** | 26-03-08 |
| **Description** | Admin retrieves the full track list including both active and inactive tracks. Supports optional filtering by isActive status. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. |
| **Trigger** | Admin navigates to the track management screen (K-7). |
| **Related UC** | SOUND-016 (delete track), SOUND-012 (update track) |

**Main Flow**
1. Admin navigates to the track management screen.
2. Frontend sends a request to `GET /api/tracks/admin` with optional query parameters (page, size, isActive).
3. Backend verifies ADMIN role authorization.
4. Backend applies filtering:
   - `isActive=true`: returns active tracks only (is_active=1).
   - `isActive=false`: returns inactive tracks only (is_active=0).
   - `isActive` not provided: returns all tracks regardless of is_active status.
5. Backend returns a paginated list of `AdminTrackListItemResponse` objects.
6. Frontend displays the track list on the management screen.

**Query Parameters**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | Integer | 1 | Page number (1-based) |
| size | Integer | 20 | Items per page |
| isActive | Boolean | (none) | Optional filter by active status |

**Response Fields (AdminTrackListItemResponse)**
| Field | Type | Description |
|-------|------|-------------|
| id | Long | Track ID |
| title | String | Track title |
| bpm | Integer | BPM value |
| tonality | String | Key/tonality |
| thumbnail | String | Thumbnail file path |
| playCount | Long | Total play count |
| isActive | Boolean | Active/inactive status |
| tags | List\<String\> | Associated tag names |
| createdAt | LocalDateTime | Track creation timestamp |

**Exception / Alternative Flow**
- Non-admin access: 403 Forbidden.
- No results matching filter: returns empty content array.

**Postconditions**
- Paginated track list (including inactive tracks) displayed on admin management screen.
