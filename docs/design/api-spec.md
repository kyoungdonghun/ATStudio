# ATStudio API Specification v5 (Confirmed)

> **Status**: 5th confirmed — standards alignment applied
> **Base**: v4 + docs/standards alignment verification results
> **Date**: 2026-02-20

---

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
sort: String (optional, "latest"|"popular", default: "latest")
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
[
  { "id": 1, "name": "Happy", "type": "MOOD" },
  { "id": 2, "name": "Pop", "type": "GENRE" }
]
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
      "track": { "id": 10, "title": "Summer Vibes", "thumbnail": "..." },
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

## 5.3 Social Login
| Field | Value |
|-------|-------|
| **URL** | `POST /api/auth/social/{provider}` |
| **Auth** | `[PUBLIC]` |
| **Description** | OAuth2.0 social login (GOOGLE/KAKAO/NAVER). On first signup, creates a users record with minimal info and returns `isProfileComplete: false`. Frontend detects this and navigates to 5.10 Profile Completion screen. |

**Request**
```json
{
  "authorizationCode": "4/0AX4XfWh..."
}
```

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
| **Description** | After initial social login signup, completes the profile by entering additional info (nickname, phone, job, user type). userType can only be set at this step. |

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

**Response** `200 OK` — Same format as 5.4 My Profile response

**Error Cases**
```json
{ "status": 409, "error": "Conflict", "errorCode": "NICKNAME_DUPLICATED", "message": "이미 사용 중인 닉네임입니다." }
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
  "job": "FREELANCER"
}
```

**Response** `200 OK` — Updated profile

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
[
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
| **Description** | Subscribe to a plan (includes payment). Business members (over 100 employees) require license approval |

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
  "expiresAt": "2026-03-19"
}
```

**Error Cases**
```json
{ "status": 403, "error": "Forbidden", "errorCode": "COMPANY_CERTIFICATION_REQUIRED", "message": "기업 인증 심사 승인 후 이용 가능합니다." }
```

## 6.4 My Subscription
| Field | Value |
|-------|-------|
| **URL** | `GET /api/user-subscriptions/me` |
| **Auth** | auth required |

**Response** `200 OK` — My current subscription status

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
| **Description** | Applied immediately, prorated amount charged |

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
  "proratedAmount": 15000.00,
  "startedAt": "2026-02-19",
  "expiresAt": "2027-02-19"
}
```

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
| **Description** | Member cancels their own active subscription. Immediate cancellation (status=CANCELLED). |

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

**Request**
```json
{
  "title": "서비스 점검 안내",
  "content": "2월 20일 오전 2시~4시 점검 예정입니다.",
  "isPinned": true
}
```

**Response** `201 Created`
```json
{
  "id": 1,
  "title": "서비스 점검 안내",
  "content": "2월 20일 오전 2시~4시 점검 예정입니다.",
  "isPinned": true,
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
```

**Response** `200 OK` — Pagination + notice list (pinned notices first)

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
  "createdAt": "2026-02-19T10:00:00",
  "updatedAt": "2026-02-19T10:00:00"
}
```

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

---

# 10. Likes (Favorites)

## 10.1 Add to Likes
| Field | Value |
|-------|-------|
| **URL** | `POST /api/likes/{trackId}` |
| **Auth** | auth required |

**Response** `201 Created`

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
  "remaining": 17
}
```

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
  - thumbnailFile: file (optional)
- Response: 201 Created
  - id, title, description, thumbnailUrl, trackCount, createdAt
- Errors: 400 INVALID_ARGUMENT

### 15.2 List Albums
- Method: GET
- URL: /api/albums
- Auth: none
- Response: 200 OK
  - dataList: [ { id, title, thumbnailUrl, trackCount } ]

### 15.3 Get Album
- Method: GET
- URL: /api/albums/{id}
- Auth: none
- Response: 200 OK
  - id, title, description, thumbnailUrl, tracks: [ { trackId, title, artistName, thumbnailUrl, order } ], createdAt
- Errors: 404 RESOURCE_NOT_FOUND

### 15.4 Update Album
- Method: PUT
- URL: /api/albums/{id}
- Auth: ADMIN
- Request: multipart/form-data (title, description, thumbnailFile -- all optional)
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
- Response: 200 OK, AlbumDetailResponse
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

# Full API Summary (87)

| # | Section | API Count |
|---|---------|-----------|
| 1 | Track | 7 |
| 2 | Tag | 4 |
| 3 | Playlist | 8 |
| 4 | Play History | 3 |
| 5 | User Info | 10 |
| 6 | Subscription | 10 |
| 7 | License | 4 |
| 8 | Question (Inquiry/Answer) | 7 |
| 9 | Notice | 5 |
| 10 | Likes (Favorites) | 3 |
| 11 | Download Queue | 3 |
| 12 | Whitelist Channels | 4 |
| 13 | Company Certification | 5 |
| 14 | Utility | 7 |
| 15 | Album | 8 |
| | **Total** | **87** |
