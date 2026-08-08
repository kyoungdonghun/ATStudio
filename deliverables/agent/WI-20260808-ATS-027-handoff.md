[WI HEADER]
WI ID: WI-20260808-ATS-027
REQ: REQ-20260808-ATS-004
Agent: qa
Depends On: WI-20260808-ATS-022
Blocks: WI-20260808-ATS-028~030
[WI SUMMARY]
Why: 백엔드와 프론트 프로덕션 빌드를 독립 검증한다.
Scope (in/out): gradlew build와 npm run build. 테스트 중복 실행 결과를 구분해 기록.
DoD: 두 빌드 성공; 경고와 번들 변화 기록; 생성 파일이 작업 트리를 오염시키지 않음.
Constraints/Forbidden: 현재 브랜치에서만 작업한다. 무관 파일·기존 ZIP·비밀값을 수정하지 않는다. 파일 삭제, 스키마 변경, 실제 데이터 변경, 외부 결제 호출은 승인 없이 금지한다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 두 빌드 성공; 경고와 번들 변화 기록; 생성 파일이 작업 트리를 오염시키지 않음.
Performance:
- [ ] 컬렉션·동시성·미디어 처리 경로에서 기존 대비 비정상적인 N+1, deadlock, 반복 디코딩을 만들지 않는다.
Quality:
- [ ] 대상 테스트와 관련 정적 검사가 통과한다.
- [ ] 변경 파일과 재현·롤백 근거를 남긴다.

[INPUT POINTERS]
Tier 0/1/2 and context:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/policies/quality-gates.md
- deliverables/user/REQ-20260808-ATS-004.md
Files:
- build.gradle
- frontend/package.json
- 빌드 출력

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-027-summary.md
Agent-facing -> deliverables/agent/WI-20260808-ATS-027-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260808-ATS-027-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, executed commands, test results, risks, rollback, and next blocked WI status are required.
