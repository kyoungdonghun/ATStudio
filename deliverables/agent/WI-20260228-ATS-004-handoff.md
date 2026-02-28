[WI HEADER]
WI ID: WI-20260228-ATS-004
REQ: REQ-20260228-ATS-010
Agent: re
Depends On: WI-20260228-ATS-001, WI-20260228-ATS-002, WI-20260228-ATS-003
Blocks: WI-20260228-ATS-005, WI-20260228-ATS-006

[WI SUMMARY]
Why: Phase 1 (WI-001~003) 수정 완료 후 전체 회귀 테스트 실행.
     WI-001(SecurityConfig/Auth/User), WI-002(Question cascade), WI-003(Track/Subscription)
     세 WI가 병렬로 수정한 파일들이 서로 충돌 없이 통합되었는지 독립 검증.
Scope (in):
  - ./gradlew test 전체 실행 및 결과 보고
  - 신규 추가된 테스트 케이스 포함 전체 통과 여부 확인
  - 실패 시 실패 테스트명 + 에러 메시지 포함
Scope (out):
  - 코드 수정 (Read-only 검증만)
  - 개별 WI 범위 검토
DoD:
  - 전체 테스트 0 failures
  - 빌드 BUILD SUCCESSFUL
Constraints/Forbidden:
  - 코드 수정 절대 금지. 순수 검증만.

[ACCEPTANCE CRITERIA]
Functional:
  - [ ] ./gradlew test BUILD SUCCESSFUL
  - [ ] 0 test failures
  - [ ] 총 테스트 수 >= 470 (WI-001: +11, WI-002: +4, WI-003: +여러 건 추가됨)
Quality:
  - [ ] 실패 시 정확한 테스트명·에러 메시지 evidence-pack에 포함

[INPUT POINTERS]
Tier 0:
  - docs/standards/core-principles.md

REQ:
  - deliverables/user/REQ-20260228-ATS-010.md

Phase 1 완료 WI:
  - deliverables/user/WI-20260228-ATS-001-summary.md
  - deliverables/user/WI-20260228-ATS-002-summary.md
  - deliverables/user/WI-20260228-ATS-003-summary.md

프로젝트 루트:
  - C:\Users\jm991\Desktop\project\ATStudio

빌드 명령:
  - cd C:\Users\jm991\Desktop\project\ATStudio && gradlew.bat test

[OUTPUT CONTRACT]
User-facing → deliverables/user/WI-20260228-ATS-004-summary.md :
  - 전체 테스트 수, 결과 (PASS/FAIL), 실패 항목 요약
Agent-facing → deliverables/agent/WI-20260228-ATS-004-evidence-pack.md :
  - gradlew test 전체 출력 요약
  - 총 테스트 수, 실패 수
  - 실패 케이스 있을 경우: 파일명, 메서드명, 에러 메시지

[TRACEABILITY REQUIREMENTS]
Evidence: gradlew test 출력 결과 포함 필수
Tests: 실행 명령어 명시
Rollback: 해당 없음 (Read-only)
