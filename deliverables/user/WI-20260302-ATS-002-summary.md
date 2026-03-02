# WI-20260302-ATS-002 Summary

**REQ**: REQ-20260302-ATS-011
**Agent**: se
**Status**: DONE
**Date**: 2026-03-02

## Change Summary

### CR-B-005: WhitelistChannel URL Validation Hardening

**Problem**: `channelUrl.contains("youtube.com")` allowed spoofed domains like `notarealsite-youtube.com`.

**Fix**: Replaced substring check with `java.net.URI`-based domain parsing. The host is extracted and validated to be exactly `youtube.com` or a subdomain ending with `.youtube.com`. Malformed URLs throw `INVALID_ARGUMENT (400)`.

### CR-P-005: Expired RefreshToken Rejection

**Problem**: `AuthService.refresh()` accepted expired JWT refresh tokens (`TokenValidationResult.EXPIRED`) and issued new access/refresh tokens.

**Fix**: Expired refresh tokens now return `REFRESH_TOKEN_EXPIRED (401)`. A new `BUSINESS_ERROR.REFRESH_TOKEN_EXPIRED` enum constant was added. The `getUserIDAllowExpired()` call was replaced with `getUserID()` since expired tokens are now rejected before user lookup.

## Risk Assessment

- **CR-B-005**: LOW. Strictly tighter validation. Existing valid YouTube URLs (`youtube.com`, `www.youtube.com`) continue to pass.
- **CR-P-005**: MEDIUM. Users with expired refresh tokens will need to re-login. This is the correct security behavior.

## Verification Results

| Check | Result |
|-------|--------|
| AuthServiceTest (7 tests) | 0 failures |
| WhitelistChannelServiceTest RegisterChannel (7 tests) | 0 failures |
| Full test suite (all XML reports) | 0 failures across all suites |
| `notarealsite-youtube.com` rejected | Confirmed (test) |
| `https://www.youtube.com/channel/xxx` accepted | Confirmed (test) |
| Malformed URL rejected | Confirmed (test) |
| Expired RefreshToken -> 401 | Confirmed (test) |
| Valid RefreshToken -> rotation | Confirmed (test) |

## Files Changed

| File | Change |
|------|--------|
| `WhitelistChannelService.java` | URL validation: `contains()` -> `URI.create()` + host check |
| `AuthService.java` | Reject expired tokens; use `getUserID()` instead of `getUserIDAllowExpired()` |
| `BUSINESS_ERROR.java` | Added `REFRESH_TOKEN_EXPIRED` enum constant |
| `WhitelistChannelServiceTest.java` | Added 3 tests (spoofed domain, malformed URL, www subdomain) |
| `AuthServiceTest.java` | Replaced expired-token-rotates test with expired-token-rejected test; fixed mock calls |
| `NoticeControllerTest.java` | Pre-existing compilation fix (signature mismatch from prior WI) |
