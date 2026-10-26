---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-001.md
    reason: User-approved PG-007-01 correction and re-review
  - path: WI-20261011-ATS-007-evidence-pack.md
    reason: Exact independently reproduced blocker and regression gap
---

# WI-20261011-ATS-009: Correct Isolated Validator Version Types

[WI HEADER]
WI ID: WI-20261011-ATS-009
REQ: REQ-20261011-ATS-001
Agent: se
Depends On: WI007 HOLD and explicit user approval of correction
Blocks: WI-20261011-ATS-010 independent re-review, then WI008 integration

[WI SUMMARY]
Why: v3 passes mapping smoke but actual Hibernate validator casts Integer version values to String and fails.
Scope: Correct exactly the two settings types, add bounded actual-validator synthetic regression, prepare a fresh v4 candidate and update its runbook identity.
DoD: Real validator succeeds against matching synthetic metadata and rejects missing table/column without real driver/DB; new exact hashes plus focused suite PASS.
Constraints: Local only. No app/schema/data/config/secret/SSH/browser/network/remote/install actions, new dependencies, commit/push/reset/delete, descendants or unrelated edits. Preserve v3 and old WI006/007 evidence. No native PASS claim.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Correct database-major/minor-version values for bundled Hibernate7.2.24 to strings without changing application source.
- [ ] Add real Hibernate SchemaValidator regression beyond existing routing double, exclude Connector/J and real sockets.
- [ ] Preserve read-only identity guards, bounded output/process and no Spring/app lifecycle.
- [ ] Fresh unused build/WI009-schema-v4 output; exact input/candidate hashes and prior exclusions recorded.
- [ ] Runbook reflects latest candidate and PG010 approval binding. Update wrapper review_wi only if required, test old WI007 rejection and new WI010 acceptance. Keep historical helper unchanged.
- [ ] Native lifecycle guidance includes signal-aware child process group cleanup, independent stop/mask and recovery path per PG.
Quality:
- [ ] Focused new and reused suites pass; actual-candidate no-connect mapping smoke; reproduce actual-validator regression.
- [ ] No broad full build/source changes. Direct whitespace/doc checks include new files.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- .claude/agents/se.md
- docs/policies/security-policy.md
Tier 2:
- .agents/skills/test/SKILL.md
- .agents/skills/build-check/SKILL.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
- deliverables/user/REQ-20261011-ATS-001.md
- deliverables/agent/WI-20261011-ATS-007-evidence-pack.md
- scripts/deployment/RuntimeSchemaProbe.java
- scripts/deployment/RuntimeSchemaProbeHarness.java
- scripts/deployment/release-readiness.py
- scripts/deployment/test_release_readiness.py
- build/WI007-pg-20261011-0153/ActualValidatorHarness.java
- build/WI007-pg-20261011-0153/reproduce_validator.py
- deploy/single-vps/runbooks/release-readiness.md

[OUTPUT CONTRACT]
Allowed implementation edits: only four listed new WI006 helper/test Java/Python files (not old JDBC helper), focused extra synthetic Java harness if necessary, release-readiness runbook and uniquely named build outputs.
Own deliverables: deliverables/user/WI-20261011-ATS-009-summary.md and deliverables/agent/WI-20261011-ATS-009-evidence-pack.md.
Do not change REQ/index/other handoffs/old evidence.
Use create-wi-evidence-pack and exact source lines/hashes/commands/results. Promptly return material blocker; no target operation.

[TRACEABILITY REQUIREMENTS]
Skill-generated packet from approved REQ plus user answer "검증 도구 수정·재검토 후 계속 승인". Existing injected Tier0/security and SE role still govern, with PG007 evidence injected at re-delegation. WI009 immediately triggers WI010; WI008 follows final review and MA outcome/cleanup.

