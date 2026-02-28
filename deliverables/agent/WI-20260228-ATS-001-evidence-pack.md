# WI-20260228-ATS-001 Evidence Pack

## WI Header

- **WI ID:** WI-20260228-ATS-001
- **REQ:** REQ-20260228-ATS-010
- **Agent:** se
- **Status:** DONE
- **Blocks:** WI-20260228-ATS-004

---

## Change Pointers

### 1. SecurityConfig.java (CR-P-001)

**File:** `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java`
**Lines:** 71-76 (inserted before existing ADMIN rules at line 77+)

**Before:**
```java
// ADMIN
.requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
.requestMatchers(HttpMethod.GET, "/api/users/*").hasRole("ADMIN")
.requestMatchers(HttpMethod.PUT, "/api/users/*").hasRole("ADMIN")
```

**After:**
```java
// USER -- /api/users/me must precede ADMIN wildcard /api/users/*
.requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
.requestMatchers(HttpMethod.PUT, "/api/users/me").authenticated()
.requestMatchers(HttpMethod.DELETE, "/api/users/me").authenticated()
.requestMatchers(HttpMethod.PUT, "/api/users/me/complete-profile").authenticated()
.requestMatchers(HttpMethod.PUT, "/api/users/me/password").authenticated()
// ADMIN
.requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
.requestMatchers(HttpMethod.GET, "/api/users/*").hasRole("ADMIN")
.requestMatchers(HttpMethod.PUT, "/api/users/*").hasRole("ADMIN")
```

**Rationale:** Spring Security 6 applies the first matching rule. `/api/users/*` matches `/api/users/me`. By placing explicit `/api/users/me` rules first, normal users are routed to `authenticated()` instead of `hasRole("ADMIN")`.

---

### 2. AuthService.java (CR-C-002)

**File:** `src/main/java/com/atstudio/atstudio/service/auth/AuthService.java`
**Lines:** 24, 35, 54, 68

**Changes:**
- Line 24: `@Transactional` -> `@Transactional(readOnly = true)` (class level)
- Line 35: Added `@Transactional` on `login()`
- Line 54: Added `@Transactional` on `socialLogin()`
- Line 68: Added `@Transactional` on `refresh()`

---

### 3. OAuth2Service.java (CR-C-002)

**File:** `src/main/java/com/atstudio/atstudio/service/auth/OAuth2Service.java`
**Lines:** 22, 58

**Changes:**
- Line 22: `@Transactional` -> `@Transactional(readOnly = true)` (class level)
- Line 58: Added `@Transactional` on `processSocialLogin()`

---

### 4. User.java (CR-C-003 support)

**File:** `src/main/java/com/atstudio/atstudio/entity/User.java`
**Lines:** 91-93 (new method)

**Added:**
```java
public void updatePassword(String encodedPassword) {
    this.password = encodedPassword;
}
```

---

### 5. UpdatePasswordRequest.java (CR-C-003 -- new file)

**File:** `src/main/java/com/atstudio/atstudio/dto/user/UpdatePasswordRequest.java`

**New DTO:**
```java
@Getter @Setter @NoArgsConstructor
public class UpdatePasswordRequest {
    @NotBlank private String currentPassword;
    @NotBlank private String newPassword;
}
```

---

### 6. UserService.java (CR-C-003)

**File:** `src/main/java/com/atstudio/atstudio/service/UserService.java`
**Lines:** 85-97 (new method)

**Added:**
```java
@Transactional
public void updatePassword(Long userID, UpdatePasswordRequest request) {
    User user = userRepository.findById(userID)
            .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    if (user.getPassword() == null
            || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
        throw new BusinessException(BUSINESS_ERROR.INVALID_CREDENTIALS);
    }
    user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
}
```

**Error code:** Reuses `INVALID_CREDENTIALS` (HttpStatus.UNAUTHORIZED, 401) -- consistent with `withdraw()` method pattern for password verification.

---

### 7. UserController.java (CR-C-003 -- new endpoint)

**File:** `src/main/java/com/atstudio/atstudio/controller/UserController.java`
**Lines:** 59-65 (new method)

**Added:**
```java
@PutMapping("/me/password")
public ResponseEntity<Void> updatePassword(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody UpdatePasswordRequest request) {
    userService.updatePassword(userDetails.getId(), request);
    return ResponseEntity.noContent().build();
}
```

---

## Test Changes

### SecurityFilterChainTest.java (7 new tests)

**File:** `src/test/java/com/atstudio/atstudio/controller/SecurityFilterChainTest.java`

| Test Method | Assertion | CR |
|-------------|-----------|-----|
| `getUsersMe_userToken_returns200()` | GET /api/users/me with USER JWT -> 200 | CR-P-001 |
| `putUsersMe_userToken_returns200()` | PUT /api/users/me with USER JWT -> 200 | CR-P-001 |
| `deleteUsersMe_userToken_returns204()` | DELETE /api/users/me with USER JWT -> 204 | CR-P-001 |
| `putUsersMeCompleteProfile_userToken_notForbidden()` | PUT /api/users/me/complete-profile -> not 403 | CR-P-001 |
| `putUsersMePassword_userToken_returns204()` | PUT /api/users/me/password with USER JWT -> 204 | CR-C-003 |
| `putUsersMePassword_noToken_returns401()` | PUT /api/users/me/password without token -> 401 | CR-C-003 |
| helper: `buildUserDetails()` | Shared helper for creating CustomUserDetails | - |

### UserServiceTest.java (4 new tests)

**File:** `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java`

| Test Method | Assertion | CR |
|-------------|-----------|-----|
| `updatePassword_success()` | Correct current password -> password updated | CR-C-003 |
| `updatePassword_wrongCurrentPassword_throwsException()` | Wrong current password -> INVALID_CREDENTIALS | CR-C-003 |
| `updatePassword_nullPassword_throwsException()` | Social user (null password) -> INVALID_CREDENTIALS | CR-C-003 |
| `updatePassword_userNotFound_throwsException()` | Non-existent user -> RESOURCE_NOT_FOUND | CR-C-003 |

---

## Acceptance Criteria Checklist

- [x] SecurityConfig: GET/PUT/DELETE /api/users/me -> authenticated() before /api/users/* ADMIN wildcard
- [x] PUT /api/users/me/complete-profile -> authenticated() before wildcard
- [x] AuthService class: @Transactional(readOnly = true)
- [x] OAuth2Service class: @Transactional(readOnly = true)
- [x] UserService.updatePassword(): BCrypt verification, INVALID_CREDENTIALS on mismatch
- [x] UserService.updatePassword(): encode + save on success
- [x] SecurityConfig test: USER role GET/PUT /api/users/me -> 200
- [x] updatePassword() test: wrong current password case
- [x] All existing tests pass (0 failures, no regressions)

---

## Files Modified (Complete List)

| File | Action | Lines Changed |
|------|--------|---------------|
| `src/main/java/.../config/SecurityConfig.java` | Modified | +5 lines (rules) |
| `src/main/java/.../service/auth/AuthService.java` | Modified | +3 @Transactional, 1 changed |
| `src/main/java/.../service/auth/OAuth2Service.java` | Modified | +1 @Transactional, 1 changed |
| `src/main/java/.../entity/User.java` | Modified | +3 lines (method) |
| `src/main/java/.../dto/user/UpdatePasswordRequest.java` | **Created** | 18 lines |
| `src/main/java/.../service/UserService.java` | Modified | +13 lines (method) |
| `src/main/java/.../controller/UserController.java` | Modified | +8 lines (endpoint) |
| `src/test/.../controller/SecurityFilterChainTest.java` | Modified | +72 lines (7 tests + helpers) |
| `src/test/.../service/UserServiceTest.java` | Modified | +52 lines (4 tests) |

---

## Follow-up WI

- **WI-20260228-ATS-004** is unblocked by this WI completion (Depends On: WI-20260228-ATS-001).
