[WI-012 SUMMARY]
WI ID: WI-20260302-ATS-012
Status: COMPLETED
Completed: 2026-03-03

## Changes

**M-6 Fix — CompanyCertificationRepository findTopByUserOrderByCreatedAtDesc**
- `CompanyCertificationRepository.java:14` — `findByUser(User)` → `findTopByUserOrderByCreatedAtDesc(User)`
- `CompanyCertificationService.java:75` — 호출부 메서드명 업데이트

**M-7 Fix — CompanyCertification 상태전이 검증**
- `CompanyCertification.java:46,53-64` — `process()` 내 `validateTransition()` 호출 추가
  - 허용: PENDING→APPROVED/REVISION_REQUESTED/REJECTED, REVISION_REQUESTED→PENDING
  - 차단: 그 외 모든 전이 → INVALID_STATE_TRANSITION(400)

**M-8 Fix — Question 상태전이 검증**
- `Question.java:47,51-61` — `updateStatus()` 내 `validateTransition()` 호출 추가
  - 허용: OPEN→IN_PROGRESS/CLOSED, IN_PROGRESS→RESOLVED/CLOSED, RESOLVED→CLOSED
  - 차단: CLOSED→any, OPEN→RESOLVED 등 역전이 → INVALID_STATE_TRANSITION(400)

## Issues Fixed
- M-6: 재신청 시 findByUser 비결정적 → 최신 레코드 보장
- M-7: CompanyCertification 상태기계 무검증 → 역방향 전이 차단
- M-8: Question 상태기계 무검증 → 유효하지 않은 전이 차단

## Test Results
BUILD SUCCESSFUL, 534 tests, 0 failures
