# WI-20260220-ATS-005 Evidence Pack

## Agent: SE (Software Engineer)
## Date: 2026-02-21
## Status: COMPLETE (pending compile verification)

## Patch Rationale

Implemented User CRUD operations (registration, profile read/update, withdrawal, social profile completion) and utility duplicate-check endpoints per the API specification provided in the handoff packet. All implementations reuse the existing exception handling framework (`BusinessException` + `BUSINESS_ERROR` enum), the `ResponseDTO` wrapper, and the `CustomUserDetails` for authentication principal extraction.

## File-Level Change Pointers

### Modified: `src/main/java/com/atstudio/atstudio/entity/User.java`
- **Lines 70-88**: Added three domain methods:
  - `updateProfile(nickname, phonePersonal, phoneCompany, job)` — null-safe partial update
  - `withdraw()` — sets `isDeleted=true`, clears `refreshToken`
  - `completeProfile(nickname, phonePersonal, phoneCompany, job, userType)` — full profile set for social accounts

### Modified: `src/main/java/com/atstudio/atstudio/repository/UserRepository.java`
- **Line 16**: Added `findByPhonePersonal(String phonePersonal)` for phone duplicate check

### Created: `src/main/java/com/atstudio/atstudio/dto/user/RegisterRequest.java`
- Bean validation: `@NotBlank` nickname/email/password, `@Email`, `@Size`, `@NotNull` userType

### Created: `src/main/java/com/atstudio/atstudio/dto/user/UserResponse.java`
- Java record with 10 fields matching API spec

### Created: `src/main/java/com/atstudio/atstudio/dto/user/UpdateProfileRequest.java`
- Optional fields only (all nullable), no `userType` field (change forbidden)

### Created: `src/main/java/com/atstudio/atstudio/dto/user/WithdrawRequest.java`
- `@NotBlank` password only

### Created: `src/main/java/com/atstudio/atstudio/dto/user/CompleteProfileRequest.java`
- `@NotBlank` nickname/phonePersonal, `@NotNull` job/userType

### Created: `src/main/java/com/atstudio/atstudio/dto/util/CheckResponse.java`
- Simple `record CheckResponse(boolean available)`

### Created: `src/main/java/com/atstudio/atstudio/service/UserService.java`
- 7 public methods: `register`, `getMyProfile`, `updateMyProfile`, `withdraw`, `completeProfile`, `isEmailAvailable`, `isPhoneAvailable`, `isNicknameAvailable`
- Dependencies: `UserRepository`, `PasswordEncoder`
- Error codes used: `EMAIL_ALREADY_REGISTERED`, `NICKNAME_DUPLICATED`, `RESOURCE_NOT_FOUND`, `INVALID_CREDENTIALS`, `PROFILE_ALREADY_COMPLETE`

### Created: `src/main/java/com/atstudio/atstudio/controller/UserController.java`
- 5 endpoints under `/api/users`
- POST (public), GET/PUT/DELETE `/me` (auth required), PUT `/me/complete-profile` (auth required)

### Created: `src/main/java/com/atstudio/atstudio/controller/UtilController.java`
- 3 GET endpoints under `/api/utils` (all public)
- `check-email`, `check-phone`, `check-nickname`

## Dependencies on Other WIs
- **WI-003**: `User.java` entity with `refreshToken` field (consumed)
- **WI-004**: Auth endpoints (`/api/auth/*`) — parallel, no conflict. WI-004 handles `SecurityConfig` permitAll rules for `/api/users` POST and `/api/utils/**`

## Reproduction / Verification
```bash
# Compile check
gradlew.bat compileJava

# Full build (if tests exist)
gradlew.bat build
```

## Follow-up WI
- **WI-008** (if applicable): SecurityConfig must configure:
  - `.requestMatchers(HttpMethod.POST, "/api/users").permitAll()`
  - `.requestMatchers("/api/utils/**").permitAll()`
  - All `/api/users/me/**` require authentication
- **Testing**: JUnit5 tests for UserService (register, withdraw, completeProfile edge cases)
