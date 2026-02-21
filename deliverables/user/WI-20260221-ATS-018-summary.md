# WI-20260221-ATS-018 Summary

## Change Summary

Playlist domain 8 APIs fully implemented as specified in api-spec.md Section 3.

### APIs Implemented

| # | Method | Endpoint | Status | Response |
|---|--------|----------|--------|----------|
| 3.1 | POST | `/api/playlists` | 201 | PlaylistResponse |
| 3.2 | GET | `/api/playlists` | 200 | List\<PlaylistListItemResponse\> |
| 3.3 | GET | `/api/playlists/{id}` | 200 | PlaylistDetailResponse |
| 3.4 | POST | `/api/playlists/{id}/tracks` | 201 | - |
| 3.5 | PUT | `/api/playlists/{id}` | 200 | PlaylistResponse |
| 3.6 | PUT | `/api/playlists/{id}/tracks` | 200 | - |
| 3.7 | DELETE | `/api/playlists/{id}/tracks/{trackId}` | 204 | - |
| 3.8 | DELETE | `/api/playlists/{id}` | 204 | - |

### Security

- All 8 APIs: subscriber check via `UserSubscriptionRepository.findActiveByUser()` -- non-subscriber gets 403 (`NO_ACTIVE_SUBSCRIPTION`)
- APIs 3.3--3.8: owner check (`playlist.user.id == currentUser.id`) -- mismatch gets 403 (`RESOURCE_NOT_ACCESS`)
- No `@PreAuthorize` needed; subscriber+owner checks enforced in service layer
- SecurityConfig catch-all `.requestMatchers("/api/**").authenticated()` handles authentication

### Created/Modified Files

| File | Action |
|------|--------|
| `entity/Playlist.java` | Modified -- added `update()` and `deactivate()` methods |
| `repository/PlaylistRepository.java` | Modified -- added `findAllByUserAndIsActiveTrueOrderByCreatedAtDesc()` |
| `repository/PlaylistTrackRepository.java` | Modified -- added 3 query methods + `@EntityGraph` |
| `dto/playlist/PlaylistResponse.java` | Created |
| `dto/playlist/PlaylistListItemResponse.java` | Created |
| `dto/playlist/PlaylistDetailResponse.java` | Created |
| `dto/playlist/PlaylistTrackItemResponse.java` | Created |
| `dto/playlist/PlaylistCreateRequest.java` | Created |
| `dto/playlist/PlaylistUpdateRequest.java` | Created |
| `dto/playlist/PlaylistAddTrackRequest.java` | Created |
| `dto/playlist/PlaylistReorderRequest.java` | Created |
| `dto/playlist/PlaylistTrackOrderItem.java` | Created |
| `service/PlaylistService.java` | Created |
| `controller/PlaylistController.java` | Created |

### Risk

- **LOW**: Standard CRUD patterns reused from LikeService, NoticeService, TrackService
- **MEDIUM**: Reorder (3.6) uses `deleteAll + saveAll` in same transaction -- requires `flush()` between delete and insert to avoid constraint violations
- Build verification could not be run due to environment restrictions; manual compilation check is recommended

### Verification

```bash
gradlew.bat compileJava
gradlew.bat test
```

Test coverage to be provided by WI-20260221-ATS-019 (re agent).
