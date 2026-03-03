[EVIDENCE PACK — WI-20260302-ATS-012]

## Changes

| File | Lines | Change |
|------|-------|--------|
| `CompanyCertificationRepository.java` | L14 | `findByUser` → `findTopByUserOrderByCreatedAtDesc` |
| `CompanyCertificationService.java` | L75 | Call site updated to `findTopByUserOrderByCreatedAtDesc(user)` |
| `CompanyCertification.java` | L5-6 | Import BusinessException, BUSINESS_ERROR added |
| `CompanyCertification.java` | L46 | `validateTransition(this.status, newStatus)` call added to `process()` |
| `CompanyCertification.java` | L53-64 | `validateTransition()` private method with switch expression |
| `Question.java` | L4-5 | Import BusinessException, BUSINESS_ERROR added |
| `Question.java` | L47 | `validateTransition(this.status, newStatus)` call added to `updateStatus()` |
| `Question.java` | L51-61 | `validateTransition()` private method with switch expression |

## Test Results
Command: `gradlew.bat clean test`
Result: BUILD SUCCESSFUL, 0 failures

## Acceptance Criteria Verification
- [x] findTopByUserOrderByCreatedAtDesc — 복수 레코드 시 최신 1건 반환
- [x] CompanyCertification.process() — APPROVED→PENDING 등 역전이 → INVALID_STATE_TRANSITION
- [x] Question.updateStatus() — CLOSED→OPEN 등 역전이 → INVALID_STATE_TRANSITION
- [x] BUILD SUCCESSFUL, 0 failures

## Rollback
`git revert <commit-hash>`
