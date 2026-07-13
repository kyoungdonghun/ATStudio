[WI HEADER]
WI ID: WI-20260711-ATS-015
REQ: REQ-20260711-ATS-001
Agent: re
Depends On: WI-20260711-ATS-009, WI-20260711-ATS-010
Blocks: WI-20260711-ATS-016, WI-20260711-ATS-017, WI-20260711-ATS-018

[WI SUMMARY]
Why: Determine measurable test coverage and explicitly identify unsupported coverage areas.
Scope (in/out): Inspect configured Java/JS coverage tooling and run existing coverage commands only when configured.
DoD: Record available reports/metrics, absent tooling, and risk-focused test gaps.
Constraints/Forbidden: Do not add dependencies, plugins, tests, or source changes.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Java coverage capability is verified.
- [ ] Frontend coverage capability is verified.
- [ ] High-risk uncovered paths are mapped from prior findings.
Performance:
- [ ] Record coverage command elapsed time when run.
Quality:
- [ ] Absence of coverage tooling is reported as a gap, not fabricated as 0%.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
Tier 1:
- docs/policies/quality-gates.md
Tier 2:
- .agents/skills/test-coverage/SKILL.md
REQ/Context Docs:
- deliverables/user/REQ-20260711-ATS-001.md
- deliverables/agent/WI-20260711-ATS-002-evidence-pack.md
- deliverables/agent/WI-20260711-ATS-003-evidence-pack.md
- deliverables/agent/WI-20260711-ATS-004-evidence-pack.md
Files:
- build.gradle
- frontend/package.json
- src/test/
- frontend/src/

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260711-ATS-015-summary.md
Agent-facing -> deliverables/agent/WI-20260711-ATS-015-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260711-ATS-015-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers and exact commands: Required
Tests: Existing coverage commands only
Rollback: Remove only this WI's two owned outputs if explicitly requested
