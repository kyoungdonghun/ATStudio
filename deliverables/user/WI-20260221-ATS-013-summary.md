# WI-20260221-ATS-013 Code Review Summary

## Overview
- **Scope:** 7 Services + 7 Controllers from REQ-20260221-ATS-002 (commit f647b7f)
- **Reviewer:** cr (Code Reviewer)
- **Date:** 2026-02-21
- **Verdict:** Conditional Approval — fix Critical and Major issues before next batch

## Issue Counts

| Severity | Count | Action |
|----------|-------|--------|
| CRITICAL | 1 | Must fix before merge |
| MAJOR | 6 | Should fix before merge |
| MINOR | 8 | Fix recommended |
| SUGGESTION | 1 | Optional |
| **Total** | **16** | |

---

## Critical Issues (Must Fix)

### [C-01] TagController POST missing @PreAuthorize
- **File:** `TagController.java:24` — `createTag()` lacks `@PreAuthorize("hasRole('ADMIN')")`
- `updateTag` (line 42) and `deleteTag` (line 54) both have the annotation, but `createTag` does not
- SecurityConfig line 77 does enforce ADMIN at filter level, but this is a defense-in-depth gap
- **Risk:** If SecurityConfig is refactored, this endpoint becomes open to all authenticated users
- **Fix:** Add `@PreAuthorize("hasRole('ADMIN')")` to `createTag()` method

---

## Major Issues (Should Fix)

### [M-01] N+1 query risk — LikeService.getMyLikes()
- `LikeRepository.findAllByUser()` returns `List<Like>` with LAZY-loaded `Track`
- `LikeResponse.from()` accesses 5 Track fields (id, title, bpm, tonality, thumbnail) → N SELECT per row
- **Fix:** Add `@EntityGraph(attributePaths = "track")` to `LikeRepository.findAllByUser()`

### [M-02] N+1 query risk — DownloadQueueService.getMyQueue()
- Identical pattern to M-01 with `DownloadQueueRepository.findAllByUser()`
- **Fix:** Add `@EntityGraph(attributePaths = "track")` to `DownloadQueueRepository.findAllByUser()`

### [M-03] N+1 query risk — PlayHistoryService.getMyHistory()
- `PlayHistoryRepository.findAllByUserOrderByPlayedAtDesc()` returns paginated `PlayHistory` with LAZY Track
- `PlayHistoryListItemResponse.from()` accesses 3 Track fields per row
- **Fix:** Add `@EntityGraph(attributePaths = "track")` to the repository method

### [M-04] UserService: class-level @Transactional missing readOnly=true
- `UserService` line 20: `@Transactional` without `readOnly = true`
- Standard template (development-standards.md Section 2A.4) requires `@Transactional(readOnly = true)` at class level
- **Fix:** Change to `@Transactional(readOnly = true)` and add `@Transactional` to mutating methods (register, updateMyProfile, withdraw, completeProfile, updateUserByAdmin)

### [M-05] LikeService + DownloadQueueService: same @Transactional violation
- Both services: class-level `@Transactional` without `readOnly = true`
- **Fix:** Same as M-04

### [M-06] NoticeController returns raw NoticeResponse
- `NoticeController.java` lines 27, 42, 48: returns `ResponseEntity<NoticeResponse>` directly
- Violates dto-standards.md Section 4 — all responses must use `ResponseDTO<E>` wrapper
- **Fix:** Wrap in `ResponseDTO.<NoticeResponse>withSingleData().data(...).build()`

---

## Minor Issues

| ID | File | Line | Description |
|----|------|------|-------------|
| m-01 | TagController | 36 | `getAllTags()` returns `List<TagResponse>` instead of `ResponseDTO` wrapper |
| m-02 | PlayHistoryService, NoticeService | 60, 57 | Uses `builder()` instead of `withAll()` semantic alias |
| m-03 | UserService | 118-126 | `ResponseDTO` constructed in service layer instead of controller |
| m-04 | PlayHistoryService, NoticeService | 60-64, 57-60 | Same ResponseDTO-in-service as m-03 |
| m-05 | UtilService | 34, 55, 80 | Takes `CustomUserDetails` instead of `Long userId` parameter |
| m-06 | PlayHistoryService, LikeService, DownloadQueueService | multiple | Same CustomUserDetails coupling as m-05 |
| m-07 | PlayHistoryService | 51 | Missing `Math.max(1, size)` → `size=0` throws IllegalArgumentException |
| m-08 | NoticeService | 49 | Same size validation issue as m-07 |

---

## Suggestion

- **[S-01]** Public check endpoints (`/api/utils/check-email`, `check-phone`, `check-nickname`) allow user enumeration. Consider rate limiting for production deployment.

---

## Overall Quality Assessment

The implementation demonstrates solid adherence to ATStudio conventions:
- **Controllers are thin** — all 7 correctly delegate to services
- **Entity/DTO separation correct** — no entities leaked to controllers
- **Exception handling consistent** — all services use `BusinessException` + `BUSINESS_ERROR` enum
- **@Valid present** on request bodies where needed
- **HTTP status codes correct** — 201 Created, 204 No Content used properly
- **SecurityConfig and @PreAuthorize mostly aligned** — one gap (C-01)
- **Password handling secure** — BCrypt encode/matches used correctly

Primary concerns: N+1 query patterns (3 services, M-01/02/03) and transaction annotation inconsistency (3 services, M-04/05). Both should be resolved in a follow-up WI before the next feature batch.
