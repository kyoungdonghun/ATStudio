---
version: 1.0
last_updated: 2026-02-28
project: ATS
owner: docops
category: architecture
status: stable
dependencies:
  - path: ../standards/core-principles.md
    reason: Constitutional baseline
  - path: ../standards/development-standards.md
    reason: Coding standards checklist source
  - path: ../design/api-spec.md
    reason: API contract reference (v5, 79 APIs — audit baseline; current spec is v6, 89 APIs)
  - path: ../design/db-schema.md
    reason: DB schema reference (v4, 21 tables — audit baseline; current schema has 23 tables)
tier: 3
target_agents:
  - sa
  - se
  - pg
  - cr
task_types:
  - review
  - implementation
  - security
---

# ATStudio Backend Audit Report

> Purpose: Consolidated findings from the four-phase backend audit (WI-028 through WI-032) covering 79 APIs, 21 tables, 22 coding-standard rules, and 14 domain business-rule groups. This document is the direct input for the remediation REQ.

---

## Executive Summary

| Metric | Value |
|--------|-------|
| Audit scope (APIs) | 79 |
| Audit scope (DB tables) | 21 |
| Coding standard rules checked | 22 |
| Domain business-rule groups | 14 |
| Total issues found | 35 |
| CRITICAL | 5 |
| MAJOR | 15 |
| MINOR | 10 |
| SUGGESTION | 5 |
| APIs with no issues (CLEAN) | 52 / 79 (66%) |

### Issue Distribution by Phase

| Phase / Reviewer | WI | Issues Found |
|------------------|----|-------------|
| cr-A (Track, Tag, Playlist, PlayHistory, License) | WI-029 | 13 (CR-A-001 to CR-A-013) |
| cr-B (Subscription, Whitelist, DownloadQueue, Likes) | WI-030 | 7 (CR-B-001 to CR-B-007) |
| cr-C (User, Auth, Inquiry, Notice, CompanyCert, Util) | WI-031 | 16 (CR-C-001 to CR-C-016) |
| pg (SecurityConfig, JWT, ResponseDTO) | WI-032 | 9 (CR-P-001 to CR-P-009) |

**Note:** CR-C-008/009 and CR-B-001/002 and CR-A-007 are also independently confirmed in CR-P-003/004/007/006 respectively. The pg phase did not add new issue numbers for these overlapping findings; the original CR-A/B/C numbers remain the single source of truth for those items.

---

## CRITICAL Issues

These issues cause functional failures, complete access breakage, or full security compromise. They must be resolved before any production deployment.

---

### CR-P-001 — SecurityConfig wildcard blocks /api/users/me for normal users

- **Source:** WI-032 (pg)
- **File:** `SecurityConfig.java:71-73`
- **Domain:** User / Auth
- **Root cause:** The rule `.requestMatchers(HttpMethod.GET, "/api/users/*").hasRole("ADMIN")` (line 72) and `.requestMatchers(HttpMethod.PUT, "/api/users/*").hasRole("ADMIN")` (line 73) match `/api/users/me` before any `authenticated()` rule. Spring Security 6 applies the first matching rule.
- **Impact:** Every authenticated non-admin user receives HTTP 403 on `GET /api/users/me` (5.4) and `PUT /api/users/me` (5.7). Both user-profile endpoints are completely inaccessible to the people they are designed for.
- **Comparison:** `/api/user-subscriptions/me` and `/api/company-certifications/me` have explicit `authenticated()` rules before their wildcard counterparts and are not affected.
- **Required fix:** Add explicit `authenticated()` rules for `/api/users/me` (GET and PUT) before the `hasRole("ADMIN")` wildcard entries.

```
SecurityConfig.java — insert before line 72:
  .requestMatchers(HttpMethod.GET,  "/api/users/me").authenticated()
  .requestMatchers(HttpMethod.PUT,  "/api/users/me").authenticated()
```

---

### CR-P-004 — JWT secret has predictable Base64 fallback hardcoded in application.yml

- **Source:** WI-032 (pg), confirmed from WI-031 (cr-C) as CR-C-009 .yml:36`
- **Domain:** Auth / Security
- **Root cause:** `secret: ${JWT_SECRET:YXRzdHVkaW8tc2Vj...}` — the fallback decodes to `atstudio-secret-key-for-development-only-2026`. This value is stored in the repository and is publicly readable.
- **Impact:** If `JWT_SECRET` is not set in the deployment environment (or the repository is public), an attacker can forge arbitrary JWT tokens including `ADMIN`-role tokens, achieving full system privilege escalation.
- **Policy violated:** `docs/policies/security-policy.md` section 6.1: "Never hardcode in application.yml. Use `${JWT_SECRET}` placeholder."
- **Required fix:** Remove the fallback entirely. The application must refuse to start if `JWT_SECRET` is absent.

```yaml
# Before (VULNERABLE):
secret: ${JWT_SECRET:YXRzdHVkaW8tc2Vj...}

# After (CORRECT):
secret: ${JWT_SECRET}
```

---

### CR-C-001 — Inquiry delete does not cascade to child records

- **Source:** WI-031 (cr-C)
- **File:** `QuestionService.java:182-188`
- **Domain:** Inquiry (8.x)
- **Root cause:** `8.7 DELETE /api/questions/{id}` deletes the `questions` row directly. Child records in `answers` and `question_attachments` are not deleted beforehand. No `ON DELETE CASCADE` is defined at the DB level.
- **Impact:** Every inquiry deletion throws `DataIntegrityViolationException` (FK violation) at runtime. The endpoint returns a 500 error for any inquiry that has answers or attachments.
- **Business rule violated:** RULE-INQ-006: "Delete cascades: answers + question_attachments + question."
- **Required fix:** Before deleting the question, call `answerRepository.deleteAllByQuestion(question)` and `attachmentRepository.deleteAllByQuestion(question)`.

---

### CR-C-002 — AuthService and OAuth2Service class-level @Transactional missing readOnly=true

- **Source:** WI-031 (cr-C)
- **Files:** `AuthService.java:24`, `OAuth2Service.java:22`
- **Domain:** Auth
- **Root cause:** Both services use `@Transactional` at class level without `readOnly = true`. This means every method, including read-only lookups, opens a full read-write transaction.
- **Impact:** (1) Performance degradation — unnecessary write-lock overhead on read operations such as token validation. (2) Coding standard violation (Rule 1): all service classes must have `@Transactional(readOnly = true)` at class level with mutating methods overriding it.
- **Required fix:** Change to `@Transactional(readOnly = true)` on both classes; add `@Transactional` (without `readOnly`) only on mutating methods (login, refresh, register).

---

### CR-A-001 — Track tag filter causes runtime crash (unmapped join path)

- **Source:** WI-029 (cr-A) — classified CRITICAL by cr-A reviewer
- **File:** `TrackSpecification.java:40`
- **Domain:** Track (1.2)
- **Root cause:** `root.join("trackTags")` references a relationship that does not exist on the `Track` entity. The `Track` entity has no `@OneToMany(mappedBy = "track") List<TrackTag> trackTags` field.
- **Impact:** Any call to `GET /api/tracks` with a tag filter (genre/mood/instrument) throws `IllegalArgumentException: Unable to locate Attribute [trackTags] on ManagedType [Track]`. Tag-filtered track search — the core music discovery feature — is completely broken. Affects 100% of tag-filter usage.
- **Why tests missed it:** `TrackServiceTest` uses Mockito `any(Specification.class)` — the Specification lambda is never executed. Only a `@DataJpaTest` integration test against a real JPA context would catch this.
- **Required fix:** Add `@OneToMany(mappedBy = "track", fetch = FetchType.LAZY) private List<TrackTag> trackTags = new ArrayList<>();` to `Track.java`.

---

## MAJOR Issues

These issues cause incorrect business behaviour, data exposure, or API contract violations that must be resolved before the frontend integration phase.

---

### CR-A-002 — DownloadService missing class-level @Transactional(readOnly=true)

- **Source:** WI-029 (cr-A)
- **File:** `DownloadService.java:19-21`
- **Domain:** Track download (1.5)
- **Coding standard violated:** Rule 1 (class-level readOnly = true on all Services).
- **Required fix:** Add `@Transactional(readOnly = true)` annotation at class level.

---

### CR-A-003 — Unlimited download plan (downloadPerDay = -1) is blocked by download limit check

- **Source:** WI-029 (cr-A)
- **File:** `DownloadService.java:46`
- **Domain:** Track download (1.5)
- **Root cause:** The limit check is `if (todayCount >= downloadPerDay)` with no guard for `-1`. When a user is on an unlimited plan, `downloadPerDay = -1`, and `todayCount (0) >= -1` is always `true`, so every download attempt is rejected with `DOWNLOAD_LIMIT_EXCEEDED`.
- **Impact:** All unlimited-plan subscribers cannot download any track.
- **Business rule violated:** RULE-SUB-006: "`download_per_day = -1` means unlimited downloads."
- **Required fix:** `if (limit != -1 && todayCount >= limit)` guard before the exception throw.

---

### CR-A-004 — Soft-delete track does not physically delete track_tags records

- **Source:** WI-029 (cr-A)
- **File:** `TrackService.java:163-167`
- **Domain:** Track (1.7)
- **Root cause:** `DELETE /api/tracks/{trackId}` sets `is_active = 0` but does not call any delete on `track_tags`.
- **Impact:** Orphaned `track_tags` rows accumulate. If the same track ID is reactivated and tags are reassigned, duplicate FK rows may cause constraint violations. Violates RULE-TRACK-003.
- **Required fix:** Call `trackTagRepository.deleteAllByTrack(track)` before or alongside the soft-delete operation.

---

### CR-A-005 — License list queries missing @EntityGraph, causing N+1 on track association

- **Source:** WI-029 (cr-A)
- **Files:** `LicenseRepository.java:18`, `LicenseRepository.java:20`
- **Domain:** License (7.1, 7.2)
- **Root cause:** The `Page<License>` query methods for `GET /api/licenses/me` and `GET /api/users/{userId}/licenses` have no `@EntityGraph` annotation. `track` is a `@ManyToOne(fetch = LAZY)` association.
- **Impact:** For a page of N licenses, N additional SELECT queries are issued to load each track. Under any real load this becomes a measurable performance problem.
- **Required fix:** Add `@EntityGraph(attributePaths = "track")` to both repository methods at `LicenseRepository.java:18` and `:20`.

---

### CR-A-006 — Playlist list computes trackCount with a loop, causing N+1

- **Source:** WI-029 (cr-A)
- **File:** `PlaylistService.java:66-71`
- **Domain:** Playlist (3.2)
- **Root cause:** The service iterates over the playlist list and calls a separate count query per playlist to derive `trackCount`.
- **Impact:** For a user with M playlists, M + 1 queries are executed. This is a standard N+1 anti-pattern.
- **Required fix:** Replace with a single batch count query, e.g., `playlistTrackRepository.countByPlaylistIdIn(playlistIds)` returning a Map, then join in memory.

---

### CR-B-001 — Subscription admin cancel (6.9) returns HTTP 200 instead of 204

- **Source:** WI-030 (cr-B), confirmed by WI-032 (pg) as CR-P-007
- **File:** `UserSubscriptionController.java:97-103`
- **Domain:** Subscription (6.9)
- **Required fix:** Replace `ResponseEntity.ok()` with `ResponseEntity.noContent().build()`.

---

### CR-B-002 — Subscription self-cancel (6.10) returns HTTP 200 instead of 204

- **Source:** WI-030 (cr-B), confirmed by WI-032 (pg) as CR-P-007
- **File:** `UserSubscriptionController.java:107-114`
- **Domain:** Subscription (6.10)
- **Required fix:** Replace `ResponseEntity.ok()` with `ResponseEntity.noContent().build()`.

---

### CR-B-003 — Proration calculation: .abs() converts downgrade refund to additional charge

- **Source:** WI-030 (cr-B)
- **File:** `UserSubscriptionService.java:176`
- **Domain:** Subscription plan change (6.7)
- **Root cause:** `proratedAmount.abs()` is called on the final charge amount. For an upgrade, `proratedAmount` is positive (correct). For a downgrade, `proratedAmount` is negative (refund), and `.abs()` flips it to a positive charge.
- **Impact:** When a subscriber downgrades, they are charged an additional amount instead of receiving a refund. This is a financial correctness bug.
- **Required fix:** Remove `.abs()`. Pass the raw signed amount to the payment service. If the Mock payment service requires an absolute value, use a separate variable: `BigDecimal chargeAmount = proratedAmount.abs(); // for mock only`.

---

### CR-C-003 — Admin user list (5.5) does not filter out soft-deleted accounts

- **Source:** WI-031 (cr-C)
- **File:** `UserRepository.java:21-27`
- **Domain:** User (5.5)
- **Root cause:** The JPQL query for `GET /api/users` (admin search) has no `AND u.isDeleted = false` condition.
- **Impact:** Withdrawn users (soft-deleted with `is_deleted = 1`) appear in the admin user list. This violates RULE-USER-004 (withdrawal = soft delete) and exposes PII of users who have withdrawn.
- **Required fix:** Add `AND u.isDeleted = false` to the WHERE clause in `UserRepository.java:21-27`.

---

### CR-C-004 — RESOURCE_DUPLICATE error returns HTTP 400 instead of HTTP 409

- **Source:** WI-031 (cr-C)
- **File:** `BUSINESS_ERROR.java:22-25`
- **Domain:** Company Certification (13.1), any duplicate-resource scenario
- **Root cause:** The `RESOURCE_DUPLICATE` error code is mapped to `HttpStatus.BAD_REQUEST` (400) in the error enum. The API specification and REST convention require `409 Conflict` for duplicate-resource errors.
- **Impact:** Clients (and future frontend) receive 400 for a scenario that is semantically a conflict, not a bad request. RULE-CC-002 specifies 409 for duplicate certification applications.
- **Required fix:** Change the `HttpStatus` mapping for `RESOURCE_DUPLICATE` to `HttpStatus.CONFLICT`.

---

### CR-C-005 — CompanyCertificationRepository.findByUser() is non-deterministic for multiple records

- **Source:** WI-031 (cr-C)
- **File:** `CompanyCertificationRepository.java:14`
- **Domain:** Company Certification (13.x)
- **Root cause:** A user may have multiple certification records (REJECTED then reapplied). `findByUser()` returns a `List` or an unordered single result. The service calling this method may get any row, not the latest one.
- **Impact:** `GET /api/company-certifications/me` (13.2) and the status check in 13.5 may operate on a stale record. RULE-CC-003 (reapply after rejection) is implicitly broken.
- **Required fix:** Change to `findTopByUserOrderByCreatedAtDesc(User user)` to always retrieve the most recent application.

---

### CR-C-006 — CompanyCertification.process() applies status transitions without validation

- **Source:** WI-031 (cr-C)
- **File:** `CompanyCertification.java:42-48`
- **Domain:** Company Certification (13.5)
- **Root cause:** The `process()` method accepts any target status and applies it unconditionally. There is no allowed-transition map enforcement.
- **Impact:** An admin can set a certification from `APPROVED` back to `PENDING`, or from `REJECTED` directly to `APPROVED` without reapplication — all invalid transitions.
- **Required fix:** Add a transition-validity map (e.g., `PENDING -> [APPROVED, REVISION_REQUESTED, REJECTED]`, `REVISION_REQUESTED -> [APPROVED, REJECTED]`) and throw `BusinessException` for invalid transitions.

---

### CR-C-007 — Question.updateStatus() applies status transitions without validation

- **Source:** WI-031 (cr-C)
- **File:** `Question.java:44-46`
- **Domain:** Inquiry (8.6)
- **Root cause:** The `updateStatus()` method assigns the new status directly with no allowed-transition check.
- **Impact:** An admin can set an inquiry from `CLOSED` back to `OPEN`, or skip states. RULE-INQ-002 defines a strict state machine: `OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED` (and `OPEN -> CLOSED` direct). Any deviation is a business-rule violation.
- **Required fix:** Add transition validation consistent with RULE-INQ-002 before assigning the new status.

---

### CR-C-008 — TestController exposes unauthenticated endpoints in production

- **Source:** WI-031 (cr-C), confirmed by WI-032 (pg) as CR-P-003
- **File:** `TestController.java:1-18`
- **Domain:** Common / Security
- **Root cause:** `TestController` defines `GET /test` and `GET /health` with no authentication. These endpoints fall through to the `anyRequest().authenticated()` catch-all only if that rule is in place; however, the pg audit confirms they are currently accessible without authentication.
- **Impact:** Internal system state or server metadata may leak to unauthenticated callers. Additionally, raw String responses bypass the `ResponseDTO` wrapper, inconsistent with all other controllers.
- **Required fix:** Delete `TestController.java` or annotate the class with `@Profile("dev")` so it is excluded from the production build.

---

### CR-C-009 — JWT secret default fallback (duplicate of CR-P-004)

This issue is fully described under **CR-P-004** (CRITICAL section). Source reference: WI-031 (cr-C). No separate remediation entry required.

---

## MINOR Issues

These issues represent deviations from coding standards, weak validations, or minor inconsistencies. Resolution is strongly recommended before the frontend integration phase.

---

### CR-A-007 / CR-P-006 — TagController returns raw List without ResponseDTO wrapper

- **Source:** WI-029 (cr-A), confirmed by WI-032 (pg) as CR-P-006
- **File:** `TagController.java:37-40`
- **Domain:** Tag (2.2)
- **Impact:** Response format inconsistency. All other list-returning controllers wrap their response in `ResponseDTO`. Frontend clients must handle two different shapes for list responses.
- **Required fix:** Wrap the return value with `ResponseDTO.dataList(tagList)` (or equivalent).

---

### CR-A-008 — Playlist soft-delete does not remove playlist_tracks records

- **Source:** WI-029 (cr-A)
- **File:** `PlaylistService.java:181-186`
- **Domain:** Playlist (3.8)
- **Impact:** Orphaned `playlist_tracks` rows accumulate. RULE-PL-005 specifies "Playlist delete cascades to playlist_tracks." If the business intent is to retain history, this must be explicitly documented; otherwise the orphans are a bug.
- **Required fix:** Either delete `playlist_tracks` rows before soft-deleting the playlist, or document the intentional retention with a code comment and a policy reference.

---

### CR-A-009 — TrackResponse exposes internal audio file storage path

- **Source:** WI-029 (cr-A)
- **File:** `TrackResponse.java:16`
- **Domain:** Track
- **Impact:** The `audioFile` field exposes the server-side file system path. This reveals the storage layout to any client that calls `GET /api/tracks` or `GET /api/tracks/{id}`, which is a security information-disclosure risk.
- **Required fix:** Remove the `audioFile` field from `TrackResponse` or replace it with a signed URL or a relative streaming path.

---

### CR-A-010 — Stream endpoint does not verify fallback resource exists

- **Source:** WI-029 (cr-A)
- **File:** `TrackService.java:116-131`
- **Domain:** Track (1.4)
- **Impact:** If `preview_file` is NULL and `audio_file` does not exist on disk, the service returns a `Resource` that `resource.exists()` would return false for. The caller may receive a 200 response with an empty or corrupt body.
- **Required fix:** Add `if (!resource.exists()) throw new BusinessException(...)` after the fallback lookup.

---

### CR-B-004 — Invalid userType query param in 6.1 causes unhandled 500

- **Source:** WI-030 (cr-B)
- **File:** `SubscriptionService.java:25`
- **Domain:** Subscription (6.1)
- **Root cause:** `UserType.valueOf(userTypeParam)` throws `IllegalArgumentException` for any string not in the enum. `GlobalExceptionHandler` does not have a specific handler for this exception, so it falls through to the catch-all and returns HTTP 500.
- **Required fix:** Use `Arrays.stream(UserType.values()).filter(e -> e.name().equalsIgnoreCase(param)).findFirst()` pattern, or wrap in try-catch and throw `BusinessException(INVALID_ARGUMENT)` for a clean 400 response.

---

### CR-B-005 — Whitelist channel URL validated with contains() — bypassable

- **Source:** WI-030 (cr-B)
- **File:** `WhitelistChannelService.java:98`
- **Domain:** Whitelist Channels (12.1, 12.3)
- **Root cause:** `channelUrl.contains("youtube.com")` matches strings like `evil.site/youtube.com` or `youtube.com.evil.site`.
- **Impact:** A subscriber can register a non-YouTube URL as a whitelist channel, defeating the purpose of the feature.
- **Required fix:** Parse the URL and check the host component: `URI.create(channelUrl).getHost()` must equal `youtube.com` or end with `.youtube.com`.

---

### CR-C-010 — User registration (5.1) does not check phonePersonal uniqueness

- **Source:** WI-031 (cr-C)
- **File:** `UserService.java:28-47`
- **Domain:** User (5.1)
- **Impact:** Multiple accounts can be registered with the same personal phone number. The DB schema implies phone uniqueness as a business constraint, but there is no application-level check generating a meaningful error.
- **Required fix:** Add a `findByPhonePersonal` existence check before saving and throw a `PHONE_DUPLICATED` business error.

---

### CR-C-011 — QuestionAttachment entity does not extend BaseEntity

- **Source:** WI-031 (cr-C)
- **File:** `QuestionAttachment.java:17`
- **Domain:** Inquiry
- **Impact:** The entity has no `created_at` field managed by `BaseEntity`. The DB table does have `created_at`. This inconsistency means the `created_at` column is unmanaged by JPA unless explicitly defined in the entity.
- **Required fix:** Add `extends BaseEntity` to `QuestionAttachment`. Verify `created_at` is not double-declared.

---

### CR-C-012 — CompanyCertificationService.getMyStatus() returns null for no-record case

- **Source:** WI-031 (cr-C)
- **File:** `CompanyCertificationService.java:77`
- **Domain:** Company Certification (13.2)
- **Impact:** `GET /api/company-certifications/me` returns a null body (or an empty 200) when no record exists. Other "me"-pattern endpoints return a structured empty response or 404. The inconsistency may confuse frontend integration.
- **Required fix:** Return an explicit empty DTO (`CompanyCertificationResponse` with null fields and a status indicator) or return `ResponseEntity` with 404 and document the chosen contract clearly.

---

### CR-C-013 — OAuth2 token exchange response fields are not null-checked

- **Source:** WI-031 (cr-C)
- **File:** `OAuth2Service.java:117-158`
- **Domain:** Auth (5.3)
- **Impact:** If the OAuth2 provider returns a malformed response (e.g., missing `access_token`), `response.get("access_token")` returns null and subsequent operations throw `NullPointerException`. A stack trace may be included in the error response if the catch-all handler does not sanitize it.
- **Required fix:** Add null-guard checks on `access_token` and other required fields. Throw `BusinessException(OAUTH2_EXCHANGE_FAILED)` with a sanitized message.

---

### CR-P-005 — Expired refresh tokens are accepted, making RT expiry setting meaningless

- **Source:** WI-032 (pg)
- **File:** `AuthService.java:70-73`
- **Domain:** Auth (14.1)
- **Root cause:** The condition `if (result != VALID && result != EXPIRED)` explicitly allows EXPIRED tokens through the validation gate, provided they pass BCrypt matching against the DB value.
- **Impact:** The `refresh-expiration` configuration value has no actual enforcement. A stolen refresh token is valid indefinitely as long as it remains in the DB. This is a low-probability but meaningful security weakening.
- **Required fix (option A):** Change the condition to reject EXPIRED tokens (`if (result != VALID)`). Clients must re-authenticate after RT expiry.
- **Required fix (option B):** If the "perpetual refresh" behaviour is intentional, remove the `refresh-expiration` setting to avoid misleading configuration.

---

## SUGGESTION

These are improvement proposals that do not affect correctness or security. They may be deferred to a later maintenance cycle.

---

### CR-A-011 — JSON-body-only DTO classes can be converted to Java records

- **Source:** WI-029 (cr-A)
- **File:** `TagCreateRequest.java` and similar
- **Impact:** Zero functional impact. Coding standard Rule 18 notes record-based DTOs as the preferred style. Consistency across the codebase improves readability.

---

### CR-A-012 — Playlist track reorder uses delete-all + re-insert instead of UPDATE

- **Source:** WI-029 (cr-A)
- **File:** `PlaylistService.java:149-161`
- **Impact:** Zero correctness impact. For large playlists, UPDATE-based reorder is more efficient. Defer to a later optimization cycle.

---

### CR-A-013 — Track tag assignment fetches tags individually instead of batch

- **Source:** WI-029 (cr-A)
- **File:** `TrackService.java:186-189`
- **Impact:** Zero correctness impact. Replace individual `findById` calls with `findAllById(tagIds)` for one round trip. Defer to a later optimization cycle.

---

### CR-B-006 — Payment rollback boundary on subscription creation is not explicitly documented

- **Source:** WI-030 (cr-B)
- **File:** `UserSubscriptionService.java:88-90`
- **Impact:** The current Mock implementation is covered by `@Transactional` auto-rollback. When the payment service is replaced with a real PG integration, the rollback boundary (outbound HTTP call vs. DB write) will need explicit design. Document this as a known pre-PG integration concern.

---

### CR-C-014 — AccessDeniedException is handled inside the catch-all chain, not a dedicated handler

- **Source:** WI-031 (cr-C), WI-032 (pg) as CR-P-008
- **File:** `GlobalExceptionHandler.java:116-118`
- **Impact:** The existing behavior is functionally correct (403 is returned). However, a dedicated `@ExceptionHandler(AccessDeniedException.class)` method makes the intent explicit and prevents accidental catch-all ordering issues in future refactors.

---

### CR-C-015 — Withdrawn account login returns 401 instead of 403

- **Source:** WI-031 (cr-C)
- **File:** `CustomUserDetailsService.java:21-23`
- **Impact:** RULE-USER-005 specifies withdrawn accounts return 403 on login. Currently 401 is returned. This is a semantic distinction; both prevent login. Align with the spec by throwing `DisabledException` or `LockedException` instead of `UsernameNotFoundException`, which maps to 403 after Spring Security processing.

---

### CR-B-007 — ChangeSubscriptionResponse.proratedAmount semantics not specified for real PG

- **Source:** WI-030 (cr-B)
- **File:** `UserSubscriptionService.java:150-162`
- **Impact:** Under the Mock implementation this is an informational field. Before PG integration, the API contract must clearly state whether this field is "amount to charge" (always positive), "signed delta" (negative = refund), or something else.

---

## Domain Compliance Matrix

| Domain | APIs Audited | CRITICAL | MAJOR | MINOR | SUGGESTION | Clean APIs | Overall |
|--------|-------------|----------|-------|-------|-----------|-----------|---------|
| Track (1.x) | 7 | 1 (A-001) | 2 (A-003, A-004) | 2 (A-009, A-010) | 1 (A-013) | 4 | ❌ |
| Tag (2.x) | 4 | 0 | 0 | 1 (A-007) | 1 (A-011) | 3 | ⚠️ |
| Playlist (3.x) | 8 | 0 | 1 (A-006) | 1 (A-008) | 1 (A-012) | 6 | ⚠️ |
| Play History (4.x) | 3 | 0 | 0 | 0 | 0 | 3 | ✅ |
| License (7.x) | 4 | 0 | 1 (A-005) | 0 | 0 | 2 | ⚠️ |
| Subscription (6.x) | 10 | 0 | 3 (B-001, B-002, B-003) | 1 (B-004) | 2 (B-006, B-007) | 6 | ⚠️ |
| Whitelist (12.x) | 4 | 0 | 0 | 1 (B-005) | 0 | 3 | ⚠️ |
| Download Queue (11.x) | 3 | 0 | 0 | 0 | 0 | 3 | ✅ |
| Likes (10.x) | 3 | 0 | 0 | 0 | 0 | 3 | ✅ |
| User (5.x) | 8 | 1 (P-001) | 1 (C-003) | 1 (C-010) | 0 | 5 | ❌ |
| Auth (5.2, 5.3, 14.1) | 3 | 1 (C-002) | 1 (C-009→P-004) | 2 (C-013, P-005) | 1 (C-015) | 0 | ❌ |
| Inquiry (8.x) | 7 | 1 (C-001) | 1 (C-007) | 1 (C-011) | 0 | 4 | ❌ |
| Notice (9.x) | 5 | 0 | 0 | 0 | 0 | 5 | ✅ |
| Company Cert (13.x) | 5 | 0 | 3 (C-004, C-005, C-006) | 1 (C-012) | 1 (C-016) | 1 | ⚠️ |
| Util (14.x) | 6 | 0 | 0 | 0 | 0 | 6 | ✅ |
| Cross-cut (Security/JWT) | 79 | 1 (P-004) | 2 (P-003, P-007) | 2 (P-005, P-006, P-008) | 0 | — | ❌ |

**Legend:** ✅ Clean (no issues at MAJOR or above) / ⚠️ Has MAJOR or MINOR issues / ❌ Has CRITICAL issues

---

## Recommended Next Steps

This section provides the direct input for composing the remediation REQ.

### Priority 1 — CRITICAL fixes (required before any QA or staging deployment)

Create a single REQ targeting these four items as the first remediation wave. All four are single-file or two-file fixes with no cross-domain dependency.

| Issue | Fix Effort | File(s) |
|-------|-----------|---------|
| CR-P-001 — /api/users/me security rule | Low (2 lines) | `SecurityConfig.java` |
| CR-P-004 — JWT secret fallback | Low (1 line) | `application.yml` |
| CR-C-001 — Inquiry cascade delete | Low (2 lines in service) | `QuestionService.java` |
| CR-C-002 — Auth service @Transactional | Low (annotation change) | `AuthService.java`, `OAuth2Service.java` |
| CR-A-001 — Track entity trackTags mapping | Low (1 field) | `Track.java` |

### Priority 2 — MAJOR fixes grouped by domain

These should be broken into two or three REQs to keep scope manageable.

**REQ candidate A — Track & License core (cr-A domain)**
- CR-A-001 Track entity mapping (Track.java)
- CR-A-003 Unlimited plan download guard (DownloadService.java)
- CR-A-004 Soft-delete track tag cascade (TrackService.java)
- CR-A-005 License N+1 (LicenseRepository.java)
- CR-A-006 Playlist trackCount N+1 (PlaylistService.java)
- CR-A-002 DownloadService @Transactional annotation

**REQ candidate B — Subscription & User correctness (cr-B, cr-C domain)**
- CR-B-001/002 DELETE 204 response (UserSubscriptionController.java)
- CR-B-003 Proration .abs() bug (UserSubscriptionService.java)
- CR-C-003 is_deleted filter in user list (UserRepository.java)
- CR-C-004 RESOURCE_DUPLICATE HTTP 409 (BUSINESS_ERROR.java)
- CR-C-005 findByUser non-deterministic (CompanyCertificationRepository.java)
- CR-C-006 CompanyCertification state machine (CompanyCertification.java)
- CR-C-007 Question state machine (Question.java)
- CR-C-008 TestController removal (TestController.java)

### Priority 3 — MINOR and SUGGESTION items

Group into a single maintenance REQ or address opportunistically during feature work:
- CR-A-007/CR-P-006, CR-A-008, CR-A-009, CR-A-010
- CR-B-004, CR-B-005
- CR-C-010, CR-C-011, CR-C-012, CR-C-013
- CR-P-005, CR-P-008
- All SUGGESTION items (CR-A-011 through CR-C-016)

### Prerequisite for Subscription domain completion

Before the Subscription (6.x) domain REQ is created, resolve **CR-A-003** (unlimited plan download guard) and **CR-B-003** (proration sign bug), as both directly affect the correctness of subscription-dependent flows.

---

## Related Documents

### Required References

- [API Specification v5](../design/api-spec.md): Contract baseline for all 79 API audit points
- [DB Schema v4](../design/db-schema.md): Table and constraint baseline for 21 tables
- [Core Principles](../standards/core-principles.md): STD-001 constitutional baseline

### Source Evidence Packs

- [WI-028 Evidence Pack](../../deliverables/agent/WI-20260227-ATS-028-evidence-pack.md): Phase 1 checklists (sa)
- [WI-029 Evidence Pack](../../deliverables/agent/WI-20260227-ATS-029-evidence-pack.md): cr-A findings (CR-A-001 to CR-A-013)
- [WI-030 Evidence Pack](../../deliverables/agent/WI-20260227-ATS-030-evidence-pack.md): cr-B findings (CR-B-001 to CR-B-007)
- [WI-031 Evidence Pack](../../deliverables/agent/WI-20260227-ATS-031-evidence-pack.md): cr-C findings (CR-C-001 to CR-C-016)
- [WI-032 Evidence Pack](../../deliverables/agent/WI-20260227-ATS-032-evidence-pack.md): pg findings (CR-P-001 to CR-P-009)

### Dependent Documents

- Remediation REQ (to be created): Will reference this report as primary input
- [Development Standards](../standards/development-standards.md): Coding rules violated by MAJOR issues
