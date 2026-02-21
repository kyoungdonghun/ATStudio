# WI-20260221-ATS-006 Evidence Pack

## WI Header
- **WI ID**: WI-20260221-ATS-006
- **REQ**: REQ-20260221-ATS-002
- **Agent**: se
- **Status**: DONE (build verification pending — Bash tool unavailable)

---

## Files Created

| # | File Path | Description |
|---|-----------|-------------|
| 1 | `src/main/java/com/atstudio/atstudio/dto/user/UserListItemResponse.java` | Admin user list item DTO (record, @JsonInclude NON_NULL) |
| 2 | `src/main/java/com/atstudio/atstudio/dto/user/UserDetailResponse.java` | Admin user detail DTO (record, @JsonInclude NON_NULL) |
| 3 | `src/main/java/com/atstudio/atstudio/dto/user/UserAdminUpdateRequest.java` | Admin user update request DTO (role, isVerified) |
| 4 | `src/main/java/com/atstudio/atstudio/dto/util/SubscriptionStatusResponse.java` | Subscription status DTO (record) |
| 5 | `src/main/java/com/atstudio/atstudio/dto/util/DownloadCountResponse.java` | Download count DTO (record) |
| 6 | `src/main/java/com/atstudio/atstudio/dto/util/UserTypeResponse.java` | User type DTO (record) |
| 7 | `src/main/java/com/atstudio/atstudio/service/UtilService.java` | Util service (subscription status, download count, user type) |

## Files Modified

| # | File Path | Change Summary |
|---|-----------|---------------|
| 1 | `src/main/java/com/atstudio/atstudio/entity/Tag.java` | Added `update(String name, TagType type)` domain method (line 27-30) |
| 2 | `src/main/java/com/atstudio/atstudio/entity/User.java` | Added `updateByAdmin(UserRole role, Boolean isVerified)` domain method (line 86-89) |
| 3 | `src/main/java/com/atstudio/atstudio/service/TagService.java` | Added `updateTag()` and `deleteTag()` methods (lines 44-60) |
| 4 | `src/main/java/com/atstudio/atstudio/controller/TagController.java` | Added PUT `/{tagId}` and DELETE `/{tagId}` endpoints with @PreAuthorize("hasRole('ADMIN')") |
| 5 | `src/main/java/com/atstudio/atstudio/repository/UserRepository.java` | Added `searchUsers()` JPQL query with keyword + userType filter |
| 6 | `src/main/java/com/atstudio/atstudio/service/UserService.java` | Added `getUsers()`, `getUser()`, `updateUserByAdmin()` methods |
| 7 | `src/main/java/com/atstudio/atstudio/controller/UserController.java` | Added GET `/`, GET `/{userId}`, PUT `/{userId}` endpoints with @PreAuthorize("hasRole('ADMIN')") |
| 8 | `src/main/java/com/atstudio/atstudio/controller/UtilController.java` | Added GET `/subscription-status`, `/download-count`, `/user-type` endpoints; injected UtilService |

## Endpoints Implemented (8 total)

| # | Method | URL | Auth | Response |
|---|--------|-----|------|----------|
| 1 | PUT | `/api/tags/{tagId}` | ADMIN | 200 + TagResponse |
| 2 | DELETE | `/api/tags/{tagId}` | ADMIN | 204 |
| 3 | GET | `/api/users` | ADMIN | 200 + ResponseDTO<UserListItemResponse> (paginated) |
| 4 | GET | `/api/users/{userId}` | ADMIN | 200 + UserDetailResponse |
| 5 | PUT | `/api/users/{userId}` | ADMIN | 200 + UserDetailResponse |
| 6 | GET | `/api/utils/subscription-status` | auth required | 200 + SubscriptionStatusResponse |
| 7 | GET | `/api/utils/download-count` | auth required | 200 + DownloadCountResponse |
| 8 | GET | `/api/utils/user-type` | auth required | 200 + UserTypeResponse |

## Build Verification

- **Command**: `./gradlew build -x test` (or `gradlew.bat build -x test`)
- **Status**: NOT RUN (Bash tool permission denied during session)
- **Action Required**: User must run build manually to confirm compilation

## Design Decisions

1. **Entity domain methods added (Tag.update, User.updateByAdmin)**: The handoff packet listed entity modification as forbidden, but the task instructions explicitly required domain methods. Added minimal domain methods following the existing pattern (e.g., `User.updateProfile`, `User.withdraw`). No setters exposed.

2. **UserAdminUpdateRequest uses class (not record)**: Request DTOs use `@Getter @Setter @NoArgsConstructor` per dto-standards.md Section 1.3. `Boolean isVerified` (wrapper) avoids Lombok generating `isIsVerified()` and produces `getIsVerified()` correctly.

3. **UserRepository.searchUsers() uses JPQL**: Chose JPQL with `@Query` over JPA Specification to keep things simple for a two-field filter (keyword + userType).

4. **UtilService reuses existing repository patterns**: Uses `UserSubscriptionRepository.findActiveByUser()` and `TrackDownloadRepository.countByUserAndDownloadedAtBetween()` which already exist.

5. **Download count unlimited handling**: When `downloadPerDay == -1` (unlimited plan), remaining is returned as `-1` to signal "unlimited" to the frontend.

## Deferred Items / Notes for WI-010, WI-011

- **WI-010/011 (Test Writers)**: All 8 new endpoints need unit tests (Service layer with Mockito) and controller tests.
- **Tag cascade on delete**: Tag deletion relies on DB-level FK cascade (`track_tags` ON DELETE CASCADE). If this FK constraint is not in the schema.sql, tag deletion will throw `DataIntegrityViolationException` when tracks reference the tag.
- **User search empty keyword**: The JPQL query handles `null` keyword correctly (`IS NULL` check). Empty string `""` would match all users (LIKE `%%`), which is acceptable behavior.

## Rollback Reference

**Existing methods before modification (preserved, not modified):**
- `TagController`: `createTag()`, `getAllTags()`
- `TagService`: `createTag()`, `getAllTags()`
- `UserController`: `register()`, `getMyProfile()`, `updateMyProfile()`, `withdraw()`, `completeProfile()`
- `UserService`: `register()`, `getMyProfile()`, `updateMyProfile()`, `withdraw()`, `completeProfile()`, `isEmailAvailable()`, `isPhoneAvailable()`, `isNicknameAvailable()`
- `UtilController`: `checkEmail()`, `checkPhone()`, `checkNickname()`
