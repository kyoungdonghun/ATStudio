# ATStudio API Specification v5 (Confirmed)

> **Status**: 5차 확정본 — 표준 문서 정합성 반영
> **Base**: v4 + docs/standards 정합성 검증 결과
> **Date**: 2026-02-20

---

## v4 → v5 변경 이력

| # | 항목 | 결정 |
|---|------|------|
| V1 | 에러 응답 `error` 필드 | **표준 준수** — `error` = HTTP reason phrase (예: "Forbidden"). 도메인 코드는 `errorCode` 신규 필드로 분리. |
| V2 | pageInfo 구조 | **표준 준수** — `totalElements/totalPages` 제거 → `total, start, end, prev, next` (블록 페이지네이션). |
| V3 | 목록 필드명 | **표준 준수** — `content` → `dataList` |
| V4 | 성공 응답 body | **표준 준수** — `status` 필드 제거. HTTP 상태코드로만 전달. |

---

## v3 → v4 변경 이력

| # | 항목 | 결정 |
|---|------|------|
| 1 | 소셜 로그인 2단계 가입 | **추가 확정** — 최초 가입 시 `isProfileComplete: false` 반환. 5.10 프로필 완성 API 추가. |
| 2 | 플레이리스트 전체 API | **구독자 전용 확정** — 3.1~3.8 권한 `인증 필요 (구독자만)` |
| 3 | 스트리밍 API 재생 기록 제거 | **분리 확정** — 1.4 stream에서 play_histories 기록 제거. 4.1 POST /api/play-histories 단일 경로. |
| 4 | 문의 수정 API 제거 | **제거 확정** — 8.6 문의 수정 API 삭제. 프론트에서 수정 불가 안내. |
| 5 | 화이트리스트 응답에서 isActive 제거 | **제거 확정** — is_active 컬럼 삭제에 따른 응답 수정. |
| 6 | 본인 구독 취소 API 추가 | **추가 확정** — 6.10 DELETE /api/user-subscriptions/me |
| 7 | 문의 상태 변경 API UC 추가 | **추가 확정** — QUESTION-008 (관리자 전용, 기존 8.7 API에 대응) |
| 8 | subscription_payments | `user_subscription_id` FK 반영 (DB 변경 연동) |

---

## v2 → v3 변경 이력

| # | 항목 | 결정 |
|---|------|------|
| 1 | 음원 스트리밍 | **preview_file 우선 제공** — `preview_file` 존재 시 저품질 스트리밍, NULL이면 `audio_file` fallback |
| 2 | 닉네임 중복 확인 API | **추가 확정** — `GET /api/utils/check-nickname` |

---

## v1 → v2 변경 이력

| # | 항목 | 결정 |
|---|------|------|
| 1 | playlog 해석 | **재생 기록(play_histories) 확정** |
| 2 | 음원 삭제 | **논리적 삭제(is_active=0)** |
| 3 | 공지사항 DB | **`notices` 테이블 추가 확정** |
| 4 | 문의 첨부파일 | **`question_attachments` 별도 테이블 추가** |
| 5 | 재생 기록 삭제 | **전체/선택 삭제 둘 다 지원** |
| 6 | 라이센스 발급 | **다운로드 시 자동 발급** (수동 발급 API 제거, 중복 방지) |
| 7 | 일괄 다운로드 | **ZIP 아님** — 프론트에서 개별 API 순차 호출 + beforeunload 이탈 방지 |
| 8 | 태그 목록 조회 | **추가 확정** |

---

## 공통 규칙

### Base URL
```
/api
```

### 인증
- JWT Bearer Token (`Authorization: Bearer {token}`)
- `[PUBLIC]` = 인증 불필요
- `[ADMIN]` = 관리자 전용

### 공통 응답 형식
```json
{
  "message": "Success",
  "data": { ... }
}
```

> `status` 필드는 HTTP 상태코드로만 전달. 응답 body에 포함하지 않음.

### 공통 에러 응답
```json
{
  "status": 400,
  "error": "Bad Request",
  "errorCode": "DOMAIN_ERROR_CODE",
  "message": "사용자 메시지"
}
```

> - `error`: HTTP reason phrase (예: "Bad Request", "Forbidden", "Not Found")
> - `errorCode`: 도메인 에러 코드 (도메인 특화 에러인 경우에만 포함, 없으면 생략)
> - `message`: 사용자에게 표시할 안전한 메시지

### 페이지네이션 (목록 조회 공통)
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

> - `total`: 전체 데이터 수
> - `start` / `end`: 현재 블록의 시작/끝 페이지 번호
> - `prev` / `next`: 이전/다음 블록 존재 여부

---

# 1. Sound — Track (음원)

## 1.1 음원 생성
| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/tracks` |
| **권한** | `[ADMIN]` |
| **설명** | 관리자가 새 음원 업로드 (검토 후 공개: is_active=0). 업로드 완료 후 비동기로 저품질 `preview_file` 생성 (실패 시 NULL 유지 → audio_file fallback) |

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

## 1.2 음원 목록 조회
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/tracks` |
| **권한** | `[PUBLIC]` |
| **설명** | 음원 목록 조회 (검색, 필터, 페이지네이션). 활성(is_active=1) 음원만 반환 |

**Query Parameters**
```
page: Integer (default: 1)
size: Integer (default: 20)
keyword: String (optional, 제목 검색)
genre: String (optional, 장르 태그 필터)
mood: String (optional, 분위기 태그 필터)
instrument: String (optional, 악기 태그 필터)
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

## 1.3 음원 한개 조회
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/tracks/{trackId}` |
| **권한** | `[PUBLIC]` |
| **설명** | 음원 상세 정보 조회 |

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

## 1.4 음원 재생
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/tracks/{trackId}/stream` |
| **권한** | `[PUBLIC]` |
| **설명** | 음원 미리듣기 스트리밍 (비회원도 가능). `preview_file` 존재 시 저품질 파일 제공, `preview_file`이 NULL이면 `audio_file` fallback. 재생 기록 저장은 프론트엔드가 별도로 4.1 API를 명시적으로 호출. |

**Response** `200 OK` — audio stream (Content-Type: audio/mpeg)

## 1.5 음원 다운로드
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/tracks/{trackId}/download` |
| **권한** | 인증 필요 (구독자만) |
| **설명** | 음원 파일 다운로드. 일일 다운로드 제한 체크. 다운로드 기록 저장 + 라이센스 자동 발급 (기존 라이센스 있으면 중복 발급 안 함) |

**Response** `200 OK` — file download (Content-Disposition: attachment)

**에러 케이스**
```json
{ "status": 403, "error": "Forbidden", "errorCode": "DOWNLOAD_LIMIT_EXCEEDED", "message": "오늘의 다운로드 한도를 초과했습니다." }
{ "status": 403, "error": "Forbidden", "errorCode": "NO_ACTIVE_SUBSCRIPTION", "message": "구독이 필요한 서비스입니다." }
```

## 1.6 음원 수정
| 항목 | 내용 |
|------|------|
| **URL** | `PUT /api/tracks/{trackId}` |
| **권한** | `[ADMIN]` |
| **설명** | 음원 정보 수정 (활성화/비활성화 포함) |

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

**Response** `200 OK` — 수정된 음원 상세 정보 (1.3과 동일 형식)

## 1.7 음원 삭제 (논리적)
| 항목 | 내용 |
|------|------|
| **URL** | `DELETE /api/tracks/{trackId}` |
| **권한** | `[ADMIN]` |
| **설명** | 논리적 삭제 (is_active=0으로 비활성화) |

**Response** `204 No Content`

---

# 2. Sound — Tag (태그)

## 2.1 태그 생성
| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/tags` |
| **권한** | `[ADMIN]` |

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

## 2.2 태그 목록 조회
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/tags` |
| **권한** | `[PUBLIC]` |
| **설명** | 필터 UI 구성용 태그 전체 목록 |

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

## 2.3 태그 수정
| 항목 | 내용 |
|------|------|
| **URL** | `PUT /api/tags/{tagId}` |
| **권한** | `[ADMIN]` |

**Request**
```json
{
  "name": "Lo-Fi Hip Hop",
  "type": "GENRE"
}
```

**Response** `200 OK` — 수정된 태그 정보

## 2.4 태그 삭제
| 항목 | 내용 |
|------|------|
| **URL** | `DELETE /api/tags/{tagId}` |
| **권한** | `[ADMIN]` |

**Response** `204 No Content`

---

# 3. Sound — Playlist (플레이리스트)

## 3.1 플레이리스트 생성
| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/playlists` |
| **권한** | 인증 필요 (구독자만) |

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

## 3.2 플레이리스트 목록 조회
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/playlists` |
| **권한** | 인증 필요 (구독자만) |
| **설명** | 내 플레이리스트 목록 조회 |

**Response** `200 OK`
```json
[
  {
    "id": 1,
    "title": "My Workout Mix",
    "thumbnail": null,
    "trackCount": 5,
    "createdAt": "2026-02-19T10:00:00"
  }
]
```

## 3.3 플레이리스트 한개 조회
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/playlists/{playlistId}` |
| **권한** | 인증 필요 (구독자만, 본인 플레이리스트만) |
| **설명** | 플레이리스트 상세 (포함된 음원 목록 + 재생 순서) |

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

## 3.4 플레이리스트에 음원 추가
| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/playlists/{playlistId}/tracks` |
| **권한** | 인증 필요 (구독자만, 본인 플레이리스트만) |

**Request**
```json
{
  "trackId": 10
}
```

**Response** `201 Created`

## 3.5 플레이리스트 수정
| 항목 | 내용 |
|------|------|
| **URL** | `PUT /api/playlists/{playlistId}` |
| **권한** | 인증 필요 (구독자만, 본인 플레이리스트만) |

**Request** (multipart/form-data)
```
title: String (optional)
description: String (optional)
thumbnail: File (optional)
```

**Response** `200 OK`

## 3.6 플레이리스트 음원 순서 변경
| 항목 | 내용 |
|------|------|
| **URL** | `PUT /api/playlists/{playlistId}/tracks` |
| **권한** | 인증 필요 (구독자만, 본인 플레이리스트만) |

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

## 3.7 플레이리스트에서 음원 제거
| 항목 | 내용 |
|------|------|
| **URL** | `DELETE /api/playlists/{playlistId}/tracks/{trackId}` |
| **권한** | 인증 필요 (구독자만, 본인 플레이리스트만) |

**Response** `204 No Content`

## 3.8 플레이리스트 삭제
| 항목 | 내용 |
|------|------|
| **URL** | `DELETE /api/playlists/{playlistId}` |
| **권한** | 인증 필요 (구독자만, 본인 플레이리스트만) |

**Response** `204 No Content`

---

# 4. Sound — Play History (재생 기록)

## 4.1 재생 기록 저장
| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/play-histories` |
| **권한** | 인증 필요 |
| **설명** | Que bar에서 음원 재생 시 자동 기록 (tracks.play_count 연동) |

**Request**
```json
{
  "trackId": 10
}
```

**Response** `201 Created`

## 4.2 재생 기록 조회
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/play-histories` |
| **권한** | 인증 필요 |
| **설명** | 내 재생 기록 목록 (최신순) |

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

## 4.3 재생 기록 삭제
| 항목 | 내용 |
|------|------|
| **URL** | `DELETE /api/play-histories` |
| **권한** | 인증 필요 |
| **설명** | 선택 삭제 (historyIds 지정) 또는 전체 삭제 (historyIds 비어있으면) |

**Request**
```json
{
  "historyIds": [100, 101, 102]
}
```
> `historyIds`가 빈 배열 `[]`이면 전체 삭제

**Response** `204 No Content`

---

# 5. User — Info (회원)

## 5.1 회원가입
| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/users` |
| **권한** | `[PUBLIC]` |

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

## 5.2 로그인
| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/auth/login` |
| **권한** | `[PUBLIC]` |

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

## 5.3 소셜 로그인
| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/auth/social/{provider}` |
| **권한** | `[PUBLIC]` |
| **설명** | OAuth2.0 소셜 로그인 (GOOGLE/KAKAO/NAVER). 최초 가입 시 최소 정보로 users 레코드 생성 후 `isProfileComplete: false` 반환. 프론트엔드가 감지 후 5.10 프로필 완성 화면으로 이동. |

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
> `isProfileComplete: false`이면 프론트엔드가 5.10 프로필 완성 화면으로 redirect.

## 5.10 소셜 회원 프로필 완성
| 항목 | 내용 |
|------|------|
| **URL** | `PUT /api/users/me/complete-profile` |
| **권한** | 인증 필요 (isProfileComplete=false인 회원만) |
| **설명** | 소셜 로그인 최초 가입 후 추가 정보(닉네임, 전화번호, 직업, 회원 유형)를 입력하여 프로필을 완성한다. userType은 이 단계에서만 설정 가능. |

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

**Response** `200 OK` — 5.4 내 정보 보기 응답과 동일 형식

**에러 케이스**
```json
{ "status": 409, "error": "Conflict", "errorCode": "NICKNAME_DUPLICATED", "message": "이미 사용 중인 닉네임입니다." }
```

## 5.4 내 정보 보기
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/users/me` |
| **권한** | 인증 필요 |

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

## 5.5 회원 목록 조회
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/users` |
| **권한** | `[ADMIN]` |

**Query Parameters**
```
page: Integer (default: 1)
size: Integer (default: 20)
keyword: String (optional, 닉네임/이메일 검색)
userType: String (optional, "INDIVIDUAL"|"BUSINESS")
```

**Response** `200 OK` — 페이지네이션 + 사용자 목록

## 5.6 특정 회원 정보 보기
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/users/{userId}` |
| **권한** | `[ADMIN]` |

**Response** `200 OK` — 사용자 상세 정보

## 5.7 내 정보 수정
| 항목 | 내용 |
|------|------|
| **URL** | `PUT /api/users/me` |
| **권한** | 인증 필요 |

**Request**
```json
{
  "nickname": "newNickname",
  "phonePersonal": "010-9999-8888",
  "phoneCompany": "02-1234-5678",
  "job": "FREELANCER"
}
```

**Response** `200 OK` — 수정된 내 정보

## 5.8 회원 정보 수정 (관리자)
| 항목 | 내용 |
|------|------|
| **URL** | `PUT /api/users/{userId}` |
| **권한** | `[ADMIN]` |

**Request**
```json
{
  "role": "ADMIN",
  "isVerified": true
}
```

**Response** `200 OK`

## 5.9 회원탈퇴 (본인)
| 항목 | 내용 |
|------|------|
| **URL** | `DELETE /api/users/me` |
| **권한** | 인증 필요 |
| **설명** | 논리적 삭제 (is_deleted = 1) |

**Request**
```json
{
  "password": "SecureP@ss123"
}
```

**Response** `204 No Content`

---

# 6. User — Subscription (구독제)

## 6.1 구독제 목록 조회
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/subscriptions` |
| **권한** | `[PUBLIC]` |

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
    "userType": "INDIVIDUAL",
    "priceMonthly": 9900.00,
    "priceYearly": 99000.00,
    "downloadPerDay": 5,
    "maxWhitelistChannels": 1
  }
]
```

## 6.2 구독제 상세 조회
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/subscriptions/{subscriptionId}` |
| **권한** | `[PUBLIC]` |

**Response** `200 OK` — 구독 플랜 상세

## 6.3 구독 신청
| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/user-subscriptions` |
| **권한** | 인증 필요 |
| **설명** | 구독제 구독 신청 (결제 포함). 기업회원(100명 초과)은 라이센스 승인 필요 |

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

**에러 케이스**
```json
{ "status": 403, "error": "Forbidden", "errorCode": "BUSINESS_LICENSE_REQUIRED", "message": "기업회원 라이센스 심사 승인 후 이용 가능합니다." }
```

## 6.4 내 구독 정보 보기
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/user-subscriptions/me` |
| **권한** | 인증 필요 |

**Response** `200 OK` — 내 현재 구독 상태

## 6.5 회원 구독 정보 목록 조회 (관리자)
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/user-subscriptions` |
| **권한** | `[ADMIN]` |

**Response** `200 OK` — 페이지네이션 + 전체 구독 목록

## 6.6 회원 구독 정보 상세 조회 (관리자)
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/user-subscriptions/{userSubscriptionId}` |
| **권한** | `[ADMIN]` |

**Response** `200 OK`

## 6.7 본인 구독 변경 (업/다운그레이드)
| 항목 | 내용 |
|------|------|
| **URL** | `PUT /api/user-subscriptions/me` |
| **권한** | 인증 필요 |
| **설명** | 즉시 적용, 차등 금액 결제 |

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

## 6.8 회원 구독 수정 (관리자)
| 항목 | 내용 |
|------|------|
| **URL** | `PUT /api/user-subscriptions/{userSubscriptionId}` |
| **권한** | `[ADMIN]` |

**Response** `200 OK`

## 6.9 회원 구독 삭제/취소 (관리자)
| 항목 | 내용 |
|------|------|
| **URL** | `DELETE /api/user-subscriptions/{userSubscriptionId}` |
| **권한** | `[ADMIN]` |

**Response** `204 No Content`

## 6.10 본인 구독 취소
| 항목 | 내용 |
|------|------|
| **URL** | `DELETE /api/user-subscriptions/me` |
| **권한** | 인증 필요 |
| **설명** | 회원이 본인의 활성 구독을 취소한다. 즉시 취소(status=CANCELLED). |

**Response** `204 No Content`

**에러**
```json
{ "status": 404, "error": "Not Found", "errorCode": "SUBSCRIPTION_NOT_FOUND", "message": "구독 정보를 찾을 수 없습니다." }
```

---

# 7. User — License (라이센스)

> 라이센스는 다운로드 시 자동 발급. 같은 곡 재다운로드 시 기존 라이센스 유지 (중복 발급 방지).

## 7.1 내 라이센스 목록 보기
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/licenses/me` |
| **권한** | 인증 필요 |

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

## 7.2 회원의 라이센스 목록 보기 (관리자)
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/users/{userId}/licenses` |
| **권한** | `[ADMIN]` |

**Response** `200 OK` — 7.1과 동일 형식

## 7.3 내 라이센스 상세 조회
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/licenses/{licenseId}` |
| **권한** | 인증 필요 (본인 라이센스만) |

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

## 7.4 회원의 라이센스 상세 조회 (관리자)
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/users/{userId}/licenses/{licenseId}` |
| **권한** | `[ADMIN]` |

**Response** `200 OK` — 7.3과 동일 형식

---

# 8. User — Question (문의/답변)

## 8.1 문의 생성
| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/questions` |
| **권한** | 인증 필요 |

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

## 8.2 문의 답변 작성
| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/questions/{questionId}/answers` |
| **권한** | 인증 필요 (문의자 본인 or ADMIN) |
| **설명** | 관리자 첫 답변 시 문의 상태 자동 변경 (OPEN → IN_PROGRESS) |

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

## 8.3 문의 목록 조회
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/questions` |
| **권한** | 인증 필요 |
| **설명** | 일반 사용자: 공개 문의 + 내 비공개 문의. 관리자: 전체 |

**Query Parameters**
```
page: Integer (default: 1)
size: Integer (default: 20)
category: String (optional)
status: String (optional, "OPEN"|"IN_PROGRESS"|"RESOLVED"|"CLOSED")
mine: Boolean (optional, true이면 내 문의만)
```

**Response** `200 OK` — 페이지네이션 + 문의 목록

## 8.4 문의 상세 조회
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/questions/{questionId}` |
| **권한** | 인증 필요 (비공개 문의: 본인+ADMIN만) |

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

## 8.5 첨부 파일 다운로드
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/questions/{questionId}/attachments/{attachmentId}` |
| **권한** | 인증 필요 (문의 열람 권한 동일: 본인+ADMIN) |

**Response** `200 OK` — file download

## 8.6 문의 상태 변경
| 항목 | 내용 |
|------|------|
| **URL** | `PUT /api/questions/{questionId}/status` |
| **권한** | `[ADMIN]` |

**Request**
```json
{
  "status": "RESOLVED"
}
```

**Response** `200 OK`

**상태 흐름:**
- OPEN → IN_PROGRESS (관리자 첫 답변 시 자동) → RESOLVED → CLOSED
- OPEN → CLOSED (관리자가 직접 닫음)

## 8.7 문의 삭제
| 항목 | 내용 |
|------|------|
| **URL** | `DELETE /api/questions/{questionId}` |
| **권한** | 인증 필요 (본인만, OPEN 상태만) 또는 `[ADMIN]` |

**Response** `204 No Content`

---

# 9. User — Notice (공지사항)

## 9.1 공지 생성
| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/notices` |
| **권한** | `[ADMIN]` |

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

## 9.2 공지 목록 조회
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/notices` |
| **권한** | `[PUBLIC]` |

**Query Parameters**
```
page: Integer (default: 1)
size: Integer (default: 20)
```

**Response** `200 OK` — 페이지네이션 + 공지 목록 (고정 공지 상단 정렬)

## 9.3 공지 상세 조회
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/notices/{noticeId}` |
| **권한** | `[PUBLIC]` |

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

## 9.4 공지 수정
| 항목 | 내용 |
|------|------|
| **URL** | `PUT /api/notices/{noticeId}` |
| **권한** | `[ADMIN]` |

**Request**
```json
{
  "title": "서비스 점검 안내 (수정)",
  "content": "점검 시간이 변경되었습니다.",
  "isPinned": true
}
```

**Response** `200 OK`

## 9.5 공지 삭제
| 항목 | 내용 |
|------|------|
| **URL** | `DELETE /api/notices/{noticeId}` |
| **권한** | `[ADMIN]` |

**Response** `204 No Content`

---

# 10. 즐겨찾기 (Likes)

## 10.1 즐겨찾기 추가
| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/likes/{trackId}` |
| **권한** | 인증 필요 |

**Response** `201 Created`

## 10.2 즐겨찾기 목록 조회
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/likes` |
| **권한** | 인증 필요 |

**Response** `200 OK`
```json
[
  {
    "trackId": 10,
    "title": "Summer Vibes",
    "bpm": 120,
    "tonality": "C",
    "thumbnail": "/tracks/thumbnail/summer-vibes.jpg",
    "createdAt": "2026-02-19T10:00:00"
  }
]
```

## 10.3 즐겨찾기 해제
| 항목 | 내용 |
|------|------|
| **URL** | `DELETE /api/likes/{trackId}` |
| **권한** | 인증 필요 |

**Response** `204 No Content`

---

# 11. 다운로드 대기 목록 (Download Queue)

> 여러 곡을 모아 프론트에서 개별 다운로드 API를 순차 호출하는 방식.
> 다운로드 중 페이지 이탈 시 `beforeunload` 이벤트로 경고 표시.

## 11.1 대기 목록에 추가
| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/download-queue/{trackId}` |
| **권한** | 인증 필요 |

**Response** `201 Created`

## 11.2 대기 목록 조회
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/download-queue` |
| **권한** | 인증 필요 |

**Response** `200 OK`
```json
[
  {
    "trackId": 10,
    "title": "Summer Vibes",
    "bpm": 120,
    "tonality": "C",
    "thumbnail": "/tracks/thumbnail/summer-vibes.jpg",
    "createdAt": "2026-02-19T10:00:00"
  }
]
```

## 11.3 대기 목록에서 제거
| 항목 | 내용 |
|------|------|
| **URL** | `DELETE /api/download-queue/{trackId}` |
| **권한** | 인증 필요 |

**Response** `204 No Content`

---

# 12. 화이트리스트 채널

## 12.1 채널 등록
| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/whitelist-channels` |
| **권한** | 인증 필요 (구독자만) |

**Request**
```json
{
  "channelUrl": "https://youtube.com/@mychannel",
  "channelName": "My Channel"
}
```

**Response** `201 Created`

**에러 케이스**
```json
{ "status": 403, "error": "Forbidden", "errorCode": "WHITELIST_CHANNEL_LIMIT_EXCEEDED", "message": "채널 등록 한도를 초과했습니다." }
```

## 12.2 내 채널 목록 조회
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/whitelist-channels` |
| **권한** | 인증 필요 |

**Response** `200 OK`
```json
[
  {
    "id": 1,
    "channelUrl": "https://youtube.com/@mychannel",
    "channelName": "My Channel",
    "createdAt": "2026-02-19T10:00:00"
  }
]
```

## 12.3 채널 수정
| 항목 | 내용 |
|------|------|
| **URL** | `PUT /api/whitelist-channels/{channelId}` |
| **권한** | 인증 필요 (본인 채널만) |

**Request**
```json
{
  "channelUrl": "https://youtube.com/@newchannel",
  "channelName": "New Channel Name"
}
```

**Response** `200 OK`

## 12.4 채널 삭제
| 항목 | 내용 |
|------|------|
| **URL** | `DELETE /api/whitelist-channels/{channelId}` |
| **권한** | 인증 필요 (본인 채널만) |

**Response** `204 No Content`

---

# 13. 기업 라이센스 심사

## 13.1 라이센스 신청
| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/business-licenses` |
| **권한** | 인증 필요 (기업회원만) |

**Request** (multipart/form-data)
```
documents: List<File> (required)
```

**Response** `201 Created`
```json
{
  "id": 1,
  "status": "PENDING",
  "documentPath": "/uploads/business-docs/1/",
  "createdAt": "2026-02-19T10:00:00"
}
```

## 13.2 내 라이센스 신청 현황 조회
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/business-licenses/me` |
| **권한** | 인증 필요 (기업회원) |

**Response** `200 OK`
```json
{
  "id": 1,
  "status": "PENDING",
  "adminNote": null,
  "licenseCode": null,
  "createdAt": "2026-02-19T10:00:00"
}
```

## 13.3 라이센스 신청 목록 조회 (관리자)
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/business-licenses` |
| **권한** | `[ADMIN]` |

**Query Parameters**
```
status: String (optional, "PENDING"|"APPROVED"|"REVISION_REQUESTED"|"REJECTED")
page: Integer (default: 1)
size: Integer (default: 20)
```

**Response** `200 OK` — 페이지네이션

## 13.4 라이센스 신청 상세 조회 (관리자)
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/business-licenses/{requestId}` |
| **권한** | `[ADMIN]` |

**Response** `200 OK`

## 13.5 라이센스 심사 처리 (관리자)
| 항목 | 내용 |
|------|------|
| **URL** | `PUT /api/business-licenses/{requestId}` |
| **권한** | `[ADMIN]` |
| **설명** | 승인/보완요청/반려 처리. 승인 시 license_code 자동 생성 |

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
  "licenseCode": "BIZ-a1b2c3d4-e5f6-...",
  "approvedAt": "2026-02-19T15:00:00"
}
```

---

# 14. Util (유틸리티)

## 14.1 토큰 재발급
| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/auth/refresh` |
| **권한** | `[PUBLIC]` (Refresh Token 필요) |

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

## 14.2 이메일 중복 확인
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/utils/check-email` |
| **권한** | `[PUBLIC]` |

**Query Parameters**
```
email: String (required)
```

**Response** `200 OK`
```json
{ "available": true }
```

## 14.3 휴대폰 중복 확인
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/utils/check-phone` |
| **권한** | `[PUBLIC]` |

**Query Parameters**
```
phone: String (required)
```

**Response** `200 OK`
```json
{ "available": true }
```

## 14.4 구독 등급 확인
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/utils/subscription-status` |
| **권한** | 인증 필요 |

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

## 14.5 다운로드 횟수 확인
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/utils/download-count` |
| **권한** | 인증 필요 |

**Response** `200 OK`
```json
{
  "todayDownloads": 3,
  "dailyLimit": 20,
  "remaining": 17
}
```

## 14.6 회원 타입 확인
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/utils/user-type` |
| **권한** | 인증 필요 |

**Response** `200 OK`
```json
{
  "userType": "INDIVIDUAL",
  "job": "EDITOR"
}
```

## 14.7 닉네임 중복 확인
| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/utils/check-nickname` |
| **권한** | `[PUBLIC]` |

**Query Parameters**
```
nickname: String (required)
```

**Response** `200 OK`
```json
{ "available": true }
```

### 제거된 항목

| 원본 항목 | 사유 |
|----------|------|
| 토큰 발급 | 로그인 API (5.2)에서 처리 |
| 라이센스 발급 (수동) | 다운로드 시 자동 발급으로 통합 |
| 입력값 검증(백엔드) | API가 아닌 내부 Validation 로직 (Bean Validation) |
| 입력값 검증(프론트엔드) | 프론트엔드 코드 영역, API 아님 |
| 일괄 다운로드 (ZIP) | 프론트에서 개별 다운로드 API 순차 호출로 대체 |

---

# 전체 API 목록 요약 (68개)

| # | 섹션 | API 수 |
|---|------|--------|
| 1 | Track (음원) | 7 |
| 2 | Tag (태그) | 4 |
| 3 | Playlist (플레이리스트) | 8 |
| 4 | Play History (재생 기록) | 3 |
| 5 | User Info (회원) | 9 |
| 6 | Subscription (구독제) | 9 |
| 7 | License (라이센스) | 4 |
| 8 | Question (문의/답변) | 8 |
| 9 | Notice (공지사항) | 5 |
| 10 | Likes (즐겨찾기) | 3 |
| 11 | Download Queue (대기 목록) | 3 |
| 12 | Whitelist Channels | 4 |
| 13 | Business License (기업 심사) | 5 |
| 14 | Util (유틸리티) | 7 |
| | **합계** | **79** |
