# 04. ATStudio API 명세 요약

> 신규 팀원이 ATStudio의 REST API 전체를 빠르게 파악할 수 있도록 89개 엔드포인트를 그룹별로 정리한 문서입니다.
> API 원본 명세의 단순 복사가 아니라, 각 API가 어떤 상황에서 사용되는지 설명을 추가했습니다.

---

## 인증 정책 요약

| 항목 | 내용 |
|------|------|
| 인증 방식 | Bearer Token (JWT) — `Authorization: Bearer {accessToken}` 헤더 |
| 토큰 갱신 | POST /api/auth/refresh (RefreshToken 사용) |
| `[PUBLIC]` | 비회원 포함 누구나 접근 가능 |
| `[ADMIN]` | ADMIN 역할 전용 |
| 그 외 | 로그인한 회원이면 누구나 접근 가능 |
| 구독자 전용 | SUBSCRIBER 이상 (활성 구독 보유 시) |

## 전체 엔드포인트 현황

| 그룹 | API 수 | 경로 접두사 |
|------|--------|-----------|
| 1. Track (음원) | 8 | /api/tracks |
| 2. Tag (태그) | 4 | /api/tags |
| 3. Playlist (재생목록) | 8 | /api/playlists |
| 4. Play History (재생기록) | 3 | /api/play-histories |
| 5. User (사용자) | 10 | /api/users, /api/auth |
| 6. Subscription (구독) | 10 | /api/subscriptions, /api/user-subscriptions |
| 7. License (라이선스) | 4 | /api/licenses |
| 8. Question (문의) | 7 | /api/questions |
| 9. Notice (공지) | 5 | /api/notices |
| 10. Likes (좋아요) | 3 | /api/likes |
| 11. Download Queue (다운로드 큐) | 3 | /api/download-queue |
| 12. Whitelist Channel (화이트리스트) | 4 | /api/whitelist-channels |
| 13. Company Certification (기업인증) | 5 | /api/company-certifications |
| 14. Utility (유틸) | 8 | /api/auth, /api/utils |
| 15. Album (앨범) | 8 | /api/albums |
| **합계** | **89** | |

---

## 공통 응답 형식

### 성공 응답

```json
{
  "message": "Track fetched",
  "data": { ... }
}
```

목록 조회의 경우:

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

`pageInfo` 필드 설명: `total` = 전체 데이터 수, `start`/`end` = 현재 블록의 시작/끝 페이지 번호, `prev`/`next` = 이전/다음 블록 존재 여부

### 오류 응답

```json
{
  "status": 403,
  "error": "Forbidden",
  "errorCode": "NO_ACTIVE_SUBSCRIPTION",
  "message": "구독이 필요한 서비스입니다."
}
```

`errorCode`는 도메인 특정 오류에만 포함되며, 일반적인 HTTP 오류(400, 500 등)에는 생략됩니다.

---

## 1. Track (음원) — /api/tracks

**음원의 업로드, 조회, 스트리밍, 다운로드를 담당합니다. 음원 업로드와 관리는 ADMIN 전용이며, 조회와 스트리밍은 비회원도 가능합니다. 다운로드는 활성 구독 보유자만 가능합니다.**

#### POST /api/tracks
- **인증**: [ADMIN]
- **요청**: multipart/form-data — title, bpm, tonality, description, audioFile(필수), thumbnail(선택), tagIds
- **응답**: 201 Created — 생성된 음원 정보 (id, title, bpm, tonality, isActive: false, tags, createdAt)
- **설명**: 관리자가 신규 음원을 업로드합니다. 업로드 후 `is_active=0` 상태로 저장되며, 관리자가 별도로 활성화해야 공개됩니다. 비동기로 저품질 preview_file이 생성됩니다.

#### GET /api/tracks
- **인증**: [PUBLIC]
- **요청**: 쿼리 파라미터 — page, size, keyword(제목 검색), genre, mood, instrument, bpmMin, bpmMax, tonality, sort("latest" 또는 "popular")
- **응답**: 200 OK — 페이지네이션된 음원 목록 (is_active=1인 음원만)
- **설명**: 비회원 포함 누구나 음원 목록을 조회할 수 있습니다. 검색 파라미터 모두 선택사항이며, 기본 정렬은 최신순입니다.

#### GET /api/tracks/{trackId}
- **인증**: [PUBLIC]
- **요청**: 경로 변수 — trackId
- **응답**: 200 OK — 음원 상세 (id, title, bpm, tonality, description, audioFile, thumbnail, isActive, playCount, tags, createdAt, updatedAt)
- **설명**: 특정 음원의 상세 정보를 조회합니다. `is_active=0`인 음원은 404를 반환합니다.

#### GET /api/tracks/{trackId}/stream
- **인증**: [PUBLIC]
- **요청**: 경로 변수 — trackId
- **응답**: 200 OK — 오디오 스트림 (Content-Type: audio/mpeg)
- **설명**: 음원 미리듣기용 스트리밍입니다. preview_file이 있으면 저품질 파일을 제공하고, 없으면 원본 audio_file을 폴백으로 제공합니다. 재생 기록은 별도로 POST /api/play-histories를 호출해야 합니다.

#### GET /api/tracks/{trackId}/download
- **인증**: 구독자 전용 (활성 구독 필요)
- **요청**: 경로 변수 — trackId
- **응답**: 200 OK — 파일 다운로드 (Content-Disposition: attachment)
- **설명**: 원본 음원 파일을 다운로드합니다. 다운로드 시 일일 다운로드 횟수를 차감하고, 라이선스를 자동 발급합니다 (기존 라이선스 있으면 재발급 안 함).
- **오류**: 403 DOWNLOAD_LIMIT_EXCEEDED (일일 한도 초과), 403 NO_ACTIVE_SUBSCRIPTION (구독 없음)

#### PUT /api/tracks/{trackId}
- **인증**: [ADMIN]
- **요청**: multipart/form-data — title, bpm, tonality, description, audioFile, thumbnail, tagIds, isActive (모두 선택)
- **응답**: 200 OK — 수정된 음원 상세
- **설명**: 음원 메타데이터와 파일을 수정합니다. `isActive`를 변경해 공개/비공개 전환도 이 API로 처리합니다.

#### DELETE /api/tracks/{trackId}
- **인증**: [ADMIN]
- **요청**: 경로 변수 — trackId
- **응답**: 204 No Content
- **설명**: 소프트 삭제 (`is_active=0`)입니다. 실제 파일은 스토리지에 남습니다.

#### GET /api/tracks/admin
- **인증**: [ADMIN]
- **요청**: 쿼리 파라미터 — page, size, is_active(선택: true/false, 생략 시 전체)
- **응답**: 200 OK — 비활성 음원 포함 전체 목록
- **설명**: 공개/비공개 여부와 관계없이 모든 음원을 조회합니다. 관리 화면(K-7)에서 사용합니다.

---

## 2. Tag (태그) — /api/tags

**태그는 음원을 분류하는 카테고리입니다. 장르(GENRE), 분위기(MOOD), 악기(INSTRUMENT) 3가지 타입이 있습니다. 생성/수정/삭제는 관리자만 가능하며, 목록 조회는 공개입니다.**

#### POST /api/tags
- **인증**: [ADMIN]
- **요청**: `{ "name": "Lo-Fi", "type": "GENRE" }`
- **응답**: 201 Created — 생성된 태그 (id, name, type, createdAt)
- **설명**: 새 태그를 생성합니다. type은 "GENRE", "MOOD", "INSTRUMENT" 중 하나여야 합니다.

#### GET /api/tags
- **인증**: [PUBLIC]
- **요청**: 쿼리 파라미터 — type(선택)
- **응답**: 200 OK — 태그 배열 (ResponseDTO 없이 raw array)
- **설명**: 음원 필터 UI에 사용할 전체 태그 목록입니다. 이 API만 예외적으로 ResponseDTO 래퍼 없이 배열을 직접 반환합니다.

#### PUT /api/tags/{tagId}
- **인증**: [ADMIN]
- **요청**: `{ "name": "Lo-Fi Hip Hop", "type": "GENRE" }`
- **응답**: 200 OK — 수정된 태그
- **설명**: 기존 태그의 이름이나 타입을 수정합니다.

#### DELETE /api/tags/{tagId}
- **인증**: [ADMIN]
- **요청**: 경로 변수 — tagId
- **응답**: 204 No Content
- **설명**: 태그를 삭제합니다.

---

## 3. Playlist (재생목록) — /api/playlists

**재생목록은 구독자 전용 기능입니다. 한 사용자당 최대 3개까지만 생성할 수 있습니다. 자신의 재생목록만 접근 가능합니다.**

#### POST /api/playlists
- **인증**: 구독자 전용
- **요청**: multipart/form-data — title(필수, 최대 50자), description(선택), thumbnail(선택)
- **응답**: 201 Created — 생성된 재생목록 (id, title, description, thumbnail, trackCount: 0, createdAt)
- **설명**: 새 재생목록을 만듭니다. 이미 3개가 있으면 409 오류가 반환됩니다. 프론트엔드에서는 3개 이상이면 생성 버튼을 미리 숨겨야 합니다.
- **오류**: 409 PLAYLIST_LIMIT_EXCEEDED

#### GET /api/playlists
- **인증**: 구독자 전용
- **요청**: 없음
- **응답**: 200 OK — 내 재생목록 목록 (id, title, thumbnail, trackCount, createdAt)
- **설명**: 로그인한 사용자의 재생목록 목록을 반환합니다.

#### GET /api/playlists/{playlistId}
- **인증**: 구독자 전용 (본인 재생목록만)
- **요청**: 경로 변수 — playlistId
- **응답**: 200 OK — 재생목록 상세 + 포함된 트랙 목록 (trackOrder 순서로 정렬)
- **설명**: 재생목록 상세와 트랙 목록(trackOrder, trackId, title, bpm, tonality)을 함께 반환합니다. 타인의 재생목록 접근 시 403 반환.

#### POST /api/playlists/{playlistId}/tracks
- **인증**: 구독자 전용 (본인 재생목록만)
- **요청**: `{ "trackId": 10 }`
- **응답**: 201 Created
- **설명**: 재생목록에 트랙을 추가합니다. 이미 포함된 트랙이면 409 반환.

#### PUT /api/playlists/{playlistId}
- **인증**: 구독자 전용 (본인 재생목록만)
- **요청**: multipart/form-data — title, description, thumbnail (모두 선택)
- **응답**: 200 OK
- **설명**: 재생목록의 제목, 설명, 썸네일을 수정합니다.

#### PUT /api/playlists/{playlistId}/tracks
- **인증**: 구독자 전용 (본인 재생목록만)
- **요청**: `{ "tracks": [{ "trackId": 22, "trackOrder": 1 }, { "trackId": 10, "trackOrder": 2 }] }`
- **응답**: 200 OK
- **설명**: 재생목록 내 트랙 순서를 일괄 변경합니다. 드래그앤드롭 UI에서 사용합니다.

#### DELETE /api/playlists/{playlistId}/tracks/{trackId}
- **인증**: 구독자 전용 (본인 재생목록만)
- **요청**: 경로 변수 — playlistId, trackId
- **응답**: 204 No Content
- **설명**: 재생목록에서 특정 트랙을 제거합니다.

#### DELETE /api/playlists/{playlistId}
- **인증**: 구독자 전용 (본인 재생목록만)
- **요청**: 경로 변수 — playlistId
- **응답**: 204 No Content
- **설명**: 재생목록 전체를 삭제합니다. 포함된 모든 트랙 연결도 함께 삭제됩니다.

---

## 4. Play History (재생기록) — /api/play-histories

**트랙을 큐바에서 재생했을 때 프론트엔드가 명시적으로 호출합니다. 스트리밍 API(GET /api/tracks/{id}/stream)에서 자동 기록되지 않습니다.**

#### POST /api/play-histories
- **인증**: 로그인 필요
- **요청**: `{ "trackId": 10 }`
- **응답**: 201 Created
- **설명**: 재생 기록을 저장하고 해당 트랙의 play_count를 증가시킵니다. 큐바 재생 시작 시점에 프론트엔드가 호출합니다.

#### GET /api/play-histories
- **인증**: 로그인 필요
- **요청**: 쿼리 파라미터 — page(기본 1), size(기본 50)
- **응답**: 200 OK — 내 재생기록 목록 (최신순, id, track 정보, playedAt)
- **설명**: 로그인한 사용자의 재생 기록 목록을 반환합니다.

#### DELETE /api/play-histories
- **인증**: 로그인 필요
- **요청**: `{ "historyIds": [100, 101, 102] }` — 빈 배열이면 전체 삭제
- **응답**: 204 No Content
- **설명**: 선택된 재생기록 또는 전체 재생기록을 삭제합니다.

---

## 5. User (사용자) — /api/users, /api/auth

**회원가입, 로그인, 프로필 관리를 담당합니다. 소셜 로그인 후 추가 정보 입력이 필요한 2단계 가입 플로우를 지원합니다.**

#### POST /api/users
- **인증**: [PUBLIC]
- **요청**: `{ "nickname", "email", "password", "phonePersonal", "phoneCompany", "job", "userType" }`
- **응답**: 201 Created — 생성된 사용자 정보
- **설명**: 이메일/비밀번호 일반 회원가입입니다. userType은 "INDIVIDUAL" 또는 "BUSINESS"입니다.

#### POST /api/auth/login
- **인증**: [PUBLIC]
- **요청**: `{ "email", "password" }`
- **응답**: 200 OK — `{ "accessToken", "refreshToken", "tokenType": "Bearer", "expiresIn": 3600 }`
- **설명**: 이메일/비밀번호로 로그인합니다. AccessToken과 RefreshToken을 반환합니다.

#### POST /api/auth/social/{provider}
- **인증**: [PUBLIC]
- **요청**: `{ "authorizationCode": "..." }` — provider는 GOOGLE, KAKAO, NAVER
- **응답**: 200 OK — 로그인 토큰 + `"isProfileComplete": true/false`
- **설명**: OAuth2.0 소셜 로그인입니다. 최초 가입 시 `isProfileComplete: false`를 반환하며, 프론트엔드는 이 값을 보고 프로필 완성 화면(5.10)으로 이동해야 합니다.

#### PUT /api/users/me/complete-profile
- **인증**: 로그인 필요 (isProfileComplete=false인 사용자만)
- **요청**: `{ "nickname", "phonePersonal", "phoneCompany", "job", "userType" }`
- **응답**: 200 OK — 업데이트된 프로필
- **설명**: 소셜 로그인 최초 가입 후 추가 정보를 입력하는 API입니다. userType은 이 단계에서만 설정할 수 있습니다.
- **오류**: 409 NICKNAME_DUPLICATED

#### GET /api/users/me
- **인증**: 로그인 필요
- **요청**: 없음
- **응답**: 200 OK — 내 프로필 (id, nickname, email, phonePersonal, phoneCompany, job, userType, role, isVerified, createdAt)
- **설명**: 현재 로그인한 사용자의 프로필을 반환합니다.

#### GET /api/users
- **인증**: [ADMIN]
- **요청**: 쿼리 파라미터 — page, size, keyword(닉네임/이메일 검색), userType
- **응답**: 200 OK — 페이지네이션된 사용자 목록
- **설명**: 관리자가 전체 사용자 목록을 조회합니다.

#### GET /api/users/{userId}
- **인증**: [ADMIN]
- **요청**: 경로 변수 — userId
- **응답**: 200 OK — 사용자 상세
- **설명**: 관리자가 특정 사용자의 상세 정보를 조회합니다.

#### PUT /api/users/me
- **인증**: 로그인 필요
- **요청**: `{ "nickname", "phonePersonal", "phoneCompany", "job" }`
- **응답**: 200 OK — 업데이트된 프로필
- **설명**: 사용자가 자신의 프로필을 수정합니다. userType과 role은 이 API로 변경할 수 없습니다.

#### PUT /api/users/{userId}
- **인증**: [ADMIN]
- **요청**: `{ "role", "isVerified" }`
- **응답**: 200 OK
- **설명**: 관리자가 특정 사용자의 역할과 인증 상태를 변경합니다.

#### DELETE /api/users/me
- **인증**: 로그인 필요
- **요청**: `{ "password": "현재 비밀번호" }`
- **응답**: 204 No Content
- **설명**: 회원 탈퇴입니다. 소프트 삭제(`is_deleted=1`)로 처리됩니다.

#### PUT /api/users/me/password
- **인증**: 로그인 필요
- **요청**: `{ "currentPassword", "newPassword" }`
- **응답**: 204 No Content
- **설명**: 비밀번호를 변경합니다. 현재 비밀번호를 먼저 검증합니다.
- **오류**: 400 INVALID_ARGUMENT (현재 비밀번호 불일치)

---

## 6. Subscription (구독) — /api/subscriptions, /api/user-subscriptions

**구독 플랜 목록 조회는 공개이며, 실제 구독 신청/변경/취소는 로그인이 필요합니다. BUSINESS 타입 회원은 기업 인증 승인 후에만 구독할 수 있습니다.**

#### GET /api/subscriptions
- **인증**: [PUBLIC]
- **요청**: 쿼리 파라미터 — userType(선택)
- **응답**: 200 OK — 구독 플랜 목록 (id, name, userType, priceMonthly, priceYearly, downloadPerDay, maxWhitelistChannels, isActive)
- **설명**: 구독 플랜 목록을 조회합니다. userType 필터로 개인/기업 플랜을 구분할 수 있습니다.

#### GET /api/subscriptions/{subscriptionId}
- **인증**: [PUBLIC]
- **요청**: 경로 변수 — subscriptionId
- **응답**: 200 OK — 구독 플랜 상세
- **설명**: 특정 구독 플랜의 상세 정보를 조회합니다.

#### POST /api/user-subscriptions
- **인증**: 로그인 필요
- **요청**: `{ "subscriptionId", "billingCycle" }` — billingCycle은 "MONTHLY" 또는 "YEARLY"
- **응답**: 201 Created — 가입된 구독 (id, subscription, billingCycle, status: "ACTIVE", startedAt, expiresAt)
- **설명**: 플랜을 선택하고 구독을 시작합니다. BUSINESS 회원은 기업 인증 승인 필수.
- **오류**: 403 COMPANY_CERTIFICATION_REQUIRED

#### GET /api/user-subscriptions/me
- **인증**: 로그인 필요
- **요청**: 없음
- **응답**: 200 OK — 내 현재 구독 상태 (구독 없으면 null)
- **설명**: 현재 로그인한 사용자의 구독 상태를 반환합니다.

#### GET /api/user-subscriptions
- **인증**: [ADMIN]
- **요청**: 쿼리 파라미터 — page, size
- **응답**: 200 OK — 전체 구독 목록 (페이지네이션)
- **설명**: 관리자가 모든 사용자의 구독 현황을 조회합니다.

#### GET /api/user-subscriptions/{userSubscriptionId}
- **인증**: [ADMIN]
- **요청**: 경로 변수 — userSubscriptionId
- **응답**: 200 OK — 특정 구독 상세
- **설명**: 관리자가 특정 구독 레코드의 상세를 조회합니다.

#### PUT /api/user-subscriptions/me
- **인증**: 로그인 필요
- **요청**: `{ "subscriptionId", "billingCycle" }`
- **응답**: 200 OK — `{ changeType: "UPGRADE" 또는 "DOWNGRADE", proratedAmount, subscription, billingCycle, status, startedAt, expiresAt }`
- **설명**: 구독 플랜을 변경합니다. **UPGRADE**는 즉시 적용되며 잔여 기간 비례 금액이 추가 청구됩니다. **DOWNGRADE**는 현재 기간 만료 후 자동 적용됩니다(pending 저장).

#### PUT /api/user-subscriptions/{userSubscriptionId}
- **인증**: [ADMIN]
- **요청**: 변경할 구독 정보
- **응답**: 200 OK
- **설명**: 관리자가 특정 사용자의 구독 정보를 수정합니다.

#### DELETE /api/user-subscriptions/{userSubscriptionId}
- **인증**: [ADMIN]
- **요청**: 경로 변수 — userSubscriptionId
- **응답**: 204 No Content
- **설명**: 관리자가 특정 구독을 강제 취소/삭제합니다.

#### DELETE /api/user-subscriptions/me
- **인증**: 로그인 필요
- **요청**: 없음
- **응답**: 204 No Content
- **설명**: 사용자가 자신의 구독을 취소합니다. 상태가 CANCELLED로 변경되지만 `expiresAt`까지 서비스 이용 가능합니다 (유예 기간).
- **오류**: 404 SUBSCRIPTION_NOT_FOUND

---

## 7. License (라이선스) — /api/licenses

**라이선스는 트랙 다운로드 시 자동 발급됩니다. 동일한 트랙을 다시 다운로드해도 라이선스가 중복 발급되지 않습니다. 라이선스를 통해 해당 트랙의 사용 권한을 증명할 수 있습니다.**

#### GET /api/licenses/me
- **인증**: 로그인 필요
- **요청**: 쿼리 파라미터 — page, size
- **응답**: 200 OK — 내 라이선스 목록 (id, track 정보, licenseCode, issuedAt)
- **설명**: 내가 다운로드한 트랙의 라이선스 목록입니다. 구독이 만료돼도 이미 발급된 라이선스는 계속 조회됩니다.

#### GET /api/users/{userId}/licenses
- **인증**: [ADMIN]
- **요청**: 경로 변수 — userId + 쿼리 파라미터 page, size
- **응답**: 200 OK — 특정 사용자의 라이선스 목록
- **설명**: 관리자가 특정 사용자의 라이선스 목록을 조회합니다.

#### GET /api/licenses/{licenseId}
- **인증**: 로그인 필요 (본인 라이선스만)
- **요청**: 경로 변수 — licenseId
- **응답**: 200 OK — 라이선스 상세 (id, track 정보, licenseCode, issuedAt, user 정보)
- **설명**: 특정 라이선스의 상세 정보를 조회합니다. 타인의 라이선스 접근 시 403 반환.

#### GET /api/users/{userId}/licenses/{licenseId}
- **인증**: [ADMIN]
- **요청**: 경로 변수 — userId, licenseId
- **응답**: 200 OK — 라이선스 상세 (licenseCode 포함)
- **설명**: 관리자가 특정 사용자의 특정 라이선스 상세를 조회합니다.

---

## 8. Question (문의) — /api/questions

**사용자가 문의를 남기면 관리자가 답변합니다. 공개 문의는 모든 회원이 볼 수 있고, 비공개 문의는 작성자와 관리자만 볼 수 있습니다. 문의 수정은 제공하지 않습니다.**

#### POST /api/questions
- **인증**: 로그인 필요
- **요청**: multipart/form-data — title, content, category, isPublic, attachments(선택)
- **응답**: 201 Created — 생성된 문의 (id, title, category, isPublic, status: "OPEN", attachments, createdAt)
- **설명**: 새 문의를 작성합니다. category는 "DOWNLOAD", "PAYMENT", "COPYRIGHT", "PRODUCTION", "OTHER" 중 하나입니다.

#### POST /api/questions/{questionId}/answers
- **인증**: 로그인 필요 (문의 작성자 또는 ADMIN)
- **요청**: `{ "content": "..." }`
- **응답**: 201 Created — 작성된 답변 (id, content, user 정보, createdAt)
- **설명**: 문의에 답변을 작성합니다. 관리자가 최초 답변 시 문의 상태가 OPEN → IN_PROGRESS로 자동 변경됩니다.

#### GET /api/questions
- **인증**: 로그인 필요
- **요청**: 쿼리 파라미터 — page, size, category, status, mine(내 문의만 조회)
- **응답**: 200 OK — 문의 목록 (페이지네이션)
- **설명**: 일반 사용자는 공개 문의 + 내 비공개 문의를 조회합니다. 관리자는 전체 조회 가능.

#### GET /api/questions/{questionId}
- **인증**: 로그인 필요 (비공개 문의: 작성자 + ADMIN만)
- **요청**: 경로 변수 — questionId
- **응답**: 200 OK — 문의 상세 (title, content, category, isPublic, status, user, attachments, answers, createdAt)
- **설명**: 문의 상세와 모든 답변을 함께 반환합니다.

#### GET /api/questions/{questionId}/attachments/{attachmentId}
- **인증**: 로그인 필요 (문의 접근 권한과 동일)
- **요청**: 경로 변수 — questionId, attachmentId
- **응답**: 200 OK — 파일 다운로드
- **설명**: 문의에 첨부된 파일을 다운로드합니다.

#### PUT /api/questions/{questionId}/status
- **인증**: [ADMIN]
- **요청**: `{ "status": "RESOLVED" }`
- **응답**: 200 OK
- **설명**: 관리자가 문의 상태를 변경합니다. 상태 흐름: OPEN → IN_PROGRESS → RESOLVED → CLOSED

#### DELETE /api/questions/{questionId}
- **인증**: 로그인 필요 (OPEN 상태의 작성자) 또는 [ADMIN]
- **요청**: 경로 변수 — questionId
- **응답**: 204 No Content
- **설명**: 문의를 삭제합니다. 사용자는 OPEN 상태인 본인 문의만 삭제 가능합니다.

---

## 9. Notice (공지) — /api/notices

**공지사항은 비회원 포함 누구나 읽을 수 있습니다. 작성/수정/삭제는 관리자만 가능합니다. isPinned=true인 공지는 목록 상단에 고정됩니다.**

#### POST /api/notices
- **인증**: [ADMIN]
- **요청**: `{ "title", "content", "isPinned": true/false }`
- **응답**: 201 Created — 생성된 공지
- **설명**: 새 공지사항을 작성합니다.

#### GET /api/notices
- **인증**: [PUBLIC]
- **요청**: 쿼리 파라미터 — page, size
- **응답**: 200 OK — 공지 목록 (isPinned=true인 항목이 상단 고정)
- **설명**: 전체 공지 목록을 조회합니다.

#### GET /api/notices/{noticeId}
- **인증**: [PUBLIC]
- **요청**: 경로 변수 — noticeId
- **응답**: 200 OK — 공지 상세 (id, title, content, isPinned, createdAt, updatedAt)
- **설명**: 특정 공지의 전문을 조회합니다.

#### PUT /api/notices/{noticeId}
- **인증**: [ADMIN]
- **요청**: `{ "title", "content", "isPinned" }`
- **응답**: 200 OK
- **설명**: 공지를 수정합니다.

#### DELETE /api/notices/{noticeId}
- **인증**: [ADMIN]
- **요청**: 경로 변수 — noticeId
- **응답**: 204 No Content
- **설명**: 공지를 삭제합니다.

---

## 10. Likes (좋아요) — /api/likes

**로그인한 사용자가 마음에 드는 트랙을 즐겨찾기 할 수 있습니다. 좋아요 목록은 재생 큐에 빠르게 추가하거나 나중에 다운로드할 트랙을 저장해두는 용도입니다.**

#### POST /api/likes/{trackId}
- **인증**: 로그인 필요
- **요청**: 경로 변수 — trackId
- **응답**: 201 Created
- **설명**: 트랙을 좋아요 목록에 추가합니다.
- **오류**: 409 TRACK_ALREADY_IN_LIKES (이미 좋아요한 트랙)

#### GET /api/likes
- **인증**: 로그인 필요
- **요청**: 없음
- **응답**: 200 OK — 좋아요 트랙 목록 (trackId, title, bpm, tonality, thumbnail, createdAt)
- **설명**: 내 좋아요 목록을 반환합니다.

#### DELETE /api/likes/{trackId}
- **인증**: 로그인 필요
- **요청**: 경로 변수 — trackId
- **응답**: 204 No Content
- **설명**: 트랙을 좋아요 목록에서 제거합니다.
- **오류**: 404 TRACK_NOT_IN_LIKES

---

## 11. Download Queue (다운로드 큐) — /api/download-queue

**여러 트랙을 한꺼번에 다운로드하고 싶을 때 큐에 담아두는 기능입니다. 구매 개념이 없으므로 "장바구니"가 아닌 "다운로드 큐"라고 부릅니다. 큐에 담긴 트랙을 실제로 다운로드하려면 GET /api/tracks/{id}/download를 순차 호출해야 합니다.**

#### POST /api/download-queue/{trackId}
- **인증**: 로그인 필요
- **요청**: 경로 변수 — trackId
- **응답**: 201 Created
- **설명**: 트랙을 다운로드 큐에 추가합니다.
- **오류**: 409 TRACK_ALREADY_IN_QUEUE

#### GET /api/download-queue
- **인증**: 로그인 필요
- **요청**: 없음
- **응답**: 200 OK — 큐 트랙 목록 (trackId, title, bpm, tonality, thumbnail, createdAt)
- **설명**: 현재 다운로드 큐에 담긴 트랙 목록을 반환합니다.

#### DELETE /api/download-queue/{trackId}
- **인증**: 로그인 필요
- **요청**: 경로 변수 — trackId
- **응답**: 204 No Content
- **설명**: 큐에서 특정 트랙을 제거합니다.
- **오류**: 404 TRACK_NOT_IN_QUEUE

---

## 12. Whitelist Channel (화이트리스트) — /api/whitelist-channels

**구독자가 트랙을 사용할 유튜브 채널을 등록하는 기능입니다. 구독 플랜별로 등록 가능한 최대 채널 수가 다릅니다 (maxWhitelistChannels). 채널 URL은 youtube.com 도메인만 허용됩니다.**

#### POST /api/whitelist-channels
- **인증**: 구독자 전용
- **요청**: `{ "channelUrl": "https://youtube.com/@mychannel", "channelName": "My Channel" }`
- **응답**: 201 Created
- **설명**: 유튜브 채널을 화이트리스트에 등록합니다. 채널 URL은 host가 정확히 `youtube.com`이거나 `.youtube.com`으로 끝나야 합니다.
- **오류**: 403 WHITELIST_CHANNEL_LIMIT_EXCEEDED, 400 INVALID_ARGUMENT (잘못된 URL)

#### GET /api/whitelist-channels
- **인증**: 로그인 필요
- **요청**: 없음
- **응답**: 200 OK — 내 채널 목록 (id, channelUrl, channelName, createdAt)
- **설명**: 등록된 내 채널 목록을 반환합니다.

#### PUT /api/whitelist-channels/{channelId}
- **인증**: 로그인 필요 (본인 채널만)
- **요청**: `{ "channelUrl", "channelName" }`
- **응답**: 200 OK
- **설명**: 채널 URL과 이름을 수정합니다.

#### DELETE /api/whitelist-channels/{channelId}
- **인증**: 로그인 필요 (본인 채널만)
- **요청**: 경로 변수 — channelId
- **응답**: 204 No Content
- **설명**: 채널을 화이트리스트에서 삭제합니다.

---

## 13. Company Certification (기업인증) — /api/company-certifications

**BUSINESS 타입 회원이 구독하려면 반드시 기업 인증 심사를 통과해야 합니다. 사업자등록증 등 서류를 업로드하면 관리자가 검토하고 승인/반려합니다. 승인 시 certification_code가 발급됩니다.**

#### POST /api/company-certifications
- **인증**: 로그인 필요 (BUSINESS 회원만)
- **요청**: multipart/form-data — documents(필수, 복수 파일)
- **응답**: 201 Created — `{ "id", "status": "PENDING", "documentPath", "createdAt" }`
- **설명**: 기업 인증 심사를 신청합니다. PENDING이나 APPROVED 상태의 신청이 이미 있으면 409 반환.

#### GET /api/company-certifications/me
- **인증**: 로그인 필요 (BUSINESS 회원만)
- **요청**: 없음
- **응답**: 200 OK — `{ "id", "status", "adminNote", "certificationCode", "createdAt" }`
- **설명**: 내 심사 신청 상태를 조회합니다. status: PENDING/APPROVED/REVISION_REQUESTED/REJECTED

#### GET /api/company-certifications
- **인증**: [ADMIN]
- **요청**: 쿼리 파라미터 — status(선택), page, size
- **응답**: 200 OK — 심사 신청 목록 (페이지네이션)
- **설명**: 관리자가 전체 기업 인증 신청 목록을 조회합니다.

#### GET /api/company-certifications/{certificationId}
- **인증**: [ADMIN]
- **요청**: 경로 변수 — certificationId
- **응답**: 200 OK — 신청 상세
- **설명**: 관리자가 특정 신청의 상세 정보를 조회합니다.

#### PUT /api/company-certifications/{certificationId}
- **인증**: [ADMIN]
- **요청**: `{ "status": "APPROVED", "adminNote": "서류 확인 완료" }`
- **응답**: 200 OK — `{ "id", "status", "certificationCode", "approvedAt" }`
- **설명**: 심사 결과를 입력합니다. APPROVED 시 certification_code가 자동 생성됩니다.

---

## 14. Utility (유틸) — /api/auth, /api/utils

**인증 보조 기능과 프론트엔드 상태 확인에 사용하는 유틸 API들입니다.**

#### POST /api/auth/refresh
- **인증**: [PUBLIC] (RefreshToken 필요)
- **요청**: `{ "refreshToken": "..." }`
- **응답**: 200 OK — `{ "accessToken"(신규), "refreshToken"(신규), "tokenType", "expiresIn" }`
- **설명**: AccessToken이 만료됐을 때 RefreshToken으로 새 토큰을 발급받습니다.
- **오류**: 401 REFRESH_TOKEN_EXPIRED, 401 INVALID_TOKEN

#### GET /api/utils/check-email
- **인증**: [PUBLIC]
- **요청**: 쿼리 파라미터 — email
- **응답**: `{ "available": true/false }`
- **설명**: 이메일 중복 여부를 확인합니다. 회원가입 UI에서 사용합니다.

#### GET /api/utils/check-phone
- **인증**: [PUBLIC]
- **요청**: 쿼리 파라미터 — phone
- **응답**: `{ "available": true/false }`
- **설명**: 전화번호 중복 여부를 확인합니다.

#### GET /api/utils/subscription-status
- **인증**: 로그인 필요
- **요청**: 없음
- **응답**: `{ "hasSubscription", "planName", "userType", "downloadPerDay", "maxWhitelistChannels" }`
- **설명**: 현재 사용자의 구독 상태와 혜택 요약을 반환합니다. UI에서 다운로드 가능 여부 등을 판단할 때 사용합니다.

#### GET /api/utils/download-count
- **인증**: 로그인 필요
- **요청**: 없음
- **응답**: `{ "todayDownloads", "dailyLimit", "remaining", "nextResetAt" }`
- **설명**: 오늘의 다운로드 현황을 반환합니다. `nextResetAt`은 일일 카운터가 리셋되는 시각(ISO-8601)입니다.

#### GET /api/utils/user-type
- **인증**: 로그인 필요
- **요청**: 없음
- **응답**: `{ "userType", "job" }`
- **설명**: 현재 사용자의 타입(INDIVIDUAL/BUSINESS)과 직업을 반환합니다.

#### GET /api/utils/check-nickname
- **인증**: [PUBLIC]
- **요청**: 쿼리 파라미터 — nickname
- **응답**: `{ "available": true/false }`
- **설명**: 닉네임 중복 여부를 확인합니다.

#### GET /api/utils/subscription-change-preview
- **인증**: 구독자 전용
- **요청**: 쿼리 파라미터 — subscriptionId, billingCycle
- **응답**: `{ "changeType"("UPGRADE" 또는 "DOWNGRADE"), "proratedAmount", "effectiveDate", "newPlanName", "newBillingCycle" }`
- **설명**: 구독 변경 전 예상 금액과 적용 일정을 미리 확인합니다. 실제 변경을 수행하지 않습니다.

---

## 15. Album (앨범) — /api/albums

**여러 트랙을 묶어 앨범 단위로 관리합니다. 앨범 생성/수정/삭제와 트랙 추가/제거는 관리자 전용입니다. 목록과 상세 조회는 비회원도 가능합니다.**

#### POST /api/albums
- **인증**: [ADMIN]
- **요청**: multipart/form-data — title(필수), description(선택), thumbnailFile(선택)
- **응답**: 201 Created — `{ "id", "title", "description", "thumbnailUrl", "trackCount": 0, "createdAt" }`
- **설명**: 새 앨범을 생성합니다.

#### GET /api/albums
- **인증**: [PUBLIC]
- **요청**: 없음
- **응답**: 200 OK — 앨범 목록 (id, title, thumbnailUrl, trackCount)
- **설명**: 전체 앨범 목록을 조회합니다.

#### GET /api/albums/{id}
- **인증**: [PUBLIC]
- **요청**: 경로 변수 — id
- **응답**: 200 OK — `{ "id", "title", "description", "thumbnailUrl", "tracks"[{trackId, title, artistName, thumbnailUrl, order}], "createdAt" }`
- **설명**: 앨범 상세와 포함된 트랙 목록(순서 포함)을 반환합니다.

#### PUT /api/albums/{id}
- **인증**: [ADMIN]
- **요청**: multipart/form-data — title, description, thumbnailFile (모두 선택)
- **응답**: 200 OK — 수정된 앨범 정보
- **설명**: 앨범 메타데이터를 수정합니다.

#### DELETE /api/albums/{id}
- **인증**: [ADMIN]
- **요청**: 경로 변수 — id
- **응답**: 204 No Content
- **설명**: 앨범을 삭제합니다.

#### POST /api/albums/{id}/tracks
- **인증**: [ADMIN]
- **요청**: `{ "trackId": number }`
- **응답**: 200 OK — 업데이트된 앨범 상세
- **설명**: 앨범에 트랙을 추가합니다.
- **오류**: 404 RESOURCE_NOT_FOUND, 409 RESOURCE_DUPLICATE

#### DELETE /api/albums/{id}/tracks/{trackId}
- **인증**: [ADMIN]
- **요청**: 경로 변수 — id, trackId
- **응답**: 204 No Content
- **설명**: 앨범에서 트랙을 제거합니다.

#### PUT /api/albums/{id}/tracks
- **인증**: [ADMIN]
- **요청**: `{ "trackOrders": [{ "trackId": number, "order": number }] }`
- **응답**: 200 OK — 업데이트된 앨범 상세
- **설명**: 앨범 내 트랙 순서를 일괄 변경합니다.

---

## 원본 참조 문서

| 문서 | 경로 | 내용 |
|------|------|------|
| API 명세 원본 | `docs/design/api-spec.md` | v6 확정 API 명세 전체 (89개) |
