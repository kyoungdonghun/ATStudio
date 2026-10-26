---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: MA
category: handoff
status: active
dependencies:
  - path: ../user/REQ-20261010-ATS-007.md
    reason: Approved first-host scope and exact rebuild approval addendum
  - path: WI-20261010-ATS-016-evidence-pack.md
    reason: Completed local first-host preparation
---
# WI-20261010-ATS-017 Handoff

[WI HEADER]
WI ID: WI-20261010-ATS-017
REQ: REQ-20261010-ATS-007
Agent: pg
Depends On: WI-20261010-ATS-016
Blocks: WI-20261010-ATS-018

[WI SUMMARY]
Why: Independently review local first-host safety while MA performs authorized identity verification.
Scope (in): Review first-host runbook, especially exact-target public host pin, optional Windows agent start/load/remove/restore, Nginx no-start and MySQL initialization hold.
Scope (out): No browser, target connection, service mutation, credentials/private files, installation, repo implementation edits, purchase or deletion.
DoD: Findings with actionable locations; reviewed hashes and verification limits; explicit readiness per stage; two-set evidence.
Constraints: Preserve dirty work. Write only own summary/evidence. Do not wait for actual login to finish review. Send any blocking issue promptly. No descendants/commit.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Review native SSH validation and agent lifecycle for secret disclosure, wrong target and cleanup failure.
- [ ] Review package planning against REQ exclusions and post-rebuild evidence boundaries.
Performance:
- [ ] Keep scope targeted; no broad test suites or new tooling.
Quality:
- [ ] Reproduce narrow safe syntax/options checks as useful; never execute login/service snippets as tests.
- [ ] Distinguish review PASS from target execution NOT RUN.
- [ ] Immediately return result for WI018 chain.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- .claude/agents/pg.md
- docs/policies/security-policy.md
Tier 2:
- deliverables/user/REQ-20261010-ATS-007.md
- deliverables/agent/WI-20261010-ATS-016-evidence-pack.md
- deploy/single-vps/runbooks/first-host-setup.md
- deploy/single-vps/CONFIGURATION.md
- .agents/skills/create-wi-evidence-pack/SKILL.md

[MA SNAPSHOT]
At 19:42 KST user explicitly approved irreversible Ubuntu24.04 rebuild of sole existing server, no data, displayed additional0KRW. MA clicked once; provider returned success, then operating status. Same approved firewall remains ON. New recovery console opened and user reports console login complete. Old host observation invalidated; new independent fingerprint comparison pending. Windows agent remains Disabled/Stopped, temporary activation approved but not executed. No SSH login/package/app/DB/backup/domain action yet. Do not record raw endpoint/account/IP/fingerprint or console token.

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261010-ATS-017-summary.md
Agent-facing: deliverables/agent/WI-20261010-ATS-017-evidence-pack.md
Use create-wi-evidence-pack, mandatory evidence pointers and owned scope.

[TRACEABILITY REQUIREMENTS]
Record source hashes, commands, findings, execution limits and next WI018. Review has no remote rollback; withhold unsafe procedures. Preserve historical evidence.

