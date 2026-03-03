[WI-007 EVIDENCE PACK]
WI ID: WI-20260303-ATS-007
REQ: REQ-20260303-ATS-003
Agent: se
Date: 2026-03-04

## 1. Change Pointers

### File 1: AlbumTrackRepository.java
Path: `src/main/java/com/atstudio/atstudio/repository/AlbumTrackRepository.java`

**M-1 (line 16): @EntityGraph attributePaths expanded**
Before:
```java
@EntityGraph(attributePaths = "track")
List<AlbumTrack> findAllByAlbumOrderByTrackOrder(Album album);
```
After:
```java
@EntityGraph(attributePaths = {"track", "track.user"})
List<AlbumTrack> findAllByAlbumOrderByTrackOrder(Album album);
```

**M-2 (line 21): countByAlbum() added**
Before: (not present)
After:
```java
long countByAlbum(Album album);
```

### File 2: AlbumService.java
Path: `src/main/java/com/atstudio/atstudio/service/AlbumService.java`

**M-2 Site 1 (line 65) - getAlbums():**
Before:
```java
int trackCount = albumTrackRepository.findAllByAlbumOrderByTrackOrder(album).size();
```
After:
```java
int trackCount = (int) albumTrackRepository.countByAlbum(album);
```

**M-2 Site 2 (line 99) - updateAlbum():**
Before:
```java
int trackCount = albumTrackRepository.findAllByAlbumOrderByTrackOrder(album).size();
```
After:
```java
int trackCount = (int) albumTrackRepository.countByAlbum(album);
```

**M-2 Site 3 (line 125) - addTrack():**
Before:
```java
int nextOrder = albumTrackRepository.findAllByAlbumOrderByTrackOrder(album).size();
```
After:
```java
int nextOrder = (int) albumTrackRepository.countByAlbum(album);
```

### File 3: AlbumServiceTest.java
Path: `src/test/java/com/atstudio/atstudio/service/AlbumServiceTest.java`

**getAlbums_returnsActiveOnly (lines 75-76):**
Before:
```java
given(albumTrackRepository.findAllByAlbumOrderByTrackOrder(album))
        .willReturn(List.of());
```
After:
```java
given(albumTrackRepository.countByAlbum(album))
        .willReturn(0L);
```

**updateAlbum_success (lines 124-125):**
Before:
```java
given(albumTrackRepository.findAllByAlbumOrderByTrackOrder(album))
        .willReturn(List.of());
```
After:
```java
given(albumTrackRepository.countByAlbum(album))
        .willReturn(0L);
```

**addTrack_success (line 160):**
Before:
```java
given(albumTrackRepository.findAllByAlbumOrderByTrackOrder(album))
        .willReturn(List.of());
```
After:
```java
given(albumTrackRepository.countByAlbum(album)).willReturn(0L);
given(albumTrackRepository.findAllByAlbumOrderByTrackOrder(album))
        .willReturn(List.of());
```
Note: addTrack() calls countByAlbum for nextOrder, then getAlbum() which uses findAllByAlbumOrderByTrackOrder for the detail response. Both mocks are required.

## 2. Test Results

```
BUILD SUCCESSFUL in 43s
Tests: 556 passed / 556 total
Failures: 0
```

## 3. Rationale

- **M-1**: `AlbumTrackItemResponse.from()` accesses `albumTrack.getTrack().getUser().getNickname()`. Since `Track.user` is `@ManyToOne(fetch = LAZY)`, each track in the list triggers a separate `SELECT` for `User`. Adding `"track.user"` to `@EntityGraph` fetches all data in a single JOIN query.
- **M-2**: Three call sites used `findAllByAlbumOrderByTrackOrder(album).size()` solely to get a count. This loaded all `AlbumTrack` + joined `Track` + `User` entities into memory just to call `.size()`. Replacing with `countByAlbum()` generates a `SELECT COUNT(*)` query -- no entity hydration, no join, minimal memory.

## 4. Risk Assessment

- **Low risk**: Both changes are query-level optimizations with no behavioral change.
- `countByAlbum` is a Spring Data derived query method -- no custom JPQL needed.
- `@EntityGraph` expansion is additive -- existing functionality unchanged.

## 5. Follow-up

- No follow-up WI required. Both fixes are self-contained.
