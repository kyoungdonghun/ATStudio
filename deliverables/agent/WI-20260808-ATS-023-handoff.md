[WI HEADER]
WI ID: WI-20260808-ATS-023
REQ: REQ-20260808-ATS-004
Agent: re
Depends On: WI-20260808-ATS-022
Blocks: WI-20260808-ATS-028~030
[WI SUMMARY]
Why: 변경된 백엔드 기능의 전체 테스트와 JaCoCo 품질 기준을 독립 검증한다.
Scope (in/out): Gradle 전체 테스트, MySQL 동시성 대상 테스트, JaCoCo. 제품 코드 수정은 재현 가능한 결함의 최소 수정만 MA에 보고 후 처리.
DoD: 실패 0; 라인/메서드/브랜치 임계값 통과; 관리자·미디어 핵심 테스트 근거 기록.
Constraints/Forbidden: 현재 브랜치에서만 작업한다. 무관 파일·기존 ZIP·비밀값을 수정하지 않는다. 파일 삭제, 스키마 변경, 실제 데이터 변경, 외부 결제 호출은 승인 없이 금지한다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 실패 0; 라인/메서드/브랜치 임계값 통과; 관리자·미디어 핵심 테스트 근거 기록.
Performance:
- [ ] 컬렉션·동시성·미디어 처리 경로에서 기존 대비 비정상적인 N+1, deadlock, 반복 디코딩을 만들지 않는다.
Quality:
- [ ] 대상 테스트와 관련 정적 검사가 통과한다.
- [ ] 변경 파일과 재현·롤백 근거를 남긴다.

[INPUT POINTERS]
Tier 0/1/2 and context:
- docs/standards/core-principles.md
- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md
- deliverables/user/REQ-20260808-ATS-004.md
- deliverables/user/REQ-20260808-ATS-004.md
Files:
- src/test/java
- build/reports/tests
- build/reports/jacoco

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-023-summary.md
Agent-facing -> deliverables/agent/WI-20260808-ATS-023-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260808-ATS-023-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, executed commands, test results, risks, rollback, and next blocked WI status are required.
