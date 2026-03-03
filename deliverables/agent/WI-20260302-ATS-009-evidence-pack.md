[EVIDENCE PACK — WI-20260302-ATS-009]

## Changes

| File | Lines | Change |
|------|-------|--------|
| `BUSINESS_ERROR.java` | L23 | `HttpStatus.BAD_REQUEST` → `HttpStatus.CONFLICT` (RESOURCE_DUPLICATE) |
| `BUSINESS_ERROR.java` | L32-35 | New `INVALID_STATE_TRANSITION(HttpStatus.BAD_REQUEST, ...)` added |
| `CompanyCertificationService.java` | L89-94 | `CompanyCertificationStatus.valueOf(status)` wrapped in try-catch → `INVALID_ARGUMENT` |

## Test Results
Command: `gradlew.bat clean test`
Result: BUILD SUCCESSFUL, 0 failures

## Acceptance Criteria Verification
- [x] RESOURCE_DUPLICATE → HTTP 409 Conflict
- [x] CompanyCertificationService.listAll() — invalid status string → INVALID_ARGUMENT(400)
- [x] INVALID_STATE_TRANSITION enum constant added (HTTP 400)
- [x] BUILD SUCCESSFUL, 0 failures

## Rollback
`git revert <commit-hash>`
