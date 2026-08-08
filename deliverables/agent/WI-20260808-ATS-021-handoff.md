[WI HEADER]
WI ID: WI-20260808-ATS-021
REQ: REQ-20260808-ATS-004
Agent: se
Depends On: WI-20260808-ATS-020, WI-20260808-ATS-017
Blocks: WI-20260808-ATS-022
[WI SUMMARY]
Why: SR-100 Usage 우선 통합 태그 탐색과 네 유형의 URL·API 계약을 구현한다.
Scope (in/out): 홈 탭 모듈, Usage 빈 상태 fallback, Instrument·Usage URL 복원, available API 네 유형, 모바일·키보드 접근성. Usage를 License로 표현하지 않음.
DoD: 네 유형 탐색 전 구간 연결; 결과 0/없음/API 실패 구분; AND 검색과 인코딩 유지; 접근성·모바일 테스트 통과.
Constraints/Forbidden: 현재 브랜치에서만 작업한다. 무관 파일·기존 ZIP·비밀값을 수정하지 않는다. 파일 삭제, 스키마 변경, 실제 데이터 변경, 외부 결제 호출은 승인 없이 금지한다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 네 유형 탐색 전 구간 연결; 결과 0/없음/API 실패 구분; AND 검색과 인코딩 유지; 접근성·모바일 테스트 통과.
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
- docs/design/usecase/sound-tag.md
- docs/SR/SR-100.md
- deliverables/user/REQ-20260808-ATS-004.md
Files:
- src/main/java/com/atstudio/atstudio/service/TagService.java
- frontend/src/pages/public/HomePage.tsx
- frontend/src/pages/public/TrackListPage.tsx
- frontend/src/api/tags.ts
- 관련 스타일과 테스트 파일

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-021-summary.md
Agent-facing -> deliverables/agent/WI-20260808-ATS-021-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260808-ATS-021-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, executed commands, test results, risks, rollback, and next blocked WI status are required.
