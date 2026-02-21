# WI-20260221-ATS-008 Evidence Pack

## WI Summary
- **WI ID:** WI-20260221-ATS-008
- **REQ:** REQ-20260221-ATS-002
- **Agent:** se
- **Scope:** Likes + Download Queue Service/Controller/DTO implementation

---

## Files Created

| # | File Path | Type | Description |
|---|-----------|------|-------------|
| 1 | `src/main/java/com/atstudio/atstudio/dto/like/LikeResponse.java` | DTO | Like response record (trackId, title, bpm, tonality, thumbnail, createdAt) |
| 2 | `src/main/java/com/atstudio/atstudio/dto/downloadqueue/DownloadQueueResponse.java` | DTO | DownloadQueue response record (same fields as LikeResponse) |
| 3 | `src/main/java/com/atstudio/atstudio/service/LikeService.java` | Service | addLike, getMyLikes, removeLike |
| 4 | `src/main/java/com/atstudio/atstudio/service/DownloadQueueService.java` | Service | addToQueue, getMyQueue, removeFromQueue |
| 5 | `src/main/java/com/atstudio/atstudio/controller/LikeController.java` | Controller | POST/GET/DELETE /api/likes |
| 6 | `src/main/java/com/atstudio/atstudio/controller/DownloadQueueController.java` | Controller | POST/GET/DELETE /api/download-queue |

## Files Modified

| # | File Path | Change |
|---|-----------|--------|
| 1 | `src/main/java/com/atstudio/atstudio/repository/LikeRepository.java` | Added `findAllByUser(User)`, `findByUserAndTrack_Id(User, Long)` |
| 2 | `src/main/java/com/atstudio/atstudio/repository/DownloadQueueRepository.java` | Added `findAllByUser(User)`, `findByUserAndTrack_Id(User, Long)` |

---

## Design Decisions

### 1. Explicit Duplicate Check (existsById) Instead of Relying on DB Exception

The handoff packet stated "DataIntegrityViolationException on duplicate -> GlobalExceptionHandler returns 409". However, entities with `@EmbeddedId` (composite PK) have a critical JPA behavior: `save()` always calls `merge()` instead of `persist()` because the ID is never null. This means:

- Duplicate `save()` silently updates (no INSERT, no exception)
- `DataIntegrityViolationException` is **never thrown** for composite PK duplicates

**Resolution:** Added explicit `existsById()` check before `save()`, throwing `BusinessException(BUSINESS_ERROR.DATA_INTEGRITY_VIOLATION)` which returns HTTP 409 CONFLICT. This matches the acceptance criteria.

### 2. DATA_INTEGRITY_VIOLATION (409) vs RESOURCE_DUPLICATE (400)

`RESOURCE_DUPLICATE` returns HTTP 400 (BAD_REQUEST), but the acceptance criteria requires HTTP 409 (CONFLICT) for duplicates. `DATA_INTEGRITY_VIOLATION` returns HTTP 409 (CONFLICT) with an appropriate client message. Selected `DATA_INTEGRITY_VIOLATION` to match the 409 requirement.

### 3. LikeId/DownloadQueueId Construction

Both entities require explicit `id` field in the builder: `Like.builder().id(new LikeId(userId, trackId)).user(user).track(track).build()`. This follows the pattern established in `LikeRepositoryTest.java`.

---

## Endpoint Summary

| Method | Path | Status | Auth | Description |
|--------|------|--------|------|-------------|
| POST | `/api/likes/{trackId}` | 201 | Required | Add like |
| GET | `/api/likes` | 200 | Required | List my likes |
| DELETE | `/api/likes/{trackId}` | 204 | Required | Remove like |
| POST | `/api/download-queue/{trackId}` | 201 | Required | Add to queue |
| GET | `/api/download-queue` | 200 | Required | List my queue |
| DELETE | `/api/download-queue/{trackId}` | 204 | Required | Remove from queue |

---

## Build Verification

- **Command:** `./gradlew build -x test`
- **Status:** PENDING - Bash execution was denied during agent session. User must run manually.

---

## Acceptance Criteria Verification

| Criteria | Status | Notes |
|----------|--------|-------|
| POST /api/likes/{trackId}: 201, duplicate 409 | IMPLEMENTED | existsById check + DATA_INTEGRITY_VIOLATION |
| GET /api/likes: 200 with track summary | IMPLEMENTED | LikeResponse record with from(Like) |
| DELETE /api/likes/{trackId}: 204, not found 404 | IMPLEMENTED | findByUserAndTrack_Id + RESOURCE_NOT_FOUND |
| POST /api/download-queue/{trackId}: 201, duplicate 409 | IMPLEMENTED | Same pattern as likes |
| GET /api/download-queue: 200 | IMPLEMENTED | DownloadQueueResponse record |
| DELETE /api/download-queue/{trackId}: 204 | IMPLEMENTED | Same pattern as likes |
| All endpoints: unauthenticated 401 | DELEGATED | Spring Security config handles this |
| ./gradlew build -x test | PENDING | User must run manually |

---

## Standards Compliance

- DTOs: Java 17 `record` + `@JsonInclude(NON_NULL)` per dto-standards.md
- Controllers: Thin, delegate to service, use `ResponseDTO` wrapper per development-standards.md Section 2A.2
- Services: `@Transactional` on class, `@Transactional(readOnly = true)` on reads per development-standards.md
- Exception handling: `BusinessException` with ENUM error codes per exception-handling.md
- Entity/DTO separation: `from()` static factory in DTO, conversion in service layer per dto-standards.md Section 1.4
- User lookup pattern: Follows `DownloadService.java` pattern (userRepository.findById + RESOURCE_NOT_FOUND)

---

## Reproduction Steps

1. Run `./gradlew build -x test` to verify compilation
2. Run `./gradlew test` to verify existing 209 tests are unaffected
3. Start application and test endpoints with authenticated requests
