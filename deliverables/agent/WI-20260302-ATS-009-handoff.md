[WI HEADER]
WI ID: WI-20260302-ATS-009
REQ: REQ-20260302-ATS-012
Agent: se
Depends On: -
Blocks: WI-20260302-ATS-013

[WI SUMMARY]
Why: M-2 RESOURCE_DUPLICATE HTTP 400→409 스펙 불일치 수정 + M-11 CompanyCertificationService valueOf try-catch 누락
Scope (in):
  - BUSINESS_ERROR.java:22-25 — RESOURCE_DUPLICATE HttpStatus.BAD_REQUEST → HttpStatus.CONFLICT
  - CompanyCertificationService.java:89 — status valueOf() try-catch → INVALID_ARGUMENT(400) 처리
  - 관련 테스트 추가/수정
Scope (out): 다른 BUSINESS_ERROR 항목 수정 금지
DoD:
  - 중복 리소스 생성 시도 시 HTTP 409 반환
  - 유효하지 않은 status 문자열 입력 시 HTTP 400 반환 (기존 500 → 400)
  - BUILD SUCCESSFUL, 0 failures
Constraints/Forbidden:
  - BUSINESS_ERROR.java의 다른 항목 수정 금지
  - CompanyCertificationService.java의 다른 메서드 수정 금지

[ACCEPTANCE CRITERIA]
Functional:
- [ ] RESOURCE_DUPLICATE 에러 시 HTTP 409 Conflict 반환
- [ ] CompanyCertificationService.listAll() — 유효하지 않은 status 문자열 → INVALID_ARGUMENT(400) 반환
- [ ] 기존 중복 체크 로직(플레이리스트 트랙 중복 등) 영향 없음
Quality:
- [ ] BUILD SUCCESSFUL
- [ ] 신규 테스트 포함 전체 테스트 0 failures

[INPUT POINTERS]
Tier 0 (Constitution):
- docs/standards/core-principles.md

Tier 0 (Standards):
- docs/standards/development-standards.md

REQ:
- deliverables/user/REQ-20260302-ATS-012.md

Files:
- src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java:20-30
- src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:85-95
- src/test/java/com/atstudio/atstudio/service/CompanyCertificationServiceTest.java (존재 시)

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260302-ATS-009-summary.md
Agent-facing → deliverables/agent/WI-20260302-ATS-009-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines): 수정된 라인 명시
Tests: gradlew.bat test --tests "*CompanyCertification*" --tests "*Exception*"
Rollback: git revert
