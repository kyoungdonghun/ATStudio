---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: cr
category: handoff
status: in_progress
related_wi: WI-20260817-ATS-018
dependencies:
  - path: ../user/REQ-20260817-ATS-009.md
    reason: Approved release-candidate scope and execution strategy
  - path: ../user/WI-20260817-ATS-017-summary.md
    reason: Direct predecessor audit and approved commit candidate boundary
  - path: WI-20260817-ATS-017-evidence-pack.md
    reason: Verification evidence and release boundary
---

[WI HEADER]
WI ID: WI-20260817-ATS-018
REQ: REQ-20260817-ATS-009
Agent: cr
Depends On: WI-20260817-ATS-017
Blocks: -

[WI SUMMARY]
Why: Assemble the user-approved, independently audited V1 release-candidate changes as six reviewable conventional commits without changing the candidate's behavioral scope.
Scope (in/out): Stage and commit only the six approved logical groups in `RC-20260817-ATS-009-verified-baseline`; create the required WI-018 summary and evidence pack after assembly. Exclude output artifacts, unrelated untracked work, the existing-development database patch, any push, and all external or destructive actions.
DoD: Six commits exist in the approved order with reviewed staged paths/hunks; each commit has staged-name and staged-diff verification; post-assembly status, whitespace, and documentation validation are recorded; the two WI-018 deliverables identify commit IDs, category membership, residual out-of-scope work only by count/category, and `git revert` rollback.
Constraints/Forbidden: Do not amend, reset, checkout, delete files/branches/worktrees, push, inspect secrets, or run DB/provider/refund/email actions. Never stage or alter `output/client-demo-screenshots-20260716-140514.zip`, `output/ui-ux-audit/`, any untracked work outside the six groups, or `scripts/database/patches/WI-20260809-ATS-068-user-consents.sql`. Use partial/hunk staging for overlapping files. Stop on an unsafe ambiguity and report a sanitized blocker.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Create exactly these six commits in order: `feat(auth): record consent and enforce verified password sessions`; `fix(frontend): make logout confirmation explicit`; `chore(frontend): upgrade React Router to 7.18.2`; `chore(database): record guarded 43-table disposable manifest`; `docs(release): synchronize V1 current-state boundaries`; `docs(work): record REQ-20260817-ATS-009 WI evidence`.
- [ ] Review each candidate diff before staging; inspect staged names and `git diff --cached --check` before every commit, with no excluded item staged.
- [ ] Record the final commit IDs and rollback command in both required WI-018 deliverables.
Performance:
- [ ] No runtime or database lifecycle is executed by release assembly.
Quality:
- [ ] `git diff --check HEAD` passes after the sixth commit.
- [ ] Documentation validation passes after the sixth commit.
- [ ] No full test suite is rerun unless release assembly changes implementation files.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
- docs/policies/versioning-policy.md

Tier 2 (Tech Stack - Conditional on project tech_stack from workspace.json):
- .agents/skills/react-best-practices/AGENTS.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-009.md
- deliverables/user/WI-20260817-ATS-017-summary.md
- deliverables/agent/WI-20260817-ATS-017-evidence-pack.md
- docs/standards/evidence-pack-standard.md

Files:
- Candidate implementation, test, configuration, and documentation files identified in WI-017's `Unstaged Commit Candidate List`
- deliverables/agent/WI-20260817-ATS-018-handoff.md
- deliverables/user/WI-20260817-ATS-018-summary.md
- deliverables/agent/WI-20260817-ATS-018-evidence-pack.md

Repro/Logs:
- `git diff`, `git diff --cached --check`, `git status --short`, `git log --oneline -6`, `git diff --check HEAD`
- `python .agents/skills/validate-docs/scripts/validate_docs.py`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-018-summary.md :
- Commit IDs, grouped membership, validation, residual out-of-scope counts/categories, external production gates, and `git revert` rollback.
Agent-facing -> deliverables/agent/WI-20260817-ATS-018-evidence-pack.md :
- Handoff input pointers, staged review evidence, commit IDs/categories, reproducibility commands/results, residual counts/categories, risks, and `git revert` rollback.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-018-handoff.md :
- This packet for direct-successor traceability.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required for every commit's staged membership and post-assembly verification.
Tests: Do not rerun the full suite unless assembly changes implementation files; record the WI-017 validation evidence as predecessor context and record final whitespace/documentation results.
Rollback (if needed): Revert the six release-assembly commits in reverse order with `git revert <commit-id>`; do not use reset or checkout.
