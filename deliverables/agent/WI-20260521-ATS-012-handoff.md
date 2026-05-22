[WI HEADER]
WI ID: WI-20260521-ATS-012
REQ: REQ-20260521-ATS-001
Agent: cr
Depends On: WI-20260521-ATS-011
Blocks: WI-20260521-ATS-013

[WI SUMMARY]
Why: Perform final review of payment hardening changes before commit.
Scope (in/out): In scope: code review, security review, rollback review. Out of scope: new implementation unless critical fix is required.
DoD: Findings are resolved or explicitly documented.
Constraints/Forbidden: Do not approve with known high-severity payment or secret exposure issues.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Review covers backend payment state transitions.
- [ ] Review covers frontend checkout/callback UX.
- [ ] Review covers docs and operational residual risks.
Quality:
- [ ] No high-severity unresolved issues remain.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md

REQ/Context Docs:
- deliverables/user/REQ-20260521-ATS-001.md
- deliverables/agent/WI-20260521-ATS-011-evidence-pack.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260521-ATS-012-summary.md
Agent-facing -> deliverables/agent/WI-20260521-ATS-012-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260521-ATS-012-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Reference final validation commands
Rollback: Document critical revert points
