[WI HEADER]
WI ID: WI-20260516-ATS-008
REQ: REQ-20260516-ATS-001
Agent: cr
Depends On: WI-20260516-ATS-005, WI-20260516-ATS-006, WI-20260516-ATS-007
Blocks: -

[WI SUMMARY]
Why: Review the completed Mock-first payment change before final user report and commit.
Scope (in): Review backend, frontend, tests, docs, security-sensitive payment boundaries, and traceability deliverables.
Scope (out): New feature implementation.
DoD: Findings are fixed or documented, and residual risks are clear.
Constraints/Forbidden: Do not expand scope into Toss live integration.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] No subscription mutation occurs before confirm success.
- [ ] Payment order lifecycle is clear and auditable.
- [ ] Frontend uses the new payment API for user-facing purchase.
- [ ] Legacy direct subscribe behavior is intentionally bounded.
Performance:
- [ ] No obvious N+1 or repeated payment order creation risk in normal flow.
Quality:
- [ ] Backend tests, frontend tests/typecheck, and docs validation evidence exists.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Based on Assignee):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Tech Stack):
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/SKILL.md

Tier 2 (Design/Context):
- docs/design/payment-integration-design.md

REQ/Context Docs:
- deliverables/user/REQ-20260516-ATS-001.md
- deliverables/agent/WI-20260516-ATS-005-handoff.md
- deliverables/agent/WI-20260516-ATS-006-handoff.md
- deliverables/agent/WI-20260516-ATS-007-handoff.md

Files:
- src/main/java/com/atstudio/atstudio
- src/test/java/com/atstudio/atstudio
- frontend/src/api
- frontend/src/pages/subscriber
- docs/design/payment-integration-design.md
- deliverables

Repro/Logs:
- Final test and validation command summaries from WI-005, WI-006, WI-007.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260516-ATS-008-summary.md:
- Review summary, final risks, approval points.
Agent-facing -> deliverables/agent/WI-20260516-ATS-008-evidence-pack.md:
- Review findings, evidence pointers, fixes, residual risks.
Handoff Packet -> deliverables/agent/WI-20260516-ATS-008-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Confirm all prior test evidence is present.
Rollback: Document high-level rollback commit or changed file groups.
