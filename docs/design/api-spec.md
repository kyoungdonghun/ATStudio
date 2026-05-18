# ATStudio API Specification v9 (Confirmed)

> **Status**: 9th confirmed — live contract sync (download history, public capabilities, auth/profile conflict cleanup, payment billing sync)
> **Base**: v8 + 2026-05-18 payment patch
> **Date**: 2026-05-18

---

## v8 → v9 Change History

| # | Item | Decision |
|---|------|----------|
| Z1 | §11 new entries | Added §11.4 GET /api/downloads/history and §11.5 GET /api/downloads/history/track-ids (SR-79/80 live download history APIs) |
| Z2 | §14 new entry | Added §14.12 GET /api/utils/public-capabilities (runtime auth/environment capability hints) |
| Z3 | §5 user flows | Added actual 409 conflict cases for email/nickname/phone duplication across register/profile flows |
| Z4 | Full API Summary | Updated total count from 104 → 107 |
| Z5 | §6 new entries | Added mock-first payment contract: §6.3.1 prepare, §6.3.2 confirm, §6.3.3 cancel/fail |
| Z6 | Full API Summary | Updated total count from 107 → 110 |
| Z7 | §6.3 payment entries | Added Toss one-time payment provider fields and redirect confirm contract |
| Z8 | §6.3 new entries | Added Toss billing agreement APIs: prepare, confirm, read current, cancel current |
| Z9 | Full API Summary | Updated total count from 110 → 114 |

---

## v7 → v8 Change History

| # | Item | Decision |
|---|------|----------|
| Y1 | §2 new entry | Added §2.5 GET /api/tags/available (tag recombination, SR-70) — returns tags present on active tracks matching current filters |
| Y2 | §1 new entry | Added §1.9 GET /api/tracks/admin/{trackId} (SR-60) — admin-only track detail including inactive tracks |
| Y3 | §1.8 GET /api/tracks/admin | Added `keyword` query parameter (NFC-normalized title search) |
| Y4 | §3 new entry | Added §3.9 POST /api/playlists/{playlistId}/tracks/batch (SR-52) — bulk add tracks (max 50), duplicates/inactive skipped silently |
| Y5 | §5.3 POST /api/auth/social/{provider} | Added `codeVerifier` field (PKCE, required) |
| Y6 | §10 Likes / §11 Download Queue | Replaced obsolete errorCodes (TRACK_ALREADY_IN_LIKES etc.) with actual backend codes: RESOURCE_DUPLICATE (409) / RESOURCE_NOT_FOUND (404) |
| Y7 | Full API Summary | Updated total count from 101 → 104 |

---

## v6 → v7 Change History

| # | Item | Decision |
|---|------|----------|
| X1 | §17 new section | Added §17 Site Settings: GET /api/settings/{key} [PUBLIC] + PUT /api/admin/settings/{key} [ADMIN] |
| X2 | Input validation | ValidationConstants centralized: 15 DTOs now enforce @Pattern/@Size/@Max constraints on backend; frontend validation.ts mirrors constraints |
| X3 | Full API Summary | Updated total count from 99 → 101 |

---

## v5 → v6 Change History

| # | Item | Decision |
|---|------|----------|
| W1 | §14.5 GET /api/utils/download-count response | Added `nextResetAt` (LocalDateTime, ISO-8601) field |
| W2 | §14 new entry | Added §14.8 GET /api/utils/subscription-change-preview |
| W3 | §6.7 PUT /api/user-subscriptions/me | Added UPGRADE/DOWNGRADE branch semantics, `changeType` field in response |
| W4 | §6.10 DELETE /api/user-subscriptions/me | Grace period semantics — expiresAt까지 서비스 이용 가능으로 수정 |
| W5 | §1 new entry | Added §1.8 GET /api/tracks/admin (admin-only full track list) |
| W6 | §3.1 POST /api/playlists error cases | Added 409 PLAYLIST_LIMIT_EXCEEDED |
| W7 | §6 new entry | Added §6.1.1 GET /api/subscriptions/admin (admin-only, includes inactive plans) |

## v4 → v5 Change History

| # | Item | Decision |
|---|------|----------|
| V1 | Error response `error` field | **Standards compliance** — `error` = HTTP reason phrase (e.g., "Forbidden"). Domain code separated into new `errorCode` field. |
| V2 | pageInfo structure | **Standards compliance** — `totalElements/totalPages` removed → `total, start, end, prev, next` (block pagination). |
| V3 | List field name | **Standards compliance** — `content` → `dataList` |
| V4 | Success response body | **Standards compliance** — `status` field removed. Delivered via HTTP status code only. |

---

## v3 → v4 Change History

| # | Item | Decision |
|---|------|----------|
| 1 | Social login two-step registration | **Added (confirmed)** — On first signup, returns `isProfileComplete: false`. Added 5.10 Profile Completion API. |
| 2 | Playlist full API | **Subscriber-only (confirmed)** — 3.1~3.8 auth: `auth required (subscribers only)` |
| 3 | Streaming API play history removal | **Separated (confirmed)** — Removed play_histories recording from 1.4 stream. Single path: 4.1 POST /api/play-histories. |
| 4 | Inquiry edit API removal | **Removed (confirmed)** — 8.6 inquiry edit API deleted. Frontend shows edit-not-allowed notice. |
| 5 | Remove isActive from whitelist response | **Removed (confirmed)** — Response updated due to is_active column removal. |
| 6 | Add self subscription cancel API | **Added (confirmed)** — 6.10 DELETE /api/user-subscriptions/me |
| 7 | Add inquiry status change API UC | **Added (confirmed)** — QUESTION-008 (admin only, corresponds to existing 8.7 API) |
| 8 | subscription_payments | `user_subscription_id` FK applied (DB change sync) |

---

## v2 → v3 Change History

| # | Item | Decision |
|---|------|----------|
| 1 | Track streaming | **preview_file served first** — If `preview_file` exists, serve low-quality stream; if NULL, `audio_file` fallback |
| 2 | Nickname duplicate check API | **Added (confirmed)** — `GET /api/utils/check-nickname` |

---

## v1 → v2 Change History

| # | Item | Decision |
|---|------|----------|
| 1 | playlog interpretation | **Play history (play_histories) confirmed** |
| 2 | Track deletion | **Soft delete (is_active=0)** |
| 3 | Notice DB | **`notices` table added (confirmed)** |
| 4 | Inquiry attachments | **`question_attachments` separate table added** |
| 5 | Play history deletion | **Both full and selective delete supported** |
| 6 | License issuance | **Auto-issued on download** (manual issuance API removed, duplicate prevention) |
| 7 | Batch download | **Not ZIP** — Frontend calls individual API sequentially + beforeunload exit prevention |
| 8 | Tag list retrieval | **Added (confirmed)** |

---

## Common Rules

### Base URL
```
/api
```

### Authentication
- JWT Bearer Token (`Authorization: Bearer {token}`)
- `[PUBLIC]` = no auth required
- `[ADMIN]` = admin only

### Common Response Format
```json
{
  "message": "Success",
  "data": { ... }
}
```

> `status` field is delivered via HTTP status code only. Not included in the response body.

### Common Error Response
```json
{
  "status": 400,
  "error": "Bad Request",
  "errorCode": "DOMAIN_ERROR_CODE",
  "message": "사용자 메시지"
}
```

> - `error`: HTTP reason phrase (e.g., "Bad Request", "Forbidden", "Not Found")
> - `errorCode`: Domain error code (included only for domain-specific errors; omitted otherwise)
> - `message`: Safe message to display to the user

### Pagination (Common for List Queries)
```json
{
  "dataList": [ ... ],
  "pageInfo": {
    "page": 1,
    "size": 20,
    "total": 150,
    "start": 1,
    "end": 8,
    "prev": false,
    "next": true
  }
}
```

> - `total`: Total data count
> - `start` / `end`: Start/end page numbers of the current block
> - `prev` / `next`: Whether previous/next block exists

---

# 1. Sound — Track

## 1.1 Create Track
| Field | Value |
|-------|-------|
| **URL** | `POST /api/tracks` |
| **Auth** | `[ADMIN]` |
| **Description** | Admin uploads a new track (published after review: is_active=0). After upload, async low-quality `preview_file` generation (on failure, stays NULL → audio_file fallback) |

**Request** (multipart/form-data)
```
title: String (required, max 100)
bpm: Integer (required)
tonality: String (required, max 10)
description: String (optional)
audioFile: File (required)
thumbnail: File (optional)
tagIds: List<Long> (optional)
```

**Response** `201 Created`
```json
{
  "id": 1,
  "title": "Summer Vibes",
  "bpm": 120,
  "tonality": "C",
  "description": "...",
  "audioFile": "/tracks/audio/summer-vibes.mp3",
  "thumbnail": "/tracks/thumbnail/summer-vibes.jpg",
  "isActive": false,
  "playCount": 0,
  "tags": [
    { "id": 1, "name": "Happy", "type": "MOOD" }
  ],
  "createdAt": "2026-02-19T10:00:00"
}
```

## 1.2 List Tracks
| Field | Value |
|-------|-------|
| **URL** | `GET /api/tracks` |
| **Auth** | `[PUBLIC]` |
| **Description** | List tracks (search, filter, pagination). Returns only active (is_active=1) tracks |

**Query Parameters**
```
page: Integer (default: 1)
size: Integer (default: 20)
keyword: String (optional, title search)
genre: String (optional, genre tag filter)
mood: String (optional, mood tag filter)
instrument: String (optional, instrument tag filter)
bpmMin: Integer (optional)
bpmMax: Integer (optional)
tonality: String (optional)
sort: String (optional, "latest"|"popular"|"likes"|"downloads", default: "latest")
```

**Response** `200 OK`
```json
{
  "dataList": [
    {
      "id": 1,
      "title": "Summer Vibes",
      "artistName": "NickName",
      "duration": 180,
      "bpm": 120,
      "tonality": "C",
      "thumbnail": "/tracks/thumbnail/summer-vibes.jpg",
      "playCount": 1500,
      "tags": [
        { "id": 1, "name": "Happy", "type": "MOOD" }
      ],
      "createdAt": "2026-02-19T10:00:00"
    }
  ],
  "pageInfo": { "page": 1, "size": 20, "total": 150, "start": 1, "end": 8, "prev": false, "next": true }
}
```

## 1.3 Get Track
| Field | Value |
|-------|-------|
| **URL** | `GET /api/tracks/{trackId}` |
| **Auth** | `[PUBLIC]` |
| **Description** | Get track detail |

**Response** `200 OK`
```json
{
  "id": 1,
  "title": "Summer Vibes",
  "artistName": "NickName",
  "duration": 180,
  "bpm": 120,
  "tonality": "C",
  "description": "A happy summer track for shorts",
  "audioFile": "/tracks/audio/summer-vibes.mp3",
  "thumbnail": "/tracks/thumbnail/summer-vibes.jpg",
  "isActive": true,
  "playCount": 1500,
  "tags": [
    { "id": 1, "name": "Happy", "type": "MOOD" },
    { "id": 5, "name": "Pop", "type": "GENRE" }
  ],
  "createdAt": "2026-02-19T10:00:00",
  "updatedAt": "2026-02-19T12:00:00"
}
```

## 1.4 Stream Track
| Field | Value |
|-------|-------|
| **URL** | `GET /api/tracks/{trackId}/stream` |
| **Auth** | `[PUBLIC]` |
| **Description** | Track preview streaming (available to non-members). If `preview_file` exists, serves low-quality file; if `preview_file` is NULL, falls back to `audio_file`. Play history recording is done by the frontend explicitly calling 4.1 API separately. |

**Response** `200 OK` — audio stream (Content-Type: audio/mpeg)

## 1.5 Download Track
| Field | Value |
|-------|-------|
| **URL** | `GET /api/tracks/{trackId}/download` |
| **Auth** | auth required (subscribers only) |
| **Description** | Download track file. Checks daily download limit. Saves download record + auto-issues license (does not re-issue if existing license exists) |

**Response** `200 OK` — file download (Content-Disposition: attachment)

**Error Cases**
```json
{ "status": 403, "error": "Forbidden", "errorCode": "DOWNLOAD_LIMIT_EXCEEDED", "message": "오늘의 다운로드 한도를 초과했습니다." }
{ "status": 403, "error": "Forbidden", "errorCode": "NO_ACTIVE_SUBSCRIPTION", "message": "구독이 필요한 서비스입니다." }
```

## 1.6 Update Track
| Field | Value |
|-------|-------|
| **URL** | `PUT /api/tracks/{trackId}` |
| **Auth** | `[ADMIN]` |
| **Description** | Update track info (including activate/deactivate) |

**Request** (multipart/form-data)
```
title: String (optional)
bpm: Integer (optional)
tonality: String (optional)
description: String (optional)
audioFile: File (optional)
thumbnail: File (optional)
tagIds: List<Long> (optional)
isActive: Boolean (optional)
```

**Response** `200 OK` — Updated track detail (same format as 1.3)

## 1.7 Delete Track (Soft Delete)
| Field | Value |
|-------|-------|
| **URL** | `DELETE /api/tracks/{trackId}` |
| **Auth** | `[ADMIN]` |
| **Description** | Soft delete (deactivate with is_active=0) |

**Response** `204 No Content`

## 1.8 List All Tracks (Admin)
| Field | Value |
|-------|-------|
| **URL** | `GET /api/tracks/admin` |
| **Auth** | `[ADMIN]` |
| **Description** | Full track list including inactive tracks (admin only). Filter by `is_active` if provided; if omitted, returns all tracks regardless of active status. |

**Query Parameters**
```
page: Integer (default: 1)
size: Integer (default: 20)
is_active: Boolean (optional — if omitted, all tracks returned; true = active only, false = inactive only)
keyword: String (optional, track title keyword search — NFC-normalized)
```

**Response** `200 OK`
```json
{
  "dataList": [
    {
      "id": 1,
      "title": "Summer Vibes",
      "bpm": 120,
      "tonality": "C",
      "thumbnail": "/tracks/thumbnail/summer-vibes.jpg",
      "playCount": 1500,
      "isActive": false,
      "tags": [
        { "id": 1, "name": "Happy", "type": "MOOD" }
      ],
      "createdAt": "2026-02-19T10:00:00"
    }
  ],
  "pageInfo": { "page": 1, "size": 20, "total": 150, "start": 1, "end": 8, "prev": false, "next": true }
}
```

**Error Cases**
```json
{ "status": 401, "error": "Unauthorized", "message": "인증이 필요합니다." }
{ "status": 403, "error": "Forbidden", "message": "관리자 권한이 필요합니다." }
```

## 1.9 Get Track Detail (Admin)
| Field | Value |
|-------|-------|
| **URL** | `GET /api/tracks/admin/{trackId}` |
| **Auth** | `[ADMIN]` |
| **Description** | Admin-only track detail endpoint. Unlike `GET /api/tracks/{trackId}`, this returns tracks regardless of `is_active` status, enabling admins to edit soft-deleted (deactivated) tracks. (SR-60) |

**Response** `200 OK` — Same shape as `GET /api/tracks/{trackId}` (TrackResponse)

**Error Cases**
```json
{ "status": 403, "error": "Forbidden", "message": "관리자 권한이 필요합니다." }
{ "status": 404, "error": "Not Found", "errorCode": "RESOURCE_NOT_FOUND", "message": "트랙을 찾을 수 없습니다." }
```

---

# 2. Sound — Tag

## 2.1 Create Tag
| Field | Value |
|-------|-------|
| **URL** | `POST /api/tags` |
| **Auth** | `[ADMIN]` |

**Request**
```json
{
  "name": "Lo-Fi",
  "type": "GENRE"
}
```

**Response** `201 Created`
```json
{
  "id": 10,
  "name": "Lo-Fi",
  "type": "GENRE",
  "createdAt": "2026-02-19T10:00:00"
}
```

## 2.2 List Tags
| Field | Value |
|-------|-------|
| **URL** | `GET /api/tags` |
| **Auth** | `[PUBLIC]` |
| **Description** | Full tag list for filter UI |

**Query Parameters**
```
type: String (optional, "MOOD"|"GENRE"|"INSTRUMENT")
```

**Response** `200 OK`
```json
{
  "dataList": [
    { "id": 1, "name": "Happy", "type": "MOOD", "createdAt": "2026-02-19T10:00:00" },
    { "id": 2, "name": "Pop", "type": "GENRE", "createdAt": "2026-02-19T10:00:00" }
  ]
}
```

## 2.3 Update Tag
| Field | Value |
|-------|-------|
| **URL** | `PUT /api/tags/{tagId}` |
| **Auth** | `[ADMIN]` |

**Request**
```json
{
  "name": "Lo-Fi Hip Hop",
  "type": "GENRE"
}
```

**Response** `200 OK` — Updated tag info

## 2.4 Delete Tag
| Field | Value |
|-------|-------|
| **URL** | `DELETE /api/tags/{tagId}` |
| **Auth** | `[ADMIN]` |

**Response** `204 No Content`

## 2.5 List Available Tags (Tag Recombination)
| Field | Value |
|-------|-------|
| **URL** | `GET /api/tags/available` |
| **Auth** | `[PUBLIC]` |
| **Description** | Returns tags that appear on at least one active track matching the current filter set. Used for narrow-down tag recombination search (SR-70): as the user selects tags, only the remaining tags present on still-matching tracks are shown, guaranteeing that any further selection yields a non-empty result set. |

**Query Parameters**
```
genre: String (optional, comma-separated genre tag names)
mood: String (optional, comma-separated mood tag names)
bpmMin: Integer (optional)
bpmMax: Integer (optional)
```

**Response** `200 OK`
```json
{
  "dataList": [
    { "id": 1, "name": "Happy", "type": "MOOD", "createdAt": "2026-02-19T10:00:00" },
    { "id": 2, "name": "Pop", "type": "GENRE", "createdAt": "2026-02-19T10:00:00" }
  ]
}
```

---

# 3. Sound — Playlist

## 3.1 Create Playlist
| Field | Value |
|-------|-------|
| **URL** | `POST /api/playlists` |
| **Auth** | auth required (subscribers only) |

**Request** (multipart/form-data)
```
title: String (required, max 50)
description: String (optional)
thumbnail: File (optional)
```

**Response** `201 Created`
```json
{
  "id": 1,
  "title": "My Workout Mix",
  "description": "운동할 때 듣는 비트",
  "thumbnail": null,
  "trackCount": 0,
  "createdAt": "2026-02-19T10:00:00"
}
```

**Error Cases**
```json
{ "status": 409, "error": "Conflict", "errorCode": "PLAYLIST_LIMIT_EXCEEDED", "message": "구독 플랜의 재생목록 한도를 초과했습니다." }
```

## 3.2 List Playlists
| Field | Value |
|-------|-------|
| **URL** | `GET /api/playlists` |
| **Auth** | auth required (subscribers only) |
| **Description** | List my playlists |

**Response** `200 OK`
```json
{
  "dataList": [
    {
      "id": 1,
      "title": "My Workout Mix",
      "thumbnail": null,
      "trackCount": 5,
      "createdAt": "2026-02-19T10:00:00"
    }
  ]
}
```

## 3.3 Get Playlist
| Field | Value |
|-------|-------|
| **URL** | `GET /api/playlists/{playlistId}` |
| **Auth** | auth required (subscribers only, owner only) |
| **Description** | Playlist detail (included tracks + play order) |

**Response** `200 OK`
```json
{
  "id": 1,
  "title": "My Workout Mix",
  "description": "운동할 때 듣는 비트",
  "thumbnail": null,
  "tracks": [
    { "trackOrder": 1, "trackId": 10, "title": "Energy Boost", "bpm": 140, "tonality": "Am" },
    { "trackOrder": 2, "trackId": 22, "title": "Run Fast", "bpm": 160, "tonality": "Em" }
  ],
  "createdAt": "2026-02-19T10:00:00",
  "updatedAt": "2026-02-19T15:00:00"
}
```

## 3.4 Add Track to Playlist
| Field | Value |
|-------|-------|
| **URL** | `POST /api/playlists/{playlistId}/tracks` |
| **Auth** | auth required (subscribers only, owner only) |

**Request**
```json
{
  "trackId": 10
}
```

**Response** `201 Created`

**Error Cases**

| Status | errorCode | Condition |
|--------|-----------|-----------|
| 409 Conflict | - | Track already in playlist |

## 3.5 Update Playlist
| Field | Value |
|-------|-------|
| **URL** | `PUT /api/playlists/{playlistId}` |
| **Auth** | auth required (subscribers only, owner only) |

**Request** (multipart/form-data)
```
title: String (optional)
description: String (optional)
thumbnail: File (optional)
```

**Response** `200 OK`

## 3.6 Reorder Playlist Tracks
| Field | Value |
|-------|-------|
| **URL** | `PUT /api/playlists/{playlistId}/tracks` |
| **Auth** | auth required (subscribers only, owner only) |

**Request**
```json
{
  "tracks": [
    { "trackId": 22, "trackOrder": 1 },
    { "trackId": 10, "trackOrder": 2 }
  ]
}
```

**Response** `200 OK`

## 3.7 Remove Track from Playlist
| Field | Value |
|-------|-------|
| **URL** | `DELETE /api/playlists/{playlistId}/tracks/{trackId}` |
| **Auth** | auth required (subscribers only, owner only) |

**Response** `204 No Content`

## 3.8 Delete Playlist
| Field | Value |
|-------|-------|
| **URL** | `DELETE /api/playlists/{playlistId}` |
| **Auth** | auth required (subscribers only, owner only) |

**Response** `204 No Content`

## 3.9 Bulk Add Tracks to Playlist
| Field | Value |
|-------|-------|
| **URL** | `POST /api/playlists/{playlistId}/tracks/batch` |
| **Auth** | auth required (subscribers only, owner only) |
| **Description** | Bulk add multiple tracks to a playlist in one request (SR-52). Silently skips tracks already in the playlist and inactive tracks; returns the count of newly added tracks. Max 50 track IDs per request. |

**Request**
```json
{
  "trackIds": [10, 22, 35, 47]
}
```

**Response** `200 OK`
```json
{
  "message": "Success",
  "data": 3
}
```
> `data` is the number of tracks actually added (duplicates and inactive tracks skipped).

**Error Cases**
```json
{ "status": 400, "error": "Bad Request", "errorCode": "INVALID_ARGUMENT", "message": "trackIds는 최대 50개까지 가능합니다." }
{ "status": 403, "error": "Forbidden", "message": "본인의 재생목록만 수정할 수 있습니다." }
{ "status": 404, "error": "Not Found", "errorCode": "RESOURCE_NOT_FOUND", "message": "재생목록을 찾을 수 없습니다." }
```

---

# 4. Sound — Play History

## 4.1 Save Play History
| Field | Value |
|-------|-------|
| **URL** | `POST /api/play-histories` |
| **Auth** | auth required |
| **Description** | Auto-records when track is played in the Que bar (synced with tracks.play_count) |

**Request**
```json
{
  "trackId": 10
}
```

**Response** `201 Created`

## 4.2 List Play History
| Field | Value |
|-------|-------|
| **URL** | `GET /api/play-histories` |
| **Auth** | auth required |
| **Description** | My play history list (newest first) |

**Query Parameters**
```
page: Integer (default: 1)
size: Integer (default: 50)
```

**Response** `200 OK`
```json
{
  "dataList": [
    {
      "id": 100,
      "track": { "id": 10, "title": "Summer Vibes", "artistName": "NickName", "thumbnail": "..." },
      "playedAt": "2026-02-19T14:30:00"
    }
  ],
  "pageInfo": { "page": 1, "size": 50, "total": 120, "start": 1, "end": 3, "prev": false, "next": true }
}
```

## 4.3 Delete Play History
| Field | Value |
|-------|-------|
| **URL** | `DELETE /api/play-histories` |
| **Auth** | auth required |
| **Description** | Selective delete (specify historyIds) or full delete (if historyIds is empty) |

**Request**
```json
{
  "historyIds": [100, 101, 102]
}
```
> If `historyIds` is an empty array `[]`, deletes all records

**Response** `204 No Content`

---

# 5. User — Info

## 5.1 Register
| Field | Value |
|-------|-------|
| **URL** | `POST /api/users` |
| **Auth** | `[PUBLIC]` |

**Request**
```json
{
  "nickname": "creator01",
  "email": "user@example.com",
  "password": "SecureP@ss123",
  "phonePersonal": "010-1234-5678",
  "phoneCompany": null,
  "job": "EDITOR",
  "userType": "INDIVIDUAL"
}
```

**Response** `201 Created`
```json
{
  "id": 1,
  "nickname": "creator01",
  "email": "user@example.com",
  "job": "EDITOR",
  "userType": "INDIVIDUAL",
  "isVerified": false,
  "createdAt": "2026-02-19T10:00:00"
}
```

**Error Cases**
```json
{ "status": 409, "error": "Conflict", "errorCode": "EMAIL_ALREADY_REGISTERED", "message": "이미 가입된 이메일입니다." }
{ "status": 409, "error": "Conflict", "errorCode": "NICKNAME_DUPLICATED", "message": "이미 사용 중인 닉네임입니다." }
{ "status": 409, "error": "Conflict", "errorCode": "PHONE_ALREADY_REGISTERED", "message": "이미 등록된 전화번호입니다." }
```

## 5.2 Login
| Field | Value |
|-------|-------|
| **URL** | `POST /api/auth/login` |
| **Auth** | `[PUBLIC]` |

**Request**
```json
{
  "email": "user@example.com",
  "password": "SecureP@ss123"
}
```

**Response** `200 OK`
```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

**Storage Notes**
- Frontend stores both `accessToken` and `refreshToken` in browser storage.
- `POST /api/auth/refresh` reads the stored refresh token and rotates both tokens on success.

## 5.3 Social Login
| Field | Value |
|-------|-------|
| **URL** | `POST /api/auth/social/{provider}` |
| **Auth** | `[PUBLIC]` |
| **Description** | OAuth2.0 social login (GOOGLE/KAKAO/NAVER) with PKCE. On first signup, creates a users record with minimal info and returns `isProfileComplete: false`. Frontend detects this and navigates to 5.10 Profile Completion screen. |

**Request**
```json
{
  "authorizationCode": "4/0AX4XfWh...",
  "codeVerifier": "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
}
```
> `codeVerifier` (required): PKCE code verifier generated by the frontend and paired with the `code_challenge` sent during the authorization request.

**Response** `200 OK`
```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "isProfileComplete": true
}
```
> If `isProfileComplete: false`, the frontend redirects to the 5.10 Profile Completion screen.

## 5.10 Complete Social Member Profile
| Field | Value |
|-------|-------|
| **URL** | `PUT /api/users/me/complete-profile` |
| **Auth** | auth required (only members with isProfileComplete=false) |
| **Description** | After initial social login signup, completes the profile by entering additional info. `INDIVIDUAL` members send `job`; `BUSINESS` members send `companyName`. userType can only be set at this step. |

**Request**
```json
{
  "nickname": "creator01",
  "phonePersonal": "010-1234-5678",
  "phoneCompany": null,
  "job": "EDITOR",
  "userType": "INDIVIDUAL"
}
```

**Business Example**
```json
{
  "nickname": "bizcreator",
  "phonePersonal": "010-1234-5678",
  "phoneCompany": "02-1234-5678",
  "job": null,
  "companyName": "ATStudio Biz",
  "userType": "BUSINESS"
}
```

**Response** `200 OK` — Same format as 5.4 My Profile response

**Error Cases**
```json
{ "status": 409, "error": "Conflict", "errorCode": "NICKNAME_DUPLICATED", "message": "이미 사용 중인 닉네임입니다." }
{ "status": 409, "error": "Conflict", "errorCode": "PHONE_ALREADY_REGISTERED", "message": "이미 등록된 전화번호입니다." }
```

## 5.4 My Profile
| Field | Value |
|-------|-------|
| **URL** | `GET /api/users/me` |
| **Auth** | auth required |

**Response** `200 OK`
```json
{
  "id": 1,
  "nickname": "creator01",
  "email": "user@example.com",
  "phonePersonal": "010-1234-5678",
  "phoneCompany": null,
  "job": "EDITOR",
  "userType": "INDIVIDUAL",
  "role": "USER",
  "isVerified": true,
  "createdAt": "2026-02-19T10:00:00"
}
```

## 5.5 List Users
| Field | Value |
|-------|-------|
| **URL** | `GET /api/users` |
| **Auth** | `[ADMIN]` |

**Query Parameters**
```
page: Integer (default: 1)
size: Integer (default: 20)
keyword: String (optional, nickname/email search)
userType: String (optional, "INDIVIDUAL"|"BUSINESS")
```

**Response** `200 OK` — Pagination + user list

## 5.6 Get User
| Field | Value |
|-------|-------|
| **URL** | `GET /api/users/{userId}` |
| **Auth** | `[ADMIN]` |

**Response** `200 OK` — User detail

## 5.7 Update My Profile
| Field | Value |
|-------|-------|
| **URL** | `PUT /api/users/me` |
| **Auth** | auth required |

**Request**
```json
{
  "nickname": "newNickname",
  "phonePersonal": "010-9999-8888",
  "phoneCompany": "02-1234-5678",
  "job": "FREELANCER",
  "companyName": "ATStudio Biz"
}
```

**Response** `200 OK` — Updated profile

**Error Cases**
```json
{ "status": 409, "error": "Conflict", "errorCode": "NICKNAME_DUPLICATED", "message": "이미 사용 중인 닉네임입니다." }
{ "status": 409, "error": "Conflict", "errorCode": "PHONE_ALREADY_REGISTERED", "message": "이미 등록된 전화번호입니다." }
{ "status": 400, "error": "Bad Request", "errorCode": "INVALID_ARGUMENT", "message": "입력값이 올바르지 않습니다. 다시 확인해주세요." }
```

> `userType` is not part of the request body. The backend validates the effective final profile state using the authenticated member's stored `userType`, and omitted fields keep their existing values.

## 5.8 Update User (Admin)
| Field | Value |
|-------|-------|
| **URL** | `PUT /api/users/{userId}` |
| **Auth** | `[ADMIN]` |

**Request**
```json
{
  "role": "ADMIN",
  "isVerified": true
}
```

**Response** `200 OK`

## 5.9 Withdraw (Self)
| Field | Value |
|-------|-------|
| **URL** | `DELETE /api/users/me` |
| **Auth** | auth required |
| **Description** | Soft delete (is_deleted = 1) |

**Request**
```json
{
  "password": "SecureP@ss123"
}
```

**Response** `204 No Content`

## 5.11 Update Password
| Field | Value |
|-------|-------|
| **URL** | `PUT /api/users/me/password` |
| **Auth** | auth required |
| **Description** | Change the currently logged-in user's password. Verifies current password before updating. |

**Request**
```json
{
  "currentPassword": "OldP@ss123",
  "newPassword": "NewP@ss456"
}
```

**Response** `204 No Content`

**Error Cases**
```json
{ "status": 400, "error": "Bad Request", "errorCode": "INVALID_ARGUMENT", "message": "현재 비밀번호가 일치하지 않습니다." }
```

---

# 6. User — Subscription

## 6.1 List Subscription Plans
| Field | Value |
|-------|-------|
| **URL** | `GET /api/subscriptions` |
| **Auth** | `[PUBLIC]` |

**Query Parameters**
```
userType: String (optional, "INDIVIDUAL"|"BUSINESS")
```

**Response** `200 OK`
```json
{
  "dataList": [
    {
      "id": 1,
      "name": "STANDARD",
      "description": "개인 구독 기본 플랜",
      "userType": "INDIVIDUAL",
      "priceMonthly": 9900.00,
      "priceYearly": 99000.00,
      "downloadPerDay": 5,
      "maxWhitelistChannels": 1,
      "isActive": true
    }
  ]
}
```

## 6.1.1 List All Subscription Plans (Admin)
| Field | Value |
|-------|-------|
| **URL** | `GET /api/subscriptions/admin` |
| **Auth** | `admin only` |
| **Description** | Returns ALL subscription plans (including inactive) for admin management |

**Response** `200 OK`
```json
{
  "dataList": [
    {
      "id": 1,
      "name": "STANDARD",
      "description": "개인 구독 기본 플랜",
      "userType": "INDIVIDUAL",
      "priceMonthly": 9900.00,
      "priceYearly": 99000.00,
      "downloadPerDay": 5,
      "maxWhitelistChannels": 1,
      "isActive": true
    }
  ]
}
```

## 6.2 Get Subscription Plan
| Field | Value |
|-------|-------|
| **URL** | `GET /api/subscriptions/{subscriptionId}` |
| **Auth** | `[PUBLIC]` |

**Response** `200 OK` — Subscription plan detail

## 6.3 Subscribe
| Field | Value |
|-------|-------|
| **URL** | `POST /api/user-subscriptions` |
| **Auth** | auth required |
| **Description** | Legacy compatibility subscription path. User-facing payment flow should use §6.3.1 → §6.3.2 instead. Business members require license approval. |

**Request**
```json
{
  "subscriptionId": 1,
  "billingCycle": "MONTHLY"
}
```

**Response** `201 Created`
```json
{
  "id": 1,
  "subscription": { "id": 1, "name": "STANDARD" },
  "billingCycle": "MONTHLY",
  "status": "ACTIVE",
  "startedAt": "2026-02-19",
  "expiresAt": "2026-03-19",
  "createdAt": "2026-02-19T10:00:00"
}
```

**Error Cases**
```json
{ "status": 403, "error": "Forbidden", "errorCode": "COMPANY_CERTIFICATION_REQUIRED", "message": "기업 인증 심사 승인 후 이용 가능합니다." }
```

## 6.3.1 Prepare Subscription Payment
| Field | Value |
|-------|-------|
| **URL** | `POST /api/payments/subscriptions/prepare` |
| **Auth** | auth required |
| **Description** | Creates an internal payment order for SUBSCRIBE or UPGRADE. Provider is selected by server configuration (`MOCK` default, `TOSS` for Toss one-time payment). |

**Request**
```json
{
  "purpose": "SUBSCRIBE",
  "subscriptionId": 1,
  "billingCycle": "MONTHLY"
}
```

**Toss Response Shape**
```json
{
  "orderId": "ATS-20260517-ABC123",
  "provider": "TOSS",
  "purpose": "SUBSCRIBE",
  "amount": 9900,
  "currency": "KRW",
  "expiresAt": "2026-05-17T23:10:00",
  "checkout": {
    "type": "TOSS_WIDGET",
    "clientKey": "test_ck_...",
    "customerKey": "ats_user_1",
    "orderName": "ATStudio STANDARD Subscription",
    "successUrl": "http://localhost:5173/subscriptions/payment/success",
    "failUrl": "http://localhost:5173/subscriptions/payment/fail"
  }
}
```

**Response** `201 Created`
```json
{
  "orderId": "ATS-20260516-ABC123",
  "provider": "MOCK",
  "purpose": "SUBSCRIBE",
  "amount": 9900,
  "currency": "KRW",
  "expiresAt": "2026-05-16T23:10:00",
  "checkout": {
    "type": "MOCK",
    "confirmToken": "mock-ATS-20260516-ABC123"
  }
}
```

## 6.3.2 Confirm Payment
| Field | Value |
|-------|-------|
| **URL** | `POST /api/payments/confirm` |
| **Auth** | auth required |
| **Description** | Confirms a prepared payment order. Subscription creation or upgrade is applied only after successful confirmation. |

**Request**
```json
{
  "orderId": "ATS-20260516-ABC123",
  "amount": 9900,
  "provider": "MOCK",
  "providerToken": "mock-ATS-20260516-ABC123"
}
```

**Toss Request**
```json
{
  "orderId": "ATS-20260517-ABC123",
  "amount": 9900,
  "provider": "TOSS",
  "paymentKey": "toss-payment-key"
}
```

For Toss, the frontend receives `paymentKey`, `orderId`, and `amount` from the Toss success redirect. The backend must use the stored payment order amount as the authoritative amount when calling Toss confirm.

**Response** `200 OK`
```json
{
  "orderId": "ATS-20260516-ABC123",
  "status": "DONE",
  "purpose": "SUBSCRIBE",
  "subscription": {
    "id": 1,
    "subscription": { "id": 1, "name": "STANDARD" },
    "billingCycle": "MONTHLY",
    "status": "ACTIVE",
    "startedAt": "2026-05-16",
    "expiresAt": "2026-06-16"
  }
}
```

**Error Cases**
- `400 Bad Request` — `PAYMENT_AMOUNT_MISMATCH`, `PAYMENT_ORDER_INVALID_STATE`, `PAYMENT_ORDER_EXPIRED`, `PAYMENT_CONFIRM_FAILED`
- `403 Forbidden` — `RESOURCE_NOT_ACCESS` when the authenticated user does not own the order
- `404 Not Found` — `PAYMENT_ORDER_NOT_FOUND`
- `400 Bad Request` — `PAYMENT_PROVIDER_NOT_CONFIGURED` when Toss provider is selected without required environment variables

## 6.3.3 Cancel or Fail Payment
| Field | Value |
|-------|-------|
| **URL** | `POST /api/payments/cancel` |
| **Auth** | auth required |
| **Description** | Closes a prepared payment order as CANCELLED or FAILED without mutating subscription state. Used by Mock controls and Toss fail redirect handling. |

**Request**
```json
{
  "orderId": "ATS-20260516-ABC123",
  "status": "CANCELLED",
  "reason": "User cancelled checkout"
}
```

**Response** `200 OK`
```json
{
  "orderId": "ATS-20260516-ABC123",
  "status": "CANCELLED",
  "purpose": "SUBSCRIBE"
}
```

## 6.3.4 Prepare Billing Agreement
| Field | Value |
|-------|-------|
| **URL** | `POST /api/payments/billing-agreements/prepare` |
| **Auth** | auth required |
| **Description** | Creates an internal billing agreement registration order and returns Toss billing-auth metadata. Subscription activation does not occur at prepare time. |

**Request**
```json
{
  "subscriptionId": 1,
  "billingCycle": "MONTHLY"
}
```

**Response** `201 Created`
```json
{
  "orderId": "ATS-BILL-20260518-ABC123",
  "provider": "TOSS_BILLING",
  "purpose": "BILLING_AGREEMENT",
  "amount": 9900,
  "currency": "KRW",
  "expiresAt": "2026-05-18T23:10:00",
  "checkout": {
    "type": "TOSS_BILLING_WIDGET",
    "clientKey": "test_ck_...",
    "customerKey": "ats_user_1_xxxxx",
    "method": "CARD",
    "successUrl": "http://localhost:5173/subscriptions/billing/success",
    "failUrl": "http://localhost:5173/subscriptions/billing/fail"
  }
}
```

## 6.3.5 Confirm Billing Agreement
| Field | Value |
|-------|-------|
| **URL** | `POST /api/payments/billing-agreements/confirm` |
| **Auth** | auth required |
| **Description** | Exchanges Toss billing `authKey` for a server-side billing key, stores it encrypted, immediately performs the first subscription charge, then activates the subscription only after that charge succeeds. |

**Request**
```json
{
  "orderId": "ATS-BILL-20260518-ABC123",
  "amount": 9900,
  "customerKey": "ats_user_1_xxxxx",
  "authKey": "toss-auth-key"
}
```

**Response** `200 OK`
```json
{
  "billingAgreement": {
    "id": 10,
    "provider": "TOSS_BILLING",
    "status": "ACTIVE",
    "payMethod": "CARD",
    "maskedMethod": "****1234",
    "nextBillingAt": "2026-06-18"
  },
  "subscription": {
    "id": 1,
    "subscription": { "id": 1, "name": "STANDARD" },
    "billingCycle": "MONTHLY",
    "status": "ACTIVE",
    "startedAt": "2026-05-18",
    "expiresAt": "2026-06-18"
  }
}
```

**Error Cases**
- `400 Bad Request` — `BILLING_AGREEMENT_INVALID_STATE`, `BILLING_AGREEMENT_CONFIRM_FAILED`, `PAYMENT_AMOUNT_MISMATCH`, `PAYMENT_ORDER_EXPIRED`
- `403 Forbidden` — `RESOURCE_NOT_ACCESS` when the authenticated user does not own the order
- `404 Not Found` — `PAYMENT_ORDER_NOT_FOUND`, `BILLING_AGREEMENT_NOT_FOUND`

## 6.3.6 My Billing Agreement
| Field | Value |
|-------|-------|
| **URL** | `GET /api/payments/billing-agreements/me` |
| **Auth** | auth required |
| **Description** | Returns the current user's billing agreement status for subscription management. Raw billing keys are never returned. |

**Response** `200 OK`
```json
{
  "id": 10,
  "provider": "TOSS_BILLING",
  "status": "ACTIVE",
  "payMethod": "CARD",
  "maskedMethod": "****1234",
  "nextBillingAt": "2026-06-18",
  "lastChargedAt": "2026-05-18T22:00:00",
  "failureCount": 0,
  "cancelledAt": null
}
```

## 6.3.7 Cancel My Billing Agreement
| Field | Value |
|-------|-------|
| **URL** | `DELETE /api/payments/billing-agreements/me` |
| **Auth** | auth required |
| **Description** | Cancels future automatic renewal. Already-paid subscription access remains available until `expiresAt`. |

**Response** `200 OK`
```json
{
  "id": 10,
  "provider": "TOSS_BILLING",
  "status": "CANCELLED",
  "payMethod": "CARD",
  "maskedMethod": "****1234",
  "nextBillingAt": null,
  "failureCount": 0,
  "cancelledAt": "2026-05-18T22:10:00"
}
```

## 6.3.8 Payment Operations Candidates (Not Implemented)

These endpoints are design candidates for the next operations phase. They are not part of the current implemented API count.

| Candidate | Purpose | Notes |
|---|---|---|
| `GET /api/admin/payments/orders` | Search payment attempts by user, status, provider, purpose, date range | Read-only support/audit view |
| `GET /api/admin/payments/orders/{orderId}` | Inspect one payment order with sanitized provider metadata | No raw provider payload or secret values |
| `GET /api/admin/payments/billing-agreements` | List billing agreements by status and next billing date | Show masked method and failure count only |
| `GET /api/admin/payments/subscription-payments` | Search finalized subscription payment records | No refund/settlement mutation in this phase |

## 6.4 My Subscription
| Field | Value |
|-------|-------|
| **URL** | `GET /api/user-subscriptions/me` |
| **Auth** | auth required |

**Response** `200 OK` — My current subscription status

**Error Cases**
- `403 Forbidden` — `NO_ACTIVE_SUBSCRIPTION` when the member has no current service-enabled subscription (neither `ACTIVE` nor `CANCELLED` within grace period)

## 6.5 List User Subscriptions (Admin)
| Field | Value |
|-------|-------|
| **URL** | `GET /api/user-subscriptions` |
| **Auth** | `[ADMIN]` |

**Response** `200 OK` — Pagination + full subscription list

## 6.6 Get User Subscription Detail (Admin)
| Field | Value |
|-------|-------|
| **URL** | `GET /api/user-subscriptions/{userSubscriptionId}` |
| **Auth** | `[ADMIN]` |

**Response** `200 OK`

## 6.7 Change My Subscription (Upgrade/Downgrade)
| Field | Value |
|-------|-------|
| **URL** | `PUT /api/user-subscriptions/me` |
| **Auth** | auth required |
| **Description** | Change plan or billing cycle. Behavior differs by change type: **UPGRADE** is applied immediately with a prorated charge; **DOWNGRADE** is saved as pending (`pendingSubscriptionId`, `pendingBillingCycle`) and takes effect after the current period expires. Response includes `changeType` to indicate which branch was taken. |

**Request**
```json
{
  "subscriptionId": 2,
  "billingCycle": "YEARLY"
}
```

**Response** `200 OK`
```json
{
  "subscription": { "id": 2, "name": "DELUXE" },
  "billingCycle": "YEARLY",
  "status": "ACTIVE",
  "changeType": "UPGRADE",
  "proratedAmount": 15000.00,
  "startedAt": "2026-02-19",
  "expiresAt": "2027-02-19"
}
```

> - `changeType`: `"UPGRADE"` — new plan applied immediately, `proratedAmount` charged.
> - `changeType`: `"DOWNGRADE"` — pending values (`pendingSubscriptionId`, `pendingBillingCycle`) stored; current plan remains active until `expiresAt`; new plan activates automatically after expiry.

## 6.8 Update User Subscription (Admin)
| Field | Value |
|-------|-------|
| **URL** | `PUT /api/user-subscriptions/{userSubscriptionId}` |
| **Auth** | `[ADMIN]` |

**Response** `200 OK`

## 6.9 Delete/Cancel User Subscription (Admin)
| Field | Value |
|-------|-------|
| **URL** | `DELETE /api/user-subscriptions/{userSubscriptionId}` |
| **Auth** | `[ADMIN]` |

**Response** `204 No Content`

## 6.10 Cancel My Subscription
| Field | Value |
|-------|-------|
| **URL** | `DELETE /api/user-subscriptions/me` |
| **Auth** | auth required |
| **Description** | Member cancels their own active subscription. status가 CANCELLED로 변경되나, expiresAt까지 서비스 이용 가능. expiresAt 이후 자동 만료. |

**Response** `204 No Content`

**Error**
```json
{ "status": 404, "error": "Not Found", "errorCode": "SUBSCRIPTION_NOT_FOUND", "message": "구독 정보를 찾을 수 없습니다." }
```

---

# 7. User — License

> Licenses are auto-issued on download. Re-downloading the same track retains the existing license (duplicate prevention).

## 7.1 My Licenses
| Field | Value |
|-------|-------|
| **URL** | `GET /api/licenses/me` |
| **Auth** | auth required |

**Response** `200 OK`
```json
{
  "dataList": [
    {
      "id": 1,
      "track": { "id": 10, "title": "Summer Vibes" },
      "licenseCode": "a1b2c3d4-e5f6-...",
      "issuedAt": "2026-02-19T10:00:00"
    }
  ],
  "pageInfo": { "page": 1, "size": 20, "total": 5, "start": 1, "end": 1, "prev": false, "next": false }
}
```

## 7.2 User's Licenses (Admin)
| Field | Value |
|-------|-------|
| **URL** | `GET /api/users/{userId}/licenses` |
| **Auth** | `[ADMIN]` |

**Response** `200 OK` — Same format as 7.1

## 7.3 My License Detail
| Field | Value |
|-------|-------|
| **URL** | `GET /api/licenses/{licenseId}` |
| **Auth** | auth required (owner only) |

**Response** `200 OK`
```json
{
  "id": 1,
  "track": { "id": 10, "title": "Summer Vibes", "bpm": 120, "tonality": "C" },
  "licenseCode": "a1b2c3d4-e5f6-...",
  "issuedAt": "2026-02-19T10:00:00",
  "user": { "id": 1, "nickname": "creator01" }
}
```

## 7.4 User's License Detail (Admin)
| Field | Value |
|-------|-------|
| **URL** | `GET /api/users/{userId}/licenses/{licenseId}` |
| **Auth** | `[ADMIN]` |

**Response** `200 OK` — Same format as 7.3

---

# 8. User — Question (Inquiry/Answer)

## 8.1 Create Inquiry
| Field | Value |
|-------|-------|
| **URL** | `POST /api/questions` |
| **Auth** | auth required |

**Request** (multipart/form-data)
```
title: String (required, max 200)
content: String (required)
category: String (required, "DOWNLOAD"|"PAYMENT"|"COPYRIGHT"|"PRODUCTION"|"OTHER")
isPublic: Boolean (required)
attachments: List<File> (optional)
```

**Response** `201 Created`
```json
{
  "id": 1,
  "title": "다운로드가 안 됩니다",
  "category": "DOWNLOAD",
  "isPublic": false,
  "status": "OPEN",
  "attachments": [
    { "id": 1, "originalName": "screenshot.png", "fileSize": 204800 }
  ],
  "createdAt": "2026-02-19T10:00:00"
}
```

## 8.2 Write Answer
| Field | Value |
|-------|-------|
| **URL** | `POST /api/questions/{questionId}/answers` |
| **Auth** | auth required (inquiry owner or ADMIN) |
| **Description** | On admin's first answer, inquiry status changes automatically (OPEN → IN_PROGRESS) |

**Request**
```json
{
  "content": "확인해보겠습니다. 구독 플랜 정보를 알려주시겠어요?"
}
```

**Response** `201 Created`
```json
{
  "id": 1,
  "content": "확인해보겠습니다...",
  "user": { "id": 99, "nickname": "admin", "role": "ADMIN" },
  "createdAt": "2026-02-19T11:00:00"
}
```

## 8.3 List Inquiries
| Field | Value |
|-------|-------|
| **URL** | `GET /api/questions` |
| **Auth** | auth required |
| **Description** | Regular user: public inquiries + my private inquiries. Admin: all |

**Query Parameters**
```
page: Integer (default: 1)
size: Integer (default: 20)
category: String (optional)
status: String (optional, "OPEN"|"IN_PROGRESS"|"RESOLVED"|"CLOSED")
mine: Boolean (optional, if true returns my inquiries only)
```

**Response** `200 OK` — Pagination + inquiry list

## 8.4 Get Inquiry Detail
| Field | Value |
|-------|-------|
| **URL** | `GET /api/questions/{questionId}` |
| **Auth** | auth required (private inquiry: owner + ADMIN only) |

**Response** `200 OK`
```json
{
  "id": 1,
  "title": "다운로드가 안 됩니다",
  "content": "구독 중인데...",
  "category": "DOWNLOAD",
  "isPublic": false,
  "status": "IN_PROGRESS",
  "user": { "id": 1, "nickname": "creator01" },
  "attachments": [
    { "id": 1, "originalName": "screenshot.png", "fileSize": 204800 }
  ],
  "answers": [
    {
      "id": 1,
      "content": "확인해보겠습니다...",
      "user": { "id": 99, "nickname": "admin", "role": "ADMIN" },
      "createdAt": "2026-02-19T11:00:00"
    }
  ],
  "createdAt": "2026-02-19T10:00:00"
}
```

## 8.5 Download Attachment
| Field | Value |
|-------|-------|
| **URL** | `GET /api/questions/{questionId}/attachments/{attachmentId}` |
| **Auth** | auth required (same access as inquiry view: owner + ADMIN) |

**Response** `200 OK` — file download

## 8.6 Change Inquiry Status
| Field | Value |
|-------|-------|
| **URL** | `PUT /api/questions/{questionId}/status` |
| **Auth** | `[ADMIN]` |

**Request**
```json
{
  "status": "RESOLVED"
}
```

**Response** `200 OK`

**Status Flow:**
- OPEN → IN_PROGRESS (auto on admin's first answer) → RESOLVED → CLOSED
- OPEN → CLOSED (admin closes directly)

## 8.7 Delete Inquiry
| Field | Value |
|-------|-------|
| **URL** | `DELETE /api/questions/{questionId}` |
| **Auth** | auth required (owner only, OPEN status only) or `[ADMIN]` |

**Response** `204 No Content`

---

# 9. User — Notice

## 9.1 Create Notice
| Field | Value |
|-------|-------|
| **URL** | `POST /api/notices` |
| **Auth** | `[ADMIN]` |
| **Content-Type** | `multipart/form-data` |

**Request (FormData)**

| Part | Type | Required | Description |
|------|------|----------|-------------|
| title | String | Y | 제목 (max 200) |
| content | String | Y | 내용 |
| isPinned | Boolean | Y | 고정 여부 |
| attachments | File[] | N | 첨부파일 (복수) |

**Response** `201 Created`
```json
{
  "id": 1,
  "title": "서비스 점검 안내",
  "content": "2월 20일 오전 2시~4시 점검 예정입니다.",
  "isPinned": true,
  "viewCount": 0,
  "attachments": [
    { "id": 1, "originalName": "schedule.pdf", "fileSize": 204800 }
  ],
  "createdAt": "2026-02-19T10:00:00"
}
```

## 9.2 List Notices
| Field | Value |
|-------|-------|
| **URL** | `GET /api/notices` |
| **Auth** | `[PUBLIC]` |

**Query Parameters**
```
page: Integer (default: 1)
size: Integer (default: 20)
sort: String (optional, "latest"|"views", default: "latest")
```

> Pinned notices always appear first regardless of sort order.

**Response** `200 OK`
```json
{
  "dataList": [
    {
      "id": 1,
      "title": "서비스 점검 안내",
      "isPinned": true,
      "viewCount": 245,
      "createdAt": "2026-02-19T10:00:00"
    }
  ],
  "pageInfo": { "page": 1, "size": 20, "total": 10, "start": 1, "end": 1, "prev": false, "next": false }
}

## 9.3 Get Notice
| Field | Value |
|-------|-------|
| **URL** | `GET /api/notices/{noticeId}` |
| **Auth** | `[PUBLIC]` |

**Response** `200 OK`
```json
{
  "id": 1,
  "title": "서비스 점검 안내",
  "content": "2월 20일 오전 2시~4시 점검 예정입니다.",
  "isPinned": true,
  "viewCount": 245,
  "attachments": [
    { "id": 1, "originalName": "schedule.pdf", "fileSize": 204800 }
  ],
  "createdAt": "2026-02-19T10:00:00",
  "updatedAt": "2026-02-19T10:00:00"
}
```

> `viewCount` is incremented on each call to this endpoint.

## 9.4 Update Notice
| Field | Value |
|-------|-------|
| **URL** | `PUT /api/notices/{noticeId}` |
| **Auth** | `[ADMIN]` |

**Request**
```json
{
  "title": "서비스 점검 안내 (수정)",
  "content": "점검 시간이 변경되었습니다.",
  "isPinned": true
}
```

**Response** `200 OK`

## 9.5 Delete Notice
| Field | Value |
|-------|-------|
| **URL** | `DELETE /api/notices/{noticeId}` |
| **Auth** | `[ADMIN]` |

**Response** `204 No Content`

> Cascade: 첨부파일(storage + DB) 자동 삭제.

## 9.6 Download Notice Attachment
| Field | Value |
|-------|-------|
| **URL** | `GET /api/notices/{noticeId}/attachments/{attachmentId}` |
| **Auth** | `[PUBLIC]` |

**Response** `200 OK` — `application/octet-stream` binary file download.

**Error Cases**
- `404` — 공지사항 또는 첨부파일 없음.

---

# 10. Likes (Favorites)

## 10.1 Add to Likes
| Field | Value |
|-------|-------|
| **URL** | `POST /api/likes/{trackId}` |
| **Auth** | auth required |

**Response** `201 Created`

**Error Cases**
```json
{ "status": 409, "error": "Conflict", "errorCode": "RESOURCE_DUPLICATE", "message": "이미 좋아요한 트랙입니다." }
```

## 10.2 List Likes
| Field | Value |
|-------|-------|
| **URL** | `GET /api/likes` |
| **Auth** | auth required |

**Response** `200 OK`
```json
{
  "dataList": [
    {
      "trackId": 10,
      "title": "Summer Vibes",
      "bpm": 120,
      "tonality": "C",
      "thumbnail": "/tracks/thumbnail/summer-vibes.jpg",
      "createdAt": "2026-02-19T10:00:00"
    }
  ]
}
```

## 10.3 Remove from Likes
| Field | Value |
|-------|-------|
| **URL** | `DELETE /api/likes/{trackId}` |
| **Auth** | auth required |

**Response** `204 No Content`

**Error Cases**
```json
{ "status": 404, "error": "Not Found", "errorCode": "RESOURCE_NOT_FOUND", "message": "좋아요 목록에 없는 트랙입니다." }
```

## 10.4 Add Album Like
| Field | Value |
|-------|-------|
| **URL** | `POST /api/likes/albums/{albumId}` |
| **Auth** | auth required |

**Response** `201 Created`

**Error Cases**
```json
{ "status": 409, "error": "Conflict", "errorCode": "RESOURCE_DUPLICATE", "message": "이미 좋아요한 앨범입니다." }
```

## 10.5 Remove Album Like
| Field | Value |
|-------|-------|
| **URL** | `DELETE /api/likes/albums/{albumId}` |
| **Auth** | auth required |

**Response** `204 No Content`

**Error Cases**
```json
{ "status": 404, "error": "Not Found", "errorCode": "RESOURCE_NOT_FOUND", "message": "좋아요 목록에 없는 앨범입니다." }
```

## 10.6 List My Album Likes
| Field | Value |
|-------|-------|
| **URL** | `GET /api/likes/albums` |
| **Auth** | auth required |

**Response** `200 OK`
```json
{
  "dataList": [
    {
      "albumId": 3,
      "title": "Summer Collection",
      "description": "Summer-themed shorts music",
      "thumbnailUrl": "/albums/thumbnail/summer.jpg",
      "trackCount": 12,
      "likeCount": 58,
      "createdAt": "2026-02-19T10:00:00"
    }
  ]
}
```

---

# 11. Download Queue

> Collects multiple tracks and the frontend calls individual download APIs sequentially.
> On page exit during download, a `beforeunload` event shows a warning.

## 11.1 Add to Queue
| Field | Value |
|-------|-------|
| **URL** | `POST /api/download-queue/{trackId}` |
| **Auth** | auth required |

**Response** `201 Created`

**Error Cases**
```json
{ "status": 409, "error": "Conflict", "errorCode": "RESOURCE_DUPLICATE", "message": "이미 다운로드 큐에 있는 트랙입니다." }
```

## 11.2 List Queue
| Field | Value |
|-------|-------|
| **URL** | `GET /api/download-queue` |
| **Auth** | auth required |

**Response** `200 OK`
```json
{
  "dataList": [
    {
      "trackId": 10,
      "title": "Summer Vibes",
      "bpm": 120,
      "tonality": "C",
      "thumbnail": "/tracks/thumbnail/summer-vibes.jpg",
      "createdAt": "2026-02-19T10:00:00"
    }
  ]
}
```

## 11.3 Remove from Queue
| Field | Value |
|-------|-------|
| **URL** | `DELETE /api/download-queue/{trackId}` |
| **Auth** | auth required |

**Response** `204 No Content`

**Error Cases**
```json
{ "status": 404, "error": "Not Found", "errorCode": "RESOURCE_NOT_FOUND", "message": "다운로드 큐에 없는 트랙입니다." }
```

## 11.4 Download History
| Field | Value |
|-------|-------|
| **URL** | `GET /api/downloads/history` |
| **Auth** | auth required |
| **Description** | Returns the current member's download history from `track_downloads`. Used by the live "다운로드 기록" page served on the legacy `/download-queue` route. |

**Query Parameters**
```
keyword: String (optional)
sort: String (optional, "latest"|"oldest", default: "latest")
page: Integer (default: 1)
size: Integer (default: 20)
```

**Response** `200 OK`
```json
{
  "dataList": [
    {
      "downloadId": 101,
      "trackId": 10,
      "title": "Summer Vibes",
      "artistName": "creator01",
      "thumbnail": "/tracks/thumbnail/summer-vibes.jpg",
      "bpm": 120,
      "tonality": "C",
      "duration": 95,
      "tags": [{ "id": 1, "name": "Vlog", "type": "GENRE" }],
      "downloadedAt": "2026-04-18T10:00:00"
    }
  ],
  "pageInfo": {
    "page": 1,
    "size": 20,
    "total": 1,
    "start": 1,
    "end": 1,
    "prev": false,
    "next": false
  }
}
```

## 11.5 Download History Track IDs
| Field | Value |
|-------|-------|
| **URL** | `GET /api/downloads/history/track-ids` |
| **Auth** | auth required |
| **Description** | Returns distinct track IDs matching the current download history filter. Used by the "전체 재다운로드" action. |

**Query Parameters**
```
keyword: String (optional)
```

**Response** `200 OK`
```json
{
  "dataList": [10, 11, 15]
}
```

---

# 12. Whitelist Channels

## 12.1 Register Channel
| Field | Value |
|-------|-------|
| **URL** | `POST /api/whitelist-channels` |
| **Auth** | auth required (subscribers only) |

**Request**
```json
{
  "channelUrl": "https://youtube.com/@mychannel",
  "channelName": "My Channel"
}
```

> **channelUrl validation**: Strict URI parsing — host must be exactly `youtube.com` or end with `.youtube.com`. Supports all URL formats — `@handle`, `/channel/UCxxx`, `/c/customname`. Spoofed domains (e.g., `notarealsite-youtube.com`) are rejected.

**Response** `201 Created`

**Error Cases**
```json
{ "status": 403, "error": "Forbidden", "errorCode": "WHITELIST_CHANNEL_LIMIT_EXCEEDED", "message": "채널 등록 한도를 초과했습니다." }
{ "status": 400, "error": "Bad Request", "errorCode": "INVALID_ARGUMENT", "message": "유튜브 채널 URL이 올바르지 않습니다." }
```

## 12.2 My Channel List
| Field | Value |
|-------|-------|
| **URL** | `GET /api/whitelist-channels` |
| **Auth** | auth required |

**Response** `200 OK`
```json
{
  "dataList": [
    {
      "id": 1,
      "channelUrl": "https://youtube.com/@mychannel",
      "channelName": "My Channel",
      "createdAt": "2026-02-19T10:00:00"
    }
  ]
}
```

## 12.3 Update Channel
| Field | Value |
|-------|-------|
| **URL** | `PUT /api/whitelist-channels/{channelId}` |
| **Auth** | auth required (owner only) |

**Request**
```json
{
  "channelUrl": "https://youtube.com/@newchannel",
  "channelName": "New Channel Name"
}
```

> **channelUrl validation**: Same as 12.1 — strict URI parsing, host must be exactly `youtube.com` or end with `.youtube.com`.

**Response** `200 OK`

## 12.4 Delete Channel
| Field | Value |
|-------|-------|
| **URL** | `DELETE /api/whitelist-channels/{channelId}` |
| **Auth** | auth required (owner only) |

**Response** `204 No Content`

---

# 13. Company Certification

## 13.1 Submit Certification Application
| Field | Value |
|-------|-------|
| **URL** | `POST /api/company-certifications` |
| **Auth** | auth required (business members only) |

**Request** (multipart/form-data)
```
documents: List<File> (required)
```

**Response** `201 Created`
```json
{
  "id": 1,
  "status": "PENDING",
  "documentPath": "/uploads/company-docs/1/",
  "createdAt": "2026-02-19T10:00:00"
}
```

## 13.2 My Certification Application Status
| Field | Value |
|-------|-------|
| **URL** | `GET /api/company-certifications/me` |
| **Auth** | auth required (business members) |

**Response** `200 OK`
```json
{
  "id": 1,
  "status": "PENDING",
  "adminNote": null,
  "certificationCode": null,
  "createdAt": "2026-02-19T10:00:00"
}
```

## 13.3 List Certification Applications (Admin)
| Field | Value |
|-------|-------|
| **URL** | `GET /api/company-certifications` |
| **Auth** | `[ADMIN]` |

**Query Parameters**
```
status: String (optional, "PENDING"|"APPROVED"|"REVISION_REQUESTED"|"REJECTED")
page: Integer (default: 1)
size: Integer (default: 20)
```

**Response** `200 OK` — Pagination

## 13.4 Get Certification Application Detail (Admin)
| Field | Value |
|-------|-------|
| **URL** | `GET /api/company-certifications/{certificationId}` |
| **Auth** | `[ADMIN]` |

**Response** `200 OK`

## 13.5 Process Certification Review (Admin)
| Field | Value |
|-------|-------|
| **URL** | `PUT /api/company-certifications/{certificationId}` |
| **Auth** | `[ADMIN]` |
| **Description** | Approve / request revision / reject. On approval, certification_code is auto-generated |

**Request**
```json
{
  "status": "APPROVED",
  "adminNote": "서류 확인 완료"
}
```

**Response** `200 OK`
```json
{
  "id": 1,
  "status": "APPROVED",
  "certificationCode": "BIZ-a1b2c3d4-e5f6-...",
  "approvedAt": "2026-02-19T15:00:00"
}
```

---

# 14. Util (Utility)

## 14.1 Refresh Token
| Field | Value |
|-------|-------|
| **URL** | `POST /api/auth/refresh` |
| **Auth** | `[PUBLIC]` (Refresh Token required) |

**Request**
```json
{
  "refreshToken": "eyJhbGciOi..."
}
```

**Response** `200 OK`
```json
{
  "accessToken": "eyJhbGciOi...(new)",
  "refreshToken": "eyJhbGciOi...(new)",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

**Error Cases**
```json
{ "status": 401, "error": "Unauthorized", "errorCode": "REFRESH_TOKEN_EXPIRED", "message": "리프레시 토큰이 만료되었습니다." }
{ "status": 401, "error": "Unauthorized", "errorCode": "INVALID_TOKEN", "message": "유효하지 않은 토큰입니다." }
```

## 14.2 Email Duplicate Check
| Field | Value |
|-------|-------|
| **URL** | `GET /api/utils/check-email` |
| **Auth** | `[PUBLIC]` |

**Query Parameters**
```
email: String (required)
```

**Response** `200 OK`
```json
{ "available": true }
```

## 14.3 Phone Duplicate Check
| Field | Value |
|-------|-------|
| **URL** | `GET /api/utils/check-phone` |
| **Auth** | `[PUBLIC]` |

**Query Parameters**
```
phone: String (required)
```

**Response** `200 OK`
```json
{ "available": true }
```

## 14.4 Subscription Status Check
| Field | Value |
|-------|-------|
| **URL** | `GET /api/utils/subscription-status` |
| **Auth** | auth required |

**Response** `200 OK`
```json
{
  "hasSubscription": true,
  "planName": "DELUXE",
  "userType": "INDIVIDUAL",
  "downloadPerDay": 20,
  "maxWhitelistChannels": 2
}
```

## 14.5 Download Count Check
| Field | Value |
|-------|-------|
| **URL** | `GET /api/utils/download-count` |
| **Auth** | auth required |

**Response** `200 OK`
```json
{
  "todayDownloads": 3,
  "dailyLimit": 20,
  "remaining": 17,
  "nextResetAt": "2026-03-08T00:00:00"
}
```

> `nextResetAt`: LocalDateTime (ISO-8601) — timestamp when the daily download counter resets.

## 14.6 User Type Check
| Field | Value |
|-------|-------|
| **URL** | `GET /api/utils/user-type` |
| **Auth** | auth required |

**Response** `200 OK`
```json
{
  "userType": "INDIVIDUAL",
  "job": "EDITOR"
}
```

## 14.7 Nickname Duplicate Check
| Field | Value |
|-------|-------|
| **URL** | `GET /api/utils/check-nickname` |
| **Auth** | `[PUBLIC]` |

**Query Parameters**
```
nickname: String (required)
```

**Response** `200 OK`
```json
{ "available": true }
```

## 14.8 Subscription Change Preview
| Field | Value |
|-------|-------|
| **URL** | `GET /api/utils/subscription-change-preview` |
| **Auth** | auth required (subscribers only) |
| **Description** | Preview the financial and scheduling impact of a plan change before committing. Returns whether the change is an UPGRADE or DOWNGRADE, the prorated amount, and effective dates. |

**Query Parameters**
```
subscriptionId: Long (required — target subscription plan ID)
billingCycle: String (required — "MONTHLY" | "YEARLY")
```

**Response** `200 OK`
```json
{
  "changeType": "UPGRADE",
  "proratedAmount": 15000.00,
  "effectiveDate": "2026-03-07",
  "newPlanName": "DELUXE",
  "newBillingCycle": "YEARLY"
}
```

> - `changeType`: `"UPGRADE"` or `"DOWNGRADE"`
> - `proratedAmount`: Charge (UPGRADE) or credit (DOWNGRADE) amount in KRW
> - `effectiveDate`: LocalDate (ISO-8601) — date the new plan takes effect
> - `newPlanName`: Name of the target subscription plan
> - `newBillingCycle`: `"MONTHLY"` or `"YEARLY"`

**Error Cases**
```json
{ "status": 401, "error": "Unauthorized", "message": "인증이 필요합니다." }
{ "status": 400, "error": "Bad Request", "errorCode": "INVALID_ARGUMENT", "message": "잘못된 파라미터입니다." }
{ "status": 404, "error": "Not Found", "errorCode": "SUBSCRIPTION_NOT_FOUND", "message": "구독 정보를 찾을 수 없습니다." }
```

## 14.9 Verify Email

| Field | Value |
|-------|-------|
| **URL** | `GET /api/auth/verify-email` |
| **Auth** | `[PUBLIC]` |
| **Description** | Verifies user email via token sent by email. Link is clicked from verification email. Token valid for 24 hours, single use. |

**Query Parameters**
```
token: String (required — UUID token from email link)
```

**Response** `200 OK`
```json
{ "message": "이메일 인증이 완료되었습니다." }
```

**Error Cases**
```json
{ "status": 400, "error": "Bad Request", "errorCode": "INVALID_TOKEN", "message": "유효하지 않은 인증 링크입니다." }
{ "status": 401, "error": "Unauthorized", "errorCode": "TOKEN_EXPIRED", "message": "인증이 만료되었습니다. 다시 로그인해주세요." }
```

## 14.10 Request Password Reset

| Field | Value |
|-------|-------|
| **URL** | `POST /api/auth/forgot-password` |
| **Auth** | `[PUBLIC]` |
| **Description** | Sends a password reset email. Always returns 200 regardless of email existence (prevents account enumeration). Token valid for 1 hour. |

**Request Body**
```json
{ "email": "user@example.com" }
```

**Response** `200 OK`
```json
{ "message": "비밀번호 재설정 이메일이 발송되었습니다." }
```

## 14.11 Reset Password

| Field | Value |
|-------|-------|
| **URL** | `POST /api/auth/reset-password` |
| **Auth** | `[PUBLIC]` |
| **Description** | Resets password using token from email link. Token is single-use, valid for 1 hour. |

**Request Body**
```json
{
  "token": "uuid-token-from-email",
  "newPassword": "newSecurePassword123"
}
```

**Response** `200 OK`
```json
{ "message": "비밀번호가 재설정되었습니다." }
```

**Error Cases**
```json
{ "status": 400, "error": "Bad Request", "errorCode": "INVALID_TOKEN", "message": "유효하지 않은 인증 링크입니다." }
{ "status": 401, "error": "Unauthorized", "errorCode": "TOKEN_EXPIRED", "message": "인증이 만료되었습니다. 다시 로그인해주세요." }
```

## 14.12 Public Capabilities

| Field | Value |
|-------|-------|
| **URL** | `GET /api/utils/public-capabilities` |
| **Auth** | `[PUBLIC]` |
| **Description** | Returns runtime capability hints used by login/signup/password reset screens. This endpoint describes whether password login is enabled, whether email-based flows are available, and which social providers are configured. |

**Response** `200 OK`
```json
{
  "passwordLoginEnabled": true,
  "emailVerification": {
    "enabled": true,
    "deliveryMode": "REMOTE_SMTP"
  },
  "passwordReset": {
    "enabled": true,
    "deliveryMode": "REMOTE_SMTP"
  },
  "socialLogin": {
    "google": {
      "enabled": true,
      "clientId": "google-client-id",
      "redirectUri": "https://app.example.com/login/social/google"
    },
    "kakao": {
      "enabled": false,
      "clientId": null,
      "redirectUri": null
    },
    "naver": {
      "enabled": false,
      "clientId": null,
      "redirectUri": null
    }
  },
  "testUsersEnabled": false
}
```

> `deliveryMode`: `UNCONFIGURED` | `LOCAL_SMTP` | `REMOTE_SMTP`

### Removed Items

| Original Item | Reason |
|---------------|--------|
| Token issuance | Handled by Login API (5.2) |
| License issuance (manual) | Consolidated into auto-issuance on download |
| Input validation (backend) | Internal Validation logic (Bean Validation), not an API |
| Input validation (frontend) | Frontend code area, not an API |
| Batch download (ZIP) | Replaced with frontend calling individual download APIs sequentially |

---

---

## 15. Album

### 15.1 Create Album
- Method: POST
- URL: /api/albums
- Auth: ADMIN
- Request: multipart/form-data
  - title: string (required)
  - description: string (optional)
  - thumbnail: file (optional)
- Response: 201 Created
  - id, title, description, thumbnailUrl, trackCount, createdAt
- Errors: 400 INVALID_ARGUMENT

### 15.2 List Albums
- Method: GET
- URL: /api/albums
- Auth: none
- Query Parameters:
  - page: Integer (default: 1)
  - size: Integer (default: 20)
  - sort: String (optional, "latest"|"trackCount", default: "latest")
    - "latest": ordered by createdAt DESC
    - "trackCount": ordered by track count DESC (computed in-memory)
- Response: 200 OK
  - dataList: [ { id, title, description, thumbnailUrl, trackCount, likeCount, createdAt } ]

### 15.3 Get Album
- Method: GET
- URL: /api/albums/{id}
- Auth: none
- Response: 200 OK
  - id, title, description, thumbnailUrl, likeCount, tracks: [ { trackId, title, artistName, thumbnailUrl, order } ], createdAt
- Errors: 404 RESOURCE_NOT_FOUND

### 15.4 Update Album
- Method: PUT
- URL: /api/albums/{id}
- Auth: ADMIN
- Request: multipart/form-data (title, description, thumbnail -- all optional)
- Response: 200 OK
  - id, title, description, thumbnailUrl, trackCount, createdAt
- Errors: 404 RESOURCE_NOT_FOUND

### 15.5 Delete Album
- Method: DELETE
- URL: /api/albums/{id}
- Auth: ADMIN
- Response: 204 No Content
- Errors: 404 RESOURCE_NOT_FOUND

### 15.6 Add Track to Album
- Method: POST
- URL: /api/albums/{id}/tracks
- Auth: ADMIN
- Request Body: { "trackId": number }
- Response: 201 Created, AlbumDetailResponse
- Errors: 404 RESOURCE_NOT_FOUND, 409 RESOURCE_DUPLICATE

### 15.7 Remove Track from Album
- Method: DELETE
- URL: /api/albums/{id}/tracks/{trackId}
- Auth: ADMIN
- Response: 204 No Content
- Errors: 404 RESOURCE_NOT_FOUND

### 15.8 Reorder Album Tracks
- Method: PUT
- URL: /api/albums/{id}/tracks
- Auth: ADMIN
- Request Body: { "trackOrders": [ { "trackId": number, "order": number } ] }
- Response: 200 OK, AlbumDetailResponse
- Errors: 404 RESOURCE_NOT_FOUND

---

## 16. Admin Dashboard

### 16.1 Get Dashboard Stats

| Field | Value |
|-------|-------|
| **Method** | `GET` |
| **URL** | `/api/admin/stats` |
| **Auth** | `[ADMIN]` |
| **Description** | Aggregated dashboard statistics for admin overview. |

**Response** `200 OK`
```json
{
  "message": "Dashboard stats retrieved",
  "data": {
    "totalUsers": 150,
    "totalTracks": 45,
    "totalSubscribers": 30,
    "recentUsers": [
      {
        "id": 1,
        "nickname": "user01",
        "email": "user@example.com",
        "userType": "INDIVIDUAL",
        "role": "USER",
        "isVerified": true,
        "createdAt": "2026-02-19T10:00:00"
      }
    ]
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| totalUsers | long | Non-deleted user count |
| totalTracks | long | Active (published) track count |
| totalSubscribers | long | Users with ACTIVE subscription status |
| recentUsers | List | Latest 5 users (same format as §5.10 admin user list item) |

---

## 17. Site Settings

### 17.1 Get Site Setting

| Field | Value |
|-------|-------|
| **URL** | `GET /api/settings/{key}` |
| **Auth** | `[PUBLIC]` |
| **Description** | Retrieve a single site configuration value by key. Used by frontend to load dynamic content (e.g., company certification guide text). |

**Path Parameters**
```
key: String (required) — setting key name
```

**Response** `200 OK`
```json
{
  "message": "Success",
  "data": {
    "key": "company_cert_guide",
    "value": "Please submit the following documents..."
  }
}
```

**Errors**
- `404 Not Found` — `RESOURCE_NOT_FOUND`: key does not exist

---

### 17.2 Update Site Setting (Admin)

| Field | Value |
|-------|-------|
| **URL** | `PUT /api/admin/settings/{key}` |
| **Auth** | `[ADMIN]` |
| **Description** | Create or update a site configuration value by key (upsert). |

**Path Parameters**
```
key: String (required) — setting key name
```

**Request Body** (application/json)
```json
{
  "value": "Updated guide text content..."
}
```

**Validation**
- `value`: required, max 5000 characters

**Response** `200 OK`
```json
{
  "message": "Success",
  "data": {
    "key": "company_cert_guide",
    "value": "Updated guide text content..."
  }
}
```

**Errors**
- `400 Bad Request` — `INVALID_ARGUMENT`: value exceeds max length or is blank

---

# Full API Summary (114)

| # | Section | API Count |
|---|---------|-----------|
| 1 | Track | 9 |
| 2 | Tag | 5 |
| 3 | Playlist | 9 |
| 4 | Play History | 3 |
| 5 | User Info | 11 |
| 6 | Subscription | 18 |
| 7 | License | 4 |
| 8 | Question (Inquiry/Answer) | 7 |
| 9 | Notice | 6 |
| 10 | Likes (Favorites) | 6 |
| 11 | Download Queue / History | 5 |
| 12 | Whitelist Channels | 4 |
| 13 | Company Certification | 5 |
| 14 | Utility / Auth | 12 |
| 15 | Album | 8 |
| 16 | Admin Dashboard | 1 |
| 17 | Site Settings | 2 |
| | **Total** | **114** |
