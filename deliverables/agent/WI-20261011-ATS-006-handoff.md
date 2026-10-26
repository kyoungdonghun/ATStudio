---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-001.md
    reason: Approved bounded release and schema verification
---

# WI-20261011-ATS-006: Release and Isolated Schema Verification

[WI HEADER]
WI ID: WI-20261011-ATS-006
REQ: REQ-20261011-ATS-001
Agent: se
Depends On: approved REQ
Blocks: WI-20261011-ATS-007

[WI SUMMARY]
Why: Close artifact/source identity and schema compatibility gaps without starting the application.
Scope: Local tool and tests, immutable release manifest/candidate; isolate Hibernate/entity validation using existing JAR libraries only. Native execution is MA-only, not authorized for SE.
DoD: Testable, reviewable fail-closed preparation, exact safe native sequence and bounded outcomes. Verify current source against WI026 artifact manifest including current frontendCompletion. No blind rebuild.
Constraints: No dependencies/install/download/network/SSH/browser/real DB/secret/private env access; no Spring application/component scan/schedulers/workers; no production or disposable DDL/DML, no prior bootstrap replay; preserve dirty/unrelated files and historical evidence. No commits/push/deletions/descendants.
Allowed writes: new scripts/deployment/release-readiness.py, RuntimeSchemaProbe.java, test_release_readiness.py and focused synthetic Java harness if needed; deploy/single-vps/runbooks/release-readiness.md; own summary/evidence; new uniquely named build outputs only. Do not alter application code, schema, old tools/manifests, REQ or index.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Hash/enumeration comparison of WI026 inputs, JAR and isolated SPA with public safe result. Report drift before packaging.
- [ ] Reuse existing JDBC guards/patterns and exact target, load only entity classes/converters and required JAR libraries for validate, no application lifecycle. Correct Spring/Hibernate naming/settings must match current app, not guessed defaults.
- [ ] Read-only structural observation with safe counts/digests of schema metadata, not rows or secrets. Clearly distinguish target snapshot from canonical fresh-schema proof and limit of Hibernate validate.
- [ ] Server env secret read only internally under fresh explicit approval; robust path/ownership/mode/ancestry and logging/output safeguards. No secret in command/env output/exception.
- [ ] Read-only JDBC sessions for every connection, no schema update/init, no reused production write-capable lifecycle. Probe timeout and no dumps/debugging, exact classpath/artifact identity.
- [ ] Native plan starts/stops only loopback MySQL temporarily and preserves live restricted Nginx/timer; no whole app startup or exposure.
Performance:
- [ ] Bounded execution/output/resources; no performance or capacity proof.
Quality:
- [ ] Focused local tests, Java17 compilation where available, sanitized fail branches, matching generated artifacts. Reuse standard libraries; no new abstraction framework.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- .claude/agents/se.md
- docs/policies/security-policy.md
Tier 2 / context:
- .agents/skills/create-wi-evidence-pack/SKILL.md
- .agents/skills/test/SKILL.md
- .agents/skills/build-check/SKILL.md
- docs/index.md#current-decision-entry
- deliverables/user/REQ-20261011-ATS-001.md
- deliverables/agent/WI-20261010-ATS-026-artifact.json
- deliverables/agent/WI-20261010-ATS-032-evidence-pack.md
- scripts/deployment/jdbc-readiness.py
- scripts/deployment/RuntimeJdbcProbe.java
- scripts/deployment/production-bootstrap.py
- deploy/single-vps/runbooks/production-readiness.md
- deploy/single-vps/runbooks/jdbc-readiness.md
- deploy/single-vps/env/atstudio.env.example
- docs/design/db-schema.md
- src/main/resources/application.yml
- src/main/java/com/atstudio/atstudio/entity/

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261011-ATS-006-summary.md
Agent-facing: deliverables/agent/WI-20261011-ATS-006-evidence-pack.md
Use create-wi-evidence-pack. Report exact changes/tests/hashes, safe run commands, known limits. If mapping validation requires a new architecture/dependency or risky workaround, stop that path and give smallest approval decision without making it.

[TRACEABILITY REQUIREMENTS]
Skill-generated packet from approved REQ, all four Tier0 documents injected by MA before role/context. Evidence owner/date/environment and native NOT_RUN explicit. Blocks WI007 immediately when complete.

