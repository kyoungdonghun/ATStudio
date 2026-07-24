[WI HEADER]
WI ID: WI-20260724-ATS-005
REQ: REQ-20260724-ATS-001
Agent: qa-fe
Depends On: WI-20260724-ATS-001, WI-20260724-ATS-002, WI-20260724-ATS-003
Blocks: WI-20260724-ATS-007

[WI SUMMARY]
Why: Prove that residual script cleanup did not regress the React SPA.
Scope (in/out): Read-only frontend verification plus evidence documents. No product fixes.
DoD: Tests, coverage, typecheck, ESLint, Prettier, and build pass.
Constraints/Forbidden: No dependency upgrades or source edits beyond evidence deliverables.

[ACCEPTANCE CRITERIA]
Quality:
- [ ] Frontend tests and coverage pass.
- [ ] Typecheck, ESLint, Prettier, and build pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 2:
- .agents/skills/react-best-practices/AGENTS.md
REQ/Context:
- deliverables/user/REQ-20260724-ATS-001.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260724-ATS-005-summary.md
Agent-facing -> deliverables/agent/WI-20260724-ATS-005-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
Record exact commands, test counts, coverage, bundle result, and PASS/FAIL.
