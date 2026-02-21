# WI-20260221-ATS-016 Evidence Pack

## WI Metadata
- **WI ID:** WI-20260221-ATS-016
- **REQ:** REQ-20260221-ATS-003
- **Agent:** se
- **Status:** DONE
- **Blocks:** WI-20260221-ATS-017

## Patch Rationale

Code review finding [M-04] and [M-05] identified 3 Service classes using bare `@Transactional` at class level, missing the standard `readOnly=true` default. This creates unnecessary read-write transactions for query-only methods, forgoing Hibernate flush-mode optimization. The fix normalizes all 3 services to match the established pattern in TagService.java and NoticeService.java.

## Change Pointers

### File 1: UserService.java
**Path:** `src/main/java/com/atstudio/atstudio/service/UserService.java`

| Location | Before | After |
|----------|--------|-------|
| Line 20 (class-level) | `@Transactional` | `@Transactional(readOnly = true)` |
| Line 27 (register) | _(none)_ | `@Transactional` added |
| Line 56 (updateMyProfile) | _(none)_ | `@Transactional` added |
| Line 72 (withdraw) | _(none)_ | `@Transactional` added |
| Line 85 (completeProfile) | _(none)_ | `@Transactional` added |
| Line 133 (updateUserByAdmin) | _(none)_ | `@Transactional` added |
| Line 50 (getMyProfile) | `@Transactional(readOnly = true)` | _(removed, inherits class-level)_ |
| Line 105 (isEmailAvailable) | `@Transactional(readOnly = true)` | _(removed, inherits class-level)_ |
| Line 109 (isPhoneAvailable) | `@Transactional(readOnly = true)` | _(removed, inherits class-level)_ |
| Line 113 (isNicknameAvailable) | `@Transactional(readOnly = true)` | _(removed, inherits class-level)_ |
| Line 117 (getUsers) | `@Transactional(readOnly = true)` | _(removed, inherits class-level)_ |
| Line 127 (getUser) | `@Transactional(readOnly = true)` | _(removed, inherits class-level)_ |

### File 2: LikeService.java
**Path:** `src/main/java/com/atstudio/atstudio/service/LikeService.java`

| Location | Before | After |
|----------|--------|-------|
| Line 21 (class-level) | `@Transactional` | `@Transactional(readOnly = true)` |
| Line 29 (addLike) | _(none)_ | `@Transactional` added |
| Line 57 (removeLike) | _(none)_ | `@Transactional` added |
| Line 50 (getMyLikes) | `@Transactional(readOnly = true)` | _(removed, inherits class-level)_ |

### File 3: DownloadQueueService.java
**Path:** `src/main/java/com/atstudio/atstudio/service/DownloadQueueService.java`

| Location | Before | After |
|----------|--------|-------|
| Line 21 (class-level) | `@Transactional` | `@Transactional(readOnly = true)` |
| Line 29 (addToQueue) | _(none)_ | `@Transactional` added |
| Line 57 (removeFromQueue) | _(none)_ | `@Transactional` added |
| Line 50 (getMyQueue) | `@Transactional(readOnly = true)` | _(removed, inherits class-level)_ |

## Reference Pattern Confirmation

Verified against `TagService.java` (lines 17-20):
```java
@Service
@Transactional(readOnly = true)   // class-level default
@RequiredArgsConstructor
public class TagService {
    @Transactional                 // mutating method override
    public TagResponse createTag(...) { ... }

    public List<TagResponse> getAllTags(...) { ... }  // inherits readOnly=true
}
```

All 3 fixed services now follow this identical pattern.

## Scope Compliance

- [x] Only @Transactional annotations changed
- [x] No business logic modified
- [x] No method signatures changed
- [x] No test files modified
- [x] No files outside the 3 target services modified

## Acceptance Criteria Checklist

- [x] UserService class: `@Transactional(readOnly = true)`
- [x] UserService.register(): `@Transactional`
- [x] UserService.updateMyProfile(): `@Transactional`
- [x] UserService.withdraw(): `@Transactional`
- [x] UserService.completeProfile(): `@Transactional`
- [x] UserService.updateUserByAdmin(): `@Transactional`
- [x] LikeService class: `@Transactional(readOnly = true)`
- [x] LikeService.addLike(): `@Transactional`
- [x] LikeService.removeLike(): `@Transactional`
- [x] DownloadQueueService class: `@Transactional(readOnly = true)`
- [x] DownloadQueueService.addToQueue(): `@Transactional`
- [x] DownloadQueueService.removeFromQueue(): `@Transactional`

## Verification

- **Compilation:** Not verified (Bash restricted). Annotation-only change, zero risk of syntax error.
- **Tests:** Deferred to WI-20260221-ATS-017 (qa agent)
- **Rollback:** `git checkout -- src/main/java/com/atstudio/atstudio/service/{UserService,LikeService,DownloadQueueService}.java`

## Follow-up WI

- **WI-20260221-ATS-017:** qa agent to run full test suite and verify no regressions
