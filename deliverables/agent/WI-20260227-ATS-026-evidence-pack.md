# WI-20260227-ATS-026 Evidence Pack

## Files Created

| File | Lines | Purpose |
|------|-------|---------|
| `src/main/java/.../dto/subscription/SubscriptionResponse.java` | 28 | Subscription plan DTO |
| `src/main/java/.../dto/subscription/UserSubscriptionRequest.java` | 10 | Subscribe request DTO |
| `src/main/java/.../dto/subscription/UserSubscriptionResponse.java` | 30 | User subscription DTO |
| `src/main/java/.../dto/subscription/ChangeSubscriptionRequest.java` | 10 | Change plan request DTO |
| `src/main/java/.../dto/subscription/ChangeSubscriptionResponse.java` | 14 | Change plan response (proratedAmount) |
| `src/main/java/.../dto/subscription/AdminUpdateSubscriptionRequest.java` | 12 | Admin update request DTO |
| `src/main/java/.../service/payment/PaymentService.java` | 12 | Payment interface |
| `src/main/java/.../service/payment/MockPaymentServiceImpl.java` | 35 | Mock payment (@Primary) |
| `src/main/java/.../service/SubscriptionService.java` | 42 | 6.1, 6.2 business logic |
| `src/main/java/.../service/UserSubscriptionService.java` | 170 | 6.3-6.10 business logic |
| `src/main/java/.../controller/SubscriptionController.java` | 38 | 6.1, 6.2 endpoints |
| `src/main/java/.../controller/UserSubscriptionController.java` | 100 | 6.3-6.10 endpoints |
| `src/test/java/.../service/SubscriptionServiceTest.java` | 90 | 5 service tests |
| `src/test/java/.../service/UserSubscriptionServiceTest.java` | 340 | 22 service tests |
| `src/test/java/.../controller/SubscriptionControllerTest.java` | 70 | 4 controller tests |
| `src/test/java/.../controller/UserSubscriptionControllerTest.java` | 210 | 20 controller tests |

## Files Modified

| File | Change |
|------|--------|
| `BUSINESS_ERROR.java` | Added `SUBSCRIPTION_ALREADY_EXISTS(CONFLICT, ...)` |
| `UserSubscription.java` | Added `upgrade()`, `cancel()`, `adminUpdate()` methods |
| `SubscriptionRepository.java` | Added `findAllByIsActive()`, `findAllByUserTypeAndIsActive()` |
| `UserSubscriptionRepository.java` | Added `findAll(Pageable)` with @EntityGraph, `findById()` with @EntityGraph |
| `SecurityConfig.java` | Added user-subscription /me (AUTH) and /* (ADMIN) rules |

## Error Code Added

```
SUBSCRIPTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "...", "...")
```
Location: `BUSINESS_ERROR.java`, after `SUBSCRIPTION_NOT_FOUND`

## Build/Test Results

```
gradlew.bat build -x test: BUILD SUCCESSFUL (15s)
gradlew.bat test: BUILD SUCCESSFUL (41s)
Total tests: 465 (was 414, +51 new)
Failures: 0
```

## New Test Cases

### SubscriptionServiceTest (5 tests)
- getAll_noFilter, getAll_withFilter, getAll_blankFilter
- getSubscription_success, getSubscription_notFound

### UserSubscriptionServiceTest (22 tests)
- subscribe: individual_success, business_certified_success, business_notCertified, duplicate, planNotFound
- getMySubscription: success, notFound
- listAll: success
- getDetail: success, notFound
- changeSubscription: upgrade, downgrade, noActiveSubscription
- adminUpdate: allFields, partial, notFound
- adminCancel: success, notFound
- selfCancel: success, noActive

### SubscriptionControllerTest (4 tests)
- list_public_success, list_withFilter, detail_public_success, detail_notFound

### UserSubscriptionControllerTest (20 tests)
- subscribe: unauthenticated(401), success(201), certRequired(403), duplicate(409)
- getMySubscription: unauthenticated(401), success(200)
- listAll: unauthenticated(401), forbidden(403), success(200)
- getDetail: unauthenticated(401), forbidden(403), success(200)
- changeSubscription: unauthenticated(401), success(200)
- adminUpdate: forbidden(403), success(200)
- adminCancel: forbidden(403), success(200)
- selfCancel: unauthenticated(401), success(200)

## Rollback

```bash
git revert <commit-hash>
```
Or manually delete all created files and revert the 5 modified files.

## Follow-up WI

- WI-20260227-ATS-027 (re): Test verification pass
