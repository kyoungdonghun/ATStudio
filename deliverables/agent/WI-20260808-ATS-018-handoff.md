[WI HEADER]
WI ID: WI-20260808-ATS-018
REQ: REQ-20260808-ATS-004
Agent: se
Depends On: WI-20260808-ATS-017
Blocks: WI-20260808-ATS-019
[WI SUMMARY]
Why: SR-101의 짧은 정상 버퍼링 오경고를 제거하고 실제 오류와 분리한다.
Scope (in/out): 2초 pending timer, 복구 이벤트 취소, 재생 generation/token race 방지, 지속 버퍼링·실제 오류 분리. PlayableTrack 데이터 보강은 다음 WI.
DoD: 1.8초 복구 시 문구 없음; 2초 이상 지연 시 안내; Track 전환 race 없음; fake timer 테스트 통과.
Constraints/Forbidden: 현재 브랜치에서만 작업한다. 무관 파일·기존 ZIP·비밀값을 수정하지 않는다. 파일 삭제, 스키마 변경, 실제 데이터 변경, 외부 결제 호출은 승인 없이 금지한다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 1.8초 복구 시 문구 없음; 2초 이상 지연 시 안내; Track 전환 race 없음; fake timer 테스트 통과.
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
- docs/SR/SR-101.md
- deliverables/user/REQ-20260808-ATS-004.md
Files:
- frontend/src/store/playerStore.ts
- frontend/src/layouts/PlayerBar.tsx
- 관련 플레이어 테스트 파일

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260808-ATS-018-summary.md
Agent-facing -> deliverables/agent/WI-20260808-ATS-018-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260808-ATS-018-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, executed commands, test results, risks, rollback, and next blocked WI status are required.
