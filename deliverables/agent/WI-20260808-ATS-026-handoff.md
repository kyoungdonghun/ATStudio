[WI HEADER]
WI ID: WI-20260808-ATS-026
REQ: REQ-20260808-ATS-004
Agent: qa-fe
Depends On: WI-20260808-ATS-022
Blocks: WI-20260808-ATS-028~030
[WI SUMMARY]
Why: 프론트 ESLint와 Prettier 형식을 독립 검증한다.
Scope (in/out): npm run lint 및 Prettier check. 전면 무관 포맷 변경 금지.
DoD: ESLint 오류 0; Prettier check 통과; 변경 범위 밖 포맷 churn 없음.
Constraints/Forbidden: 현재 브랜치에서만 작업한다. 무관 파일·기존 ZIP·비밀값을 수정하지 않는다. 파일 삭제, 스키마 변경, 실제 데이터 변경, 외부 결제 호출은 승인 없이 금지한다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] ESLint 오류 0; Prettier check 통과; 변경 범위 밖 포맷 churn 없음.
Performance:
- [ ] 컬렉션·동시성·미디어 처리 경로에서 기존 대비 비정상적인 N+1, deadlock, 반복 디코딩을 만들지 않는다.
Quality:
- [ ] 대상 테스트와 관련 정적 검사가 통과한다.
- [ ] 변경 파일과 재현·롤백 근거를 남긴다.

[INPUT POINTERS]
Tier 0/1/2 and context:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/frontend-standards.md
- deliverables/user/REQ-20260808-ATS-004.md
Files:
- frontend/src
- frontend ESLint/Prettier 설정

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-026-summary.md
Agent-facing -> deliverables/agent/WI-20260808-ATS-026-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260808-ATS-026-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, executed commands, test results, risks, rollback, and next blocked WI status are required.
