[WI HEADER]
WI ID: WI-20260808-ATS-028
REQ: REQ-20260808-ATS-004
Agent: cr
Depends On: WI-20260808-ATS-023~027
Blocks: WI-20260808-ATS-030
[WI SUMMARY]
Why: 관리자 역할·구독 권한 보정 변경을 보안·동시성·감사 관점에서 독립 리뷰한다.
Scope (in/out): SR-96/97, 잠금 순서, 권한 재검사, 세션, 감사·PII, 외부 결제 비실행 경계. 수정은 하지 않고 findings 우선 보고.
DoD: BLOCKER/MAJOR/MINOR 분류; 파일·라인 근거; 누락 테스트와 롤백 위험 기록.
Constraints/Forbidden: 현재 브랜치에서만 작업한다. 무관 파일·기존 ZIP·비밀값을 수정하지 않는다. 파일 삭제, 스키마 변경, 실제 데이터 변경, 외부 결제 호출은 승인 없이 금지한다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] BLOCKER/MAJOR/MINOR 분류; 파일·라인 근거; 누락 테스트와 롤백 위험 기록.
Performance:
- [ ] 컬렉션·동시성·미디어 처리 경로에서 기존 대비 비정상적인 N+1, deadlock, 반복 디코딩을 만들지 않는다.
Quality:
- [ ] 대상 테스트와 관련 정적 검사가 통과한다.
- [ ] 변경 파일과 재현·롤백 근거를 남긴다.

[INPUT POINTERS]
Tier 0/1/2 and context:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/SR/SR-96.md
- docs/SR/SR-97.md
- deliverables/user/REQ-20260808-ATS-004.md
Files:
- 관리자 역할·구독 관련 변경 diff와 테스트

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-028-summary.md
Agent-facing -> deliverables/agent/WI-20260808-ATS-028-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260808-ATS-028-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, executed commands, test results, risks, rollback, and next blocked WI status are required.
