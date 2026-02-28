# WI-20260228-ATS-003 Summary

## Change Summary

### CR-A-001 [CRITICAL] Track Entity trackTags Field Missing
- **Problem:** `TrackSpecification.hasTagWithNameAndType()` calls `root.join("trackTags")` but `Track` entity had no `trackTags` field, causing `IllegalArgumentException` at runtime. Tag-based track search was completely broken.
- **Fix:** Added `@OneToMany(mappedBy = "track", fetch = FetchType.LAZY)` `trackTags` field to `Track.java`. The `mappedBy` value matches `TrackTag.track` field name.

### CR-B-001/002 [MAJOR] DELETE Subscription Endpoints Return 200 Instead of 204
- **Problem:** `DELETE /api/user-subscriptions/{id}` and `DELETE /api/user-subscriptions/me` returned `200 OK` with a response body, violating API spec (204 No Content).
- **Fix:** Changed both endpoints to return `ResponseEntity.noContent().build()` (HTTP 204, empty body).

### CR-B-003 [MAJOR] Downgrade proratedAmount.abs() Bug
- **Problem:** `changeSubscription()` applied `.abs()` to `proratedAmount` before passing to `processPayment()`. On downgrade, a negative prorated amount (refund) was converted to a positive charge.
- **Fix:** Removed `.abs()` call. Negative amounts are now passed directly to `processPayment()` for refund processing.

### CR-B-004 [MAJOR] UserType.valueOf() Unhandled IllegalArgumentException
- **Problem:** `SubscriptionService.getActiveSubscriptions()` called `UserType.valueOf(userType)` without catching `IllegalArgumentException`. Invalid values like `"WRONG_TYPE"` caused HTTP 500.
- **Fix:** Wrapped in try/catch, throwing `BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT)` which maps to HTTP 400 Bad Request.

## Risk Assessment
- **Low risk.** All changes are localized bugfixes with no schema changes.
- The `Track.trackTags` field addition is a read-only LAZY association that does not affect existing queries.
- Pre-existing compilation error in `UserControllerTest.java` (`UserResponse` class not found) is outside WI scope.

## Verification
- **WI-scoped tests:** 3 test classes, all pass (0 failures)
  - `UserSubscriptionControllerTest` -- DELETE 204 verified
  - `UserSubscriptionServiceTest` -- Downgrade negative amount verified via `argThat(signum < 0)`
  - `SubscriptionServiceTest` -- Invalid userType -> INVALID_ARGUMENT verified
- **Full build:** Blocked by pre-existing `UserControllerTest` compilation error (unrelated to this WI)
