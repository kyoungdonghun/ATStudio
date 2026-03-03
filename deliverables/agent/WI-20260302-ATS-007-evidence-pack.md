[EVIDENCE PACK -- WI-20260302-ATS-007]

## Changes

| File | Lines | Change |
|------|-------|--------|
| `src/main/java/com/atstudio/atstudio/service/DownloadService.java` | L20 | Added `@Transactional(readOnly = true)` at class level (M-1) |
| `src/main/java/com/atstudio/atstudio/service/DownloadService.java` | L47-48 | Extracted `int downloadPerDay` variable; added `downloadPerDay != -1` guard before limit check (C-1) |
| `src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java` | L49-119 | Added 3 new test cases for unlimited/limited plan download scenarios |

## Test Results
Command: `gradlew.bat test --tests "*DownloadServiceTest"`
Result: BUILD SUCCESSFUL, 9 tests, 0 failures, 0 errors (1.739s)

### Test Cases (9 total, 3 new)
| Test | Status | Type |
|------|--------|------|
| download_success_newLicense | PASS | existing |
| download_success_existingLicense | PASS | existing |
| download_success_unlimitedPlan_zeroCount | PASS | **new** |
| download_success_unlimitedPlan_highUsage | PASS | **new** |
| download_success_limitedPlan_underLimit | PASS | **new** |
| download_fail_userNotFound | PASS | existing |
| download_fail_inactiveTrack | PASS | existing |
| download_fail_noSubscription | PASS | existing |
| download_fail_limitExceeded | PASS | existing |

## Acceptance Criteria Verification
- [x] downloadPerDay == -1 -> download succeeds (todayCount=0 and todayCount=1000 both pass)
- [x] downloadPerDay > 0 -> existing limit check maintained (todayCount=5, limit=5 still throws DOWNLOAD_LIMIT_EXCEEDED)
- [x] downloadPerDay > 0, under limit -> download succeeds (todayCount=4, limit=5 passes)
- [x] @Transactional(readOnly=true) applied at class level (L20)
- [x] Mutating method download() has @Transactional override (L31)
- [x] BUILD SUCCESSFUL, 0 failures

## Reproduction Steps
1. `gradlew.bat test --tests "*DownloadServiceTest"` to run all 9 tests
2. Verify XML report at `build/test-results/test/TEST-com.atstudio.atstudio.service.DownloadServiceTest.xml`

## Rollback
`git revert <commit-hash>` for the commit containing these changes
