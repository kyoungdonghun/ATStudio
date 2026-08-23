[WI HEADER]
WI ID: WI-20260823-ATS-010
REQ: REQ-20260823-ATS-002
Agent: se
Depends On: -
Blocks: -

[WI SUMMARY]
Why: 공개 PlayerBar의 저장 상태가 새로고침 뒤 사라지는 실제 브라우저 결함을 최소 변경으로 제거한다.
Scope (in/out): `frontend/src/store/authStore.ts`, `frontend/src/store/playerStore.ts`, 관련 테스트만 검토·수정한다. 클라이언트 acceptance worktree, 백엔드, DB, 외부 결제·메일·OAuth는 제외한다.
DoD: 비로그인 공개 재생의 곡·seek 위치가 새로고침 뒤 복원되고, 세션 정리의 사용자 전용 상태 정리는 유지된다.
Constraints/Forbidden: 원문 localStorage/cookie 값을 출력·검사하지 말 것. 추측으로 API·정책을 변경하지 말 것. 기존 더티 변경을 되돌리거나 포맷 범위를 넓히지 말 것.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 인증 상태 초기화가 공개 PlayerBar queue를 불필요하게 지우지 않도록 원인을 확인하고 수정한다.
- [ ] 현재 저장된 재생 곡과 유효한 시간 위치가 수화 후 복원된다.
- [ ] 명시적 clearQueue 동작은 기존 의도대로 저장 상태를 비운다.
Quality:
- [ ] 변경된 테스트가 결함 경로를 실패 후 통과로 보장한다.
- [ ] 관련 Vitest, `npm run typecheck`, `npm run lint`, `npm run format`, `npm run build`가 통과한다.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/quality-gates.md

Tier 2 (React / domain):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
- docs/design/usecase/sound-track.md

REQ/Context Docs:
- deliverables/user/REQ-20260823-ATS-002.md
- deliverables/user/REQ-20260823-ATS-001.md

Files:
- frontend/src/store/authStore.ts: session clearing boundary
- frontend/src/store/playerStore.ts: persisted player hydration
- frontend/src/store/playerPersistence.test.ts: hydration regression coverage
- frontend/src/store/authStore.test.ts: session boundary coverage if present

Repro/Logs:
- Actual browser: `/tracks/4` -> play -> pause near 0.7s -> reload -> PlayerBar was empty before fix.
- Public `POST /api/tracks/batch` returns playable IDs, so do not change the endpoint without proof.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260823-ATS-010-summary.md:
- 변경 요약, 실제 재현 결과, 위험과 롤백
Agent-facing -> deliverables/agent/WI-20260823-ATS-010-evidence-pack.md:
- 원인 근거, 변경 파일, 명령 결과, 브라우저 재현 절차
Handoff Packet -> deliverables/agent/WI-20260823-ATS-010-handoff.md:
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: relevant Vitest plus static quality commands
Rollback: this WI's code commit can be reverted independently
