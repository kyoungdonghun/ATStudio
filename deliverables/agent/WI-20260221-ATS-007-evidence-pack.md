# WI-20260221-ATS-007 Evidence Pack

## WI Summary
PlayHistory CRUD implementation: save play history + atomic play_count increment, paginated list retrieval, selective/full deletion.

## Files Created

| File | Description |
|------|-------------|
| `src/main/java/com/atstudio/atstudio/dto/playhistory/PlayHistorySaveRequest.java` | Request DTO (record) for POST /api/play-histories |
| `src/main/java/com/atstudio/atstudio/dto/playhistory/PlayHistoryDeleteRequest.java` | Request DTO (record) for DELETE /api/play-histories |
| `src/main/java/com/atstudio/atstudio/dto/playhistory/PlayHistoryListItemResponse.java` | Response DTO (record) with nested TrackSummary for GET list |
| `src/main/java/com/atstudio/atstudio/service/PlayHistoryService.java` | Service: savePlayHistory, getMyHistory, deleteHistory |
| `src/main/java/com/atstudio/atstudio/controller/PlayHistoryController.java` | Controller: POST/GET/DELETE /api/play-histories |

## Files Modified

| File | Change |
|------|--------|
| `src/main/java/com/atstudio/atstudio/repository/TrackRepository.java` | Added `@Modifying @Query` `incrementPlayCount(Long trackId)` for atomic play_count +1 |
| `src/main/java/com/atstudio/atstudio/repository/PlayHistoryRepository.java` | Added 3 query methods: `findAllByUserOrderByPlayedAtDesc`, `deleteByIdInAndUser`, `deleteAllByUser` |

## SecurityConfig Verification

`/api/play-histories` endpoints are NOT in the permitAll list. They fall under the catch-all `.requestMatchers("/api/**").authenticated()` rule (line 88 of SecurityConfig.java). No SecurityConfig modification needed.

## Implementation Details

### play_count Atomicity
- `TrackRepository.incrementPlayCount()` uses `@Modifying @Query("UPDATE Track t SET t.playCount = t.playCount + 1 WHERE t.id = :trackId")`
- This generates a single SQL UPDATE statement, safe under concurrent requests (database-level atomic increment)
- Entity field `playCount` is NOT modified via setter -- constraint honored

### Pagination Pattern
- Follows `LicenseService.buildLicensePage()` pattern exactly
- Uses `ResponseDTO.<PlayHistoryListItemResponse>builder()` with `.dataList()` and `.pageInfo(PageInfo.of(page, size, total, 10))`
- Page parameter is 1-based (converted to 0-based via `Math.max(0, page - 1)`)

### Delete Logic
- `historyIds` empty list -> `deleteAllByUser(user)` (full clear)
- `historyIds` non-empty -> `deleteByIdInAndUser(ids, user)` (selective, user-scoped)
- User ownership enforced by Spring Data derived query (AND user = :user)

### DTO Compliance
- All 3 DTOs are Java records with `@JsonInclude(JsonInclude.Include.NON_NULL)`
- `PlayHistoryListItemResponse` contains nested `TrackSummary` record with `from(PlayHistory)` static factory
- `@NotNull` validation on request DTOs, `@Valid` on controller parameters

### Transaction Strategy
- Class-level `@Transactional(readOnly = true)` on PlayHistoryService
- Method-level `@Transactional` on `savePlayHistory` and `deleteHistory` (write operations)
- `getMyHistory` inherits class-level readOnly

## Build Result

**PENDING** -- Bash execution was denied. User must run manually:
```bash
gradlew.bat build -x test
```

## Notes for Test Writers (WI-010, WI-011)

### Unit Tests (WI-010: PlayHistoryServiceTest)
- `savePlayHistory`: mock trackRepository.findById to return active track, verify save + incrementPlayCount called
- `savePlayHistory`: inactive track -> TRACK_NOT_FOUND
- `savePlayHistory`: non-existent track -> TRACK_NOT_FOUND
- `savePlayHistory`: non-existent user -> RESOURCE_NOT_FOUND
- `getMyHistory`: verify pagination (page/size conversion, PageInfo construction)
- `deleteHistory`: empty historyIds -> verify deleteAllByUser called
- `deleteHistory`: non-empty historyIds -> verify deleteByIdInAndUser called

### Integration Tests (WI-011: PlayHistoryControllerTest)
- POST /api/play-histories: 201 Created with valid trackId + auth
- POST /api/play-histories: 401 without auth token
- POST /api/play-histories: 400 with null trackId (@Valid)
- GET /api/play-histories: 200 with pagination params
- DELETE /api/play-histories: 204 with historyIds
- DELETE /api/play-histories: 204 with empty historyIds (full clear)

### Repository Tests
- `TrackRepository.incrementPlayCount`: verify play_count incremented by 1
- `PlayHistoryRepository.findAllByUserOrderByPlayedAtDesc`: verify ordering + pagination
- `PlayHistoryRepository.deleteByIdInAndUser`: verify only user's records deleted
- `PlayHistoryRepository.deleteAllByUser`: verify all user's records deleted, others untouched

## Rollback Info
- TrackRepository: remove `incrementPlayCount` method and its 3 imports (`Modifying`, `Query`, `Param`)
- PlayHistoryRepository: revert to empty interface (remove `User` import, `Page`/`Pageable` imports, `List` import, 3 methods)
- Delete 5 created files listed above
