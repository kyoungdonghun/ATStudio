---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-001.md
    reason: Approved exact v5 native procedure preparation
  - path: WI-20261011-ATS-012-evidence-pack.md
    reason: Immutable v5 candidate review
---

# WI-20261011-ATS-013: Exact v5 Lifecycle Procedure

[WI HEADER]
WI ID: WI-20261011-ATS-013
REQ: REQ-20261011-ATS-001
Agent: se
Depends On: WI-20261011-ATS-012, fresh user approval
Blocks: WI-20261011-ATS-014

[WI SUMMARY]
Why: The preserved revision3 operator binds v4 and cannot execute the reviewed v5.
Scope: Prepare a new exact v5 operator and offline mocked regression checks in build/WI013-native-v5. Preserve original operator and all candidate files. Specify exact staging and invocation. MA handles access and target execution.
DoD: Exact public hashes, explicit narrow diff from rev3, cleanup tests, two-set evidence, PG handoff. Native remains NOT_RUN.
Constraints: No SSH, remote actions, service/agent changes, secrets, DB, browser, installation, commit/push, deletion or candidate regeneration. Do not change wrapper or its WI012 approval binding. Do not weaken any guard. Unexpected blocker requires MA escalation.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Bind /root/wi011, build/WI011-schema-v5, exact reviewed v5 bundle/wrapper and WI012 approval/check.
- [ ] Preserve signal-aware owned process-group cleanup, stop and independent mask, historical identity, root-only fresh approval and restricted TLS guards.
- [ ] Exact delta is limited and justified; legacy source is unchanged.
Performance:
- [ ] Keep bounded subprocess timeouts, child deadline and output; no full app startup.
Quality:
- [ ] Offline tests exercise success, rejection, timeout, stop failure/timeout with mask still attempted and nonzero HOLD.
- [ ] Provide exact commands and hashes for PG, plus any known process/signal test limitations.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
- docs/policies/access-control-policy.md
Tier 2:
- deploy/single-vps/runbooks/release-readiness.md
- .agents/skills/test/SKILL.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
Context and files:
- deliverables/user/REQ-20261011-ATS-001.md
- deliverables/agent/WI-20261011-ATS-010-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-012-evidence-pack.md
- build/WI010-pg-20261011-0210/ma-native-operator-revision3.py
- build/WI011-schema-v5/artifact.json
- scripts/deployment/release-readiness.py

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261011-ATS-013-summary.md
Agent-facing: deliverables/agent/WI-20261011-ATS-013-evidence-pack.md
Code/tests: build/WI013-native-v5/ only, unused output names, apply_patch for manual edits.
Handoff: This packet, generated using create-wi-handoff-packet.

[TRACEABILITY REQUIREMENTS]
Record exact file/hash/commands, synthetic test evidence and rollback (do not run new operator). Preserve historical files. Return completed WI013 so MA immediately triggers WI014. No independent follow-up delegation.
