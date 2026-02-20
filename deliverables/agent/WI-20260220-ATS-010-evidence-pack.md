# WI-20260220-ATS-010 Evidence Pack: API Logic Code Review

**WI**: WI-20260220-ATS-010
**Agent**: cr
**Date**: 2026-02-21
**Scope**: AuthController, UserController, UtilController, AuthService, UserService, all Auth/User DTOs, User entity
**Standards**: core-principles.md, development-standards.md, dto-standards.md, exception-handling.md

---

## Issues Found & Fixed

### M-1: Exception-driven control flow in AuthService.refresh()
- **Root cause**: `getUserID()` always threw `ExpiredJwtException` for expired tokens, using exception handling as normal control flow.
- **Fix**: Added `JwtTokenProvider.getUserIDAllowExpired(String token)` that extracts sub from claims without throwing for expired tokens.
- **Files changed**:
  - `security/JwtTokenProvider.java` — added `getUserIDAllowExpired()` method
  - `service/auth/AuthService.java` — replaced try/catch block with single `getUserIDAllowExpired()` call; removed unused `ExpiredJwtException` import

### M-2: isProfileComplete logic centralized in entity
- **Root cause**: `phonePersonal != null && job != null` duplicated in `AuthService.java:62` and `UserService.java:82`
- **Fix**: Added `User.isProfileComplete()` domain method in entity.
- **Files changed**:
  - `entity/User.java` — added `public boolean isProfileComplete()` method
  - `service/auth/AuthService.java` — line 62: `user.isProfileComplete()`
  - `service/UserService.java` — line 82: `user.isProfileComplete()`

### m-2: UserRepository.findByRefreshToken() removed
- **Root cause**: Method was unused and could not work correctly since refresh tokens are BCrypt-hashed in DB.
- **Fix**: Removed from `repository/UserRepository.java`

---

## Code Quality Findings (Approved)

- Controllers: thin delegation only, no business logic ✅
- `@Transactional(readOnly = true)` on all query methods ✅
- Entity never exposed to controllers ✅
- Request DTOs: Bean Validation annotations complete ✅
- Response DTOs: Java 17 `record` ✅
- Error handling: `BusinessException` + `BUSINESS_ERROR` ENUM ✅
- HTTP status: 201 (register), 204 (withdraw), 200 (others) ✅

---

## Build & Test Evidence

```
gradlew.bat build -x test → BUILD SUCCESSFUL in 3s
gradlew.bat test          → BUILD SUCCESSFUL in 14s (131/131 PASS)
```

---

## Deferred Items

- **m-1**: UtilController `ResponseDTO<CheckResponse>` wrapper vs api-spec flat format → update api-spec.md
- **m-3**: `UpdateProfileRequest` at-least-one-field validation → low priority, harmless
- **S-2**: `UserService.toResponse()` → `UserResponse.from(User)` static factory → optional refactor
