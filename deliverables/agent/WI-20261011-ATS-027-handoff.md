---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: active
dependencies:
  - path: WI-20261011-ATS-026-handoff.md
    reason: Exact repaired bundle review scope
  - path: ../user/REQ-20261011-ATS-003.md
    reason: Approved independent review and fresh one-attempt scope
---

[WI HEADER]
WI ID: WI-20261011-ATS-027
REQ: REQ-20261011-ATS-003
Agent: pg
Depends On: WI026 local completion and exact freeze
Blocks: MA new one-attempt native test; WI028 evidence closure

[WI SUMMARY]
Why: Independently gate the narrowly repaired one-shot helper following WI024 PREPARE/PATH_ANCESTRY HOLD.
Scope (in/out): Review fresh WI026 namespace/log path, exact staging/approval/invocation, tests, historical-byte preservation, unchanged containment and cleanup. Local inert verification only. Do not SSH, read secrets, install, start services, modify implementation or author native approvals.
DoD: Exact ALLOW/HOLD verdict, pins including README and 3 shell blocks, independent focused regressions, limitation and MA conditions, evidence pack/summary.
Constraints/Forbidden: New user approval permits one fresh attempt after review, not reuse of WI024 claim. Old /var/log permissions and failures preserved. No broad refactor or weakened security. Source JAR unchanged.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Log ancestry now /var/lib/atstudio/wi026-private-logs with exact root0700 leaf; actual root0755 parent assumptions valid.
- [ ] No cross-namespace accidental execution/overwrite, including unit/cgroup/mask and README.
- [ ] Old /var/log0775 remains rejected. Symlink/group-writable checks unchanged.
- [ ] All inherited security/DB/GET/time/cleanup tests remain valid; no change to secret model.
Performance:
- [ ] Maximum180s native start; one invocation; no retry.
Quality:
- [ ] Independently execute narrow inert tests and verify all frozen bytes.
- [ ] Review exact shell blocks and upload identity recipe, with fresh seven operator attestations.
- [ ] Clearly local PASS, native NOT_RUN and internet NOT_RUN.

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
- docs/standards/evidence-pack-standard.md
- .agents/skills/test/SKILL.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
Context:
- deliverables/user/REQ-20261011-ATS-003.md
- deliverables/agent/WI-20261011-ATS-026-handoff.md
- deliverables/agent/WI-20261011-ATS-026-evidence-pack.md
- build/WI026-private-start/
- build/WI024-private-start/
- build/WI025-review/
- deliverables/agent/WI-20261011-ATS-025-evidence-pack.md
Native MA context:
- WI024 v2 consumed once 12:56:16 KST, PREPARE/PATH_ANCESTRY, app NOT_RUN, cleanup PASS. /var/log root:syslog0775. No service start. Historical upload/tools/approval/claim/empty run+log dirs remain.
- Fresh readonly metadata: /, /var, /var/lib, /var/lib/atstudio, /run, /run/systemd, /run/systemd/system, /etc, /opt, /opt/atstudio, /opt/atstudio/releases root:root0755; /etc/atstudio root:root0700.
- 13:03:30KST source641/641 and JAR pin unchanged. SSH agent Running/Manual one owned key; MySQL inactive/masked; app units absent. No native permission weakening.
- User explicitly approved narrow repair, independent review, and fresh one-attempt private start. Existing bounded secret use remains in scope.

[OUTPUT CONTRACT]
Implementation is read-only.
Independent tests/results -> build/WI027-review/
User-facing -> deliverables/user/WI-20261011-ATS-027-summary.md
Agent-facing -> deliverables/agent/WI-20261011-ATS-027-evidence-pack.md
Return exact gate and hashes plus MA conditions; notify MA immediately for any genuine blocker. No commit/push.

[TRACEABILITY REQUIREMENTS]
Use create-wi-evidence-pack; preserve all WI025 evidence. Only WI026 byte-specific verdict is current. A failure means HOLD and no automatic retry, not deleting old attempt artifacts.
