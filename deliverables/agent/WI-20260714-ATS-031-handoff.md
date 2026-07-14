[WI HEADER]
WI ID: WI-20260714-ATS-031
REQ: REQ-20260714-ATS-001
Agent: qa-fe
Depends On: WI-20260714-ATS-026, WI-20260714-ATS-027
Blocks: WI-20260714-ATS-032, WI-20260714-ATS-033, WI-20260714-ATS-034

[WI SUMMARY]
Why: Verify frontend lint and formatting plus repository documentation/code hygiene without rewriting unrelated files.
Scope: frontend ESLint, scoped Prettier check for changed frontend files, repository lint where configured, and `git diff --check`.
Out: Bulk formatting, auto-fixing unrelated files, or staging generated/runtime artifacts.
DoD: Checks pass or exact owned violations are narrowly fixed and rechecked.
Constraints: Check before fix. Keep Prettier scoped to changed relevant files. Never format runtime logs, evidence logs, generated build output, or unrelated user changes.

[ACCEPTANCE CRITERIA]
- [ ] Frontend lint passes.
- [ ] Scoped formatting check passes.
- [ ] Repository diff has no whitespace errors.
- [ ] Exclusions and warnings are documented.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- deliverables/agent/WI-20260714-ATS-025-evidence-pack.md
Files:
- frontend/package.json
- changed frontend files
- .agents/skills/eslint/SKILL.md
- .agents/skills/prettier/SKILL.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-031-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-031-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-031-handoff.md

[TRACEABILITY REQUIREMENTS]
Scoped file list, commands, violations/fixes, exclusions, diff-check result, rollback, and residual risk are required.
