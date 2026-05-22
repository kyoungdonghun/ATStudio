[WI HEADER]
WI ID: WI-20260521-ATS-009
REQ: REQ-20260521-ATS-001
Agent: qa-fe
Depends On: WI-20260521-ATS-005, WI-20260521-ATS-006
Blocks: WI-20260521-ATS-011

[WI SUMMARY]
Why: Strengthen frontend regression coverage for checkout, access blocking, failure UX, and admin read-only payment view.
Scope (in/out): In scope: Vitest, typecheck, ESLint for changed frontend scope. Out of scope: backend tests.
DoD: Frontend payment UX and admin read-only behavior are covered.
Constraints/Forbidden: Do not make tests rely on real Toss network calls.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] New subscription routes to recurring checkout.
- [ ] One-time subscription widget is not user-facing.
- [ ] Billing failure/callback paths are tested.
- [ ] Admin payment view does not expose raw secrets.
Quality:
- [ ] `npm test`, `npm run typecheck`, and `npm run lint` pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 2:
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md

REQ/Context Docs:
- deliverables/user/REQ-20260521-ATS-001.md
- docs/ui/screen-flow.md

Files:
- frontend/src/pages/**/*.test.tsx
- frontend/src/router/*.test.tsx

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260521-ATS-009-summary.md
Agent-facing -> deliverables/agent/WI-20260521-ATS-009-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260521-ATS-009-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Include commands and results
Rollback: Document test-only changes
