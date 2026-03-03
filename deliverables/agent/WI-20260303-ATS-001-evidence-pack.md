[EVIDENCE PACK -- WI-20260303-ATS-001]

## Changes
No code changes made. All requested items were already present in the codebase.

| File | Method/Line | Status |
|------|-------------|--------|
| `src/test/java/.../service/DownloadServiceTest.java` | `downloadLimited_zeroPlan_blocks()` L167-183 | Already exists |
| `src/test/java/.../service/auth/OAuth2ServiceTest.java` | `KakaoProfileNull.fetchKakaoUserInfo_nullProfile_throwsSocialAuthFailed()` L190-220 | Already exists |
| `src/test/java/.../entity/CompanyCertificationTest.java` | `approvedToInvalid()` L110-121 | Already exists (3 @ParameterizedTest cases) |
| `src/test/java/.../entity/CompanyCertificationTest.java` | `rejectedToInvalid()` L122-134 | Already exists (3 @ParameterizedTest cases) |
| `src/main/java/.../service/auth/OAuth2Service.java` | `buildTokenRequestBody()` L161-171 | Already uses UriComponentsBuilder |

## Added Test Methods
None added -- all already present:
- `DownloadServiceTest.downloadLimited_zeroPlan_blocks()`
- `OAuth2ServiceTest.KakaoProfileNull.fetchKakaoUserInfo_nullProfile_throwsSocialAuthFailed()`
- `CompanyCertificationTest.InvalidTransitions.approvedToInvalid(CompanyCertificationStatus)` x3
- `CompanyCertificationTest.InvalidTransitions.rejectedToInvalid(CompanyCertificationStatus)` x3

## Test Results
```
./gradlew test --rerun: 542 passed, 0 failed
BUILD SUCCESSFUL in 33s
```

## Verification Commands
```bash
# Full test suite
./gradlew test --rerun

# Specific test classes
./gradlew test --tests "com.atstudio.atstudio.service.DownloadServiceTest"
./gradlew test --tests "com.atstudio.atstudio.service.auth.OAuth2ServiceTest"
./gradlew test --tests "com.atstudio.atstudio.entity.CompanyCertificationTest"
```

## Observation
All 4 tasks described in the WI handoff packet were already implemented prior to this WI execution.
The OAuth2Service `buildTokenRequestBody()` method at line 161-171 already uses UriComponentsBuilder
and is shared by all 3 providers (Google L124, Kakao L138, Naver L152).
