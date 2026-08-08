[WI HEADER]
WI ID: WI-20260808-ATS-020
REQ: REQ-20260808-ATS-004
Agent: se
Depends On: WI-20260808-ATS-019
Blocks: WI-20260808-ATS-021
[WI SUMMARY]
Why: SR-98 신규·교체 음원 썸네일의 1:1 계약과 실제 cover 미리보기를 구현한다.
Scope (in/out): 서버 이미지 디코딩·1:1 검증·기존 정규화 재사용, 프론트 안내·미리보기, 기존 비정사각형 보존·교체 필요 표시. 자동 크롭은 제외.
DoD: 신규/교체 비정사각형 거절; 2048 권장과 10MB 계약 표시; 미리보기와 실제 화면 일치; 기존 이미지 비파괴.
Constraints/Forbidden: 현재 브랜치에서만 작업한다. 무관 파일·기존 ZIP·비밀값을 수정하지 않는다. 파일 삭제, 스키마 변경, 실제 데이터 변경, 외부 결제 호출은 승인 없이 금지한다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 신규/교체 비정사각형 거절; 2048 권장과 10MB 계약 표시; 미리보기와 실제 화면 일치; 기존 이미지 비파괴.
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
- docs/SR/SR-98.md
- docs/SR/SR-68.md
- deliverables/user/REQ-20260808-ATS-004.md
Files:
- TrackService와 이미지 정규화 서비스
- frontend/src/pages/creator/TrackUploadPage.tsx
- frontend/src/pages/creator/TrackEditPage.tsx
- 관련 스타일과 테스트 파일

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-020-summary.md
Agent-facing -> deliverables/agent/WI-20260808-ATS-020-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260808-ATS-020-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, executed commands, test results, risks, rollback, and next blocked WI status are required.
