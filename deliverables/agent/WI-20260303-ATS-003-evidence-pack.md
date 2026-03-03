# WI-20260303-ATS-003 Evidence Pack

## WI Metadata

| Field | Value |
|-------|-------|
| WI ID | WI-20260303-ATS-003 |
| REQ | REQ-20260303-ATS-002 |
| Agent | se |
| Status | DONE |

## Change Pointers

### 1. BUSINESS_ERROR.java

- **Path**: `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java`
- **Lines**: 128-131
- **Change**: Added `PLAYLIST_LIMIT_EXCEEDED` enum constant with `HttpStatus.CONFLICT` (409)

```java
PLAYLIST_LIMIT_EXCEEDED(
        HttpStatus.CONFLICT,
        "플레이리스트는 최대 3개까지 생성할 수 있습니다.",
        "활성 플레이리스트 3개 초과 시도."),
```

### 2. PlaylistRepository.java

- **Path**: `src/main/java/com/atstudio/atstudio/repository/PlaylistRepository.java`
- **Line**: 13
- **Change**: Added Spring Data derived query method

```java
int countByUserAndIsActiveTrue(User user);
```

### 3. PlaylistService.java

- **Path**: `src/main/java/com/atstudio/atstudio/service/PlaylistService.java`
- **Lines**: 46-48
- **Change**: Added count validation after `validateSubscriber()` and before playlist creation

```java
if (playlistRepository.countByUserAndIsActiveTrue(user) >= 3) {
    throw new BusinessException(BUSINESS_ERROR.PLAYLIST_LIMIT_EXCEEDED);
}
```

### 4. PlaylistServiceTest.java

- **Path**: `src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java`
- **Lines**: 68-100
- **Tests added**:
  - `createPlaylist_limitExceeded_throws` (L68-84): Verifies `PLAYLIST_LIMIT_EXCEEDED` when count = 3
  - `createPlaylist_atLimit_succeeds` (L86-100): Verifies successful creation when count = 2

## Acceptance Criteria Verification

| Criterion | Status |
|-----------|--------|
| Active playlists >= 3 throws PLAYLIST_LIMIT_EXCEEDED | PASS (test: `createPlaylist_limitExceeded_throws`) |
| Active playlists = 2 allows creation | PASS (test: `createPlaylist_atLimit_succeeds`) |
| is_active=false excluded from count | PASS (guaranteed by `countByUserAndIsActiveTrue`) |
| gradlew.bat test 0 failures | PASS (546 tests, 0 failures) |

## Test Execution

```
Command: gradlew.bat test
Result: BUILD SUCCESSFUL in 36s
Tests: 546 total, 0 failures, 0 ignored
```

## Existing Test Compatibility

- `createPlaylist_success`: Mockito defaults `countByUserAndIsActiveTrue` to `0` (int default) -> passes limit check. No mock needed.
- `createPlaylist_notSubscribed`: Fails before count check (at `validateSubscriber`). No impact.

## Follow-up WI

None. This WI is self-contained.
