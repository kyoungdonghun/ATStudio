[EVIDENCE PACK]
WI ID: WI-20260308-ATS-030
REQ: REQ-20260308-ATS-010
Agent: se
Completed: 2026-03-08

## Prerequisites Verified

- `@EnableMethodSecurity` already present on `SecurityConfig.java:23` -- no config change needed.
- `SecurityConfig` URL-level rules already protect admin endpoints -- `@PreAuthorize` is defense-in-depth only.

## Change Evidence

### 1. LikeController.getMyLikes() -- message field added

**File:** `src/main/java/com/atstudio/atstudio/controller/LikeController.java`
**Line:** 38

Before:
```java
return ResponseEntity.ok(ResponseDTO.<LikeResponse>withAll()
        .dataList(likes)
        .build());
```

After:
```java
return ResponseEntity.ok(ResponseDTO.<LikeResponse>withAll()
        .message("Likes retrieved")
        .dataList(likes)
        .build());
```

### 2. UserSubscriptionController -- @PreAuthorize on 4 admin methods

**File:** `src/main/java/com/atstudio/atstudio/controller/UserSubscriptionController.java`

Import added (L11):
```java
import org.springframework.security.access.prepost.PreAuthorize;
```

| Method | Line | Annotation Added |
|--------|------|------------------|
| `listAll()` | L51 | `@PreAuthorize("hasRole('ADMIN')")` |
| `getDetail()` | L61 | `@PreAuthorize("hasRole('ADMIN')")` |
| `adminUpdate()` | L88 | `@PreAuthorize("hasRole('ADMIN')")` |
| `adminCancel()` | L102 | `@PreAuthorize("hasRole('ADMIN')")` |

### 3. CompanyCertificationController.processReview() -- @PreAuthorize added

**File:** `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java`

Import added (L14):
```java
import org.springframework.security.access.prepost.PreAuthorize;
```

Method annotation (L79):
```java
@PutMapping("/{certificationId}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ResponseDTO<CompanyCertificationResponse>> processReview(...)
```

## Test Results

```
BUILD SUCCESSFUL in 38s
Tests: 567 | Failures: 0 | Skipped: 0 | Duration: 35.227s
```

## Acceptance Criteria Checklist

- [x] LikeController.getMyLikes() -- `.message("Likes retrieved")` added
- [x] UserSubscriptionController.listAll() -- `@PreAuthorize("hasRole('ADMIN')")` added
- [x] UserSubscriptionController.getDetail() -- `@PreAuthorize("hasRole('ADMIN')")` added
- [x] UserSubscriptionController.adminUpdate() -- `@PreAuthorize("hasRole('ADMIN')")` added
- [x] UserSubscriptionController.adminCancel() -- `@PreAuthorize("hasRole('ADMIN')")` added
- [x] CompanyCertificationController.processReview() -- `@PreAuthorize("hasRole('ADMIN')")` added
- [x] gradlew.bat test -- 567 tests, 0 failures

## Scope Compliance

- No service/repository logic changed
- No other domain files modified
- No DTO fields changed
- SecurityConfig URL-level rules untouched (defense-in-depth preserved)
