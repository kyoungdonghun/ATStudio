---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: work-summary
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-002.md
    reason: Approved local-only startup preparation
---

# WI-20261011-ATS-016 Handoff

[WI HEADER]
WI ID: WI-20261011-ATS-016
REQ: REQ-20261011-ATS-002
Agent: se
Depends On: approved REQ
Blocks: WI018, WI019

[WI SUMMARY]
Why: Add minimal property-controlled suppression of all automatic domain work while preserving existing default behavior.
Scope:
Own src/main/java and src/main/resources/application.yml plus focused src/test/java tests; no deploy/ or build scripts.
Inventory all @Scheduled, lifecycle hooks and private executors. Implement an explicit master background-work switch (use APP_BACKGROUND_TASKS_ENABLED / app.background-tasks.enabled unless current code provides a better existing seam); coordinate that exact name with MA. Default true preserves established behavior. False must suppress Spring scheduled registration, storage recovery ApplicationReadyEvent, and the audio readiness/private executor even if its legacy flag is true. Keep strict integrity and mandatory encryption-key validation active. Keep seed/bootstrap flags off via separate existing controls.
Avoid disabling whole services used by controllers if that breaks dependency injection; preserve manual-service semantics while suppressing only automatic entries. Find direct references before choosing conditions. Do not replace business logic. Test false/true/missing/invalid configuration and absence of side effects from readiness/scheduling. Focused mock/context tests must not start the full app or DB. Include coverage for future accidental @EnableScheduling regressions.
Run focused tests into a NEW build output directory if necessary; do not run bootJar or overwrite build/libs existing JAR. Read test skill. Record changes/tests in two-set outputs using evidence-pack skill.

[ACCEPTANCE CRITERIA]
Functional: explicit off mode covers automatic entries; existing production defaults and safety guards preserved.
Performance: no production sizing claims; no performance tuning.
Quality: focused local proof with exact bounds; no external side effects.

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
User-facing: deliverables/user/WI-20261011-ATS-016-summary.md
Agent-facing: deliverables/agent/WI-20261011-ATS-016-evidence-pack.md
Use create-wi-evidence-pack skill. Include injected context, commands/results,
changed paths, rollback (local proposed reversal only), limitations and next WI.

