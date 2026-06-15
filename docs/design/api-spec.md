# ATStudio API Specification v17 (Confirmed)

> **Status**: 17th confirmed — whitelist channel request/admin operation workflow
> **Base**: v16 + 2026-06-03 whitelist channel workflow patch
> **Date**: 2026-06-03

---

## v16 → v17 Change History

| # | Item | Decision |
|---|------|----------|
| AH1 | §12 whitelist user workflow | Reworked whitelist from immediate subscriber-only registration to saved channel drafts plus explicit registration request. Added request and primary-channel endpoints. |
| AH2 | §12 admin whitelist operations | Added admin list/status update/CSV export APIs for manual external YouTube/agency registration workflow. CSV includes `userEmail`. |
| AH3 | Full API Summary | Updated total count from 139 → 145. |

---

## v15 → v16 Change History

| # | Item | Decision |
|---|------|----------|
| AG1 | §1.2 public track search | Added `usage` tag filter and expanded `keyword` to search both track title and `USAGE` guide tag names. |
| AG2 | §2 tag contract | Added `USAGE` as an allowed tag type for visible guide hashtags such as `#쇼츠용` and `#유튜브용`. |
| AG3 | Full API Summary | Endpoint count unchanged at 139. |

---

## v14 → v15 Change History

| # | Item | Decision |
|---|------|----------|
| AF1 | §6.3.8 admin settlement import/reconciliation workflow | Added CSV/manual settlement import, settlement list, missing-provider evidence scan, and ignore APIs. |
| AF2 | Settlement boundary | Settlement rows are accounting evidence and operator review records. They do not mutate subscription access, payment status, refund status, billing agreements, or provider state. |
| AF3 | Full API Summary | Updated total count from 135 → 139 |

---

## v13 → v14 Change History

| # | Item | Decision |
|---|------|----------|
| AE1 | §6.3.8 admin entitlement correction workflow | Added refund-linked entitlement correction preview, request, approve, execute, list, and detail APIs. |
| AE2 | Refund/access boundary | Provider refund remains separate from local subscription access mutation. Entitlement correction applies only explicit admin-provided target state and does not infer previous plan rollback automatically. |
| AE3 | Full API Summary | Updated total count from 129 → 135 |

---

## v12 → v13 Change History

| # | Item | Decision |
|---|------|----------|
| AD1 | §6.3.8 admin refund workflow | Added refund preview, request, approve, execute, list, and detail APIs for audited admin refund operations. |
| AD2 | Refund execution boundary | Provider refund/cancel execution is admin-only and uses the persisted refund ledger plus provider idempotency key; it does not automatically mutate subscription entitlement. |
| AD3 | Full API Summary | Updated total count from 123 → 129 |

---

## v11 → v12 Change History

| # | Item | Decision |
|---|------|----------|
| AC1 | §6.3.8 payment receipt evidence | Added admin read-only API for persisted payment receipt evidence captured from successful provider charge payloads. |
| AC2 | §6.3.8 payment operation audit logs | Added admin read-only API for payment operation audit logs. Reconciliation incident status changes now write an audit row. |
| AC3 | Full API Summary | Updated total count from 121 → 123 |

---

## v10 → v11 Change History

| # | Item | Decision |
|---|------|----------|
| AB1 | §6.7 change semantics | Added `SCHEDULED_CHANGE` and `NO_CHANGE`; pending plan/cycle changes can be overwritten, and selecting the current plan/current cycle clears pending changes. |
| AB2 | §6.10/§6.11 cancel/reactivate | Subscription cancellation now means stop next renewal while keeping paid access; added reactivation before expiresAt using the stored billing key. |
| AB3 | §14.8 preview semantics | Preview now returns `UPGRADE`, `SCHEDULED_CHANGE`, or `NO_CHANGE`. |
| AB4 | Full API Summary | Updated total count from 117 → 118 |
| AB5 | §6.3.4/§6.3.5/§6.7 billing recovery | Active users with an expired/removed billing key can re-register a payment method through `BILLING_AGREEMENT` orders; removed provider billing keys return `BILLING_AGREEMENT_REAUTH_REQUIRED`. |
| AB6 | §6.3.7/§6.7 cycle-only pending UX | Provider-level billing-agreement cancel is separated from user-facing subscription cancel; upgrade plus next-cycle changes must show the upgraded plan as active and only the billing cycle as pending. |
| AB7 | §6.3.8 admin reconciliation | Added read-only admin local/provider payment reconciliation endpoint. |
| AB8 | Full API Summary | Updated total count from 118 → 119 |
| AB9 | §6.3.8 reconciliation incidents | Added admin APIs for persisted reconciliation incident list and status workflow; updated total count from 119 → 121 |

---

## v9 → v10 Change History

| # | Item | Decision |
|---|------|----------|
| AA1 | §6.3.1/§6.3.2 payment policy | One-time `SUBSCRIBE`/`UPGRADE` subscription prepare/confirm is blocked in backend; new subscription checkout uses billing agreement APIs only |
| AA2 | §6.3.4 billing callback URLs | Default Toss billing auth callbacks moved to `/subscriptions/checkout/success` and `/subscriptions/checkout/fail` |
| AA3 | §6.3.8 operations APIs | Added implemented read-only admin payment order, billing agreement, and subscription payment list APIs |
| AA4 | Full API Summary | Updated total count from 114 → 117 |

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
| Z10 | §6.3/§6.7 payment policy | Subscription user flow is recurring-first; upgrade uses active billing agreement charge through §6.7, not one-time Toss Widget checkout |
| Z11 | §6.7/§14.8 payment preview | Upgrade charge amount is whole KRW; preview exposes nextBillingDate and nextBillingAmount |

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
keyword: String (optional, track title or USAGE guide tag name search)
genre: String (optional, genre tag filter)
mood: String (optional, mood tag filter)
instrument: String (optional, instrument tag filter)
usage: String (optional, usage guide tag filter)
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
        { "id": 1, "name": "Happy", "type": "MOOD" },
        { "id": 69, "name": "쇼츠용", "type": "USAGE" }
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
    { "id": 5, "name": "Pop", "type": "GENRE" },
    { "id": 69, "name": "쇼츠용", "type": "USAGE" }
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
type: String (optional, "MOOD"|"GENRE"|"INSTRUMENT"|"USAGE")
```

**Response** `200 OK`
```json
{
  "dataList": [
    { "id": 1, "name": "Happy", "type": "MOOD", "createdAt": "2026-02-19T10:00:00" },
    { "id": 2, "name": "Pop", "type": "GENRE", "createdAt": "2026-02-19T10:00:00" },
    { "id": 69, "name": "쇼츠용", "type": "USAGE", "createdAt": "2026-06-02T10:00:00" }
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
usage: String (optional, comma-separated usage guide tag names)
bpmMin: Integer (optional)
bpmMax: Integer (optional)
```

**Response** `200 OK`
```json
{
  "dataList": [
    { "id": 1, "name": "Happy", "type": "MOOD", "createdAt": "2026-02-19T10:00:00" },
    { "id": 2, "name": "Pop", "type": "GENRE", "createdAt": "2026-02-19T10:00:00" },
    { "id": 69, "name": "쇼츠용", "type": "USAGE", "createdAt": "2026-06-02T10:00:00" }
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
{ "status": 409, "error": "Conflict", "errorCode": "PLAYLIST_LIMIT_EXCEEDED", "message": "활성 재생목록은 최대 3개까지 생성할 수 있습니다." }
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
| **Description** | Blocked legacy direct subscription creation path. User-facing subscription purchase must use §6.3.4 → §6.3.5 recurring billing checkout. This endpoint remains only so stale clients fail explicitly instead of creating a mock-style subscription. |

**Request**
```json
{
  "subscriptionId": 1,
  "billingCycle": "MONTHLY"
}
```

**Response** `410 Gone`
```json
{
  "status": 410,
  "error": "Gone",
  "errorCode": "SUBSCRIPTION_CHECKOUT_REQUIRED",
  "message": "Subscription checkout is required."
}
```

**Error Cases**
- `410 Gone` — `SUBSCRIPTION_CHECKOUT_REQUIRED`
- New subscription validation, company-certification checks, billing-key issuance, first charge, and subscription mutation are handled by §6.3.4 → §6.3.5.

## 6.3.1 Prepare Subscription Payment
| Field | Value |
|-------|-------|
| **URL** | `POST /api/payments/subscriptions/prepare` |
| **Auth** | auth required |
| **Description** | Blocked legacy one-time subscription payment preparation. User-facing recurring subscription purchase uses §6.3.4 → §6.3.5. User-facing upgrade uses §6.7 and must not route to this one-time Toss Widget path. |

**Request**
```json
{
  "purpose": "SUBSCRIBE",
  "subscriptionId": 1,
  "billingCycle": "MONTHLY"
}
```

**Response** `400 Bad Request`
```json
{
  "status": 400,
  "error": "Bad Request",
  "errorCode": "INVALID_ARGUMENT",
  "message": "Invalid argument."
}
```

> Compatibility note: the endpoint remains present so stale clients fail explicitly instead of mutating subscriptions. It must not be used by current frontend subscription flows.

## 6.3.2 Confirm Payment
| Field | Value |
|-------|-------|
| **URL** | `POST /api/payments/confirm` |
| **Auth** | auth required |
| **Description** | Confirms non-subscription payment orders only. One-time `SUBSCRIBE` and `UPGRADE` orders are rejected before provider confirmation; recurring subscription creation uses §6.3.5 and upgrades use §6.7. Already-`DONE` legacy orders remain idempotent. |

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

For current subscription flows, the frontend must not call this endpoint. Toss one-time success redirects under `/subscriptions/payment/success` are treated as stale legacy routes.

**Blocked subscription response** `400 Bad Request`
```json
{
  "status": 400,
  "error": "Bad Request",
  "errorCode": "PAYMENT_ORDER_INVALID_STATE",
  "message": "Invalid payment order state."
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
| **Description** | Creates an internal billing agreement registration order and returns Toss billing-auth metadata. For a new subscription, the order purpose is `SUBSCRIBE` and the first-period amount is charged during confirm. For an existing active/grace-period subscription that is re-registering an interrupted, expired, or missing payment method, the order purpose is `BILLING_AGREEMENT` and amount is `0`; subscription activation or plan change does not occur at prepare time. |

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
    "type": "TOSS_BILLING_AUTH",
    "clientKey": "test_ck_...",
    "customerKey": "ats_user_1_xxxxx",
    "method": "CARD",
    "successUrl": "http://localhost:5173/subscriptions/checkout/success",
    "failUrl": "http://localhost:5173/subscriptions/checkout/fail"
  }
}
```

For payment-method re-registration on an existing active/grace-period subscription, `purpose` is `BILLING_AGREEMENT` and `amount` is `0`.

## 6.3.5 Confirm Billing Agreement
| Field | Value |
|-------|-------|
| **URL** | `POST /api/payments/billing-agreements/confirm` |
| **Auth** | auth required |
| **Description** | Exchanges Toss billing `authKey` for a server-side billing key and stores it encrypted. For `SUBSCRIBE` orders, the backend immediately performs the first subscription charge and activates the subscription only after that charge succeeds. For `BILLING_AGREEMENT` orders, the backend updates the stored payment method only, keeps the current subscription unchanged, and sets the next billing date to the current subscription `expiresAt`. |

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
- `409 Conflict` — `SUBSCRIPTION_ALREADY_EXISTS` when an active subscription user attempts a new-subscription checkout for a different plan/cycle instead of payment-method re-registration
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
| **Description** | Provider-level billing agreement cancellation endpoint. This is not the primary user-facing subscription cancel UX. The user-facing stop-renewal path is §6.10 `DELETE /api/user-subscriptions/me`, which keeps the encrypted billing key for possible reactivation before `expiresAt`. This endpoint deletes/cancels the provider billing key when one exists, clears local issued-key display fields, marks the agreement `CANCELLED`, and also cancels the active subscription if one exists. Reactivation without payment-method re-registration may not be possible after this endpoint clears the issued key. Already-paid subscription access remains available until `expiresAt`. |

**Response** `200 OK`
```json
{
  "id": 10,
  "provider": "TOSS_BILLING",
  "status": "CANCELLED",
  "payMethod": null,
  "maskedMethod": null,
  "nextBillingAt": null,
  "failureCount": 0,
  "cancelledAt": "2026-05-18T22:10:00"
}
```

## 6.3.8 Payment Operations Admin APIs

These endpoints are implemented as admin support/audit views and controlled payment operation APIs. Payment order, billing agreement, subscription payment, receipt evidence, operation audit log, and on-demand reconciliation endpoints are read-only. Reconciliation incident status updates mutate only the incident workflow state and create an audit log row. Refund APIs mutate only the `payment_refunds` ledger until an approved refund is explicitly executed; execution calls the provider cancel API with the persisted idempotency key and does not automatically change subscriptions, billing agreements, or user entitlement. Entitlement correction APIs are a separate admin-only workflow that applies an explicit target subscription state after support approval. Settlement APIs import and compare PG settlement evidence against local payment/refund ledgers, but do not mutate subscriptions, payment status, refund status, billing agreements, or provider state. These endpoints must not expose raw billing keys, auth keys, customer keys, Toss secret keys, raw provider payloads, or raw card data.

| API | Purpose | Notes |
|---|---|---|
| `GET /api/admin/payments/orders` | List payment attempts by latest created date | Read-only; includes status, purpose, provider, amount, sanitized failure code/message |
| `GET /api/admin/payments/billing-agreements` | List billing agreements by latest created date | Shows masked method and failure count only |
| `GET /api/admin/payments/subscription-payments` | List finalized subscription payment records | No refund/settlement mutation in this phase |
| `GET /api/admin/payments/receipts` | List persisted payment receipt evidence | Read-only; stores safe provider receipt URL/key metadata only |
| `GET /api/admin/payments/operation-audit-logs` | List payment operation audit logs | Read-only; includes admin/system action metadata without raw provider payloads |
| `GET /api/admin/payments/refund-preview/{subscriptionPaymentId}` | Preview refundable amount for a finalized subscription payment | Read-only; rejects unsupported provider/status/missing provider payment key |
| `GET /api/admin/payments/settlements` | List settlement reconciliation rows | Optional status/source/base-date filters; support-safe accounting evidence only |
| `POST /api/admin/payments/settlements/import` | Import settlement CSV evidence and reconcile rows | Local accounting ledger mutation only; no provider call |
| `POST /api/admin/payments/settlements/reconcile` | Generate local-payment-without-provider-settlement candidates | Local review row creation only; no provider call |
| `PUT /api/admin/payments/settlements/{settlementId}/ignore` | Mark a settlement row ignored | Local workflow mutation and audit row only |
| `GET /api/admin/payments/refunds` | List refund ledger records | Read-only list of local refund operation records |
| `GET /api/admin/payments/refunds/{refundId}` | Get refund ledger detail | Read-only detail for one local refund operation |
| `POST /api/admin/payments/refunds` | Create a refund request | Local ledger mutation only; provider is not called |
| `POST /api/admin/payments/refunds/{refundId}/approve` | Approve a refund request | Local ledger mutation only; provider is not called |
| `POST /api/admin/payments/refunds/{refundId}/execute` | Execute an approved refund through Toss cancel | Provider money movement; reuses the persisted idempotency key |
| `POST /api/admin/payments/entitlement-correction-preview` | Preview explicit local entitlement correction target | Read-only; requires a succeeded refund record |
| `GET /api/admin/payments/entitlement-corrections` | List entitlement correction ledger records | Read-only list of local access correction operations |
| `GET /api/admin/payments/entitlement-corrections/{correctionId}` | Get entitlement correction detail | Read-only detail with before/target snapshots |
| `POST /api/admin/payments/entitlement-corrections` | Create an entitlement correction request | Local ledger mutation only; no provider call |
| `POST /api/admin/payments/entitlement-corrections/{correctionId}/approve` | Approve an entitlement correction request | Local workflow mutation only |
| `POST /api/admin/payments/entitlement-corrections/{correctionId}/execute` | Execute approved local entitlement correction | Mutates `user_subscriptions` and optionally local `billing_agreements`; no provider billing-key delete call |
| `GET /api/admin/payments/reconciliation` | Run local/provider payment reconciliation | Read-only; returns support-safe mismatch counts and issue records, no raw provider secrets |
| `GET /api/admin/payments/reconciliation-incidents` | List persisted reconciliation incidents | Optional `status` filter; support-safe incident workflow metadata only |
| `PUT /api/admin/payments/reconciliation-incidents/{incidentId}/status` | Update incident workflow status | Mutates incident status/note only; no payment/subscription/provider mutation |

### GET /api/admin/payments/receipts

| Field | Value |
|---|---|
| **URL** | `GET /api/admin/payments/receipts?page=1&size=20` |
| **Auth** | ADMIN |
| **Description** | Lists receipt evidence rows captured after successful subscription charges. This endpoint is read-only and returns support-safe receipt metadata only. It does not expose raw provider payloads, billing keys, auth keys, customer keys, or raw card data. |

**Response** `200 OK`

```json
{
  "dataList": [
    {
      "id": 1,
      "userId": 12,
      "userNickname": "customer12",
      "paymentOrderId": 3001,
      "orderId": "ATS-REN-20260525-ABC123",
      "subscriptionPaymentId": 901,
      "provider": "TOSS_BILLING",
      "type": "PAYMENT_RECEIPT",
      "status": "ISSUED",
      "providerPaymentKey": "payment_key",
      "receiptKey": null,
      "receiptUrl": "https://dashboard.tosspayments.com/receipt/payment_key",
      "issuedAt": "2026-05-25T10:00:00",
      "cancelledAt": null,
      "createdAt": "2026-05-25T10:00:02"
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

### GET /api/admin/payments/operation-audit-logs

| Field | Value |
|---|---|
| **URL** | `GET /api/admin/payments/operation-audit-logs?page=1&size=20` |
| **Auth** | ADMIN |
| **Description** | Lists append-only payment operation audit rows. Current actions include reconciliation incident status updates, system-created receipt evidence rows, admin refund workflow transitions, and admin entitlement correction workflow transitions. This endpoint is read-only. |

**Response** `200 OK`

```json
{
  "dataList": [
    {
      "id": 1,
      "action": "RECONCILIATION_INCIDENT_STATUS_UPDATE",
      "targetType": "RECONCILIATION_INCIDENT",
      "targetId": 10,
      "actorUserId": 99,
      "actorEmail": "admin@test.com",
      "targetUserId": 12,
      "targetUserNickname": "customer12",
      "paymentOrderId": 3001,
      "orderId": "ATS-REN-20260525-ABC123",
      "subscriptionPaymentId": null,
      "reconciliationIncidentId": 10,
      "provider": "TOSS_BILLING",
      "providerTransactionId": "payment_key",
      "beforeStatus": "OPEN",
      "afterStatus": "ACKNOWLEDGED",
      "reasonCode": "PROVIDER_DONE_LOCAL_NOT_FINALIZED",
      "note": "Investigating against Toss dashboard.",
      "createdAt": "2026-05-25T10:30:00"
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

### GET /api/admin/payments/settlements

| Field | Value |
|---|---|
| **URL** | `GET /api/admin/payments/settlements?page=1&size=20&status=MISMATCHED&source=CSV_MANUAL&baseDateFrom=2026-05-01&baseDateTo=2026-05-31` |
| **Auth** | ADMIN |
| **Description** | Lists imported or generated settlement reconciliation rows by latest created date. The endpoint is read-only and returns support-safe settlement evidence only. It does not expose raw provider payloads, raw card data, billing keys, auth keys, customer keys, or provider secrets. |

**Response** `200 OK`

```json
{
  "dataList": [
    {
      "id": 1,
      "source": "CSV_MANUAL",
      "provider": "TOSS_BILLING",
      "status": "MATCHED",
      "orderId": "ATS-REN-20260525-ABC123",
      "providerPaymentKey": "payment_key",
      "providerSettlementId": "settlement_row_1",
      "paymentOrderId": 3001,
      "subscriptionPaymentId": 901,
      "userId": 12,
      "userNickname": "customer12",
      "grossAmount": 29900,
      "refundAmount": 0,
      "feeAmount": 900,
      "vatAmount": 90,
      "netSettlementAmount": 28910,
      "currency": "KRW",
      "settlementBaseDate": "2026-05-25",
      "settlementPayoutDate": "2026-05-31",
      "providerStatus": "PAID_OUT",
      "mismatchReason": null,
      "sourceFileName": "settlements.csv",
      "sourceRowNumber": 2,
      "operatorNote": "May settlement import",
      "ignoredBy": null,
      "ignoredAt": null,
      "reconciledAt": "2026-05-26T10:00:00",
      "createdAt": "2026-05-26T10:00:00"
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

### POST /api/admin/payments/settlements/import

| Field | Value |
|---|---|
| **URL** | `POST /api/admin/payments/settlements/import` |
| **Auth** | ADMIN |
| **Content-Type** | `multipart/form-data` |
| **Description** | Imports a CSV settlement evidence file through the `CSV_MANUAL` source adapter, validates each row, deduplicates rows by deterministic settlement key, and compares imported evidence with local `payment_orders`, `subscription_payments`, and succeeded `payment_refunds`. This endpoint does not call Toss and does not mutate subscription access or payment/refund state. |

**Form Data**

| Field | Required | Description |
|---|---|---|
| `file` | yes | CSV file. Required headers: `provider`, `order_id`, `gross_amount`, `net_settlement_amount`, `settlement_base_date`. |
| `note` | no | Operator note stored on imported rows when provided. |

**Response** `200 OK`

```json
{
  "data": {
    "importBatchKey": "ATS-SETTLE-6f88d2a9a8424f0c",
    "totalRows": 10,
    "importedRows": 9,
    "skippedDuplicateRows": 1,
    "failedRows": 0,
    "statusCounts": {
      "MATCHED": 8,
      "MISMATCHED": 1
    },
    "errors": []
  }
}
```

### POST /api/admin/payments/settlements/reconcile

| Field | Value |
|---|---|
| **URL** | `POST /api/admin/payments/settlements/reconcile` |
| **Auth** | ADMIN |
| **Description** | Scans local finalized subscription payments in the selected created-at range and creates `PROVIDER_SETTLEMENT_NOT_FOUND` review rows when no imported provider settlement evidence exists for the order. This endpoint does not call Toss and does not mutate subscription access or provider state. |

**Request**

```json
{
  "baseDateFrom": "2026-05-01",
  "baseDateTo": "2026-05-31"
}
```

**Response** `200 OK` — returns the same import summary shape as settlement import.

### PUT /api/admin/payments/settlements/{settlementId}/ignore

| Field | Value |
|---|---|
| **URL** | `PUT /api/admin/payments/settlements/{settlementId}/ignore` |
| **Auth** | ADMIN |
| **Description** | Marks a settlement reconciliation row as `IGNORED` with an operator note and writes a payment operation audit row. This endpoint does not delete the row and does not mutate any payment, refund, subscription, billing agreement, or provider state. |

**Request**

```json
{
  "note": "Confirmed as expected fee adjustment."
}
```

**Response** `200 OK` — returns `AdminPaymentSettlementResponse` with `status: "IGNORED"`.

### GET /api/admin/payments/refund-preview/{subscriptionPaymentId}

| Field | Value |
|---|---|
| **URL** | `GET /api/admin/payments/refund-preview/{subscriptionPaymentId}` |
| **Auth** | ADMIN |
| **Description** | Previews whether a finalized subscription payment can be refunded and how much remains refundable after existing requested, approved, processing, pending-confirmation, or succeeded refund rows. This endpoint is read-only and must not call Toss. |

**Response** `200 OK`

```json
{
  "data": {
    "subscriptionPaymentId": 901,
    "paymentOrderId": 3001,
    "orderId": "ATS-REN-20260525-ABC123",
    "userId": 12,
    "userNickname": "customer12",
    "provider": "TOSS_BILLING",
    "originalAmount": 29900,
    "alreadyRefundedOrReservedAmount": 10000,
    "refundableAmount": 19900,
    "providerPaymentKey": "payment_key",
    "refundable": true,
    "reason": null
  }
}
```

### GET /api/admin/payments/refunds

| Field | Value |
|---|---|
| **URL** | `GET /api/admin/payments/refunds?page=1&size=20` |
| **Auth** | ADMIN |
| **Description** | Lists local refund ledger records by latest created date. It returns support-safe refund metadata, provider payment key, provider cancel transaction key, status, actor, and failure summary fields only. |

**Response** `200 OK`

```json
{
  "dataList": [
    {
      "id": 1,
      "subscriptionPaymentId": 901,
      "paymentOrderId": 3001,
      "orderId": "ATS-REN-20260525-ABC123",
      "userId": 12,
      "userNickname": "customer12",
      "provider": "TOSS_BILLING",
      "status": "REQUESTED",
      "amount": 19900,
      "currency": "KRW",
      "reasonCode": "CUSTOMER_REQUEST",
      "reasonNote": "Support-approved refund.",
      "idempotencyKey": "ATS-REFUND-8F68B0D6F73A",
      "providerPaymentKey": "payment_key",
      "providerRefundTransactionId": null,
      "failureCode": null,
      "failureMessage": null,
      "requestedById": 99,
      "requestedByEmail": "admin@test.com",
      "approvedById": null,
      "approvedByEmail": null,
      "executedById": null,
      "executedByEmail": null,
      "approvedAt": null,
      "executedAt": null,
      "createdAt": "2026-05-25T11:00:00",
      "updatedAt": "2026-05-25T11:00:00"
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

### GET /api/admin/payments/refunds/{refundId}

| Field | Value |
|---|---|
| **URL** | `GET /api/admin/payments/refunds/{refundId}` |
| **Auth** | ADMIN |
| **Description** | Returns one local refund ledger record. It must not expose raw provider cancel payload, raw card data, or provider secrets. |

**Response** `200 OK`

```json
{
  "data": {
    "id": 1,
    "subscriptionPaymentId": 901,
    "paymentOrderId": 3001,
    "orderId": "ATS-REN-20260525-ABC123",
    "userId": 12,
    "userNickname": "customer12",
    "provider": "TOSS_BILLING",
    "status": "APPROVED",
    "amount": 19900,
    "currency": "KRW",
    "reasonCode": "CUSTOMER_REQUEST",
    "reasonNote": "Support-approved refund.",
    "idempotencyKey": "ATS-REFUND-8F68B0D6F73A",
    "providerPaymentKey": "payment_key",
    "providerRefundTransactionId": null,
    "failureCode": null,
    "failureMessage": null,
    "requestedById": 99,
    "requestedByEmail": "admin@test.com",
    "approvedById": 100,
    "approvedByEmail": "lead@test.com",
    "executedById": null,
    "executedByEmail": null,
    "approvedAt": "2026-05-25T11:05:00",
    "executedAt": null,
    "createdAt": "2026-05-25T11:00:00",
    "updatedAt": "2026-05-25T11:05:00"
  }
}
```

### POST /api/admin/payments/refunds

| Field | Value |
|---|---|
| **URL** | `POST /api/admin/payments/refunds` |
| **Auth** | ADMIN |
| **Description** | Creates a local refund request with a stable idempotency key. This endpoint does not call Toss and does not mutate subscription entitlement. The requested amount must be at least 1 KRW and must not exceed the remaining refundable amount. |

**Request**

```json
{
  "subscriptionPaymentId": 901,
  "amount": 19900,
  "reasonCode": "CUSTOMER_REQUEST",
  "reasonNote": "Support-approved refund."
}
```

**Response** `200 OK` — returns `AdminPaymentRefundResponse` with `status: "REQUESTED"`.

### POST /api/admin/payments/refunds/{refundId}/approve

| Field | Value |
|---|---|
| **URL** | `POST /api/admin/payments/refunds/{refundId}/approve` |
| **Auth** | ADMIN |
| **Description** | Approves a `REQUESTED` refund request and writes a payment operation audit log row. This endpoint does not call Toss. |

**Request**

```json
{
  "note": "Verified against customer support ticket."
}
```

**Response** `200 OK` — returns `AdminPaymentRefundResponse` with `status: "APPROVED"`.

### POST /api/admin/payments/refunds/{refundId}/execute

| Field | Value |
|---|---|
| **URL** | `POST /api/admin/payments/refunds/{refundId}/execute` |
| **Auth** | ADMIN |
| **Description** | Executes an `APPROVED` or `PENDING_PROVIDER_CONFIRMATION` refund through the configured provider. For Toss billing, the server calls `POST /v1/payments/{paymentKey}/cancel` with the persisted idempotency key. Success stores the provider cancel transaction key and marks the refund `SUCCEEDED`. Ambiguous provider/network errors mark the refund `PENDING_PROVIDER_CONFIRMATION`; deterministic provider failures mark it `FAILED`. This endpoint does not automatically mutate subscription entitlement. |

**Request**

```json
{
  "note": "Executing after approval."
}
```

**Response** `200 OK` — returns `AdminPaymentRefundResponse` with `status: "SUCCEEDED"`, `FAILED`, or `PENDING_PROVIDER_CONFIRMATION`.

### POST /api/admin/payments/entitlement-correction-preview

| Field | Value |
|---|---|
| **URL** | `POST /api/admin/payments/entitlement-correction-preview` |
| **Auth** | ADMIN |
| **Description** | Previews whether a succeeded refund can receive a local entitlement correction. The request must explicitly provide the target subscription plan, billing cycle, status, expiration date, pending-change clearing preference, and local billing-agreement cancellation preference. This endpoint is read-only and must not call Toss. |

**Request**

```json
{
  "paymentRefundId": 1,
  "targetSubscriptionId": 1,
  "targetBillingCycle": "MONTHLY",
  "targetStatus": "EXPIRED",
  "targetExpiresAt": "2026-05-25",
  "clearPendingChange": true,
  "cancelBillingAgreement": true,
  "reasonNote": "Full refund entitlement correction."
}
```

**Response** `200 OK`

```json
{
  "data": {
    "paymentRefundId": 1,
    "refundStatus": "SUCCEEDED",
    "userId": 12,
    "userNickname": "customer12",
    "userSubscriptionId": 20,
    "currentSubscriptionId": 3,
    "currentPlanName": "PREMIUM",
    "currentBillingCycle": "YEARLY",
    "currentStatus": "ACTIVE",
    "currentExpiresAt": "2027-05-25",
    "currentPendingSubscriptionId": 1,
    "currentPendingPlanName": "STANDARD",
    "currentPendingBillingCycle": "MONTHLY",
    "targetSubscriptionId": 1,
    "targetPlanName": "STANDARD",
    "targetBillingCycle": "MONTHLY",
    "targetStatus": "EXPIRED",
    "targetExpiresAt": "2026-05-25",
    "clearPendingChange": true,
    "cancelBillingAgreement": true,
    "currentBillingAgreementStatus": "ACTIVE",
    "targetBillingAgreementStatus": "CANCELLED",
    "executable": true,
    "reason": null
  }
}
```

### GET /api/admin/payments/entitlement-corrections

| Field | Value |
|---|---|
| **URL** | `GET /api/admin/payments/entitlement-corrections?page=1&size=20` |
| **Auth** | ADMIN |
| **Description** | Lists local entitlement correction ledger records by latest created date. The endpoint is read-only and returns before/target snapshots plus actor metadata. |

**Response** `200 OK` — paginated list of `AdminPaymentEntitlementCorrectionResponse`.

### GET /api/admin/payments/entitlement-corrections/{correctionId}

| Field | Value |
|---|---|
| **URL** | `GET /api/admin/payments/entitlement-corrections/{correctionId}` |
| **Auth** | ADMIN |
| **Description** | Returns one entitlement correction ledger record. It must not expose raw provider payload, billing key, auth key, customer key, or card data. |

**Response** `200 OK`

```json
{
  "data": {
    "id": 1,
    "paymentRefundId": 1,
    "subscriptionPaymentId": 901,
    "paymentOrderId": 3001,
    "orderId": "ATS-REN-20260525-ABC123",
    "userSubscriptionId": 20,
    "userId": 12,
    "userNickname": "customer12",
    "provider": "TOSS_BILLING",
    "status": "REQUESTED",
    "action": "SET_SUBSCRIPTION_STATE",
    "beforeSubscriptionId": 3,
    "beforePlanName": "PREMIUM",
    "beforeBillingCycle": "YEARLY",
    "beforeStatus": "ACTIVE",
    "beforeExpiresAt": "2027-05-25",
    "beforePendingSubscriptionId": 1,
    "beforePendingPlanName": "STANDARD",
    "beforePendingBillingCycle": "MONTHLY",
    "targetSubscriptionId": 1,
    "targetPlanName": "STANDARD",
    "targetBillingCycle": "MONTHLY",
    "targetStatus": "EXPIRED",
    "targetExpiresAt": "2026-05-25",
    "clearPendingChange": true,
    "cancelBillingAgreement": true,
    "beforeBillingAgreementStatus": "ACTIVE",
    "afterBillingAgreementStatus": "ACTIVE",
    "reasonNote": "Full refund entitlement correction.",
    "failureCode": null,
    "failureMessage": null,
    "requestedById": 99,
    "requestedByEmail": "admin@test.com",
    "approvedById": null,
    "approvedByEmail": null,
    "executedById": null,
    "executedByEmail": null,
    "approvedAt": null,
    "executedAt": null,
    "createdAt": "2026-05-25T12:00:00",
    "updatedAt": "2026-05-25T12:00:00"
  }
}
```

### POST /api/admin/payments/entitlement-corrections

| Field | Value |
|---|---|
| **URL** | `POST /api/admin/payments/entitlement-corrections` |
| **Auth** | ADMIN |
| **Description** | Creates a local entitlement correction request linked to a succeeded refund. It stores the current subscription snapshot and the explicit target state. It does not call Toss and does not mutate user access yet. |

**Request** — same body as `POST /api/admin/payments/entitlement-correction-preview`.

**Response** `200 OK` — returns `AdminPaymentEntitlementCorrectionResponse` with `status: "REQUESTED"`.

### POST /api/admin/payments/entitlement-corrections/{correctionId}/approve

| Field | Value |
|---|---|
| **URL** | `POST /api/admin/payments/entitlement-corrections/{correctionId}/approve` |
| **Auth** | ADMIN |
| **Description** | Approves a `REQUESTED` entitlement correction and writes a payment operation audit log row. It does not mutate user access yet. |

**Request**

```json
{
  "note": "Support ticket verified."
}
```

**Response** `200 OK` — returns `AdminPaymentEntitlementCorrectionResponse` with `status: "APPROVED"`.

### POST /api/admin/payments/entitlement-corrections/{correctionId}/execute

| Field | Value |
|---|---|
| **URL** | `POST /api/admin/payments/entitlement-corrections/{correctionId}/execute` |
| **Auth** | ADMIN |
| **Description** | Executes an `APPROVED` entitlement correction by applying the explicit target state to `user_subscriptions`. If `cancelBillingAgreement` is true, it marks the local billing agreement cancelled to stop future local charges. It does not delete or cancel the provider billing key. Unexpected local failures roll back the transaction so the correction can be retried after investigation. |

**Request**

```json
{
  "note": "Executing after approval."
}
```

**Response** `200 OK` — returns `AdminPaymentEntitlementCorrectionResponse` with `status: "SUCCEEDED"`.

### GET /api/admin/payments/reconciliation

| Field | Value |
|---|---|
| **URL** | `GET /api/admin/payments/reconciliation` |
| **Auth** | ADMIN |
| **Description** | Runs read-only local ledger and provider API-backed reconciliation for recent subscription payment orders. Provider lookup is skipped when the provider lookup configuration is unavailable. This endpoint is for operations diagnostics only and must not mutate payment, billing agreement, or subscription state. It returns current mismatch results on demand but does not persist incident records, acknowledge issues, resolve issues, or send operator notifications. |

**Response** `200 OK`

```json
{
  "data": {
    "localLedger": {
      "checkedOrders": 100,
      "checkedBillingAgreements": 12,
      "doneOrdersWithoutPayment": 0,
      "activeAgreementsWithoutSubscription": 0,
      "hasMismatch": false,
      "issues": []
    },
    "providerLedger": {
      "checkedOrders": 100,
      "skippedOrders": 0,
      "providerNotFound": 2,
      "lookupFailures": 0,
      "providerDoneWithoutLocalFinalization": 1,
      "localDoneButProviderNotDone": 0,
      "amountMismatches": 0,
      "hasMismatch": true,
      "issues": [
        {
          "issueType": "PROVIDER_DONE_LOCAL_NOT_FINALIZED",
          "paymentOrderId": 3001,
          "userId": 12,
          "billingAgreementId": 10,
          "orderId": "ATS-REN-20260524-ABC123",
          "provider": "TOSS_BILLING",
          "purpose": "RENEWAL",
          "localStatus": "IN_PROGRESS",
          "providerStatus": "DONE",
          "localAmount": 9900,
          "providerAmount": 9900,
          "providerTransactionId": "payment_key",
          "failureCode": null,
          "failureMessage": null
        }
      ]
    }
  }
}
```

### GET /api/admin/payments/reconciliation-incidents

| Field | Value |
|---|---|
| **URL** | `GET /api/admin/payments/reconciliation-incidents?status=OPEN&page=1&size=20` |
| **Auth** | ADMIN |
| **Description** | Lists persisted reconciliation incidents created by scheduled reconciliation. `status` is optional and may be `OPEN`, `ACKNOWLEDGED`, `RESOLVED`, or `IGNORED`. This endpoint is for operations triage and does not mutate payment state. |

**Response** `200 OK`

```json
{
  "dataList": [
    {
      "id": 1,
      "dedupeKey": "PROVIDER_DONE_LOCAL_NOT_FINALIZED:order:ATS-REN-20260524-ABC123",
      "issueType": "PROVIDER_DONE_LOCAL_NOT_FINALIZED",
      "status": "OPEN",
      "severity": "CRITICAL",
      "paymentOrderId": 3001,
      "billingAgreementId": 10,
      "userId": 12,
      "userNickname": "customer12",
      "orderId": "ATS-REN-20260524-ABC123",
      "provider": "TOSS_BILLING",
      "purpose": "RENEWAL",
      "localStatus": "IN_PROGRESS",
      "providerStatus": "DONE",
      "localAmount": 9900,
      "providerAmount": 9900,
      "providerTransactionId": "payment_key",
      "failureCode": null,
      "failureMessage": null,
      "occurrenceCount": 2,
      "firstDetectedAt": "2026-05-24T01:00:00",
      "lastDetectedAt": "2026-05-24T01:00:00",
      "notifiedAt": "2026-05-24T01:00:02",
      "resolvedAt": null,
      "resolutionNote": null,
      "createdAt": "2026-05-24T01:00:00"
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

### PUT /api/admin/payments/reconciliation-incidents/{incidentId}/status

| Field | Value |
|---|---|
| **URL** | `PUT /api/admin/payments/reconciliation-incidents/{incidentId}/status` |
| **Auth** | ADMIN |
| **Description** | Updates the reconciliation incident workflow state and writes a `payment_operation_audit_logs` row. Valid statuses are `OPEN`, `ACKNOWLEDGED`, `RESOLVED`, and `IGNORED`. This endpoint does not mutate payment orders, subscriptions, billing agreements, or provider state. |

**Request**

```json
{
  "status": "ACKNOWLEDGED",
  "note": "Investigating against Toss dashboard."
}
```

**Response** `200 OK`

```json
{
  "data": {
    "id": 1,
    "dedupeKey": "PROVIDER_DONE_LOCAL_NOT_FINALIZED:order:ATS-REN-20260524-ABC123",
    "issueType": "PROVIDER_DONE_LOCAL_NOT_FINALIZED",
    "status": "ACKNOWLEDGED",
    "severity": "CRITICAL",
    "orderId": "ATS-REN-20260524-ABC123",
    "occurrenceCount": 2,
    "resolutionNote": "Investigating against Toss dashboard."
  }
}
```

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

## 6.7 Change My Subscription
| Field | Value |
|-------|-------|
| **URL** | `PUT /api/user-subscriptions/me` |
| **Auth** | auth required |
| **Description** | Change plan or billing cycle. Behavior differs by change type: **UPGRADE** requires a reusable billing agreement, immediately charges the remaining-period difference through recurring billing, then applies the higher plan while preserving the current paid period and next billing date. If the provider reports the stored billing key as removed/invalid, the local billing agreement is marked `EXPIRED`, issued-key fields are cleared, and the API returns `BILLING_AGREEMENT_REAUTH_REQUIRED` without changing the subscription. If the requested billing cycle differs from the current one, it is stored as pending and starts at the next renewal. The immediate charge is rounded to whole KRW; if the rounded amount is `0`, no provider charge is attempted. **SCHEDULED_CHANGE** saves or overwrites pending values (`pendingSubscriptionId`, `pendingBillingCycle`) and takes effect after the current period expires. **NO_CHANGE** clears a pending change when the user selects the current plan/current cycle. A CANCELLED grace-period subscription is reactivated before the change when a stored billing key is still usable. |

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
  "proratedAmount": 5000.00,
  "startedAt": "2026-05-01",
  "expiresAt": "2026-06-01"
}
```

> - `changeType`: `"UPGRADE"` — reusable billing agreement is charged for `proratedAmount` when the rounded amount is greater than `0`; new plan applies immediately; current `billingCycle` and `expiresAt` remain the active period basis.
> - `changeType`: `"SCHEDULED_CHANGE"` — pending values (`pendingSubscriptionId`, `pendingBillingCycle`) are saved or overwritten; current plan remains active until `expiresAt`; new plan/cycle activates after the next successful renewal charge.
> - `changeType`: `"NO_CHANGE"` — pending values are cleared; current plan/cycle and expiresAt remain unchanged.
> - `billingCycle` in an UPGRADE response is the requested billing cycle to use on the next renewal charge; the current subscription response may still show the active period's current `billingCycle` until renewal.
> - If an UPGRADE applies the higher plan immediately and only the requested next-renewal billing cycle differs, the frontend must show the higher plan as currently active and label only the billing-cycle change as pending.

**Error Cases**
- `409 Conflict` — `BILLING_AGREEMENT_REAUTH_REQUIRED` when the provider reports the stored billing key is removed, not found, or invalid. The current subscription remains unchanged and the user should re-register a payment method.
- `400 Bad Request` — `BILLING_AGREEMENT_INVALID_STATE` when no reusable billing key is available for the requested upgrade.

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
| **Description** | Member cancels their own active subscription. This means stop the next renewal: `user_subscriptions.status` becomes `CANCELLED`, local billing agreement renewal is stopped, and paid access remains available until `expiresAt`. The encrypted billing key is retained so the user can reactivate before expiry. |

**Response** `204 No Content`

**Error**
```json
{ "status": 404, "error": "Not Found", "errorCode": "SUBSCRIPTION_NOT_FOUND", "message": "구독 정보를 찾을 수 없습니다." }
```

## 6.11 Reactivate My Subscription
| Field | Value |
|-------|-------|
| **URL** | `POST /api/user-subscriptions/me/reactivate` |
| **Auth** | auth required |
| **Description** | Reactivate a CANCELLED grace-period subscription before `expiresAt`. The backend reuses the stored billing key, restores the billing agreement to `ACTIVE`, sets `nextBillingAt` to the current subscription `expiresAt`, and returns the updated subscription. |

**Response** `200 OK`
```json
{
  "message": "Subscription reactivated",
  "data": {
    "id": 100,
    "billingCycle": "MONTHLY",
    "status": "ACTIVE",
    "expiresAt": "2026-06-01"
  }
}
```

**Error**
```json
{ "status": 403, "error": "Forbidden", "errorCode": "NO_ACTIVE_SUBSCRIPTION", "message": "구독이 필요한 서비스입니다." }
{ "status": 404, "error": "Not Found", "errorCode": "BILLING_AGREEMENT_NOT_FOUND", "message": "등록된 정기결제 수단이 없습니다." }
{ "status": 400, "error": "Bad Request", "errorCode": "BILLING_AGREEMENT_INVALID_STATE", "message": "현재 자동결제 상태에서는 처리할 수 없습니다." }
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

## 12.1 Save Channel Draft
| Field | Value |
|-------|-------|
| **URL** | `POST /api/whitelist-channels` |
| **Auth** | auth required |

**Request**
```json
{
  "channelUrl": "https://youtube.com/@mychannel",
  "channelName": "My Channel",
  "youtubeHandle": "@mychannel",
  "youtubeChannelId": "UCxxxxxxxxxxxxxxxxxxxxxx"
}
```

> **channelUrl validation**: Strict URI parsing — host must be exactly `youtube.com` or end with `.youtube.com`. Supports all URL formats — `@handle`, `/channel/UCxxx`, `/c/customname`. Spoofed domains (e.g., `notarealsite-youtube.com`) are rejected.
> Saving a channel creates a `DRAFT` row. Active subscription and plan limit checks are performed by 12.4 when the user requests registration.

**Response** `201 Created`
```json
{
  "id": 1,
  "channelUrl": "https://youtube.com/@mychannel",
  "channelName": "My Channel",
  "youtubeHandle": "@mychannel",
  "youtubeChannelId": "UCxxxxxxxxxxxxxxxxxxxxxx",
  "status": "DRAFT",
  "primary": true,
  "adminNote": null,
  "requestedAt": null,
  "exportedAt": null,
  "processedAt": null,
  "removalRequestedAt": null,
  "createdAt": "2026-06-03T10:00:00"
}
```

**Error Cases**
```json
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
      "youtubeHandle": "@mychannel",
      "youtubeChannelId": "UCxxxxxxxxxxxxxxxxxxxxxx",
      "status": "PENDING",
      "primary": true,
      "adminNote": null,
      "requestedAt": "2026-06-03T10:05:00",
      "exportedAt": null,
      "processedAt": null,
      "removalRequestedAt": null,
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
  "channelName": "New Channel Name",
  "youtubeHandle": "@newchannel",
  "youtubeChannelId": "UCyyyyyyyyyyyyyyyyyyyyyy"
}
```

> **channelUrl validation**: Same as 12.1 — strict URI parsing, host must be exactly `youtube.com` or end with `.youtube.com`.
> Updating a channel in `REGISTERED`, `EXPORTED`, or `REVISION_REQUESTED` is treated as a reprocessing request. The backend checks active subscription and plan limit, excludes the target channel's own counted slot, then moves it back to `PENDING` for operator reprocessing.

**Response** `200 OK`

## 12.4 Request Whitelist Registration
| Field | Value |
|-------|-------|
| **URL** | `POST /api/whitelist-channels/{channelId}/request` |
| **Auth** | auth required (owner only, active subscription required) |

**Description**
Moves a saved channel to `PENDING` after checking active subscription and plan limit.

`PENDING` requests are idempotent. For `REVISION_REQUESTED`, the target channel's own counted slot is excluded before comparing against the plan limit, then the channel moves back to `PENDING`. Direct request attempts from `EXPORTED`, `REGISTERED`, or `REMOVAL_REQUESTED` return `INVALID_STATE_TRANSITION`.

**Plan Limit Counted Statuses**
```
PENDING, EXPORTED, REGISTERED, REVISION_REQUESTED, REMOVAL_REQUESTED
```

**Response** `200 OK`

**Error Cases**
```json
{ "status": 403, "error": "Forbidden", "errorCode": "NO_ACTIVE_SUBSCRIPTION", "message": "활성 구독이 없습니다." }
{ "status": 403, "error": "Forbidden", "errorCode": "WHITELIST_CHANNEL_LIMIT_EXCEEDED", "message": "채널 등록 한도를 초과했습니다." }
{ "status": 400, "error": "Bad Request", "errorCode": "INVALID_STATE_TRANSITION", "message": "상태를 변경할 수 없습니다." }
```

## 12.5 Set Primary Channel
| Field | Value |
|-------|-------|
| **URL** | `PUT /api/whitelist-channels/{channelId}/primary` |
| **Auth** | auth required (owner only) |

**Description**
Marks the selected channel as the user's representative YouTube channel and clears the previous primary channel if present.

**Response** `200 OK`

## 12.6 Delete Channel / Request Removal
| Field | Value |
|-------|-------|
| **URL** | `DELETE /api/whitelist-channels/{channelId}` |
| **Auth** | auth required (owner only) |

**Description**
Deletes local-only states (`DRAFT`, `PENDING`, `REVISION_REQUESTED`, `REJECTED`, `CANCELLED`). For `EXPORTED` and `REGISTERED`, the row is kept and changed to `REMOVAL_REQUESTED` because external removal must be handled manually by an operator.

If the deleted local-only row was the user's primary channel and another saved channel remains, the backend promotes one remaining channel as primary.

**Response** `204 No Content`

## 12.7 Admin List Whitelist Channels
| Field | Value |
|-------|-------|
| **URL** | `GET /api/admin/whitelist-channels` |
| **Auth** | `[ADMIN]` |

**Query Parameters**
```
status: WhitelistChannelStatus (optional)
keyword: String (optional; user email/nickname/channel name/URL/handle/channelId)
page: Integer (default: 1)
size: Integer (default: 20)
```

**Response** `200 OK`
```json
{
  "dataList": [
    {
      "id": 1,
      "userId": 10,
      "userEmail": "user@test.com",
      "userNickname": "user10",
      "channelUrl": "https://youtube.com/@mychannel",
      "channelName": "My Channel",
      "youtubeHandle": "@mychannel",
      "youtubeChannelId": "UCxxxxxxxxxxxxxxxxxxxxxx",
      "status": "PENDING",
      "primary": true,
      "adminNote": null,
      "processedByEmail": null,
      "planName": "DELUXE",
      "billingCycle": "MONTHLY",
      "requestedAt": "2026-06-03T10:05:00",
      "exportedAt": null,
      "processedAt": null,
      "removalRequestedAt": null,
      "createdAt": "2026-06-03T10:00:00"
    }
  ],
  "pageInfo": {
    "page": 1,
    "size": 20,
    "total": 1,
    "last": 1,
    "start": 1,
    "end": 1,
    "prev": false,
    "next": false
  }
}
```

## 12.8 Admin Update Whitelist Channel Status
| Field | Value |
|-------|-------|
| **URL** | `PUT /api/admin/whitelist-channels/{channelId}/status` |
| **Auth** | `[ADMIN]` |

**Request**
```json
{
  "status": "REGISTERED",
  "adminNote": "External registration completed."
}
```

**Supported Status Values**
```
REGISTERED, REVISION_REQUESTED, REJECTED, REMOVAL_REQUESTED, CANCELLED
```

**Response** `200 OK`

## 12.9 Admin Export Whitelist Channels
| Field | Value |
|-------|-------|
| **URL** | `POST /api/admin/whitelist-channels/export` |
| **Auth** | `[ADMIN]` |

**Query Parameters**
```
status: WhitelistChannelStatus (optional; default PENDING)
note: String (optional)
```

> Export is status-based. `keyword` filtering is supported by 12.7 list only and is not applied to CSV export in the current implementation.

**Response** `200 OK`
```
Content-Type: text/csv;charset=UTF-8
Content-Disposition: attachment; filename*=UTF-8''whitelist-channels-20260603-100000.csv
```

**CSV Columns**
```
requestId,userId,userEmail,userNickname,channelName,youtubeHandle,channelUrl,youtubeChannelId,requestedAt,planName,billingCycle,exportedAt
```

**Side Effects**
- Creates `whitelist_export_batches` and `whitelist_export_items` snapshot rows when export target rows exist.
- When exporting `PENDING` rows, marks those channels as `EXPORTED`.
- Exporting non-`PENDING` rows is allowed for operations review/removal handoff, but it does not overwrite the current workflow status.

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
| **Description** | Preview the financial and scheduling impact of a plan change before committing. Returns whether the change is an UPGRADE, SCHEDULED_CHANGE, or NO_CHANGE, the immediate upgrade charge amount, effective date, next billing date, and next billing amount. |

**Query Parameters**
```
subscriptionId: Long (required — target subscription plan ID)
billingCycle: String (required — "MONTHLY" | "YEARLY")
```

**Response** `200 OK`
```json
{
  "changeType": "UPGRADE",
  "proratedAmount": 5000,
  "effectiveDate": "2026-03-07",
  "nextBillingDate": "2026-04-01",
  "nextBillingAmount": 199000.00,
  "newPlanName": "DELUXE",
  "newBillingCycle": "YEARLY"
}
```

> - `changeType`: `"UPGRADE"`, `"SCHEDULED_CHANGE"`, or `"NO_CHANGE"`
> - `proratedAmount`: Immediate whole-KRW charge amount for UPGRADE, `0` for SCHEDULED_CHANGE/NO_CHANGE
> - `effectiveDate`: LocalDate (ISO-8601) — date the new plan takes effect
> - `nextBillingDate`: LocalDate (ISO-8601) — date of the next recurring charge
> - `nextBillingAmount`: Amount to charge on `nextBillingDate` for the selected target plan/cycle
> - `newPlanName`: Name of the target subscription plan
> - `newBillingCycle`: `"MONTHLY"` or `"YEARLY"`; for UPGRADE, this is the next-renewal billing cycle when it differs from the active period.

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

# Full API Summary (145)

| # | Section | API Count |
|---|---------|-----------|
| 1 | Track | 9 |
| 2 | Tag | 5 |
| 3 | Playlist | 9 |
| 4 | Play History | 3 |
| 5 | User Info | 11 |
| 6 | Subscription | 19 |
| 7 | License | 4 |
| 8 | Question (Inquiry/Answer) | 7 |
| 9 | Notice | 6 |
| 10 | Likes (Favorites) | 6 |
| 11 | Download Queue / History | 5 |
| 12 | Whitelist Channels | 9 |
| 13 | Company Certification | 5 |
| 14 | Utility / Auth | 12 |
| 15 | Album | 8 |
| 16 | Admin Dashboard | 1 |
| 17 | Site Settings | 2 |
| 18 | Admin Payment Operations | 24 |
| | **Total** | **145** |
