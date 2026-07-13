[WI HEADER]
WI ID: WI-20260711-ATS-012
REQ: REQ-20260711-ATS-001
Agent: qa-fe
Depends On: WI-20260711-ATS-003
Blocks: WI-20260711-ATS-018

[WI SUMMARY]
Why: Measure frontend lint and formatting conformance without modifying files.
Scope (in/out): Run ESLint and Prettier check commands independently.
DoD: Record exact commands, exit codes, counts, and representative paths.
Constraints/Forbidden: No auto-fix or formatting writes.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] ESLint runs.
- [ ] Prettier check runs.
Performance:
- [ ] Record elapsed times.
Quality:
- [ ] Findings distinguish errors, warnings, and formatting drift.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
Tier 2:
- .agents/skills/eslint/SKILL.md
- .agents/skills/prettier/SKILL.md
REQ/Context Docs:
- deliverables/user/REQ-20260711-ATS-001.md
Files:
- frontend/package.json
- frontend/eslint.config.js

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260711-ATS-012-summary.md
Agent-facing -> deliverables/agent/WI-20260711-ATS-012-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260711-ATS-012-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers and exact commands: Required
Tests: ESLint and Prettier check
Rollback: Remove only this WI's two owned outputs if explicitly requested
