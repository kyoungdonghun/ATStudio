# WI-20260221-ATS-015 Summary

## Change Summary

N+1 query issues in 3 Repository interfaces were resolved by adding `@EntityGraph(attributePaths = "track")` annotations. This ensures that when Like, DownloadQueue, or PlayHistory lists are queried by user, the associated Track entity is fetched via a single LEFT JOIN query instead of N separate queries.

## Changes Made

| # | File | Method | Fix |
|---|------|--------|-----|
| M-01 | `LikeRepository.java` | `findAllByUser(User user)` | Added `@EntityGraph(attributePaths = "track")` |
| M-02 | `DownloadQueueRepository.java` | `findAllByUser(User user)` | Added `@EntityGraph(attributePaths = "track")` |
| M-03 | `PlayHistoryRepository.java` | `findAllByUserOrderByPlayedAtDesc(User user, Pageable pageable)` | Added `@EntityGraph(attributePaths = "track")` |

All three files also received the import: `org.springframework.data.jpa.repository.EntityGraph`.

## Risk Assessment

- **Risk Level**: LOW
- No method signatures changed
- No business logic modified
- No new queries or methods added
- Only annotation-level changes on existing Spring Data JPA derived query methods

## Verification

- **Build**: Build verification was not executed due to Bash access restriction. Manual `gradlew.bat build -x test` recommended.
- **Entity field confirmation**: All three entities (Like, DownloadQueue, PlayHistory) have a `track` field with `@ManyToOne(fetch = FetchType.LAZY)`, confirming the `attributePaths = "track"` value is correct.
- **Full test verification**: Delegated to WI-017 (qa).
