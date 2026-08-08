[WI HEADER]
WI ID: WI-20260808-ATS-019
REQ: REQ-20260808-ATS-004
Agent: se
Depends On: WI-20260808-ATS-018, WI-20260808-ATS-016
Blocks: WI-20260808-ATS-020
[WI SUMMARY]
Why: SR-101의 공통 PlayableTrack 계약을 집계 API·프론트 mapper·대기열·저장 상태에 적용한다.
Scope (in/out): duration+waveform projection, 앨범·재생목록·좋아요·다운로드·Drawer·history·queue 보강, batch hydration, N+1 방지. SR-90 디자인은 제외.
DoD: 모든 진입점 real peak 전달; null/0 임의 생성 제거; 기존 저장 상태 hydration; 요청·DB 쿼리 N+1 없음.
Constraints/Forbidden: 현재 브랜치에서만 작업한다. 무관 파일·기존 ZIP·비밀값을 수정하지 않는다. 파일 삭제, 스키마 변경, 실제 데이터 변경, 외부 결제 호출은 승인 없이 금지한다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 모든 진입점 real peak 전달; null/0 임의 생성 제거; 기존 저장 상태 hydration; 요청·DB 쿼리 N+1 없음.
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
- docs/design/usecase/sound-track.md
- docs/SR/SR-101.md
- deliverables/user/REQ-20260808-ATS-004.md
Files:
- 집계 응답 DTO와 서비스
- frontend/src/pages/public/AlbumDetailPage.tsx
- frontend/src/pages/subscriber/*
- frontend/src/components/player/*
- frontend/src/store/playerStore.ts 및 관련 테스트

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-019-summary.md
Agent-facing -> deliverables/agent/WI-20260808-ATS-019-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260808-ATS-019-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, executed commands, test results, risks, rollback, and next blocked WI status are required.
