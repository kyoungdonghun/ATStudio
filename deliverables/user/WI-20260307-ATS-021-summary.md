# WI-20260307-ATS-021 Summary

## Change Summary

**BD-1 CRITICAL fix**: Subscription cancellation grace period implementation.

### Problem
`findActiveByUser()` query filtered `WHERE status = ACTIVE`, so after `selfCancel()` set status to CANCELLED, the user was immediately blocked from all services (download, playlist, whitelist) even though `expiresAt` had not passed.

### Solution
Modified `findActiveByUser()` query to accept both ACTIVE and CANCELLED statuses within the `expiresAt` window:

| Before | After |
|--------|-------|
| `WHERE us.status = :status AND us.expiresAt >= :today` | `WHERE us.status IN ('ACTIVE', 'CANCELLED') AND us.expiresAt >= :today` |

The `status` parameter was removed from the method signature (3-arg -> 2-arg).

### Files Modified

| File | Change |
|------|--------|
| `UserSubscriptionRepository.java:21-25` | Query updated, `status` param removed |
| `UserSubscriptionService.java:64,101,144,237` | 4 call sites updated (2-arg) |
| `UtilService.java:44,65,110` | 3 call sites updated |
| `DownloadService.java:41` | 1 call site updated |
| `PlaylistService.java:209` | 1 call site updated |
| `WhitelistChannelService.java:42` | 1 call site updated |
| `UserSubscriptionServiceTest.java` | All mocks updated + 3 new BD-1 tests |
| `DownloadServiceTest.java` | All mocks updated |
| `UtilServiceTest.java` | All mocks updated |
| `PlaylistServiceTest.java` | All mocks updated |
| `WhitelistChannelServiceTest.java` | All mocks updated |

### Unused Import Cleanup
Removed unused `SubscriptionStatus` imports from: `DownloadService`, `UtilService`, `PlaylistService`, `WhitelistChannelService`, `UserSubscriptionService`, `DownloadServiceTest`, `UtilServiceTest`, `PlaylistServiceTest`.

### Risk
- **Low**: Query change is additive (includes CANCELLED in addition to ACTIVE).
- `subscribe()` duplicate check now also catches CANCELLED+unexpired subscriptions, preventing re-subscription while grace period is active. This is correct behavior.
- `changeSubscription()` also works with CANCELLED subscriptions within grace period.

### Test Results
- **567 tests total, 565 passed, 2 failed** (pre-existing `TrackServiceTest` failures unrelated to this WI)
- **All 5 affected test classes pass**: UserSubscriptionServiceTest, DownloadServiceTest, UtilServiceTest, PlaylistServiceTest, WhitelistChannelServiceTest
- **3 new tests added** for BD-1 grace period scenarios

### Verification
```bash
./gradlew test --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest" --tests "com.atstudio.atstudio.service.DownloadServiceTest" --tests "com.atstudio.atstudio.service.UtilServiceTest" --tests "com.atstudio.atstudio.service.PlaylistServiceTest" --tests "com.atstudio.atstudio.service.WhitelistChannelServiceTest"
```
