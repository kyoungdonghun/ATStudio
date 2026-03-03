[EVIDENCE PACK — WI-20260303-ATS-002]

## Review Status
CONDITIONALLY PASS — CRITICAL 0, MAJOR 1, MINOR 5, SUGGESTION 2
Date: 2026-03-03

## Verification Summary

| Metric | Result |
|--------|--------|
| API URL+Method match | 79/79 (100%) |
| HTTP status codes match | 79/79 (100%) |
| Auth/permission match | 79/79 (100%) |
| Extra impl API (not in spec) | 1건 (MAJOR-001) |
| Response wrapping mismatch | 4건 (MINOR-001~004) |
| DTO extra fields | 1건 (MINOR-005) |

## Section-by-Section Results

### 1. Track (7 APIs) — ALL PASS
| # | API | Result |
|---|-----|--------|
| 1.1 | POST /api/tracks | PASS |
| 1.2 | GET /api/tracks | PASS |
| 1.3 | GET /api/tracks/{trackId} | PASS |
| 1.4 | GET /api/tracks/{trackId}/stream | PASS |
| 1.5 | GET /api/tracks/{trackId}/download | PASS |
| 1.6 | PUT /api/tracks/{trackId} | PASS |
| 1.7 | DELETE /api/tracks/{trackId} | PASS |

Evidence: `TrackController.java:26-87`, SecurityConfig lines 60-62,80-82

### 2. Tag (4 APIs) — ALL PASS
| # | API | Result |
|---|-----|--------|
| 2.1 | POST /api/tags | PASS |
| 2.2 | GET /api/tags | PASS (SUGGESTION-002) |
| 2.3 | PUT /api/tags/{tagId} | PASS |
| 2.4 | DELETE /api/tags/{tagId} | PASS |

Evidence: `TagController.java:24-59`, SecurityConfig lines 63,83-85

### 3. Playlist (8 APIs) — ALL PASS
| # | API | Result |
|---|-----|--------|
| 3.1 | POST /api/playlists | PASS |
| 3.2 | GET /api/playlists | PASS (MINOR-002) |
| 3.3 | GET /api/playlists/{id} | PASS |
| 3.4 | POST /api/playlists/{id}/tracks | PASS |
| 3.5 | PUT /api/playlists/{id} | PASS |
| 3.6 | PUT /api/playlists/{id}/tracks | PASS |
| 3.7 | DELETE /api/playlists/{id}/tracks/{trackId} | PASS |
| 3.8 | DELETE /api/playlists/{id} | PASS |

Evidence: `PlaylistController.java:27-122`

### 4. Play History (3 APIs) — ALL PASS
| # | API | Result |
|---|-----|--------|
| 4.1 | POST /api/play-histories | PASS |
| 4.2 | GET /api/play-histories | PASS |
| 4.3 | DELETE /api/play-histories | PASS |

Evidence: `PlayHistoryController.java:23-45`

### 5. User Info (10 spec APIs) — MAJOR-001
| # | API | Result |
|---|-----|--------|
| 5.1 | POST /api/users | PASS |
| 5.2 | POST /api/auth/login | PASS |
| 5.3 | POST /api/auth/social/{provider} | PASS |
| 5.4 | GET /api/users/me | PASS |
| 5.5 | GET /api/users | PASS |
| 5.6 | GET /api/users/{userId} | PASS |
| 5.7 | PUT /api/users/me | PASS |
| 5.8 | PUT /api/users/{userId} | PASS |
| 5.9 | DELETE /api/users/me | PASS |
| 5.10 | PUT /api/users/me/complete-profile | PASS |
| N/A | **PUT /api/users/me/password** | **SPEC MISSING (MAJOR-001)** |

**MAJOR-001 Evidence**:
- 구현: `UserController.java:59-65`
- 권한: `SecurityConfig.java:75` — `.requestMatchers(HttpMethod.PUT, "/api/users/me/password").authenticated()`
- DTO: `UpdatePasswordRequest.java` (exists)
- 추가 이력: REQ-20260228-ATS-010 (CR-C-003), 커밋 `fc371f6`
- api-spec.md 미반영

### 6. Subscription (10 APIs) — ALL PASS
| # | API | Result |
|---|-----|--------|
| 6.1 | GET /api/subscriptions | PASS (MINOR-005) |
| 6.2 | GET /api/subscriptions/{id} | PASS |
| 6.3 | POST /api/user-subscriptions | PASS (SUGGESTION-001) |
| 6.4 | GET /api/user-subscriptions/me | PASS |
| 6.5 | GET /api/user-subscriptions | PASS |
| 6.6 | GET /api/user-subscriptions/{id} | PASS |
| 6.7 | PUT /api/user-subscriptions/me | PASS |
| 6.8 | PUT /api/user-subscriptions/{id} | PASS |
| 6.9 | DELETE /api/user-subscriptions/{id} | PASS |
| 6.10 | DELETE /api/user-subscriptions/me | PASS |

Evidence: `SubscriptionController.java:21-41`, `UserSubscriptionController.java:23-112`

**MINOR-005**: `SubscriptionResponse.java:10-16` — description, isActive 필드가 spec에 없음
**SUGGESTION-001**: UserSubscriptionResponse가 전체 SubscriptionResponse 중첩 (spec: `{ id, name }` only)

### 7. License (4 APIs) — ALL PASS
Evidence: `LicenseController.java:20-54`

### 8. Question/Inquiry (7 APIs) — ALL PASS
Evidence: `QuestionController.java:29-120`

### 9. Notice (5 APIs) — ALL PASS
Evidence: `NoticeController.java:25-74`

### 10. Likes (3 APIs) — ALL PASS
**MINOR-001**: `LikeController.java:33-39` — spec raw array vs ResponseDTO 래핑
Evidence: `LikeController.java:22-48`

### 11. Download Queue (3 APIs) — ALL PASS
**MINOR-003**: `DownloadQueueController.java:33-39` — spec raw array vs ResponseDTO 래핑
Evidence: `DownloadQueueController.java:22-48`

### 12. Whitelist Channels (4 APIs) — ALL PASS
**MINOR-004**: `WhitelistChannelController.java:40-48` — spec raw array vs ResponseDTO 래핑
Evidence: `WhitelistChannelController.java:26-72`

### 13. Company Certification (5 APIs) — ALL PASS
Evidence: `CompanyCertificationController.java:29-87`

### 14. Utility (7 APIs) — ALL PASS
Evidence: `AuthController.java:38-44`, `UtilController.java:27-73`

## Mismatch Summary

| ID | Severity | Section | Spec Location | Implementation | Description |
|----|----------|---------|---------------|----------------|-------------|
| MAJOR-001 | MAJOR | 5. User | api-spec.md Section 5 (누락) | UserController.java:59-65, SecurityConfig.java:75 | PUT /api/users/me/password spec 미등록 |
| MINOR-001 | MINOR | 10. Likes | api-spec.md ~L1223 | LikeController.java:33-39 | 10.2 List: spec raw 배열 vs impl ResponseDTO |
| MINOR-002 | MINOR | 3. Playlist | api-spec.md ~L390 | PlaylistController.java:42-49 | 3.2 List: spec raw 배열 vs impl ResponseDTO |
| MINOR-003 | MINOR | 11. DownloadQueue | api-spec.md ~L1266 | DownloadQueueController.java:33-39 | 11.2 List: spec raw 배열 vs impl ResponseDTO |
| MINOR-004 | MINOR | 12. Whitelist | api-spec.md ~L1320 | WhitelistChannelController.java:40-48 | 12.2 List: spec raw 배열 vs impl ResponseDTO |
| MINOR-005 | MINOR | 6. Subscription | api-spec.md ~L780-793 | SubscriptionResponse.java:10-16 | description, isActive 필드 spec 미정의 |
| SUGGESTION-001 | SUGGESTION | 6. Subscription | api-spec.md ~L820-828 | UserSubscriptionResponse.java:12 | 중첩 객체 과잉 노출 |
| SUGGESTION-002 | SUGGESTION | 2. Tag | - | TagController.java:37-39 | raw List 반환, 다른 엔드포인트와 비일관 |

## Recommended Action for MAJOR-001

api-spec.md Section 5에 추가할 내용:

```
### 5.11 Update Password
- Method: PUT
- URL: /api/users/me/password
- Auth: 인증 필요 (USER/BUSINESS/ADMIN)
- Request Body:
  - currentPassword: string (현재 비밀번호)
  - newPassword: string (새 비밀번호)
- Response: 204 No Content
- Errors: 400 INVALID_ARGUMENT (현재 비밀번호 불일치)
```
