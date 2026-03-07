# WI-20260307-ATS-006 Evidence Pack

## Patch Rationale

### M-1: UserSubscriptionService isUpgrade condition
- **File**: `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java`
- **Line**: 151-152
- **Before**: `compareTo(...) >= 0` -- same-price treated as UPGRADE (immediate apply + prorated payment)
- **After**: `compareTo(...) > 0` -- same-price treated as DOWNGRADE (pending reservation, no payment)
- **Why**: Same-price plan change should not trigger immediate billing and subscription reset

### M-2: UserSubscription.upgrade() pending clear
- **File**: `src/main/java/com/atstudio/atstudio/entity/UserSubscription.java`
- **Line**: 59-66
- **Before**: `upgrade()` set subscription/billingCycle/startedAt/expiresAt only
- **After**: Added `this.pendingSubscription = null; this.pendingBillingCycle = null;`
- **Why**: If user had a pending downgrade, then upgrades, stale pending data would remain and could be incorrectly applied at expiry

### M-3: UtilService isUpgrade condition
- **File**: `src/main/java/com/atstudio/atstudio/service/UtilService.java`
- **Line**: 119
- **Before**: `compareTo(currentPriceMonthly) >= 0`
- **After**: `compareTo(currentPriceMonthly) > 0`
- **Why**: Preview endpoint must mirror actual change logic (M-1)

### M-4: UtilServiceTest verify addition
- **File**: `src/test/java/com/atstudio/atstudio/service/UtilServiceTest.java`
- **Line**: 272 (after assertThatThrownBy block)
- **Added**: `verify(userRepository, never()).findById(any());`
- **Added imports**: `import static org.mockito.Mockito.never;` and `import static org.mockito.Mockito.verify;`
- **Why**: Ensures early-return on invalid billingCycle -- no DB call should be made

## Verification

```
gradlew.bat test
BUILD SUCCESSFUL
560 tests, 0 failures
```

## Follow-up WI

None identified. All four MAJOR items resolved.
