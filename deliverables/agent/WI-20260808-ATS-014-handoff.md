[WI HEADER]
WI ID: WI-20260808-ATS-014
REQ: REQ-20260808-ATS-004
Agent: pg
Depends On: -
Blocks: WI-20260808-ATS-015
[WI SUMMARY]
Why: SR-96 관리자 역할 변경 불변조건과 세션·감사 동기화를 구현한다.
Scope (in/out): 자기 강등·마지막 관리자·동시 강등 방지, 공유 guard, Refresh Token 제거, 프론트 재동기화, 감사와 테스트. 공개 복구 API와 QA bootstrap 활성화는 제외.
DoD: 동시 교차 강등에서 최대 한 요청만 성공하고 관리자 1명 이상 유지; 자기 강등 거절; 화면 역할 재동기화; 최소 감사정보와 테스트.
Constraints/Forbidden: 현재 브랜치에서만 작업한다. 무관 파일·기존 ZIP·비밀값을 수정하지 않는다. 파일 삭제, 스키마 변경, 실제 데이터 변경, 외부 결제 호출은 승인 없이 금지한다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 동시 교차 강등에서 최대 한 요청만 성공하고 관리자 1명 이상 유지; 자기 강등 거절; 화면 역할 재동기화; 최소 감사정보와 테스트.
Performance:
- [ ] 컬렉션·동시성·미디어 처리 경로에서 기존 대비 비정상적인 N+1, deadlock, 반복 디코딩을 만들지 않는다.
Quality:
- [ ] 대상 테스트와 관련 정적 검사가 통과한다.
- [ ] 변경 파일과 재현·롤백 근거를 남긴다.

[INPUT POINTERS]
Tier 0/1/2 and context:
- docs/standards/core-principles.md
- docs/policies/security-policy.md
- docs/standards/development-standards.md
- docs/policies/access-control-policy.md
- docs/SR/SR-96.md
- deliverables/user/REQ-20260808-ATS-004.md
Files:
- src/main/java/com/atstudio/atstudio/service/UserService.java
- src/main/java/com/atstudio/atstudio/repository/UserRepository.java
- frontend/src/pages/admin/UserManagePage.tsx
- 관련 DTO, 예외, 감사, 테스트 파일

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-014-summary.md
Agent-facing -> deliverables/agent/WI-20260808-ATS-014-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260808-ATS-014-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, executed commands, test results, risks, rollback, and next blocked WI status are required.
