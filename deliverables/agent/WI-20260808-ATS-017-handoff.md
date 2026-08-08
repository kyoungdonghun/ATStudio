[WI HEADER]
WI ID: WI-20260808-ATS-017
REQ: REQ-20260808-ATS-004
Agent: se
Depends On: WI-20260808-ATS-016
Blocks: WI-20260808-ATS-018, WI-20260808-ATS-021
[WI SUMMARY]
Why: SR-95 태그 정규화 계약과 SR-94 중복 오류 UX를 함께 구현한다.
Scope (in/out): trim·공백 collapse·NFC·allowlist·전역 유일성, # 표시 전용, 생성·수정·직접 API 검증, DB 경합 오류 변환, 모달 상태 보존.
DoD: 동일 정규화 함수; 중복 사전·서버·DB 경합 안내 일치; 실패 후 목록·모달·입력 유지; 대상 테스트 통과.
Constraints/Forbidden: 현재 브랜치에서만 작업한다. 무관 파일·기존 ZIP·비밀값을 수정하지 않는다. 파일 삭제, 스키마 변경, 실제 데이터 변경, 외부 결제 호출은 승인 없이 금지한다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 동일 정규화 함수; 중복 사전·서버·DB 경합 안내 일치; 실패 후 목록·모달·입력 유지; 대상 테스트 통과.
Performance:
- [ ] 컬렉션·동시성·미디어 처리 경로에서 기존 대비 비정상적인 N+1, deadlock, 반복 디코딩을 만들지 않는다.
Quality:
- [ ] 대상 테스트와 관련 정적 검사가 통과한다.
- [ ] 변경 파일과 재현·롤백 근거를 남긴다.

[INPUT POINTERS]
Tier 0/1/2 and context:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/design/usecase/sound-tag.md
- docs/SR/SR-94.md
- docs/SR/SR-95.md
- deliverables/user/REQ-20260808-ATS-004.md
Files:
- src/main/java/com/atstudio/atstudio/service/TagService.java
- src/main/java/com/atstudio/atstudio/common/exception
- frontend/src/pages/admin/TagManagePage.tsx
- 관련 DTO, API 오류, 테스트 파일

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-017-summary.md
Agent-facing -> deliverables/agent/WI-20260808-ATS-017-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260808-ATS-017-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, executed commands, test results, risks, rollback, and next blocked WI status are required.
