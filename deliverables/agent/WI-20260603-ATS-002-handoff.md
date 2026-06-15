[WI HEADER]
WI ID: WI-20260603-ATS-002
REQ: REQ-20260603-ATS-001
Agent: se
Depends On: WI-20260603-ATS-001
Blocks: WI-20260603-ATS-003

[WI SUMMARY]
Why: 확장된 화이트리스트 백엔드를 사용자 채널 관리 UX와 관리자 운영 UX에 반영한다.
Scope (in/out):
- In:
  - 사용자 화이트리스트 채널 화면에서 채널명, YouTube handle, 채널 URL, 채널 ID 입력·수정.
  - 대표 채널 설정.
  - 등록 요청, 상태 표시, 상태별 삭제/해제 요청 UX.
  - 프로필 수정 화면에서 채널 관리 진입점 제공.
  - 관리자 메뉴/화면에서 화이트리스트 채널 조회, 상태 변경, CSV export.
- Out:
  - 회원가입 필수 입력.
  - YouTube 채널 소유 인증 UI.
  - 다운그레이드 시 채널 재선택 전용 UX.
DoD:
- 사용자는 채널을 저장하고 등록 요청할 수 있다.
- 관리자는 요청 채널을 확인하고 CSV로 내보낼 수 있다.
- 화면 문구는 상태별 의미를 혼동 없이 안내한다.
Constraints/Forbidden:
- 카드/결제 화면과 무관한 UI를 변경하지 않는다.
- 관리자에게 민감 정보가 과도하게 노출되지 않도록 CSV/화면 정보 범위를 REQ 기준으로 유지한다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] 사용자 화면에서 새 필드 4종이 표시된다.
- [ ] 대표 채널 설정 버튼 또는 액션이 제공된다.
- [ ] 등록 요청/삭제/해제 요청 액션이 상태별로 다르게 보인다.
- [ ] 프로필 화면에서 화이트리스트 채널 관리로 이동할 수 있다.
- [ ] 관리자 화면에서 상태 필터와 CSV export가 가능하다.
Performance:
- [ ] 대량 목록은 paging 기반으로 조회한다.
Quality:
- [ ] `npm run typecheck` passes.
- [ ] `npm run lint` passes.
- [ ] `npm run build` passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md

Tier 2 (Tech Stack / Domain):
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/design/api-spec.md
- docs/design/usecase/whitelist.md
- docs/ui/screen-flow.md

REQ/Context Docs:
- deliverables/user/REQ-20260603-ATS-001.md

Files:
- frontend/src/api/whitelistChannels.ts
- frontend/src/api/admin.ts
- frontend/src/types/index.ts
- frontend/src/pages/subscriber/WhitelistChannelPage.tsx
- frontend/src/pages/subscriber/WhitelistChannelPage.module.css
- frontend/src/pages/subscriber/ProfilePage.tsx
- frontend/src/pages/admin/
- frontend/src/layouts/AdminLayout.tsx
- frontend/src/router/index.tsx

Repro/Logs:
- `cd frontend; npm run typecheck`
- `cd frontend; npm run lint`
- `cd frontend; npm run build`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260603-ATS-002-summary.md :
- Summary, risks, approval points
Agent-facing -> deliverables/agent/WI-20260603-ATS-002-evidence-pack.md :
- Evidence pointers, patch notes, repro & tests, follow-up WI
Handoff Packet -> deliverables/agent/WI-20260603-ATS-002-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Frontend quality gates required
Rollback (if needed): Revert frontend whitelist/admin route and API changes
