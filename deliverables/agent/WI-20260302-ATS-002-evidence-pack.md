# WI-20260302-ATS-002 Evidence Pack

**REQ**: REQ-20260302-ATS-011
**Agent**: se
**Date**: 2026-03-02
**Blocks**: WI-20260302-ATS-004

## Patch Rationale

### CR-B-005: URL Validation

The original `channelUrl.contains("youtube.com")` is a substring check that passes any string containing `youtube.com` anywhere, including attacker-controlled domains like `notarealsite-youtube.com` or `youtube.com.evil.io`.

The fix uses `java.net.URI.create()` to parse the URL and extract the host. The host must exactly equal `youtube.com` or end with `.youtube.com` (to allow subdomains like `www.youtube.com`, `m.youtube.com`). `IllegalArgumentException` from malformed URIs is caught and mapped to `INVALID_ARGUMENT`.

### CR-P-005: Expired RefreshToken

The original code treated `EXPIRED` the same as `VALID` (line 74: `result != TokenValidationResult.VALID && result != TokenValidationResult.EXPIRED`), allowing expired refresh tokens to generate new access tokens indefinitely.

The fix checks for `EXPIRED` first and throws `REFRESH_TOKEN_EXPIRED` (new enum constant, HTTP 401). Only `VALID` tokens proceed. The `getUserIDAllowExpired()` call is replaced with `getUserID()` since expired tokens are now rejected before reaching user lookup.

## File/Line Change Pointers

### Production Code

| File | Lines | Change |
|------|-------|--------|
| `src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java:20` | Added `import java.net.URI;` |
| `src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java:98-107` | Replaced `contains()` with URI parsing + host validation |
| `src/main/java/com/atstudio/atstudio/service/auth/AuthService.java:73-80` | Split EXPIRED/INVALID handling; EXPIRED -> throw; use `getUserID()` |
| `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java:129-132` | Added `REFRESH_TOKEN_EXPIRED` enum constant |

### Test Code

| File | Lines | Change |
|------|-------|--------|
| `src/test/java/com/atstudio/atstudio/service/WhitelistChannelServiceTest.java` | Added `fail_spoofedDomain()` - `notarealsite-youtube.com` -> INVALID_ARGUMENT |
| `src/test/java/com/atstudio/atstudio/service/WhitelistChannelServiceTest.java` | Added `fail_malformedUrl()` - malformed URL -> INVALID_ARGUMENT |
| `src/test/java/com/atstudio/atstudio/service/WhitelistChannelServiceTest.java` | Added `success_wwwSubdomain()` - `www.youtube.com` -> allowed |
| `src/test/java/com/atstudio/atstudio/service/auth/AuthServiceTest.java` | Replaced `refresh_expiredToken_stillRotates` with `refresh_expiredToken_throwsRefreshTokenExpired` |
| `src/test/java/com/atstudio/atstudio/service/auth/AuthServiceTest.java` | All `getUserIDAllowExpired` mocks -> `getUserID` |

### Pre-existing Fix (Unblock Compilation)

| File | Lines | Change |
|------|-------|--------|
| `src/test/java/com/atstudio/atstudio/controller/NoticeControllerTest.java:114` | `updateNotice(anyLong(), any())` -> `updateNotice(anyLong(), any(), any())` |
| `src/test/java/com/atstudio/atstudio/controller/NoticeControllerTest.java:143` | `deleteNotice(anyLong())` -> `deleteNotice(anyLong(), any())` |

## Code Snippets

### Before (WhitelistChannelService:97-101)
```java
private void validateChannelUrl(String channelUrl) {
    if (!channelUrl.contains("youtube.com")) {
        throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
    }
}
```

### After (WhitelistChannelService:97-107)
```java
private void validateChannelUrl(String channelUrl) {
    try {
        URI uri = URI.create(channelUrl);
        String host = uri.getHost();
        if (host == null
                || !(host.equals("youtube.com") || host.endsWith(".youtube.com"))) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
    } catch (IllegalArgumentException e) {
        throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
    }
}
```

### Before (AuthService:73-79)
```java
TokenValidationResult result = jwtTokenProvider.validateToken(requestToken);
if (result != TokenValidationResult.VALID && result != TokenValidationResult.EXPIRED) {
    throw new BusinessException(BUSINESS_ERROR.REFRESH_TOKEN_INVALID);
}
Long userID = jwtTokenProvider.getUserIDAllowExpired(requestToken);
```

### After (AuthService:73-81)
```java
TokenValidationResult result = jwtTokenProvider.validateToken(requestToken);
if (result == TokenValidationResult.EXPIRED) {
    throw new BusinessException(BUSINESS_ERROR.REFRESH_TOKEN_EXPIRED);
}
if (result != TokenValidationResult.VALID) {
    throw new BusinessException(BUSINESS_ERROR.REFRESH_TOKEN_INVALID);
}
Long userID = jwtTokenProvider.getUserID(requestToken);
```

## Test Results

- AuthServiceTest: **7/7 passed, 0 failures**
- WhitelistChannelServiceTest RegisterChannel: **7/7 passed, 0 failures**
- WhitelistChannelServiceTest UpdateChannel: **3/3 passed, 0 failures**
- WhitelistChannelServiceTest DeleteChannel: **2/2 passed, 0 failures**
- WhitelistChannelServiceTest GetMyChannels: **1/1 passed, 0 failures**
- Full suite: **0 failures** (grep `failures="[^0]"` across all XML reports returned no matches)

Note: `gradlew.bat test` reports BUILD FAILED due to Windows-specific `NoSuchFileException` on Gradle binary result file. This is a known Gradle 9.x Windows issue unrelated to test execution. All individual test XML results confirm 0 failures.

## New Test Methods Added

| Test Class | Method | Validates |
|------------|--------|-----------|
| `WhitelistChannelServiceTest$RegisterChannel` | `fail_spoofedDomain()` | `notarealsite-youtube.com` rejected |
| `WhitelistChannelServiceTest$RegisterChannel` | `fail_malformedUrl()` | Invalid URI syntax rejected |
| `WhitelistChannelServiceTest$RegisterChannel` | `success_wwwSubdomain()` | `www.youtube.com` accepted |
| `AuthServiceTest` | `refresh_expiredToken_throwsRefreshTokenExpired()` | Expired token -> REFRESH_TOKEN_EXPIRED (401) |

## Follow-up WI

- WI-20260302-ATS-004 (blocked by this WI) is now unblocked.
