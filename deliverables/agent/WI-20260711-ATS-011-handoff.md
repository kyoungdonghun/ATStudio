[WI HEADER]
WI ID: WI-20260711-ATS-011
REQ: REQ-20260711-ATS-001
Agent: qa
Depends On: WI-20260711-ATS-002, WI-20260711-ATS-003
Blocks: WI-20260711-ATS-017

[WI SUMMARY]
Why: Verify Java and TypeScript compile-time contracts independently.
Scope (in/out): Run Java compile/type check and frontend TypeScript no-emit check; restore only a newly modified tracked tsbuildinfo after proving baseline cleanliness.
DoD: Record both commands, outcomes, warnings, and any generated-file effect.
Constraints/Forbidden: No source fixes or unrelated worktree changes.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Java compile check runs.
- [ ] TypeScript typecheck runs.
Performance:
- [ ] Record elapsed times.
Quality:
- [ ] Generated state handling is explicit and evidence-backed.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
Tier 2:
- .agents/skills/typecheck/SKILL.md
REQ/Context Docs:
- deliverables/user/REQ-20260711-ATS-001.md
Files:
- build.gradle
- frontend/package.json
- frontend/tsconfig.json

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260711-ATS-011-summary.md
Agent-facing -> deliverables/agent/WI-20260711-ATS-011-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260711-ATS-011-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers and exact commands: Required
Tests: Compile/type checks only
Rollback: Remove only this WI's outputs and proven generated-file delta if explicitly requested
