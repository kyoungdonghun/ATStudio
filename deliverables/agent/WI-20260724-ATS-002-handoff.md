[WI HEADER]
WI ID: WI-20260724-ATS-002
REQ: REQ-20260724-ATS-001
Agent: se
Depends On: -
Blocks: WI-20260724-ATS-004, WI-20260724-ATS-006

[WI SUMMARY]
Why: Remove the stale machine-specific acceptance credentials default from the demo seed tool.
Scope (in/out): Edit only demo seed scripts and focused tests/docs. Preserve seed, verify, cleanup, and dry-run behavior.
DoD: Direct Node execution cannot silently use a personal path; non-dry-run credentials are explicit; wrapper and direct CLI contracts agree.
Constraints/Forbidden: Do not read credentials content, run destructive cleanup, or modify demo data.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] No user-specific or retired runtime path remains in the demo seed source.
- [ ] Missing credentials fails safely for non-dry-run direct execution.
- [ ] Dry-run remains secret-free and usable.
Quality:
- [ ] Focused demo script checks pass.
- [ ] `git diff --check` passes for owned files.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
REQ/Context:
- deliverables/user/REQ-20260724-ATS-001.md
Files:
- scripts/demo/seed-client-demo.mjs
- scripts/demo/seed-client-demo.ps1

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260724-ATS-002-summary.md
Agent-facing -> deliverables/agent/WI-20260724-ATS-002-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
List CLI cases tested, output redaction behavior, files changed, and rollback.
