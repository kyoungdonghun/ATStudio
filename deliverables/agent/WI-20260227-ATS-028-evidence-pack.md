# WI-20260227-ATS-028 Evidence Pack: Backend Audit Phase 1 Checklists

> **WI**: WI-20260227-ATS-028
> **Role**: SA (Read-only document analysis)
> **Date**: 2026-02-27
> **REQ**: REQ-20260227-ATS-009
> **Purpose**: This evidence pack serves as the definitive checklist for Phase 2 (cr-A/B/C code review) and Phase 3 (pg security audit).

---

## Table of Contents

1. [API Verification Checklist (79 APIs)](#1-api-verification-checklist-79-apis)
2. [DB Verification Checklist (21 Tables)](#2-db-verification-checklist-21-tables)
3. [Coding Standards Rules Checklist](#3-coding-standards-rules-checklist)
4. [Domain Business Rules](#4-domain-business-rules)
5. [Phase 2 cr-A/B/C Domain Split](#5-phase-2-cr-abc-domain-split)

---

## 1. API Verification Checklist (79 APIs)

### 1.x Track (7 APIs) -- cr-A

| # | Method | URL | Auth | Key Verification Points |
|---|--------|-----|------|------------------------|
| 1.1 | POST | `/api/tracks` | ADMIN | 1) multipart/form-data handling (audioFile required, thumbnail optional, tagIds optional) 2) is_active=0 default on creation 3) track_tags bulk insert 4) File storage path generation |
| 1.2 | GET | `/api/tracks` | PUBLIC | 1) Only is_active=1 returned 2) Dynamic filtering (keyword, genre, mood, instrument, bpmMin/Max, tonality) -- verify Specification or query builder 3) Pagination (dataList + pageInfo with block pagination) 4) sort: "latest" vs "popular" (play_count desc) |
| 1.3 | GET | `/api/tracks/{trackId}` | PUBLIC | 1) 404 if not found OR is_active=0 2) Tags included via @EntityGraph or JOIN FETCH (N+1 check) |
| 1.4 | GET | `/api/tracks/{trackId}/stream` | PUBLIC | 1) preview_file first, audio_file fallback if NULL 2) Content-Type: audio/mpeg 3) No play history recording in this endpoint (separated to 4.1) |
| 1.5 | GET | `/api/tracks/{trackId}/download` | Subscriber | 1) Subscription ACTIVE check 2) Daily download COUNT query: `WHERE user_id=? AND DATE(downloaded_at)=CURDATE()` vs plan download_per_day 3) track_downloads record insert 4) License dedup: check existing (user_id, track_id) before issuing UUID 5) Content-Disposition: attachment 6) Error codes: DOWNLOAD_LIMIT_EXCEEDED, NO_ACTIVE_SUBSCRIPTION |
| 1.6 | PUT | `/api/tracks/{trackId}` | ADMIN | 1) Partial update (all fields optional) 2) If audioFile changed: preview_file regeneration 3) If tagIds changed: track_tags replacement + tracks.updated_at update 4) multipart/form-data with isActive boolean |
| 1.7 | DELETE | `/api/tracks/{trackId}` | ADMIN | 1) Soft delete: is_active=0 2) track_tags physical deletion on soft delete (per UC SOUND-016) 3) 204 No Content |

### 2.x Tag (4 APIs) -- cr-A

| # | Method | URL | Auth | Key Verification Points |
|---|--------|-----|------|------------------------|
| 2.1 | POST | `/api/tags` | ADMIN | 1) name UNIQUE constraint -- 409 on duplicate 2) type: MOOD/GENRE/INSTRUMENT enum validation |
| 2.2 | GET | `/api/tags` | PUBLIC | 1) Optional type filter 2) Returns flat array (no pagination) 3) No auth required |
| 2.3 | PUT | `/api/tags/{tagId}` | ADMIN | 1) name UNIQUE check on update 2) 409 on duplicate name |
| 2.4 | DELETE | `/api/tags/{tagId}` | ADMIN | 1) Must delete track_tags records FIRST (application-level cascade) 2) Then delete tag 3) 204 No Content |

### 3.x Playlist (8 APIs) -- cr-A

| # | Method | URL | Auth | Key Verification Points |
|---|--------|-----|------|------------------------|
| 3.1 | POST | `/api/playlists` | Subscriber | 1) Active subscription check 2) multipart/form-data (title required, max 50) 3) is_active=1 default |
| 3.2 | GET | `/api/playlists` | Subscriber | 1) Active subscription check 2) Returns only current user's playlists 3) trackCount calculated field |
| 3.3 | GET | `/api/playlists/{playlistId}` | Subscriber+Owner | 1) Active subscription check 2) Owner-only access (403 for others) 3) Tracks with trackOrder included 4) N+1 check on track details |
| 3.4 | POST | `/api/playlists/{playlistId}/tracks` | Subscriber+Owner | 1) Owner check 2) Duplicate track check (409 Conflict) 3) trackOrder = max existing + 1 |
| 3.5 | PUT | `/api/playlists/{playlistId}` | Subscriber+Owner | 1) Owner check 2) multipart/form-data (partial update) |
| 3.6 | PUT | `/api/playlists/{playlistId}/tracks` | Subscriber+Owner | 1) Owner check 2) Batch trackOrder update 3) Validate all trackIds belong to playlist |
| 3.7 | DELETE | `/api/playlists/{playlistId}/tracks/{trackId}` | Subscriber+Owner | 1) Owner check 2) 204 No Content |
| 3.8 | DELETE | `/api/playlists/{playlistId}` | Subscriber+Owner | 1) Owner check 2) Cascade: delete playlist_tracks then playlist 3) 204 No Content |

### 4.x Play History (3 APIs) -- cr-A

| # | Method | URL | Auth | Key Verification Points |
|---|--------|-----|------|------------------------|
| 4.1 | POST | `/api/play-histories` | Auth | 1) Creates play_histories record 2) Increments tracks.play_count atomically 3) 201 Created |
| 4.2 | GET | `/api/play-histories` | Auth | 1) Current user only 2) Newest first (played_at DESC) 3) Pagination (dataList + pageInfo) 4) @EntityGraph for track info (N+1 check) |
| 4.3 | DELETE | `/api/play-histories` | Auth | 1) Request body with historyIds array 2) Empty array [] = delete ALL for user 3) Verify ownership of each historyId 4) 204 No Content |

### 5.x User Info (10 APIs) -- cr-C

| # | Method | URL | Auth | Key Verification Points |
|---|--------|-----|------|------------------------|
| 5.1 | POST | `/api/users` | PUBLIC | 1) BCrypt password hashing 2) Unique checks: email, nickname, phonePersonal 3) is_verified=0, role=USER default 4) userType: INDIVIDUAL/BUSINESS 5) 201 Created |
| 5.2 | POST | `/api/auth/login` | PUBLIC | 1) Email lookup + BCrypt verify 2) is_deleted=0 check (403 for withdrawn) 3) AccessToken + RefreshToken issuance 4) 401 for bad credentials |
| 5.3 | POST | `/api/auth/social/{provider}` | PUBLIC | 1) OAuth code exchange with provider (GOOGLE/KAKAO/NAVER) 2) social_accounts (provider, provider_id) lookup 3) New user: minimal record (password=NULL, phone=NULL, job=NULL) + social_accounts link 4) isProfileComplete derivation: phone_personal IS NOT NULL AND job IS NOT NULL 5) Token issuance |
| 5.10 | PUT | `/api/users/me/complete-profile` | Auth (incomplete) | 1) Gate: only if isProfileComplete=false 2) Sets nickname, phonePersonal, job, userType 3) Nickname duplicate check (409 NICKNAME_DUPLICATED) 4) userType can ONLY be set here (immutable after) |
| 5.4 | GET | `/api/users/me` | Auth | 1) Extract userId from JWT 2) Return full profile including role, isVerified |
| 5.5 | GET | `/api/users` | ADMIN | 1) Pagination 2) keyword search (nickname/email) 3) userType filter 4) Only is_deleted=0 |
| 5.6 | GET | `/api/users/{userId}` | ADMIN | 1) Admin-only 2) Full user detail |
| 5.7 | PUT | `/api/users/me` | Auth | 1) Editable: nickname, phonePersonal, phoneCompany, job 2) NOT editable: email, userType 3) Nickname duplicate check |
| 5.8 | PUT | `/api/users/{userId}` | ADMIN | 1) Admin-only 2) Editable: role, isVerified only |
| 5.9 | DELETE | `/api/users/me` | Auth | 1) Password re-verification in request body 2) Soft delete: is_deleted=1 3) 204 No Content 4) 401 for wrong password |

### 6.x Subscription (10 APIs) -- cr-B

| # | Method | URL | Auth | Key Verification Points |
|---|--------|-----|------|------------------------|
| 6.1 | GET | `/api/subscriptions` | PUBLIC | 1) Optional userType filter 2) Only is_active=1 plans 3) Returns flat array |
| 6.2 | GET | `/api/subscriptions/{subscriptionId}` | PUBLIC | 1) Plan detail including prices, limits |
| 6.3 | POST | `/api/user-subscriptions` | Auth | 1) **1-user-1-subscription**: user_subscriptions.user_id is UNIQUE 2) BUSINESS type: verify company_certifications.status=APPROVED (403 COMPANY_CERTIFICATION_REQUIRED) 3) subscription_payments record created 4) started_at/expires_at calculation based on billingCycle 5) status=ACTIVE |
| 6.4 | GET | `/api/user-subscriptions/me` | Auth | 1) Current user's subscription 2) Return null/404 if none |
| 6.5 | GET | `/api/user-subscriptions` | ADMIN | 1) Pagination 2) Full subscription list |
| 6.6 | GET | `/api/user-subscriptions/{id}` | ADMIN | 1) Admin detail view |
| 6.7 | PUT | `/api/user-subscriptions/me` | Auth | 1) Proration calculation 2) Immediate plan change 3) subscription_payments record with prorated amount 4) New expires_at calculation |
| 6.8 | PUT | `/api/user-subscriptions/{id}` | ADMIN | 1) Admin update |
| 6.9 | DELETE | `/api/user-subscriptions/{id}` | ADMIN | 1) Admin cancel/delete -- **AMBIGUITY**: UC says "deletes or sets CANCELLED". Verify which. 2) 204 No Content |
| 6.10 | DELETE | `/api/user-subscriptions/me` | Auth | 1) Must have ACTIVE subscription 2) Set status=CANCELLED (not physical delete) 3) 404 SUBSCRIPTION_NOT_FOUND if none 4) 204 No Content |

### 7.x License (4 APIs) -- cr-A

| # | Method | URL | Auth | Key Verification Points |
|---|--------|-----|------|------------------------|
| 7.1 | GET | `/api/licenses/me` | Auth | 1) Current user's licenses 2) Pagination (dataList + pageInfo) 3) issuedAt mapped from created_at (no separate column) 4) Viewable even without active subscription |
| 7.2 | GET | `/api/users/{userId}/licenses` | ADMIN | 1) Admin-only 2) Same format as 7.1 |
| 7.3 | GET | `/api/licenses/{licenseId}` | Auth+Owner | 1) Owner-only (403 for others) 2) Includes track detail + user info |
| 7.4 | GET | `/api/users/{userId}/licenses/{licenseId}` | ADMIN | 1) Admin-only 2) Full license detail |

### 8.x Inquiry/Question (7 APIs) -- cr-C

| # | Method | URL | Auth | Key Verification Points |
|---|--------|-----|------|------------------------|
| 8.1 | POST | `/api/questions` | Auth | 1) multipart/form-data 2) category ENUM validation 3) isPublic boolean 4) status=OPEN default 5) question_attachments records if files present 6) File storage |
| 8.2 | POST | `/api/questions/{id}/answers` | Auth (owner+admin) | 1) Permission: question owner OR admin 2) **Auto status transition**: if admin's FIRST answer AND status=OPEN, change to IN_PROGRESS 3) questions.updated_at update |
| 8.3 | GET | `/api/questions` | Auth | 1) **Visibility matrix**: USER sees (is_public=1) OR (is_public=0 AND user_id=self). ADMIN sees all. 2) Filters: category, status, mine flag 3) Pagination |
| 8.4 | GET | `/api/questions/{id}` | Auth | 1) Private inquiry: owner + admin only (403 otherwise) 2) Includes answers list + attachments list |
| 8.5 | GET | `/api/questions/{id}/attachments/{attachId}` | Auth | 1) Same access control as 8.4 (private: owner + admin) 2) File download |
| 8.6 | PUT | `/api/questions/{id}/status` | ADMIN | 1) Admin-only 2) Status flow validation: OPEN->IN_PROGRESS->RESOLVED->CLOSED or OPEN->CLOSED |
| 8.7 | DELETE | `/api/questions/{id}` | Auth (owner if OPEN) or ADMIN | 1) **Dual permission**: owner can delete only if status=OPEN; admin can delete any 2) Cascade: delete answers, question_attachments, then question 3) 204 No Content |

### 9.x Notice (5 APIs) -- cr-C

| # | Method | URL | Auth | Key Verification Points |
|---|--------|-----|------|------------------------|
| 9.1 | POST | `/api/notices` | ADMIN | 1) title, content required 2) isPinned boolean 3) user_id = admin's ID |
| 9.2 | GET | `/api/notices` | PUBLIC | 1) Pinned notices first (is_pinned=1 sorted to top) 2) Then by createdAt DESC 3) Pagination |
| 9.3 | GET | `/api/notices/{noticeId}` | PUBLIC | 1) Standard detail retrieval |
| 9.4 | PUT | `/api/notices/{noticeId}` | ADMIN | 1) Partial update (title, content, isPinned) |
| 9.5 | DELETE | `/api/notices/{noticeId}` | ADMIN | 1) Physical delete 2) 204 No Content |

### 10.x Likes (3 APIs) -- cr-B

| # | Method | URL | Auth | Key Verification Points |
|---|--------|-----|------|------------------------|
| 10.1 | POST | `/api/likes/{trackId}` | Auth | 1) Track exists + is_active=1 check 2) Composite PK duplicate: use explicit existsById(), NOT rely on save() exception 3) 409 if already liked 4) 201 Created |
| 10.2 | GET | `/api/likes` | Auth | 1) Current user's likes 2) Returns flat array with track info 3) @EntityGraph on track (N+1 check) |
| 10.3 | DELETE | `/api/likes/{trackId}` | Auth | 1) Verify (user_id, track_id) exists 2) 404 if not in likes 3) 204 No Content |

### 11.x Download Queue (3 APIs) -- cr-B

| # | Method | URL | Auth | Key Verification Points |
|---|--------|-----|------|------------------------|
| 11.1 | POST | `/api/download-queue/{trackId}` | Auth | 1) Track exists + is_active=1 check 2) Composite PK duplicate: explicit existsById() 3) 409 if already in queue 4) 201 Created |
| 11.2 | GET | `/api/download-queue` | Auth | 1) Current user's queue 2) Flat array with track info 3) @EntityGraph on track |
| 11.3 | DELETE | `/api/download-queue/{trackId}` | Auth | 1) Verify existence 2) 404 if not in queue 3) 204 No Content |

### 12.x Whitelist Channels (4 APIs) -- cr-B

| # | Method | URL | Auth | Key Verification Points |
|---|--------|-----|------|------------------------|
| 12.1 | POST | `/api/whitelist-channels` | Subscriber | 1) Active subscription check 2) channelUrl must contain "youtube.com" (loose check) 3) Channel count < max_whitelist_channels from subscription plan 4) Error codes: WHITELIST_CHANNEL_LIMIT_EXCEEDED, INVALID_ARGUMENT |
| 12.2 | GET | `/api/whitelist-channels` | Auth | 1) Current user's channels 2) Flat array |
| 12.3 | PUT | `/api/whitelist-channels/{channelId}` | Auth+Owner | 1) Owner-only (403 for others) 2) channelUrl youtube.com validation |
| 12.4 | DELETE | `/api/whitelist-channels/{channelId}` | Auth+Owner | 1) Owner-only 2) Physical delete 3) 204 No Content |

### 13.x Company Certification (5 APIs) -- cr-C

| # | Method | URL | Auth | Key Verification Points |
|---|--------|-----|------|------------------------|
| 13.1 | POST | `/api/company-certifications` | Auth (BUSINESS) | 1) userType=BUSINESS check (403 otherwise) 2) No existing PENDING or APPROVED application (409) 3) multipart/form-data: documents required 4) File storage to /uploads/company-docs/{userId}/ 5) status=PENDING |
| 13.2 | GET | `/api/company-certifications/me` | Auth (BUSINESS) | 1) Current user's latest application 2) Returns null if none 3) Includes adminNote, certificationCode |
| 13.3 | GET | `/api/company-certifications` | ADMIN | 1) Pagination 2) Optional status filter (PENDING/APPROVED/REVISION_REQUESTED/REJECTED) |
| 13.4 | GET | `/api/company-certifications/{id}` | ADMIN | 1) Full application detail with applicant info |
| 13.5 | PUT | `/api/company-certifications/{id}` | ADMIN | 1) Status transition: APPROVED -> auto-generate certification_code (UUID) + record approved_at 2) REVISION_REQUESTED/REJECTED -> save adminNote 3) Verify status transition validity |

### 14.x Utility (7 APIs) -- cr-C

| # | Method | URL | Auth | Key Verification Points |
|---|--------|-----|------|------------------------|
| 14.1 | POST | `/api/auth/refresh` | PUBLIC (RefreshToken) | 1) Validate RefreshToken signature + expiry 2) Issue new AccessToken + new RefreshToken (rotation) 3) 401 if invalid/expired |
| 14.2 | GET | `/api/utils/check-email` | PUBLIC | 1) Query param: email 2) Returns { available: true/false } 3) Check against users table |
| 14.3 | GET | `/api/utils/check-phone` | PUBLIC | 1) Query param: phone 2) Returns { available: true/false } |
| 14.4 | GET | `/api/utils/subscription-status` | Auth | 1) Join user_subscriptions + subscriptions 2) hasSubscription boolean 3) Plan details if subscribed |
| 14.5 | GET | `/api/utils/download-count` | Auth | 1) COUNT query on track_downloads WHERE DATE(downloaded_at)=CURDATE() 2) dailyLimit from subscription plan 3) remaining = dailyLimit - todayDownloads |
| 14.6 | GET | `/api/utils/user-type` | Auth | 1) Returns userType + job from JWT userId |
| 14.7 | GET | `/api/utils/check-nickname` | PUBLIC | 1) Query param: nickname 2) Returns { available: true/false } |

---

## 2. DB Verification Checklist (21 Tables)

| # | Table | PK | Key FK(s) | Enum/Special | Verification Points |
|---|-------|----|-----------|-------------|-------------------|
| 1 | `users` | id (BIGINT AI) | -- | role: USER/ADMIN; job: EDITOR/ARTIST/FREELANCER (nullable); user_type: INDIVIDUAL/BUSINESS; is_deleted: soft delete | 1) password nullable (social login) 2) phone_personal nullable (social) 3) job nullable (social) 4) nickname UNIQUE 5) email UNIQUE 6) DEFAULT user_type='INDIVIDUAL' |
| 2 | `social_accounts` | id (BIGINT AI) | user_id->users | provider: GOOGLE/KAKAO/NAVER | 1) UNIQUE (provider, provider_id) 2) One user can have multiple social accounts |
| 3 | `subscriptions` | id (BIGINT AI) | -- | user_type: INDIVIDUAL/BUSINESS; is_active flag | 1) UNIQUE (name, user_type) 2) download_per_day: -1 = unlimited 3) Seed data: 5 plans |
| 4 | `user_subscriptions` | id (BIGINT AI) | user_id->users (UNIQUE), subscription_id->subscriptions | billing_cycle: MONTHLY/YEARLY; status: ACTIVE/CANCELLED/EXPIRED | 1) **user_id UNIQUE** = 1 subscription per user 2) started_at/expires_at = DATE type |
| 5 | `company_certifications` | id (BIGINT AI) | user_id->users | status: PENDING/APPROVED/REVISION_REQUESTED/REJECTED | 1) certification_code UNIQUE (nullable, set on APPROVED) 2) approved_at nullable 3) document_path stores directory path |
| 6 | `tracks` | id (BIGINT AI) | user_id->users | is_active: 0=unpublished, 1=published | 1) preview_file nullable (fallback logic) 2) play_count BIGINT default 0 3) is_active default 0 |
| 7 | `tags` | id (BIGINT AI) | -- | type: MOOD/GENRE/INSTRUMENT | 1) name UNIQUE |
| 8 | `track_tags` | (track_id, tag_id) composite | track_id->tracks, tag_id->tags | -- | 1) Composite PK 2) Application-level cascade delete on tag deletion |
| 9 | `playlists` | id (BIGINT AI) | user_id->users | is_active flag | 1) title max 50 2) is_active default 1 |
| 10 | `playlist_tracks` | (playlist_id, track_id) composite | playlist_id->playlists, track_id->tracks | track_order INT | 1) Composite PK 2) track_order management on add/remove/reorder |
| 11 | `track_downloads` | id (BIGINT AI) | user_id->users, track_id->tracks | -- | 1) Same track can be downloaded multiple times 2) INDEX (user_id, downloaded_at) for daily count query 3) downloaded_at DATETIME |
| 12 | `play_histories` | id (BIGINT AI) | user_id->users, track_id->tracks | -- | 1) Each play recorded (not deduplicated) 2) played_at DATETIME |
| 13 | `subscription_payments` | id (BIGINT AI) | user_id->users, user_subscription_id->user_subscriptions, subscription_id->subscriptions | billing_cycle: MONTHLY/YEARLY; payment_status: READY/DONE/REFUND | 1) user_subscription_id FK (v4 addition) 2) pg_transaction_id nullable (PG integration) 3) amount DECIMAL(10,2) |
| 14 | `likes` | (user_id, track_id) composite | user_id->users, track_id->tracks | -- | 1) Composite PK 2) No updated_at (only created_at) 3) @EmbeddedId merge() behavior |
| 15 | `download_queue` | (user_id, track_id) composite | user_id->users, track_id->tracks | -- | 1) Composite PK 2) No updated_at (only created_at) 3) @EmbeddedId merge() behavior |
| 16 | `whitelist_channels` | id (BIGINT AI) | user_id->users | -- | 1) channel_url VARCHAR(255) -- youtube.com validation at app level 2) Physical delete (no is_active) |
| 17 | `questions` | id (BIGINT AI) | user_id->users | category: DOWNLOAD/PAYMENT/COPYRIGHT/PRODUCTION/OTHER; status: OPEN/IN_PROGRESS/RESOLVED/CLOSED | 1) is_public TINYINT(1) default 0 2) Status flow enforcement at app level |
| 18 | `answers` | id (BIGINT AI) | question_id->questions, user_id->users | -- | 1) 1:N with questions 2) User role (USER/ADMIN) determines display |
| 19 | `licenses` | id (BIGINT AI) | user_id->users, track_id->tracks | -- | 1) license_code UNIQUE (UUID) 2) UNIQUE (user_id, track_id) -- one license per user per track 3) No separate issued_at column (use created_at, map to issuedAt in API) |
| 20 | `notices` | id (BIGINT AI) | user_id->users | -- | 1) is_pinned TINYINT(1) default 0 2) user_id = admin who created |
| 21 | `question_attachments` | id (BIGINT AI) | question_id->questions | -- | 1) No updated_at (only created_at) 2) file_size BIGINT (bytes) 3) file_path = server storage path |

---

## 3. Coding Standards Rules Checklist

These rules must be verified across ALL Service/Controller/Entity/DTO classes during Phase 2 code review.

| # | Rule | Where to Check | How to Verify |
|---|------|---------------|--------------|
| 1 | `@Transactional(readOnly = true)` at class level on all Services | All `*Service.java` | Grep for `@Transactional` on class; verify `readOnly = true` |
| 2 | Mutating methods override with `@Transactional` (no readOnly) | Service methods that do write operations | Grep for `@Transactional` without `readOnly` on individual methods |
| 3 | Controller is thin -- no business logic | All `*Controller.java` | No repository calls, no conditional logic beyond delegation to service |
| 4 | DTO/Entity separation -- Entity never returned from Controller | All controllers | Return type is always `ResponseEntity<ResponseDTO<*Response>>` or similar DTO |
| 5 | `@NoArgsConstructor(access = AccessLevel.PROTECTED)` on all Entities | All `*Entity/*.java` in entity/ | Verify Lombok annotation present |
| 6 | `@Getter` on entities, NO `@Setter` | All entities | Grep for `@Setter` -- should not exist on entities |
| 7 | `BaseEntity` inheritance for created_at/updated_at | All entities except mapping tables (track_tags, playlist_tracks, likes, download_queue) | Verify `extends BaseEntity` |
| 8 | `@ManyToOne(fetch = FetchType.LAZY)` default | All `@ManyToOne` relationships | Grep for `@ManyToOne` and verify `fetch = FetchType.LAZY` |
| 9 | `@EntityGraph` or JOIN FETCH for list/page queries with LAZY associations | Repository methods returning List/Page with joined entities | Check repository interfaces for EntityGraph annotations |
| 10 | N+1 prevention on collection queries | Service methods doing list operations | Verify no lazy loading in loops |
| 11 | `GlobalExceptionHandler` handles all exceptions | Single `@RestControllerAdvice` class | Verify `BusinessException` (4xx) and catch-all `Exception` handler exist |
| 12 | `AccessDeniedException` NOT caught by catch-all | GlobalExceptionHandler | Verify explicit handler for `AccessDeniedException` before `Exception.class` handler |
| 13 | Error response format: `{ status, error, errorCode, message }` | GlobalExceptionHandler + ExceptionResponseDTO | Verify `errorCode` field exists for domain errors |
| 14 | Pagination response: `{ dataList, pageInfo: { page, size, total, start, end, prev, next } }` | All paginated endpoints | Verify block pagination fields, NOT Spring's default `totalElements/totalPages` |
| 15 | Success response: `{ message, data }` -- NO `status` field in body | All success responses | Verify `status` not included in response body |
| 16 | Composite PK entities use explicit `existsById()` for duplicate detection | Like, DownloadQueue services | Verify NOT relying on `save()` throwing exception |
| 17 | `GenerationType.IDENTITY` for all PKs | All `@GeneratedValue` annotations | Grep for `GenerationType` |
| 18 | Java 17 features: `record` for DTOs | DTO classes | Check if `record` is used (preferred but not mandatory) |
| 19 | Secrets not hardcoded | application.yml | JWT secret, DB password use `${ENV_VAR}` |
| 20 | `@RequiredArgsConstructor` for constructor injection | All Service and Controller classes | Verify no `@Autowired` field injection |
| 21 | Spring Boot 4.x correct import packages | `@AutoConfigureMockMvc`, `@DataJpaTest` | Verify 4.x packages, not 3.x |
| 22 | `addSpec()` helper for Specification.where() | Track list filtering | Verify NOT using `Specification.where(null)` (Spring Data 4.x bug) |

---

## 4. Domain Business Rules

### 4.1 Track Domain (1.x)

- **RULE-TRACK-001**: Tracks are created with `is_active=0`. Admin must explicitly activate.
- **RULE-TRACK-002**: Streaming serves `preview_file` first; falls back to `audio_file` if NULL.
- **RULE-TRACK-003**: Soft delete sets `is_active=0` AND physically deletes `track_tags` records.
- **RULE-TRACK-004**: Track list (1.2) returns ONLY `is_active=1` tracks.
- **RULE-TRACK-005**: Track detail (1.3) returns 404 for `is_active=0` tracks.

### 4.2 Tag Domain (2.x)

- **RULE-TAG-001**: Tag name is UNIQUE. Duplicate returns 409.
- **RULE-TAG-002**: Tag deletion must first delete all `track_tags` referencing this tag (application-level, not DB cascade).

### 4.3 Playlist Domain (3.x)

- **RULE-PL-001**: ALL playlist operations require ACTIVE subscription.
- **RULE-PL-002**: Playlists are owner-only (403 for other users).
- **RULE-PL-003**: Adding duplicate track to playlist returns 409.
- **RULE-PL-004**: trackOrder auto-assigned on add (max + 1).
- **RULE-PL-005**: Playlist delete cascades to playlist_tracks.

### 4.4 Play History Domain (4.x)

- **RULE-PH-001**: Play history recording is separate from streaming (frontend calls 4.1 explicitly).
- **RULE-PH-002**: `tracks.play_count` must be incremented atomically with play history creation.
- **RULE-PH-003**: Delete with empty historyIds `[]` = delete ALL for the user.

### 4.5 User Domain (5.x)

- **RULE-USER-001**: Password is BCrypt hashed. NULL for social-only accounts.
- **RULE-USER-002**: `isProfileComplete` = `phone_personal IS NOT NULL AND job IS NOT NULL` (derived, not stored).
- **RULE-USER-003**: `userType` can only be set during registration (5.1) or social profile completion (5.10). Immutable after.
- **RULE-USER-004**: Withdrawal = soft delete (`is_deleted=1`). Requires password re-verification.
- **RULE-USER-005**: Withdrawn accounts (`is_deleted=1`) return 403 on login attempt.
- **RULE-USER-006**: Admin can only update `role` and `isVerified` for other users (5.8).

### 4.6 Subscription Domain (6.x) -- UNIMPLEMENTED

- **RULE-SUB-001**: One subscription per user (`user_id` UNIQUE on `user_subscriptions`).
- **RULE-SUB-002**: BUSINESS members must have `company_certifications.status=APPROVED` before subscribing.
- **RULE-SUB-003**: Subscription change is immediate with prorated payment.
- **RULE-SUB-004**: Self-cancel sets `status=CANCELLED` (not physical delete).
- **RULE-SUB-005**: Subscription plan list returns only `is_active=1` plans.
- **RULE-SUB-006**: `download_per_day = -1` means unlimited downloads.
- **RULE-SUB-007**: Billing cycle: MONTHLY or YEARLY. Prices differ.

### 4.7 License Domain (7.x)

- **RULE-LIC-001**: Licenses are auto-issued on download (1.5). No manual issuance API.
- **RULE-LIC-002**: One license per (user_id, track_id) -- UNIQUE constraint. Re-download does not create duplicate.
- **RULE-LIC-003**: `issuedAt` in API response maps from `created_at` column (no separate issued_at column).
- **RULE-LIC-004**: Licenses remain viewable even after subscription expires.

### 4.8 Inquiry Domain (8.x)

- **RULE-INQ-001**: Private inquiries (`is_public=0`) visible only to owner + admin.
- **RULE-INQ-002**: Status flow: OPEN -> IN_PROGRESS (auto on admin's first answer) -> RESOLVED -> CLOSED. Also: OPEN -> CLOSED (direct).
- **RULE-INQ-003**: User can delete own inquiry ONLY if status=OPEN. Admin can delete any.
- **RULE-INQ-004**: Inquiry editing is NOT supported. Delete and rewrite.
- **RULE-INQ-005**: Answer writers: inquiry owner OR admin.
- **RULE-INQ-006**: Delete cascades: answers + question_attachments + question.

### 4.9 Notice Domain (9.x)

- **RULE-NOTICE-001**: Pinned notices (`is_pinned=1`) appear first in list, then by createdAt DESC.
- **RULE-NOTICE-002**: Physical delete (not soft delete).

### 4.10 Likes Domain (10.x)

- **RULE-LIKE-001**: Composite PK (user_id, track_id). Use explicit existsById() for duplicate check.
- **RULE-LIKE-002**: Track must exist and be active (`is_active=1`).
- **RULE-LIKE-003**: Already-liked returns 409.

### 4.11 Download Queue Domain (11.x)

- **RULE-DLQ-001**: Same rules as Likes for composite PK handling.
- **RULE-DLQ-002**: Track must exist and be active.
- **RULE-DLQ-003**: Already-queued returns 409.

### 4.12 Whitelist Channel Domain (12.x)

- **RULE-WL-001**: channelUrl must contain "youtube.com" (loose validation).
- **RULE-WL-002**: Max channels limited by `subscriptions.max_whitelist_channels`.
- **RULE-WL-003**: Requires active subscription for registration.
- **RULE-WL-004**: Physical delete (no soft delete, column was removed in v4).
- **RULE-WL-005**: Owner-only for update/delete.

### 4.13 Company Certification Domain (13.x)

- **RULE-CC-001**: Only `userType=BUSINESS` members can apply.
- **RULE-CC-002**: Cannot apply if existing PENDING or APPROVED application exists (409).
- **RULE-CC-003**: Can reapply after REJECTED or REVISION_REQUESTED.
- **RULE-CC-004**: On APPROVED: auto-generate UUID `certification_code` + record `approved_at`.
- **RULE-CC-005**: certification_code is a prerequisite for BUSINESS subscription (checked in 6.3).

### 4.14 Utility Domain (14.x)

- **RULE-UTIL-001**: Token refresh rotates both access and refresh tokens.
- **RULE-UTIL-002**: Duplicate checks (email, phone, nickname) return `{ available: true/false }`.
- **RULE-UTIL-003**: Download count uses COUNT query: `WHERE user_id=? AND DATE(downloaded_at)=CURDATE()`.
- **RULE-UTIL-004**: Subscription status returns `hasSubscription: false` if no active subscription.

---

## 5. Phase 2 cr-A/B/C Domain Split

### cr-A: Sound Core (26 APIs)

| Domain | APIs | Count |
|--------|------|-------|
| Track (1.x) | 1.1-1.7 | 7 |
| Tag (2.x) | 2.1-2.4 | 4 |
| Playlist (3.x) | 3.1-3.8 | 8 |
| Play History (4.x) | 4.1-4.3 | 3 |
| License (7.x) | 7.1-7.4 | 4 |
| **Total** | | **26** |

**cr-A Focus Areas:**
- File I/O operations (track upload, streaming, download)
- N+1 prevention on track list queries with tags
- Subscription gate enforcement on all playlist endpoints
- play_count atomic increment
- Tag cascade deletion
- License dedup logic in download flow
- preview_file fallback logic

### cr-B: Subscription & Collections (20 APIs)

| Domain | APIs | Count |
|--------|------|-------|
| Subscription (6.x) | 6.1-6.10 | 10 |
| Whitelist (12.x) | 12.1-12.4 | 4 |
| Likes (10.x) | 10.1-10.3 | 3 |
| Download Queue (11.x) | 11.1-11.3 | 3 |
| **Total** | | **20** |

**cr-B Focus Areas:**
- **Subscription is entirely unimplemented** -- cr-B will review against spec only once implemented
- 1-user-1-subscription UNIQUE constraint
- BUSINESS certification gate in subscribe flow
- Proration calculation logic
- Composite PK merge() behavior for Likes/DownloadQueue
- Whitelist channel count limit enforcement
- PG payment integration patterns (subscription_payments)

### cr-C: User, Auth, Admin, Communication (33 APIs)

| Domain | APIs | Count |
|--------|------|-------|
| User Info (5.x) | 5.1, 5.4-5.9, 5.10 | 8 |
| Auth (5.2, 5.3, 14.1) | 5.2, 5.3, 14.1 | 3 |
| Inquiry (8.x) | 8.1-8.7 | 7 |
| Notice (9.x) | 9.1-9.5 | 5 |
| Company Cert (13.x) | 13.1-13.5 | 5 |
| Util (14.x) | 14.2-14.7 | 6 |
| **Total** | | **34** |

> Note: Actual total is 34, not 33. Auth APIs (5.2, 5.3) are counted under User Info in the API spec but logically belong with Auth for security review coherence.

**cr-C Focus Areas:**
- JWT token lifecycle (issuance, refresh, rotation)
- Social login OAuth flow + two-step registration
- isProfileComplete derived field consistency
- Inquiry visibility matrix (public/private x owner/admin)
- Inquiry status state machine
- Company certification status transitions
- Password verification on withdrawal
- Admin-only endpoint authorization

---

## 6. Referenced Documents

| Document | Path | Version |
|----------|------|---------|
| API Spec | `docs/design/api-spec.md` | v5 |
| DB Schema | `docs/design/db-schema.md` | v4 |
| Use Case Index | `docs/design/usecase/index.md` | v4 |
| Core Principles | `docs/standards/core-principles.md` | 2.0 |
| Development Standards | `docs/standards/development-standards.md` | 2.0 |
| 16 Use Case files | `docs/design/usecase/*.md` | v4 (26-02-20) |
