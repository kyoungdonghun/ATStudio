# WI-20260307-ATS-001 Evidence Pack

## REQ
REQ-20260307-ATS-007

## Change Pointers

### T-1: DownloadCountResponse nextResetAt

| File | Lines | Change |
|------|-------|--------|
| `src/main/java/com/atstudio/atstudio/dto/util/DownloadCountResponse.java:10` | L10 | `LocalDateTime nextResetAt` field added to record |
| `src/main/java/com/atstudio/atstudio/service/UtilService.java:71` | L71 | `nextResetAt = LocalDate.now().plusDays(1).atStartOfDay()` |
| `src/main/java/com/atstudio/atstudio/service/UtilService.java:74,86` | L74, L86 | Both return paths include nextResetAt (no-subscription / with-subscription) |

### T-2: subscription-change-preview API

| File | Lines | Change |
|------|-------|--------|
| `src/main/java/com/atstudio/atstudio/dto/util/SubscriptionChangePreviewResponse.java` | L1-14 | New record (changeType, proratedAmount, effectiveDate, newPlanName, newBillingCycle) |
| `src/main/java/com/atstudio/atstudio/service/UtilService.java:38` | L38 | `SubscriptionRepository` dependency added |
| `src/main/java/com/atstudio/atstudio/service/UtilService.java:97-158` | L97-158 | `previewSubscriptionChange()` method |
| `src/main/java/com/atstudio/atstudio/service/UtilService.java:100-105` | L100-105 | BillingCycle valueOf try-catch (400 on invalid) |
| `src/main/java/com/atstudio/atstudio/service/UtilService.java:119` | L119 | UPGRADE/DOWNGRADE 판정: `newPriceMonthly.compareTo(currentPriceMonthly) >= 0` |
| `src/main/java/com/atstudio/atstudio/service/UtilService.java:121-147` | L121-147 | UPGRADE path: prorated calculation mirroring UserSubscriptionService:150-167 |
| `src/main/java/com/atstudio/atstudio/service/UtilService.java:148-157` | L148-157 | DOWNGRADE path: proratedAmount=0, effectiveDate=expiresAt |
| `src/main/java/com/atstudio/atstudio/controller/UtilController.java:76-84` | L76-84 | `GET /api/utils/subscription-change-preview` endpoint |

### Tests

| File | Lines | Test Case |
|------|-------|-----------|
| `src/test/java/com/atstudio/atstudio/service/UtilServiceTest.java:91` | L91 | Existing: nextResetAt assertion added to `getDownloadCount_withFiniteSubscription` |
| `src/test/java/com/atstudio/atstudio/service/UtilServiceTest.java:109` | L109 | Existing: nextResetAt assertion added to `getDownloadCount_unlimitedSubscription` |
| `src/test/java/com/atstudio/atstudio/service/UtilServiceTest.java:125` | L125 | Existing: nextResetAt assertion added to `getDownloadCount_noSubscription` |
| `src/test/java/com/atstudio/atstudio/service/UtilServiceTest.java:131-175` | New | `previewSubscriptionChange_upgrade` |
| `src/test/java/com/atstudio/atstudio/service/UtilServiceTest.java:177-213` | New | `previewSubscriptionChange_downgrade` |
| `src/test/java/com/atstudio/atstudio/service/UtilServiceTest.java:215-226` | New | `previewSubscriptionChange_noSubscription` |
| `src/test/java/com/atstudio/atstudio/service/UtilServiceTest.java:228-235` | New | `previewSubscriptionChange_invalidBillingCycle` |

## Test Execution Result

```
Command: gradlew.bat test --tests "*.UtilServiceTest"
Result: BUILD SUCCESSFUL
Tests: 11, Failures: 0, Errors: 0, Skipped: 0
Time: 2.79s
```

XML Evidence (build/test-results/test/TEST-com.atstudio.atstudio.service.UtilServiceTest.xml):
- `tests="11" skipped="0" failures="0" errors="0"`

## Constraints Verification

| Constraint | Status |
|-----------|--------|
| UserSubscriptionService 수정 금지 | PASS - 변경 없음 |
| PaymentService 호출 금지 | PASS - 호출 없음 |
| UserSubscription.upgrade()/cancel() 호출 금지 | PASS - 조회+계산만 수행 |
| @Transactional(readOnly = true) 클래스 레벨 유지 | PASS - UtilService L32 |
| 기존 UtilServiceTest 7건 전체 통과 | PASS |

## Rollback

Revert the following files to restore pre-WI state:
1. `src/main/java/com/atstudio/atstudio/dto/util/DownloadCountResponse.java` - remove nextResetAt field
2. `src/main/java/com/atstudio/atstudio/dto/util/SubscriptionChangePreviewResponse.java` - delete file
3. `src/main/java/com/atstudio/atstudio/service/UtilService.java` - remove SubscriptionRepository dep, nextResetAt, previewSubscriptionChange()
4. `src/main/java/com/atstudio/atstudio/controller/UtilController.java` - remove subscription-change-preview endpoint
5. `src/test/java/com/atstudio/atstudio/service/UtilServiceTest.java` - remove SubscriptionRepository mock, nextResetAt assertions, 4 new test methods

## Follow-up WI
- WI-20260307-ATS-003 (Blocked by this WI) - 다음 체인 작업
