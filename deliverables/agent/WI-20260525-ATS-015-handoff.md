[WI HEADER]
WI ID: WI-20260525-ATS-015
REQ: REQ-20260525-ATS-006
Agent: se
Depends On: WI-20260525-ATS-014
Blocks: WI-20260525-ATS-016

[WI SUMMARY]
Why: 환불, 권한 보정, 영수증, 감사로그가 backend admin API로 구현되어 있으나 `/admin/payments` 화면에서는 사용할 수 없어 운영자가 API를 직접 호출해야 한다.
Scope (in/out): In은 frontend admin API client 확장과 `/admin/payments` 탭/테이블/운영 mutation controls 구현이다. Out은 backend 정책 변경, settlement/tax invoice/cash receipt/webhook/multi-PG 구현이다.
DoD: 관리자 화면에서 receipt/audit/refund/entitlement correction 조회와 환불/권한보정 preview/request/approve/execute를 수행할 수 있다.
Constraints/Forbidden: raw secret/card/provider payload 노출 금지. 환불과 권한 보정을 하나의 자동 버튼으로 합치지 않는다. provider refund와 local entitlement correction의 분리 정책을 화면 문구로 유지한다.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `/admin/payments`에 `영수증`, `감사로그`, `환불`, `권한 보정` 탭이 추가된다.
- [ ] 환불 탭에서 subscription payment ID 입력 또는 결제내역 기반 ID로 preview 후 request를 만들 수 있다.
- [ ] 환불 request는 승인과 실행을 별도 버튼으로 수행한다.
- [ ] 권한 보정 탭에서 succeeded refund ID와 target state를 입력해 preview/request를 만들 수 있다.
- [ ] 권한 보정 request는 승인과 실행을 별도 버튼으로 수행한다.
Quality:
- [ ] `npm run typecheck` 통과.
- [ ] `npm run lint` 통과.
- [ ] `npm run build` 통과.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md

Tier 2 (Tech Stack):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260525-ATS-006.md
- docs/design/api-spec.md
- docs/design/payment-refund-receipt-settlement-policy.md
- docs/design/payment-operations-runbook.md
- docs/SR/SR-93.md
- docs/ui/atstudio-front-list.md

Files:
- frontend/src/api/admin.ts
- frontend/src/pages/admin/PaymentReadOnlyPage.tsx
- frontend/src/pages/admin/PaymentReadOnlyPage.module.css

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260525-ATS-015-summary.md :
- 구현 요약, 사용 흐름, 남은 리스크
Agent-facing -> deliverables/agent/WI-20260525-ATS-015-evidence-pack.md :
- 변경 파일, 검증 명령, UI 확인 포인트, rollback
Handoff Packet -> deliverables/agent/WI-20260525-ATS-015-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: frontend API/page/CSS files, relevant docs, validation output.
Tests: `npm run typecheck`, `npm run lint`, `npm run build`, targeted backend tests.
Rollback: revert changed frontend files and documentation updates.
