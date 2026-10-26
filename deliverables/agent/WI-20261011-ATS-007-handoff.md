---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-001.md
    reason: Approved release and read-only schema verification
  - path: WI-20261011-ATS-006-handoff.md
    reason: Implementation dependency; review starts after its completion
---

# WI-20261011-ATS-007: Independent Release Probe Review

[WI HEADER]
WI ID: WI-20261011-ATS-007
REQ: REQ-20261011-ATS-001
Agent: pg
Depends On: WI-20261011-ATS-006 completed candidate
Blocks: WI-20261011-ATS-008 final integration

[WI SUMMARY]
Why: Independently verify exact local candidate and MA-only native read-only procedure before secret use.
Scope: Review WI006 code/tests/pinned artifact, reproduce focused synthetic tests and mapping smoke without real DB; inspect native procedure and candidate assumptions.
DoD: PASS or precise blockers for exact hashes and steps, not native proof.
Constraints: No implementation changes, network/SSH/browser/DB/native actions, private env or secrets, installs/dependencies, source rebuild, reset/delete, commit/push or descendants. Preserve dirty tree. Write only own two deliverables; unique build test outputs allowed.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Exact source/artifact enumeration does not silently miss deployable source; pinned JAR/libs/model only. Current manifest frontendCompletion used; excluded inputs documented.
- [ ] Correct existing Boot naming/config and Hibernate metadata-only validation, no SessionFactory/SpringApplication/component/scheduler/worker initialization. Audit entity listeners/static effects/dependency service loading.
- [ ] Every DB connection server-side read-only, exact host/account/DB/server UUID, no DDL/DML/secret SQL; only approved schema metadata and bounded aggregates.
- [ ] Secrets only from exact root-private file into internal child stdin, no argv/env/output/log/error leak; logging/dump controls and privileged-inspection limits explicit.
- [ ] Native path checks, approval TTL/context/hash binding, bounded classpath/time/output, cleanup on failure/interruption.
- [ ] Actual restricted TLS/timer remain running; old host_guard requiring Nginx off not reused incorrectly. New manual/operator checks are real gates, not fabricated approval booleans.
- [ ] MA pre/post old trusted database_guard(fresh=False), grants, schema/plan observations compared safely; source/helper pins and root-only metadata verified.
- [ ] Script success is NOT app runnable/canonical fresh manifest/full mapping behavior/capacity/general production readiness.
Quality:
- [ ] Independently reproduce focused Python/Java synthetic failures and mapping smoke; no real connection.
- [ ] Review exact staging, command, guard and finally cleanup sequence, then provide actionable bounded PASS/HOLD.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- .claude/agents/pg.md
- docs/policies/security-policy.md
Tier 2:
- .agents/skills/test/SKILL.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
- docs/index.md#current-decision-entry
- deliverables/user/REQ-20261011-ATS-001.md
- deliverables/agent/WI-20261011-ATS-006-evidence-pack.md
- scripts/deployment/release-readiness.py
- scripts/deployment/RuntimeSchemaProbe.java
- scripts/deployment/test_release_readiness.py
- scripts/deployment/jdbc-readiness.py
- scripts/deployment/production-bootstrap.py
- deploy/single-vps/runbooks/release-readiness.md
- deploy/single-vps/runbooks/production-readiness.md
- deliverables/agent/WI-20261010-ATS-026-artifact.json

[OUTPUT CONTRACT]
User: deliverables/user/WI-20261011-ATS-007-summary.md
Agent: deliverables/agent/WI-20261011-ATS-007-evidence-pack.md
Use create-wi-evidence-pack. Findings first, file/line evidence, exact hashes/tests and native commands approved or remaining blocking conditions. No generic full-security/CVE claim.

[TRACEABILITY REQUIREMENTS]
All four Tier0 documents injected in order before role/security/context. Skill-generated from approved REQ. MA-only runtime observations stay separately attributed. Immediately notify MA on critical blocker; no unilateral fixes. WI008 follows completion including HOLD documentation.

