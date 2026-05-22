[WI HEADER]
WI ID: WI-20260521-ATS-013
REQ: REQ-20260521-ATS-001
Agent: MA
Depends On: WI-20260521-ATS-012
Blocks: -

[WI SUMMARY]
Why: Stage explicit files, commit the operating hardening work, and report the final result.
Scope (in/out): In scope: final git status, explicit staging, commit, concise report. Out of scope: push unless requested.
DoD: Worktree contains only intended committed changes plus known unrelated untracked residue.
Constraints/Forbidden: Do not stage unrelated 20260420 deliverables, vite logs, or pid files unless explicitly requested.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Explicit file list staged.
- [ ] Commit created with clear Korean message.
- [ ] Final status reported.
Quality:
- [ ] Validation evidence is summarized.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md

REQ/Context Docs:
- deliverables/user/REQ-20260521-ATS-001.md
- deliverables/agent/WI-20260521-ATS-012-evidence-pack.md

Repro/Logs:
- git status --short --branch
- git diff --stat

[OUTPUT CONTRACT]
User-facing -> final response
Agent-facing -> deliverables/agent/WI-20260521-ATS-013-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260521-ATS-013-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Include final validation summary
Rollback: Commit hash and revert guidance
