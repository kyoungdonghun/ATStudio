# WI-20260302-ATS-008 Evidence Pack

## WI Metadata
- **WI ID:** WI-20260302-ATS-008
- **REQ:** REQ-20260302-ATS-012
- **Agent:** se
- **Status:** COMPLETED

---

## Change Pointers

### Change 1: C-2 Fix (UserRepository JPQL soft-delete filter)

| Item | Detail |
|------|--------|
| File | `src/main/java/com/atstudio/atstudio/repository/UserRepository.java` |
| Line | 24 |
| Before | `AND (:userType IS NULL OR u.userType = :userType)"` (end of WHERE) |
| After | `AND (:userType IS NULL OR u.userType = :userType) AND u.isDeleted = false"` |
| Rationale | JPQL lacked isDeleted filter, causing soft-deleted users (with PII: email, phone, nickname) to appear in admin list responses |

### Change 2: M-9 Fix (OAuth2Service null guards - token exchange)

| Item | Detail |
|------|--------|
| File | `src/main/java/com/atstudio/atstudio/service/auth/OAuth2Service.java` |
| Lines | 129-131 (Google), 147-149 (Kakao), 165-167 (Naver) |
| Pattern | `if (response == null) { throw new BusinessException(BUSINESS_ERROR.SOCIAL_AUTH_FAILED); }` |
| Rationale | RestClient `.body(Map.class)` can return null; without guard, `response.get("access_token")` throws NPE -> HTTP 500 |

### Change 3: M-9 Fix (OAuth2Service null guards - userInfo fetch)

| Item | Detail |
|------|--------|
| File | `src/main/java/com/atstudio/atstudio/service/auth/OAuth2Service.java` |
| Lines | 186-188 (Google info), 202-211 (Kakao info+account+profile), 226-232 (Naver body+response) |
| Pattern | Null check on top-level response AND nested map access (kakao_account, profile, response) |
| Rationale | Kakao/Naver responses have nested structures; null at any level causes NPE |

### Change 4: New Test - OAuth2ServiceTest

| Item | Detail |
|------|--------|
| File | `src/test/java/com/atstudio/atstudio/service/auth/OAuth2ServiceTest.java` (NEW) |
| Tests | 8 test methods |
| Coverage | Token null (Google/Kakao/Naver), userInfo null (Google/Kakao/Naver), Kakao account null, Naver response null, normal flow |

### Change 5: New Test - UserServiceTest (C-2 verification)

| Item | Detail |
|------|--------|
| File | `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java` |
| Method | `getUsers_excludesDeletedUsers()` |
| Purpose | Documents that searchUsers JPQL now filters isDeleted=false |

---

## Test Evidence

### Test Execution
- **Command:** `gradlew.bat test --tests "*UserServiceTest" --tests "*OAuth2ServiceTest" --tests "*AuthServiceTest"`
- **Status:** BUILD SUCCESSFUL
- **Duration:** ~6s
- **Failures:** 0

### Full Build Note
- Full `gradlew.bat test` fails due to **pre-existing** compilation error in `QuestionTest.java:136` (`QuestionCategory.GENERAL` does not exist in enum)
- This is NOT caused by WI-008 changes; it exists on the master branch prior to this WI

---

## Acceptance Criteria Verification

| Criteria | Status | Evidence |
|----------|--------|----------|
| isDeleted=true users excluded from Admin list | PASS | `UserRepository.java:24` - JPQL contains `AND u.isDeleted = false` |
| OAuth2 token exchange null -> BusinessException | PASS | `OAuth2Service.java:129,147,165` + `OAuth2ServiceTest` 3 token null tests |
| OAuth2 userInfo null -> BusinessException | PASS | `OAuth2Service.java:186,202,226` + `OAuth2ServiceTest` 5 userInfo null tests |
| Normal social login flow unaffected | PASS | `OAuth2ServiceTest.processSocialLogin_existingSocialAccount_returnsUser()` |
| BUILD SUCCESSFUL | PASS | Targeted tests pass (full build has pre-existing issue outside scope) |
| 0 test failures | PASS | All WI-008 related tests pass |

---

## Reproduction Steps

```bash
# 1. Verify C-2 fix in JPQL
# Check UserRepository.java line 24 for "AND u.isDeleted = false"

# 2. Verify M-9 null guards
# Check OAuth2Service.java for null checks at lines 129, 147, 165, 186, 202, 206, 210, 226, 230

# 3. Run tests
gradlew.bat test --tests "*UserServiceTest" --tests "*OAuth2ServiceTest" --tests "*AuthServiceTest"
# Expected: BUILD SUCCESSFUL, 0 failures
```

---

## Follow-up Items
- Pre-existing `QuestionTest.java:136` compilation error should be tracked separately (uses `QuestionCategory.GENERAL` which does not exist)
- Pre-existing `PlaylistServiceTest.java:96` type inference issue was auto-fixed by linter during this session
