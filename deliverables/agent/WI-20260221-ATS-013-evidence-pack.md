# WI-20260221-ATS-013 Evidence Pack

## Meta
- **WI:** WI-20260221-ATS-013
- **REQ:** REQ-20260221-ATS-002
- **Agent:** cr
- **Date:** 2026-02-21
- **Commit Reviewed:** f647b7f

---

## 1. Per-File Review Results

### 1.1 TagService.java
- **Path:** `src/main/java/com/atstudio/atstudio/service/TagService.java`
- **@Transactional:** Class-level `readOnly = true` — **CORRECT**
- **Mutating methods:** `createTag`, `updateTag`, `deleteTag` override with `@Transactional` — **CORRECT**
- **Exception handling:** `TAG_NAME_DUPLICATED`, `TAG_NOT_FOUND` — **CORRECT**
- **DI:** `@RequiredArgsConstructor` + `final` — **CORRECT**
- **N+1:** No lazy-loaded associations in response mapping — **NO RISK**
- **Business logic:** Duplicate name check, cascade delete TrackTag → Tag — **CORRECT**
- **Verdict:** ✅ PASS

### 1.2 UserService.java
- **Path:** `src/main/java/com/atstudio/atstudio/service/UserService.java`
- **@Transactional:** Class-level `@Transactional` WITHOUT `readOnly = true` (line 20) — **[M-04]**
- **Exception handling:** Correct use of `BUSINESS_ERROR` enum — **CORRECT**
- **Password handling:** `passwordEncoder.encode()` on register, `passwordEncoder.matches()` on withdraw — **CORRECT**
- **ResponseDTO in service:** `getUsers()` (lines 118-126) builds `ResponseDTO` — **[m-03]**
- **N+1:** User entity fields accessed directly (no lazy associations in DTO mapping) — **NO RISK**
- **Verdict:** ⚠️ CONDITIONAL PASS (fix M-04, m-03)

### 1.3 UtilService.java
- **Path:** `src/main/java/com/atstudio/atstudio/service/UtilService.java`
- **@Transactional:** Class-level `readOnly = true` — **CORRECT** (all methods are reads)
- **CustomUserDetails parameter:** All 3 public methods take `CustomUserDetails` — **[m-05]**
- **Lazy access:** `subscriptionOpt.get().getSubscription()` accesses LAZY `Subscription` — low risk (single object, not list; 1+1 queries)
- **Magic number:** `dailyLimit == -1` represents "unlimited" — consider extracting as constant
- **Verdict:** ✅ PASS (minor items noted)

### 1.4 PlayHistoryService.java
- **Path:** `src/main/java/com/atstudio/atstudio/service/PlayHistoryService.java`
- **@Transactional:** Class-level `readOnly = true` — **CORRECT**
- **Mutating methods:** `savePlayHistory`, `deleteHistory` override with `@Transactional` — **CORRECT**
- **N+1:** `findAllByUserOrderByPlayedAtDesc` returns `Page<PlayHistory>` with LAZY Track; DTO accesses Track fields — **[M-03]**
- **Size validation:** Line 51 `PageRequest.of(Math.max(0, page - 1), size)` missing `Math.max(1, size)` — **[m-07]**
- **ResponseDTO in service:** Lines 60-64 — **[m-04]**
- **CustomUserDetails parameter:** Lines 35, 47, 68 — **[m-06]**
- **Delete logic:** Empty list = delete all, non-empty = selective delete — matches API contract
- **Verdict:** ⚠️ CONDITIONAL PASS (fix M-03, m-07)

### 1.5 LikeService.java
- **Path:** `src/main/java/com/atstudio/atstudio/service/LikeService.java`
- **@Transactional:** Class-level `@Transactional` WITHOUT `readOnly = true` (line 21) — **[M-05]**
- **N+1:** `findAllByUser()` returns `List<Like>` with LAZY Track; `LikeResponse.from()` accesses 5 Track fields — **[M-01]**
- **Duplicate check:** `existsById(likeId)` before save — **CORRECT** for composite PK
- **Error code:** `DATA_INTEGRITY_VIOLATION` for duplicate — acceptable; domain-specific code like `LIKE_ALREADY_EXISTS` would be clearer
- **Verdict:** ⚠️ CONDITIONAL PASS (fix M-01, M-05)

### 1.6 DownloadQueueService.java
- **Path:** `src/main/java/com/atstudio/atstudio/service/DownloadQueueService.java`
- **@Transactional:** Class-level `@Transactional` WITHOUT `readOnly = true` (line 21) — **[M-05]**
- **N+1:** `findAllByUser()` with LAZY Track — **[M-02]**
- **Pattern:** Near-identical to LikeService — same issues
- **Verdict:** ⚠️ CONDITIONAL PASS (fix M-02, M-05)

### 1.7 NoticeService.java
- **Path:** `src/main/java/com/atstudio/atstudio/service/NoticeService.java`
- **@Transactional:** Class-level `readOnly = true` — **CORRECT**
- **Mutating methods:** `createNotice`, `updateNotice`, `deleteNotice` override — **CORRECT**
- **Size validation:** Line 49 missing `Math.max(1, size)` — **[m-08]**
- **ResponseDTO in service:** Lines 57-60 — **[m-04]**
- **N+1:** Notice fields accessed directly (no lazy associations) — **NO RISK**
- **Verdict:** ⚠️ CONDITIONAL PASS (fix m-08)

### 1.8 TagController.java
- **Path:** `src/main/java/com/atstudio/atstudio/controller/TagController.java`
- **Thin controller:** YES — **CORRECT**
- **@PreAuthorize:** `updateTag` (line 42), `deleteTag` (line 54) have `hasRole('ADMIN')`. `createTag` (line 24) MISSING — **[C-01]**
- **@Valid:** Present on request bodies — **CORRECT**
- **HTTP status:** 201 CREATED for POST, 204 NO CONTENT for DELETE — **CORRECT**
- **getAllTags return type:** `List<TagResponse>` instead of `ResponseDTO` wrapper — **[m-01]**
- **Verdict:** ❌ CONDITIONAL PASS (fix C-01 immediately)

### 1.9 UserController.java
- **Path:** `src/main/java/com/atstudio/atstudio/controller/UserController.java`
- **Thin controller:** YES — **CORRECT**
- **@PreAuthorize:** Admin endpoints (lines 69, 79, 87) correctly annotated — **CORRECT**
- **@AuthenticationPrincipal:** Correct on /me endpoints — **CORRECT**
- **HTTP status:** 201 for register, 204 for withdraw — **CORRECT**
- **Verdict:** ✅ PASS

### 1.10 UtilController.java
- **Path:** `src/main/java/com/atstudio/atstudio/controller/UtilController.java`
- **Thin controller:** YES — **CORRECT**
- **Public endpoints:** check-email/phone/nickname — matches SecurityConfig `permitAll` — **CORRECT**
- **Authenticated endpoints:** subscription-status, download-count, user-type use `@AuthenticationPrincipal` — **CORRECT**
- **Verdict:** ✅ PASS

### 1.11 PlayHistoryController.java
- **Path:** `src/main/java/com/atstudio/atstudio/controller/PlayHistoryController.java`
- **Thin controller:** YES — **CORRECT**
- **@AuthenticationPrincipal:** All 3 endpoints — **CORRECT**
- **@Valid:** Present on save and delete request bodies — **CORRECT**
- **HTTP status:** 201 for save, 204 for delete — **CORRECT**
- **Verdict:** ✅ PASS

### 1.12 LikeController.java
- **Path:** `src/main/java/com/atstudio/atstudio/controller/LikeController.java`
- **Thin controller:** YES — **CORRECT**
- **HTTP status:** 201 for addLike, 204 for removeLike — **CORRECT**
- **@AuthenticationPrincipal:** All 3 endpoints — **CORRECT**
- **Verdict:** ✅ PASS

### 1.13 DownloadQueueController.java
- **Path:** `src/main/java/com/atstudio/atstudio/controller/DownloadQueueController.java`
- **Thin controller:** YES — **CORRECT**
- **HTTP status:** 201 for add, 204 for remove — **CORRECT**
- **@AuthenticationPrincipal:** All 3 endpoints — **CORRECT**
- **Verdict:** ✅ PASS

### 1.14 NoticeController.java
- **Path:** `src/main/java/com/atstudio/atstudio/controller/NoticeController.java`
- **@PreAuthorize:** create (line 26), update (line 47), delete (line 55) have `hasRole('ADMIN')` — **CORRECT**
- **Public endpoints:** `getNotices`, `getNotice` — matches SecurityConfig `permitAll` — **CORRECT**
- **ResponseDTO wrapper missing:** Lines 27, 42, 48 return raw `NoticeResponse` — **[M-06]**
- **Verdict:** ⚠️ CONDITIONAL PASS (fix M-06)

---

## 2. Security Analysis

### 2.1 SecurityConfig Alignment

| Endpoint | SecurityConfig | Controller @PreAuthorize | Status |
|----------|---------------|------------------------|--------|
| POST /api/tags | hasRole("ADMIN") (line 77) | MISSING | ❌ GAP [C-01] |
| PUT /api/tags/* | hasRole("ADMIN") (line 78) | hasRole('ADMIN') (line 42) | ✅ OK |
| DELETE /api/tags/* | hasRole("ADMIN") (line 79) | hasRole('ADMIN') (line 54) | ✅ OK |
| GET /api/users | hasRole("ADMIN") (line 71) | hasRole('ADMIN') (line 69) | ✅ OK |
| GET /api/users/* | hasRole("ADMIN") (line 72) | hasRole('ADMIN') (line 79) | ✅ OK |
| PUT /api/users/* | hasRole("ADMIN") (line 73) | hasRole('ADMIN') (line 87) | ✅ OK |
| POST /api/notices | hasRole("ADMIN") (line 80) | hasRole('ADMIN') (line 26) | ✅ OK |
| PUT /api/notices/* | hasRole("ADMIN") (line 81) | hasRole('ADMIN') (line 47) | ✅ OK |
| DELETE /api/notices/* | hasRole("ADMIN") (line 82) | hasRole('ADMIN') (line 55) | ✅ OK |

### 2.2 User Enumeration (Suggestion S-01)
- `/api/utils/check-email`, `check-phone`, `check-nickname` are public, return boolean availability
- Standard registration UX pattern but allows enumeration of registered emails/phones
- Mitigation: Rate limiting recommended for production

### 2.3 Password Handling
- `UserService.register()`: BCrypt encoding via `passwordEncoder.encode()` — **CORRECT**
- `UserService.withdraw()`: Verification via `passwordEncoder.matches()` — **CORRECT**
- No plaintext password exposure in any DTO — **CORRECT**

---

## 3. Transaction Analysis

| Service | Class-Level | Compliant | Issues |
|---------|------------|-----------|--------|
| TagService | `@Transactional(readOnly = true)` | ✅ YES | None |
| UserService | `@Transactional` | ❌ NO | M-04 |
| UtilService | `@Transactional(readOnly = true)` | ✅ YES | None |
| PlayHistoryService | `@Transactional(readOnly = true)` | ✅ YES | None |
| LikeService | `@Transactional` | ❌ NO | M-05 |
| DownloadQueueService | `@Transactional` | ❌ NO | M-05 |
| NoticeService | `@Transactional(readOnly = true)` | ✅ YES | None |

Standard reference: `development-standards.md` Section 2A.4 — class-level must be `@Transactional(readOnly = true)`.

---

## 4. N+1 Query Risk Summary

| Service | Method | Lazy Field | Risk Level | Fix |
|---------|--------|-----------|------------|-----|
| LikeService | `getMyLikes()` | `Like.track` | **HIGH** (list) | `@EntityGraph(attributePaths = "track")` on `findAllByUser` |
| DownloadQueueService | `getMyQueue()` | `DownloadQueue.track` | **HIGH** (list) | `@EntityGraph(attributePaths = "track")` on `findAllByUser` |
| PlayHistoryService | `getMyHistory()` | `PlayHistory.track` | **HIGH** (paginated list) | `@EntityGraph(attributePaths = "track")` on `findAllByUserOrderByPlayedAtDesc` |
| UtilService | `getSubscriptionStatus()` | `UserSubscription.subscription` | LOW (single object) | Low priority; acceptable 1+1 pattern |

---

## 5. DTO/Entity Separation Verification

- **Entities never returned from controllers:** ✅ VERIFIED across all 7 controllers
- **Mapping location:** Service layer handles Entity→DTO conversion — **CORRECT** per dto-standards.md Section 1.4
- **No `toEntity()` on DTOs:** ✅ VERIFIED — builders used in services
- **Response DTOs use records:** ✅ VERIFIED (LikeResponse, DownloadQueueResponse, PlayHistoryListItemResponse, NoticeResponse, NoticeListItemResponse, UserDetailResponse, UserListItemResponse, TagResponse)

---

## 6. Recommended Fixes (Priority Order)

### Priority 1 — Critical (immediate)
```java
// TagController.java — add to createTag() method
@PostMapping
@PreAuthorize("hasRole('ADMIN')")   // ADD THIS LINE
@ResponseStatus(HttpStatus.CREATED)
public TagResponse createTag(@Valid @RequestBody TagCreateRequest request) {
    return tagService.createTag(request);
}
```

### Priority 2 — Major: N+1 Fix
```java
// LikeRepository.java
@EntityGraph(attributePaths = "track")
List<Like> findAllByUser(User user);

// DownloadQueueRepository.java
@EntityGraph(attributePaths = "track")
List<DownloadQueue> findAllByUser(User user);

// PlayHistoryRepository.java
@EntityGraph(attributePaths = "track")
Page<PlayHistory> findAllByUserOrderByPlayedAtDesc(User user, Pageable pageable);
```

### Priority 3 — Major: Transaction pattern
```java
// UserService.java, LikeService.java, DownloadQueueService.java
@Transactional(readOnly = true)   // Change class-level annotation
public class XxxService {
    // ...
    @Transactional   // Add to each mutating method
    public void create(...) { ... }
}
```

### Priority 4 — Major: NoticeController ResponseDTO
```java
// NoticeController.java — wrap all returns
return ResponseEntity.ok(ResponseDTO.<NoticeResponse>withSingleData().data(response).build());
```

### Priority 5 — Minor: Size validation
```java
// PlayHistoryService.java line 51, NoticeService.java line 49
PageRequest.of(Math.max(0, page - 1), Math.max(1, size))
```

---

## 7. Files Reviewed

**Services (7):** TagService, UserService, UtilService, PlayHistoryService, LikeService, DownloadQueueService, NoticeService

**Controllers (7):** TagController, UserController, UtilController, PlayHistoryController, LikeController, DownloadQueueController, NoticeController

**Support files:** SecurityConfig, GlobalExceptionHandler, ErrorCode, ResponseDTO; selected DTOs and Repositories as needed
