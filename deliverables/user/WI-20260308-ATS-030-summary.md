[WI SUMMARY]
WI ID: WI-20260308-ATS-030
REQ: REQ-20260308-ATS-010
Status: Completed

## Changes

| # | File | Line | Description |
|---|------|------|-------------|
| 1 | `LikeController.java` | L38 | Added `.message("Likes retrieved")` to `getMyLikes()` response |
| 2 | `UserSubscriptionController.java` | L11,L51 | Added `@PreAuthorize("hasRole('ADMIN')")` to `listAll()` |
| 3 | `UserSubscriptionController.java` | L61 | Added `@PreAuthorize("hasRole('ADMIN')")` to `getDetail()` |
| 4 | `UserSubscriptionController.java` | L88 | Added `@PreAuthorize("hasRole('ADMIN')")` to `adminUpdate()` |
| 5 | `UserSubscriptionController.java` | L102 | Added `@PreAuthorize("hasRole('ADMIN')")` to `adminCancel()` |
| 6 | `CompanyCertificationController.java` | L14,L79 | Added `@PreAuthorize("hasRole('ADMIN')")` to `processReview()` |

## Risk

- LOW: All changes are additive annotations; no logic modified.
- `@PreAuthorize` adds defense-in-depth on top of existing `SecurityConfig` URL-level rules.
- `@EnableMethodSecurity` was already present in `SecurityConfig.java` (L23).

## Test Results

- Total tests: **567**
- Failures: **0**
- Skipped: **0**
- Duration: 35.2s
- Command: `gradlew.bat test` (BUILD SUCCESSFUL)
