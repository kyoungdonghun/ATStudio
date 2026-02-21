# WI-20260221-ATS-014 Summary

## Overview
WI-013 code review findings: 4 targeted fixes for security, DTO standard compliance, and input validation.

## Changes

| # | Issue | File | Description |
|---|-------|------|-------------|
| 1 | C-01 | TagController.java:25 | Added `@PreAuthorize("hasRole('ADMIN')")` to `createTag()` -- previously unprotected write endpoint |
| 2 | M-06 | NoticeController.java:27,46,56 | Wrapped `createNotice()`, `getNotice()`, `updateNotice()` return types with `ResponseDTO<NoticeResponse>` to match project DTO standard |
| 3 | m-07 | PlayHistoryService.java:51 | Added `Math.max(1, size)` guard to prevent zero/negative page size in `getMyHistory()` |
| 4 | m-08 | NoticeService.java:49 | Added `Math.max(1, size)` guard to prevent zero/negative page size in `getNotices()` |

## Risk
- **Low.** All changes are additive guards or standard-compliance wrappers.
- No business logic altered. No new imports required (all already present).
- Existing tests check HTTP status codes only -- ResponseDTO wrapper change does not break them.

## Verification
- Compilation check: Delegated to WI-017 (qa).
- Test regression: Delegated to WI-017 (qa).
