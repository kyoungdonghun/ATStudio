# WI-20260221-ATS-018 Evidence Pack

## WI Reference
- WI ID: WI-20260221-ATS-018
- REQ: REQ-20260221-ATS-004
- Agent: se
- Handoff: `deliverables/agent/WI-20260221-ATS-018-handoff.md`

## Files Created/Modified

### Modified Files

| File | Lines | Change |
|------|-------|--------|
| `src/main/java/com/atstudio/atstudio/entity/Playlist.java` | 45 | Added `update(title, description, thumbnail)` (L36-40) and `deactivate()` (L42-44) |
| `src/main/java/com/atstudio/atstudio/repository/PlaylistRepository.java` | 12 | Added `findAllByUserAndIsActiveTrueOrderByCreatedAtDesc(User)` |
| `src/main/java/com/atstudio/atstudio/repository/PlaylistTrackRepository.java` | 17 | Added `findAllByIdPlaylistIdOrderByTrackOrderAsc` with `@EntityGraph(attributePaths="track")`, `countByIdPlaylistId`, `deleteAllByIdPlaylistId` |

### Created Files

| File | Lines | Description |
|------|-------|-------------|
| `src/main/java/com/atstudio/atstudio/dto/playlist/PlaylistResponse.java` | 25 | 3.1/3.5 response record with `from(Playlist, int)` factory |
| `src/main/java/com/atstudio/atstudio/dto/playlist/PlaylistListItemResponse.java` | 24 | 3.2 response record with `from(Playlist, int)` factory |
| `src/main/java/com/atstudio/atstudio/dto/playlist/PlaylistDetailResponse.java` | 30 | 3.3 response record with nested tracks list |
| `src/main/java/com/atstudio/atstudio/dto/playlist/PlaylistTrackItemResponse.java` | 22 | Track item within playlist detail; `from(PlaylistTrack)` extracts Track fields |
| `src/main/java/com/atstudio/atstudio/dto/playlist/PlaylistCreateRequest.java` | 16 | Lombok class for multipart binding (`@ModelAttribute`) |
| `src/main/java/com/atstudio/atstudio/dto/playlist/PlaylistUpdateRequest.java` | 15 | Lombok class for multipart binding (`@ModelAttribute`) |
| `src/main/java/com/atstudio/atstudio/dto/playlist/PlaylistAddTrackRequest.java` | 8 | Record with `@NotNull Long trackId` |
| `src/main/java/com/atstudio/atstudio/dto/playlist/PlaylistReorderRequest.java` | 10 | Record with `@NotEmpty List<PlaylistTrackOrderItem>` |
| `src/main/java/com/atstudio/atstudio/dto/playlist/PlaylistTrackOrderItem.java` | 9 | Record with `trackId` + `trackOrder` |
| `src/main/java/com/atstudio/atstudio/service/PlaylistService.java` | 209 | 8 API methods + 2 helper methods |
| `src/main/java/com/atstudio/atstudio/controller/PlaylistController.java` | 123 | 8 endpoint methods, thin controller pattern |

## Implementation Details

### Pattern Reuse

| Pattern | Source | Usage |
|---------|--------|-------|
| Composite PK + `existsById` | `LikeService.addLike()` | `PlaylistService.addTrack()` L103-106 |
| `@EntityGraph(attributePaths)` | `LikeRepository.findAllByUser()` | `PlaylistTrackRepository.findAllByIdPlaylistIdOrderByTrackOrderAsc()` |
| Subscriber check | `UtilService.getSubscriptionStatus()` | `PlaylistService.validateSubscriber()` L190-196 |
| `StorageService.store()` | `TrackService.createTrack()` | `PlaylistService.createPlaylist()` L43-45, `updatePlaylist()` L130-132 |
| `@Transactional(readOnly=true)` class-level | `LikeService`, `NoticeService`, `TrackService` | `PlaylistService` L24 |
| Thin controller + ResponseDTO | `NoticeController`, `TrackController` | `PlaylistController` all methods |
| Record DTOs + `@JsonInclude(NON_NULL)` | `NoticeResponse` | All response DTOs |
| Lombok class for multipart request | `TrackCreateRequest` | `PlaylistCreateRequest`, `PlaylistUpdateRequest` |

### Key Design Decisions

1. **Subscriber error code**: Used `NO_ACTIVE_SUBSCRIPTION` (403) instead of generic `FORBIDDEN` from handoff guide, since the codebase already has this dedicated error code for subscription-gated features.

2. **Owner check error code**: Used `RESOURCE_NOT_ACCESS` (403) which matches the existing codebase pattern for permission-denied scenarios.

3. **Reorder flush**: Added `playlistTrackRepository.flush()` between `deleteAll` and `saveAll` in `reorderTracks()` to ensure the persistence context sends the DELETE before INSERT, preventing unique constraint violations.

4. **trackOrder auto-assignment on addTrack**: Assigns `trackOrder = countByIdPlaylistId()` so new tracks are appended at the end.

5. **Request DTOs for multipart**: Used Lombok `@Getter/@Setter/@NoArgsConstructor` classes (not records) for `PlaylistCreateRequest`/`PlaylistUpdateRequest` because `@ModelAttribute` binding requires mutable objects with setters.

### N+1 Prevention

- `PlaylistTrackRepository.findAllByIdPlaylistIdOrderByTrackOrderAsc()` uses `@EntityGraph(attributePaths = "track")` to eagerly fetch the Track entity in a single query, preventing N+1 when building `PlaylistTrackItemResponse`.

### SecurityConfig

- No changes needed. Playlist endpoints fall under `.requestMatchers("/api/**").authenticated()` catch-all (SecurityConfig L88).
- Subscriber and owner authorization enforced in service layer, not via `@PreAuthorize`.

## Verification

- Manual code review: all imports resolve to existing classes
- Pattern consistency verified against LikeService, NoticeService, TrackService
- Build verification pending (Bash not available in this session)

## Follow-up WI

- **WI-20260221-ATS-019**: Unit tests for PlaylistService and PlaylistController (re agent)
