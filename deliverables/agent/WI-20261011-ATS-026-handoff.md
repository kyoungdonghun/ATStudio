---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: handoff
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-003.md
    reason: Approved narrow repair and fresh one-attempt scope
---

[WI HEADER]
WI ID: WI-20261011-ATS-026
REQ: REQ-20261011-ATS-003
Agent: sa
Depends On: WI024/WI025 historical HOLD; fresh user repair and one-attempt approval
Blocks: WI027

[WI SUMMARY]
Why: Native WI024 v2 stopped at PREPARE/PATH_ANCESTRY before any service start because /var/log is root:syslog 0775. Preserve that failed attempt and move the log to a secure parent without relaxing host permissions.
Scope (in/out): Clone the reviewed local helper into build/WI026-private-start. Change only the log location to /var/lib/atstudio/wi026-private-logs and fresh attempt namespaces, with consistent pins, exact staging/approval/run blocks and focused regression tests. Keep all WI024 artifacts unchanged. No server access, installation, secret access, native execution or app source changes.
DoD: Narrow auditable delta; regression tests including real parent metadata; new frozen identities; README exact commands; standardized evidence and summary.
Constraints/Forbidden: No security weakening; no chmod/chown of existing host parent directories; no WI024 claim reuse/removal; no retry loop; no broad redesign; all existing timeout, secret, BPF, DB read-only, endpoint and cleanup boundaries remain. Source JAR remains wi022-upload-20261011. MA alone performs native operations after independent approval.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Fresh WI026 upload/tools/approval/claim/run/release/log/unit namespace, consistent throughout helper, unit, tests and README.
- [ ] Log under /var/lib/atstudio/wi026-private-logs; root-only ancestry enforced.
- [ ] Regress /var/log root:syslog0775 rejection and supplied new parent metadata acceptance, plus group-writable/symlink rejection.
- [ ] Preserve old bytes and all prior tests/security properties.
Performance:
- [ ] One native app start, maximum180s; 5s/64KiB read-only GET bounds unchanged.
Quality:
- [ ] Tests PASS, compile, exact immutable bundle and command block pins.
- [ ] Independent WI027 required; local PASS never native PASS.

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
- deliverables/agent/WI-20261011-ATS-024-evidence-pack.md
- deliverables/agent/WI-20261011-ATS-025-evidence-pack.md
- build/WI024-private-start/
- build/WI025-review/
Native facts (MA observed 2026-10-11 KST):
- 12:56:16 WI024 result HOLD/PREPARE/PATH_ANCESTRY, app_start NOT_RUN, cleanup PASS. Only empty run/log directories and consumed claim were created after staging; unit/release/probe/log absent.
- /var/log root:syslog0775 caused strict ancestry rejection. Do not change it.
- /, /var, /var/lib, /var/lib/atstudio, /run, /run/systemd, /run/systemd/system, /opt, /opt/atstudio, /opt/atstudio/releases are root:root0755 directories. /etc root:root0755, /etc/atstudio root:root0700.
- MySQL inactive/masked, no app/DB/probe ports after HOLD.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20261011-ATS-026-summary.md
Agent-facing -> deliverables/agent/WI-20261011-ATS-026-evidence-pack.md
Handoff -> deliverables/agent/WI-20261011-ATS-026-handoff.md
Implementation -> build/WI026-private-start/ only
Return exact paths, delta/pins/tests, remaining limitations and next WI027. No commit/push.

[TRACEABILITY REQUIREMENTS]
Retain all WI024 failure artifacts. Cite reproducible tests, reviewed exact bytes and native-vs-local distinction. Rollback is not to execute this new helper, not to delete old evidence.
