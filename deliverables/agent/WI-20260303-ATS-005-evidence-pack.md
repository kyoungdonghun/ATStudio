# WI-20260303-ATS-005 Evidence Pack

## WI Header
- WI ID: WI-20260303-ATS-005
- REQ: REQ-20260303-ATS-003
- Agent: se
- Date: 2026-03-03

## File Change Pointers

### Entity Layer
| File | Lines | Notes |
|------|-------|-------|
| `entity/key/AlbumTrackId.java` | 1-21 | Composite PK, Serializable, @EqualsAndHashCode |
| `entity/AlbumTrack.java` | 1-34 | @EmbeddedId, @MapsId pattern, updateOrder() method |
| `entity/Album.java` | 1-51 | BaseEntity, softDelete(), update(), @Builder.Default isActive=true |

### Repository Layer
| File | Lines | Notes |
|------|-------|-------|
| `repository/AlbumRepository.java` | 1-11 | findAllByIsActiveTrueOrderByCreatedAtDesc() |
| `repository/AlbumTrackRepository.java` | 1-18 | @EntityGraph on findAll, existsByAlbumAndTrack for duplicate check |

### DTO Layer (9 files in dto/album/)
| File | Pattern | Notes |
|------|---------|-------|
| `AlbumCreateRequest.java` | Lombok class | @NotBlank title, @Size(max=100) |
| `AlbumUpdateRequest.java` | Lombok class | All optional |
| `AlbumTrackAddRequest.java` | record | @NotNull trackId |
| `AlbumTrackOrderItem.java` | record | trackId + order |
| `AlbumTrackOrderRequest.java` | record | @NotEmpty trackOrders list |
| `AlbumResponse.java` | record | static from(Album, int) factory |
| `AlbumListItemResponse.java` | record | static from(Album, int) factory |
| `AlbumTrackItemResponse.java` | record | static from(AlbumTrack) - includes artistName from track.user |
| `AlbumDetailResponse.java` | record | static from(Album, List) factory |

### Service Layer
| File | Method | Lines | Notes |
|------|--------|-------|-------|
| `AlbumService.java` | createAlbum() | 41-57 | StorageService for thumbnail, User lookup |
| `AlbumService.java` | getAlbums() | 61-68 | Active only, track count per album |
| `AlbumService.java` | getAlbum() | 72-82 | Active check, AlbumTrack list with @EntityGraph |
| `AlbumService.java` | updateAlbum() | 86-97 | Partial update, optional thumbnail |
| `AlbumService.java` | deleteAlbum() | 101-104 | Soft delete (isActive=false) |
| `AlbumService.java` | addTrack() | 108-126 | Duplicate check via existsByAlbumAndTrack, auto-order |
| `AlbumService.java` | removeTrack() | 130-138 | Find by composite PK then delete |
| `AlbumService.java` | reorderTracks() | 142-157 | In-place updateOrder() on each AlbumTrack |

### Controller Layer
| File | Endpoint | HTTP | Notes |
|------|----------|------|-------|
| `AlbumController.java` | /api/albums | POST | multipart/form-data, 201 Created |
| `AlbumController.java` | /api/albums | GET | Public, list |
| `AlbumController.java` | /api/albums/{id} | GET | Public, detail |
| `AlbumController.java` | /api/albums/{id} | PUT | multipart/form-data |
| `AlbumController.java` | /api/albums/{id} | DELETE | 204 No Content |
| `AlbumController.java` | /api/albums/{id}/tracks | POST | JSON body |
| `AlbumController.java` | /api/albums/{id}/tracks/{trackId} | DELETE | 204 No Content |
| `AlbumController.java` | /api/albums/{id}/tracks | PUT | JSON body, reorder |

### Security Config
| File | Lines | Notes |
|------|-------|-------|
| `SecurityConfig.java` | 89-96 | GET permitAll, POST/PUT/DELETE hasRole("ADMIN"), includes /tracks sub-paths |

### Documentation
| File | Section | Notes |
|------|---------|-------|
| `docs/design/api-spec.md` | Section 15 | 8 Album APIs added, total updated 79 -> 87 |
| `docs/design/db-schema.md` | Section 14 | albums + album_tracks tables, total 21 -> 23 |

## Design Decisions

1. **Composite PK pattern**: AlbumTrackId follows PlaylistTrackId pattern exactly (Serializable, @EqualsAndHashCode, @MapsId)
2. **Soft delete**: Album.softDelete() sets isActive=false; getActiveAlbum() helper rejects inactive albums with RESOURCE_NOT_FOUND
3. **Track order management**: reorderTracks() uses in-place updateOrder() on existing AlbumTrack entities (dirty checking), unlike Playlist which does delete-all-then-reinsert. This is more efficient for albums.
4. **Duplicate track prevention**: existsByAlbumAndTrack() throws RESOURCE_DUPLICATE (HTTP 409), distinct from Playlist's DATA_INTEGRITY_VIOLATION
5. **No subscription requirement**: Album is admin-curated, public read, no subscriber validation needed (unlike Playlist)
6. **@EntityGraph on findAllByAlbumOrderByTrackOrder**: Prevents N+1 for track lazy loading when listing album tracks
7. **SecurityConfig**: Added specific matchers for /api/albums/*/tracks and /api/albums/*/tracks/* to cover sub-path ADMIN restrictions

## Test Cases (10)
| # | Test | Assertion |
|---|------|-----------|
| 1 | createAlbum_success | id=1, title matches, trackCount=0 |
| 2 | getAlbums_returnsActiveOnly | Returns active albums, trackCount=0 for empty |
| 3 | getAlbum_notFound_throws | RESOURCE_NOT_FOUND for missing album |
| 4 | getAlbum_inactive_throws | RESOURCE_NOT_FOUND for soft-deleted album |
| 5 | updateAlbum_success | Title updated correctly |
| 6 | deleteAlbum_success | isActive becomes false |
| 7 | addTrack_success | albumTrackRepository.save() called |
| 8 | addTrack_duplicate_throws | RESOURCE_DUPLICATE on duplicate track |
| 9 | removeTrack_success | albumTrackRepository.delete() called |
| 10 | reorderTracks_success | trackOrder updated, response has 2 tracks |

## Verification Command
```bash
cd C:/Users/jm991/Desktop/project/ATStudio
gradlew.bat test --tests "com.atstudio.atstudio.service.AlbumServiceTest"
gradlew.bat test   # Full suite: 556 passed, 0 failures
```

## Follow-up WI
- None identified. Album domain implementation complete per spec.
