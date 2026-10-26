---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: reference
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved local preparation for designated functional tests
  - path: WI-20261011-ATS-059-evidence-pack.md
    reason: Bounded implementation design
---
# WI-20261011-ATS-063
[WI HEADER]
WI ID: WI-20261011-ATS-063
REQ: REQ-20261011-ATS-007
Agent: se
Depends On: WI059 design
Blocks: independent feature review and MA functional trial

[WI SUMMARY]
Why: Prepare minimal first-admin provisioning and tiny media fixtures while BPF testing proceeds.
Scope (in/out): Local files only: scripts/deployment/feature-provisioning.py, RuntimeFeatureProvisioner.java, test_feature_provisioning.py, RuntimeFeatureProvisionerHarness.java; scripts/validation/feature-trial/fixtures.py and test_fixtures.py; own reports and build/WI063-* generated tests. No app source, existing deployment tools, private files, server, production DB, or source identity inputs changed.
DoD: Implement WI059 first-admin inspect/promote operation only, not buyer grants yet. Promotion requires exactly designated existing verified USER, zero ADMIN, fenced stopped writers, private approval/target/pins/one-use receipt and transaction-safe one row plus audit. No account creation/password/consent invention. Deterministic two 10s WAV fixtures with no overwrite and tests. Ready for independent review, native NOT RUN.
Constraints/Forbidden: No secrets/PII in code/reports, no service/DB/native commands, no file deletion, no commit/push, no old artifact modification, no new framework. Read-only/in-memory mocks do not establish MySQL concurrency; disclose remaining real-MySQL proof.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Strict framing, identity/metadata, scope and absent/present/drift/replay branches.
- [ ] Atomic promotion/audit with no arbitrary SQL/DDL/other mutations; no raw errors.
- [ ] Fixed two WAVs, bytes/hash/duration checks, path traversal/overwrite refusal.
Performance:
- [ ] Tiny bounded fixtures and process/IO deadlines.
Quality:
- [ ] Tests, Java compile/harness if available, failures and native limits recorded.
- [ ] Existing probes unchanged; no root source changes while web artifact identity is used.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md
Tier 2:
- .agents/skills/test/SKILL.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
- docs/templates/eval-report-template.md
- docs/templates/wi-subagent-handoff-template.md
Context:
- deliverables/user/REQ-20261011-ATS-007.md
- deliverables/agent/WI-20261011-ATS-059-evidence-pack.md
- scripts/deployment/RuntimeJdbcProbe.java
- scripts/deployment/jdbc-readiness.py
- scripts/deployment/production-bootstrap.py
- src/main/java/com/atstudio/atstudio/service/AdminOperationAuditService.java
- src/main/resources/schema.sql

[OUTPUT CONTRACT]
User: deliverables/user/WI-20261011-ATS-063-summary.md
Agent: deliverables/agent/WI-20261011-ATS-063-evidence-pack.md
Skill-generated handoff: this file. No subdelegation. Shared workspace, disjoint ownership.

[TRACEABILITY REQUIREMENTS]
Exact changed paths, pins, reproducible tests, rollback and independent review next gate. Rule source .claude/config/context-injection-rules.json; ATS project; Tier0 first.

