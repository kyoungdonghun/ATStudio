---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: MA
category: handoff
status: approved
dependencies:
  - path: ../user/REQ-20261010-ATS-007.md
    reason: Approved host-hardening continuation
  - path: WI-20261010-ATS-019-evidence-pack.md
    reason: Completed SE preparation
---

# WI-20261010-ATS-020 Handoff

[WI HEADER]
WI ID: WI-20261010-ATS-020
REQ: REQ-20261010-ATS-007
Agent: pg
Depends On: WI-20261010-ATS-019
Blocks: WI-20261010-ATS-021

[WI SUMMARY]
Why: Independently review the prepared host changes before MA applies them.
Scope: Review WI019 exact candidates, layout checker and staged runbook. Test bounded local failure paths. Write only this WI's summary and evidence pack.
DoD: Findings by severity, per-stage apply/HOLD verdict, reproducible local checks and reviewed hashes. No production validation claim.
Forbidden: No remote connections or changes, secrets, purchases, source edits, provider changes, destructive actions, commits, descendant delegation. Preserve dirty files.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Lockout, effective SSH precedence, fresh admin/sudo/PTY and root denial tests reviewed.
- [ ] Directory, MySQL include/start/stop, journal ordering and rollback gates reviewed.
Performance:
- [ ] No capacity guarantee from local/static checks.
Quality:
- [ ] Reproduce focused tests as appropriate; identify blockers without broad scope expansion.
- [ ] Distinguish MA observations from independently verified results.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- .claude/agents/pg.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
Tier 2:
- .agents/skills/create-wi-evidence-pack/SKILL.md
- docs/standards/evidence-pack-standard.md
- docs/index.md#current-decision-entry
- deploy/single-vps/CONFIGURATION.md
- deploy/single-vps/runbooks/first-host-setup.md
- deploy/single-vps/runbooks/host-hardening.md
- scripts/deployment/host-hardening/
- deploy/single-vps/mysql/atstudio.cnf
- deploy/single-vps/journald/atstudio.conf
Context:
- deliverables/user/REQ-20261010-ATS-007.md
- deliverables/agent/WI-20261010-ATS-018-evidence-pack.md
- deliverables/agent/WI-20261010-ATS-019-evidence-pack.md

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261010-ATS-020-summary.md
Agent-facing: deliverables/agent/WI-20261010-ATS-020-evidence-pack.md
Use injected evidence-pack skill. Exact reviewed hashes, findings, per-stage verdict, local commands, limits and rollback. Do not delay for MA remote application.

[TRACEABILITY REQUIREMENTS]
Evidence pointers and commands required. User has now reported recovery-console login after approved user-owned password reset. MA will verify live console; no remote configuration changed yet. Agent review never substitutes for MA fresh host gates. WI020 completion immediately triggers WI021 via MA.

