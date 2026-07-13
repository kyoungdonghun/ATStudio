[WI HEADER]
WI ID: WI-20260711-ATS-014
REQ: REQ-20260711-ATS-001
Agent: docops
Depends On: WI-20260711-ATS-001
Blocks: WI-20260711-ATS-019

[WI SUMMARY]
Why: Verify documentation integrity and audit worktree hygiene.
Scope (in/out): Run the configured documentation validator and `git diff --check`; inspect index drift without fixing it.
DoD: Record exact commands, outcomes, validator coverage limitations, and whitespace errors.
Constraints/Forbidden: No documentation/source corrections.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Docs validator runs.
- [ ] Git diff check runs.
Performance:
- [ ] Not applicable.
Quality:
- [ ] Passing validation is not overstated beyond validator coverage.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/quality-gates.md
Tier 2:
- .agents/skills/validate-docs/SKILL.md
- .agents/skills/sync-docs-index/SKILL.md
REQ/Context Docs:
- deliverables/user/REQ-20260711-ATS-001.md
- deliverables/agent/WI-20260711-ATS-001-evidence-pack.md
Files:
- docs/
- scripts/validate_docs.py

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260711-ATS-014-summary.md
Agent-facing -> deliverables/agent/WI-20260711-ATS-014-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260711-ATS-014-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers and exact commands: Required
Tests: Docs validation and diff check
Rollback: Remove only this WI's two owned outputs if explicitly requested
