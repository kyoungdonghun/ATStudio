[WI HEADER]
WI ID: WI-20260711-ATS-002
REQ: REQ-20260711-ATS-001
Agent: sa
Depends On: -
Blocks: WI-20260711-ATS-006, WI-20260711-ATS-007, WI-20260711-ATS-008

[WI SUMMARY]
Why: Audit backend architecture and domain behavior against intended product rules.
Scope (in/out): Inspect controllers, services, entities, repositories, DTOs, transactions, schedulers, exception handling, and tests. Cover payment/subscription, whitelist, company certification, users/auth, music/search, and admin operations. Do not edit production code.
DoD: Produce an architecture/domain inventory and evidence-backed defects, omissions, state-transition risks, and maintainability findings.
Constraints/Forbidden: Read-only except WI outputs. Do not execute state-changing APIs or DB operations. Do not expose secrets.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Trace controller-to-service-to-repository flows for each core domain.
- [ ] Verify transaction boundaries, idempotency assumptions, state transitions, and failure compensation.
- [ ] Identify unreachable, duplicate, legacy, or policy-ambiguous behavior.
- [ ] Tie findings to exact file/line pointers and expected impact.
Performance:
- [ ] Flag obvious N+1, unbounded query, scheduler-scan, and large-payload risks with evidence.
Quality:
- [ ] Separate confirmed bugs from design improvements and policy questions.
- [ ] Identify missing backend tests for high-risk behavior.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Domain Context):
- docs/design/
- docs/payment/
- docs/adr/
- docs/guides/

REQ/Context Docs:
- deliverables/user/REQ-20260711-ATS-001.md

Files:
- src/main/java/com/atstudio/atstudio/
- src/test/java/
- src/main/resources/application.yml

Repro/Logs:
- rg --files src/main/java src/test/java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260711-ATS-002-summary.md : concise Korean findings and risks
Agent-facing -> deliverables/agent/WI-20260711-ATS-002-evidence-pack.md : domain map, evidence, severity, test gaps, follow-up inputs
Handoff Packet -> deliverables/agent/WI-20260711-ATS-002-handoff.md : this packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required, with narrow file/line references
Tests: Static inspection now; list focused tests required later
Rollback: Only remove this WI's newly created summary/evidence files if explicitly requested
