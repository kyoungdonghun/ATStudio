[WI HEADER]
WI ID: WI-20260516-ATS-004
REQ: REQ-20260516-ATS-001
Agent: qa-integ
Depends On: WI-20260516-ATS-001, WI-20260516-ATS-002, WI-20260516-ATS-003
Blocks: WI-20260516-ATS-005, WI-20260516-ATS-006, WI-20260516-ATS-007

[WI SUMMARY]
Why: Ensure backend payment APIs and frontend mock checkout contract agree before final verification.
Scope (in): Validate endpoint paths, request/response DTOs, status names, error handling, and navigation expectations across backend and frontend.
Scope (out): Full E2E browser automation unless a contract mismatch requires it.
DoD: API contract mismatches are fixed or tracked before test-focused WIs begin.
Constraints/Forbidden: Do not widen scope into Toss live integration.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Frontend request DTOs match backend request DTOs.
- [ ] Frontend reads backend response wrapper shape correctly.
- [ ] Success/failure/cancel states use backend statuses consistently.
- [ ] Deprecated direct subscribe endpoint is not used by user-facing payment page.
Performance:
- [ ] No unnecessary extra plan fetches or repeated prepares after a confirmed order.
Quality:
- [ ] Integration findings include file pointers.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md

Tier 2 (Tech Stack):
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/SKILL.md

Tier 2 (Design/Context):
- docs/design/payment-integration-design.md
- docs/design/api-spec.md

REQ/Context Docs:
- deliverables/user/REQ-20260516-ATS-001.md
- deliverables/agent/WI-20260516-ATS-001-handoff.md
- deliverables/agent/WI-20260516-ATS-002-handoff.md
- deliverables/agent/WI-20260516-ATS-003-handoff.md

Files:
- src/main/java/com/atstudio/atstudio/controller
- src/main/java/com/atstudio/atstudio/dto
- frontend/src/api
- frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx

Repro/Logs:
- Contract review notes in evidence pack.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260516-ATS-004-summary.md:
- Summary, fixed mismatches, remaining risks.
Agent-facing -> deliverables/agent/WI-20260516-ATS-004-evidence-pack.md:
- Contract matrix, evidence pointers, repro notes.
Handoff Packet -> deliverables/agent/WI-20260516-ATS-004-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Note which backend/frontend tests cover the contract.
Rollback: Document affected files if contract fixes are made.
