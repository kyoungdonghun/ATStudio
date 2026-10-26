---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: reference
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved scoped functional preparation and autonomous repairs
  - path: WI-20261011-ATS-059-evidence-pack.md
    reason: Selected audio-only suppression design
---
# WI-20261011-ATS-064
[WI HEADER]
WI ID: WI-20261011-ATS-064
REQ: REQ-20261011-ATS-007
Agent: se
Depends On: WI059 design; MA release of current source identity gate before application
Blocks: feature independent review and fresh JAR build

[WI SUMMARY]
Why: Prepare audio conversion-only startup while keeping scheduling and ready-event recovery suppressed.
Scope (in/out): Initially own build/WI064-audio-only/ patch/proposal/test plan and two reports only. Read current source; create a minimal applyable patch but DO NOT apply to src before explicit MA signal after WI060 native trial. Proposed later source ownership is WI059 SE-A: BackgroundTasksEnabledCondition.java, BackgroundTasksConfiguration.java, new ScheduledBackgroundTasksEnabledCondition.java, StorageMutationRecoveryService.java, application.yml, corresponding config/storage tests and new TrackAudioProcessingWorkerIsolationTest.java. No worker algorithm change, frontend, deployment tools or unrelated source.
DoD: Default-off audio-only flag with strict validation; master false still disables all; true/audio-only suppresses six schedules and storage ready recovery but permits existing audio worker. Preserve compatibility and guards. Prepare tests for truth table and malformed config. After MA signal, apply reviewed bounded patch and run isolated focused Gradle tests under build/WI064-* without old output overwrite.
Constraints/Forbidden: No source writes before MA signal; no secrets, private environment files, server, DB, mail, credentials, new dependency, commit/push, deletion, or subdelegation. Preserve existing dirty source. No claim that global queue is row-scoped; retain zero prior queue/debt prerequisite. No new JAR build until independent review gate.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Default behavior unchanged; master stop and strict guards preserved.
- [ ] Audio-only has no scheduling postprocessor/ready storage recovery; invalid values fail even with master false.
- [ ] Global audio queue prerequisite and eight-journal success prediction addressed with focused tests or precise remaining evidence.
Performance:
- [ ] Reuse one existing worker/executor; no new scheduling framework.
Quality:
- [ ] Minimal patch with source preimage hashes, focused test plan, application gate explicit.
- [ ] Isolated test process receives only explicit nonsecret environment; old artifacts unchanged.

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
- src/main/java/com/atstudio/atstudio/config/BackgroundTasksConfiguration.java
- src/main/java/com/atstudio/atstudio/config/BackgroundTasksEnabledCondition.java
- src/main/java/com/atstudio/atstudio/service/audio/TrackAudioProcessingWorker.java
- src/main/java/com/atstudio/atstudio/service/storage/StorageMutationRecoveryService.java
- src/main/resources/application.yml

[OUTPUT CONTRACT]
User: deliverables/user/WI-20261011-ATS-064-summary.md
Agent: deliverables/agent/WI-20261011-ATS-064-evidence-pack.md
Skill-generated handoff: this file. No subdelegation. Shared workspace and disjoint ownership.

[TRACEABILITY REQUIREMENTS]
Use source preimage hashes; preserve historical evidence. Report exact changes, tests, limitations and next gate. Source .claude/config/context-injection-rules.json; ATS, Tier0 first. Initial completion is PATCH_READY_ONLY, not source applied or native readiness.
