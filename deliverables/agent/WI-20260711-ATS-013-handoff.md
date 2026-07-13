[WI HEADER]
WI ID: WI-20260711-ATS-013
REQ: REQ-20260711-ATS-001
Agent: qa
Depends On: WI-20260711-ATS-009, WI-20260711-ATS-010, WI-20260711-ATS-011
Blocks: WI-20260711-ATS-017

[WI SUMMARY]
Why: Verify production buildability of backend and frontend.
Scope (in/out): Run complete Gradle build and frontend production build; no source fixes.
DoD: Record commands, outcomes, warnings, artifacts, and elapsed times.
Constraints/Forbidden: Do not publish, deploy, or mutate external systems.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Backend build runs.
- [ ] Frontend build runs.
Performance:
- [ ] Record elapsed times and output size warnings.
Quality:
- [ ] Generated tracked-file effects are inspected.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
Tier 2:
- .agents/skills/build-check/SKILL.md
REQ/Context Docs:
- deliverables/user/REQ-20260711-ATS-001.md
Files:
- build.gradle
- frontend/package.json

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260711-ATS-013-summary.md
Agent-facing -> deliverables/agent/WI-20260711-ATS-013-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260711-ATS-013-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers and exact commands: Required
Tests: Backend and frontend builds
Rollback: Remove only this WI's two owned outputs and proven generated-file delta if explicitly requested
