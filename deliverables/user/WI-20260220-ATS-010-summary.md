# API Logic Code Review Summary — WI-20260220-ATS-010

**WI**: WI-20260220-ATS-010
**Agent**: cr
**Date**: 2026-02-21
**Status**: CONDITIONAL APPROVAL → RESOLVED ✅

## Overall

The Auth/User API logic is well-structured and follows ATStudio architectural patterns correctly.
Two MAJOR issues and three MINOR issues were identified. MAJOR issues are **resolved**; MINOR issues noted.

## Issues Found & Resolved

### MAJOR (Fixed ✅)

**M-1. `AuthService.refresh()` exception-driven control flow**
- **Location**: `src/main/java/.../service/auth/AuthService.java:72-82`
- **Issue**: For every expired refresh token, `getUserID()` always threw `ExpiredJwtException` as normal flow — anti-pattern where exceptions control expected business logic.
- **Fix**: Added `JwtTokenProvider.getUserIDAllowExpired(String token)` method that handles both valid and expired tokens without relying on exception flow. `AuthService.refresh()` now calls this single method.

**M-2. `isProfileComplete` logic duplicated across two services**
- **Location**: `AuthService.java:62` + `UserService.java:82`
- **Issue**: `phonePersonal != null && job != null` check scattered in two places.
- **Fix**: Added `User.isProfileComplete()` domain method in entity. Both services now delegate to `user.isProfileComplete()`.

### MINOR (Noted)

**m-1. UtilController response wrapping vs api-spec flat format**
- `ResponseDTO<CheckResponse>` wrapping is used (project standard), but api-spec shows flat `{ "available": true }`. API spec should be updated to reflect the actual response shape.

**m-2. `UserRepository.findByRefreshToken()` unused + BCrypt incompatible**
- **Fix**: Removed the unused method from `UserRepository.java`.

**m-3. `UpdateProfileRequest` no at-least-one-field validation**
- Deferred — sending `{}` returns 200 OK harmlessly. Can be added if needed.

## Approved Items

- Controllers (AuthController, UserController, UtilController): zero business logic, pure delegation ✅
- `@Transactional(readOnly = true)` applied correctly on query methods ✅
- No entity exposed to controllers — all responses use DTOs ✅
- DTO standards: Request (`@Getter @Setter @NoArgsConstructor`), Response (`record`) ✅
- Exception handling: `BusinessException` + ENUM pattern ✅
- Refresh token rotation: BCrypt hash in DB, DB mismatch clears token ✅
- HTTP status codes: `201 Created` (register), `204 No Content` (withdraw) ✅
- `userID` naming convention (abbreviation-preserving camelCase) ✅
