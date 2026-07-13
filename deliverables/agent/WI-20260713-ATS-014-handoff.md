[WI HEADER]
WI ID: WI-20260713-ATS-014
REQ: REQ-20260713-ATS-001
Agent: qa
Depends On: WI-20260713-ATS-012
Blocks: WI-20260713-ATS-017

[WI SUMMARY]
Why: Verify compile and frontend static-quality gates for the merged P0 state.
Scope (in/out): Run Java compile/check plus frontend typecheck, lint, tests, and scoped Prettier for the changed frontend file. Do not format or edit files.
DoD: All commands pass or a pre-existing baseline exception is precisely identified.
Constraints/Forbidden: Read-only verification; restore only generated `frontend/tsconfig.tsbuildinfo` if the command modifies it.

[ACCEPTANCE CRITERIA]
- [ ] Java compile succeeds.
- [ ] Frontend typecheck, lint, and tests pass.
- [ ] `frontend/src/api/tracks.ts` passes Prettier check.
- [ ] Generated build metadata is not included in the deliverable diff.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
Tier 2:
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
REQ/Context Docs:
- deliverables/user/REQ-20260713-ATS-001.md
Files:
- frontend/src/api/tracks.ts
- frontend/package.json
- build.gradle

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260713-ATS-014-summary.md
Agent-facing -> deliverables/agent/WI-20260713-ATS-014-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260713-ATS-014-handoff.md

[TRACEABILITY REQUIREMENTS]
Commands, exits, generated-file cleanup, risks, and rollback: Required
