# WI-20260221-ATS-015 Evidence Pack

## WI Reference
- **WI ID**: WI-20260221-ATS-015
- **REQ**: REQ-20260221-ATS-003
- **Agent**: se
- **Date**: 2026-02-21

## Entity Field Verification

Before applying `@EntityGraph`, the `track` field name was verified in each entity:

| Entity | File:Line | Field Declaration |
|--------|-----------|-------------------|
| `Like` | `Like.java:31` | `private Track track;` with `@ManyToOne(fetch = FetchType.LAZY)` at line 28 |
| `DownloadQueue` | `DownloadQueue.java:31` | `private Track track;` with `@ManyToOne(fetch = FetchType.LAZY)` at line 28 |
| `PlayHistory` | `PlayHistory.java:29` | `private Track track;` with `@ManyToOne(fetch = FetchType.LAZY)` at line 28 |

All three entities use the field name `track` -- matching `attributePaths = "track"`.

## Change Details

### M-01: LikeRepository.java

**File**: `src/main/java/com/atstudio/atstudio/repository/LikeRepository.java`

**Before (lines 6-13)**:
```java
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, LikeId> {

    List<Like> findAllByUser(User user);
```

**After (lines 6-15)**:
```java
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, LikeId> {

    @EntityGraph(attributePaths = "track")
    List<Like> findAllByUser(User user);
```

### M-02: DownloadQueueRepository.java

**File**: `src/main/java/com/atstudio/atstudio/repository/DownloadQueueRepository.java`

**Before (lines 6-13)**:
```java
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DownloadQueueRepository extends JpaRepository<DownloadQueue, DownloadQueueId> {

    List<DownloadQueue> findAllByUser(User user);
```

**After (lines 6-15)**:
```java
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DownloadQueueRepository extends JpaRepository<DownloadQueue, DownloadQueueId> {

    @EntityGraph(attributePaths = "track")
    List<DownloadQueue> findAllByUser(User user);
```

### M-03: PlayHistoryRepository.java

**File**: `src/main/java/com/atstudio/atstudio/repository/PlayHistoryRepository.java`

**Before (lines 7-13)**:
```java
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {

    Page<PlayHistory> findAllByUserOrderByPlayedAtDesc(User user, Pageable pageable);
```

**After (lines 7-15)**:
```java
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {

    @EntityGraph(attributePaths = "track")
    Page<PlayHistory> findAllByUserOrderByPlayedAtDesc(User user, Pageable pageable);
```

## Expected SQL Behavior

### Before (N+1 pattern)
```sql
-- 1 query: fetch all likes for user
SELECT * FROM likes WHERE user_id = ?;
-- N queries: one per like to fetch track
SELECT * FROM tracks WHERE id = ?;  -- repeated N times
```

### After (single JOIN)
```sql
-- 1 query: fetch all likes with tracks via LEFT JOIN
SELECT l.*, t.* FROM likes l LEFT JOIN tracks t ON l.track_id = t.id WHERE l.user_id = ?;
```

Same pattern applies to `download_queue` and `play_histories` tables.

## Acceptance Criteria Checklist

- [x] LikeRepository.findAllByUser(User user) -- `@EntityGraph(attributePaths = "track")` applied (line 14)
- [x] DownloadQueueRepository.findAllByUser(User user) -- `@EntityGraph(attributePaths = "track")` applied (line 14)
- [x] PlayHistoryRepository.findAllByUserOrderByPlayedAtDesc(User user, Pageable pageable) -- `@EntityGraph(attributePaths = "track")` applied (line 14)
- [x] `import org.springframework.data.jpa.repository.EntityGraph` added to all 3 files
- [ ] Compilation verified (Bash access restricted; manual verification required)

## Files Modified (exhaustive list)

1. `src/main/java/com/atstudio/atstudio/repository/LikeRepository.java`
2. `src/main/java/com/atstudio/atstudio/repository/DownloadQueueRepository.java`
3. `src/main/java/com/atstudio/atstudio/repository/PlayHistoryRepository.java`

No other files were modified.

## Follow-up

- **WI-017 (qa)**: Build + test verification delegated
- **Rollback**: `git diff` on the 3 repository files; revert by removing `@EntityGraph` annotation and import line
