# WI-20260228-ATS-001 Summary

## Change Summary

### CR-P-001 (CRITICAL) -- SecurityConfig /api/users/me wildcard fix

**Problem:** `/api/users/*` with `hasRole("ADMIN")` matched `/api/users/me` before any `authenticated()` rule, blocking all non-admin users from their own profile endpoints (GET/PUT/DELETE /api/users/me).

**Fix:** Added explicit `authenticated()` rules for `/api/users/me` (GET, PUT, DELETE), `/api/users/me/complete-profile` (PUT), and `/api/users/me/password` (PUT) before the ADMIN wildcard rules.

**Verification:** 7 new integration tests in `SecurityFilterChainTest` confirm USER-role JWT tokens receive 200/204 on all `/api/users/me` endpoints.

### CR-C-002 (CRITICAL) -- AuthService/OAuth2Service @Transactional(readOnly=true)

**Problem:** Both services used `@Transactional` at class level without `readOnly = true`, opening unnecessary read-write transactions on all methods.

**Fix:**
- Changed class-level annotation to `@Transactional(readOnly = true)` on both `AuthService` and `OAuth2Service`.
- Added `@Transactional` (read-write) override on mutating methods: `login()`, `socialLogin()`, `refresh()` in AuthService; `processSocialLogin()` in OAuth2Service.

**Verification:** Existing 7 AuthService unit tests pass unchanged. No behavioral regression.

### CR-C-003 (MAJOR) -- UserService.updatePassword() current password verification

**Problem:** No password change method existed with current-password verification.

**Fix:**
- Added `User.updatePassword(String encodedPassword)` domain method.
- Added `UserService.updatePassword(Long userID, UpdatePasswordRequest request)` with BCrypt current-password verification. Throws `INVALID_CREDENTIALS` on mismatch (including social accounts with null password).
- Added `UpdatePasswordRequest` DTO (currentPassword, newPassword with @NotBlank validation).
- Added `PUT /api/users/me/password` controller endpoint returning 204 No Content.

**Verification:** 4 new unit tests in `UserServiceTest` cover: success, wrong password, null password (social user), user not found.

## Risk Assessment

- **Low risk:** All changes are additive. Existing ADMIN wildcard rules are untouched (only order changed by inserting specific rules above them).
- **No DB schema changes.**
- **No behavioral regression:** All existing tests pass (0 failures).

## Test Results

- **Total tests:** BUILD SUCCESSFUL, 0 failures
- **New tests added:** 11 (7 in SecurityFilterChainTest, 4 in UserServiceTest)
- **Existing tests:** All pass unchanged
