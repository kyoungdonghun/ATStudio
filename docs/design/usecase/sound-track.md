# Sound -- Track Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 1 (Track)
> **DB Reference**: `docs/design/db-schema.md` Section 4.1 (`tracks`, `track_tags`, `track_downloads`)

---

## SOUND-001: Create Track

| Field | Value |
|-------|-------|
| **Code** | SOUND-001 |
| **Version** | 26-08-09 |
| **Description** | Admin registers a new track. The service stores the original and waveform metadata; public listening later uses the controller without exposing the storage key or a direct static URL. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. At least one tag exists in the tags DB. |
| **Trigger** | Admin clicks the 'Create' button on the track registration page. |
| **Related UC** | SOUND-006 (view track detail), SOUND-012 (update track), SOUND-010 (play track) |

**Main Flow**
1. Admin enters metadata (title, BPM, key, description) and selects tags, including optional visible `USAGE` guide tags.
2. Admin attaches the audio file and an optional square JPEG/PNG thumbnail.
3. Frontend validates the thumbnail type, size, and 1:1 natural dimensions and
   shows the selected image in the same centered square `cover` viewport used
   by Track cards. The backend remains authoritative.
4. Frontend sends metadata and files to the backend as multipart/form-data.
5. Backend performs authorization and server-side validation.
6. Backend decodes the audio once through the Java Sound/mp3spi path and derives
   rounded duration plus the 200-point waveform from the same PCM pass. Invalid
   audio returns 400 `AUDIO_ANALYSIS_FAILED` before storage or DB mutation.
7. Backend requires a new thumbnail to be square, downscales without upscaling
   to at most 2048x2048, and canonicalizes it to JPEG.
8. Backend stores files through the existing mutation coordinator and creates
   the Track (`is_active=0`, `play_count=0`) with duration, waveform, and
   `track_tags` in one transactional workflow.
9. Backend keeps the original storage key as private operational metadata and does not publish a direct static URL.
10. Backend returns an admin response containing the original `audioFile` storage key (201 Created).

**Postconditions**
- Track record (is_active=0) created in DB.
- `audioFile` is saved in file storage.
- track_tags linked. Admin must separately set is_active=1 to expose the track to users.

---

## SOUND-005: List Tracks

| Field | Value |
|-------|-------|
| **Code** | SOUND-005 |
| **Version** | 26-08-13 |
| **Description** | User (including non-members) views the track list. Only tracks with is_active=1 are returned. |
| **Actor** | User (including non-members), Backend |
| **Preconditions** | - |
| **Trigger** | User navigates to the track list screen or enters search criteria. |
| **Related UC** | SOUND-010 (play track), SOUND-011 (download track), SOUND-006 (view track detail) |

**Main Flow**
1. User selects/enters search criteria (keyword, Genre, Mood, Instrument,
   Usage Guide Tag, BPM range, key, sort order). All are optional.
2. Frontend sends each selected Tag as a repeated `genre`, `mood`,
   `instrument`, or `usage` query parameter. Commas and `#` remain inside one
   encoded Tag value.
3. Backend searches `keyword` against the track title and associated `USAGE` guide tag names only.
4. Backend canonicalizes and de-duplicates Tag values, combines every selected
   value with AND semantics, and returns only matching active Tracks.
5. Backend validates `page >= 1` and `1 <= size <= 100` before creating the
   database page request. The paginated response uses `dataList` plus a
   1-based `pageInfo`.
6. Frontend restores all four Tag types from the URL, preserves them through
   sort/page changes, and renders `USAGE` as a visible hashtag subline.
7. Frontend canonicalizes malformed, non-integer, zero, negative, and
   beyond-last-page values before the corresponding bounded request. The
   public catalog uses page size 20 and keeps all compatible query state.

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
- No search results: returns an empty `dataList` with `pageInfo`.
- Invalid page or size: returns 400 `INVALID_ARGUMENT` without querying Track
  search results.
- A frontend URL with an invalid or beyond-last page is replaced with its
  canonical 1-based page and does not repeat the invalid request.
- If available-Tag loading fails, every registered filter choice remains
  selectable rather than being disabled from stale availability data.

**Postconditions**
- Track list matching criteria (title, tag names, BPM, key, thumbnail, playCount, likeCount, downloadCount, tags) and pageInfo displayed on screen.

---

## SOUND-006: View Track Detail

| Field | Value |
|-------|-------|
| **Code** | SOUND-006 |
| **Version** | 26-08-13 |
| **Description** | User (including non-members) views detailed track information. |
| **Actor** | User (including non-members), Backend |
| **Preconditions** | Target track exists in DB with is_active=1. |
| **Trigger** | User clicks a track or navigates to the track detail URL. |
| **Related UC** | SOUND-010 (play track), SOUND-011 (download track) |

**Main Flow**
1. Frontend sends a detail request including trackId to the backend.
2. Backend retrieves the track record and associated tags, then returns a public `TrackResponse` with `audioFile=null`.
3. Frontend displays the track detail on screen, including visible `USAGE` guide hashtags if linked to the track.

**Exception / Alternative Flow**
- Track not found or is_active=0: 404 response.
- A missing Track or recoverable request failure renders fixed Korean retry,
  safe Back, and Home recovery. Raw transport and server error text is absent.

**Postconditions**
- Track metadata (title, BPM, key, description, tags, visible usage guide hashtags, counts, waveform data, etc.) is displayed. The original storage key is not included in public detail data.

---

## SOUND-010: Play Track

| Field | Value |
|-------|-------|
| **Code** | SOUND-010 |
| **Version** | 26-08-13 |
| **Description** | User (including non-members) listens to the complete active Track through the public controller-mediated stream. Listening remains separate from official download and License entitlement. Play history recording is handled by the frontend calling SOUND-004 separately. |
| **Actor** | User (including non-members), Backend |
| **Preconditions** | Track exists in DB with is_active=1. audio_file exists in file storage. |
| **Trigger** | User clicks the 'Play' button on a track. |
| **Related UC** | SOUND-005 (list tracks) |

**Main Flow**
1. User clicks the 'Play' button.
2. Frontend sends a streaming request including trackId to the backend.
3. Backend loads the active Track's `audio_file` through the stream controller without returning its storage key or publishing a direct static URL.
4. Without a `Range` header, Backend returns the complete resource representation. With one valid Range, Backend resolves it against the full resource length.
5. Frontend starts playback and marks the player as playing only after `HTMLAudioElement.play()` succeeds.
6. `waiting` or `stalled` starts a pending timer. A non-fatal buffering message
   appears only after 2 seconds and is cancelled by recovery, pause, retry,
   Track change, or error. A generation fence prevents an old timer from
   affecting a newer Track.
7. `audio.error` and rejected `play()` use the separate playback-error state;
   an actual error takes precedence over the buffering status.
8. Frontend records browser-local Play History only after playback starts.
9. Restored and seeked current time is normalized to a finite non-negative
   value and clamped to the current positive media duration when known. The
   player time and waveform use that same bound.

**Exception / Alternative Flow**
- No `Range` header: returns `200` with the full resource length.
- One valid start/end, open-ended, or suffix Range: returns `206` against the full resource length.
- Malformed, multiple, unsupported, zero-length, or unsatisfiable Range: returns `416` with `Content-Range: bytes */{fullLength}`.
- Missing/inactive track or unavailable resource: track-not-found response.

**Postconditions**
- The complete active Track is available for public listening through the controller. The original storage key is absent from public responses, `/uploads/tracks/audio/**` remains denied, and listening creates no download record or License. Play history and play_count updates are handled in SOUND-004.

## PlayableTrack Batch Hydration

`POST /api/tracks/batch` is a public, non-paginated hydration endpoint used by
album, playlist, likes, download-history, queue, persisted player, and local
Play History paths.

- Request: `ids`, 1 to 100 positive non-null Track IDs.
- Processing: de-duplicate in first-requested-ID order and query active Tracks
  plus tags in bounded collection queries.
- Response: `dataList` of `id`, `title`, `artistName`, `duration`, `thumbnail`,
  `waveformData`, `bpm`, `tonality`, and `tags`, preserving requested ID order.
- Missing or inactive IDs are omitted. No per-row Track-detail HTTP request is
  issued.
- Browser persistence stores Track IDs and current time, then hydrates on
  restore. Generation/snapshot checks reject stale hydration results.

---

## SOUND-011: Download Track

| Field | Value |
|-------|-------|
| **Code** | SOUND-011 |
| **Version** | 26-07-15 |
| **Description** | An authenticated member performs an official download of the original Track file. A first download records the event and automatically issues a License; an existing License permits entitled re-download without duplicate issuance. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. For a first download, user_subscriptions.status = ACTIVE and today's download count < plan download_per_day. |
| **Trigger** | User clicks the 'Download' button on a track. |
| **Related UC** | UTIL-006 (check download count), INFO-009 (my license list) |

**Main Flow**
1. User clicks the 'Download' button.
2. Frontend sends a download request including auth token and trackId to the backend.
3. For a first download, Backend checks subscription status from user_subscriptions.
4. For a first download, Backend calculates today's download count via COUNT query on track_downloads (DATE(downloaded_at) = CURDATE()) and compares against the plan limit.
5. Backend retrieves the audio_file (original) from file storage.
6. If no License exists, Backend saves a download record in track_downloads.
7. Backend checks for an existing License (user_id, track_id) in the licenses table. If none exists, it issues a new UUID-based License. If one exists, it skips duplicate issuance and the additional daily-count entry.
8. Backend returns the file (Content-Disposition: attachment).

**Exception / Alternative Flow**
- First download without an active subscription: 403 `NO_ACTIVE_SUBSCRIPTION`.
- First download after the daily limit: 403 `DOWNLOAD_LIMIT_EXCEEDED`.

**Postconditions**
- audio_file downloaded to user's device.
- First download adds a record to track_downloads.
- License for the track exists in licenses (newly issued or existing).

---

## SOUND-012: Update Track

| Field | Value |
|-------|-------|
| **Code** | SOUND-012 |
| **Version** | 26-08-09 |
| **Description** | Admin updates track metadata and files. Includes publish/unpublish via is_active change. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Target track exists in DB. |
| **Trigger** | Admin clicks the 'Edit' button on the track detail page. |
| **Related UC** | SOUND-006 (view track detail) |

**Main Flow**
1. Admin modifies fields (title, BPM, key, description, tags, files, is_active).
2. Frontend sends the changed data as multipart/form-data to the backend.
3. Backend performs authorization and validation.
4. If audio changes, Backend analyzes first, then replaces the storage key,
   duration, and waveform as one logical change. Analysis/storage/DB failure
   keeps all old values.
5. If a new thumbnail is supplied, the same square/canonical-JPEG rule as
   create applies before replacement. An existing non-square thumbnail is
   preserved when no replacement file is supplied; the UI only warns that a
   square replacement is recommended.
6. If tags changed: updates track_tags + updates tracks.updated_at.
7. Metadata-only update does not decode audio and preserves duration/waveform.
8. Backend updates the DB record and returns the updated track information.

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
| **Version** | 26-07-16 |
| **Description** | User adds a track to an existing playlist from the track list or track detail screen. Canonical definition is in `sound-playlist.md`. This entry exists as a trigger-side reference for track-context UX flows. |
| **Actor** | User (subscriber), Backend |
| **Preconditions** | Logged in. Has a service-enabled subscription (`user_subscriptions.status=ACTIVE`, or `status=CANCELLED` before `expiresAt`). Must own the target playlist. |
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
- No results matching filter: returns an empty `dataList` with `pageInfo`.

**Postconditions**
- Paginated track list (including inactive tracks) displayed on admin management screen.

---

## ADMIN Track Audio Analysis Dry-Run

- API: `GET /api/admin/tracks/audio-analysis/dry-run?page=1&size=20`.
- Scope: active and inactive Tracks, sorted by Track ID ascending; page size is
  limited to 1 through 100.
- Response: `dataList` plus `pageInfo`, with readability, stored/analyzed
  duration, delta, waveform presence, format, recommendation, decoded frames,
  sample rate, and channel count.
- Safety: the response contains no audio storage key/path. The service is
  read-only and does not save, update, delete, or backfill a Track.
- Boundary: no existing-row dry-run has been executed against current
  persistent storage under WI-022, and no backfill has been approved or run.
