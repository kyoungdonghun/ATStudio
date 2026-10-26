---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: reference
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved local validation with no existing data changes
  - path: WI-20261011-ATS-063-evidence-pack.md
    reason: Candidate requiring concrete concurrency evidence
---
# WI-20261011-ATS-066
[WI HEADER]
WI ID: WI-20261011-ATS-066
REQ: REQ-20261011-ATS-007
Agent: se
Depends On: WI063
Blocks: WI065 first-admin native readiness gate

[WI SUMMARY]
Why: Replace mocked transaction-only evidence with a bounded local disposable MySQL experiment.
Scope (in/out): Initially design/implement test harness under build/WI066-mysql and own two reports; do not start mysqld until MA checks the exact launcher. Reuse installed C:/Program Files/MySQL/MySQL Server 8.0/bin/mysqld.exe (8.0.45). No install, production/known local DB connection, private config, credential use, service registration or deletion. Fresh unique data/log directory under own build path only; explicit no-defaults; bind127.0.0.1 with a checked unused port, no X protocol, bounded lifetime, retain test files and stop only owned PID. Only synthetic schema/rows, no real account information.
DoD: Prepare isolated launcher with exact binary/path/PID containment and bounded stop; two independent JDBC transactions run real first-admin locking/DML with competing users, one winner and one audit, rollback and no second ADMIN. Test actual candidate transaction logic rather than reimplementing SQL. Native-release constants stay false; test seam must not make production entrypoint executable. Version8.0.45 versus server8.0.46 boundary must be explicit; no silent version check relaxation.
Constraints/Forbidden: No existing data directory/service/port, no secrets, no schema/data changes in any existing database, no external network, no password changes, no cleanup delete, no shared app-source edits, no subdelegation. No process start until MA launcher validation. Coordinate candidate fixes with WI063 ownership if requested by MA.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Before starting, independent file/process/port checks prove unique isolated target.
- [ ] Two actual connections exercise candidate lock/commit/rollback and audit atomicity.
- [ ] Owned process always stopped; artifacts preserved; ambiguous failures cannot report PASS.
Quality:
- [ ] Exact source/binary/version hashes and commands, counts and remaining target gaps.
- [ ] No hardcoded production endpoint or shared credentials in test path.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md
Tier 2:
- .agents/skills/test/SKILL.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
- docs/templates/wi-subagent-handoff-template.md
Context:
- deliverables/user/REQ-20261011-ATS-007.md
- deliverables/agent/WI-20261011-ATS-059-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-063-evidence-pack.md
- scripts/deployment/RuntimeFeatureProvisioner.java
- scripts/deployment/RuntimeFeatureProvisionerHarness.java
- src/main/resources/schema.sql

[OUTPUT CONTRACT]
User: deliverables/user/WI-20261011-ATS-066-summary.md
Agent: deliverables/agent/WI-20261011-ATS-066-evidence-pack.md
Skill-generated handoff: this file. No subdelegation.

[TRACEABILITY REQUIREMENTS]
Synthetic-only experiment; preserve all files. MA process-start gate before any database experiment. ATS; rule source .claude/config/context-injection-rules.json; Tier0 first.
