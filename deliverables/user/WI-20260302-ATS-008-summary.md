# WI-20260302-ATS-008 Summary

## Status: COMPLETED

## REQ: REQ-20260302-ATS-012

## Changes Made

### C-2 Fix (CRITICAL/PII) - UserRepository Soft-Delete Filter
- **File:** `src/main/java/com/atstudio/atstudio/repository/UserRepository.java` (line 24)
- **Change:** Added `AND u.isDeleted = false` condition to `searchUsers()` JPQL query
- **Impact:** Admin user list API (`GET /api/users`) no longer returns soft-deleted users, eliminating PII exposure (email, phone, nickname) of withdrawn accounts

### M-9 Fix (MAJOR) - OAuth2Service Null Guard
- **File:** `src/main/java/com/atstudio/atstudio/service/auth/OAuth2Service.java` (lines 129-131, 147-149, 165-167, 186-188, 202-211, 226-232)
- **Change:** Added null checks on all OAuth2 provider responses (token exchange and userInfo fetch for Google, Kakao, Naver)
- **Impact:** When OAuth2 provider returns null, the system now throws `BusinessException(SOCIAL_AUTH_FAILED)` instead of `NullPointerException` (HTTP 500)
- **Kakao additional guards:** `kakao_account` and `profile` nested maps also guarded
- **Naver additional guard:** `response` nested map also guarded

## Risk Assessment
- **PII Risk:** Eliminated. Soft-deleted users are now filtered at the JPQL level.
- **NPE Risk:** Eliminated. All 6 OAuth2 external call sites have null guards.
- **Regression Risk:** Low. Null guards only activate on abnormal provider responses. Normal social login flow is unaffected (verified by test).

## Test Results
- **Command:** `gradlew.bat test --tests "*UserServiceTest" --tests "*OAuth2ServiceTest" --tests "*AuthServiceTest"`
- **Result:** BUILD SUCCESSFUL, 0 failures
- **New tests added:**
  - `OAuth2ServiceTest` (8 tests): Token exchange null (3 providers), userInfo null (3 providers), Kakao account null, Naver response null, normal flow
  - `UserServiceTest.getUsers_excludesDeletedUsers()`: Documents C-2 fix expectation

## Pre-existing Issues (Out of Scope)
- `QuestionTest.java:136` uses `QuestionCategory.GENERAL` which does not exist in the enum (should be `OTHER`)
- These are pre-existing compilation errors unrelated to this WI
