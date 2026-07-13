[WI HEADER]
WI ID: WI-20260711-ATS-010
REQ: REQ-20260711-ATS-001
Agent: qa-fe
Depends On: WI-20260711-ATS-006, WI-20260711-ATS-007, WI-20260711-ATS-008
Blocks: WI-20260711-ATS-018

[WI SUMMARY]
Why: Establish a fresh frontend regression baseline.
Scope (in/out): Run the configured Vitest suite only; no snapshots or source files may be updated.
DoD: Record command, pass/fail counts, skipped tests, duration, and error excerpts.
Constraints/Forbidden: Do not run watch mode or modify source/generated state.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] The repository-configured non-watch frontend test command is executed.
- [ ] Failures and skipped tests are recorded exactly.
Performance:
- [ ] Record elapsed time and timeout if any.
Quality:
- [ ] User summary and evidence pack are created.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
Tier 2:
- .agents/skills/test/SKILL.md
- .agents/skills/react-best-practices/AGENTS.md
REQ/Context Docs:
- deliverables/user/REQ-20260711-ATS-001.md
Files:
- frontend/package.json
- frontend/src/

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260711-ATS-010-summary.md
Agent-facing -> deliverables/agent/WI-20260711-ATS-010-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260711-ATS-010-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers and exact command: Required
Tests: Full configured frontend suite
Rollback: Remove only this WI's two owned outputs if explicitly requested
