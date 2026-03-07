# WI-20260307-ATS-021 Evidence Pack

## WI Info
- **WI ID**: WI-20260307-ATS-021
- **REQ**: REQ-20260307-ATS-008
- **Agent**: se
- **Issue**: BD-1 CRITICAL - Subscription cancellation grace period

## Change Pointers

### Repository (Core Fix)
- **File**: `src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java`
- **Lines**: 17-25
- **Before**: `@Query("... WHERE us.user = :user AND us.status = :status AND us.expiresAt >= :today")` with 3 params (user, status, today)
- **After**: `@Query("... WHERE us.user = :user AND us.status IN ('ACTIVE', 'CANCELLED') AND us.expiresAt >= :today")` with 2 params (user, today)
- **Import removed**: `com.atstudio.atstudio.entity.enums.SubscriptionStatus` (no longer needed)

### Service Call Sites (10 total)
| File | Line(s) | Change |
|------|---------|--------|
| `src/main/java/.../service/UserSubscriptionService.java` | 64, 101, 144, 237 | `findActiveByUser(user, LocalDate.now())` |
| `src/main/java/.../service/UtilService.java` | 44, 65, 110 | `findActiveByUser(user, LocalDate.now())` |
| `src/main/java/.../service/DownloadService.java` | 41 | `findActiveByUser(user, LocalDate.now())` |
| `src/main/java/.../service/PlaylistService.java` | 209 | `findActiveByUser(user, LocalDate.now())` |
| `src/main/java/.../service/WhitelistChannelService.java` | 42 | `findActiveByUser(user, LocalDate.now())` |

### Unused Import Cleanup
| File | Removed Import |
|------|---------------|
| `UserSubscriptionService.java` | `SubscriptionStatus` |
| `UtilService.java` | `SubscriptionStatus` |
| `DownloadService.java` | `SubscriptionStatus` |
| `PlaylistService.java` | `SubscriptionStatus` |
| `WhitelistChannelService.java` | `SubscriptionStatus` |
| `DownloadServiceTest.java` | `SubscriptionStatus` |
| `UtilServiceTest.java` | `SubscriptionStatus` |
| `PlaylistServiceTest.java` | `SubscriptionStatus` |

### Test Updates (Mock Signature Changes)
| Test File | Lines Updated | Pattern |
|-----------|--------------|---------|
| `UserSubscriptionServiceTest.java` | 68, 95, 135, 151, 178, 194, 283, 318, 348, 464, 478 | `eq(SubscriptionStatus.ACTIVE), any()` -> `any(LocalDate.class)` |
| `DownloadServiceTest.java` | 59, 83, 105, 129, 153, 175, 218, 234 | Same pattern |
| `UtilServiceTest.java` | 61, 75, 91, 110, 127, 197, 237, 256 | Same pattern |
| `PlaylistServiceTest.java` | 115, 401 | Same pattern |
| `WhitelistChannelServiceTest.java` | 63, 120, 137, 156 | Same pattern + added `LocalDate` import |

### New Tests Added
- **File**: `src/test/java/.../service/UserSubscriptionServiceTest.java`
- **Nested Class**: `CancelledGracePeriod` (BD-1 dedicated)
- **Tests**:
  1. `selfCancel_statusCancelledAndExpiresAtPreserved` - Verifies cancel() sets CANCELLED and preserves expiresAt
  2. `cancelledWithinGracePeriod_getMySubscription_returnsSubscription` - Verifies CANCELLED+unexpired returns subscription with status="CANCELLED"
  3. `cancelledPastExpiry_getMySubscription_throwsNoActive` - Verifies expired CANCELLED returns NO_ACTIVE_SUBSCRIPTION

## Test Evidence

### Test Execution
- **Command**: `./gradlew test`
- **Status**: 567 total, 565 passed, 2 failed (pre-existing TrackServiceTest failures)
- **WI-affected tests**: ALL PASS

### Targeted Test Execution
- **Command**: `./gradlew test --tests "...UserSubscriptionServiceTest" --tests "...DownloadServiceTest" --tests "...UtilServiceTest" --tests "...PlaylistServiceTest" --tests "...WhitelistChannelServiceTest"`
- **Status**: BUILD SUCCESSFUL
- **All affected tests pass**

### Pre-existing Failures (Not Related)
- `TrackServiceTest.getTracksForAdmin()` - 2 tests at lines 176, 217 (IllegalArgumentException, present before this WI)

## Acceptance Criteria Verification

- [x] `findActiveByUser()` modified: ACTIVE + CANCELLED, expiresAt >= today
- [x] `selfCancel()` after: `getMySubscription()` returns subscription (status=CANCELLED, expiresAt preserved)
- [x] CANCELLED past expiresAt: NO_ACTIVE_SUBSCRIPTION correctly thrown
- [x] All `findActiveByUser` call sites updated (10 production, 25+ test mocks)
- [x] Existing tests pass (no regression)
- [x] 3 new BD-1 grace period tests added

## Constraints Compliance
- [x] `subscribe()` logic NOT changed (only findActiveByUser call updated)
- [x] `changeSubscription()` logic NOT changed (only findActiveByUser call updated)
- [x] `cancel()` entity method NOT changed
- [x] No API response spec changes

## Reproduction Steps
1. `cd C:\Users\jm991\Desktop\project\ATStudio`
2. `./gradlew test --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest"`
3. Verify all tests including `CancelledGracePeriod` nested class pass
