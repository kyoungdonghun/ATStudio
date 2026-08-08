[WI HEADER]
WI ID: WI-20260808-ATS-003
REQ: REQ-20260808-ATS-002
Agent: pg
Depends On: -
Blocks: WI-20260808-ATS-006
[WI SUMMARY]
Why: 관리자 역할 변경이 자기 강등 또는 마지막 관리자 상실을 허용하는지 확인하고 보안·운영 불변조건을 설계할 근거를 만든다.
Scope (in/out): 관련 프론트엔드, API, service, repository, 인증 세션, 테스트와 정책의 읽기 전용 조사 및 보고만 포함한다. SR/코드/DB는 수정하지 않는다.
DoD: 현재 허용 여부, 자기·타인·동시 강등 시나리오, 세션 반영, 감사·복구 요구, 권고 우선순위가 코드 포인터와 함께 정리된다.
Constraints/Forbidden: UI 비활성화만으로 문제를 해결했다고 보지 않는다. 경쟁 조건을 배제한 단순 count 검사만 권고하지 않는다. 확인되지 않은 런타임 동작을 단정하지 않는다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 현재 ADMIN→USER 변경이 프론트·API·service에서 허용되는지 근거를 제시한다.
- [ ] 자기 강등, 마지막 관리자 강등, 두 관리자의 동시 상호 강등을 각각 분석한다.
- [ ] 백엔드 최종 불변조건, 동시성 제어, UI 안내, 역할 세션 반영, 감사 로그, 복구 절차를 제안한다.
- [ ] 구현 시 필요한 정상·실패·경쟁 테스트 목록을 제시한다.
Performance:
- [ ] 해당 없음(읽기 전용 조사).
Quality:
- [ ] 사실, 추론, 제안을 구분한다.
- [ ] 모든 핵심 판단에 파일·라인 또는 재현 명령 포인터가 있다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0/1 (Standards and Policies - Based on Assignee/Task):
- docs/standards/development-standards.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md

Tier 2 (Task Context):
- docs/design/api-spec.md
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260808-ATS-002.md

Files:
- frontend/src/pages/admin/UserManagePage.tsx
- frontend/src/api/admin.ts
- src/main/java/com/atstudio/atstudio/controller/UserController.java
- src/main/java/com/atstudio/atstudio/service/UserService.java
- src/main/java/com/atstudio/atstudio/entity/User.java
- src/main/java/com/atstudio/atstudio/repository/UserRepository.java
- src/test/java/com/atstudio/atstudio/service/UserServiceTest.java
- src/main/java/com/atstudio/atstudio/config/security/

Repro/Logs:
- `rg -n "updateUserByAdmin|updateByAdmin|countByIsDeletedFalseAndRole|findByIdForUpdate|updateUserAdmin" frontend/src src/main/java src/test/java`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-003-summary.md :
- 현재 판정, 위험, 권고안, 남은 정책 결정
Agent-facing -> deliverables/agent/WI-20260808-ATS-003-evidence-pack.md :
- Evidence pointers, 조사 결과, 재현 명령, 위험, SR-96 필수 요구
Handoff Packet -> deliverables/agent/WI-20260808-ATS-003-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: 실행하지 않더라도 필요한 회귀·동시성 테스트를 명시
Rollback (if needed): 읽기 전용 조사 산출물 제거 방법 기록
