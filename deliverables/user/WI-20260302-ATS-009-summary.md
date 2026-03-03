[WI-009 SUMMARY]
WI ID: WI-20260302-ATS-009
Status: COMPLETED
Completed: 2026-03-03

## Changes

**M-2 Fix — RESOURCE_DUPLICATE HTTP 400 → 409 Conflict**
- `BUSINESS_ERROR.java:23` — HttpStatus.BAD_REQUEST → HttpStatus.CONFLICT

**INVALID_STATE_TRANSITION 추가**
- `BUSINESS_ERROR.java:32-35` — 신규 enum 상수 추가 (HTTP 400)

**M-11 Fix — CompanyCertificationService.listAll() valueOf try-catch**
- `CompanyCertificationService.java:89-94` — IllegalArgumentException catch → INVALID_ARGUMENT(400) 반환

## Issues Fixed
- M-2: 중복 리소스 생성 시 HTTP 400 → 409 Conflict (api-spec 정합성)
- M-11: 유효하지 않은 status 문자열 입력 시 HTTP 500 → 400 반환
- INVALID_STATE_TRANSITION 에러 코드 추가 (WI-012 상태기계 검증에서 사용)

## Test Results
BUILD SUCCESSFUL, 534 tests, 0 failures
