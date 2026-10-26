---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: work-summary
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-002.md
    reason: Approved local preparation
---

# WI-20261011-ATS-019 Handoff

[WI HEADER]
WI ID: WI-20261011-ATS-019
REQ: REQ-20261011-ATS-002
Agent: re
Depends On: WI-20261011-ATS-016
Blocks: WI-20261011-ATS-020

[WI SUMMARY]
Why: independent evidence before new release packaging.
Scope: Independently verify WI016 changes using targeted JUnit tests for background suppression and neighboring scheduler/storage/audio/strict startup guard semantics. Only mock/local synthetic contexts; no full @SpringBootTest, DB service, retained data, private env or mail/payment calls. Read actual selected test sources before execution. Use offline cached Gradle, no-daemon, no clean/bootJar/build target; new isolated build/WI019-tests/ output via own build/WI019-* init script with apply_patch. Write only own evidence/summary and ephemeral verification helper/tests under build/WI019-*. Do not edit app source/existing tests. Never run Java in build/libs. Test counts and exact command/XML receipts; skipped/coverage not run explicit. Cross-check disabled sched registration+readiness/executor suppression, default/true behavior, invalid configuration rejection and guard preservation. Report source defects to MA, do not silently patch. Use test and evidence-pack skills. No subdelegation.

[ACCEPTANCE CRITERIA]
Functional: match changed behavior and exact candidate; preserve defaults and mandatory guards.
Performance: not measured; no supported-user or capacity claim.
Quality: recorded independent commands/results, clear coverage limits, immutable old artifacts.

[INPUT POINTERS]
Tier 0 (fully injected by MA):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/index.md (latest Current Decision Entry only)
- deploy/single-vps/CONFIGURATION.md
- deploy/single-vps/runbooks/production-readiness.md (dated; latest index supersedes)
- deliverables/user/REQ-20261011-ATS-002.md
- deliverables/agent/WI-20261011-ATS-015-evidence-pack.md
- deliverables/agent/WI-20261010-ATS-026-artifact.json

[CONSTRAINTS]
Local-only. No SSH, real secrets, private env reads, DB service, remote API,
full application start, package installation, purchase, commit/push or deletion.
Preserve existing dirty files and immutable prior artifacts. No sub-delegation.
Use apply_patch for manual edits. Report new blocker/scope change to MA.
Tests may use isolated synthetic configurations/mocks, not retained application
environments. No dependency upgrade. No runtime Spring guard bypass.
Document target tests as NOT RUN, not implied by local checks.

[OUTPUT CONTRACT]
Use evidence-pack skill; deliverables/agent/WI-20261011-ATS-019-evidence-pack.md and
 deliverables/user/WI-20261011-ATS-019-summary.md. Include exact tested hashes, commands, test
counts, findings/risks, no runtime claim and local rollback guidance.

